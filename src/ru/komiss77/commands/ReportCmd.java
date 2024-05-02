package ru.komiss77.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.enums.Operation;
import ru.komiss77.enums.ReportStage;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.ProfileManager;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.modules.player.profile.ShowReports;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.SmartInventory;



public class ReportCmd implements CommandExecutor, TabCompleter {
    
    private static final Map <String,Integer> consoleReportStamp = new HashMap<>(); 
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] arg) {
        final List <String> sugg = new ArrayList<>();
        switch (arg.length) {
            
            case 1:
                //0- пустой (то,что уже введено)
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().startsWith(arg[0])) sugg.add(p.getName());
                }
                break;

            case 2:
                //1-то,что вводится (обновляется после каждой буквы
                sugg.add("читы");
                sugg.add("гриф");
                sugg.add("неадекват");
                break;
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
        
        if (cs instanceof Player) {
            //if (ApiOstrov.isLocalBuilder(cs, false) || ApiOstrov.hasGroup(cs.getName(), "moder")) {
                //PM.getOplayer(cs.getName()).menu.openAllReports((Player) cs, 0);//openReportMenuAll((Player)cs, 0 );
            //} else {
            //    openPlayerReports( (Player)cs, cs.getName(), 0 );
            //}
                if (arg.length==0) {
                    
                    openAllReports(cs, PM.getOplayer(cs.getName()), 0);//openReportMenuAll((Player)cs, 0 );
                    return true;
                    
                } else if (arg.length==1) {
                    
                    //if (arg[0].equalsIgnoreCase(cs.getName())) {
                   // } else {
                        openPlayerReports(cs, PM.getOplayer(cs.getName()), cs.getName(), 0);  //report ник - просмотр данных по игроку
                        return true;
                      //  cs.sendMessage("§cНа себя жалобы не принимаются!"); 
                   // }
                    
                }
            //return true;
        }
        
        if (arg.length<2) {
            cs.sendMessage("§creport <ник> текст жалобы");
            return true;
        }
        
        if (arg[0].equalsIgnoreCase(cs.getName())) {
            cs.sendMessage("§cНа себя жалобы не принимаются!");
        //    PM.getOplayer(cs.getName()).menu.openPlayerReports((Player) cs, cs.getName(), 0);
            return true;
        } 
        
        StringBuilder text = new StringBuilder();
        for (int i=1; i<arg.length; i++) {
            text.append(" ").append(arg[i]);
        }
        if (text.length()>128) {
            text = new StringBuilder(text.substring(0, 128));
        }
        final Player reporter = cs instanceof Player ? (Player) cs : null;
        final Player target = Bukkit.getPlayerExact(arg[0]);
        
