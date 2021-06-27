package ru.komiss77.Commands;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Enums.Action;
import ru.komiss77.Enums.ReportStage;
import ru.komiss77.Managers.SM;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;

import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;





public class Report implements CommandExecutor, TabCompleter {
    

    private final List<String> argList = Arrays.asList("fly");
    private static final Map <String,Integer> consoleReportStamp = new HashMap<>(); 
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] arg) {
        final List <String> sugg = new ArrayList<>();
//System.out.println("l="+strings.length+" 0="+strings[0]);
        switch (arg.length) {
            
            case 1:
                //0- пустой (то,что уже введено)
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().startsWith(arg[0])) sugg.add(p.getName());
                }
                break;

            case 2:
                //1-то,что вводится (обновляется после каждой буквы
//System.out.println("l="+strings.length+" 0="+strings[0]+" 1="+strings[1]);
                //if (strings[0].equalsIgnoreCase("build") || strings[0].equalsIgnoreCase("destroy") ) {
                    for (final String s : argList) {
                        if (s.startsWith(arg[0])) sugg.add(s);
                    }

                //}
                break;
                
         /*   case 3:
                //1-то,что вводится (обновляется после каждой буквы
//System.out.println("l="+strings.length+" 0="+strings[0]+" 1="+strings[1]);
                if (strings[1].equalsIgnoreCase("group") ) {
                    for (final Group g:OstrovDB.groups.values()) {
                        if (!g.isStaff() && g.name.startsWith(strings[2])) sugg.add(g.name);
                    }
                    //sugg.addAll(OstrovDB.groups.keySet());
                } else if (strings[1].equalsIgnoreCase("money") || strings[1].equalsIgnoreCase("exp") || strings[1].equalsIgnoreCase("reputation"))  {
                    sugg.add("add");
                    sugg.add("get");
                } else if (strings[1].equalsIgnoreCase("permission") ) {
                    sugg.add("ostrov.perm");
                    sugg.add(Bukkit.getServer().getMotd()+".builder");
                }
                break;

            case 4:
                //1-то,что вводится (обновляется после каждой буквы
//System.out.println("l="+strings.length+" 0="+strings[0]+" 1="+strings[1]);
                //if (strings[0].equalsIgnoreCase("build") || strings[0].equalsIgnoreCase("destroy") ) {
                if (strings[1].equalsIgnoreCase("group") ||strings[1].equalsIgnoreCase("permission")  ) {
                    sugg.add("1h");
                    sugg.add("10h");
                    sugg.add("1d");
                    sugg.add("7d");
                    sugg.add("30d");
                    sugg.add("forever");
                } else if (strings[1].equalsIgnoreCase("money") || strings[1].equalsIgnoreCase("exp") || strings[1].equalsIgnoreCase("reputation"))  {
                    sugg.add("10");
                    sugg.add("100");
                    sugg.add("1000");
                    sugg.add("rnd:0:100");
                }
                //}
                break;*/
        }
        
       return sugg;
    }    
    



   // public Report() {
        //init();
   // }
    

    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        //if ( !OstrovDB.useOstrovData ) {
        //    cs.sendMessage("§cСоединение с БД Острова отключено в конфиге!");
        //    return true;
        //}
        
        //if ( ApiOstrov.getOstrovConnection()==null) {
        //    cs.sendMessage("§cНет соединения с БД Острова!");
        //    return true;
        //}
        
        if (arg.length==0 && cs instanceof Player) {
            if (ApiOstrov.isLocalBuilder(cs, false) || ApiOstrov.hasGroup(cs.getName(), "moder")) {
                openReportMenu( (Player)cs, 0 );
            } else {
                openPlayerReports( (Player)cs, cs.getName(), 0 );
            }
            return true;
        }
        
        if (arg.length<2) {
            cs.sendMessage("§creport <ник> текст жалобы");
            return true;
        }
        
        if (arg[0].equalsIgnoreCase(cs.getName())) {
            cs.sendMessage("§cНа себя жалобы не принимаются!");
            return true;
        } 
        
        String text = "";
        for (int i=1; i<arg.length; i++) {
            text = text+" "+arg[i];
        }
        if (text.length()>128) {
            text = text.substring(0, 128);
        }
        final Player reporter = cs instanceof Player ? (Player) cs : null;
        final Player target = Bukkit.getPlayer(arg[0]);
        
