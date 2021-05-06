package ru.komiss77.utils.inventory;

import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryAPI {
    
    private static InventoryAPI api;
    private final JavaPlugin host;
    private final InventoryManager manager;
    
    public InventoryAPI(final JavaPlugin host) {
        this(host, true);
    }
    
    public InventoryAPI(final JavaPlugin host, final boolean useAnvilAPI) {
        api = this;
        this.host = host;
        this.manager = new InventoryManager();
        Bukkit.getPluginManager().registerEvents(new InventoryAPIListener(this.manager, host), (Plugin)host);
        
    }
    
    public static InventoryAPI get() {
        return InventoryAPI.api;
    }
    
    public InventoryManager getManager() {
        return this.manager;
    }
    
    public JavaPlugin getHost() {
        return this.host;
    }
}
