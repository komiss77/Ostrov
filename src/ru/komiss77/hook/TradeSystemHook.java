package ru.komiss77.hook;


import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import ru.komiss77.Ostrov;



public class TradeSystemHook {
    

    
    static {
    }

    
    public static void hook(final Plugin plugin) {

        Bukkit.getPluginManager().registerEvents(new TradeLst(), Ostrov.instance);
        
        Ostrov.log_ok ("§bПодключен TradeSystem!");


    }

    

}
