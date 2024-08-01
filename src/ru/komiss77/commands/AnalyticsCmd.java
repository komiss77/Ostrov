package ru.komiss77.commands;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.Timer;
import ru.komiss77.enums.ServerType;
import ru.komiss77.modules.games.GM;
import ru.komiss77.utils.DateUtil;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


public class AnalyticsCmd implements OCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("analytics").executes(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof final Player pl)) {
                cs.sendMessage("§eНе консольная команда!");
                return 0;
            }

            if (!ApiOstrov.isStaff(pl)) {
                pl.sendMessage("§cДоступно только персоналу!");
                return 0;
            }

            if (GM.GAME.type != ServerType.LOBBY) {
                cs.sendMessage("§cкоманда рабоает только в лобби");
                return 0;
            }

            if (Timer.has(pl, "anal")) {
                pl.sendMessage("§сДанные загружаются...");
                return 0;
            }
            Timer.add(pl, "anal", 5);

            pl.closeInventory();
            ApiOstrov.sendBossbar(pl, "§5Сбор информации...", 5, Color.PINK, BossBar.Overlay.NOTCHED_6);

            Ostrov.async(() -> {

                final List<ClickableItem> menuItems = new ArrayList<>();
                final Map<Integer, Integer> notRegister = new HashMap<>();
                final Set<Integer> guest = new HashSet<>();
                //int guest = 0;
                //final Map <Integer,Integer>notRegisterTry = new HashMap<>();

                Statement stmt = null;
                ResultSet rs = null;
                //ResultSet rs = null;

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
                calendar.setTimeInMillis(Timer.getTimeStamp());
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);//вычисление текущего понедельника
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.add(Calendar.DATE, -5 * 7); //вычисление начала понедельника 5 недель назад
                final int fiveWeeksAgo = (int) (calendar.getTimeInMillis() / 1000);

                final String begin = calendar.get(Calendar.DATE) + "." + (calendar.get(Calendar.MONTH) + 1);
//Bukkit.broadcastMessage("time="+Timer.getTimeStamp()+" cal="+calendar.getTimeInMillis());
                //final int currentMonday = (int) (calendar.getTimeInMillis()/1000);
                //int fiveWeeksAgo = currentMonday - 7*5*24*60*60; //вычисление понедельника 5 недель назад - это будет первый слот
//if (1==1)return;
                try {
                    stmt = OstrovDB.getConnection().createStatement();

                    //прогружаем счётчик заходов без регистрации
                    rs = stmt.executeQuery("SELECT `time`, `try`, `guest` FROM `notRegister` WHERE `time`>'" + fiveWeeksAgo + "' ORDER BY `time` ASC"); //ASC
                    while (rs.next()) {
                        if (rs.getInt("guest") > 0) {
                            guest.add(rs.getInt("time")); //в итоге прошел гостем
                        } else {
                            notRegister.put(rs.getInt("time"), rs.getInt("try")); //зашел -вышел без регистрации
                        }
                    }
                    rs.close();

                    //SELECT sience,PLAY_TIME FROM `userData` LEFT JOIN `stats` ON `userData`.`userid` = `stats`.`userId`  WHERE `sience`>'1634650674' ORDER BY `sience` ASC
                    //получаем отсортированный список начиная с утра понедельника, 5 недель назад
                    rs = stmt.executeQuery("SELECT sience,PLAY_TIME FROM `userData` LEFT JOIN `stats` ON `userData`.`userid` = `stats`.`userId`  WHERE `sience`>'" + fiveWeeksAgo + "' ORDER BY `sience` ASC"); //ASC

                    int accauntCounter = 0; //колл-во регистраций суточное
                    int plyTimeCounter = 0; //игровое время суточное
                    int accauntTotal = 0; //общее колл-во регистраций за период
                    int plyTimeTotal = 0; //общее игровое время за период
                    int playTimeAverage; //среднее игровое время за сутки
                    int sience;  //дата регистрации аккаунта
                    int playtime; //игровое время аккаунта
                    calendar.set(Calendar.HOUR_OF_DAY, 23); //перевод календаря на конец дня
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    int dayEndStamp = (int) (calendar.getTimeInMillis() / 1000);//fiveWeeksAgo+24*60*60-1; //штамп конца понедельника
                    int dayBeginStamp = dayEndStamp - 86399;//fiveWeeksAgo+24*60*60-1; //штамп начала понедельника
                    //calendar.setTimeInMillis(dayEnd*1000); //переводи календарь для иконок

                    int more6hour = 0;
                    int more3hour = 0;
                    int more1hour = 0;
                    int more15min = 0;
                    int more5min = 0;
                    int less5min = 0;
                    int nonRegCount = 0;
                    int nonRegTry = 0;
                    int guestCount = 0;
                    Material mat = null;

//Bukkit.broadcastMessage("fiveWeeksAgo="+fiveWeeksAgo);

                    while (rs.next()) {

                        sience = rs.getInt("sience");
                        playtime = rs.getInt("PLAY_TIME");

                        if (sience < dayEndStamp && !rs.isLast()) { //время регистрации меньше конца дня - добавляем счётчики.
                            // без rs.isLast не показывало последний день. с ним теряет один аккаунт, но показывает.
                            accauntCounter++; //подсчёт новых аккаунтов
                            accauntTotal++; //подсчёт новых аккаунтов
                            plyTimeCounter += rs.getInt("PLAY_TIME"); //и суммы игрового времени
                            plyTimeTotal += rs.getInt("PLAY_TIME"); //и суммы игрового времени

                            if (playtime > 21600) { //больше 6 часов
                                more6hour++;
                            } else if (playtime > 10800) { //больше 3 часов
                                more3hour++;
                            } else if (playtime > 3600) { //больше часа
                                more1hour++;
                            } else if (playtime > 900) { //больше 15 минут
                                more15min++;
                            } else if (playtime > 300) { //больше 5 минут
                                more5min++;
                            } else { //меньше 5 минут
                                less5min++;
                            }

                        } else {

                            playTimeAverage = accauntCounter > 0 ? plyTimeCounter / accauntCounter : 0; //вычисление среднего игрового времени за сутки

                            if (playTimeAverage > 3600) {  //60*60
                                mat = Material.NETHERITE_HELMET;
                            } else if (playTimeAverage > 2700) {  //45*60
                                mat = Material.DIAMOND_HELMET;
                            } else if (playTimeAverage > 1800) {  //30*60
                                mat = Material.GOLDEN_HELMET;
                            } else if (playTimeAverage > 900) {  //15*60
                                mat = Material.IRON_HELMET;
                            } else if (playTimeAverage > 300) { //5*60
                                mat = Material.CHAINMAIL_HELMET;
                            } else {
                                mat = Material.LEATHER_HELMET;
                            }
                            int amm = accauntCounter / 10; //колл-во аккаунтов делим на 10 для нагладности
                            if (amm < 1) amm = 1;
                            else if (amm > 64) amm = 64; //фильтрик
//Bukkit.broadcastMessage("playTimeAverage="+playTimeAverage+" mat="+mat+" amm="+amm);


                            for (int stamp : notRegister.keySet()) {
                                if (stamp > dayBeginStamp && stamp < dayEndStamp) {
                                    nonRegCount++;
                                    nonRegTry++; //первая попытка в таблице пишется со значением 0
                                    nonRegTry += notRegister.get(stamp);
                                }
                            }

                            for (int stamp : guest) {
                                if (stamp > dayBeginStamp && stamp < dayEndStamp) {
                                    guestCount++;
                                }
                            }


                            menuItems.add(ClickableItem.empty(new ItemBuilder(mat)
                                .amount(amm)
                                .name("§f" + calendar.get(Calendar.DATE) + "." + (calendar.get(Calendar.MONTH) + 1) + ", " + DateUtil.dayOfWeekName(calendar.get(Calendar.DAY_OF_WEEK)))
                                //.addLore("§7")
                                .lore("§7Новых акк.: §b" + accauntCounter + " §7, Гостей: §e" + guestCount)
                                .lore("§5незарегались: §d" + nonRegCount + " §5(попыток:§d" + nonRegTry + "§5)")
                                .lore("§7Новички наиграли: §6" + (plyTimeCounter / 60 / 60) + "ч.")
                                //.addLore("§6"+((int)plyTimeCounter/60/60)+"ч.")
                                //.addLore("§7")
                                .lore("§7Игровое время:")
                                .lore("§a>6 часов: §7" + more6hour)
                                .lore("§2>3 часов: §7" + more3hour)
                                .lore("§3>1 часа: §7" + more1hour)
                                .lore("§6>15 минут: §7" + more15min)
                                .lore("§4>5 минут: §7" + more5min)
                                .lore("§cменее 5 минут: §7" + less5min)
                                .lore("§7")
                                .lore("§7Среднее игровое время за день: ")
                                .lore("§3" + ApiOstrov.secondToTime(playTimeAverage))
                                .lore("§7новых с " + begin + ": §b" + accauntTotal)
                                .lore("§7наиграно с " + begin + ": §6" + (plyTimeTotal / 60 / 60) + "ч.")
                                //.addLore("§6"+((int)plyTimeTotal/60/60)+"ч.")
                                .lore("")
                                .flags(ItemFlag.HIDE_ATTRIBUTES)
                                //.addLore("§7ПКМ - разобраться на месте")
                                //.addLore(ApiOstrov.isLocalBuilder(p, false) || ApiOstrov.hasGroup(p.name(), "moder") ? "§7Клав. Q - выгнать с Острова" : "")
                                .build()
                            ));

                            accauntCounter = 0; //сброс суточного счётчика аккаунтов
                            plyTimeCounter = 0; //сброс суточного счётчика игрового времени
                            more6hour = 0; //сброс диапазонов
                            more3hour = 0;
                            more1hour = 0;
                            more15min = 0;
                            more5min = 0;
                            less5min = 0;
                            guestCount = 0;
                            nonRegCount = 0;
                            nonRegTry = 0;
                            calendar.add(Calendar.DATE, 1);//dayEnd+=24*60*60; //переключаемся на конец след.дня
                            dayEndStamp = (int) (calendar.getTimeInMillis() / 1000);//calendar.setTimeInMillis(dayEnd*1000); //переводи календарь для иконок
                            dayBeginStamp = dayEndStamp - 86399;
//Bukkit.broadcastMessage("accauntCounter="+accauntCounter+" dayEnd="+dayEnd);

                        }

                    }


                    Ostrov.sync(() -> {
                        SmartInventory
                            .builder()
                            .id(pl.getName() + "analytics")
                            .provider(new ShowAnatytics(menuItems))
                            .size(6, 9)
                            .title("Новые аккаунты")
                            .build()
                            .open(pl);
                    }, 0);

                } catch (SQLException e) {

                    Ostrov.log_err("§с openAnalytics - " + e.getMessage());

                } finally {
                    try {
                        if (rs != null) rs.close();
                        if (rs != null) rs.close();
                        if (stmt != null) stmt.close();
                    } catch (SQLException e) {
                        Ostrov.log_err("§с openAnalytics close - " + e.getMessage());
                    }
                }

            }, 20);
            return Command.SINGLE_SUCCESS;
        }).build();
    }

    @Override
    public List<String> aliases() {
        return List.of("аналитика");
    }

    @Override
    public String description() {
        return "Присмотр аналитики";
    }


    private static class ShowAnatytics implements InventoryProvider {


        private final ClickableItem border = ClickableItem.empty(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
            .name("§8.")
            .lore("§8Колличество шлемов - ")
            .lore("§8число новых аккаунтов")
            .lore("§8за день делёное на 10.")
            .lore("§8")
            .lore("§8Материал шлема")
            .lore("§8отображает среднее")
            .lore("§8время игры за день.")
            .build());
        private final List<ClickableItem> buttons;


        public ShowAnatytics(final List<ClickableItem> buttons) {
            this.buttons = buttons;
        }


        @Override
        public void init(final Player p, final InventoryContent content) {
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);


            //линия - разделитель
            content.fillColumn(0, border);
            content.fillColumn(8, border);


            if (buttons.isEmpty()) {

                content.add(ClickableItem.empty(new ItemBuilder(Material.GLASS_BOTTLE)
                    .name("§7нет записей!")
                    .build()
                ));

            } else {

                for (final ClickableItem head : buttons) {
                    content.add(head);
                }

            }


        }


    }


}
    
    
 
