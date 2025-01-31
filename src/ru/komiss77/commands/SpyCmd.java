package ru.komiss77.commands;

import java.util.ArrayList;
import java.util.List;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.inventory.*;


public class SpyCmd implements OCommand {

    //запрос банжи, если есть - разкодировать raw
    //если пустой - выкачать из снапшота БД

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String nameArg = "nameArg";
        return Commands.literal("spy")
            .executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player p)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                final Oplayer op = PM.getOplayer(p);
                if (!op.isStaff && !ApiOstrov.canBeBuilder(p)) {
                    p.sendMessage("§cДоступно только персоналу!");
                    return 0;
                }

                SpyMenu.open(p);
                return Command.SINGLE_SUCCESS;
            })
            .then(Resolver.player(nameArg).executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player p)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                final Oplayer op = PM.getOplayer(p);
                if (!op.isStaff && !ApiOstrov.canBeBuilder(p)) {
                    p.sendMessage("§cДоступно только персоналу!");
                    return 0;
                }

                final Player tgt = Resolver.player(cntx, nameArg);
                if (tgt == null) {
                    p.sendMessage(Ostrov.PREFIX + "§cТакой игрок не онлайн");
                    return 0;
                }

                if (op.spyOrigin != null) {
                    cs.sendMessage("§cСначала закончите текущее наблюдение!");
                    return 0;
                }
                final String targetName = tgt.getName();
                if (p.getName().equals(targetName)) {
                    cs.sendMessage("§cЗа собой следить не получится!");
                    return 0;
                }

                //if (tgt.getGameMode() == GameMode.SPECTATOR) {
                //    cs.sendMessage("§c" + tgt.getName() + " в режиме зрителя!");
                //    return 0;
                //}

                op.spyOrigin = p.getLocation();
                p.setGameMode(GameMode.SPECTATOR);
                p.teleport(tgt);
                //p.setSpectatorTarget(tgt);
                tgt.hidePlayer(Ostrov.instance, p);
                op.tag.visible(false);
                //final String nameArg = tgt.getName();

                new BukkitRunnable() {
                    final GameMode oldGamemode = p.getGameMode();

                    @Override
                    public void run() {
                        if (!p.isOnline()) {
                            this.cancel();
                            return;
                        }
                        final Player t = Bukkit.getPlayerExact(targetName); //обновлять каждый раз, после гибели плеер обновляется

                        if (p.isDead() || p.getGameMode() != GameMode.SPECTATOR) {
                            end();
                        } else if (t == null || !t.isOnline()) {
                            p.sendMessage("§6Цель наблюдения покинула сервер.");
                            end();
                            //} else if (t.getGameMode() == GameMode.SPECTATOR) {
                            //    p.sendMessage("§6Цель наблюдения перешла в режим SPECTATOR.");
                            //     end();
                        } else if (t.isDead()) {
                            ScreenUtil.sendActionBarDirect(p, "§7ЛКМ - меню наблюдения  §6Ожидаем возрождение цели...");
                        } else {
                            //if (p.getSpectatorTarget() == null || !p.getSpectatorTarget().getName().equals(targetName) ) {
                            //    p.setSpectatorTarget(tgt);
                            //}
                            final int distance = LocUtil.getDistance(p.getLocation(), t.getLocation());
                            if (distance > 20) {
                                p.teleport(t);
                            } else if (distance > 15) {
                                ScreenUtil.sendActionBarDirect(p, "§7ЛКМ - меню наблюдения, дистанция §4" + distance);
                            }
                            if (distance > 10) {
                                ScreenUtil.sendActionBarDirect(p, "§7ЛКМ - меню наблюдения, дистанция §6" + distance);
                            } else {
                                ScreenUtil.sendActionBarDirect(p, "§7ЛКМ - меню наблюдения, дистанция §a" + distance);
                            }
                        }
                    }

                    private void end() {
                        //if (p.getGameMode() == GameMode.SPECTATOR) {
                        //p.setSpectatorTarget(null);
                        //}
                        p.teleport(op.spyOrigin == null ? p.getLocation() : op.spyOrigin);
                        op.spyOrigin = null;
                        Ostrov.sync(() -> p.setGameMode(oldGamemode), 1);
                        tgt.showPlayer(Ostrov.instance, p);
                        //p.resetTitle();
                        op.tag.visible(true);
                        this.cancel();
                    }

                }.runTaskTimer(Ostrov.instance, 3, 11);
                return Command.SINGLE_SUCCESS;
            }))
            .build();
    }

    @Override
    public List<String> aliases() {
        return List.of(""); //альясы на кирилице не робят
    }

    @Override
    public String description() {
        return "Слежка за игроками";
    }


    public static class SpyMenu implements InventoryProvider {

        private final ItemStack fill = new ItemBuilder(ItemType.GREEN_STAINED_GLASS_PANE).name("§8.").build();

        public static void open(Player p) {
            SmartInventory.builder()
                .id("SpyMenu" + p.getName())
                .provider(new SpyMenu())
                .size(6, 9)
                .title("§5За кем следим?")
                .build().open(p);
        }

        @Override
        public void init(final Player p, final InventoryContent contents) {
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, .5f, 5);
            contents.fillBorders(ClickableItem.empty(fill));

            final Oplayer op = PM.getOplayer(p.getUniqueId());

            if (op.spyOrigin != null) {

               /*contents.set(2, 3, ClickableItem.of(new ItemBuilder(ItemType.REDSTONE)
                    .name("§fТП к цели")
                    .build(), e -> {
                    if (e.isLeftClick()) {
                        p.setGameMode(GameMode.CREATIVE); //гм не совпадает - таймер отменит наблюдение сам
                        Ostrov.sync( () -> reopen(p, contents), 2); //откроет для выбора новой цели
                    }
                }));*/
                contents.set(2, 4, ClickableItem.of(new ItemBuilder(ItemType.REDSTONE)
                    .name("§6Закончить текущее наблюдение")
                    .build(), e -> {
                    if (e.isLeftClick()) {
                        p.setGameMode(GameMode.CREATIVE); //гм не совпадает - таймер отменит наблюдение сам
                        Ostrov.sync(() -> reopen(p, contents), 2); //откроет для выбора новой цели
                    }
                }));


                return;
            }

            final Pagination pagination = contents.pagination();
            final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

            for (final Player pl : Bukkit.getOnlinePlayers()) {

                if (pl.getName().equals(p.getName()) || pl.getGameMode() == GameMode.SPECTATOR || pl.hasPermission("ostrov.spy"))
                    continue;

                final ItemStack icon = new ItemBuilder(ItemType.PLAYER_HEAD)
                    .name("§f" + pl.getName())
                    .build();

                menuEntry.add(ClickableItem.of(icon, e -> {
                    if (e.isLeftClick()) {
                        p.closeInventory();
                        p.performCommand("spy " + pl.getName());
                    } else {
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                    }

                }));
            }

            pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
            pagination.setItemsPerPage(21);

            if (!pagination.isLast()) {
                contents.set(5, 8, ClickableItem.of(ItemUtil.nextPage, e
                    -> contents.getHost().open(p, pagination.next().getPage()))
                );
            }

            if (!pagination.isFirst()) {
                contents.set(5, 0, ClickableItem.of(ItemUtil.previosPage, e
                    -> contents.getHost().open(p, pagination.previous().getPage()))
                );
            }

            pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1)).allowOverride(false));
        }
    }
}
    
    
 
