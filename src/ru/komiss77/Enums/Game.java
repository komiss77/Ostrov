package ru.komiss77.Enums;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public enum Game {

    GLOBAL (4, 0, GameType.MULTIPLE, "NAUTILUS_SHELL", "§eОстров", "", 0, 0),
    
    LOBBY (0, 13, GameType.LOBBY, "COBWEB", "§7лобби", "", 0, 0),

    AR (9, 0, GameType.SINGLE, "GOLDEN_PICKAXE", "§2Аркаим", "§fОтдохни, расслабься!<br>&fКреатив для всех!", 0, 0),
    MI (10, 4, GameType.SINGLE, "RED_BANNER", "§bМидгард", "§bКланы, турели, <br>§bразвитие, войны", 0, 0),
    DA (11, 2, GameType.SINGLE, "GRASS", "§6Даария", "&fКлассическое выживание<br>Данжи, работа, приваты...", 0, 0),
    SB (12, 6, GameType.SINGLE, "MILK_BUCKET", "§3СкайБлок", "&fНачни жизнь на крохотном островке<br>&fи построй...", 0, 0),
    PA (13, 8, GameType.SINGLE, "FEATHER", "§fПаркуры", "Сервер паркуров", 0, 0),

    BW (19, 18, GameType.MULTIPLE, "RED_BED", "§eБедВарс", "", 0, 0),
    SG (20, 28, GameType.MULTIPLE, "APPLE", "§4Голодные Игры", "", 0, 0),
    BB (21, 34, GameType.MULTIPLE, "NETHER_QUARTZ_ORE", "§aБитва Строителей", "", 0, 0),
    GR (22, 26, GameType.MULTIPLE, "GOLD_INGOT", "§6Золотая Лихорадка", "", 0, 0),
    HS (23, 22, GameType.MULTIPLE, "PUMPKIN", "§3Прятки", "", 0, 0),
    SW (24, 32, GameType.MULTIPLE, "COMPASS", "§5SkyWars", "", 0, 0),
    CS (25, 38, GameType.MULTIPLE, "FLINT_AND_STEEL", "§5Контра", "", 0, 0),
    TW (26, 30, GameType.MULTIPLE, "LEATHER", "§dТвист", "", 0, 0),
    QU (27, 42, GameType.MULTIPLE, "TRIDENT", "§cQuake", "", 0, 0),
    SN (28, 40, GameType.MULTIPLE, "STRING", "§fЗмейка", "", 0, 0),
    ZH (29, 46, GameType.MULTIPLE, "ZOMBIE_SPAWN_EGG", "§cЗомби", "", 0, 0),
    KB (30, 48, GameType.MULTIPLE, "DIAMOND_CHESTPLATE", "§bКит-ПВП", "", 0, 0),
    
    ;

    public final int statSlot;
    public final int menuSlot;
    public final GameType type;
    public final String mat;
    public final String displayName;
    public final String description;
    public final int level;
    public final int reputation;
    private static final Map<String,Game> nameMap;
    
    
    private Game(final int statSlot, final int menuSlot, final GameType type, final String mat, final String displayName, final String description, final int level, final int reputation){
        this.statSlot = statSlot;
        this.menuSlot = menuSlot;
        this.type = type;
        this.mat = mat;
        this.displayName = displayName;
        this.description = description;
        this.level = level;
        this.reputation = reputation;
    }
    
    static {
        Map<String,Game> sm = new ConcurrentHashMap<>();
        for (Game d : Game.values()) {
            sm.put(d.name().toLowerCase(),d);
        }
        nameMap = Collections.unmodifiableMap(sm);
    }
    
    public static Game fromServerName(final String serverName) { //araim daaria bw01 bb01 sg02
        //fix!!!
       // switch (game_name) {
       //     case "tnt": return TR;
       //     case "tnts" :return SP;
       //     case "park" :return PA;
       // }
        if (serverName.length()>4) { //аркаим даария и т.д.
            if(serverName.startsWith("lobby")) { //lobby0 lobby1
                return LOBBY;
            } else {
                return nameMap.get(serverName);
            }
        } else if (serverName.length()==4) { //bw01 bb01 sg02
            return nameMap.get(serverName.substring(0, 2));
        }// else {
       //     return "";
       // }
        //for (Game current:Game.values()) {
        //    if (current.toString().equalsIgnoreCase(game_name)) return current;
        //}
        return GLOBAL; //rg0 ol0 ?
    }
    
    public static Game fromString(final String gameNameAtLowerCase) { //araim daaria bw01 bb01 sg02
        return nameMap.get(gameNameAtLowerCase);
    }
    
   // public static Game fromStat(final Stat es) {
   //     final String game_name = es.toString().substring(0,2);
  //      return fromString(game_name);
  //  }
    /*
    public static boolean exist(final String name) {
        for (E_Stat_menu current:E_Stat_menu.values()) {
            if (current.toString().equals(name)) return true;
        }
        return false;
    }*/

    
public static enum GameType {
    SINGLE, MULTIPLE, LOBBY
    ;
}     
    
}
