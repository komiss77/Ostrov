package ru.komiss77.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// !!!!!!!  не ставить ничего от бакита, не грузит банжик!!!

public enum Game { 
        //слот в меню статы  : страница в меню режимов : слот в меню режимов
    GLOBAL  ("", 4, 0, 0, "", "", ServerType.NONE, "NAUTILUS_SHELL", "§eОстров", Arrays.asList(""),0, 0),
    
    LOBBY   ("§6☣1 ", 0, 0, 22, "ХАБ", "lobby", ServerType.LOBBY, "HONEYCOMB", "§6§k0 §e§lЛобби §6§k0", Arrays.asList(""), 0, -99),

    DA      ("§a❂ ", 1, 0, 3, "Даария", "daaria", ServerType.ONE_GAME, "NETHERITE_SWORD", "§a§lДаария", Arrays.asList("§fКлассическое выживание 1.16+","§fДанжи, работа, приваты и др."), 0, -99),
    SE      ("§4☠ ", 2, 0, 24, "Седна", "sedna_wastes", ServerType.ONE_GAME, "CRIMSON_NYLIUM", "§c§lСедна", Arrays.asList("§c§nУльтраХаркор, постАпокалипсис.", "§c§nскиллы, боссы, магия!", "§c§nНовичкам не рекомендуется.","§bБета Тест"), 0, -99),
    AR      ("§a☺ ", 3, 0, 5, "Аркаим", "arcaim", ServerType.ONE_GAME, "DIAMOND_PICKAXE", "§e§lАркаим", Arrays.asList("§fОтдохни, расслабься!","§fКреатив для всех!"), 0, -99),
    MI      ("§5✠ ", 4, 0, 16, "Мидгард", "midgard", ServerType.ONE_GAME, "SPYGLASS", "§d§lМидгард", Arrays.asList("§fКланы, турели, ","§fразвитие, войны"), 0, -99),
    SK      ("§f☯ ", 5, 0, 20, "Скай-Ворлд", "skyworld", ServerType.ONE_GAME, "FLOWERING_AZALEA", "§b§lОстрова", Arrays.asList("§fНачни жизнь на крохотном островке","§fи построй Империю!"), 0, -99),
    OB      ("§b◈ ", 6, 0, 28, "Ван-Блок", "oneblock", ServerType.ONE_GAME, "AZALEA", "§bOneBlock", Arrays.asList("§fКогда классический","§fСкайблокслишком сложен,","§fвам сюда!","§fЛомайте всего один блок,","§fи развивайтесь!"), 0, -77),
    SD      ("§f✜ ", 7, 0, 30, "Скай-Грид", "skygrid", ServerType.ONE_GAME, "SCULK_SHRIEKER", "§bSkyGrid", Arrays.asList("§fНевероятное приключение","§fв сетчатом мире.","§fГлавная цель-,","§fпобедить дракона!"), 0, -77),
    EN      ("E", 7, 0, 10, "Энигма", "enigma", ServerType.ONE_GAME, "END_PORTAL_FRAME", "§d§lЭнигма", Arrays.asList("§fСервер тайн и загадок"), 0, -77),
    PA      ("§5❖ ", 8, 0, 34, "Паркуры", "parkur", ServerType.ONE_GAME, "FEATHER", "§b§lПаркуры", Arrays.asList("§fМного паркуров разной сложности"), 0, -77),
    FA      ("§5🤓 ", 9, 0, 16, "Фатта", "fatta", ServerType.ONE_GAME, "LECTERN", "§ф§lФатта", Arrays.asList("§fЛамповый РП Режим","§fРаботы, города, банды...","§кВ РАЗРАБОТКЕ!"), 0, -99),
    
