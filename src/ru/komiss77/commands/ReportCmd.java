package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.enums.Operation;
import ru.komiss77.enums.ReportStage;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.ProfileManager;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.modules.player.profile.ShowReports;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.SmartInventory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class ReportCmd implements OCommand {

    //запрос банжи, если есть - разкодировать raw
    //если пустой - выкачать из снапшота БД

//    private static final Map <String,Integer> consoleReportStamp = new HashMap<>();

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String player = "player", reason = "reason";
        return Commands.literal("report")
            .executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player pl)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                openAllReports(cs, PM.getOplayer(pl), 0);
                return Command.SINGLE_SUCCESS;
            })
            .then(Resolver.string(player).suggests((cntx, sb) -> {
                Bukkit.getOnlinePlayers().forEach(p -> sb.suggest(p.getName()));
                return sb.buildFuture();
            }).executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player p)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                openPlayerReports(cs, PM.getOplayer(p), Resolver.string(cntx, player), 0);
                return Command.SINGLE_SUCCESS;
            }).then(Resolver.string(reason).suggests((cntx, sb) -> {
                sb.suggest("читы");
                sb.suggest("гриф");
                sb.suggest("неадекват");
                return sb.buildFuture();
            }).executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                final String tnm = Resolver.string(cntx, player);
                if (tnm.equalsIgnoreCase(cs.getName())) {
                    cs.sendMessage("§cНа себя жалобы не принимаются!");
                    return 0;
                }

                final String rsn = Resolver.string(cntx, reason);
                final Player pl = cs instanceof Player ? (Player) cs : null;
                final Player target = Bukkit.getPlayer(tnm);

                //вычитывать из локальной копии!!
                if (pl == null) { //консоль
              /*if (consoleReportStamp.containsKey(arg[0]) && ApiOstrov.currentTimeSec() - consoleReportStamp.get(arg[0]) < 1800) {
                cs.sendMessage("§cНа одного игрока консоль может делать один репорт в пол часа");
                return true;
              }
              consoleReportStamp.put(arg[0], ApiOstrov.currentTimeSec());*/
                    SpigotChanellMsg.sendMessage(Bukkit.getOnlinePlayers().stream().findAny().get(), Operation.REPORT_SERVER,
                        Ostrov.MOT_D, 0, 0, 0, tnm, target == null ? "" : LocationUtil.toString(target.getLocation()), rsn);
                } else {
                    SpigotChanellMsg.sendMessage(pl, Operation.REPORT_PLAYER, pl.getName(), 0, 0, 0, Ostrov.MOT_D,
                        LocationUtil.toString(pl.getLocation()), tnm, target == null ? "" : LocationUtil.toString(target.getLocation()), rsn, "");
                }
                return Command.SINGLE_SUCCESS;
            })))
            .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("репорт");
    }

    @Override
    public String description() {
        return "Репорты игрока";
    }

    public static void openAllReports(final CommandSender cs, final Oplayer op, final int page) {
        op.menu.section = Section.ПРОФИЛЬ;
        op.menu.profileMode = ProfileManager.ProfileMode.Репорты;
        op.menu.runLoadAnimations();

        Ostrov.async(() -> {

            final List<ClickableItem> reports = new ArrayList<>();
            boolean hasNext = false;

            Statement stmt = null;
            ResultSet rs = null;

            try {
                stmt = OstrovDB.getConnection().createStatement();

                rs = stmt.executeQuery("SELECT * FROM `reportsCount` ORDER BY `lastTime` DESC LIMIT " + page * 36 + ",37"); //ASC

                List<String> list = new ArrayList<>();
                ReportStage currentStage;

                int count = 0;

                while (rs.next()) {

                    if (count == 36) {
                        hasNext = true;
                        break;

                    } else {

                        currentStage = ReportStage.get(rs.getInt("stage"));
                        list.clear();

                        for (final ReportStage stage : ReportStage.values()) {
                            if (stage == ReportStage.Нет) continue;
                            list.add(currentStage.ordinal() >= stage.ordinal() ? "§e✔ §6" + stage : "§8" + stage + " при §c" + stage.fromConsole + " §8или §4" + stage.fromPlayers);
                        }

                        //System.out.println("+++ rs name="+rs.getString("toName"));
                        final String name = rs.getString("toName");
                        reports.add(ClickableItem.of(new ItemBuilder(ItemType.PLAYER_HEAD)
                                .name(name)
                                .lore("§7Последняя запись:")
                                .lore("§f" + ApiOstrov.dateFromStamp(rs.getInt("lastTime")))
                                .lore("")
                                .lore("§7Записей от консоли : §c" + rs.getInt("fromConsole"))
                                .lore("§7Жалоб от игроков: §4" + rs.getInt("fromPlayers"))
                                .lore("")
                                .lore("§7Наказания:")
                                .lore(list)
                                .lore("")
                                .lore("§7ЛКМ - показать записи")
                                .lore("")
                                .lore("* §5Дела модераторов")
                                .lore("§5рассматривает")
                                .lore("§5Административная комисиия.")
                                .lore("")
                                //.addLore("§7ПКМ - разобраться на месте")
                                //.addLore(ApiOstrov.isLocalBuilder(p, false) || ApiOstrov.hasGroup(p.name(), "moder") ? "§7Клав. Q - выгнать с Острова" : "")
                                .build(), e -> {
                                if (e.isLeftClick()) {
                                    openPlayerReports(cs, op, name, 0);
                                } else if (e.isRightClick()) {
                                    op.getPlayer().sendMessage("jump не доделан");
                                    //ApiOstrov.sendToServer(p, , name);
                                }
                            }
                        ));
                    }
                    count++;
                }

                final boolean next = hasNext;

                Ostrov.sync(() -> {
                    if (op.menu.section == Section.ПРОФИЛЬ && op.menu.profileMode == ProfileManager.ProfileMode.Репорты) {
//System.out.println("rawData="+rawData);
                        op.menu.stopLoadAnimations();
                        op.menu.current = SmartInventory
                            .builder()
                            .id(op.nik + op.menu.section.name())
                            .provider(new ShowReports(reports, page, next))
                            .size(6, 9)
                            .title("Профиль : Все репорты")
                            .build()
                            .open(op.getPlayer());
                    }// else p.sendMessage("уже другое меню"); }
                }, 0);

            } catch (SQLException e) {

                Ostrov.log_err("§с openAllReports - " + e.getMessage());

            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                } catch (SQLException e) {
                    Ostrov.log_err("§с openAllReports close - " + e.getMessage());
                }
            }

        }, 20);


    }


    public static void openPlayerReports(final CommandSender cs, final Oplayer op, final String toName, final int page) {
        op.menu.section = Section.ПРОФИЛЬ;
        op.menu.profileMode = ProfileManager.ProfileMode.Репорты;
        op.menu.runLoadAnimations();

        Ostrov.async(() -> {

            final List<ClickableItem> reports = new ArrayList<>();
            boolean hasNext = false;

            Statement stmt = null;
            ResultSet rs = null;

            try {
                stmt = OstrovDB.getConnection().createStatement();

                rs = stmt.executeQuery("SELECT * FROM `reports` WHERE `toName`='" + toName + "' ORDER BY `time` DESC LIMIT " + page * 36 + ",37"); //ASC

                int count = 0;
                boolean console;

                while (rs.next()) {

                    if (count == 36) {

                        hasNext = true;
                        break;

                    } else {

                        console = rs.getString("fromName").equals("консоль");

                        reports.add(ClickableItem.empty(new ItemBuilder(console ? ItemType.BOOK : ItemType.PAPER)
                            .name(ApiOstrov.dateFromStamp(rs.getInt("time")))
                            .lore("")
                            .lore("§7От: " + (console ? "§bконсоль" : "§6" + rs.getString("fromName")))
                            .lore("")
                            .lore("§7Сервер: " + rs.getString("server"))
                            //палит где находятся игроки на Даарии / Седне при репорте
                            .lore(console ? "" : "Локция источника:")
                            .lore(console ? "" : rs.getString("toLocation").isEmpty() || !ApiOstrov.isLocalBuilder(cs, false) ? "не определена" : rs.getString("toLocation"))
                            .lore("")
                            .lore("Локция нарушителя:")
                            .lore(rs.getString("toLocation").isEmpty() || !ApiOstrov.isLocalBuilder(cs, false) ? "не определена" : rs.getString("toLocation"))
                            .lore("")
                            .lore("§7Основание:")
                            .lore(ItemUtils.genLore(null, rs.getString("text"), "§e"))
                            .lore("")
                            .build()
                        ));

                    }
                    count++;
                }

                final boolean next = hasNext;

                Ostrov.sync(() -> {
                    if (op.menu.section == Section.ПРОФИЛЬ && op.menu.profileMode == ProfileManager.ProfileMode.Репорты) {
//System.out.println("rawData="+rawData);
                        op.menu.stopLoadAnimations();
                        op.menu.current = SmartInventory
                            .builder()
                            .id(op.nik + op.menu.section.name())
                            .provider(new ShowReports(reports, page, next))
                            .size(6, 9)
                            .title("Профиль : Репорты на " + (toName.equals(op.nik) ? "меня" : toName))
                            .build()
                            .open(op.getPlayer());
                    }// else p.sendMessage("уже другое меню"); }
                }, 0);

            } catch (SQLException e) {

                Ostrov.log_err("§с openPlayerReports - " + e.getMessage());

            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                } catch (SQLException e) {
                    Ostrov.log_err("§с openPlayerReports close - " + e.getMessage());
                }
            }

        }, 20);

    }


}
    
    
 