//cs.sendMessage("жалоба на "+arg[0]+", сервер "+Bukkit.getServer().getMotd()+" : "+text);
//cs.sendMessage("Где вы: "+ (cs instanceof ConsoleCommandSender ? "консоль" : LocationUtil.StringFromLoc(((Player) cs).getLocation())) );
//cs.sendMessage( "Где нарушитель: "+( target==null? "нет на сервере" : LocationUtil.StringFromLoc(target.getLocation()) ) );
        

        int id = 0;
        int time = 0;
        
        
        //вычитывать из локальной копии!!
        if (reporter == null) { //консоль
            
            if ( consoleReportStamp.containsKey(arg[0]) && ApiOstrov.currentTimeSec() - consoleReportStamp.get(arg[0]) < 1800) {
                cs.sendMessage("§cНа одного игрока консоль может делать один репорт в пол часа");
                return true;
            }
            consoleReportStamp.put(arg[0], ApiOstrov.currentTimeSec());
            ApiOstrov.sendMessage( Action.REPORT_SERVER, SM.this_server_name, 0, 0, 0, arg[0], target==null? "" : LocationUtil.StringFromLoc(target.getLocation()), text);
            
        } else {
            

            //    ApiOstrov.sendMessage(((Player)cs), Action.REPORT, cs.getName(), id, 1, 0, arg[0], text, LocationUtil.StringFromLoc(reporter.getLocation()), target==null? "" : LocationUtil.StringFromLoc(target.getLocation()), "");
            ApiOstrov.sendMessage( Action.REPORT_PLAYER, reporter.getName(), 0, 0, 0, SM.this_server_name, LocationUtil.StringFromLoc(reporter.getLocation()), arg[0], target==null? "" : LocationUtil.StringFromLoc(target.getLocation()), text, "");
            //при жалобе от игрока ищем ИД предыдущей жалобы
           /* try {

                Statement stmt = ApiOstrov.getOstrovConnection().createStatement();
                //ResultSet rs = stmt.executeQuery( "SELECT `id`, `time` FROM `reports` WHERE `server`='"+Bukkit.getServer().getMotd()+"' AND `fromName`='"+cs.getName()+"' AND `toName`='"+arg[0]+"' LIMIT 1; ");
                ResultSet rs = stmt.executeQuery( "SELECT `id`, `time` FROM `reports` WHERE `fromName`='"+cs.getName()+"' AND `toName`='"+arg[0]+"' LIMIT 1; ");

                if (rs.next()) {
                    id = rs.getInt("id");
                    time = rs.getInt("time");
                    //cs.sendMessage("§f"+ApiOstrov.dateFromStamp(rs.getInt("time"))+" §6Вы уже отправляли жалобу на §b"+arg[0]+" §6на этом сервере.");
                    cs.sendMessage("§f"+ApiOstrov.dateFromStamp(time)+" §6Вы уже отправляли жалобу на §b"+arg[0]);
                    //cs.sendMessage("§aЖалоба обновлена.");
                } else {
                    //ApiOstrov.a
                }
                rs.close();


            } catch (SQLException e) {
                Ostrov.log_err("writeReport SELECT error : "+e.getMessage());
                //e.printStackTrace();
            }
            */
            
        }

        
        
      //  if (id==0) { 
            
            //жалоба на этого игрока ранее не подавалась - создаём новую. Для консоли ИД будет всегда 0 (новая жалоба)
           // if (reporter == null) { //от имени игрока


            //} else { //от сервера


           // }
  /*          
            try {

                PreparedStatement prepStmt = ApiOstrov.getOstrovConnection().prepareStatement("INSERT INTO `reports` ( "
                        + "`server`, "
                        + "`fromName`, "
                        + "`fromLocation`, "
                        + "`toName`, "
                        + "`toLocation`, "
                        + "`text`, "
                        + "`time` ) VALUES "
                        + " ( ?, ?, ?, ?, ?, ?, ? ) ");

                prepStmt.setString(1, Bukkit.getServer().getMotd());
                prepStmt.setString(2, reporter==null ? "консоль" : reporter.getName());
                prepStmt.setString(3, reporter==null ? "" : LocationUtil.StringFromLoc(reporter.getLocation()));
                prepStmt.setString(4, arg[0]);
                prepStmt.setString(5, target==null? "" : LocationUtil.StringFromLoc(target.getLocation()));
                prepStmt.setString(6, text);
                prepStmt.setInt(7, ApiOstrov.currentTimeSec() );

                prepStmt.executeUpdate();
                prepStmt.close();

                cs.sendMessage("§aЖалоба на "+arg[0]+" отправлена.");

            } catch (SQLException e) {
                Ostrov.log_err("writeReport error : "+e.getMessage());
                cs.sendMessage("§cНе удалось отправить жалобу : "+e.getMessage());
                //e.printStackTrace();
            }  

        } else {

            
            //жалоба уже была - обновить
            
            try {
                Statement stmt = ApiOstrov.getOstrovConnection().createStatement();
                stmt.executeUpdate( "UPDATE `reports` SET `text` ='"+text+"', `time` ='"+ApiOstrov.currentTimeSec()+"' WHERE `id`='"+id+"' ; "); 
                //stmt.executeUpdate( "UPDATE `players` SET `factionId`='0', `joinedAt`='0', `settings`='', `perm`='" + reason + "' WHERE `factionId`='" + factionId + "' ;"); 
                stmt.close();
                
                cs.sendMessage("§aЖалоба на "+arg[0]+" обновлена.");
                
            } catch (SQLException e) {
                Ostrov.log_err("writeReport UPDATE error : "+e.getMessage());
                cs.sendMessage("§cНе удалось обновить жалобу : "+e.getMessage());
            }
            
            
        }*/

       // ApiOstrov.sendMessage(
        //    Action.NOTYFY_MODER,
        //    "§6[§eReport§6] §7от §f"+(reporter==null ? "консоль" : p.getName())+" §7на §6"+arg[0]+"§7, сервер "+Bukkit.getServer().getMotd()+(target==null? "" : "§7, "+LocationUtil.StringFromLoc(target.getLocation()))
        //);
        /*
        
        
        boolean hasRecord = false;
        Stage stage = Stage.Нет;
        int fromConsole = 0;
        int fromPlayers = 0;
        
        
        try {

            Statement stmt = ApiOstrov.getOstrovConnection().createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM `reportsCount` WHERE `toName`='"+arg[0]+"' LIMIT 1; ");

            if (rs.next()) {
                hasRecord = true;
                stage = Stage.get(rs.getInt("stage"));
                fromConsole = rs.getInt("fromConsole");
                fromPlayers = rs.getInt("fromPlayers");
            }
            rs.close();

        } catch (SQLException e) {
            Ostrov.log_err("writeReport SELECT reportsCount error : "+e.getMessage());
            //e.printStackTrace();
        }        
        
        if (p==null) fromConsole++;  //счётчик консоди добавляем всегда
        else if (id==0) fromPlayers++;  //от игрока добавляем толькоуникальные жалобы

        
        
        
        if (hasRecord) { //записи уже были - проверить на кик,бан и тд
            //на модеров - не кикать, только запоминать
//System.out.println("hasRecord ! stage="+stage);
            //PM.getOplayer(arg[0]).isStaff
            if (Stage.reachedNext(stage, fromConsole, fromPlayers)) {
                stage = Stage.getNext(stage);
                ApiOstrov.sendMessage(
                    Action.REPORT,
                    stage.action.tag,
                    stage.ammount,
                    arg[0], //имя на кого
                    "§6[§eReport§6] §f" + arg[0] + (p==null ? "§7 -> замечаний от консоли : §c"+fromConsole : "§7 -> жалоб от игроков : §4"+fromPlayers)+"§7, \n§e"+stage.msg
                );
            }

            
            try {

                PreparedStatement prepStmt = ApiOstrov.getOstrovConnection().prepareStatement(
                        " UPDATE `reportsCount` SET `fromConsole`='"+fromConsole+"',"
                                + " `fromPlayers`='"+fromPlayers+"',"
                                + " `stage`='"+stage.ordinal()+"', "
                                + " `lastTime`='"+ApiOstrov.currentTimeSec()+"' WHERE `toName`='"+arg[0]+"' ;"
                );

                prepStmt.executeUpdate();
                prepStmt.close();


            } catch (SQLException e) {
                Ostrov.log_err("writeReport reportsCount : "+e.getMessage());
                //e.printStackTrace();
            }       
            

            
        } else { //первая запись - точно бе будет наказаний, просто сохраняить.
            
            try {

                PreparedStatement prepStmt = ApiOstrov.getOstrovConnection().prepareStatement(
                        "INSERT INTO `reportsCount` ( `toName`, `fromConsole`, `fromPlayers`, `lastTime` ) VALUES ( ?, ?, ?, ? ) ;"
                );
                //??? "fromConsole=VALUES(fromConsole), ");

                prepStmt.setString(1, arg[0]);
                prepStmt.setInt(2, fromConsole);
                prepStmt.setInt(3, fromPlayers);
                prepStmt.setInt(4, ApiOstrov.currentTimeSec() );

                prepStmt.executeUpdate();
                prepStmt.close();


            } catch (SQLException e) {
                Ostrov.log_err("writeReport reportsCount : "+e.getMessage());
                //e.printStackTrace();
            }              
        }
*/

        return true;

    }

    
    
    

    


    
    
    
    
    
    
    
    
    
    private void openReportMenu(final Player p, final int page) {
        p.closeInventory();
        p.sendTitle("", "§5Загрузка данных...", 1, 80, 1);
        Ostrov.async( ()-> {
            
                final List<ClickableItem> reports = new ArrayList<>();
                Statement stmt = null;
                ResultSet rs = null;
                
                try { 
                    stmt = ApiOstrov.getOstrovConnection().createStatement();

                    rs = stmt.executeQuery( "SELECT * FROM `reportsCount` ORDER BY `lastTime` DESC LIMIT "+page*36+",35" ); //ASC
                    
                    List<String>list = new ArrayList<>();
                    ReportStage currentStage;
                    
                    while (rs.next()) {
                        
                        currentStage = ReportStage.get(rs.getInt("stage"));
                        list.clear();
                        
                        for (final ReportStage stage : ReportStage.values()) {
                            if (stage==ReportStage.Нет) continue;
                            list.add( currentStage.ordinal()>=stage.ordinal() ? "§e✔ §6"+stage : "§8"+stage+" при §c"+stage.fromConsole+" §8или §4"+stage.fromPlayers );
                        }
                    
//System.out.println("+++ rs name="+rs.getString("toName"));
                        final String name = rs.getString("toName");
                        reports.add( ClickableItem.of( new ItemBuilder( Material.PLAYER_HEAD )
                            .name(name)
                            .lore("§7Последняя запись:")
                            .lore("§f"+ApiOstrov.dateFromStamp(rs.getInt("lastTime")))
                            .lore("")
                            //.lore("§7Консоль : §c"+rs.getInt("fromConsole")+"§7, Игроки : §4"+rs.getInt("fromPlayers"))
                            .lore("§7Записей от консоли : §c"+rs.getInt("fromConsole"))
                            .lore("§7Жалоб от игроков: §4"+rs.getInt("fromPlayers"))
                            .lore("")
                            .lore("§7Наказания:")
                            .addLore(list)
                            .lore("")
                            .lore("§7ЛКМ - показать записи")
                            .lore("")
                            .lore("* §5Дела модераторов")
                            .lore("§5рассматривает")
                            .lore("§5Административная комисиия.")
                            .lore("")
                            //.lore("§7ПКМ - разобраться на месте")
                            //.lore(ApiOstrov.isLocalBuilder(p, false) || ApiOstrov.hasGroup(p.getName(), "moder") ? "§7Клав. Q - выгнать с Острова" : "")
                            .build(), e -> {
                                if (e.isLeftClick()) {
                                    openPlayerReports(p, name, 0);
                                } else if (e.isRightClick()) {
p.sendMessage("jump не доделан");
                                    //ApiOstrov.sendToServer(p, , name);
                                }
                            }
                        ));
                    }
                    
//System.out.println("+++ reports="+reports);
                            
                    Ostrov.sync( ()-> {
                        p.resetTitle();
                        SmartInventory.builder()
                            .id("ReportMenuMain"+p.getName())
                            .provider(new ReportMenuMain(reports, page))
                            .size(6, 9)
                            .title("§cПросмотр репортов")
                            .build()
                            .open(p);
                    }, 5);
                    
                    
                } catch (SQLException e) { 

                    Ostrov.log_err("§сзагрузка ReportMenuMain - "+e.getMessage());

                } finally {
                    try{
                        if (rs!=null) rs.close();
                        if (stmt!=null) stmt.close();
                    } catch (SQLException e) {
                        Ostrov.log_err("§сзагрузка ReportMenuMain2 - "+e.getMessage());
                    }
                }
            }, 10);        
        
        
    }
    



    

    
 
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    private static final ItemStack fill = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("§8.").build();;

    public class ReportMenuMain implements InventoryProvider {


        private final List<ClickableItem> reports;
        private final int page;

        public ReportMenuMain(final List<ClickableItem> reports, final int page) {
            this.reports = reports;
            this.page = page;
        }



        @Override
        public void init(final Player p, final InventoryContent contents) {
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
            
            contents.fillRow( 4, ClickableItem.empty(fill));

            for (final ClickableItem head : reports) {
//System.out.println("+++init name="+name);
                contents.add(head);
                //final String name = ChatColor.stripColor(head.getItemMeta().getDisplayName());
                //contents.add( ClickableItem.of( head, e -> {
                //        p.sendMessage("подробно об "+name);
                //    }
               // ));            
            
            }



            if (page>0) {
                contents.set( 5, 0, ClickableItem.of(ItemUtils.previosPage, e -> 
                    openReportMenu(p, page-1)
                ));
            }

            contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("закрыть").build(), e -> 
                p.closeInventory()
            ));

            if (reports.size()>=36) {
                contents.set( 5, 8, ClickableItem.of(ItemUtils.nextPage, e -> 
                    openReportMenu(p, page+1)
                ));
            }

        }


    }



    private void openPlayerReports(final Player p, final String toName, final int page) {
        p.closeInventory();
        p.sendTitle("", "§5Загрузка данных...", 1, 80, 1);
        Ostrov.async( ()-> {
            
                final List<ClickableItem> reports = new ArrayList<>();
                Statement stmt = null;
                ResultSet rs = null;
                
                try { 
                    stmt = ApiOstrov.getOstrovConnection().createStatement();

                    rs = stmt.executeQuery( "SELECT * FROM `reports` WHERE `toName`='"+toName+"' ORDER BY `time` DESC LIMIT "+page*36+",35" ); //ASC
                    
                    boolean console;
                    while (rs.next()) {
                        
//System.out.println("+++ rs name="+rs.getString("toName"));
                        console = rs.getString("fromName").equals("консоль");
                        
                        reports.add( ClickableItem.empty(new ItemBuilder( console ? Material.BOOK : Material.PAPER )
                            .name(ApiOstrov.dateFromStamp(rs.getInt("time")))
                            .lore("")
                            .name("§7От: "+( console ? "§bконсоль" : "§6"+rs.getString("fromName")))
                            .lore("")
                            .lore("§7Сервер: "+rs.getString("server"))
                            .lore(console ? "" : "Локция источника:")
                            .lore(console ? "" : rs.getString("toLocation").isEmpty() ? "не определена" : rs.getString("toLocation"))
                            .lore("")
                            .lore("Локция нарушителя:")
                            .lore(rs.getString("toLocation").isEmpty() ? "не определена" : rs.getString("toLocation"))
                            .lore("")
                            .lore("§7Основание:")
                            .addLore( ItemUtils.Gen_lore(null, rs.getString("text"), "§e") )
                            .lore("")
                            .build()
                        ));
                    }
                    
//System.out.println("+++ reports="+reports);
                            
                    Ostrov.sync( ()-> {
                        p.resetTitle();
                        SmartInventory.builder()
                            .id("ReportMenuPlayer"+p.getName())
                            .provider(new ReportMenuPlayer(toName, reports, page))
                            .size(6, 9)
                            .title("§eЗамечания и Жалобы")
                            .build()
                            .open(p);
                    }, 5);
                    
                    
                } catch (SQLException e) { 

                    Ostrov.log_err("§сзагрузка ReportMenuPlayer - "+e.getMessage());

                } finally {
                    try{
                        if (rs!=null) rs.close();
                        if (stmt!=null) stmt.close();
                    } catch (SQLException e) {
                        Ostrov.log_err("§сзагрузка ReportMenuPlayer2 - "+e.getMessage());
                    }
                }
            }, 10);        
        
        
    }
    



    


    public class ReportMenuPlayer implements InventoryProvider {


        private final String toName;
        private int page;
        private final List<ClickableItem> reports;

        public ReportMenuPlayer(final String toName, final List<ClickableItem> reports, final int page) {
            this.toName = toName;
            this.reports = reports;
            this.page = page;
        }



        @Override
        public void init(final Player p, final InventoryContent contents) {
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
            
            contents.fillRow( 4, ClickableItem.empty(fill));

            
            
            if (reports.isEmpty()) {
                contents.set(2,4, ClickableItem.empty(new ItemBuilder( Material.GLASS_BOTTLE)
                 .name("§aЗамечаний и жалоб нет!")
                 .build()));  
            } else {
                reports.forEach((head) -> {
                    contents.add(head);
                });
            }



            if (page>0) {
                contents.set( 5, 0, ClickableItem.of(ItemUtils.previosPage, e -> 
                    openPlayerReports(p, toName, page-1)
                ));
            }

            contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e -> 
                openReportMenu(p, 0)
            ));

            if (reports.size()>=36) {
                contents.set( 5, 8, ClickableItem.of(ItemUtils.nextPage, e -> {
                    if (ApiOstrov.isLocalBuilder(p, false) || ApiOstrov.hasGroup(p.getName(), "moder")) {
                        openPlayerReports(p,  toName, page+1);
                    } else {
                        p.closeInventory();
                    }
                }
                ));
            }

        }






    }

    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

}
    
    
 