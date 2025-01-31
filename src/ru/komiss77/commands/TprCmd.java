package ru.komiss77.commands;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.events.RandomTpFindEvent;
import ru.komiss77.hook.WGhook;
import ru.komiss77.modules.DelayTeleport;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.MoveUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.ScreenUtil;


public class TprCmd implements OCommand {

  private static final String COMMAND = "tpr";
  private static final List<String> ALIASES = List.of("rtp");
  private static final String DESCRIPTION = "Случайный телепорт";
  private static final boolean CAN_CONSOLE = true;
  private static final String arg0 = "player", arg1 = "world", arg2 = "radius", arg3 = "arg4", arg4 = "arg4";
  private static final HashMap<String, BukkitTask> tpData;
  private static final int TRY_PER_TICK = 3;

  static {
    tpData = new HashMap<>();
  }

  @Override
  public LiteralCommandNode<CommandSourceStack> command() {

    return Commands.literal(COMMAND)
        .executes(executor())//выполнение без аргументов
        //1 аргумент
        .then(Resolver.string(arg0)
            .suggests((cntx, sb) -> {

              //обычно 0 аргумент - имя игрока
              PM.suggester(sb.getRemaining()).forEach(s -> sb.suggest(s));

              return sb.buildFuture();
            })
            .executes(executor())//выполнение c 1 аргументом

            //2 аргумент
            .then(Resolver.string(arg1)
                .suggests((cntx, sb) -> {
                  Bukkit.getWorlds().forEach((w) -> sb.suggest(w.getName()));
                  return sb.buildFuture();
                })
                .executes(executor())//выполнение c 2 аргументами

                //3 аргумент
                .then(Resolver.string(arg2)
                    .suggests((cntx, sb) -> {
                      sb.suggest(10);
                      sb.suggest(100);
                      sb.suggest(1000);
                      return sb.buildFuture();
                    })
                    .executes(executor())//выполнение c 3 аргументами

                    //4 аргумент
                    .then(Resolver.string(arg3)
                        .suggests((cntx, sb) -> {
                          //sb.suggest("четвёртый");
                          return sb.buildFuture();
                        })
                        .executes(executor())//выполнение c 4 аргументами

                        //5 аргумент
                        .then(Resolver.string(arg4)
                            .suggests((cntx, sb) -> {
                              //sb.suggest("пятый");
                              return sb.buildFuture();
                            })
                            .executes(executor())//выполнение c 5 аргументами

                        )
                    )
                )
            )
        )

        .build();
  }