    BW      ("§e☢ ", 18, 1, 10, "бедварс", "bw01", ServerType.ARENAS, "RED_BED", "§e§lБедВарс", Arrays.asList(""), 0, -77),
    SG      ("§c☠ ", 19, 1, 25, "голодные", "sg01", ServerType.ARENAS, "GOLDEN_APPLE", "§4§lГолодные Игры", Arrays.asList(""), 0, -77),
    SW      ("§b҈ ", 20, 1, 2, "скайварс", "sw01", ServerType.ARENAS, "COMPASS", "§5§lСкайВарс", Arrays.asList(""), 0, -77),
    ZH      ("§4⚚ ", 21, 1, 6, "зомби", "zh01", ServerType.ARENAS, "ROTTEN_FLESH", "§c§lЗомби", Arrays.asList(""), 0, -77),
    KB      ("§c⚔ ", 22, 1, 5, "китпвп", "kb01", ServerType.ARENAS, "DIAMOND_CHESTPLATE", "§b§lКит-ПВП", Arrays.asList(""), 0, -77),
    GR      ("§6$ ", 23, 1, 16, "лихорадка", "gr01", ServerType.ARENAS, "RAW_GOLD", "§6§lЗолотая Лихорадка", Arrays.asList(""), 0, -77),
    WZ      ("§3⚒ ", 24, 1, 3, "поле-брани", "wz01", ServerType.ARENAS, "TOTEM_OF_UNDYING", "§b§lПоле Брани", Arrays.asList(""), 0, -77), //warzone
    BB      ("§3✍ ", 25, 1, 33, "билдбатл", "bb01", ServerType.ARENAS, "GOLDEN_PICKAXE", "§a§lБитва Строителей", Arrays.asList(""), 0, -77),
    //21 - пусто
    TW      ("§e▦ ", 27, 1, 30, "твист", "twis", ServerType.ARENAS, "MUSIC_DISC_RELIC", "§d§lТвист", Arrays.asList(""), 0, -77),
    SN      ("§6ಊ ", 28, 1, 32, "змейка", "snek", ServerType.ARENAS, "STRING", "§f§lЗмейка", Arrays.asList(""), 0, -77),
    CS      ("§3✡ ", 29, 1, 19, "контра", "cs01", ServerType.ARENAS, "FLINT_AND_STEEL", "§5§lКонтра", Arrays.asList(""), 0, -77),
    HS      ("§a۩ ", 30, 1, 22, "прятки", "hs01", ServerType.ARENAS, "JACK_O_LANTERN", "§3§lПрятки", Arrays.asList(""), 0, -77),
    QU      ("§4⚛ ", 31, 1, 29, "квэйк", "qu01", ServerType.ARENAS, "TRIDENT", "§c§lКвэйк", Arrays.asList(""), 0, -77),
    
    ;

    
    
    public static String getGamePageTitle(final int page) {
        return switch (page) {
            case 0 -> "§bБольшие";
            case 1 -> "§eМини-Игры";
            default -> "Режимы";
        };
    }


    
    
    public final String defaultlogo;
    public final int statSlot;
    public final int menuPage;
    public final int menuSlot;
    public final String suggestName;
    public final String serverName;
    public final ServerType type;
    public final String mat;
    public final String displayName;
    public final List<String> description;
    public final int level;
    public final int reputation;
    public static final Set<String> displayNames; //для команды /server
    private static final Map<String,Game> nameMap; //напихать максимально для распознавания
    
    
    private Game(final String defaultlogo, final int statSlot, final int menuPage, final int menuSlot, final String suggestName, final String serverName, final ServerType type, final String mat, final String displayName, final List<String> description, final int level, final int reputation){
        this.defaultlogo = defaultlogo;
        this.statSlot = statSlot;
        this.menuPage = menuPage;
        this.menuSlot = menuSlot;
        this.suggestName = suggestName;
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
        //Map<String,Game> nm = new ConcurrentHashMap<>();
        Map<String,Game> gnm = new ConcurrentHashMap<>();
        displayNames = new HashSet<>();
        for (Game game : Game.values()) {
            if ( 36 * game.menuPage + game.menuSlot>MAX_SLOT) {
                MAX_SLOT = 36 * game.menuPage + game.menuSlot;
            }
            if (game==GLOBAL) continue;
            //nm.put(game.serverName.toLowerCase(),game);
            gnm.put(game.name().toLowerCase(),game); //lobby da pa bw bb sg
            gnm.put(game.suggestName.toLowerCase(),game);
            gnm.put(game.serverName,game);
            
            
            displayNames.add(game.suggestName);
            //if (!game.serverName.isEmpty()) {
               //snm.put(d.serverName.toLowerCase(),d);
               //nm.put(game.serverName.toLowerCase(),game);
            //} //else {
                //snm.put(game.name().toLowerCase(),game);
            //}
            //nm.put(game.name().toLowerCase(),game);
        }
        //nameMap = Collections.unmodifiableMap(nm);
        gnm.put("хаб", LOBBY);
        gnm.put("лобби", LOBBY);
        gnm.put("hub", LOBBY);
        gnm.put("lobby", LOBBY);
        gnm.put("lobby0", LOBBY);
        gnm.put("lobby1", LOBBY);
        gnm.put("lobby2", LOBBY);
        gnm.put("lobby3", LOBBY);
        gnm.put("lobby4", LOBBY);
        
        
        
        nameMap = Collections.unmodifiableMap(gnm);
        
    }
    
    //park skyblock не определило
    public static Game fromServerName(String serverName) { //araim daaria bw01 bb01 sg02
        if (serverName==null || serverName.isEmpty()) return GLOBAL;
        
        //прямой поиск
        Game game = nameMap.get(serverName);
//Ostrov.log_warn("serverName="+serverName+" game="+game);
        if (game!=null) return game;
        
        //не получился прямой - обработка имени
        serverName = serverName.toLowerCase();
        if (serverName.length()==4) { //bw01 bb01 sg02 обрезать до bw bb sg
            serverName = serverName.substring(0, 2);
        }
        game = nameMap.get(serverName); //прямой поиск
//Ostrov.log_warn("serverName="+serverName+" game="+game);
        if (game!=null) return game;
        
        if (serverName.startsWith("sedna_")) {
            return SE;
        }

        return GLOBAL; //rg0 ol0 ?
    }
}


