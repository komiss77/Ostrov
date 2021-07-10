package ru.komiss77.modules.player;


import ru.komiss77.modules.games.GM;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.komiss77.Cfg;
import ru.komiss77.enums.Data;
import ru.komiss77.events.GroupChangeEvent;
import ru.komiss77.Ostrov;
import ru.komiss77.LocalDB;
import ru.komiss77.OstrovDB;
import ru.komiss77.scoreboard.NameTag.NametagManager;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.InventoryManager;






public class PM {
    
    private static final Map <String, Oplayer> oplayers = new ConcurrentHashMap<>();
    public static boolean use_preffix_suffix_wothout_deluxechat;
    public static String prefix_p_n;   //разделители
    public static String prefix_n_s;   //разделители
    public static String prefix_s_m;   //разделители
    public static boolean ostrovStatScore = false;    
    public static boolean tablist_header_footer = false;    
    //public static int sec = 0;    
    public static NametagManager nameTagManager;
    public static InventoryManager im = InventoryManager.get();
    
    public static void Init() {
        LoadVars();
    }
    
    public static void postWorldLoadInit() {
        if (nameTagManager==null && Cfg.GetCongig().getBoolean("modules.name_tag_manager")) {
            nameTagManager = new NametagManager(Ostrov.GetInstance());
            Ostrov.log_ok ("§aNametagManager инициализирован!");

        }
    }
  

    public static void LoadVars() {
        use_preffix_suffix_wothout_deluxechat = Cfg.GetCongig().getBoolean("system.prefix.use_preffix_suffix_wothout_deluxechat");
        prefix_p_n = Cfg.GetCongig().getString("system.prefix.prefix_name_space");
        prefix_n_s = Cfg.GetCongig().getString("system.prefix.name_suffix_space");
        prefix_s_m = Cfg.GetCongig().getString("system.prefix.suffix_message_space");
        ostrovStatScore = Cfg.GetCongig().getBoolean("player.show_ostrov_info_on_scoreboard");
        tablist_header_footer = Cfg.GetCongig().getBoolean("player.set_tab_list_header_footer");
        
    }

    public static void ReLoadVars() {
        LoadVars();
    }
    
    public static Collection<Oplayer> getOplayers() {
        return oplayers.values();
    }
    
    public static void createOplayer(final Player p) {
        if (!exist(p.getName())) oplayers.put( p.getName(), new Oplayer(p) );
        //if (Ostrov.debug) {
        //    PM.getOplayer(p.getName()).bungeeDataInject("");
        //    PM.getOplayer(p.getName()).bungeeStatInject("");
        //}
    }

    public static Oplayer getOplayer(final String nik) {
        return oplayers.get(nik);
    }
    public static Oplayer getOplayer(final Player p) {
        //if (p==null || !p.isOnline()) return null;
        //if (oplayers.containsKey(p.getName())) 
        return oplayers.get(p.getName());
        //else {
        //    final Oplayer op = new Oplayer(p);
        //    oplayers.put( p.getName(), op );
        //    return op;
        //}
    }
    
    public static boolean exist (final String nik) {
        return oplayers.containsKey(nik) && Bukkit.getPlayer(nik)!=null;
        //переделать на runable
    }
    public static void remove (final String nik) {
        if (oplayers.containsKey(nik)) oplayers.remove(nik);
        //переделать на runable
    }

    public static int getOnlineCount() {
        return oplayers.size();
    }
    public static boolean hasOplayers() {
        return !oplayers.isEmpty();
    }

