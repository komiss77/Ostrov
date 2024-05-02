package ru.komiss77.modules.player.mission;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import com.google.common.collect.ImmutableList;
import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.Timer;
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


public class MissionCmd implements CommandExecutor, TabCompleter {
    
    private final List<String> subCmd = Arrays.asList("journal", "select", "accept", "deny", "complete", "forceload");
    
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] strings) {
        
        final List <String> sugg;
        switch (strings.length) {
            
            case 1 -> {
                return subCmd;
            }

            case 2 -> {
                //1-то,что вводится (обновляется после каждой буквы
                if (!PM.exist(cs.getName())) return ImmutableList.of();
                sugg = new ArrayList<>();
                if (strings[1].equalsIgnoreCase("deny") || strings[1].equalsIgnoreCase("complete") ) {
                    for (final int id:PM.getOplayer(cs.getName()).missionIds) {
                        sugg.add(String.valueOf(id));
                    }
                    return sugg;
}
                if (strings[1].equalsIgnoreCase("accept")) {
                    for (final int id:MissionManager.missions.keySet()) {
                        sugg.add(String.valueOf(id));
                    }
                    return sugg;
                }
            }
            
        }
        
        return ImmutableList.of();
    }    
    




    @Override
    public boolean onCommand(final CommandSender cs, final Command cmd, final String string, final String[] arg) {
        
        if (cs==null) return true;
        
        if ( !( cs instanceof Player) ) {
            cs.sendMessage("§cКоманда исполняется от имени игрока!");
            return false;
        }
        
        final Player p =(Player)cs;
        
        if (arg.length>=1 && arg[0].equalsIgnoreCase("forceload")) {
            if (ApiOstrov.isLocalBuilder(cs, true)) {
                MissionManager.loadMissions();
                p.sendMessage("§aМиссии прогружены из БД Острова");
            }
            return true;
        }

        
        final Oplayer op = PM.getOplayer(p);

        if ( op.isGuest ) {
            p.sendMessage("§6Гостям недоступны миссии! Пожалуйста, §bзарегистрируйтесь§6!");
            return false;
        }
        
        
        
        
        
        if (arg.length==0) {
            SmartInventory
                .builder()
                .provider(new MissionMainMenu())
                .size(5, 9)
                .title("§a§lМиссии")
                .build()
                .open(p);
            return true;
        }
       
        
        
        
        switch (arg[0]) {
            
                
            case "journal" -> {
                p.getOpenInventory().close();
                Ostrov.async(()-> {
                    try (Statement stmt = OstrovDB.getConnection().createStatement(); ResultSet rs = stmt.executeQuery( "SELECT * FROM `missions` ORDER BY `activeFrom` DESC" )){ 

                        final ItemStack book = new ItemBuilder(Material.WRITTEN_BOOK)
                                .name("Журнал \"Миссия сегодня\"")
                                .build();
                        BookMeta bookMeta = (BookMeta) book.getItemMeta();
                        
                        while (rs.next()) {
                            final TextComponent.Builder page = Component.text().content("§l§"+rs.getString("nameColor")+rs.getString("name"));
                            //page.append(new ComponentBuilder("\n§1Уровень: §6"+rs.getInt("level")+"§1, Репутация: §6"+rs.getInt("reputation")).create());
                            page.append(Component.text("\n§1Уровень: §6"+rs.getInt("level")+"§1, реп: §6"+rs.getInt("reputation")));
                            page.append(Component.text("\n§1Награда: §6"+rs.getInt("reward")+" §1рил \n(фонд: §6"+rs.getInt("reward")*rs.getInt("rewardFund")+"§1 рил)"));

                            if (Timer.getTime()>rs.getInt("validTo") || Timer.getTime()>rs.getInt("validTo")) {
                                page.append(Component.text("\n§cc "+ApiOstrov.dateFromStamp(rs.getInt("activeFrom"))+"\n§cпо "+ApiOstrov.dateFromStamp(rs.getInt("validTo"))));
                            } else {
                                page.append(Component.text("\n§ac "+ApiOstrov.dateFromStamp(rs.getInt("activeFrom"))+"\n§aпо "+ApiOstrov.dateFromStamp(rs.getInt("validTo"))));
                            }

                            page.append(Component.text("\n§1Требования:"));
                            for (final Entry<String, Integer> e : MissionManager.getMapFromString(rs.getString("request")).entrySet()) {
                                page.append(Component.text("\n§b"+e.getKey()+" §7: §5"+e.getValue()));
                            }

                            bookMeta.addPages(page.build());
                        }
                        
                        bookMeta.setTitle("Журнал \"Миссия сегодня\"");
                        bookMeta.setAuthor("Остров77");
                        book.setItemMeta(bookMeta);  
                        
                        Ostrov.sync( ()-> {
                            p.openBook(book);
                        }, 0);
                        
                    } catch (SQLException e) {
                        Ostrov.log_err("§с MissionCmd journal - "+e.getMessage());
                    }
                }, 0);
            }

                
            
            case "accept" -> {
                if (!MissionManager.canUseCommand(p,"accept"))return true;
                
                if (arg.length==2) { //принятие с указанием ИД
                    final int missionId = ApiOstrov.getInteger(arg[1]);
                    if (missionId<0 || !MissionManager.missions.containsKey(missionId)) {
                        p.sendMessage("§cНет активной миссии с ИД "+arg[1]+"!");
                        return true;
                    }
                    final Mission mi = MissionManager.missions.get(missionId);
                    
                    if (op.missionIds.contains(mi.id)) {
                        p.sendMessage("§cМисия уже принята!");
                        return true;
                    }
                    if (mi.canComplete<=0) {
                        p.sendMessage("§cПризовой фонд исчерпан! :(");
                        return true;
                    }
                    if (Timer.getTime()>mi.validTo) {
                        p.sendMessage("§cМиссия просрочена! :(");
                        return true;
                    }
                    if (op.getStat(Stat.LEVEL)<mi.level) {
                        p.sendMessage("§cДолжен быть уровень не менее §6"+mi.level);
                        return true;
                    }
                    if (op.getStat(Stat.REPUTATION)<mi.reputation) {
                        p.sendMessage("§cДолжна быть репутация не менее §6"+mi.reputation);
                        return true;
                    }
                    final int limit = MissionManager.getLimit(op);
                    if (op.missionIds.size()>=limit) {
                        p.sendMessage("§cЛимит миссий для вашей группы: §e"+limit);
                        return true;
                    }
                    p.getOpenInventory().close();
                    
                    Ostrov.async( ()-> { //в остальных случаях открыт меню выбора
                        OstrovDB.getResultSet(p, "SELECT `missionId`,`completed` FROM `missionsProgress` WHERE `name`='"+op.nik+"' AND `completed`>0", (completed)-> {
                            if (completed==null) {
                                p.sendMessage("§cОшибка запроса к БД!");
                                return;
                            }
                            if (completed.containsKey(String.valueOf(mi.id))) { //уже выполнена
                                p.sendMessage("§5Миссия уже выполнена §d"+ApiOstrov.dateFromStamp((int) completed.get(String.valueOf(mi.id))));
                                return;
                            }
                            //принятие
                            OstrovDB.executePstAsync(p, "INSERT INTO missionsProgress (recordId,name,missionId,taken) VALUES ('"+mi.getRecordID(op.nik)+"', '"+op.nik+"', '"+mi.id+"', '"+Timer.getTime()+"'); "); 
                            OstrovDB.executePstAsync(p, "UPDATE missions SET doing=doing+1 WHERE missionId="+mi.id); //добавить претендента в БД

                            Ostrov.sync( () -> {
                              mi.doing++;
                              op.missionIds.add(missionId);//обновить missionIds
                              op.setData(Data.MISSIONS, ApiOstrov.listToString(op.missionIds, ";"));//обновить Data.MISSION
                              final Title.Times times =  Title.Times.times(Duration.ofMillis(20*50), Duration.ofMillis(20*50), Duration.ofMillis(80*50));
                              ApiOstrov.sendTitle(p, Component.text(""), Component.text("Принятие миссии ", NamedTextColor.GRAY).append(mi.displayName()), times);
                              //p.sendMessage("§fВы приняли миссию "+mi.getDisplayName()+"§f, выполните её до "+ApiOstrov.dateFromStamp(mi.validTo));
                              p.sendMessage(Component.text("Вы приняли миссию ", NamedTextColor.WHITE)
                                .append(mi.displayName())
                                .append(Component.text(", выполните её до "+ApiOstrov.dateFromStamp(mi.validTo), NamedTextColor.WHITE))
                              );
                              p.getWorld().playSound(p.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_2, 1, 1);
                              Bukkit.getPluginManager().callEvent( new MissionEvent(p, mi.name, MissionEvent.MissionAction.Accept));
                            }, 1 );
                        });
                        
                    }, 0);
                    
                    return true;
                } 
                
                
                Ostrov.async( ()-> {
                    
                    final Connection conn = OstrovDB.getConnection();
                    if (conn==null) return;
                    
                    Statement stmt = null;
                    ResultSet rs = null;
                    
                    try {
                        
                        stmt = conn.createStatement();
                        rs = stmt.executeQuery( "SELECT `missionId`,`completed` FROM `missionsProgress` WHERE `name`='"+op.nik+"' AND `completed`>0");
                        
                        final HashMap<Integer,Integer> completed = new HashMap<>();
                        while (rs.next()) {
                            completed.put(rs.getInt("missionId"), rs.getInt("completed"));
                        }
                        rs.close();
                        
                        Ostrov.sync( ()-> {
                            SmartInventory
                                    .builder()
                                    .provider(new MissionSelectMenu(completed))
                                    .size(5, 9)
                                    .title("Актуальные Миссии")
                                    .build()
                                    .open(p);
                        },0);
                        
                    } catch (SQLException ex) {
                        
                        Ostrov.log_err("§с MissionCmd accept : "+ex.getMessage());
                        
                    } finally {
                        
                        try {
                            if (rs!=null) rs.close();
                            if (stmt!=null) stmt.close();
                        } catch (SQLException ex) {
                            Ostrov.log_err("§с MissionCmd accept close "+ex.getMessage());
                        }
                        
                    }
                    
                }, 0);
            }
            
          
                
                
            
            case "select" -> {
                if (!MissionManager.canUseCommand(p,"select"))return true;

                Ostrov.async( ()-> {

                    final Connection conn = OstrovDB.getConnection();
                    if (conn==null) return;

                    try (Statement stmt = conn.createStatement(); 
                            ResultSet rs = stmt.executeQuery( "SELECT `missionId`,`completed` FROM `missionsProgress` WHERE `name`='"+op.nik+"' AND `completed`>0");){


                        final HashMap<Integer,Integer> completed = new HashMap<>();
                        while (rs.next()) {
                            completed.put(rs.getInt("missionId"), rs.getInt("completed"));
                        }
                        //rs.close();

                        Ostrov.sync( ()-> {
                            SmartInventory
                                    .builder()
                                    .provider(new MissionSelectMenu(completed))
                                    .size(5, 9)
                                    .title("§2§lВыбор Миссии")
                                    .build()
                                    .open(p);
                        },0);                                

                    } catch (SQLException ex) {
                        Ostrov.log_err("§с MissionCmd select : "+ex.getMessage());
                    }

                }, 0);
                
                

            }
            
          
                                
                
                
                
                
                
                
                
                
                
                
                
            case "complete" -> {
                if (!MissionManager.canUseCommand(p,"complete"))return true;
                //обновить missionIds и Data.MISSION
                if (arg.length==2) { //выполнить с указанием ИД
                    final int missionId = ApiOstrov.getInteger(arg[1]);
                    if (missionId<0) {  //missionIds подгружаются при входе и меняются при принятии!
                        p.sendMessage("§cНе может быть миссии с ИД "+arg[1]+"!");
                        return true;
                    }
                    final Mission mi = MissionManager.missions.get(missionId);
                    if (mi==null) { 
                        p.sendMessage("§cМиссия с ИД "+missionId+" не подгружена!");
                        return true;
                    }
                    if (!op.missionIds.contains(missionId)) { 
                        p.sendMessage( Component.text("Вы не выполняли миссию ", NamedTextColor.RED).append(mi.displayName()) );
                        //p.sendMessage("§cВы не выполняли миссию "+mi.getDisplayName()+" !");
                        return true;
                    }
                    
                    Ostrov.async( ()-> {
                        
                        final Connection conn = OstrovDB.getConnection();
                        if (conn==null) return;

                        Statement stmt = null;
                        ResultSet rs = null;

                        try {

                            stmt = conn.createStatement();
                            rs = stmt.executeQuery( "SELECT `progress` FROM `missionsProgress` WHERE `recordId`='"+mi.getRecordID(op.nik)+"' AND `completed`='0';" );

                            String progress = null;
                            if (rs.next()) {
                                progress = rs.getString("progress");
//System.out.println("progress="+progress);
                            }
                            rs.close();
                            
                            
                            if (progress==null || progress.isEmpty()) {
                                
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
                                        if (current>=request) {
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
                                    OstrovDB.executePstAsync(p, "UPDATE missionsProgress SET progress='', completed='"+Timer.getTime()+"' WHERE `recordId`='"+mi.getRecordID(op.nik)+"'; ");
                                    OstrovDB.executePstAsync(p, "UPDATE missions SET doing=doing-1,rewardFund=rewardFund-1 WHERE missionId="+missionId); //убавить претендента в БД и фонд
                                    Ostrov.sync( ()-> {
                                        op.missionIds.remove(missionId);
                                        op.setData(Data.MISSIONS, ApiOstrov.listToString(op.missionIds, ";"));//обновить Data.MISSION
                                        //награда
                                        op.setData(Data.RIL, op.getDataInt(Data.RIL)+mi.reward);
                                        op.addStat(Stat.REPUTATION, 1);
                                        op.addStat(Stat.EXP, 10);
                                        p.sendMessage(" ");
                                        final String rc = TCUtils.randomColor();
                                        p.sendMessage(rc + "§m-----§4§k AA §eМиссия завершена §4§k AA" + rc + "§m-----");
                                        p.sendMessage(Component.text(" Миссия §7-> ", NamedTextColor.WHITE).append(mi.displayName()) );
                                        p.sendMessage(" §fНаграда §7-> §e"+mi.reward+" рил");
                                        p.sendMessage(" ");
                                        //поправить счётчики миссии
                                        mi.doing--;
                                        mi.canComplete--;
                                        //эффекты
                                        p.getWorld().playSound(p.getLocation(),Sound.ITEM_GOAT_HORN_SOUND_5, 1, 1);
                                        Bukkit.getPluginManager().callEvent( new MissionEvent(p, mi.name, MissionEvent.MissionAction.Complete));
                                        DonatEffect.spawnRandomFirework(p.getLocation().clone().add(0, 2, 0));
                                    }, 0);
                                    DonatEffect.display(p.getLocation());
                                    Ostrov.sync(()->DonatEffect.spawnRandomFirework(p.getLocation().clone().add(0, 2, 0)), 10);
                                    Ostrov.sync(()->DonatEffect.spawnRandomFirework(p.getLocation().clone().add(0, 2, 0)), 20);
                                } else {
                                    //op.getPlayer().sendMessage("§cУсловия миссии "+mi.getDisplayName()+ " не выполнены!");
                                    op.getPlayer().sendMessage( Component.text("Условия миссии ", NamedTextColor.RED)
                                            .append(mi.displayName()) 
                                            .append(Component.text(" не выполнены!", NamedTextColor.RED))
                                    );
                                }
                            }

                        } catch (SQLException ex) {

                            Ostrov.log_err("§с MissionCmd complete : "+ex.getMessage());

                        } finally {

                            try {
                                if (rs!=null) rs.close();
                                if (stmt!=null) stmt.close();
                            } catch (SQLException ex) {
                                Ostrov.log_err("§с MissionCmd complete close "+ex.getMessage());
                            }

                        }

                    }, 0);
                    return true;
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
                        .id(op.nik+"Миссии")
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
                if (!MissionManager.canUseCommand(p,"deny"))return true;
                //отказ должен быть возможен для устаревших тоже!
                if (arg.length==2) { //отказ с указанием ИД
                    final int missionId = ApiOstrov.getInteger(arg[1]);
                    //if (missionId<0 || !op.missionIds.contains(missionId)) {  //missionIds подгружаются при входе и меняются при принятии!
                    //    p.sendMessage("§cВы не выполняете миссию с ИД "+arg[1]+"!");
                    //    return true;
                    //}
                    p.getOpenInventory().close();
                    if (missionId<0) {  //missionIds подгружаются при входе и меняются при принятии!
                        p.sendMessage("§cНе может быть миссии с ИД "+arg[1]+"!");
                        return true;
                    }
                    //отказ - обработка по выполнению запроса к БД?
                    OstrovDB.executePstAsync(p, "DELETE FROM missionsProgress WHERE `name`='"+op.nik+"' AND `missionId`='"+missionId+"'");
                    //OstrovDB.executePstAsync(p, "DELETE FROM missionsProgress WHERE `recordId`='"'"); 
                    OstrovDB.executePstAsync(p, "UPDATE missions SET doing=doing-1 WHERE missionId="+missionId); //убавить претендента в БД
                    if (MissionManager.missions.containsKey(missionId)) {
                        MissionManager.missions.get(missionId).doing--;
                        Bukkit.getPluginManager().callEvent( new MissionEvent(p, MissionManager.missions.get(missionId).name, MissionEvent.MissionAction.Deny));
                    }
                    if (op.missionIds.remove(missionId)) {//обновить missionIds
                        op.setData(Data.MISSIONS, ApiOstrov.listToString(op.missionIds, ";"));//обновить Data.MISSION
                        p.getWorld().playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, .5f, .5f);
                        p.sendMessage("§5Вы отказались от миссии !");
                    }

                }
            }
            
        }
        
        
        
        
        

            

            //чекаем уровень и репу для перехода на серв вообще
            //hasLevel =  op.getStat(Stat.LEVEL)>=game.level;
            //hasReputation =  op.reputationCalc>=game.reputation;
            //if (!hasLevel || !hasReputation) {
                //p.sendMessage("§cТребуется уровень > "+game.level+" и репутация > "+game.reputation);
               // return true;
           // }
            
//System.out.print("Eco cs="+cs);


        

    



    

    
    
    
    
    
    
    
    
        return true;
    }


}
    
    
 
