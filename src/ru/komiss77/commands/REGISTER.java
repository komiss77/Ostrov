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
        Ostrov.regCommand(new SeenCmd()); //+
        Ostrov.regCommand(new WarpCmd()); //+
        Ostrov.regCommand(new TpaCmd()); //+
        Ostrov.regCommand(new KitCmd()); //+
        Ostrov.regCommand(new PvpCmd()); //+
        Ostrov.regCommand(new EntityCmd()); //+
        Ostrov.regCommand(new PassportCmd()); //+
        Ostrov.regCommand(new ProtocolCmd()); //+
        Ostrov.regCommand(new ReportCmd()); //не даёт русские аргументы
        Ostrov.regCommand(new DonateCmd()); //+

        //модерские
        Ostrov.regCommand(new InvseeCmd()); //+
        Ostrov.regCommand(new SpyCmd()); //+
        Ostrov.regCommand(new BuilderCmd()); //+

        //системные
        Ostrov.regCommand(new WM()); //+
        Ostrov.regCommand(new BossBarCmd()); //+
        Ostrov.regCommand(new OreloadCmd()); //отчёт игроку
        Ostrov.regCommand(new OcleanCmd());//+ просто /clean перехватывает прокси!!
        Ostrov.regCommand(new RewardCmd()); //+
        Ostrov.regCommand(new StatCmd()); //+

        //на проверку
        Ostrov.regCommand(new AnalyticsCmd());
        Ostrov.regCommand(new ResourcePacksLst());

        //на переделку
        Ostrov.regCommand(new Server()); //не даёт русские аргументы
        Ostrov.regCommand(new TprCmd()); //не учитывает мир в аргументе
        Ostrov.regCommand(new MissionCmd()); //не предлагает миссии


    }


    public static void registerAuth() {
        Ostrov.regCommand(new EntityCmd());
        Ostrov.regCommand(new BossBarCmd());
        Ostrov.regCommand(new WM());
    }

    public static void registerPay() {
        Ostrov.regCommand(new RewardCmd());
    }

}