    public static void onDisable() {
        oplayers.clear();
    }

    
    
    
    public static void onExit(final Player p) {                                          //процедура выхода с сервера
        final String name = p.getName();
        if (!oplayers.containsKey(name)) return;
        
     //   getOplayer(name).onExit();
        
        if (!LocalDB.useLocalData) {
            oplayers.remove(name);
            return;
        }
        
        if (getOplayer(name).mysqldata_loaded) { //сохраняем, если было реально загружено!
            
            int pweather = -1;
                if (p.getPlayerWeather() != null) {
                    if (p.getPlayerWeather()==WeatherType.CLEAR) pweather=0; 
                    else if (p.getPlayerWeather()==WeatherType.DOWNFALL) pweather=1;
                }
                //сохранение в авинхроне, удаляется в finally
            getOplayer(name).saveLocalData(
                    pweather,
                    p.getAllowFlight(), 
                    ((int)(p.getFlySpeed()*10)),
                    ((int)(p.getWalkSpeed()*10)), 
                    p.isPlayerTimeRelative(),
                    ((int)(p.getPlayerTimeOffset()/1000))
            );

        }  else {
            oplayers.remove(name);
        }

    }














// -------------------------- Настройки ----------------------------------------
    public static Inventory OP_Get_settings(Player p) {
        if (oplayers.get(p.getName()).settings==null) {
            oplayers.get(p.getName()).settings=ItemUtils.create_settings_inv(p);
        } else {
            oplayers.get(p.getName()).settings= ItemUtils.update_settings( p, oplayers.get(p.getName()).settings);
        }
        return oplayers.get(p.getName()).settings;
    }
    public static void OP_Set_settings(Player p, Inventory set ) {
        oplayers.get(p.getName()).settings=set;
    }

//------------------------------------------------------------------------------










//---------------------- Режим боя, сброс инвентаря ----------------------------
    public static boolean inBattle (String nik) {
        return oplayers.containsKey(nik) && getOplayer(nik).pvp_time>0;
    }
    public static int inBattle_time_left (String nik) {
    //System.out.println(" ???? inBattle "+nik+"  time:"+CMD.pvp_battle_time+" -> "+Timer.CD_has(nik, "pvp") );
        return oplayers.containsKey(nik)? getOplayer(nik).pvp_time : 0;
    }

//------------------------------------------------------------------------------











//---------------------------------- Дома, положение в мирах, тп -------------------
public static void OP_SetHome (Player p, String home) {
    if (oplayers.containsKey(p.getName())) {
        oplayers.get(p.getName()).SetHome(home,p.getLocation());
        LocalDB.Save_Home(p.getName(), oplayers.get(p.getName()));
    }
}
public static Set <String> OP_GetHomeList (String nik) {
    if (oplayers.containsKey(nik)) return oplayers.get(nik).HomeList();
    else return new HashSet<>();
}
public static Location OP_GetHomeLocation (Player p, String home) {
    return  oplayers.get(p.getName()).GetHomeLoc(home);
}
public static void OP_DelHome (Player p, String home) {
    oplayers.get(p.getName()).DelHome(home);
    LocalDB.Save_Home(p.getName(), oplayers.get(p.getName()));
}
public static void OP_Set_world_position (Player p, String world) {
    oplayers.get(p.getName()).Set_world_position(world,p.getLocation());
}
public static Set <String> OP_Get_world_positions (String nik) {
    return oplayers.get(nik).Get_world_positions();
}
public static Location OP_Get_world_position_at (Player p, String world) {
    return  oplayers.get(p.getName()).Get_world_position_at(world);
}
public static Location OP_Get_back_location (String nik) {
    return  oplayers.get(nik).Get_back_location();
}
public static void OP_Set_back_location (String nik, Location loc) {
      oplayers.get(nik).Set_back_location(loc);
}

//------------------------------------------------------------------------------










