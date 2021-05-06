package ru.komiss77.Managers;


import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Enums.Action;
import ru.komiss77.Objects.Oplayer;
import ru.komiss77.Ostrov;
import ru.komiss77.scoreboard.NameTag.NametagManager;
import ru.komiss77.utils.ItemUtils;






public class PM {
    
    private static final Map <String, Oplayer> oplayers = new ConcurrentHashMap<>();
    public static boolean use_preffix_suffix_wothout_deluxechat;
    public static String prefix_p_n;   //разделители
    public static String prefix_n_s;   //разделители
    public static String prefix_s_m;   //разделители
    public static boolean ostrovStatScore = false;    
    public static boolean tablist_header_footer = false;    
    public static int sec = 0;    
    public static NametagManager nameTagManager;
    
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

    public static void onDisable() {
        oplayers.clear();
    }

    public static void tickOplayers() { //каждую секунду
        oplayers.values().stream().forEach((op) -> {
            op.Tick_every_second(sec);
        });
        sec++;
        if (sec>60) sec=0;
        if (ostrovStatScore && sec%10==0 && !oplayers.isEmpty()) {
            ApiOstrov.sendMessage(Bukkit.getOnlinePlayers().stream().findAny().get(), Action.GET_BUNGEE_ONLINE, "");
        }
    }


    public static void onExit(final Player p) {                                          //процедура выхода с сервера
        final String name = p.getName();
        if (!oplayers.containsKey(name)) return;
        
        getOplayer(name).onExit();
        
        if (!MysqlLocal.useLocalData) {
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
    public static void dropInv(Player p) {
    //System.out.println("dropInv>>");
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null) {
                p.getWorld().dropItemNaturally(p.getLocation(), item);
                p.getInventory().remove(item);
            }
        }
        p.getInventory().clear();
        p.updateInventory();
        p.sendMessage("§cВаши вещи достались победителю!");
    }
//------------------------------------------------------------------------------











//---------------------------------- Дома, положение в мирах, тп -------------------
public static void OP_SetHome (Player p, String home) {
    if (oplayers.containsKey(p.getName())) {
        oplayers.get(p.getName()).SetHome(home,p.getLocation());
        MysqlLocal.Save_Home(p.getName(), oplayers.get(p.getName()));
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
    MysqlLocal.Save_Home(p.getName(), oplayers.get(p.getName()));
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
    public static long Kit_last_acces(String nik, String kit) {
        return oplayers.get(nik).Kit_last_acces(kit);
    }
    //------------------------------------------------------------------------------













// ------------------------- Префикс, Игровое время ----------------------------

public static String OP_GetPrefix(String nik) {
    try {
        return oplayers.get(nik).GetPrefix();
    } catch (NullPointerException ex) {
        Ostrov.log_err("Ошибка префикса для "+nik+" : "+ex.getMessage());
        return "";
    }
}
public static String OP_GetSuffix(String nik) {
    try {
        return oplayers.get(nik).GetSuffix();
    } catch (NullPointerException ex) {
        Ostrov.log_err("Ошибка суффикса для "+nik+" : "+ex.getMessage());
        return "";
    }
}

public static int OP_GetPlytime(String nik) {
    return oplayers.get(nik).GetPlytime();
}
public static String OP_GetPlytime_s ( String nik ) {
    return ApiOstrov.IntToTime(OP_GetPlytime(nik) );
}  
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



























}
