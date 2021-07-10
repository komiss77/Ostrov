package ru.komiss77.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public enum Game {
        //слот в меню статы  : страница в меню режимов : слот в меню режимов
    GLOBAL  (4, 0, 0, "", ServerType.NONE, "NAUTILUS_SHELL", "§eОстров", Arrays.asList(""),0, 0),
    
    LOBBY   (0, 0, 13, "", ServerType.LOBBY, "HONEYCOMB", "§6§k0 §e§lЛобби §6§k0", Arrays.asList(""), 0, 0),

    DA      (1, 0, 1, "daaria", ServerType.ONE_GAME, "GRASS_BLOCK", "§a§lДаария", Arrays.asList("§fКлассическое выживание 1.16+","§fДанжи, работа, приваты и др."), 0, 0),
    SE      (2, 0, 2, "sedna", ServerType.ONE_GAME, "CRIMSON_NYLIUM", "§c§lСедна", Arrays.asList("§c§nВ разработке!"), 0, 0),
    AR      (3, 0, 3, "arcaim", ServerType.ONE_GAME, "YELLOW_STAINED_GLASS", "§e§lАркаим", Arrays.asList("§fОтдохни, расслабься!","§fКреатив для всех!"), 0, 0),
    MI      (4, 0, 4, "midgard", ServerType.ONE_GAME, "RED_BANNER", "§d§lМидгард", Arrays.asList("§fКланы, турели, ","§fразвитие, войны"), 0, 0),
    SK      (5, 0, 5, "skyworld", ServerType.ONE_GAME, "FLOWER_BANNER_PATTERN", "§b§lОстрова", Arrays.asList("§fНачни жизнь на крохотном островке","§fи построй Империю!"), 0, 0),
    PA      (6, 0, 6, "parkur", ServerType.ONE_GAME, "FEATHER", "§b§lПаркуры", Arrays.asList("§fБолее 60+ карт"), 0, 0),

    BW      (18, 1, 0, "", ServerType.ARENAS, "RED_BED", "§e§lБедВарс", Arrays.asList(""), 0, 0),
    SG      (19, 1, 1, "", ServerType.ARENAS, "GOLDEN_APPLE", "§4§lГолодные Игры", Arrays.asList(""), 5, 50),
    SW      (20, 1, 2, "", ServerType.ARENAS, "COMPASS", "§5§lСкайВарс", Arrays.asList(""), 3, 30),
    ZH      (21, 1, 3, "", ServerType.ARENAS, "FERMENTED_SPIDER_EYE", "§c§lЗомби", Arrays.asList(""), 0, 0),
    KB      (22, 1, 4, "", ServerType.ARENAS, "DIAMOND_CHESTPLATE", "§b§lКит-ПВП", Arrays.asList(""), 0, 0),
    GR      (23, 1, 5, "", ServerType.ARENAS, "GOLD_NUGGET", "§6§lЗолотая Лихорадка", Arrays.asList(""), 0, 0),
    WZ      (24, 1, 6, "", ServerType.ARENAS, "TOTEM_OF_UNDYING", "§b§lПоле Брани", Arrays.asList(""), 0, 0),
    BB      (25, 1, 7, "", ServerType.ARENAS, "GOLDEN_PICKAXE", "§a§lБитва Строителей", Arrays.asList(""), 0, 0),
    MM      (26, 1, 8, "", ServerType.ARENAS, "IRON_SWORD", "§b§lМаньяк", Arrays.asList(""), 0, 0),
    TW      (27, 1, 9, "", ServerType.ARENAS, "MUSIC_DISC_CHIRP", "§d§lТвист", Arrays.asList(""), 0, 0),
    SN      (28, 1, 10, "", ServerType.ARENAS, "STRING", "§f§lЗмейка", Arrays.asList(""), 0, 0),
    CS      (29, 1, 11, "", ServerType.ARENAS, "FLINT_AND_STEEL", "§5§lКонтра", Arrays.asList(""), 0, 0),
    HS      (30, 1, 12, "", ServerType.ARENAS, "JACK_O_LANTERN", "§3§lПрятки", Arrays.asList(""), 0, 0),
    QU      (31, 1, 14, "", ServerType.ARENAS, "TRIDENT", "§c§lКвэйк", Arrays.asList(""), 0, 0),

    ;

    
    
    public static String getGamePageTitle(final int page) {
        switch (page) {
            case 0: return "Режимы : §bБольшие";
            case 1: return "Режимы : §eПВП";
            default: return "Режимы";
        }
    }







    
    
    
    
    
    
    
    
    
    public final int statSlot;
    public final int menuPage;
    public final int menuSlot;
    public final String serverName;
    public final ServerType type;
    public final String mat;
    public final String displayName;
    public final List<String> description;
    public final int level;
    public final int reputation;
    private static final Map<String,Game> nameMap;
    private static final Map<String,Game> serverNameMap;
    
    
    private Game(final int statSlot, final int menuPage, final int menuSlot, final String serverName, final ServerType type, final String mat, final String displayName, final List<String> description, final int level, final int reputation){
        this.statSlot = statSlot;
        this.menuPage = menuPage;
        this.menuSlot = menuSlot;
        this.serverName = serverName;
        this.type = type;
        this.mat = mat;
        this.displayName = displayName;
        this.description = description;
        this.level = level;
        this.reputation = reputation;
    }
    
    public static int MAX_SLOT;
    static {
        Map<String,Game> nm = new ConcurrentHashMap<>();
        Map<String,Game> snm = new ConcurrentHashMap<>();
        for (Game d : Game.values()) {
            if (36*d.menuPage+d.menuSlot>MAX_SLOT) MAX_SLOT = 36*d.menuPage+d.menuSlot;
            if (!d.serverName.isEmpty()) {
                snm.put(d.serverName.toLowerCase(),d);
            } else {
                snm.put(d.name().toLowerCase(),d);
            }
            nm.put(d.name().toLowerCase(),d);
        }
        nameMap = Collections.unmodifiableMap(nm);
        serverNameMap = Collections.unmodifiableMap(snm);
    }
    
    //park skyblock не определило
    public static Game fromServerName(final String serverName) { //araim daaria bw01 bb01 sg02
        if (serverName==null || serverName.isEmpty()) return GLOBAL;
        //fix!!!
       // switch (game_name) {
       //     case "tnt": return TR;
       //     case "tnts" :return SP;
       //     case "park" :return PA;
       // }
       Game game = null;
        if (serverName.length()>4) { //аркаим даария lobby0 
            if(serverName.startsWith("lobby")) { //lobby0 lobby1
                return LOBBY;
            } else {
                game = serverNameMap.get(serverName);
            }
        } else if (serverName.length()==4) { //bw01 bb01 sg02
            game = serverNameMap.get(serverName.substring(0, 2));
        }// else {
       //     return "";
       // }
        //for (Game current:Game.values()) {
        //    if (current.toString().equalsIgnoreCase(game_name)) return current;
        //}
        return game == null ? GLOBAL : game; //rg0 ol0 ?
    }
    
    public static Game fromString(final String gameName) { //araim daaria bw01 bb01 sg02
        if (gameName==null || gameName.isEmpty()) return null;
        return nameMap.get(gameName);
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

//public static enum ServerType {
//    SINGLE, MULTIPLE, LOBBY
//    ;
//}     
    
}


