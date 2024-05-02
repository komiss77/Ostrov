package ru.komiss77;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Table;
import ru.komiss77.events.GroupChangeEvent;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.objects.Group;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.version.Nms;


public class Perm {
    
    private static boolean pathPermissions;
    public static final OstrovConfig localPerms;
    private static final Set<String> defaultPerms;
    private static Map <String,Group> groups; //название в БД, группа


    static {
        groups = new CaseInsensitiveMap<>();
        defaultPerms = new HashSet<>();
        localPerms = Config.manager.getNewConfig("default_perms.yml", new String[]{"", "Права по умолчанию на этом сервере", "наследование не учитывается!", "просто чтобы не захламлять БД острова"} );
        localPerms.addDefault("default", Arrays.asList( 
                "chatformat.default"
            ) 
        );
        localPerms.saveConfig();
    }
    
    public static void loadGroups (final boolean updatePlayerPermissions) {
        
        defaultPerms.clear();
        for (String group : localPerms.getKeys() ) { //дфолтные права будут всегда! загрузать до мускул, или может concurrent!
            if (group.equals("default")) {
                defaultPerms.addAll(localPerms.getStringList(group));
            }
//System.out.println("group="+group+" : "+Arrays.toString(localGroupPermissions.get(group).toArray()));
        }
        loadGroupsDB(updatePlayerPermissions); //2!!! сначала прогрузить allBungeeServersName, или не определяет пермы по серверам

    }
    
    
    
    

    public static @Nullable Group groupByItemName(final String group_chat_name) {
        for (Group gr:groups.values()) {
            if (gr.chat_name.equals(group_chat_name)) return gr;
        }
        return null;
    }        
    
    public static Group getGroup(final String groupName) {
        return groups.get(groupName);
    }    
    
    
    public static int getLimit(final Oplayer op, final String perm) {
        return op.limits.getOrDefault(perm, 0);
    }

    public static Collection<Group> getGroups() {
        return groups.values();
    }

    protected static int getStorageLimit(final Oplayer op) {
        return getLimit(op, "storage")<=90 ? Timer.getTime()+7776000 : Timer.getTime()+getLimit(op, "storage")*86400;
    }
    
