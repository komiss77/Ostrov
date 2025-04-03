package ru.komiss77;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.boot.Registries;
import ru.komiss77.commands.OCommand;
import ru.komiss77.commands.REGISTER;
import ru.komiss77.enums.Module;
import ru.komiss77.enums.*;
import ru.komiss77.events.WorldsLoadCompleteEvent;
import ru.komiss77.hook.SkinRestorerHook;
import ru.komiss77.listener.*;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.netty.OsQuery;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.EmptyChunkGenerator;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;


public class Ostrov extends JavaPlugin {

    public static Ostrov instance;
    public static Registries registries;
    public static LifecycleEventManager<Plugin> mgr;
  public static boolean USE_NETTY_QUERRY = true;
    public static final Map<String, Initiable> modules;
    public static final Random random;
    public static final String L = "Ł";
    public static final String R = "ק";
    public static final String PREFIX = "§2[§aОстров§2] §f";
    public static final String MOT_D;
    public static int server_id = -1;

    public static ComponentLogger logger;
    public static boolean newDay, dynmap, wg;
    public static boolean SHUT_DOWN; //по этому плагу другие плагины не будут сохранять данные асинх   org.bukkit.plugin.IllegalPluginAccessException: Plugin attempted to register task while disabled
    public static boolean STARTUP = true; //до окончания прогрузки всех миров
    public static final Calendar calendar; //не двигать,не переименовывать! direct USE!
    private static final Date date;
    private static final SimpleDateFormat full_sdf;
    private final static boolean windows;

    static {
        random = new Random();
        modules = new HashMap<>();//EnumMap(Object.class);
        calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        date = new java.util.Date(System.currentTimeMillis());
        full_sdf = new java.text.SimpleDateFormat("dd.MM.yy HH:mm");
        MOT_D = TCUtil.deform(Bukkit.motd());
        final String os = System.getProperty("os.name");
        windows = os.startsWith("Windows");
    }


