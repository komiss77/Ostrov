package ru.komiss77.commands;

import ru.komiss77.modules.kits.KitCmd;
import ru.komiss77.Ostrov;



public class RegisterCommands {
    private static Ostrov plugin;

    public static void register(final Ostrov plugin) {
        RegisterCommands.plugin=plugin;

        plugin.log_ok ("§5Регистрация команд:");
        
        plugin.getCommand("profile").setExecutor(new Profile());
        plugin.getCommand("invsee").setExecutor(new Invsee());
        plugin.getCommand("seen").setExecutor(new Seen());
        plugin.getCommand("warp").setExecutor(new WarpCmd());
        plugin.getCommand("kit").setExecutor(new KitCmd());
        plugin.getCommand("pvp").setExecutor(new Pvp());
        //plugin.getCommand("passport").setExecutor(new Passport());
        plugin.getCommand("reward").setExecutor(new Reward());
        plugin.getCommand("statadd").setExecutor(new StatAdd());
        plugin.getCommand("wm").setExecutor(new WorldManagerCmd());
        plugin.getCommand("spy").setExecutor(new Spy());
        plugin.getCommand("bossbar").setExecutor(new OpAsBossBar());
        plugin.getCommand("oreload").setExecutor(new Oreload());
        plugin.getCommand("builder").setExecutor(new Builder());
        plugin.getCommand("report").setExecutor(new Report());
        plugin.getCommand("nbtfind").setExecutor(new Nbtfind(plugin));
        plugin.getCommand("nbtcheck").setExecutor(new Nbtcheck(plugin));
    }
    
}