    //вызывается из Timer и loadGroups(выше) async!! useOstrovData и соединение чекать до вызова! 
    public static void loadGroupsDB (final boolean updatePlayerPermissions) {
        
        final Map <String,Group> loadedGroup = new CaseInsensitiveMap<>();
        
        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = OstrovDB.getConnection().createStatement();
            rs = stmt.executeQuery( "SELECT * FROM  "+Table.PEX_GROUPS.table_name+" WHERE type != 'off' ;" ); //кинуло на home1 attempted  duplicate class definition
            while (rs.next()) {
                loadedGroup.put(rs.getString("gr"), new Group (rs.getString("gr"), rs.getString("name"), rs.getString("inh"), rs.getString("type"), rs.getInt("price"), rs.getInt("inv_slot"), rs.getString("mat"), rs.getString("group_desc") ) );
            } 
            rs.close();


            rs = stmt.executeQuery( "SELECT * FROM "+Table.PEX_GROUP_PERMS.table_name );

            Group g;
            while (rs.next()) {
                g = loadedGroup.get(rs.getString("gr"));
                if (g!=null) {
//System.out.println("g="+g.name+" perm="+rs.getString("perm")+" inh="+g.inheritance);    
                    final String perm = thisSertverPermission(rs.getString("perm"));
//Ostrov.log_warn("loadGroups sql="+rs.getString("perm")+" perm="+perm);
                    if (perm!=null) {
                        g.permissions.add(perm);
                        for (Group parent : loadedGroup.values()) {
    //System.out.println("child_group="+child_group+" containsKey?"+groups.containsKey(child_group));                        
                            if (parent.inheritance.contains(g.name)) {
                                parent.permissions.add(perm);
                            }
                        }
                    }
                }
            }
            rs.close();
            
            //подгрузка из файла
            //Group g;
            for (String groupName : localPerms.getKeys() ) { //после загрузки с мускул!
                g = loadedGroup.get(groupName);
//Ostrov.log_warn("loadGroups groupName="+groupName+" g="+g);
                if (g!=null) {
                    for (String perm : localPerms.getStringList(groupName)) {
//Ostrov.log_warn(" perm="+g);
                        g.permissions.add(perm);
                        for (Group parent : loadedGroup.values()) {
//System.out.println("child_group="+child_group+" containsKey?"+groups.containsKey(child_group));                        
                            if (parent.inheritance.contains(g.name)) { //if ( !child_group.equals(g.name) && groups.containsKey(child_group) ) {
                                parent.permissions.add(perm);//groups.get(child_group).permissions.add(rs.getString("perm"));
                            }
                        }

                    }

                }
            }
            
            groups = loadedGroup; //если в загрузке была ошибка - загруженное не изменится

            if (updatePlayerPermissions) {
                Ostrov.sync(()->{
                    for (Oplayer op : PM.getOplayers()) {
                        calculatePerms(op.getPlayer(), op, true);
                    }
                }, 0);
            }
                
        } catch (SQLException e) { 
            
            Ostrov.log_warn("§с LoadPlayerGroups error - "+e.getMessage()); 
            
        } finally {
            
            try{
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            } catch (SQLException ex) {
                Ostrov.log_warn("§ LoadPlayerGroups close error - "+ex.getMessage());
            }

            


            Ostrov.log_ok("Database: Загружены группы+права групп! ("+groups.size()+"групп)");
        }
                
    
    }
    
    
   

    
    
    
    
    
    
    //вызывается при:
    //- входе на серв
    //- поступлении новых групп или прав
    //- включении билдера
   // - команда opreload groups
    public static void calculatePerms(final Player p, final Oplayer op, final boolean notify){
        op.isStaff = false;
        
        try {
            op.groups.clear();
            op.user_perms.clear();
            op.limits.clear();
            op.chat_group=" ---- ";
//System.out.println("-calculatePerms notify="+notify); 

            op.user_perms.addAll(defaultPerms);

            if ( !op.getDataString(Data.USER_GROUPS).isEmpty() ) {                       //если у игрока есть группы
                op.chat_group="";
                for (String group_name : op.getDataString(Data.USER_GROUPS).split(",")) {                   //добавляем группы игроку
                    final Group group = groups.get(group_name);
                    if (group!=null) {
                        op.groups.add(group_name);
                        op.user_perms.addAll(group.permissions);
                        op.chat_group=op.chat_group+", "+group.chat_name;
                        if (group.isStaff()) {
                        	op.tabSuffix(" §7{§e"+group.chat_name+"§7}", p);
                            op.isStaff = true;
                        } else {
                            op.tabPrefix("§6✪ §f", p);
                        }
                    } else {
                        if (OstrovDB.useOstrovData) {
                            Ostrov.log_err("У игрока "+op.nik+" есть группа "+group_name+", но её нет в базе групп!" );
                        }
                    }
                }
                op.chat_group=op.chat_group.replaceFirst(", ", "");
            }
   
            
            if ( !op.getDataString(Data.USER_PERMS).isEmpty() ) {                       //если у игрока есть права
//System.out.println("--calculatePerms getBungeeData(Data.USER_PERMS)");   
                for (String perm : op.getDataString(Data.USER_PERMS).split(",")) {
                    perm = thisSertverPermission(perm);
                    if (perm!=null) {
                        op.user_perms.add(perm);
                    }
                }
            }
            
//Bukkit.broadcastMessage("calculatePerms canBeBuilder ? "+ApiOstrov.isLocalBuilder(p));
            if (ApiOstrov.isLocalBuilder(p)) {
                op.user_perms.add("astools.*");
            }
            
            final Iterator <String> it = op.user_perms.iterator();
            
            while (it.hasNext()) {
                String perm = it.next();
                if (perm.startsWith("limit.")) {
                    int idx = perm.lastIndexOf(".");
                    if (idx<=0) continue;
                    int limit = ApiOstrov.getInteger(perm.substring(idx+1));
                    if (limit<0) continue;
                    perm = perm.replaceFirst("limit.","").replaceFirst("."+limit,"");   // limit.home.5 -> home.5
                    //perm = perm.substring(0, idx-1);    // home.5 -> home
                    if (op.limits.containsKey(perm)) {
                        if (limit > op.limits.get(perm)) {
                            op.limits.put(perm, limit);
                        }
                    } else {
                        op.limits.put(perm, limit);
                    }
                    it.remove();
                }
            }
            
            if (!pathPermissions) {
              Nms.pathPermissions();
                pathPermissions = true;
            }
            
            if (op.permissionAttachmen != null) {
                op.permissionAttachmen.remove();
            }
            
            op.permissionAttachmen = p.addAttachment(Ostrov.instance);
           
            for (String perm : op.user_perms) {  //закидываем собранные пермы в атачмент
                op.permissionAttachmen.setPermission(perm, true);
            }

            final int validNew = getStorageLimit(op);
            if (op.getDataInt(Data.VALID)<validNew) {
                op.setData(Data.VALID, validNew);
            }
            
            
        } catch (NumberFormatException | IllegalStateException | ArrayIndexOutOfBoundsException | NullPointerException ex) {
            Ostrov.log_err("Ошибка calculatePermissions "+op.nik+" : "+ex.getMessage());
            p.sendMessage(Ostrov.PREFIX+" §c Ошибка calculatePermissions, сообщите администрации! : "+ex.getMessage());
            ex.printStackTrace();
        }
        
        Bukkit.getPluginManager().callEvent(new GroupChangeEvent ( p, op.groups ) );

        if (notify) {
            p.sendMessage(Component.text( "§3Ваши права группы обновились: §6"+op.chat_group+" §8<<< клик-подробно" )
            	.hoverEvent(HoverEvent.showText(Component.text("§aклик - открыть показать подробно в меню")))
            	.clickEvent(ClickEvent.runCommand("/operm")));
        }



    }

    












    //если на входе ostrov.home  и есть перм ostrov.home.4 вернёт 4
    //добавить учитывая сервер??
    @Deprecated
    public static int getBigestPermValue(final Oplayer op, final String perm) {
        if(perm == null || !perm.contains(".")) return 0;
        
        int final_res=0;
        int current_res;

        for (String userPerm : op.user_perms ) {
            //if ( find.contains(".") && find.startsWith(perm) ) {
                try {
                    current_res=Integer.parseInt(userPerm.replaceFirst(perm+".",""));
                    if (current_res>final_res) {
                        final_res=current_res;
                    }
                } catch (NumberFormatException e) {
                    
                }
//System.out.println("--getBigestPermValue() perm="+ai.getPermission());
                //current_res=Integer.valueOf(find.replaceFirst(perm+".",""));
                //if (current_res>final_res) final_res=current_res;
            //} 
        }
//System.out.println("---getBigestPermValue() result="+final_res);
        return final_res;
    }

    public static boolean hasPermissions(final Oplayer op, final String worldName, String perm) { //при первом поиске worldName должен игнорироваться. Если мир указан, то право только для этого мира
            if( op.user_perms.contains(perm)) return true; //|| op.getPlayer().hasPermission(perm) ) return true; в op.user_perms будут все права!
            int lastDot  = perm.lastIndexOf(".");//проверяем, заменяя концовку после точки на *
            if (lastDot > 0) {
            	perm = perm.substring(0, lastDot)+".*";
                return op.user_perms.contains(perm);
            }
            return false;
        //}
         
     }




    protected static @Nullable String thisSertverPermission(final String perm) {
        final int idx = perm.indexOf(".");
        if (idx>=0) {
            final String serverName = perm.substring(0, idx); //отделить сервер
//System.out.println("-- serverName="+serverName+ " this?"+serverName.equals(GM.this_server_name)+" other?"+GM.allBungeeServersName.contains(serverName));
            if (serverName.equals(Ostrov.MOT_D))  { //если для этого сервера - отрезать сервер
                return (perm.substring(idx+1));//op.user_perms.add(perm.substring(idx+1));
            } else if (GM.allBungeeServersName.contains(serverName) ) { //если начинается с имени другого сервера
                return null;
            } else {
                return perm;
            }
        } else {
            return perm;//op.user_perms.add(perm); //в праве нет точки - может быть и такое, просто закидываем
        }    
    }

    //пермы
    public static boolean canColorChat(final Oplayer op) {
      return op.hasGroup("hero") || op.hasGroup("supermoder");
    }
}
