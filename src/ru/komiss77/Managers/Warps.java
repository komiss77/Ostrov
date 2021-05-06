package ru.komiss77.Managers;



import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import ru.komiss77.Cfg;
import ru.komiss77.Objects.Warp;






public class Warps {
   
private static HashMap <String, Warp> warps;
private static TreeSet <String> swarp;
private static TreeSet <String> pwarp;
public static Map <String,Integer> warps_per_group;

public static boolean use;
public static boolean сonsoleOnlyUse = false;
private static String allowed = "_0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";




public static void LoadVars() {
    use = Cfg.GetCongig().getBoolean("modules.command.warp.use");
    сonsoleOnlyUse = Cfg.GetCongig().getBoolean("modules.command.warp.сonsoleOnlyUse");
    Cfg.GetCongig().getConfigurationSection("modules.command.warp.amount_per_group").getKeys(false).stream().forEach((s) -> {
        warps_per_group.put ( s, Cfg.GetCongig().getInt("modules.command.warp.amount_per_group."+s) );
    });
    
}


public static void Init() {
    warps = new HashMap<>();
    swarp = new TreeSet();
    pwarp = new TreeSet();
    warps_per_group = new HashMap<>();

    LoadVars();
    
    if(use) MysqlLocal.Load_warps();
}

public static void ReLoadVars() {
        LoadVars();
    }














// -----------------------------  получение данных с мускула ---------------------
public static void Load_warp( String name, String type, String owner, String desc, String loc, boolean open, boolean need_perm, int use_cost, int counter, long create_time ) {

    Warp warp = new Warp ( name, type, owner, desc, String_to_Loc(loc), open, need_perm, use_cost, counter, create_time );
    warps.put(name, warp);

    if (type.equals("server")) swarp.add(name);
    else if (type.equals("player")) pwarp.add(name);
   
}
public static void Create_warp( Player p, String name, String type, String desc, boolean need_perm, int use_cost ) {

    Warp warp = new Warp ( name, type, p.getName(), desc, p.getLocation(), true, need_perm, use_cost, 0, Timer.Единое_время()/1000 );
    warps.put(name, warp);

    if (type.equals("server")) swarp.add(name);
    else if (type.equals("player")) pwarp.add(name);
   
    MysqlLocal.Add_warp(p, name, warp, true);
}

public static boolean Warp_exist(String name) {                                 //проверка на существование
    return warps.containsKey(name) && ( swarp.contains(name) || pwarp.contains(name) );
    }

public static void Del_warp (Player p, String name) {                                 
    if (warps.containsKey(name)) warps.remove(name);
    if (swarp.contains(name)) swarp.remove(name);
    if (pwarp.contains(name)) pwarp.remove(name);
    MysqlLocal.Del_warp(p, name);
}

public static TreeSet<String> Get_swarps () {                                 
    return swarp;
}

public static TreeSet<String> Get_pwarps () {                                 
    return pwarp;
}










public static int Warp_ammount(String nik) {
        int c=0;
        for ( String n : pwarp ) {
            if ( Get_owner(n).equalsIgnoreCase(nik)) c++;
        }
        return c;
    }
public static List<String> Get_Uset_warps (String nik) {
    List<String> uw = new ArrayList();
    
        for ( String n : pwarp ) {
            if ( Get_owner(n).equalsIgnoreCase(nik)) uw.add(n);
        }
        return uw;
    }



    
    
    
    
    
    
    
    
    
    
    
    
    public static String Get_type(String name) {
        return warps.get(name).Get_type();
    }

    public static String Get_owner(String name) {
        return warps.get(name).Get_owner();
    }

    public static String Get_desc(String name) {
        return warps.get(name).Get_desc();
    }

    public static Location Get_loc(String name) {
        return warps.get(name).Get_loc().clone().add(0, 0.5, 0);
    }

    public static boolean Is_open(String name) {
        return warps.get(name).Is_open();
    }

    public static boolean Need_perm(String name) {
        return warps.get(name).Need_perm();
    }

    public static int Get_cost(String name) {
        return warps.get(name).Get_cost();
    }

    public static int Get_counter(String name) {
        return warps.get(name).Get_counter();
    }
 
    public static String Get_createtime(String name) {
        return Long_to_date( warps.get(name).Get_createtime() );
    }





    public static void Set_open(Player p, String name, boolean on) {
        warps.get(name).Set_open(on);
        int o = 0;
        if (on) o = 1;
        MysqlLocal.Set_open( name, o);
    }

    public static void Set_cost(Player p, String name, int cost) {
        warps.get(name).Set_cost(cost);
        MysqlLocal.Set_cost( name, cost );
    }
   
    public static void Add_count(Player p, String name) {
        warps.get(name).Add_count();
        MysqlLocal.Add_count( name );
    }
    
    
    
    
    
    
    
    
    
    
    
    









public static String IntToTime(int min) {
    int h = (min / (60));
    int m = (int) ((min - (h * 60)) );
      if (h==0) {
          if (m==0)return  "меньше минуты";
          else return  String.format("%02d", m ).replaceFirst("0", "")+" мин.";
      }
      else return  h + " ч. " + String.format("%02d", (int) m  ).replaceFirst("0", "")+" мин.";
    }



public static String Loc_to_String ( Location loc) {
        return loc.getWorld().getName()+":"+loc.getBlockX()+":"+loc.getBlockY()+":"+loc.getBlockZ()+":"+(int)loc.getYaw()+":"+(int)loc.getPitch();
    }


public static Location String_to_Loc ( String loc) {
    
    String[] pos = loc.split(":");
    
        return new Location( 
                Bukkit.getWorld(pos[0]),                                        
                (double)Integer.valueOf(pos[1]), 
                (double)Integer.valueOf(pos[2]), 
                (double)Integer.valueOf(pos[3]), 
                (float)Integer.valueOf(pos[4]), 
                (float)Integer.valueOf(pos[5]) );
    }


public static String Long_to_date (long sec) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");  
                    Date resultdate = new Date(sec*1000);
                    return sdf.format(resultdate);
}


   public static boolean checkString ( String message) {
      for(int i = 0; i < message.length(); ++i) {
         if(!allowed.contains(String.valueOf(message.charAt(i)))) {
            return false;
         }
      }
      return true;
   }


 











}
