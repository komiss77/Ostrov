package ru.komiss77.Commands;

import ru.komiss77.Kits.KitCmd;
import ru.komiss77.Ostrov;



public class RegisterCommands {
    private static Ostrov plugin;

    public static void register(final Ostrov plugin) {
        RegisterCommands.plugin=plugin;

        plugin.log_ok ("§5Регистрация команд:");
        
        plugin.getCommand("profile").setExecutor(new Profile());
        plugin.getCommand("invsee").setExecutor(new Invsee());
        plugin.getCommand("seen").setExecutor(new Seen());
        plugin.getCommand("warp").setExecutor(new Warp());
        plugin.getCommand("setwarp").setExecutor(new Setwarp());
        plugin.getCommand("setswarp").setExecutor(new Setswarp());
        plugin.getCommand("kit").setExecutor(new KitCmd());
        plugin.getCommand("pvp").setExecutor(new Pvp());
        plugin.getCommand("passport").setExecutor(new Passport());
        plugin.getCommand("reward").setExecutor(new Reward());
        plugin.getCommand("statadd").setExecutor(new StatAdd());
        plugin.getCommand("wm").setExecutor(new WorldManagerCommand());
        plugin.getCommand("spy").setExecutor(new Spy());
        plugin.getCommand("bossbar").setExecutor(new OpAsBossBar());
        plugin.getCommand("oreload").setExecutor(new Oreload());
        //plugin.getCommand("sound").setExecutor(new Oreload());
        
    }
    
}
