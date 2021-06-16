package ru.komiss77.utils.inventory;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.komiss77.Ostrov;

public final class InventoryAPI {
    
    private static InventoryAPI api;
    private final InventoryManager manager;
    
    
    public InventoryAPI() {
        api = this;
        manager = new InventoryManager();
        Bukkit.getPluginManager().registerEvents(new InventoryAPIListener(manager, getHost()), getHost());
        
    }
    
    public static InventoryAPI get() {
        return InventoryAPI.api;
    }
    
    public InventoryManager getManager() {
        return this.manager;
    }
    
    public JavaPlugin getHost() {
        return Ostrov.instance;
    }
}