  private static Command<CommandSourceStack> executor() {
    return cntx -> {
      final CommandSender cs = cntx.getSource().getSender();
      Player p = (cs instanceof Player) ? (Player) cs : null;
      final boolean console = cs instanceof ConsoleCommandSender;

      int idx = cntx.getInput().indexOf(" ");
      final String[] arg; //интересуют только аргументы, сама команда типа известна
      if (idx < 0) {
        arg = new String[0]; //"без параметров!");
      } else {
        arg = cntx.getInput().substring(idx + 1).split(" ");
      }
      int delay = Cfg.tpr_command_delay;
      if (delay < 1 && !console) {
        cs.sendMessage("§c" + Lang.t(p, "Телепорт в случайное место командой отключён на этом сервере!"));
        return 0;
      }

      if (arg.length >= 1) { //при попытке тп другого игрока
        if (!ApiOstrov.isLocalBuilder(p) && !console) {
          cs.sendMessage("§c" + Lang.t(p, "Вы не можете ТПР другого игрока!"));
          return 0;
        }
      }

      World world = null;
      int radiusLimit = 0;

      switch (arg.length) {

        case 3:
          radiusLimit = NumUtil.intOf(arg[2], 0);
          if (radiusLimit < 1) {
            cs.sendMessage("§c" + Lang.t(p, "Лимит радиуса поиска - число больше 1!"));
            return 0;
          }

        case 2:
          world = Bukkit.getWorld(arg[1]);
          if (world == null) {
            cs.sendMessage("§c" + Lang.t(p, "Нет мира с названием ") + arg[1]);
            return 0;
          }
          //break;


        case 1:
          p = Bukkit.getPlayerExact(arg[0]);
          if (p == null) {
            cs.sendMessage("§c" + Lang.t(p, "Игрок ") + arg[0] + Lang.t(p, " не найден!"));
            return 0;
          }
          delay = 5;
          break;

        case 0:
          if (console) {
            cs.sendMessage("§cДля консоли - нужно указать ник!");
            return 0;
          }
          break;


        default:
          if (console) {
            cs.sendMessage("§ctpr <ник> [мир] [радиус]");
          } else {
            cs.sendMessage("§ctpr [ник] [мир] [радиус]");
          }
          return 0;

      }

      if (world == null && p != null) {
        world = p.getWorld();
      }

      //if (!console && world.getEnvironment() != World.Environment.NORMAL ) {
      //    cs.sendMessage( "§cТелепорт работает только в обычном мире!");
      //    return true;
      //}

      if (!Timer.has(p, "tpr_command")) {
        if (!ApiOstrov.isLocalBuilder(p)) {
          Timer.add(p, "tpr_command", delay);
        }
        runCommand(p, world, radiusLimit, console, false, pl -> pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 5));

        //p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 5);
      } else {
        cs.sendMessage("§8" + Lang.t(p, "Телепортер перезаряжается! Осталось: ") + Timer.getLeft(p, "tpr_command") + " сек.!");
      }


      return Command.SINGLE_SUCCESS;
    };
  }


  public static void runCommand(final Player p, final World world, final int radiusLimit, final boolean ignoreMove, final boolean anyCase, final Consumer<Player> onDone) {

    //if ( !p.hasPermission("ostrov.tpr.free")) {
    //    if (ApiOstrov.moneyGetBalance(p.getName())<100) {
    //        p.sendMessage("§cНедостаточно денег для перемещения! Стоимость: 100 лони");
    //        return;
    //    }
    //}
    if (tpData.containsKey(p.getName())) {
      p.sendMessage("§c" + Lang.t(p, "Для Вас уже ищется место для телепорта!"));
      return;
    }

    //p.sendMessage("§bТелепортер ищет безопасное место для Вас...");


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

//Bukkit.broadcastMessage("radiusLimit="+radiusLimit+" worldDiameter="+ worldDiameter+" min="+ min+" max="+ max+" minFindRadius="+minFindRadius+" maxFindRadius="+maxFindRadius);
    tpData.put(p.getName(), new BukkitRunnable() {

          final int xMax = center_x + maxFindRadius;
          final int xMin = center_x - maxFindRadius;
          final int zMax = center_z + maxFindRadius;
          final int zMin = center_z - maxFindRadius;

          //System.out.println("xMax="+ xMax+" xMin="+ xMin+" zMax="+ zMax+" zMin="+ zMin);
          final int x = p.getLocation().getBlockX();
          final int y = p.getLocation().getBlockY();
          final int z = p.getLocation().getBlockZ();
          final int minY = p.getWorld().getMinHeight() + 1;
          final int maxY = p.getWorld().getEnvironment() == World.Environment.NETHER ? 125 : p.getWorld().getMaxHeight() - 2;

          final String name = p.getName();
          int find_try = 100; //если делать меньше, то изменить ниже Поиск места: §3"+(100-find_try)+"%
          int tryPereTick = TRY_PER_TICK;
          //int find_x, find_z, feet_y;
          final WXYZ feetLoc = new WXYZ(world, 0, 0, 0);//Location(world, 0, 0, 0);
          //Location temp;

          //Material headMat = Material.AIR;
          //Material feetMat = Material.AIR;
          ;
          //Material downMat;
          //final boolean wg = Ostrov.getWorldGuard()!=null;

          @Override
          public void run() {

            if (p == null || !p.isOnline() || p.isDead()) {
              this.cancel();
              tpData.remove(name);
              return;
            }

            if (!ignoreMove) {
              if (p.getLocation().getBlockX() != x || p.getLocation().getBlockY() != y || p.getLocation().getBlockZ() != z) {
                ScreenUtil.sendActionBarDirect(p, "§c" + Lang.t(p, "ТП отменяется!"));
                this.cancel();
                tpData.remove(name);
                return;
              }
            }

            if (find_try == 0) {

              done();

            } else {

              if (ignoreMove) {
                ScreenUtil.sendActionBarDirect(p, "§8" + Lang.t(p, "Поиск места") + ": §3" + (100 - find_try) + "%");
              } else {
                ScreenUtil.sendActionBarDirect(p, "§e" + Lang.t(p, "Сохраняйте неподвижность, ищем!") + " §b" + (100 - find_try) + "%");
              }

            }


            tryPereTick = TRY_PER_TICK;

            for (; tryPereTick > 0; tryPereTick--) {

              //find_x = Ostrov.random.nextBoolean() ? ApiOstrov.randInt(center_x + minFindRadius, xMax) : ApiOstrov.randInt(xMin, center_x - minFindRadius);
              //find_z = Ostrov.random.nextBoolean() ? ApiOstrov.randInt(center_z + minFindRadius, zMax) : ApiOstrov.randInt(zMin, center_z - minFindRadius);
              feetLoc.x = Ostrov.random.nextBoolean() ? NumUtil.randInt(center_x + minFindRadius, xMax) : NumUtil.randInt(xMin, center_x - minFindRadius);
              feetLoc.z = Ostrov.random.nextBoolean() ? NumUtil.randInt(center_z + minFindRadius, zMax) : NumUtil.randInt(zMin, center_z - minFindRadius);

              feetLoc.y = maxY;

              //feetLoc.set(find_x + 0.5, feet_y, find_z + 0.5);//=world.getBlockAt(find_x, world.getHighestBlockYAt(find_x, find_z), find_z).getLocation();
              //feetLoc.x = find_x;
              //feetLoc.y = feet_y;
              //feetLoc.z = find_z;

              //в кланах приват чанками, поэтому можно чекнуть не определяя y
              // if (faction && ApiFactions.geFaction(feetLoc) !=null) {
              //     continue;
              // }
              final RandomTpFindEvent e = new RandomTpFindEvent(p, feetLoc);
              Bukkit.getPluginManager().callEvent(e);
              if (e.isCancelled()) {
                continue;
              }

//Ostrov.log("                                    find_try="+find_try+" tryPereTick="+tryPereTick);
              //feetLoc = TeleportLoc.findSafeLocation(p, feetLoc);
              //if (feetLoc == null) {
              //    continue;
              //}

                            /*for (; feet_y >= minY; feet_y--) {
                                headMat = feetMat;
                                feetMat = downMat;
                                downMat = Nms.getFastMat(world, find_x, feet_y - 1, find_z);

                                //в аду или при генерации как в аду (определяем потолок из бедрока)
                                if ((world.getEnvironment() == World.Environment.NETHER || feet_y > 0) && downMat == Material.BEDROCK) {
                                    continue;
                                }

                                //feetMat = VM.getNmsServer().getFastMat(world, find_x, find_y+1, find_z);
                                //headMat = VM.getNmsServer().getFastMat(world, find_x, find_y+2, find_z);
//Ostrov.log(find_x+","+find_y+","+find_z+" "+downMat+"-"+feetMat+"-"+headMat);
                                //если над нижним блоком нет 2 блока для тела, пропускаем ниже
                                //if (!LocationUtil.isPassable(headMat) || !LocationUtil.isPassable(feetMat)) {
                                //    continue;
                                //}
                                feetLoc.setY(feet_y + 0.6);
                                //if (LocationUtil.canStand(downMat) || downMat==Material.WATER) { //вода или подходит для стояния - сойдёт
                                if (TeleportLoc.isSafePlace(headMat, feetMat, downMat)) {
                                    break;
                                }
                            }*/


              //if (feet_y<=minY) { //это будет в энде скорее всего
              //    continue;
              //}

              //feetLoc.setY(feet_y+0.6);//приподнять на пол блока

              if (Ostrov.wg && !WGhook.canBuild(p, feetLoc.getCenterLoc())) {
                continue;
              }

              //if (Nms.getFastMat(feetLoc) == Material.WATER) { //была найдена поверхность воды - ставим кувшинку
//Ostrov.log("WATER!!!!"+feetLoc.getBlock().getType()+"->LILY_PAD");
              //    feetLoc.getBlock().setType(Material.LILY_PAD);
              //}

              done();
              return;
            }


            find_try--;


          }


          private void done() {
            this.cancel();
            tpData.remove(name);

            if (find_try == 0) {
              if (anyCase) {
                p.sendMessage("§b" + Lang.t(p, "Это лучшее, что мы смогли найти.."));
              } else {
                p.sendMessage("§b" + Lang.t(p, "Телепортер не смог найти подходящее место! Попробуйте позже.."));
                return;
              }
            }
//Ostrov.log("TPR feetLoc="+feetLoc+" ignoreMove?"+ignoreMove);
            if (ignoreMove) {
              //p.teleport(feetLoc.getCenterLoc());
              MoveUtil.safeTP(p, feetLoc.getCenterLoc());
            } else {
              DelayTeleport.tp(p, feetLoc.getCenterLoc(), 3, Lang.t(p, "Вы в рандомной локации."), true, true, DyeColor.WHITE);
            }
            if (onDone != null) {
              onDone.accept(p);
            }

                       /* if (Nms.getFastMat(feetLoc) == Material.WATER) { //была поставлена кувшинка на воде
                            tpData.put(p.getName(), new BukkitRunnable() { //чтобы не давало новое ТПР пока не сошел с места
                                final String name = p.getName();

                                @Override
                                public void run() {
                                    final Player pl = Bukkit.getPlayerExact(name);
                                    //if (pl == null || !pl.isOnline() || pl.isDead() || pl.getLocation().getBlockX() != find_x || pl.getLocation().getBlockZ() != find_z) {
                                    if (pl == null || !pl.isOnline() || pl.isDead() || pl.getLocation().getBlockX() != feetLoc.x || pl.getLocation().getBlockZ() != feetLoc.z) {
                                        tpData.remove(name);
                                        this.cancel();
                                        feetLoc.getBlock().setType(Material.AIR);
                                    }
                                }
                            }.runTaskTimer(Ostrov.instance, 30 + (ignoreMove ? 60 : 0), 10));
                        }*/

          }


        }.runTaskTimer(Ostrov.instance, 1, 1)
    );


  }


  @Override
  public List<String> aliases() {
    return ALIASES;
  }

  @Override
  public String description() {
    return DESCRIPTION;
  }
}
