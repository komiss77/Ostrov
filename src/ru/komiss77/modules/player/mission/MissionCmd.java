package ru.komiss77.modules.player.mission;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.Timer;
import ru.komiss77.commands.OCommand;
import ru.komiss77.commands.args.Resolver;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Stat;
import ru.komiss77.events.MissionEvent;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.DonatEffect;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.SmartInventory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class MissionCmd implements OCommand {

    private final List<String> subCmd = Arrays.asList("journal", "select", "accept", "deny", "complete", "forceload");

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String act = "action", mid = "id";
        return Commands.literal("mission").executes(cntx->{
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
                                try (final Statement stmt = OstrovDB.getConnection().createStatement();
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
                                            page.append(Component.text("\n§cc " + ApiOstrov.dateFromStamp(rs.getInt("activeFrom"))
                                                + "\n§cпо " + ApiOstrov.dateFromStamp(rs.getInt("validTo"))));
                                        } else {
                                            page.append(Component.text("\n§ac " + ApiOstrov.dateFromStamp(rs.getInt("activeFrom"))
                                                + "\n§aпо " + ApiOstrov.dateFromStamp(rs.getInt("validTo"))));
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
                                final Connection conn = OstrovDB.getConnection();
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

                                final Connection conn = OstrovDB.getConnection();
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
                            default: break;
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
                                    OstrovDB.getResultSet(pl, "SELECT `id`,`completed` FROM `missionsProgress` WHERE `name`='"
                                            + op.nik + "' AND `completed`>0", (completed) -> {

                                        if (completed == null) {
                                            pl.sendMessage("§cОшибка запроса к БД!");
                                            return;
                                        }
                                        if (completed.containsKey(String.valueOf(mi.id))) { //уже выполнена
                                            pl.sendMessage("§5Миссия уже выполнена §d" + ApiOstrov
                                                    .dateFromStamp((int) completed.get(String.valueOf(mi.id))));
                                            return;
                                        }
                                        //принятие
                                        OstrovDB.executePstAsync(pl, "INSERT INTO missionsProgress (recordId,name,id,taken) VALUES ('"
                                                + mi.getRecordID(op.nik) + "', '" + op.nik + "', '" + mi.id + "', '" + Timer.getTime() + "'); ");
                                        OstrovDB.executePstAsync(pl, "UPDATE missions SET doing=doing+1 WHERE id=" + mi.id);
                                        //добавить претендента в БД

                                        Ostrov.sync(() -> {
                                            mi.doing++;
                                            op.missionIds.add(id);//обновить missionIds
                                            op.setData(Data.MISSIONS, ApiOstrov.listToString(op.missionIds, ";"));//обновить Data.MISSION
                                            final int times = 50;
                                            ApiOstrov.sendTitle(pl, Component.empty(), TCUtils.form("<gray>Принятие миссии ")
                                                    .append(mi.displayName()), times, times, times);
                                            pl.sendMessage(TCUtils.form("<white>Вы приняли миссию ").append(mi.displayName())
                                                    .append(TCUtils.form("<white>, выполните её до " + ApiOstrov.dateFromStamp(mi.validTo)))
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
                                OstrovDB.executePstAsync(pl, "DELETE FROM missionsProgress WHERE `name`='" + op.nik + "' AND `id`='" + id + "'");
                                //OstrovDB.executePstAsync(pl, "DELETE FROM missionsProgress WHERE `recordId`='"'");
                                OstrovDB.executePstAsync(pl, "UPDATE missions SET doing=doing-1 WHERE id=" + id); //убавить претендента в БД
                                if (MissionManager.missions.containsKey(id)) {
                                    MissionManager.missions.get(id).doing--;
                                    Bukkit.getPluginManager().callEvent(new MissionEvent(pl, MissionManager.missions.get(id).name, MissionEvent.MissionAction.Deny));
                                }
                                if (op.missionIds.remove(id)) {//обновить missionIds
                                    op.setData(Data.MISSIONS, ApiOstrov.listToString(op.missionIds, ";"));//обновить Data.MISSION
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

                                    final Connection conn = OstrovDB.getConnection();
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
                                            op.getPlayer().sendMessage(TCUtils.form("<red>нет прогресса по миссии").append(mi.displayName()));

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
                                                OstrovDB.executePstAsync(pl, "UPDATE missionsProgress SET progress='', completed='"
                                                        + Timer.getTime() + "' WHERE `recordId`='" + mi.getRecordID(op.nik) + "'; ");
                                                OstrovDB.executePstAsync(pl, "UPDATE missions SET doing=doing-1,rewardFund=rewardFund-1 " +
                                                        "WHERE id=" + id); //убавить претендента в БД и фонд
                                                Ostrov.sync(() -> {
                                                    op.missionIds.remove(id);
                                                    op.setData(Data.MISSIONS, ApiOstrov.listToString(op.missionIds, ";"));//обновить Data.MISSION
                                                    //награда
                                                    op.setData(Data.RIL, op.getDataInt(Data.RIL) + mi.reward);
                                                    op.addStat(Stat.REPUTATION, 1);
                                                    op.addStat(Stat.EXP, 10);
                                                    pl.sendMessage(" ");
                                                    final String rc = TCUtils.randomColor();
                                                    pl.sendMessage(rc + "§m-----§4§k AA §eМиссия завершена §4§k AA" + rc + "§m-----");
                                                    pl.sendMessage(Component.text(" Миссия §7-> ", NamedTextColor.WHITE).append(mi.displayName()));
                                                    pl.sendMessage(" §fНаграда §7-> §e" + mi.reward + " рил");
                                                    pl.sendMessage(" ");
                                                    //поправить счётчики миссии
                                                    mi.doing--;
                                                    mi.canComplete--;
                                                    //эффекты
                                                    pl.getWorld().playSound(pl.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_5, 1, 1);
                                                    Bukkit.getPluginManager().callEvent(new MissionEvent(pl, mi.name, MissionEvent.MissionAction.Complete));
                                                    DonatEffect.spawnRandomFirework(pl.getLocation().clone().add(0, 2, 0));
                                                }, 0);
                                                DonatEffect.display(pl.getLocation());
                                                Ostrov.sync(() -> DonatEffect.spawnRandomFirework(pl.getLocation().clone().add(0, 2, 0)), 10);
                                                Ostrov.sync(() -> DonatEffect.spawnRandomFirework(pl.getLocation().clone().add(0, 2, 0)), 20);
                                            } else {
                                                //op.getPlayer().sendMessage("§cУсловия миссии "+mi.getDisplayName()+ " не выполнены!");
                                                op.getPlayer().sendMessage(TCUtils.form("<red>Условия миссии ")
                                                        .append(mi.displayName()).append(TCUtils.form(" <red>не выполнены!"))
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
    public List<String> aliases() {
        return List.of("миссии");
    }

    @Override
    public String description() {
        return "Просмотр миссий";
    }
}
    
    
 
