package ru.komiss77.commands;

import ru.komiss77.Ostrov;
import ru.komiss77.builder.BuilderCmd;
import ru.komiss77.listener.ResourcePacksLst;
import ru.komiss77.modules.kits.KitCmd;
import ru.komiss77.modules.player.mission.MissionCmd;


public class REGISTER {

    public static void register() {
        Ostrov.log_ok("§5Регистрация команд:");

        new IOO5OOCmd();//100500 команд
        new HatCmd(); //+
        new PrefixCmd(); //+
        new SuffixCmd(); //+

        Ostrov.regCommand(new FlyCmd()); //+
        Ostrov.regCommand(new OpermCmd());//+
        Ostrov.regCommand(new TpposCMD());//+
        Ostrov.regCommand(new TphereCmd());//+
        Ostrov.regCommand(new OcleanCmd());//+ просто /clean перехватывает прокси!!
        Ostrov.regCommand(new InvseeCmd()); //+
        Ostrov.regCommand(new SeenCmd());
        Ostrov.regCommand(new WarpCmd());
        Ostrov.regCommand(new TpaCmd());
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
        Ostrov.regCommand(new SkinCmd());
        Ostrov.regCommand(new TprCmd());
        Ostrov.regCommand(new BuilderCmd());
        Ostrov.regCommand(new DonateCmd());
        Ostrov.regCommand(new ResourcePacksLst());

        //Ostrov.regCommand(new HomeCmd()); переделка на ГУИ
        //Ostrov.regCommand(new AdminCmd()); мелкие команды без аргументов в CMD
        //Ostrov.regCommand(new ProfileCmd());
//        plugin.getCommand("statreach").setExecutor(new StatReachCmd());
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