    //------------------- Наборы ------------------------------------------
    public static boolean Kit_has_acces(String nik, String kit) {
        return oplayers.get(nik).Has_kit_acces(kit);
    }
    public static void Kit_add_acces(String nik, String kit) {
        oplayers.get(nik).Add_kit_acces(kit);
    }
    public static void Kit_remove_acces(String nik, String kit) {
        oplayers.get(nik).Remove_kit_acces(kit);
    }
    public static void Kit_recieved(String nik, String kit) {
        oplayers.get(nik).Kit_recieved(kit);
    }
    public static int Kit_last_acces(String nik, String kit) {
        return oplayers.get(nik).Kit_last_acces(kit);
    }
    //------------------------------------------------------------------------------













// ------------------------- Игровое время ----------------------------

//public static int getPlytime(String nik) {
 //   return oplayers.get(nik).getStat(Stat.PLAY_TIME);
//}
//public static String getDisplayPlytime ( String nik ) {
 //   return ApiOstrov.secondToTime(getPlytime(nik) );
//}  
// -----------------------------------------------------------------------------






// ---------------------- Счетчики ---------------------------------------------
public static void Addbplace(String nik) { 
    if (oplayers.containsKey(nik)) oplayers.get(nik).Addbplace(); }        
public static void Addbbreak(String nik) { 
    if (oplayers.containsKey(nik)) oplayers.get(nik).Addbbreak(); }
public static void Addmobkill(String nik) { 
    if (oplayers.containsKey(nik)) oplayers.get(nik).Addmobkill(); }
public static void Addmonsterkill(String nik) { 
    if (oplayers.containsKey(nik)) oplayers.get(nik).Addmonsterkill(); }
public static void Addpkill(String nik) { 
    if (oplayers.containsKey(nik)) oplayers.get(nik).Addpkill(); }
public static void Addbdead(String nik) {
    if (oplayers.containsKey(nik)) oplayers.get(nik).Addbdead(); }
public static int Getbplace(String nik) { return oplayers.get(nik).Getbplace(); }
public static int Getbbreak(String nik) { return oplayers.get(nik).Getbbreak(); }
public static int Getmobkill(String nik) { return oplayers.get(nik).Getmobkill(); }
public static int Getmonsterkill(String nik) { return oplayers.get(nik).Getmonsterkill(); }
public static int Getpkill(String nik) { return oplayers.get(nik).Getpkill(); }
public static int Getbdead(String nik) { return oplayers.get(nik).Getbdead(); }
//------------------------------------------------------------------------------




