package ru.komiss77.modules.player.mission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.Timer;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.SmartInventory;


public class MissionManager {
    
    //public static final int WITHDRAW_MIN = 50;
    public static final int WITHDRAW_MAX = 300;
    
    protected static final Map<Integer,Mission> missions = new HashMap<>();
    private static final List<RecordData> record = new ArrayList<>();
    
    public static final CaseInsensitiveMap<String> customStatsDisplayNames = new CaseInsensitiveMap<>();
    public static final CaseInsensitiveMap<Boolean> customStatsShowAmmount = new CaseInsensitiveMap<>();
    
    protected static int getLimit(final Oplayer op) {
        //if (op.isStaff) return 0;
        if (op.getPlayer().hasPermission("ostrov.missioner")) return 5; //ApiOstrov.hasGroup(op.nik, "missioner")) return 5;
        if (ApiOstrov.isLocalBuilder(op.getPlayer(), false)) return 0;
        return 2;
    }
    
    public static int getMin(final Oplayer op) {
        return (1+op.getStat(Stat.WD_c)) * 5;
    } 
    
    
    public static void tickAsync() {
        if (record.isEmpty()) return;
        final RecordData[] recordCopy = record.toArray(RecordData[]::new);//new ArrayList<>(record);
        record.clear();
//System.out.println("recordCopy="+recordCopy.size());        
       // Ostrov.async( ()-> {
            final Connection conn = OstrovDB.getConnection();
            if (conn==null) return;
            Statement stmt = null;
            ResultSet rs = null;
            PreparedStatement pst = null;
            
            for (final RecordData rd:recordCopy) {
                
                try {
                    stmt = conn.createStatement();
                    rs = stmt.executeQuery( "SELECT `progress` FROM `missionsProgress` WHERE `recordId`='"+rd.recordId+"'; " );

                    String progress = null;
                    boolean skip = false;
                    
                    if (rs.next()) {
                        progress = rs.getString("progress");
    //System.out.println("progress="+progress);
    
                        if (progress.isEmpty()) { //нет прогресса совсем никакого (строка прогресса пустая)
                            
                            progress = rd.customStatName+":"+rd.value; //заносим стату в прогресс (reach тут не интересен-предыдущего значения не было)
                            
                        } else {
                            
                            final int index1 = progress.indexOf(rd.customStatName); //ищем позицию статы в строке

                            if (index1>=0) {   //в строке упоминается стата (т.е.прогресс уже был)
                                final String left = progress.substring(0, index1); //получаем то, что слева от статы
                                final int index2 = progress.indexOf("∫", index1);
                                final String right = index2>0 ? progress.substring(index2) : ""; //получаем то, что справа от статы
                                final String midle = index2>0 ? progress.substring(index1,index2) :  progress.substring(index1); ////получаем середину, т.е. нашу стату
    //System.out.println("midle="+midle);
    //System.out.println("right="+right);
                                //int current = Integer.parseInt(midle.replaceAll("\\D+","")); //из серединки вычисляем значение (убрать всё кроме цифр-косяк! цифры могут быть в названии)
                                if (midle.contains(":")) {
                                    int current = Integer.parseInt(midle.split(":")[1]);
                                    
                                    if (rd.reach) { //при флаге reach записать значение, если оно больше предыдущего
                                        if (current<rd.value) {
                                            progress = left+ rd.customStatName+":"+rd.value +right; //собираем строку обратно с новым достигнутым значением!!
                                        } else {
                                            skip = true; //пропустить запись
                                        }
                                    } else { //без флага добавить к предыдущему значению
                                        current+=rd.value;
                                        progress = left+ rd.customStatName+":"+current +right; //собираем строку обратно
                                    }
                                    //progress = left+rd.customStatName+":"+current+right; //собираем строку обратно
                                }
    //System.out.println("current="+current);
    
    //System.out.println("progress new="+progress);
                            } else { //в стата в строке отсутствует (т.е.прогресса ранее не было)
                                progress = progress+"∫"+rd.customStatName+":"+rd.value; //(reach тут не интересен-предыдущего значения не было)
                            }
                            
                        }
    
    
                       /* if (!progress.isEmpty()) { //

                            final int index1 = progress.indexOf(rd.customStatName); 

                            if (index1>=0) {   //строка со статой есть   //уже есть какой-то прогресс по данной стате
                                final String left = progress.substring(0, index1);
                                final int index2 = progress.indexOf("∫", index1);
                                final String right = index2>0 ? progress.substring(index2) : "";
                                final String midle = index2>0 ? progress.substring(index1,index2) :  progress.substring(index1);
    //System.out.println("midle="+midle);
    //System.out.println("right="+right);
                                int current = Integer.parseInt(midle.replaceAll("\\D+",""));
    //System.out.println("current="+current);
    
                                current+=rd.add;
                                progress = left+rd.customStatName+":"+current+right;
    //System.out.println("progress new="+progress);
                            } else {
                                progress = progress+"∫"+rd.customStatName+":"+rd.add;
                            }

                        } else { //нет прогресса совсем никакого
                            progress = rd.customStatName+":"+rd.add;
                        }*/

                    }
                    rs.close();

                    if (progress==null) {
                        if (PM.exist(rd.name)) {
                            Bukkit.getPlayerExact(rd.name).sendMessage("§cнет прогресса для recordId "+rd.recordId);
                        }
                    } else if (!skip) {
                        pst = conn.prepareStatement("UPDATE `missionsProgress` SET `progress` = '"+progress+"' WHERE `recordId`='"+rd.recordId+"'; ");
                        pst.execute(); //через OstrovDB счётчик статы в задании теряет данные
 //Bukkit.broadcastMessage("record");
                        //OstrovDB.executePstAsync(Bukkit.getPlayer(rd.name), "UPDATE `missionsProgress` SET `progress` = '"+progress+"' WHERE `recordId`='"+rd.recordId+"'; ");
                   // } else {
 //Bukkit.broadcastMessage("skip");
                    }

                } catch (SQLException ex) {

                    Ostrov.log_err("§с MissionManager onCustomStatAdd : "+ex.getMessage());

                } finally {
                    try {
                        if (rs!=null) rs.close();
                        if (stmt!=null) stmt.close();
                        if (pst!=null) pst.close();
                    } catch (SQLException ex) {
                        Ostrov.log_err("§с MissionManager onCustomStatAdd close "+ex.getMessage());
                    }
                }
            }

        //}, 0);
        
        
        
    }
    
    
    
    
    
    
    
    
    