    //на папер плагин не работает
    @Override
    //вызывается из CraftServer  public ChunkGenerator getGenerator(String world), если указать в bukkit.yml worlds: world: generator: Ostrov
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new EmptyChunkGenerator();
    }



    @Override
    public void onLoad() {
        instance = this;
        mgr = instance.getLifecycleManager();
        logger = getComponentLogger();
        //Cfg.init(); // 1 !  загрузится само при первом обращении
        Nms.pathServer();
        Nms.chatFix();
    }


    @Override
    public void onEnable() {
        registries = new Registries();
      if (USE_NETTY_QUERRY) new OsQuery();
        //первый инит синхронно, или плагины пишут состояние, когда еще нет соединения!!
        RemoteDB.init(MOT_D.length() > 3 && !MOT_D.startsWith("nb"), false); //pay, авторизация - права не грузим. если ставить в onLoad то не может запустить async task!
        Timer.init(); //на статичную загрузку не переделать, к таймеру может никто не обращаться!


        log_ok("§5===== Регистрация каналов Proxy =====");
        for (final Chanell ch : Chanell.values()) {
          if (ch == Chanell.SKIN && !SkinRestorerHook.USE) continue;
          instance.getServer().getMessenger().registerOutgoingPluginChannel(instance, ch.name);
          instance.getServer().getMessenger().registerIncomingPluginChannel(instance, ch.name, new SpigotChanellMsg());
        }

        if (MOT_D.length() == 3) { // для серверов авторизации
          log_warn("§bРежим Auth (Newbie)");
            REGISTER.registerAuth();
            Bukkit.getPluginManager().registerEvents(new SpigotChanellMsg(), this);
            if (MOT_D.startsWith("nb")) {
              REGISTER.register(); //подключатся только нужные для nb
            }
            return;
        }

        log_ok("§5===== Регистрация слушателей : onEnable =====");
      Bukkit.getPluginManager().registerEvents(new ChatLst(), instance);
      Bukkit.getPluginManager().registerEvents(new SpigotChanellMsg(), instance); //в режиме AUTH инициализация дубль выше
      Bukkit.getPluginManager().registerEvents(new ServerLst(), instance);
      Bukkit.getPluginManager().registerEvents(new PlayerLst(), instance);
      Bukkit.getPluginManager().registerEvents(new InteractLst(), instance);
      Bukkit.getPluginManager().registerEvents(new TestLst(), instance);
      Bukkit.getPluginManager().registerEvents(new GlobalBugFix(), instance);

        if (Cfg.getConfig().getBoolean("system.use_armor_equip_event")) {
          Bukkit.getPluginManager().registerEvents(new ArmorEquipLst(), instance);
        }

        Bukkit.getOnlinePlayers().forEach(PM::createOplayer);

        LocalDB.init();// выполнится синхронно, если нет коннекта-подвиснет! выше есть для auth

        log_ok("§5===== Инициализация модулей =====");
        for (final Module module : Module.values()) {
            try {
                modules.put(module.name(), module.clazz.getDeclaredConstructor().newInstance());
            } catch (Exception ex) {
                log_err("**** инициализацяя " + module + " : " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        REGISTER.register(); //после модулей!!

        log_ok("§2Остров готов к работе!");

    }


    @Override
    public void onDisable() {
      SHUT_DOWN = true;
      HandlerList.unregisterAll(instance);
      RemoteDB.Disconnect();
      OsQuery.shutdown();
      if (MOT_D.length() == 3) return;
      for (final Player p : Bukkit.getOnlinePlayers()) {
        PM.onLeave(p, false);
      }
      //if (PM.hasOplayers()) {
      //for (Oplayer op : PM.getOplayers()) {
      //op.onLeave(op.getPlayer(), false);//LocalDB.saveLocalData(op.getPlayer(), op); //сохранить синхронно!!
      //}
      //}
      if (LocalDB.useLocalData) {
        LocalDB.Disconnect();
      }
      if (RemoteDB.useOstrovData) {
        RemoteDB.Disconnect();
      }

      modules.values().forEach(
          (module) -> (module).onDisable()
      );
      log_ok("§4Остров выгружен!");
    }



    public static void regCommand(final OCommand cmd) {
        mgr.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(cmd.command(), cmd.description(), cmd.aliases());
        });
    }

    public static void regCommands(final OCommand... cmds) {
        mgr.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            for (final OCommand cmd : cmds) {
                commands.register(cmd.command(), cmd.description(), cmd.aliases());
            }
        });
    }

    public static void postWorld() { //вызывается один раз при старте сервера после загрузки миров
        log_ok("");
        log_ok("§5===== Регистрация слушателей : postWorld =====");
        WorldManager.autoLoadWorlds(); // 1 !! найти и догрузить миры, помеченные на автозагрузку
        GM.onWorldsLoadDone(); //прописать состояние серверов на таблички

        modules.values().forEach(
            (module) -> {
                try {
                    (module).postWorld();
                } catch (Exception ex) {
                    log_err("postWorld module " + module + " : " + ex.getMessage());
                }
            }
        );

        new WorldsLoadCompleteEvent().callEvent(); // оповестить остальные плагины
        Ostrov.STARTUP = false;

        if (GM.GAME == Game.AR) {
            Bukkit.getServer().getPluginManager().registerEvents(new ArcaimLst(), instance);
            //PlayerPacketHandler.nbtCheck.set(true);
            log_ok("§eПодключены ивенты для сервера Аркаим");
        }
        //if (Ostrov.MOT_D.equals("home1")) {
        //    Bukkit.getServer().getPluginManager().registerEvents(new ArcaimLst(), instance);
        //    log_warn("§eПодключен nbtCheck для home1");
        //}

    }

    public static Collection<Initiable> getModules() {
        return modules.values();
    }

    public static Initiable getModule(final Module mod) {
        return modules.get(mod.name());
    }

    public static Ostrov getInstance() {
        return Ostrov.instance;
    }