   public static void soundDeny(final Player p) {
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1);
    }











    
    public static void calculatePerms(final Player p, final Oplayer op, final boolean notify){
        op.isStaff = false;
        
        //final Player p = getPlayer();
        try {
            op.groups.clear();
            op.user_perms.clear();
            op.chat_group=" ---- ";
//System.out.println("-calculatePerms notify="+notify); 
            
            //for (PermissionAttachmentInfo  ai : getPlayer().getEffectivePermissions()) {  //делать до удаления permissionAttachmen!
            //for (String perm : OstrovDB.default_permissions) {  //закидываем дефолтные из файлика permissions.yml
//System.out.println("+"+ai.getPermission());
            //if (OstrovDB.localGroupPermissions.containsKey("default")) {
            //    user_perms.addAll(OstrovDB.localGroupPermissions.get("default"));
           // }
            op.user_perms.addAll(OstrovDB.defaultPerms);
//System.out.println("--calculatePerms 2");        
//дефолтные слетают. сделать нах файлик в острове!
            //for (PermissionAttachmentInfo  ai : getPlayer().getEffectivePermissions()) {  //закидываем дефолтные из файлика permissions.yml

            if ( !op.getDataString(Data.USER_GROUPS).isEmpty() ) {                       //если у игрока есть группы
                op.chat_group="";
//System.out.println("--calculatePerms");        
                    for (String group_name : op.getDataString(Data.USER_GROUPS).split(",")) {                   //добавляем группы игроку
//System.out.println("--calculatePerms group_name="+group_name);        
                        if (OstrovDB.groups.containsKey(group_name)) {   
                            op.groups.add(group_name);
                            op.chat_group=op.chat_group+", "+OstrovDB.groups.get(group_name).chat_name;
                            //if (SM.this_server_name.length()!=4) { //на играх не ставим!
                                if (OstrovDB.groups.get(group_name).isStaff()) {
                                    op.tab_list_name_siffix = "§7(§e"+OstrovDB.groups.get(group_name).chat_name+"§7)";
                                    op.isStaff = true;
                                } else {
                                    op.tab_list_name_prefix = "§6✪ §f";
                                    //tab_list_name_color = "§f";
                                }
                            //}
                        } else {
                            if (OstrovDB.useOstrovData) Ostrov.log_err("У игрока "+op.nik+" есть группа "+group_name+", но её нет в базе групп!" );
                        }
                    }
                    op.chat_group=op.chat_group.replaceFirst(", ", "");
            }
           

            //for (String group_name : getBungeeData(Data.USER_GROUPS).split(",")) {                   //добавляем права групп игроку
            if (!op.groups.isEmpty()) {
                for (String group_name : op.groups) {                   //добавляем права групп игроку
                    //OstrovDB.groups.get(group_name).permissions.stream().forEach((perm) -> { //в группах права уже с учётом наследования!
                    for (String perm : OstrovDB.groups.get(group_name).permissions) { //в группах права уже с учётом наследования!
//System.out.println("----setPermission "+perm);   
                        //permissionAttachmen.setPermission(perm, true);
                        op.user_perms.add(perm);
                        //if (OstrovDB.localGroupPermissions.containsKey(group_name)) {
                        //    user_perms.addAll(OstrovDB.localGroupPermissions.get(group_name));
                            
                        //}
                    }
                }
            }

//System.out.println("");
//System.out.println(" +++++ Группы игрока: " + groups);
//System.out.println("");
//System.out.println(" +++++ Права игрока: " + user_perms);
//System.out.println("");

//System.out.println("--allservers="+SM.allBungeeServersName);        

            if ( !op.getDataString(Data.USER_PERMS).isEmpty() ) {                       //если у игрока есть права
//System.out.println("--calculatePerms getBungeeData(Data.USER_PERMS)");   
                String split;
                    for (String perm : op.getDataString(Data.USER_PERMS).split(",")) {                   //добавляем группы игроку
                        
                        //perm=perm;  //отделить сервер
                        
                        if (perm.startsWith("allservers.")) {
                            perm = perm.replaceFirst("allservers.", "");
                            op.user_perms.add(perm);
//System.out.println("++личное право="+perm);        
                        } else if (perm.startsWith(GM.this_server_name+".")){
                            perm = perm.replaceFirst(GM.this_server_name+".", "");
                            op.user_perms.add(perm);
//System.out.println("++личное право="+perm);        
                        } else {
//System.out.println("1 perm="+perm);
//System.out.println("2 split="+perm.split("."));
//System.out.println("3 "+(perm.split("\\."))[0]);
                            split=(perm.split("\\."))[0];       //желательно проверять- если начинается с имени другого сервера, то пропускать. Но это надо
                            if (!GM.allBungeeServersName.contains(split)) { //вытаскивать данные из bungee_servers
                                //permissionAttachmen.setPermission(perm, true);
                                op.user_perms.add(perm);   //пока расчёт на то, что с другим сервером в начале право не сработает.
//System.out.println("++личное право="+perm);        
                            }
                        }
       
//System.out.println("++личное право="+perm);        
                    }
            }
            
            if (op.permissionAttachmen != null) p.removeAttachment(op.permissionAttachmen); //permissionAttachmen пришлось оставить, без него не работает DeluxeChat!!
            op.permissionAttachmen = p.addAttachment(Ostrov.instance);
            
            
            for (String perm : op.user_perms) {  //закидываем собранные пермы в атачмент
//System.out.println("+"+ai.getPermission());     
                op.permissionAttachmen.setPermission(perm, true);
            }
            
            p.recalculatePermissions();
            
            
//System.out.println("");
            
        } catch (Exception ex) {
            Ostrov.log_err("Ошибка calculatePermissions "+op.nik+" : "+ex.getMessage());
            p.sendMessage(Ostrov.prefix+" §c Ошибка calculatePermissions, сообщите администрации! : "+ex.getMessage());
        }
        
        Bukkit.getPluginManager().callEvent(new GroupChangeEvent ( p, op.groups.treeSet ) );

        if (notify) p.sendMessage(Ostrov.prefix+"Ваши группы обновились: §e"+op.chat_group);

            


    }
    












}