    public static void onStatAdd(final Oplayer op, final Stat stat, final int ammount) {
//System.out.println("onStatAdd "+bp.name+" "+stat+" "+ammount+" ids="+bp.missionIds.toString());
        onCustomStat(op, stat.name(), ammount, false);
    }

    //при флаге reach записать значение, если оно больше предыдущего
    //без флага добавить к предыдущему значению
    public static void onCustomStat(final Oplayer op, final String customStatName, final int value, final boolean reach) {
//Bukkit.broadcastMessage("onCustomStat "+op.nik+" "+customStatName+" "+value+" ids="+op.missionIds.toString()+ " reach?"+reach);   
        if (!OstrovDB.useOstrovData) {
            //op.getPlayer().sendMessage("§cБД острова отключена!");
            return;
        }
        if (op.missionIds.isEmpty()) return;
        Mission mission;
        for (final int id : op.missionIds) {
//System.out.println("find id "+id);
            mission = missions.get(id);
                if (mission!=null) {
                //проверить на действующую
                //миссия просрочена - удалить и лог в журнал
                if (mission.request.containsKey(customStatName)) { //эта стата участвует в миссии
//System.out.println("+"+ammount+" "+customStatName+", миссия "+mission.name);
                    final int recordId = mission.getRecordID(op.nik);
                    record.add(new RecordData(op.nik, recordId, customStatName, value, reach));
                }
            }
        }
    }





