package ru.komiss77.commands;

import ru.komiss77.builder.BuilderCmd;
import ru.komiss77.modules.kits.KitCmd;
import ru.komiss77.Ostrov;
import ru.komiss77.listener.ResourcePacksLst;
import ru.komiss77.modules.player.mission.MissionCmd;



public class RegisterCommands {
    //private static Ostrov plugin;

    public static void register(final Ostrov plugin) {
        //RegisterCommands.plugin=plugin;

        Ostrov.log_ok("§5Регистрация команд:");
        Ostrov.regCommand(new AdminCmd());
        Ostrov.regCommand(new Prefix());
        Ostrov.regCommand(new Suffix());
        Ostrov.regCommand(new ProfileCmd());
        Ostrov.regCommand(new InvseeCmd());
        Ostrov.regCommand(new SeenCmd());
        Ostrov.regCommand(new WarpCmd());
        Ostrov.regCommand(new KitCmd());
        Ostrov.regCommand(new PvpCmd());
        Ostrov.regCommand(new ServerCmd());
        Ostrov.regCommand(new EntityCmd());
        Ostrov.regCommand(new PassportCmd());
        Ostrov.regCommand(new RewardCmd());

        plugin.getCommand("statadd").setExecutor(new StatAddCmd());
        plugin.getCommand("statreach").setExecutor(new StatReachCmd());
        plugin.getCommand("wm").setExecutor(new WorldManagerCmd());
        plugin.getCommand("spy").setExecutor(new SpyCmd());
        plugin.getCommand("bossbar").setExecutor(new OpAsBossBarCmd());
        plugin.getCommand("world").setExecutor(new WorldCmd());
        plugin.getCommand("oreload").setExecutor(new OreloadCmd());
        plugin.getCommand("report").setExecutor(new ReportCmd());
        plugin.getCommand("nbtfind").setExecutor(new NbtfindCmd(plugin));
        plugin.getCommand("nbtcheck").setExecutor(new NbtcheckCmd(plugin));
        plugin.getCommand("mission").setExecutor(new MissionCmd());
        plugin.getCommand("tpr").setExecutor(new TprCmd());
        plugin.getCommand("analytics").setExecutor(new AnalyticsCmd());
        plugin.getCommand("home").setExecutor(new HomeCmd());
        plugin.getCommand("skin").setExecutor(new SkinCmd());

        plugin.getCommand("builder").setExecutor(new BuilderCmd());
        plugin.getCommand("moder").setExecutor(new ModerCmd());
        plugin.getCommand("donate").setExecutor(new DonateCmd());
        plugin.getCommand("protocol").setExecutor(new ProtocolCmd());
        plugin.getCommand("clean").setExecutor(new CleanCmd());
        plugin.getCommand("hat").setExecutor(new HatCmd());
        plugin.getCommand("rp").setExecutor(ResourcePacksLst.resourcePacks);//new ResourcePacks(false));
    }

    public static void registerAuth(final Ostrov plugin) {
      Ostrov.regCommand(new EntityCmd());

        plugin.getCommand("bossbar").setExecutor(new OpAsBossBarCmd());
        plugin.getCommand("world").setExecutor(new WorldCmd());
    }
    
    public static void registerPay(final Ostrov plugin) {
      Ostrov.regCommand(new RewardCmd());
    }
    
}
