package ru.komiss77.version;

import java.util.HashMap;


public interface INbt {

    

    boolean hasString(org.bukkit.inventory.ItemStack item, String name);
    
    /**
     * 
     * @param item
     * @param name
     * @param value
     * @return при вызове обязательно присваивать результат!!!!
     */
    org.bukkit.inventory.ItemStack addString(org.bukkit.inventory.ItemStack item, String path, String value);
    
    org.bukkit.inventory.ItemStack removeString(org.bukkit.inventory.ItemStack item, String path);
    
    String getString(org.bukkit.inventory.ItemStack item, String name);

    org.bukkit.inventory.ItemStack setDamage(org.bukkit.inventory.ItemStack item, int damage);

    int getDamage(org.bukkit.inventory.ItemStack item);

    HashMap<String,String> getTagMap(org.bukkit.inventory.ItemStack item);
    
}
