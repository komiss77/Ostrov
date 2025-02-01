package ru.komiss77.modules.player.mission;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.BookMeta;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.RemoteDB;
import ru.komiss77.Timer;
import ru.komiss77.commands.Comm;
import ru.komiss77.commands.OCommand;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Stat;
import ru.komiss77.events.MissionEvent;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.SmartInventory;


public class MissionCmd implements OCommand {

  private static final String COMMAND = "mission";
  private static final Set<String> ALIASES = Set.of();
  private static final String DESCRIPTION = "Управления миссиями";
  private static final boolean CAN_CONSOLE = false;
  private static final String arg0 = "action", arg1 = "arg1", arg2 = "arg2", arg3 = "arg4", arg4 = "arg4";
  private final List<String> subCmd = List.of("journal", "select", "accept", "deny", "complete", "forceload");

  @Override
  public LiteralCommandNode<CommandSourceStack> command() {

    return Commands.literal(COMMAND)
        .executes(executor())//выполнение без аргументов
        //1 аргумент
        .then(Resolver.string(arg0)
            .suggests((cntx, sb) -> {

              subCmd.stream()
                  .filter(c -> c.startsWith(sb.getRemaining()))
                  .forEach(c -> sb.suggest(c));

              return sb.buildFuture();
            })
            .executes(executor())//выполнение c 1 аргументом

            //2 аргумент
            .then(Resolver.string(arg1)
                .suggests((cntx, sb) -> {
                  final CommandSender cs = cntx.getSource().getSender();
                  if (PM.exist(cs.getName())) {
                    final String sub = Comm.arg(sb, 0); //смотрим подкоманду
                    if (sub.equals("deny") || sub.equals("complete")) {
                      for (final int id : PM.getOplayer(cs.getName()).missionIds) {
                        sb.suggest(id);
                      }
                    } else if (sub.equals("accept")) {
                      for (final int id : MissionManager.missions.keySet()) {
                        sb.suggest(id);
                      }
                    }
                  }
                  return sb.buildFuture();
                })
                .executes(executor())//выполнение c 2 аргументами

                //3 аргумент
                .then(Resolver.string(arg2)
                    .suggests((cntx, sb) -> {
                      //sb.suggest("третий");
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
      final Player p = (cs instanceof Player) ? (Player) cs : null;
      if (!CAN_CONSOLE && p == null) {
        cs.sendMessage("§eНе консольная команда!");
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

      if (arg.length >= 1 && arg[0].equalsIgnoreCase("forceload")) {
        if (ApiOstrov.isLocalBuilder(cs, true)) {
          MissionManager.loadMissions();
          p.sendMessage("§aМиссии прогружены из БД Острова");
        }
        return 0;
      }


      final Oplayer op = PM.getOplayer(p);

      if (op.isGuest) {
        p.sendMessage("§6Гостям недоступны миссии! Пожалуйста, §bзарегистрируйтесь§6!");
        return 0;
      }


      if (arg.length == 0) {
        SmartInventory
            .builder()
            .provider(new MissionMainMenu())
            .size(5, 9)
            .title("§a§lМиссии")
            .build()
            .open(p);
        return 0;
      }


      switch (arg[0]) {


        case "journal" -> {
          p.getOpenInventory().close();
          Ostrov.async(() -> {
            try (Statement stmt = RemoteDB.getConnection().createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM `missions` ORDER BY `activeFrom` DESC")) {

              final ItemStack book = new ItemBuilder(ItemType.WRITTEN_BOOK)
                  .name("Журнал \"Миссия сегодня\"")
                  .build();
              BookMeta bookMeta = (BookMeta) book.getItemMeta();

              while (rs.next()) {
                final TextComponent.Builder page = Component.text().content("§l§" + rs.getString("nameColor") + rs.getString("name"));
                //page.append(new ComponentBuilder("\n§1Уровень: §6"+rs.getInt("level")+"§1, Репутация: §6"+rs.getInt("reputation")).create());
                page.append(Component.text("\n§1Уровень: §6" + rs.getInt("level") + "§1, реп: §6" + rs.getInt("reputation")));
                page.append(Component.text("\n§1Награда: §6" + rs.getInt("reward") + " §1рил \n(фонд: §6" + rs.getInt("reward") * rs.getInt("rewardFund") + "§1 рил)"));

                if (Timer.getTime() > rs.getInt("validTo") || Timer.getTime() > rs.getInt("validTo")) {
                  page.append(Component.text("\n§cc " + TimeUtil.dateFromStamp(rs.getInt("activeFrom")) + "\n§cпо " + TimeUtil.dateFromStamp(rs.getInt("validTo"))));
                } else {
                  page.append(Component.text("\n§ac " + TimeUtil.dateFromStamp(rs.getInt("activeFrom")) + "\n§aпо " + TimeUtil.dateFromStamp(rs.getInt("validTo"))));
                }

                page.append(Component.text("\n§1Требования:"));
                for (final Map.Entry<String, Integer> e : MissionManager.getMapFromString(rs.getString("request")).entrySet()) {
                  page.append(Component.text("\n§b" + e.getKey() + " §7: §5" + e.getValue()));
                }

                bookMeta.addPages(page.build());
              }

              bookMeta.setTitle("Журнал \"Миссия сегодня\"");
              bookMeta.setAuthor("Остров77");
              book.setItemMeta(bookMeta);

              Ostrov.sync(() -> {
                p.openBook(book);
              }, 0);

            } catch (SQLException e) {
              Ostrov.log_err("§с MissionCmd journal - " + e.getMessage());
            }
          }, 0);
        }


        case "accept" -> {
          if (!MissionManager.canUseCommand(p, "accept")) return 0;

          if (arg.length == 2) { //принятие с указанием ИД
            final int missionId = NumUtil.intOf(arg[1], -1);
            if (missionId < 0 || !MissionManager.missions.containsKey(missionId)) {
              p.sendMessage("§cНет активной миссии с ИД " + arg[1] + "!");
              return 0;
            }
            final Mission mi = MissionManager.missions.get(missionId);

            if (op.missionIds.contains(mi.id)) {
              p.sendMessage("§cМисия уже принята!");
              return 0;
            }
            if (mi.canComplete <= 0) {
              p.sendMessage("§cПризовой фонд исчерпан! :(");
              return 0;
            }
            if (Timer.getTime() > mi.validTo) {
              p.sendMessage("§cМиссия просрочена! :(");
              return 0;
            }
            if (op.getStat(Stat.LEVEL) < mi.level) {
              p.sendMessage("§cДолжен быть уровень не менее §6" + mi.level);
              return 0;
            }
            if (op.getStat(Stat.REPUTATION) < mi.reputation) {
              p.sendMessage("§cДолжна быть репутация не менее §6" + mi.reputation);
              return 0;
            }
            final int limit = MissionManager.getLimit(op);
            if (op.missionIds.size() >= limit) {
              p.sendMessage("§cЛимит миссий для вашей группы: §e" + limit);
              return 0;
            }
            p.getOpenInventory().close();

            Ostrov.async(() -> { //в остальных случаях открыт меню выбора
              RemoteDB.getResultSet(p, "SELECT `missionId`,`completed` FROM `missionsProgress` WHERE `name`='" + op.nik + "' AND `completed`>0", (completed) -> {
                if (completed == null) {
                  p.sendMessage("§cОшибка запроса к БД!");
                  return;
                }
                if (completed.containsKey(String.valueOf(mi.id))) { //уже выполнена
                  p.sendMessage("§5Миссия уже выполнена §d" + TimeUtil.dateFromStamp((int) completed.get(String.valueOf(mi.id))));
                  return;
                }
                //принятие
                RemoteDB.executePstAsync(p, "INSERT INTO missionsProgress (recordId,name,missionId,taken) VALUES ('" + mi.getRecordID(op.nik) + "', '" + op.nik + "', '" + mi.id + "', '" + Timer.getTime() + "'); ");
                RemoteDB.executePstAsync(p, "UPDATE missions SET doing=doing+1 WHERE missionId=" + mi.id); //добавить претендента в БД

                Ostrov.sync(() -> {
                  mi.doing++;
                  op.missionIds.add(missionId);//обновить missionIds
                  op.setData(Data.MISSIONS, StringUtil.listToString(op.missionIds, ";"));//обновить Data.MISSION
                  final Title.Times times = Title.Times.times(Duration.ofMillis(20 * 50), Duration.ofMillis(20 * 50), Duration.ofMillis(80 * 50));
                  ScreenUtil.sendTitle(p, Component.text(""), Component.text("Принятие миссии ", NamedTextColor.GRAY).append(mi.displayName()), times);
                  //p.sendMessage("§fВы приняли миссию "+mi.getDisplayName()+"§f, выполните её до "+ApiOstrov.dateFromStamp(mi.validTo));
                  p.sendMessage(Component.text("Вы приняли миссию ", NamedTextColor.WHITE)
                      .append(mi.displayName())
                      .append(Component.text(", выполните её до " + TimeUtil.dateFromStamp(mi.validTo), NamedTextColor.WHITE))
                  );
                  p.getWorld().playSound(p.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_2, 1, 1);
                  Bukkit.getPluginManager().callEvent(new MissionEvent(p, mi.name, MissionEvent.MissionAction.Accept));
                }, 1);
              });

            }, 0);

            return 0;
          }


          Ostrov.async(() -> {

            final Connection conn = RemoteDB.getConnection();
            if (conn == null) return;

            Statement stmt = null;
            ResultSet rs = null;

            try {

              stmt = conn.createStatement();
              rs = stmt.executeQuery("SELECT `missionId`,`completed` FROM `missionsProgress` WHERE `name`='" + op.nik + "' AND `completed`>0");

              final HashMap<Integer, Integer> completed = new HashMap<>();
              while (rs.next()) {
                completed.put(rs.getInt("missionId"), rs.getInt("completed"));
              }
              rs.close();

              Ostrov.sync(() -> {
                SmartInventory
                    .builder()
                    .provider(new MissionSelectMenu(completed))
                    .size(5, 9)
                    .title("Актуальные Миссии")
                    .build()
                    .open(p);
              }, 0);

            } catch (SQLException ex) {

              Ostrov.log_err("§с MissionCmd accept : " + ex.getMessage());

            } finally {

              try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
              } catch (SQLException ex) {
                Ostrov.log_err("§с MissionCmd accept close " + ex.getMessage());
              }

            }

          }, 0);
        }


        case "select" -> {
          if (!MissionManager.canUseCommand(p, "select")) return 0;

          Ostrov.async(() -> {

            final Connection conn = RemoteDB.getConnection();
            if (conn == null) return;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT `missionId`,`completed` FROM `missionsProgress` WHERE `name`='" + op.nik + "' AND `completed`>0")) {


              final HashMap<Integer, Integer> completed = new HashMap<>();
              while (rs.next()) {
                completed.put(rs.getInt("missionId"), rs.getInt("completed"));
              }
              //rs.close();

              Ostrov.sync(() -> {
                SmartInventory
                    .builder()
                    .provider(new MissionSelectMenu(completed))
                    .size(5, 9)
                    .title("§2§lВыбор Миссии")
                    .build()
                    .open(p);
              }, 0);

            } catch (SQLException ex) {
              Ostrov.log_err("§с MissionCmd select : " + ex.getMessage());
            }

          }, 0);


        }


        case "complete" -> {
          if (!MissionManager.canUseCommand(p, "complete")) return 0;
          //обновить missionIds и Data.MISSION
          if (arg.length == 2) { //выполнить с указанием ИД
            final int missionId = NumUtil.intOf(arg[1], -1);
            if (missionId < 0) {  //missionIds подгружаются при входе и меняются при принятии!
              p.sendMessage("§cНе может быть миссии с ИД " + arg[1] + "!");
              return 0;
            }
            final Mission mi = MissionManager.missions.get(missionId);
            if (mi == null) {
              p.sendMessage("§cМиссия с ИД " + missionId + " не подгружена!");
              return 0;
            }
            if (!op.missionIds.contains(missionId)) {
              p.sendMessage(Component.text("Вы не выполняли миссию ", NamedTextColor.RED).append(mi.displayName()));
              //p.sendMessage("§cВы не выполняли миссию "+mi.getDisplayName()+" !");
              return 0;
            }

            Ostrov.async(() -> {

              final Connection conn = RemoteDB.getConnection();
              if (conn == null) return;

              Statement stmt = null;
              ResultSet rs = null;

              try {

                stmt = conn.createStatement();
                rs = stmt.executeQuery("SELECT `progress` FROM `missionsProgress` WHERE `recordId`='" + mi.getRecordID(op.nik) + "' AND `completed`='0';");

                String progress = null;
                if (rs.next()) {
                  progress = rs.getString("progress");
//System.out.println("progress="+progress);
                }
                rs.close();


                if (progress == null || progress.isEmpty()) {

                  //op.getPlayer().sendMessage("§cнет прогресса по миссии "+mi.getDisplayName());
                  op.getPlayer().sendMessage(Component.text("нет прогресса по миссии", NamedTextColor.RED).append(mi.displayName()));

                } else {

                  //проверка условий
                  final CaseInsensitiveMap<Integer> progressMap = MissionManager.getMapFromString(progress);
                  int request;
                  int current;
                  boolean done = true;

                  for (String requestName : mi.request.keySet()) {

                    request = mi.request.get(requestName);

                    if (progressMap.containsKey(requestName)) {
                      current = progressMap.get(requestName);
                      if (current >= request) {
                        //
                      } else {
                        done = false;
                        break;
                      }
                    } else {
                      done = false;
                      break;
                    }

                  }

                  if (done) {
                    //пометить выполнение
                    RemoteDB.executePstAsync(p, "UPDATE missionsProgress SET progress='', completed='" + Timer.getTime() + "' WHERE `recordId`='" + mi.getRecordID(op.nik) + "'; ");
                    RemoteDB.executePstAsync(p, "UPDATE missions SET doing=doing-1,rewardFund=rewardFund-1 WHERE missionId=" + missionId); //убавить претендента в БД и фонд
                    Ostrov.sync(() -> {
                      op.missionIds.remove(missionId);
                      op.setData(Data.MISSIONS, StringUtil.listToString(op.missionIds, ";"));//обновить Data.MISSION
                      //награда
                      op.setData(Data.RIL, op.getDataInt(Data.RIL) + mi.reward);
                      op.addStat(Stat.REPUTATION, 1);
                      op.addStat(Stat.EXP, 10);
                      p.sendMessage(" ");
                      final String rc = TCUtil.randomColor();
                      p.sendMessage(rc + "§m-----§4 <obf>AA<!obf> §eМиссия завершена §4<obf>AA<!obf> " + rc + "§m-----");
                      p.sendMessage(Component.text(" Миссия §7-> ", NamedTextColor.WHITE).append(mi.displayName()));
                      p.sendMessage(" §fНаграда §7-> §e" + mi.reward + " рил");
                      p.sendMessage(" ");
                      //поправить счётчики миссии
                      mi.doing--;
                      mi.canComplete--;
                      //эффекты
                      p.getWorld().playSound(p.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_5, 1, 1);
                      Bukkit.getPluginManager().callEvent(new MissionEvent(p, mi.name, MissionEvent.MissionAction.Complete));
                      ParticleUtil.spawnRandomFirework(p.getLocation().clone().add(0, 2, 0));
                    }, 0);
                    ParticleUtil.display(p.getLocation());
                    Ostrov.sync(() -> ParticleUtil.spawnRandomFirework(p.getLocation().clone().add(0, 2, 0)), 10);
                    Ostrov.sync(() -> ParticleUtil.spawnRandomFirework(p.getLocation().clone().add(0, 2, 0)), 20);
                  } else {
                    //op.getPlayer().sendMessage("§cУсловия миссии "+mi.getDisplayName()+ " не выполнены!");
                    op.getPlayer().sendMessage(Component.text("Условия миссии ", NamedTextColor.RED)
                        .append(mi.displayName())
                        .append(Component.text(" не выполнены!", NamedTextColor.RED))
                    );
                  }
                }

              } catch (SQLException ex) {

                Ostrov.log_err("§с MissionCmd complete : " + ex.getMessage());

              } finally {

                try {
                  if (rs != null) rs.close();
                  if (stmt != null) stmt.close();
                } catch (SQLException ex) {
                  Ostrov.log_err("§с MissionCmd complete close " + ex.getMessage());
                }

              }

            }, 0);
            return 0;
          }
          //Ostrov.async( ()-> { //в остальных случаях открыть меню выбора
          // OstrovDB.getResultSet(p, "SELECT * FROM `missionsProgress` WHERE `name`='"+op.nik+"' AND `completed`='0';", (completed)-> {
          //    if (completed==null) {
          //        p.sendMessage("§cОшибка запроса к БД!");
          //       return;
          //   }
          // Ostrov.sync( ()-> {
          SmartInventory
              .builder()
              .id(op.nik + "Миссии")
              .type(InventoryType.HOPPER)
              .provider(new MissionsCompleteMenu())
              //.size(3, 9)
              .title("Завершение Миссии")
              .build()
              .open(p);
          // },0);
          //});
          //}, 0);
        }


        case "deny" -> {
          if (!MissionManager.canUseCommand(p, "deny")) return 0;
          //отказ должен быть возможен для устаревших тоже!
          if (arg.length == 2) { //отказ с указанием ИД
            final int missionId = NumUtil.intOf(arg[1], -1);
            //if (missionId<0 || !op.missionIds.contains(missionId)) {  //missionIds подгружаются при входе и меняются при принятии!
            //    p.sendMessage("§cВы не выполняете миссию с ИД "+arg[1]+"!");
            //    return true;
            //}
            p.getOpenInventory().close();
            if (missionId < 0) {  //missionIds подгружаются при входе и меняются при принятии!
              p.sendMessage("§cНе может быть миссии с ИД " + arg[1] + "!");
              return 0;
            }
            //отказ - обработка по выполнению запроса к БД?
            RemoteDB.executePstAsync(p, "DELETE FROM missionsProgress WHERE `name`='" + op.nik + "' AND `missionId`='" + missionId + "'");
            //OstrovDB.executePstAsync(p, "DELETE FROM missionsProgress WHERE `recordId`='"'");
            RemoteDB.executePstAsync(p, "UPDATE missions SET doing=doing-1 WHERE missionId=" + missionId); //убавить претендента в БД
            if (MissionManager.missions.containsKey(missionId)) {
              MissionManager.missions.get(missionId).doing--;
              Bukkit.getPluginManager().callEvent(new MissionEvent(p, MissionManager.missions.get(missionId).name, MissionEvent.MissionAction.Deny));
            }
            if (op.missionIds.remove(missionId)) {//обновить missionIds
              op.setData(Data.MISSIONS, StringUtil.listToString(op.missionIds, ";"));//обновить Data.MISSION
              p.getWorld().playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, .5f, .5f);
              p.sendMessage("§5Вы отказались от миссии !");
            }

          }
        }

      }

      return Command.SINGLE_SUCCESS;
    };
  }


  @Override
  public Set<String> aliases() {
    return ALIASES;
  }

  @Override
  public String description() {
    return DESCRIPTION;
  }

}






/*

public class M implements OCommand {

    private final List<String> subCmd = Arrays.asList("journal", "select", "accept", "deny", "complete", "forceload");

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String act = "action", mid = "id";
        return Commands.literal("mission").executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getExecutor();
                if (!(cs instanceof final Player pl)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                final Oplayer op = PM.getOplayer(pl);

                if (op.isGuest) {
                    pl.sendMessage("§6Гостям недоступны миссии! Пожалуйста, §bзарегистрируйся§6!");
                    return 0;
                }

                SmartInventory
                    .builder()
                    .provider(new MissionMainMenu())
                    .size(5, 9)
                    .title("§a§lМиссии")
                    .build()
                    .open(pl);
                return Command.SINGLE_SUCCESS;
            })
            .then(Resolver.string(act)
                .suggests((cntx, sb) -> {
                    subCmd.forEach(sc -> sb.suggest(sc));
                    return sb.buildFuture();
                })
                .executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getExecutor();
                    if (!(cs instanceof final Player pl)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }

                    final Oplayer op = PM.getOplayer(pl);

                    if (op.isGuest) {
                        pl.sendMessage("§6Гостям недоступны миссии! Пожалуйста, §bзарегистрируйся§6!");
                        return 0;
                    }

                    return switch (Resolver.string(cntx, act)) {
                        case "forceload" -> {
                            if (!ApiOstrov.isLocalBuilder(cs, true)) {
                                cs.sendMessage("§cДоступно только персоналу!");
                                yield 0;
                            }
                            MissionManager.loadMissions();
                            pl.sendMessage("§aМиссии прогружены из БД Острова");
                            yield Command.SINGLE_SUCCESS;
                        }
                        case "journal" -> {
                            pl.getOpenInventory().close();
                            Ostrov.async(() -> {
                                try (final Statement stmt = RemoteDB.getConnection().createStatement();
                                     final ResultSet rs = stmt.executeQuery("SELECT * FROM `missions` ORDER BY `activeFrom` DESC")) {

                                    final ItemStack book = new ItemBuilder(Material.WRITTEN_BOOK)
                                            .name("Журнал \"Миссия сегодня\"")
                                            .build();
                                    BookMeta bookMeta = (BookMeta) book.getItemMeta();

                                    while (rs.next()) {
                                        final TextComponent.Builder page = Component.text().content("§l§"
                                            + rs.getString("nameColor") + rs.getString("name"));
                                        page.append(Component.text("\n§1Уровень: §6" + rs.getInt("level")
                                            + "§1, реп: §6" + rs.getInt("reputation")));
                                        page.append(Component.text("\n§1Награда: §6" + rs.getInt("reward")
                                            + " §1рил \n(фонд: §6" + rs.getInt("reward") * rs.getInt("rewardFund") + "§1 рил)"));

                                        if (Timer.getTime() > rs.getInt("validTo") || Timer.getTime() > rs.getInt("validTo")) {
                                            page.append(Component.text("\n§cc " + TimeUtil.dateFromStamp(rs.getInt("activeFrom"))
                                                    + "\n§cпо " + TimeUtil.dateFromStamp(rs.getInt("validTo"))));
                                        } else {
                                            page.append(Component.text("\n§ac " + TimeUtil.dateFromStamp(rs.getInt("activeFrom"))
                                                    + "\n§aпо " + TimeUtil.dateFromStamp(rs.getInt("validTo"))));
                                        }

                                        page.append(Component.text("\n§1Требования:"));
                                        for (final Entry<String, Integer> e : MissionManager.getMapFromString(rs.getString("request")).entrySet()) {
                                            page.append(Component.text("\n§b" + e.getKey() + " §7: §5" + e.getValue()));
                                        }

                                        bookMeta.addPages(page.build());
                                    }

                                    bookMeta.setTitle("Журнал \"Миссия сегодня\"");
                                    bookMeta.setAuthor("Остров77");
                                    book.setItemMeta(bookMeta);

                                    Ostrov.sync(() -> {
                                        pl.openBook(book);
                                    }, 0);

                                } catch (SQLException e) {
                                    Ostrov.log_err("§с MissionCmd journal - " + e.getMessage());
                                }
                            }, 0);
                            yield Command.SINGLE_SUCCESS;
                        }
                        case "select" -> {
                            Ostrov.async(() -> {
                                final Connection conn = RemoteDB.getConnection();
                                if (conn == null) return;

                                try (final Statement stmt = conn.createStatement();
                                     final ResultSet rs = stmt.executeQuery("SELECT `missionId`,`completed` FROM " +
                                         "`missionsProgress` WHERE `name`='" + pl.getName() + "' AND `completed`>0");) {

                                    final HashMap<Integer, Integer> completed = new HashMap<>();
                                    while (rs.next()) {
                                        completed.put(rs.getInt("missionId"), rs.getInt("completed"));
                                    }

                                    Ostrov.sync(() -> {
                                        SmartInventory
                                                .builder()
                                                .provider(new MissionSelectMenu(completed))
                                                .size(5, 9)
                                                .title("§2§lВыбор Миссии")
                                                .build()
                                                .open(pl);
                                    }, 0);

                                } catch (SQLException ex) {
                                    Ostrov.log_err("§с MissionCmd select : " + ex.getMessage());
                                }

                            }, 0);
                            yield Command.SINGLE_SUCCESS;
                        }
                        case "accept" -> {
                            Ostrov.async(() -> {

                                final Connection conn = RemoteDB.getConnection();
                                if (conn == null) return;

                                Statement stmt = null;
                                ResultSet rs = null;

                                try {

                                    stmt = conn.createStatement();
                                    rs = stmt.executeQuery("SELECT `missionId`,`completed` FROM `missionsProgress` " +
                                        "WHERE `name`='" + op.nik + "' AND `completed`>0");

                                    final HashMap<Integer, Integer> completed = new HashMap<>();
                                    while (rs.next()) {
                                        completed.put(rs.getInt("missionId"), rs.getInt("completed"));
                                    }
                                    rs.close();

                                    Ostrov.sync(() -> {
                                        SmartInventory
                                            .builder()
                                            .provider(new MissionSelectMenu(completed))
                                            .size(5, 9)
                                            .title("Актуальные Миссии")
                                            .build()
                                            .open(pl);
                                    }, 0);

                                } catch (SQLException ex) {

                                    Ostrov.log_err("§с MissionCmd accept : " + ex.getMessage());

                                } finally {

                                    try {
                                        if (rs != null) rs.close();
                                        if (stmt != null) stmt.close();
                                    } catch (SQLException ex) {
                                        Ostrov.log_err("§с MissionCmd accept close " + ex.getMessage());
                                    }

                                }

                            }, 0);
                            yield Command.SINGLE_SUCCESS;
                        }
                        case "complete", "deny" -> {
                            SmartInventory
                                .builder()
                                .id(pl.getName() + "Миссии")
                                .type(InventoryType.HOPPER)
                                .provider(new MissionsCompleteMenu())
                                //.size(3, 9)
                                .title("Завершение Миссии")
                                .build()
                                .open(pl);
                            yield 0;
                        }
                        default -> {
                            cs.sendMessage("§cВыбери обну из опций!");
                            yield 0;
                        }
                    };
                }).then(Resolver.integer(mid, 0)
                    .suggests((cntx, sb) -> {
                        switch (Resolver.string(cntx, act)) {
                            case "accept", "deny", "complete":
                                MissionManager.missions.keySet()
                                    .forEach(sc -> sb.suggest(sc));
                            default:
                                break;
                        }
                        return sb.buildFuture();
                    })
                    .executes(cntx -> {
                        final CommandSender cs = cntx.getSource().getExecutor();
                        if (!(cs instanceof final Player pl)) {
                            cs.sendMessage("§eНе консольная команда!");
                            return 0;
                        }

                        final Oplayer op = PM.getOplayer(pl);
                        final int id = Resolver.integer(cntx, mid);
                        final Mission mi;
                        return switch (Resolver.string(cntx, act)) {
                            case "accept" -> {
                                if (!MissionManager.missions.containsKey(id)) {
                                    pl.sendMessage("§cНет активной миссии с ИД " + id + "!");
                                    yield 0;
                                }
                                mi = MissionManager.missions.get(id);

                                if (op.missionIds.contains(mi.id)) {
                                    pl.sendMessage("§cМисия уже принята!");
                                    yield 0;
                                }
                                if (mi.canComplete <= 0) {
                                    pl.sendMessage("§cПризовой фонд исчерпан! :(");
                                    yield 0;
                                }
                                if (Timer.getTime() > mi.validTo) {
                                    pl.sendMessage("§cМиссия просрочена! :(");
                                    yield 0;
                                }
                                if (op.getStat(Stat.LEVEL) < mi.level) {
                                    pl.sendMessage("§cДолжен быть уровень не менее §6" + mi.level);
                                    yield 0;
                                }
                                if (op.getStat(Stat.REPUTATION) < mi.reputation) {
                                    pl.sendMessage("§cДолжна быть репутация не менее §6" + mi.reputation);
                                    yield 0;
                                }
                                final int limit = MissionManager.getLimit(op);
                                if (op.missionIds.size() >= limit) {
                                    pl.sendMessage("§cЛимит миссий для вашей группы: §e" + limit);
                                    yield 0;
                                }
                                pl.getOpenInventory().close();

                                Ostrov.async(() -> { //в остальных случаях открыт меню выбора
                                    RemoteDB.getResultSet(pl, "SELECT `id`,`completed` FROM `missionsProgress` WHERE `name`='"
                                            + op.nik + "' AND `completed`>0", (completed) -> {

                                        if (completed == null) {
                                            pl.sendMessage("§cОшибка запроса к БД!");
                                            return;
                                        }
                                        if (completed.containsKey(String.valueOf(mi.id))) { //уже выполнена
                                            pl.sendMessage("§5Миссия уже выполнена §d" + TimeUtil
                                                    .dateFromStamp((int) completed.get(String.valueOf(mi.id))));
                                            return;
                                        }
                                        //принятие
                                        RemoteDB.executePstAsync(pl, "INSERT INTO missionsProgress (recordId,name,id,taken) VALUES ('"
                                                + mi.getRecordID(op.nik) + "', '" + op.nik + "', '" + mi.id + "', '" + Timer.getTime() + "'); ");
                                        RemoteDB.executePstAsync(pl, "UPDATE missions SET doing=doing+1 WHERE id=" + mi.id);
                                        //добавить претендента в БД

                                        Ostrov.sync(() -> {
                                            mi.doing++;
                                            op.missionIds.add(id);//обновить missionIds
                                            op.setData(Data.MISSIONS, StringUtil.listToString(op.missionIds, ";"));//обновить Data.MISSION
                                            final int times = 50;
                                            ScreenUtil.sendTitle(pl, Component.empty(), TCUtil.form("<gray>Принятие миссии ")
                                                    .append(mi.displayName()), times, times, times);
                                            pl.sendMessage(TCUtil.form("<white>Вы приняли миссию ").append(mi.displayName())
                                                    .append(TCUtil.form("<white>, выполните её до " + TimeUtil.dateFromStamp(mi.validTo)))
                                            );
                                            pl.getWorld().playSound(pl.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_2, 1, 1);
                                            Bukkit.getPluginManager().callEvent(new MissionEvent(pl, mi.name, MissionEvent.MissionAction.Accept));
                                        }, 1);
                                    });

                                }, 0);
                                yield Command.SINGLE_SUCCESS;
                            }
                            case "deny" -> {
                                pl.getOpenInventory().close();
                                //отказ - обработка по выполнению запроса к БД?
                                RemoteDB.executePstAsync(pl, "DELETE FROM missionsProgress WHERE `name`='" + op.nik + "' AND `id`='" + id + "'");
                                //RemoteDB.executePstAsync(pl, "DELETE FROM missionsProgress WHERE `recordId`='"'");
                                RemoteDB.executePstAsync(pl, "UPDATE missions SET doing=doing-1 WHERE id=" + id); //убавить претендента в БД
                                if (MissionManager.missions.containsKey(id)) {
                                    MissionManager.missions.get(id).doing--;
                                    Bukkit.getPluginManager().callEvent(new MissionEvent(pl, MissionManager.missions.get(id).name, MissionEvent.MissionAction.Deny));
                                }
                                if (op.missionIds.remove(id)) {//обновить missionIds
                                    op.setData(Data.MISSIONS, StringUtil.listToString(op.missionIds, ";"));//обновить Data.MISSION
                                    pl.getWorld().playSound(pl.getLocation(), Sound.ITEM_TOTEM_USE, .5f, .5f);
                                    pl.sendMessage("§5Вы отказались от миссии !");
                                }
                                yield Command.SINGLE_SUCCESS;
                            }
                            case "complete" -> {
                                mi = MissionManager.missions.get(id);
                                if (mi == null) {
                                    pl.sendMessage("§cМиссия с ИД " + id + " не подгружена!");
                                    yield 0;
                                }
                                if (!op.missionIds.contains(id)) {
                                    pl.sendMessage(Component.text("Вы не выполняли миссию ", NamedTextColor.RED).append(mi.displayName()));
                                    //pl.sendMessage("§cВы не выполняли миссию "+mi.getDisplayName()+" !");
                                    yield 0;
                                }

                                Ostrov.async(() -> {

                                    final Connection conn = RemoteDB.getConnection();
                                    if (conn == null) return;

                                    Statement stmt = null;
                                    ResultSet rs = null;

                                    try {

                                        stmt = conn.createStatement();
                                        rs = stmt.executeQuery("SELECT `progress` FROM `missionsProgress` " +
                                                "WHERE `recordId`='" + mi.getRecordID(op.nik) + "' AND `completed`='0';");

                                        String progress = null;
                                        if (rs.next()) {
                                            progress = rs.getString("progress");
//System.out.println("progress="+progress);
                                        }
                                        rs.close();


                                        if (progress == null || progress.isEmpty()) {

                                            //op.getPlayer().sendMessage("§cнет прогресса по миссии "+mi.getDisplayName());
                                            op.getPlayer().sendMessage(TCUtil.form("<red>нет прогресса по миссии").append(mi.displayName()));

                                        } else {

                                            //проверка условий
                                            final CaseInsensitiveMap<Integer> progressMap = MissionManager.getMapFromString(progress);
                                            int request;
                                            int current;
                                            boolean done = true;

                                            for (String requestName : mi.request.keySet()) {

                                                request = mi.request.get(requestName);

                                                if (progressMap.containsKey(requestName)) {
                                                    current = progressMap.get(requestName);
                                                    if (current < request) {
                                                        done = false;
                                                        break;
                                                    }
                                                } else {
                                                    done = false;
                                                    break;
                                                }

                                            }

                                            if (done) {
                                                //пометить выполнение
                                                RemoteDB.executePstAsync(pl, "UPDATE missionsProgress SET progress='', completed='"
                                                        + Timer.getTime() + "' WHERE `recordId`='" + mi.getRecordID(op.nik) + "'; ");
                                                RemoteDB.executePstAsync(pl, "UPDATE missions SET doing=doing-1,rewardFund=rewardFund-1 " +
                                                        "WHERE id=" + id); //убавить претендента в БД и фонд
                                                Ostrov.sync(() -> {
                                                    op.missionIds.remove(id);
                                                    op.setData(Data.MISSIONS, StringUtil.listToString(op.missionIds, ";"));//обновить Data.MISSION
                                                    //награда
                                                    op.setData(Data.RIL, op.getDataInt(Data.RIL) + mi.reward);
                                                    op.addStat(Stat.REPUTATION, 1);
                                                    op.addStat(Stat.EXP, 10);
                                                    pl.sendMessage(" ");
                                                    final String rc = TCUtil.randomColor();
                                                    pl.sendMessage(rc + "§m-----§4 <obf>AA<!obf> §eМиссия завершена §4 <obf>AA<!obf>" + rc + "§m-----");
                                                    pl.sendMessage(Component.text(" Миссия §7-> ", NamedTextColor.WHITE).append(mi.displayName()));
                                                    pl.sendMessage(" §fНаграда §7-> §e" + mi.reward + " рил");
                                                    pl.sendMessage(" ");
                                                    //поправить счётчики миссии
                                                    mi.doing--;
                                                    mi.canComplete--;
                                                    //эффекты
                                                    pl.getWorld().playSound(pl.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_5, 1, 1);
                                                    Bukkit.getPluginManager().callEvent(new MissionEvent(pl, mi.name, MissionEvent.MissionAction.Complete));
                                                    ParticleUtil.spawnRandomFirework(pl.getLocation().clone().add(0, 2, 0));
                                                }, 0);
                                                ParticleUtil.display(pl.getLocation());
                                                Ostrov.sync(() -> ParticleUtil.spawnRandomFirework(pl.getLocation().clone().add(0, 2, 0)), 10);
                                                Ostrov.sync(() -> ParticleUtil.spawnRandomFirework(pl.getLocation().clone().add(0, 2, 0)), 20);
                                            } else {
                                                //op.getPlayer().sendMessage("§cУсловия миссии "+mi.getDisplayName()+ " не выполнены!");
                                                op.getPlayer().sendMessage(TCUtil.form("<red>Условия миссии ")
                                                        .append(mi.displayName()).append(TCUtil.form(" <red>не выполнены!"))
                                                );
                                            }
                                        }

                                    } catch (SQLException ex) {
                                        Ostrov.log_err("§с MissionCmd complete : " + ex.getMessage());
                                    } finally {
                                        try {
                                            if (rs != null) rs.close();
                                            if (stmt != null) stmt.close();
                                        } catch (SQLException ex) {
                                            Ostrov.log_err("§с MissionCmd complete close " + ex.getMessage());
                                        }
                                    }

                                }, 0);
                                yield Command.SINGLE_SUCCESS;
                            }
                            default -> {
                                cs.sendMessage("§cВыбери обну из опций!");
                                yield 0;
                            }
                        };
                    })))
            .build();
    }

    @Override
    public Set<String> aliases() {
        return Set.of("миссии");
    }

    @Override
    public String description() {
        return "Просмотр миссий";
    }
}

 */