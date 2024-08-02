package ru.komiss77.commands;

import ru.komiss77.Ostrov;
import ru.komiss77.builder.BuilderCmd;
import ru.komiss77.listener.ResourcePacksLst;
import ru.komiss77.modules.kits.KitCmd;
import ru.komiss77.modules.player.mission.MissionCmd;


public class RegisterCommands {
    //private static Ostrov plugin;

    public static void register() {
        new HatCmd();//новый способ
        new PrefixCmd();//сравнение

        Ostrov.log_ok("§5Регистрация команд:");
        Ostrov.regCommand(new AdminCmd());
        Ostrov.regCommand(new SuffixCmd());
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
        Ostrov.regCommand(new MissionCmd());
        Ostrov.regCommand(new AnalyticsCmd());
        Ostrov.regCommand(new HomeCmd());
        Ostrov.regCommand(new SkinCmd());
        Ostrov.regCommand(new TprCmd());
        Ostrov.regCommand(new BuilderCmd());
        Ostrov.regCommand(new DonateCmd());
        Ostrov.regCommand(new ResourcePacksLst());

//        plugin.getCommand("statreach").setExecutor(new StatReachCmd());
//        plugin.getCommand("world").setExecutor(new WorldCmd());
//        plugin.getCommand("nbtfind").setExecutor(new NbtfindCmd(plugin));
//        plugin.getCommand("nbtcheck").setExecutor(new NbtcheckCmd(plugin));
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
