package ru.komiss77.ProfileMenu;

import org.bukkit.Material;



public enum E_Stat_menu {

    GLOBAL (31, Material.NAUTILUS_SHELL, "§eОстров"),
    
    BW (1, Material.RED_BED, "§eБедВарс"),
    SG (3, Material.APPLE, "§4Голодные Игры"),
    BB (5, Material.NETHER_QUARTZ_ORE, "§aБитва Строителей"),
    GR (7, Material.GOLD_INGOT, "§6Золотая Лихорадка"),
    HS (9, Material.PUMPKIN, "§3Прятки"),
    SW (11, Material.COMPASS, "§5SkyWars"),
    CS (13, Material.FLINT_AND_STEEL, "§5Контра"),
    TW (15, Material.LEATHER, "§dТвист"),
    QU (17, Material.TRIDENT, "§cQuake"),
    SN (19, Material.STRING, "§fЗмейка"),
    
    ZH (21, Material.ZOMBIE_SPAWN_EGG, "§cЗомби"),
    //TR (23, Material.TNT, "§cTNT run"),
    PA (23, Material.FEATHER, "§fПаркуры"),
    
    KB (25, Material.DIAMOND_CHESTPLATE, "§bКит-ПВП"),
    
    ;

    public int slot;
    public Material mat;
    public String game_name;
    
    
    private E_Stat_menu(int slot, Material mat, String game_name ){
        this.slot = slot;
        this.mat = mat;
        this.game_name = game_name;
        //Material.h
    }
    
    public static E_Stat_menu fromString(final String game_name) {
        //fix!!!
       // switch (game_name) {
       //     case "tnt": return TR;
       //     case "tnts" :return SP;
       //     case "park" :return PA;
       // }
        
        for (E_Stat_menu current:E_Stat_menu.values()) {
            if (current.toString().equalsIgnoreCase(game_name)) return current;
        }
        return null;
    }
    
    public static E_Stat_menu fromEStat(final E_Stat es) {
        final String game_name = es.toString().substring(0,2);
        return fromString(game_name);
    }
    /*
    public static boolean exist(final String name) {
        for (E_Stat_menu current:E_Stat_menu.values()) {
            if (current.toString().equals(name)) return true;
        }
        return false;
    }*/

    
    
    
}