//cs.sendMessage("жалоба на "+arg[0]+", сервер "+Bukkit.getServer().getMotd()+" : "+text);
//cs.sendMessage("Где вы: "+ (cs instanceof ConsoleCommandSender ? "консоль" : LocationUtil.StringFromLoc(((Player) cs).getLocation())) );
//cs.sendMessage( "Где нарушитель: "+( target==null? "нет на сервере" : LocationUtil.StringFromLoc(target.getLocation()) ) );

        
        //вычитывать из локальной копии!!
        if (reporter == null) { //консоль
            
            if ( consoleReportStamp.containsKey(arg[0]) && ApiOstrov.currentTimeSec() - consoleReportStamp.get(arg[0]) < 1800) {
                cs.sendMessage("§cНа одного игрока консоль может делать один репорт в пол часа");
                return true;
            }
            consoleReportStamp.put(arg[0], ApiOstrov.currentTimeSec());
            SpigotChanellMsg.sendMessage(Bukkit.getOnlinePlayers().stream().findAny().get(), Operation.REPORT_SERVER, Ostrov.MOT_D, 0, 0, 0, arg[0], target==null? "" : LocationUtil.toString(target.getLocation()), text.toString());
            
        } else {
            
            SpigotChanellMsg.sendMessage(reporter, Operation.REPORT_PLAYER, reporter.getName(), 0, 0, 0, Ostrov.MOT_D, LocationUtil.toString(reporter.getLocation()), arg[0], target==null? "" : LocationUtil.toString(target.getLocation()), text.toString(), "");
            //при жалобе от игрока ищем ИД предыдущей жалобы
        }



        return true;

    }

    
    
    

    
    
    
    
    
    
    
    public static void openAllReports (final CommandSender cs, final Oplayer op, final int page) {
        op.menu.section = Section.ПРОФИЛЬ;
        op.menu.profileMode = ProfileManager.ProfileMode.Репорты;
        op.menu.runLoadAnimations();
        
        Ostrov.async(()-> {
            
            final List<ClickableItem> reports = new ArrayList<>();
            boolean hasNext = false;

            Statement stmt = null;
            ResultSet rs = null;

            try { 
                stmt = OstrovDB.getConnection().createStatement();

                rs = stmt.executeQuery( "SELECT * FROM `reportsCount` ORDER BY `lastTime` DESC LIMIT "+page*36+",37" ); //ASC
                    
                List<String>list = new ArrayList<>();
                ReportStage currentStage;
                
                int count = 0;
                
                while (rs.next()) {
                    
                    if (count==36) {
                        hasNext=true;
                        break;
                        
                    } else {
                        
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
                            .addLore("§7Последняя запись:")
                            .addLore("§f"+ApiOstrov.dateFromStamp(rs.getInt("lastTime")))
                            .addLore("")
                            //.addLore("§7Консоль : §c"+rs.getInt("fromConsole")+"§7, Игроки : §4"+rs.getInt("fromPlayers"))
                            .addLore("§7Записей от консоли : §c"+rs.getInt("fromConsole"))
                            .addLore("§7Жалоб от игроков: §4"+rs.getInt("fromPlayers"))
                            .addLore("")
                            .addLore("§7Наказания:")
                            .addLore(list)
                            .addLore("")
                            .addLore("§7ЛКМ - показать записи")
                            .addLore("")
                            .addLore("* §5Дела модераторов")
                            .addLore("§5рассматривает")
                            .addLore("§5Административная комисиия.")
                            .addLore("")
                            //.addLore("§7ПКМ - разобраться на месте")
                            //.addLore(ApiOstrov.isLocalBuilder(p, false) || ApiOstrov.hasGroup(p.getName(), "moder") ? "§7Клав. Q - выгнать с Острова" : "")
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

                Ostrov.sync( ()-> {
                    if (op.menu.section==Section.ПРОФИЛЬ && op.menu.profileMode == ProfileManager.ProfileMode.Репорты) {
//System.out.println("rawData="+rawData);
                    op.menu.stopLoadAnimations();
                    op.menu.current = SmartInventory
                            .builder()
                            .id(op.nik+op.menu.section.name())
                            .provider(new ShowReports(reports, page, next))
                            .size(6, 9)
                            .title("Профиль : Все репорты")
                            .build()
                            .open(op.getPlayer());
                    }// else p.sendMessage("уже другое меню"); }
                }, 0);

            } catch (SQLException e) { 

                Ostrov.log_err("§с openAllReports - "+e.getMessage());

            } finally {
                try{
                    if (rs!=null) rs.close();
                    if (stmt!=null) stmt.close();
                } catch (SQLException e) {
                    Ostrov.log_err("§с openAllReports close - "+e.getMessage());
                }
            }
            
        }, 20);
        
        
        
    
        
        
    }

    
    
    
    
    
    public static void openPlayerReports (final CommandSender cs, final Oplayer op, final String toName, final int page) {
        op.menu.section = Section.ПРОФИЛЬ;
        op.menu.profileMode = ProfileManager.ProfileMode.Репорты;
        op.menu.runLoadAnimations();
        
        Ostrov.async(()-> {
            
            final List<ClickableItem> reports = new ArrayList<>();
            boolean hasNext = false;

            Statement stmt = null;
            ResultSet rs = null;

            try { 
                stmt = OstrovDB.getConnection().createStatement();
                
                rs = stmt.executeQuery( "SELECT * FROM `reports` WHERE `toName`='"+toName+"' ORDER BY `time` DESC LIMIT "+page*36+",37" ); //ASC
                    
                int count = 0;
                boolean console;
                
                while (rs.next()) {
                    
                    if (count==36) {
                        
                        hasNext=true;
                        break;
                        
                    } else {
                        
                        console = rs.getString("fromName").equals("консоль");

                        reports.add( ClickableItem.empty(new ItemBuilder( console ? Material.BOOK : Material.PAPER )
                            .name(ApiOstrov.dateFromStamp(rs.getInt("time")))
                            .addLore("")
                            .name("§7От: "+( console ? "§bконсоль" : "§6"+rs.getString("fromName")))
                            .addLore("")
                            .addLore("§7Сервер: "+rs.getString("server"))
                            //палит где находятся игроки на Даарии / Седне при репорте
                            .addLore(console ? "" : "Локция источника:")
                            .addLore(console ? "" : rs.getString("toLocation").isEmpty() || !ApiOstrov.isLocalBuilder(cs, false) ? "не определена" : rs.getString("toLocation"))
                            .addLore("")
                            .addLore("Локция нарушителя:")
                            .addLore(rs.getString("toLocation").isEmpty() || !ApiOstrov.isLocalBuilder(cs, false) ?  "не определена" : rs.getString("toLocation"))
                            .addLore("")
                            .addLore("§7Основание:")
                            .addLore( ItemUtils.genLore(null, rs.getString("text"), "§e") )
                            .addLore("")
                            .build()
                        ));
                        
                    }
                    count++;
                }
                
                final boolean next = hasNext;

                Ostrov.sync( ()-> {
                    if (op.menu.section==Section.ПРОФИЛЬ && op.menu.profileMode == ProfileManager.ProfileMode.Репорты) {
//System.out.println("rawData="+rawData);
                    op.menu.stopLoadAnimations();
                    op.menu.current = SmartInventory
                            .builder()
                            .id(op.nik+op.menu.section.name())
                            .provider(new ShowReports(reports, page, next))
                            .size(6, 9)
                            .title("Профиль : Репорты на "+ (toName.equals(op.nik) ? "меня" : toName) )
                            .build()
                            .open(op.getPlayer());
                    }// else p.sendMessage("уже другое меню"); }
                }, 0);

            } catch (SQLException e) { 

                Ostrov.log_err("§с openPlayerReports - "+e.getMessage());

            } finally {
                try{
                    if (rs!=null) rs.close();
                    if (stmt!=null) stmt.close();
                } catch (SQLException e) {
                    Ostrov.log_err("§с openPlayerReports close - "+e.getMessage());
                }
            }
            
        }, 20);
        
    }
    
    
    
    

    
    


    

    
 
    
    
    
    
    
    
    
    
    
    
    
        
    
    
    
    
    
    
    
    

}
    
    
 
