package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.commands.args.Resolver;
import ru.komiss77.events.RandomTpFindEvent;
import ru.komiss77.hook.WGhook;
import ru.komiss77.modules.DelayTeleport;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.modules.world.LocFinder;
import ru.komiss77.modules.world.WXYZ;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;


public class TprCmd implements OCommand {

    private static final int DST = 0;

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String player = "player", world = "world", dist = "distance";
        return Commands.literal("tpr").executes(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof final Player pl)) {
                cs.sendMessage("§cУкажи ник игрока!");
                return 0;
            }

            return perform(cs, pl, pl.getWorld(), DST);
        }).then(Resolver.player(player)
            .executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                final Player pl = Resolver.player(cntx, player);
                return perform(cs, pl, pl.getWorld(), DST);
            }).then(Resolver.world(world).executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                final Player pl = Resolver.player(cntx, player);
                return perform(cs, pl, Resolver.world(cntx, world), DST);
            }).then(Resolver.integer(dist, 0).executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                final Player pl = Resolver.player(cntx, player);
                return perform(cs, pl, Resolver.world(cntx, world), Resolver.integer(cntx, dist));
            })))
        ).build();
    }

    private int perform(final CommandSender cs, final Player p, final World in, final int dst) {
        final boolean force = ApiOstrov.isLocalBuilder(cs);

        if (cs instanceof final Player sp) {
            if (sp.getEntityId() != p.getEntityId()) {
                cs.sendMessage("§cТолько консоль может тп других игроков!");
                return 0;
            }
        }

        if (Timer.has(p, "tpr")) {
            cs.sendMessage("§8" + Lang.t(p, "Телепортер перезаряжается! Осталось: ") + Timer.getLeft(p, "tpr_command") + " сек.!");
            return 0;
        }

        if (!force) Timer.add(p, "tpr", Config.tpr_command_delay);
        rtp(p, in, dst, force, pl -> pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 5));

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public List<String> aliases() {
        return List.of("тпр", "rtp", "ртп");
    }

    @Override
    public String description() {
        return "Присмотр сущностей";
    }

    private static final HashMap<String, BukkitTask> tpData = new HashMap<>();
    private static final int TRY_PER_TICK = 3;

    public static void rtp(final Player p, final World world, final int radiusLimit, final boolean force, final Consumer<Player> onDone) {

        if (tpData.containsKey(p.getName())) {
            p.sendMessage("§c" + Lang.t(p, "Для Вас уже ищется место для телепорта!"));
            return;
        }

        /*
        Это устанавливает максимально возможный размер в блоках, выраженный в радиусе, который может получить мировая граница.
        Установка большей границы мира приводит к успешному выполнению команд,
        но фактическая граница не выходит за пределы этого ограничения блока.
        Установка max-world-size выше значения по умолчанию, похоже, ничего не дает.
        Установка max-world-size на 1000 позволяет игроку иметь границу мира 2000 × 2000.
        Установка max-world-size на 4000 дает игроку границу мира 8000 × 8000.
        https://minecraft.fandom.com/wiki/World_border
        */
        //вычисляем максимум +/- для x,z - РАДИУС!!!
        //для каждой команды все параметры внутри, или могут запускать в разных мирах и подменятся параметры!

        final int center_x = world.getWorldBorder().getCenter().getBlockX();
        final int center_z = world.getWorldBorder().getCenter().getBlockZ();

        final int worldDiameter = (int) world.getWorldBorder().getSize() < Bukkit.getServer().getMaxWorldSize() ? ((int) world.getWorldBorder().getSize()) : Bukkit.getServer().getMaxWorldSize();//VM.getNmsServer().getMaxWorldSize(world);//propertyManager.getInt("max-world-size", 500);
        //вычисляем минимум +/- для x,z
        final int min = (worldDiameter / 2) / 50; //при мире 5к даст для поиска - дельтф будет +/- ( рандом от min до max)100, при 500 даст 10
        int max = worldDiameter / 2 - min;  // - min чтобы не прижимало к границе

        final int minFindRadius = radiusLimit > 1 && min > radiusLimit ? radiusLimit : min;
        final int maxFindRadius = radiusLimit > 1 && radiusLimit < max ? radiusLimit : max;

        tpData.put(p.getName(), new BukkitRunnable() {

            final int xMax = center_x + maxFindRadius;
            final int xMin = center_x - maxFindRadius;
            final int zMax = center_z + maxFindRadius;
            final int zMin = center_z - maxFindRadius;

            //System.out.println("xMax="+ xMax+" xMin="+ xMin+" zMax="+ zMax+" zMin="+ zMin);
            final int x = p.getLocation().getBlockX();
            final int y = p.getLocation().getBlockY();
            final int z = p.getLocation().getBlockZ();
            final int maxY = p.getWorld().getEnvironment() == World.Environment.NETHER ? 125 : p.getWorld().getMaxHeight() - 2;

            final String name = p.getName();
            int find_try = 100; //если делать меньше, то изменить ниже Поиск места: §3"+(100-find_try)+"%
            int tryPerTick = TRY_PER_TICK;
            //int find_x, find_z, feet_y;
            final WXYZ feetLoc = new WXYZ(p.getLocation());//Location(world, 0, 0, 0);

            @Override
            public void run() {

                if (!p.isOnline() || p.isDead()) {
                    this.cancel();
                    tpData.remove(name);
                    return;
                }

                if (!force) {
                    if (p.getLocation().getBlockX() != x || p.getLocation().getBlockY() != y || p.getLocation().getBlockZ() != z) {
                        ApiOstrov.sendActionBarDirect(p, "§c" + Lang.t(p, "ТП отменяется!"));
                        this.cancel();
                        tpData.remove(name);
                        return;
                    }
                }

                if (find_try == 0) {

                    done();

                } else {

                    if (force) {
                        ApiOstrov.sendActionBarDirect(p, "§8" + Lang.t(p, "Поиск места") + ": §3" + (100 - find_try) + "%");
                    } else {
                        ApiOstrov.sendActionBarDirect(p, "§e" + Lang.t(p, "Сохраняйте неподвижность, ищем!") + " §b" + (100 - find_try) + "%");
                    }

                }


                tryPerTick = TRY_PER_TICK;

                for (; tryPerTick > 0; tryPerTick--) {

                    feetLoc.x = Ostrov.random.nextBoolean() ? ApiOstrov.randInt(center_x + minFindRadius, xMax) : ApiOstrov.randInt(xMin, center_x - minFindRadius);
                    feetLoc.z = Ostrov.random.nextBoolean() ? ApiOstrov.randInt(center_z + minFindRadius, zMax) : ApiOstrov.randInt(zMin, center_z - minFindRadius);

                    feetLoc.y = maxY;
                    final WXYZ lc = new LocFinder(feetLoc).find(true, 1, 1);
                    if (lc == null) continue;
                    feetLoc.x = lc.x;
                    feetLoc.y = lc.y;
                    feetLoc.z = lc.z;

                    if (!new RandomTpFindEvent(p, feetLoc).callEvent()) {
                        continue;
                    }

                    if (Ostrov.wg && !WGhook.canBuild(p, feetLoc.getCenterLoc())) {
                        continue;
                    }

                    done();
                    return;
                }


                find_try--;


            }


            private void done() {
                this.cancel();
                tpData.remove(name);

                if (find_try == 0) {
                    if (force) {
                        p.sendMessage("§b" + Lang.t(p, "Это лучшее, что мы смогли найти.."));
                    } else {
                        p.sendMessage("§b" + Lang.t(p, "Телепортер не смог найти подходящее место! Попробуйте позже.."));
                        return;
                    }
                }
                if (force) {
                    ApiOstrov.teleportSave(p, feetLoc.getCenterLoc(), true);
                } else {
                    DelayTeleport.tp(p, feetLoc.getCenterLoc(), 3, Lang.t(p, "Вы в рандомной локации."), true, true, DyeColor.WHITE);
                }
                if (onDone != null) {
                    onDone.accept(p);
                }
            }


        }.runTaskTimer(Ostrov.instance, 1, 1));

    }

}