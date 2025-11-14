package ru.komiss77.commands;

import ru.komiss77.Ostrov;
import ru.komiss77.builder.BuilderCmd;
import ru.komiss77.modules.kits.KitCmd;
import ru.komiss77.modules.netty.RemoteCmd;
import ru.komiss77.modules.player.mission.MissionCmd;


public class REGISTER {

    public static void register() {
        Ostrov.log_ok("§5Регистрация команд:");

        Ostrov.regCommand(new Server());
        Ostrov.regCommand(new BuilderCmd()); //оплеер не создаётся,работать не будет
        Ostrov.regCommand(new WM());
        Ostrov.regCommand(new EntityCmd());
        new WorldCmd();
        new FigureCmd();

        if (Ostrov.MOT_D.startsWith("nb")) {
            return;
        }

        new IOO5OOCmd();//100500 команд
        new HatCmd(); //+
        new FigureCmd(); //+
        new PrefixCmd(); //+
        new SuffixCmd(); //+
        new GmCmd(); //++
        new IGroupCmd();//++
        new ISpecCmd();//++
        new SpyCmd(); //+
        new ProtocolCmd(); //+
        new PvpCmd();
      new PassportCmd();
        Ostrov.regCommand(new FlyCmd()); //+
        Ostrov.regCommand(new OpermCmd());//+
        Ostrov.regCommand(new TpposCMD());//+
        Ostrov.regCommand(new TphereCmd());//+
        Ostrov.regCommand(new SeenCmd()); //+
        Ostrov.regCommand(new WarpCmd()); //+
        Ostrov.regCommand(new TpaCmd()); //+
        Ostrov.regCommand(new KitCmd()); //+
        //Ostrov.regCommand(new EntityCmd()); //+
        Ostrov.regCommand(new DonateCmd()); //+
        Ostrov.regCommand(new MissionCmd()); //+
        Ostrov.regCommand(new TprCmd()); //+
        //Ostrov.regCommand(new Server()); //не даёт русские аргументы - сервера пришлось сделать на англ

        //модерские
        Ostrov.regCommand(new InvseeCmd()); //+
        //Ostrov.regCommand(new BuilderCmd()); //+
        Ostrov.regCommand(new AnalyticsCmd()); //+
        Ostrov.regCommand(new ReportCmd()); //++

        //системные
        //Ostrov.regCommand(new WM()); //+
        Ostrov.regCommand(new BossBarCmd()); //+
        Ostrov.regCommand(new OreloadCmd()); //+
        Ostrov.regCommand(new OcleanCmd());//+ юзаем /oclean. Просто /clean перехватывает прокси!!
        Ostrov.regCommand(new RewardCmd()); //+
        Ostrov.regCommand(new StatCmd()); //+
        new RemoteCmd(); //+

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
