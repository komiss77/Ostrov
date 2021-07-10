package ru.komiss77.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public enum Game {
    
    GLOBAL (4, 0, "", ServerType.NONE, "NAUTILUS_SHELL", "§eОстров", Arrays.asList(""),0, 0),
    
    LOBBY (0, 13, "", ServerType.LOBBY, "HONEYCOMB", "§6§k0 §e§lЛобби §6§k0", Arrays.asList(""), 0, 0),

    DA (1, 1, "daaria", ServerType.ONE_GAME, "GRASS_BLOCK", "§a§lДаария", Arrays.asList("&fКлассическое выживание 1.16+","§fДанжи, работа, приваты и др."), 0, 0),
    SE (2, 2, "sedna", ServerType.ONE_GAME, "CRIMSON_NYLIUM", "§c§lСедна", Arrays.asList("&c&nВ разработке!"), 0, 0),
    AR (3, 3, "arcaim", ServerType.ONE_GAME, "YELLOW_STAINED_GLASS", "§e§lАркаим", Arrays.asList("§fОтдохни, расслабься!","&fКреатив для всех!"), 0, 0),
    MI (4, 4, "midgard", ServerType.ONE_GAME, "RED_BANNER", "§d§lМидгард", Arrays.asList("§fКланы, турели, ","§fразвитие, войны"), 0, 0),
    SK (5, 5, "skyworld", ServerType.ONE_GAME, "FLOWER_BANNER_PATTERN", "§b§lОстрова", Arrays.asList("&fНачни жизнь на крохотном островке","&fи построй Империю!"), 0, 0),
    PA (6, 6, "parkur", ServerType.ONE_GAME, "FEATHER", "§b§lПаркуры", Arrays.asList("§fБолее 60+ карт"), 0, 0),

    BW (18, 18, "", ServerType.ARENAS, "RED_BED", "§e§lБедВарс", Arrays.asList(""), 5, 50),
    SG (19, 19, "", ServerType.ARENAS, "GOLDEN_APPLE", "§4§lГолодные Игры", Arrays.asList(""), 5, 50),
    SW (20, 20, "", ServerType.ARENAS, "COMPASS", "§5§lСкайВарс", Arrays.asList(""), 3, 30),
    ZH (21, 21, "", ServerType.ARENAS, "FERMENTED_SPIDER_EYE", "§c§lЗомби", Arrays.asList(""), 0, 0),
    KB (22, 22, "", ServerType.ARENAS, "DIAMOND_CHESTPLATE", "§b§lКит-ПВП", Arrays.asList(""), 0, 0),
    GR (23, 23, "", ServerType.ARENAS, "GOLD_NUGGET", "§6§lЗолотая Лихорадка", Arrays.asList(""), 0, 0),
    WZ (24, 24, "", ServerType.ARENAS, "TOTEM_OF_UNDYING", "§b§lПоле Брани", Arrays.asList(""), 0, 0),
    BB (25, 25, "", ServerType.ARENAS, "GOLDEN_PICKAXE", "§a§lБитва Строителей", Arrays.asList(""), 0, 0),
    MM (26, 26, "", ServerType.ARENAS, "IRON_SWORD", "§b§lМаньяк", Arrays.asList(""), 0, 0),
    TW (27, 27, "", ServerType.ARENAS, "MUSIC_DISC_CHIRP", "§d§lТвист", Arrays.asList(""), 0, 0),
    SN (28, 28, "", ServerType.ARENAS, "STRING", "§f§lЗмейка", Arrays.asList(""), 0, 0),
    CS (29, 29, "", ServerType.ARENAS, "FLINT_AND_STEEL", "§5§lКонтра", Arrays.asList(""), 0, 0),
    HS (30, 30, "", ServerType.ARENAS, "JACK_O_LANTERN", "§3§lПрятки", Arrays.asList(""), 0, 0),
    QU (31, 31, "", ServerType.ARENAS, "TRIDENT", "§c§lКвэйк", Arrays.asList(""), 0, 0),
/*
    GLOBAL (0, 0, "", ServerType.NONE, "NAUTILUS_SHELL", "§eОстров", "", 0, 0),
    
    LOBBY (0, 13, "", ServerType.LOBBY, "COBWEB", "§7лобби", "", 0, 0),

    AR (1, 0, "arcaim", ServerType.ONE_GAME, "GOLDEN_PICKAXE", "§2Аркаим", "§fОтдохни, расслабься!<br>&fКреатив для всех!", 0, 0),
    MI (2, 4, "midgard", ServerType.ONE_GAME, "RED_BANNER", "§bМидгард", "§bКланы, турели, <br>§bразвитие, войны", 0, 0),
    DA (3, 2, "daaria", ServerType.ONE_GAME, "GRASS_BLOCK", "§6Даария", "&fКлассическое выживание<br>Данжи, работа, приваты...", 0, 0),
    SK (4, 6, "skyblock", ServerType.ONE_GAME, "MILK_BUCKET", "§3СкайБлок", "&fНачни жизнь на крохотном островке<br>&fи построй...", 0, 0),
    PA (5, 8, "parkur", ServerType.ONE_GAME, "FEATHER", "§fПаркуры", "Сервер паркуров", 0, 0),

    BW (9, 18, "", ServerType.ARENAS, "RED_BED", "§eБедВарс", "", 0, 0),
    SG (19, 28, "", ServerType.ARENAS, "APPLE", "§4Голодные Игры", "", 0, 0),
    BB (11, 34, "", ServerType.ARENAS, "NETHER_QUARTZ_ORE", "§aБитва Строителей", "", 0, 0),
    GR (12, 26, "", ServerType.ARENAS, "GOLD_INGOT", "§6Золотая Лихорадка", "", 0, 0),
    HS (13, 22, "", ServerType.ARENAS, "PUMPKIN", "§3Прятки", "", 0, 0),
    SW (14, 32, "", ServerType.ARENAS, "COMPASS", "§5SkyWars", "", 0, 0),
    CS (15, 38, "", ServerType.ARENAS, "FLINT_AND_STEEL", "§5Контра", "", 0, 0),
    TW (16, 30, "", ServerType.ARENAS, "LEATHER", "§dТвист", "", 0, 0),
    QU (17, 42, "", ServerType.ARENAS, "TRIDENT", "§cQuake", "", 0, 0),
    SN (18, 40, "", ServerType.ARENAS, "STRING", "§fЗмейка", "", 0, 0),
    ZH (19, 46, "", ServerType.ARENAS, "ZOMBIE_SPAWN_EGG", "§cЗомби", "", 0, 0),
    KB (20, 48, "", ServerType.ARENAS, "DIAMOND_CHESTPLATE", "§bКит-ПВП", "", 0, 0),
    */
    ;

    public final int statSlot;
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
    
    
    private Game(final int statSlot, final int menuSlot, final String serverName, final ServerType type, final String mat, final String displayName, final List<String> description, final int level, final int reputation){
        this.statSlot = statSlot;
        this.menuSlot = menuSlot;
        this.serverName = serverName;
        this.type = type;
        this.mat = mat;
        this.displayName = displayName;
        this.description = description;
        this.level = level;
        this.reputation = reputation;
    }
    
    static {
        Map<String,Game> nm = new ConcurrentHashMap<>();
        Map<String,Game> snm = new ConcurrentHashMap<>();
        for (Game d : Game.values()) {
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
