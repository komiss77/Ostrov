package ru.komiss77.commands;


import com.google.common.collect.ImmutableList;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.inventory.ItemFlag;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.Timer;
import ru.komiss77.enums.ServerType;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.DateUtil;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;


public class AnalyticsCmd implements CommandExecutor, TabCompleter {
    

    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] arg) {
        //final List <String> sugg = new ArrayList<>();
//System.out.println("l="+strings.length+" 0="+strings[0]);
        switch (arg.length) {
            
            case 1:
                break;

            case 2:
                break;
        }
        
       return ImmutableList.of();
    }    
    



   // public Report() {
        //init();
   // }
    

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( ! (cs instanceof Player) ) {
            cs.sendMessage("§eне консольная команда!");
            return true;
        }
        
       if ( GM.GAME.type != ServerType.LOBBY ) {
            cs.sendMessage("§cкоманда рабоает только в лобби");
            return true;
        }
        
        final Oplayer op = PM.getOplayer(cs.getName());
        if (!op.hasGroup("xpanitely") && !op.hasGroup("owner")) {
            cs.sendMessage("§сНужна группа §expanitely или owner");
            return true;
        }

        final Player p = (Player) cs;
        if (Timer.has(p, "anal")) {
            p.sendMessage( "§сДанные загружаются...");
            return true;
        }
        Timer.add(p, "anal", 5);
        
        p.closeInventory();
        ApiOstrov.sendBossbar(p, "§5Сбор информации...", 5, Color.PINK, BossBar.Overlay.NOTCHED_6);
        
        Ostrov.async(()-> {
            
            final List<ClickableItem> menuItems = new ArrayList<>();
            final Map <Integer,Integer>notRegister = new HashMap<>();
            final Set <Integer>guest = new HashSet<>();
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
            calendar.add(Calendar.DATE, -5*7); //вычисление начала понедельника 5 недель назад
            final int fiveWeeksAgo = (int)(calendar.getTimeInMillis()/1000);
            
            final String begin = calendar.get(Calendar.DATE)+"."+(calendar.get(Calendar.MONTH)+1);
//Bukkit.broadcastMessage("time="+Timer.getTimeStamp()+" cal="+calendar.getTimeInMillis());
            //final int currentMonday = (int) (calendar.getTimeInMillis()/1000);
            //int fiveWeeksAgo = currentMonday - 7*5*24*60*60; //вычисление понедельника 5 недель назад - это будет первый слот
//if (1==1)return;
            try { 
                stmt = OstrovDB.getConnection().createStatement();
                
                //прогружаем счётчик заходов без регистрации
                rs = stmt.executeQuery( "SELECT `time`, `try`, `guest` FROM `notRegister` WHERE `time`>'"+fiveWeeksAgo+"' ORDER BY `time` ASC" ); //ASC
                while (rs.next()) {
                    if (rs.getInt("guest")>0) {
                        guest.add(rs.getInt("time")); //в итоге прошел гостем
                    } else {
                        notRegister.put(rs.getInt("time"), rs.getInt("try")); //зашел -вышел без регистрации
                    }
                }
                rs.close();
                
                //SELECT sience,PLAY_TIME FROM `userData` LEFT JOIN `stats` ON `userData`.`userid` = `stats`.`userId`  WHERE `sience`>'1634650674' ORDER BY `sience` ASC
                //получаем отсортированный список начиная с утра понедельника, 5 недель назад
                rs = stmt.executeQuery( "SELECT sience,PLAY_TIME FROM `userData` LEFT JOIN `stats` ON `userData`.`userid` = `stats`.`userId`  WHERE `sience`>'"+fiveWeeksAgo+"' ORDER BY `sience` ASC" ); //ASC
                    
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
                int dayEndStamp = (int)(calendar.getTimeInMillis()/1000);//fiveWeeksAgo+24*60*60-1; //штамп конца понедельника
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
                    
                    if (sience<dayEndStamp && !rs.isLast()) { //время регистрации меньше конца дня - добавляем счётчики.
                        // без rs.isLast не показывало последний день. с ним теряет один аккаунт, но показывает.
                        accauntCounter++; //подсчёт новых аккаунтов
                        accauntTotal++; //подсчёт новых аккаунтов
                        plyTimeCounter+= rs.getInt("PLAY_TIME"); //и суммы игрового времени
                        plyTimeTotal+= rs.getInt("PLAY_TIME"); //и суммы игрового времени
                        
                        if (playtime>21600) { //больше 6 часов
                            more6hour++;
                        } else if (playtime>10800) { //больше 3 часов
                           more3hour++;
                        } else if (playtime>3600) { //больше часа
                           more1hour++;
                        } else if (playtime>21600) { //больше 15 минут
                           more15min++;
                        } else if (playtime>21600) { //больше 5 минут
                           more5min++;
                        } else { //меньше 5 минут
                           less5min++;
                        }
                        
                    } else {
                        
                        playTimeAverage = accauntCounter>0 ? plyTimeCounter/accauntCounter : 0; //вычисление среднего игрового времени за сутки
                        
                        if (playTimeAverage>3600) {  //60*60
                            mat = Material.NETHERITE_HELMET;
                        } else if (playTimeAverage>2700) {  //45*60
                            mat = Material.DIAMOND_HELMET;
                        } else if (playTimeAverage>1800) {  //30*60
                            mat = Material.GOLDEN_HELMET;
                        } else if (playTimeAverage>900) {  //15*60
                            mat = Material.IRON_HELMET; 
                        } else if (playTimeAverage>300) { //5*60
                            mat = Material.CHAINMAIL_HELMET;
                        } else {
                            mat = Material.LEATHER_HELMET;
                        }
                        int amm = accauntCounter/10; //колл-во аккаунтов делим на 10 для нагладности
                        if (amm<1)amm=1; else if (amm>64) amm=64; //фильтрик
//Bukkit.broadcastMessage("playTimeAverage="+playTimeAverage+" mat="+mat+" amm="+amm);
                        
    
                        for (int stamp : notRegister.keySet()) {
                            if (stamp > dayBeginStamp && stamp<dayEndStamp) {
                                nonRegCount++;
                                nonRegTry++; //первая попытка в таблице пишется со значением 0
                                nonRegTry += notRegister.get(stamp);
                            }
                        }

                        for (int stamp : guest) {
                            if (stamp > dayBeginStamp && stamp<dayEndStamp) {
                                guestCount++;
                            }
                        }


                        menuItems.add( ClickableItem.empty(new ItemBuilder( mat )
                            .setAmount(amm)
                            .name("§f"+calendar.get(Calendar.DATE)+"."+(calendar.get(Calendar.MONTH)+1)+", "+DateUtil.dayOfWeekName(calendar.get(Calendar.DAY_OF_WEEK)))
                            //.addLore("§7")
                            .addLore("§7Новых акк.: §b"+accauntCounter+" §7, Гостей: §e"+guestCount)
                            .addLore("§5незарегались: §d"+nonRegCount+" §5(попыток:§d"+nonRegTry+"§5)")
                            .addLore("§7Новички наиграли: §6"+((int)plyTimeCounter/60/60)+"ч.")
                            //.addLore("§6"+((int)plyTimeCounter/60/60)+"ч.")
                            //.addLore("§7")
                            .addLore("§7Игровое время:")
                            .addLore("§a>6 часов: §7"+more6hour)
                            .addLore("§2>3 часов: §7"+more3hour)
                            .addLore("§3>1 часа: §7"+more1hour)
                            .addLore("§6>15 минут: §7"+more15min)
                            .addLore("§4>5 минут: §7"+more5min)
                            .addLore("§cменее 5 минут: §7"+less5min)
                            .addLore("§7")
                            .addLore("§7Среднее игровое время за день: ")
                            .addLore("§3"+ApiOstrov.secondToTime(playTimeAverage))
                            .addLore("§7новых с "+begin+": §b"+accauntTotal)
                            .addLore("§7наиграно с "+begin+": §6"+((int)plyTimeTotal/60/60)+"ч.")
                            //.addLore("§6"+((int)plyTimeTotal/60/60)+"ч.")
                            .addLore("")
                            .addFlags(ItemFlag.HIDE_ATTRIBUTES)
                            //.addLore("§7ПКМ - разобраться на месте")
                            //.addLore(ApiOstrov.isLocalBuilder(p, false) || ApiOstrov.hasGroup(p.getName(), "moder") ? "§7Клав. Q - выгнать с Острова" : "")
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
                        dayEndStamp = (int)(calendar.getTimeInMillis()/1000);//calendar.setTimeInMillis(dayEnd*1000); //переводи календарь для иконок
                        dayBeginStamp = dayEndStamp - 86399;
//Bukkit.broadcastMessage("accauntCounter="+accauntCounter+" dayEnd="+dayEnd);
                        
                    }
                    
                }
                

                
                
                
                Ostrov.sync( ()-> {
                    SmartInventory
                        .builder()
                        .id(p.getName()+"analytics")
                        .provider(new ShowAnatytics(menuItems))
                        .size(6, 9)
                        .title("Новые аккаунты")
                        .build()
                        .open(p);
                }, 0);

            } catch (SQLException e) { 

                Ostrov.log_err("§с openAnalytics - "+e.getMessage());

            } finally {
                try{
                    if (rs!=null) rs.close();
                    if (rs!=null) rs.close();
                    if (stmt!=null) stmt.close();
                } catch (SQLException e) {
                    Ostrov.log_err("§с openAnalytics close - "+e.getMessage());
                }
            }
            
        }, 20);
        
        
        
    
        return true;
        
    }

    
    

    class ShowAnatytics implements InventoryProvider {


        private final ClickableItem border = ClickableItem.empty(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name("§8.")
                .addLore("§8Колличество шлемов - ")
                .addLore("§8число новых аккаунтов")
                .addLore("§8за день делёное на 10.")
                .addLore("§8")
                .addLore("§8Материал шлема")
                .addLore("§8отображает среднее")
                .addLore("§8время игры за день.")
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
    
    
 
