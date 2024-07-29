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
        Ostrov.regCommand(new StatCmd());
        Ostrov.regCommand(new ProtocolCmd());
        Ostrov.regCommand(new WorldManagerCmd());
        Ostrov.regCommand(new SpyCmd());
        Ostrov.regCommand(new BossBarCmd());
        Ostrov.regCommand(new OreloadCmd());
        Ostrov.regCommand(new ReportCmd());

//        plugin.getCommand("statreach").setExecutor(new StatReachCmd());
//        plugin.getCommand("world").setExecutor(new WorldCmd());
//        plugin.getCommand("nbtfind").setExecutor(new NbtfindCmd(plugin));
//        plugin.getCommand("nbtcheck").setExecutor(new NbtcheckCmd(plugin));
        /*plugin.getCommand("mission").setExecutor(new MissionCmd());
        plugin.getCommand("tpr").setExecutor(new TprCmd());
        plugin.getCommand("analytics").setExecutor(new AnalyticsCmd());
        plugin.getCommand("home").setExecutor(new HomeCmd());
        plugin.getCommand("skin").setExecutor(new SkinCmd());

        plugin.getCommand("builder").setExecutor(new BuilderCmd());
        plugin.getCommand("moder").setExecutor(new ModerCmd());
        plugin.getCommand("donate").setExecutor(new DonateCmd());
        plugin.getCommand("clean").setExecutor(new CleanCmd());
        plugin.getCommand("hat").setExecutor(new HatCmd());
        plugin.getCommand("rp").setExecutor(new ResourcePacksLst());//plugin.getCommand("rp").setExecutor(ResourcePacksLst.resourcePacks);//new ResourcePacks(false));
    */
    }


    public static void registerAuth() {
        Ostrov.regCommand(new EntityCmd());
        Ostrov.regCommand(new BossBarCmd());
        Ostrov.regCommand(new WorldManagerCmd());
    }

    public static void registerPay() {
        Ostrov.regCommand(new RewardCmd());
    }

}
