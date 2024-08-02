package ru.komiss77.commands;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.hook.DynmapHook;
import ru.komiss77.hook.WGhook;
import ru.komiss77.objects.CaseInsensitiveSet;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


public class CleanCmd implements OCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String type = "type", world = "world";
        return Commands.literal("clean").executes(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof final Player pl)) {
                cs.sendMessage("§eНе консольная команда!");
                return 0;
            }

            if (!ApiOstrov.isStaff(pl) || !pl.isOp()) {
                pl.sendMessage("§cДоступно только операторам!");
                return 0;
            }

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
            return Command.SINGLE_SUCCESS;
        }).then(Resolver.string(type).suggests((cntx, sb)->{
            if (cntx.getSource().getSender() instanceof final Player pl) {
                if (!ApiOstrov.isStaff(pl) || !pl.isOp()) {
                    return sb.buildFuture();
                }
            }
            sb.suggest("dynmap");
            return sb.buildFuture();
        }).then(Resolver.world(world).executes(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (cs instanceof final Player pl) {
                if (!ApiOstrov.isStaff(pl) || !pl.isOp()) {
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
                default -> {
                    cs.sendMessage("§cТакой тип нельзя очистить!");
                    yield 0;
                }
            };
        }))).build();
    }

    @Override
    public List<String> aliases() {
        return List.of("очистка");
    }

    @Override
    public String description() {
        return "Очистка данных";
    }
}
    
    
 
