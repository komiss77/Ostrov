package ru.komiss77.commands;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import ru.komiss77.ApiOstrov;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.hook.DynmapHook;
import ru.komiss77.hook.WGhook;
import ru.komiss77.objects.CaseInsensitiveSet;
import ru.komiss77.utils.LocUtil;


public class OcleanCmd implements OCommand {

  private static final String COMMAND = "oclean"; //просто /clean перехватывает прокси!!
  private static final List<String> ALIASES = List.of();
  private static final String DESCRIPTION = "Очистка данных";
  private static final String arg0 = "arg0", arg1 = "arg1", arg2 = "arg2", arg3 = "arg4", arg4 = "arg4";

  @Override
  public LiteralCommandNode<CommandSourceStack> command() {

    return Commands.literal(COMMAND)
        .executes(executor())//выполнение без аргументов

        //1 аргумент
        .then(Resolver.string(arg0)
            .suggests((cntx, sb) -> {

              sb.suggest("dynmap");

              return sb.buildFuture();
            })
            .executes(executor())//выполнение c 1 аргументом

            //2 аргумента
            .then(Resolver.string(arg1)
                .suggests((cntx, sb) -> {

                  LocUtil.worldNames().forEach(n -> sb.suggest(n));

                  return sb.buildFuture();
                })
                .executes(executor())//выполнение c 2 аргументами

                //3 аргумента
                .then(Resolver.string(arg2)
                    .suggests((cntx, sb) -> {
                      //sb.suggest("третий");
                      return sb.buildFuture();
                    })
                    .executes(executor())//выполнение c 3 аргументами

                    //4 аргумента
                    .then(Resolver.string(arg3)
                        .suggests((cntx, sb) -> {
                          //sb.suggest("четвёртый");
                          return sb.buildFuture();
                        })
                        .executes(executor())//выполнение c 4 аргументами

                        //5 аргументов
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

      if (!ApiOstrov.isStaff(cs)) {
        cs.sendMessage("§cДоступно только операторам!");
        return 0;
      }

      int idx = cntx.getInput().indexOf(" ");
      final String[] arg; //интересуют только аргументы, сама команда типа известна
      if (idx < 0) {
        arg = new String[0]; //"без параметров!");
      } else {
        arg = cntx.getInput().substring(idx + 1).split(" ");
      }
      //тут юзаем по старинке со всеми аргументами   /команда arg[0] ... arg[4]

      if (LocalDB.getConnection() == null) {
        cs.sendMessage("§cНет соединения с БД!");
        return 0;
      }
      if (!LocalDB.useLocalData) {
        cs.sendMessage("§cЭтот сервер не сохраняет данные!");
        return 0;
      }
      if (Timer.has("clean".hashCode())) {
        cs.sendMessage("§6Очистка уже запущена...");
        return 0;
      }
      Timer.add("clean".hashCode(), 10);

      if (arg.length == 0) {
        final int currentTime = Timer.getTime();
        final int threeMonthLater = Timer.getTime() - 3 * 30 * 24 * 60 * 60;
        //cs.sendMessage(new TextComponent("три_месяца_назад="+threeMonthLater));

        if (threeMonthLater > System.currentTimeMillis() / 1000) {
          cs.sendMessage("три_месяца_назад недопустимо - больше currentTimeMillis!");
          return 0;
        }

        if (threeMonthLater <= 0) {
          cs.sendMessage("три_месяца_назад недопустимо - <=0 !");
          return 0;
        }

        Collection<String> validUsers = new CaseInsensitiveSet();
        Map<UUID, String> validUuids = new HashMap<>(); //uuid,name

        Set<Integer> id_to_del = new HashSet<>();
        //Set<String> name_to_del=new HashSet<>();

        Ostrov.async(() -> {

          boolean mysqlError = true; //при ошибке sql validId будет пустой, снесёт всё!!
          try {

            final PreparedStatement prepStmt = LocalDB.getConnection().prepareStatement("DELETE FROM `playerData` " +
                "WHERE `lastActivity`<'" + threeMonthLater + "' AND `validTo`<'" + currentTime + "' ;");
            prepStmt.executeUpdate();
            prepStmt.close();

            //загрузка оставшихся ников
            final Statement stmt = LocalDB.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT `name`,`uuid` FROM playerData ;");
            while (rs.next()) {
              validUsers.add(rs.getString("name"));
              if (rs.getString("uuid").length() == 36) {
                validUuids.put(UUID.fromString(rs.getString("uuid")), rs.getString("name"));
              }
            }
            rs.close();

            cs.sendMessage("§eИз локальной БД удалены не заходившие более 3мес. и §fvalidTo §eменьше текущей даты.");
            cs.sendMessage("§aосталось в базе ников: §f" + validUsers.size() + "§a, uuid: §f" + validUuids.size());


            rs = stmt.executeQuery("SELECT `id`,`name` FROM `moneyOffline` ;");
            while (rs.next()) {
              if (!validUsers.contains(rs.getString("name"))) {
                id_to_del.add(rs.getInt("id"));
              }
            }
            rs.close();
            for (int id : id_to_del) {
              try (final PreparedStatement delStmt = LocalDB.getConnection()
                  .prepareStatement("DELETE FROM `moneyOffline` WHERE `id`=" + id)) {
                delStmt.executeUpdate();
              }

            }
            cs.sendMessage("§e moneyOffline - удалено:" + id_to_del.size());
            id_to_del.clear();


            mysqlError = false;

            stmt.close();

          } catch (SQLException e) {

            Ostrov.log_err("§с clean 1 - " + e.getMessage());

          }

          if (mysqlError) return;

          File dataDir = new File(Bukkit.getWorldContainer().getPath() + File.separator
              + Bukkit.getWorlds().getFirst().getName() + File.separator + "playerdata");
          if (dataDir.isDirectory()) {
            int dot;
            UUID uuid;

            File[] files = dataDir.listFiles();
            File pdFile;
            int count = 0;

            for (int i = 0; i < files.length; i++) {//for (File f : dataDir.listFiles()) {
              pdFile = files[i];
              dot = pdFile.getName().indexOf(".");
              if (dot > 0) {
                uuid = UUID.fromString(pdFile.getName().substring(0, dot));
                if (!validUuids.containsKey(uuid)) { //uuid удалён из базы
                  pdFile.delete();
                  count++;
                }
              }
            }
            cs.sendMessage("§e playerDataFile - удалено:" + count);
          }


          dataDir = new File(Bukkit.getWorldContainer().getPath() + File.separator
              + Bukkit.getWorlds().getFirst().getName() + File.separator + "advancements");
          if (dataDir.isDirectory()) {
            int dot;
            UUID uuid;

            File[] files = dataDir.listFiles();
            File pdFile;
            int count = 0;

            for (int i = 0; i < files.length; i++) {//for (File f : dataDir.listFiles()) {
              pdFile = files[i];
              dot = pdFile.getName().indexOf(".");
              if (dot > 0) {
                uuid = UUID.fromString(pdFile.getName().substring(0, dot));
                if (!validUuids.containsKey(uuid)) { //uuid удалён из базы
                  pdFile.delete();
                  count++;
                }
              }
            }
            cs.sendMessage("§e advancements - удалено:" + count);
          }


          dataDir = new File(Bukkit.getWorldContainer().getPath() + File.separator
              + Bukkit.getWorlds().getFirst().getName() + File.separator + "stats");
          if (dataDir.isDirectory()) {
            int dot;
            UUID uuid;

            File[] files = dataDir.listFiles();
            File pdFile;
            int count = 0;

            for (int i = 0; i < files.length; i++) {//for (File f : dataDir.listFiles()) {
              pdFile = files[i];
              dot = pdFile.getName().indexOf(".");
              if (dot > 0) {
                uuid = UUID.fromString(pdFile.getName().substring(0, dot));
                if (!validUuids.containsKey(uuid)) { //uuid удалён из базы
                  //if (Bukkit.getPlayer(uuids.get(uuid))!=null) { //на всяк случай,вдруг онлайн
                  //     continue;
                  //}
                  pdFile.delete();
                  count++;
                }
              }
            }
            cs.sendMessage("§e stats - удалено:" + count);
          }

          if (Ostrov.wg) {
            final int deleted = WGhook.purgeDeadRegions(validUsers, validUuids.keySet());
            cs.sendMessage("§e WG regions - удалено:" + deleted);
          }

        }, 20);


      } else if (arg.length == 1) {

        if (arg[0].equalsIgnoreCase("dynmap")) {
          if (!Ostrov.dynmap) {
            cs.sendMessage("§cDynmap нет в плагинах!");
            return 0;
          }
        }

      } else {
        if (arg[0].equalsIgnoreCase("dynmap")) {
          final World w = Bukkit.getWorld(arg[1]);
          if (w == null) {
            cs.sendMessage("§cНет мира " + arg[1] + " !");
            return 0;
          }
          if (!Ostrov.dynmap) {
            cs.sendMessage("§cDynmap нет в плагинах!");
            return 0;
          }
          DynmapHook.purge(Resolver.world(cntx, w.getName()).getName());

        }
      }
      return Command.SINGLE_SUCCESS;
    };
  }


  @Override
  public List<String> aliases() {
    return ALIASES;
  }

  @Override
  public String description() {
    return DESCRIPTION;
  }



/*
    public CleanCmd() {
        final String type = "type", world = "world";

        new OCmdBuilder("clean", "/clean <тип> <мир>").run(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();

            return Command.SINGLE_SUCCESS;


        })
            .then(Resolver.string(type)).suggest(cntx -> {
                if (cntx.getSource().getSender() instanceof final Player pl) {
                    if (!ApiOstrov.isStaff(pl)) {
                        return Set.of();
                    }
                }
                return Set.of("dynmap");
            }, true)

            .then(Resolver.world(world))
            .suggest(cntx -> {
                if (cntx.getSource().getSender() instanceof final Player pl) {
                    if (!ApiOstrov.isStaff(pl)) {
                        return Set.of();
                    }
                }
                return LocUtil.worldNames();
            }, true)
            .run(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (cs instanceof final Player pl) {
                    if (!ApiOstrov.isStaff(pl)) {
                        pl.sendMessage("§cДоступно только операторам!");
                        return 0;
                    }
                }
                return switch (Resolver.string(cntx, type)) {
                    case "dynmap" -> {
                        if (!Ostrov.dynmap) {
                            cs.sendMessage("§cDynmap нет в плагинах!");
                            yield 0;
                        }
                        DynmapHook.purge(Resolver.world(cntx, world).getName());
                        yield Command.SINGLE_SUCCESS;
                    }
                    default -> 0;
                };
            })
        .description("Очистка данных")
        //.aliases("очистка")
        .register();
    }*/
}
    
    
 