    public static void openMissionsMenu(final Oplayer op, final boolean inProfile) {
       
        if (inProfile) {
            op.menu.section = Section.МИССИИ;

            if (op.isGuest) {
    //System.out.println("rawData="+rawData);
                op.menu.current = SmartInventory
                    .builder()
                    .id(op.nik+op.menu.section.name())
                    .provider(new ProfileManageMenu(null, null))
                    .size(6, 9)
                    .title(op.eng ? Section.МИССИИ.item_nameEn : Section.МИССИИ.item_nameRu)
                    .build()
                    .open(op.getPlayer());
                return;
            }
            //profileMode = ProfileManager.ProfileMode.АккаунтыБД;
            op.menu.runLoadAnimations();
        }
        
        Ostrov.async( ()-> {
            
            final List<ClickableItem> buttonsCurrent = new ArrayList<>();
            final List<ClickableItem> buttonsDone = new ArrayList<>();

//System.out.println("missions="+missions);

            try (Statement stmt = OstrovDB.getConnection().createStatement();
                   ResultSet rs = stmt.executeQuery( "SELECT * FROM `missionsProgress` WHERE `name` = '"+op.nik+"' " ) ){


                Mission mission;
                int request;
                int current;
                String displayName;
                Stat stat;
                boolean done;
                
                while (rs.next()) {

                    final List<Component>lore = new ArrayList<>();
                    final int missionId = rs.getInt("missionId");
//System.out.println("missionId="+rs.getInt("missionId"));
                    mission = missions.get(missionId);
                        if (mission!=null) {
                        
                        
                        //lore.add("§7ID: §3"+mission.id);
                        //lore.add("§7Награда: §e"+mission.reward+" рил");
                        //lore.add("§7Призовой фонд: §6"+mission.rewardFund*mission.reward+" рил" + (mission.rewardFund<=0?"§сисчерпан!":""));
                        //lore.add("§7Претенденты: §f"+mission.doing);
                        //ore.add("");
                        //lore.add("§fПринята:");
                        //lore.add("§7"+ApiOstrov.dateFromStamp(rs.getInt("taken")));
                        //lore.add(Component.empty());
                        
                        
                        if (rs.getInt("completed")>0) { //уже выполнена
                        
                            lore.add(Component.empty());
                            lore.add(Component.text("§a§lЗавершена §2"+ApiOstrov.dateFromStamp(rs.getInt("completed"))));
                            lore.add(Component.empty());
                            //lore.add("§8Награда: "+mission.reward+" рил");
                            //lore.add("§8Призовой фонд: "+mission.rewardFund*mission.reward+" рил" + (mission.rewardFund<=0?"исчерпан!":""));
                            //lore.add("§8Претенденты: "+mission.doing);
                            //lore.add(Component.empty());
                            //lore.add("§8Принята:");
                            //lore.add("§8"+ApiOstrov.dateFromStamp(rs.getInt("taken")));
                            //lore.add(Component.empty());
                        
                            buttonsDone.add(ClickableItem.empty(new ItemBuilder(Material.GUNPOWDER)
                                .name(mission.displayName())
                                .setLore(lore)
                                .build()
                            ));

                        } else if (Timer.getTime()<mission.activeFrom) { //еще не началась, возможно когда изменили дату начала уже взятой миссии
                            
                            lore.add(Component.text("§7Награда: §e"+mission.reward+" рил"));
                            lore.add(Component.text( mission.canComplete <= 0 ? "§7Призовой фонд: §6"+mission.canComplete*mission.reward+" рил" : "§сисчерпан!") );
                            //lore.add("§7Претенденты: §f"+mission.doing);
                            lore.add(Component.empty());
                            //lore.add("§fПринята:");
                            //lore.add("§7"+ApiOstrov.dateFromStamp(rs.getInt("taken")));
                            //lore.add(Component.empty());
                            lore.add(Component.text("§bПланируется, до начала:"));
                            lore.add(Component.text("§f"+ApiOstrov.secondToTime(mission.activeFrom-Timer.getTime())));
                            lore.add(Component.empty());
                            //lore.add("§7Уровень не менее §6"+mission.level);
                            //lore.add("§7Репутация не менее §6"+mission.reputation);
                            //lore.add(Component.empty());
                            //lore.addAll(Mission.getRequest(mission));
                            //lore.add(Component.empty());
                            buttonsDone.add(ClickableItem.empty(new ItemBuilder(Material.SUGAR)
                                .name(mission.displayName())
                                .setLore(lore)
                                .build()
                            ));
                            
                        } else if (Timer.getTime()>mission.validTo) { //просрочена
                            
                            lore.add(Component.empty());
                            lore.add(Component.text("§сПросрочена"));
                            lore.add(Component.empty());
                            
                            buttonsDone.add(ClickableItem.empty(new ItemBuilder(Material.GUNPOWDER)
                                .name(mission.displayName())
                                .setLore(lore)
                                .build()
                            ));
                            
                        } else if (mission.canComplete <= 0) { //призовой фонд исчерпан
                            
                            lore.add(Component.text("§f**********************"));
                            lore.add(Component.text("§f*     §eПринята      §f*"));
                            lore.add(Component.text("§f**********************")); 
                            lore.add(Component.empty());
                            lore.add(Component.text("§cПризовой фонд исчерпан!"));
                            lore.add(Component.empty());
                            lore.add(Component.text("§7Клав. Q - §4отказаться"));
                            lore.add(Component.empty());
                            lore.add(Component.text("§сПри отказе от миссии"));
                            lore.add(Component.text("§cвесь прогресс будет потерян!"));

                            buttonsDone.add(ClickableItem.of(new ItemBuilder(Material.GUNPOWDER)
                                .name(mission.displayName())
                                .setLore(lore)
                                .build(), e-> {
                                    if (e.getClick()==ClickType.DROP) {
                                        op.getPlayer().performCommand("mission deny "+missionId);
                                    } else {
                                        PM.soundDeny(op.getPlayer());
                                    }
                                }
                            ));
                            
                        } else { //прогресс
                            //lore.add("§fПринята:");
                            //lore.add("§7"+ApiOstrov.dateFromStamp(rs.getInt("taken")));
                            lore.add(Component.text("§fНаграда§7: §e"+mission.reward+" рил§7, §fПризовой фонд§7: §6"+mission.canComplete*mission.reward+" рил" ));
                            lore.add(Component.text("§fПретенденты§7: §b"+mission.doing ));
                            lore.add(Component.text("§f**********************"));
                            lore.add(Component.text("§f*    §eПринята      §f*"));
                            lore.add(Component.text("§f**********************")); 
                            //lore.add("§7Претенденты: §f"+mission.doing);
                            //lore.add(Component.empty());
                            //lore.add(Component.empty());
                            //lore.add(Component.empty());
                            lore.add(Component.text("§f§lПрогресс:"));
                            
                            final CaseInsensitiveMap<Integer> progressMap = getMapFromString(rs.getString("progress"));
                            done=true;
                            boolean showAmmount;
                            
                            for (String requestName : mission.request.keySet()) {
                                
                                request = mission.request.get(requestName);
                                stat = Stat.fromName(requestName);
                                showAmmount = !customStatsShowAmmount.containsKey(requestName) || customStatsShowAmmount.get(requestName);//customStatsShowAmmount.containsKey(requestName)?customStatsShowAmmount.get(requestName):true;
                                
                                if (stat==null) {
                                    displayName = "§b"+(customStatsDisplayNames.getOrDefault(requestName, requestName))+ (showAmmount?" §7: §d":"");
                                } else {
                                    displayName = stat.game.displayName+"§7, "+stat.desc+"§d";
                                }
                                
                                if (progressMap.containsKey(requestName)) {
                                    
                                    current = progressMap.get(requestName);
                                    if (current>=request) {
                                        if (showAmmount) {
                                            lore.add(Component.text("§a✔ §8"+TCUtils.stripColor(displayName)+current+" ("+request+")"));
                                        } else {
                                            lore.add(Component.text("§a✔ §8"+TCUtils.stripColor(displayName)));
                                        }
                                    } else {
                                        if (showAmmount) {
                                            lore.add(Component.text(displayName+current+" §7из §5"+request));
                                        } else {
                                            lore.add(Component.text(displayName));
                                        }
                                        //lore.add(displayName+current+" §7из §5"+request);
                                        done = false;
                                    }
                                    
                                } else {
                                    
                                    if (showAmmount) {
                                        lore.add(Component.text(displayName+"§fнакопите §5"+request));
                                    } else {
                                        lore.add(Component.text(displayName));
                                    }
                                    done = false;
                                    
                                }
                                
                            }
                            
                            lore.add(Component.empty());
                            if (done) {
                                lore.add(Component.text("§aВсе условия выполнены!"));
                                lore.add(Component.text("§fЛКМ §7- §eЗавершить и получить награду!"));
                                buttonsCurrent.add(ClickableItem.of(new ItemBuilder(mission.mat)
                                    .name(mission.displayName())
                                    .setLore(lore)
                                        .addEnchant(Enchantment.LUCK)
                                        .addFlags(ItemFlag.HIDE_ENCHANTS)
                                    .build(), e-> {
                                            op.getPlayer().performCommand("mission complete "+missionId);
                                        }
                                    )
                                );
                            } else {
                                lore.add(Component.text("§7Клав. Q - §4отказаться"));
                                //lore.add(Component.text("§сПри отказе от миссии"));
                                lore.add(Component.text("§7(§cвесь прогресс будет потерян!§7)"));
                                buttonsCurrent.add(ClickableItem.of(new ItemBuilder(mission.mat)
                                    .name(mission.displayName())
                                    .setLore(lore)
                                    .build(), e-> {
                                        if (e.getClick()==ClickType.DROP) {
                                            op.getPlayer().performCommand("mission deny "+missionId);
                                        } else {
                                            PM.soundDeny(op.getPlayer());
                                        }
                                    }
                                ));
                            }

                        }
                        
                        
                       /* buttons.add(ClickableItem.empty(new ItemBuilder(mission.mat)
                            .name(mission.displayName())
                            .setLore(lore)
                            .build()
                        ));*/
                        
                        
                    } else {
                        
                        buttonsCurrent.add(ClickableItem.of(new ItemBuilder( Material.MUSIC_DISC_11)
                            .name("§7ID: §3"+missionId)
                            .addLore("§cМиссия неактивна")
                            .addLore("")
                            .addLore("§7Клав.Q - §cотказаться")
                            .addLore("")
                            //.addLore("§сПри отказе от миссии")
                            //.addLore("§cвесь прогресс будет потерян!")
                            .addLore("")
                            .build(), e-> {
                                op.getPlayer().performCommand("mission deny "+missionId);
                                MissionManager.openMissionsMenu(op, inProfile);
                            }
                        ));
                        
                    }
                    

                }
                

                Ostrov.sync( ()-> {
                    if (op.getPlayer()!=null) {
                        if (inProfile) {
                            op.menu.stopLoadAnimations();
                            if (op.menu.section==Section.МИССИИ){// && profileMode == ProfileManager.ProfileMode.АккаунтыБД) {
        //System.out.println("rawData="+rawData);
                            op.menu.current = SmartInventory
                                .builder()
                                .id(op.nik+op.menu.section.name())
                                .provider(new ProfileManageMenu(buttonsCurrent, buttonsDone))
                                .size(6, 9)
                                .title(op.eng ? Section.МИССИИ.item_nameEn : Section.МИССИИ.item_nameRu)
                                .build()
                                .open(op.getPlayer());
                            }// else p.sendMessage("уже другое меню"); }
                        } else {
                            SmartInventory
                                .builder()
                                .provider(new MissionManageMenu(buttonsCurrent, buttonsDone))
                                .size(5, 9)
                                .title("§b§lМиссионария")
                                .build()
                                .open(op.getPlayer());
                        }
                    }
                }, 0);

            } catch (SQLException e) { 

                Ostrov.log_err("§с openMissionsMenu - "+e.getMessage());

            } 
            
        }, inProfile ? 20 : 0);
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    public static void openMissionsEditMenu(final Player p) { 
        if (!PM.getOplayer(p.getName()).hasGroup("supermoder") && !PM.getOplayer(p.getName()).hasGroup("ownner")) {
            p.sendMessage("§cУправлять миссиями могут супермодеры!");
            return;
        }
        
        if (!MissionManager.canUseCommand(p,"edit"))return;
        
        ApiOstrov.sendActionBar(p, "§6Загрузка данных...");
        Ostrov.async(()-> {
            
            Statement stmt = null;
            ResultSet rs = null;
//System.out.println("missions="+missions);

            try { 
                stmt = OstrovDB.getConnection().createStatement();
                rs = stmt.executeQuery( "SELECT * FROM `missions` ORDER BY `activeFrom` DESC" );
                final List<Mission>list = new ArrayList<>();
                while (rs.next()) {
                    list.add(fromResultSet(rs));
                }
                
                Ostrov.sync( ()-> {
                    SmartInventory.builder()
                        .id("Миссии")
                        .provider(new MissionSetupMenu(list))
                        .size(6, 9)
                        .title("Миссии")
                        .build()
                        .open(p);
                }, 0);

            } catch (SQLException e) { 

                Ostrov.log_err("§с openMissionsEditMenu - "+e.getMessage());

            } finally {
                try{
                    if (rs!=null) rs.close();
                    if (stmt!=null) stmt.close();
                } catch (SQLException e) {
                    Ostrov.log_err("§с openMissionsEditMenu close - "+e.getMessage());
                }
            }
            
        }, 10);

        
    }
    
    
    public static void editMission(final Player p, final Mission mission) {
        SmartInventory.builder()
            .id("Редактор Миссии")
            .provider(new MissionEditor(mission))
            .size(6, 9)
            .title("Редактор Миссии")
            .build()
            .open(p);
}

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //вызывается из Timer async каждую минуту!!
    public static void loadMissions() {
//System.out.println("loadMissions 1");                                    
        if (!OstrovDB.useOstrovData) return;
        final Connection conn = OstrovDB.getConnection();
        if (conn==null) {
            Ostrov.log_warn("loadMissions - нет соединения с БД!");
            return;
        }
        Statement stmt=null;
        ResultSet rs = null;
        final List<Mission> list = new ArrayList<>();
        
        try {
            stmt = conn.createStatement();
            
            rs = stmt.executeQuery( " SELECT * FROM `missions` WHERE "+Timer.getTime()+">`activeFrom` AND "+Timer.getTime()+"<`validTo`" ); 
            while (rs.next()) {
                final Mission mission = fromResultSet(rs);//new Mission();
                if (mission.request.isEmpty()) {
                    Ostrov.log_warn("миссия "+mission.id+" : не загружена - пустые требования!");
                } else {
                    list.add(mission);
                }
            }
            rs.close();
            
            rs = stmt.executeQuery( " SELECT * FROM `customStats`" ); 
            while (rs.next()) {
                customStatsDisplayNames.put(rs.getString("name"), rs.getString("displayName"));
                customStatsShowAmmount.put(rs.getString("name"), rs.getBoolean("showAmmount"));
            }
//System.out.println("loadMissions list size="+list.size());                                    

            rs.close();
            stmt.close();

        } catch (SQLException ex) { 
            
            Ostrov.log_warn("§4SM Не удалось загрузить миссии: "+ex.getMessage());
            
        } finally {
            try {
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            } catch (SQLException ex) {
                Ostrov.log_warn("§4SM Не удалось закрыть соединение миссии: "+ex.getMessage());
            }
            Ostrov.sync( () -> {
                missions.clear();
                list.forEach( (mission) -> {
                    missions.put(mission.id, mission);
                });
//System.out.println("loadMissions size="+missions.size());                                    
            } ,0);
        }

    }

    
    
    
    
    
    
    
    
    

    public static Material customStatMat(final String customStatName) {
        if (customStatName.length()>=4) {
            final Game game = Game.fromServerName(customStatName.substring(0, 4));
            if (game!=Game.GLOBAL) {
                return Material.matchMaterial(game.mat);
            }
        }
        return Material.NAME_TAG;
    }
        
    
    
    
    protected static CaseInsensitiveMap<Integer> getMapFromString(final String raw) {
        final CaseInsensitiveMap<Integer> map = new CaseInsensitiveMap<>();
        int splitterIndex;
        for (final String progressRaw : raw.split("∫")) {
            splitterIndex = progressRaw.indexOf(":");
            if (splitterIndex>0 && ApiOstrov.isInteger(progressRaw.substring(splitterIndex+1))) {
                map.put(progressRaw.substring(0, splitterIndex), Integer.valueOf(progressRaw.substring(splitterIndex+1)));
            }
        }
        return map;
    }

    private static Mission fromResultSet(final ResultSet rs) throws SQLException {
        final Mission mi = new Mission();
        mi.id = rs.getInt("missionId");
        mi.name  = rs.getString("name");
        mi.nameColor = rs.getString("nameColor");
if (mi.nameColor.length()==1) { //фиксик старого кода в один чар, убрать после обновы всех
    mi.nameColor = "§"+mi.nameColor;
}
        //if (!rs.getString("nameColor").isEmpty()) {
        //    mission.nameColor = TCUtils.getTextColor(rs.getString("nameColor").charAt(0));
        //    if (mission.nameColor==null) {
        //        mission.nameColor = NamedTextColor.WHITE;
        //    }
        //}
        mi.mat  = Material.matchMaterial(rs.getString("mat"));
        mi.level = rs.getInt("level");
        mi.reputation = rs.getInt("reputation");
        mi.reward = rs.getInt("reward");
        mi.doing = rs.getInt("doing");
        mi.canComplete = rs.getInt("rewardFund");
        mi.activeFrom = rs.getInt("activeFrom");
        mi.validTo = rs.getInt("validTo");

        if (mi.mat==null) mi.mat = Material.BEDROCK;
        mi.request = getMapFromString(rs.getString("request"));
        return mi;
    }

    


    protected static boolean canUseCommand(final Player p, final String command) {
        if (GM.GAME!=Game.LOBBY  && !Ostrov.MOT_D.equals("home1")) {
            p.sendMessage("§cУправлять миссиями можно только в лобби!");
            return false;
        }
        return true;
    }

    protected static class RecordData {

        final String name;
        final int recordId;
        final String customStatName;
        final int value;
        final boolean reach;
        
        public RecordData(final String name, final int recordId, final String customStatName, final int value, final boolean reach) {
            this.name = name;
            this.recordId = recordId;
            this.customStatName = customStatName;
            this.value = value;
            this.reach = reach;
        }
        
    }

    
    
}