//    public static final String prefixOK = "§a[§2Остров§a] §7";//"\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] ";
//    public static final String prefixWARN = "§e[§6Остров§e] §7";//"\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001B[33m";
//    public static final String prefixERR = "§c[§4Остров§c] §7";//"\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001B[31m";

    public static void log(String s) {
      logger.info(TCUtil.form(s));
    }

    public static void log_ok(String s) {
      //if (!windows) { хм, в линуксе тоже цвета грохнулись, используем костыль
      //  logger.info(s);//logger.info(TCUtil.form(s));
      //} else {//кринж      цветов в винде не появилось, вернул кодировку консоли. Еще ComponentLogger ставит в начале название плагина, всегда монохромное
        if (s.startsWith("§") && s.length() >= 2) {
          final String strip = s.substring(2);
          switch (s.charAt(1)) {
            case '0' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;232m" + strip;
            case '1' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[34;1m" + strip;
            case '2' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;29m" + strip;
            case '3' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;6m" + strip;
            case '4' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;1m" + strip;
            case '5' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;128m" + strip;
            case '6' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;214m" + strip;
            case '7' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;241m" + strip;
            case '8' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;238m" + strip;
            case '9' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;63m" + strip;
            case 'a' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[32;1m" + strip;
            case 'b' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[36;1m" + strip;
            case 'c' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;9m" + strip;
            case 'd' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;207m" + strip;
            case 'e' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[33m" + strip;
            case 'f' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[37m" + strip;
            default -> {
              Bukkit.getLogger().info(s);
              return;
            }
          }
          Bukkit.getLogger().info(s + "\u001B[0m");
        } else {
          Bukkit.getLogger().info("\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] " + s);
        }
      // }
    }

    public static void log_warn(String s) {
        logger.warn(TCUtil.form(s));
    }

    public static void log_err(String s) {
      logger.warn(TCUtil.form(s));//Bukkit.getLogger().log(Level.SEVERE, prefixERR+s);
        if (LocalDB.useLocalData && LocalDB.connection != null) {
            try (PreparedStatement pst1 = LocalDB.connection.prepareStatement("INSERT INTO `errors` (`msg`) VALUES (?);")) {
                pst1.setString(1, s);
                pst1.execute();
            } catch (SQLException ex) {
                Bukkit.getConsoleSender().sendMessage("§clog_err ошибка записи в БД: " + ex.getMessage());
            }
        }
    }

  //@Deprecated
    public static void globalLog(final GlobalLogType type, final String sender, final String msg) {
        RemoteDB.executePstAsync(Bukkit.getConsoleSender(),
            "INSERT INTO globalLog (type,server,sender,msg,time) VALUES ('" + type.name() + "', '" + Ostrov.MOT_D + "', '" + sender + "', '" + msg + "', '" + Timer.getTime() + "'); ");
    }

  public static void history(final HistoryType type, final Oplayer op, final String msg) {
    RemoteDB.executePstAsync(Bukkit.getConsoleSender(),
        "INSERT INTO " + Table.HISTORY.table_name + " (`action`, `sender`, `target`, `target_ip`, `report`, `data`, `note`) VALUES ('" + type.toString() + "','','" + op.nik + "','" + op.getDataString(Data.IP) + "','" + msg + "','" + Timer.secTime() + "',''); ");
    ;
  }


    public static String dateFromStamp(int stamp_in_second) {
        date.setTime(stamp_in_second * 1000L);
        return full_sdf.format(date);
    }

    public static String getCurrentHourMin() {
        return calendar.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE));
    }

    public static void sync(final Runnable runnable) { //use sync ( ()->{} ,1 );
        if (runnable == null || SHUT_DOWN)
            return; //SHUT_DOWN для фикса IllegalPluginAccessException: Plugin attempted to register task while disabled
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            }.runTask(instance);
        }
    }


    public static void sync(final Runnable runnable, final int delayTicks) { //sync ( ()->{} ,1 );
        if (delayTicks == 0) {
            sync(runnable);
        } else {
            if (runnable == null || SHUT_DOWN) return;
            new BukkitRunnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            }.runTaskLater(instance, delayTicks);
        }
    }

    public static void async(final Runnable runnable) { //sync ( ()->{} ,1 );
        if (runnable == null || SHUT_DOWN) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskAsynchronously(instance);
    }


    public static void async(final Runnable runnable, final int delayTicks) { //sync ( ()->{} ,1 );
        if (delayTicks == 0) {
            async(runnable);
        } else {
            if (runnable == null || SHUT_DOWN) return;
            new BukkitRunnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            }.runTaskLaterAsynchronously(instance, delayTicks);
        }
    }

    public static boolean debug() {
        return MOT_D.equals("home1");
    }
}




    /*
    Всего в ANSI 256 цветов. Они составляются так: \u001b[38;5;КОДm, где вместо КОД — число от 0 до 255:
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    Bright Black: \u001b[30;1m
    Bright Red: \u001b[31;1m
    Bright Green: \u001b[32;1m
    Bright Yellow: \u001b[33;1m
    Bright Blue: \u001b[34;1m
    Bright Magenta: \u001b[35;1m
    Bright Cyan: \u001b[36;1m
    Bright White: \u001b[37;1m
    */
        /*if (s.startsWith("§") && s.length()>=2) {
            final String strip = s.substring(2);
            switch (s.charAt(1)) {
                case '0' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;232m"+strip;
                case '1' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[34;1m"+strip;
                case '2' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;29m"+strip;
                case '3' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;6m"+strip;
                case '4' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;1m"+strip;
                case '5' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;128m"+strip;
                case '6' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;214m"+strip;
                case '7' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;241m"+strip;
                case '8' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;238m"+strip;
                case '9' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;63m"+strip;
                case 'a' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[32;1m"+strip;
                case 'b' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[36;1m"+strip;
                case 'c' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;9m"+strip;
                case 'd' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[38;5;207m"+strip;
                case 'e' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[33m"+strip;
                case 'f' -> s = "\u001b[32;1m[\u001B[38;5;28mОстров\u001b[32;1m] \u001b[37m"+strip;
                default -> {
                    Bukkit.getLogger().info(prefixOK+strip);
                    return;
                }
            }
            Bukkit.getLogger().info(s+"\u001B[0m");
        } else {
           Bukkit.getLogger().info(prefixOK+s);
        }*/   
