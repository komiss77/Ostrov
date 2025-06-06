package ru.komiss77;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Operation;
import ru.komiss77.enums.ServerType;
import ru.komiss77.enums.Table;
import ru.komiss77.events.RestartWarningEvent;
import ru.komiss77.listener.PlayerLst;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.Informator;
import ru.komiss77.modules.games.ArenaInfo;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.modules.netty.OsQuery;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.TimeUtil;
import ru.komiss77.version.Nms;


public class Timer {

    private static BukkitTask timer;
    private static BukkitTask playerTimer;
    public static int syncSecondCounter = 1; //начинаем с 1, чтобы сразу не срабатывало %x==0

    public static boolean auto_restart, to_restart;
    private static int rstHour, rstMin;

    private static boolean perms_autoupdate, mission_tick, jailMode, authMode;
    private static int reloadPermIntervalSec;

    private static final ConcurrentHashMap<Integer, Integer> cd;
    private static long tickTime = System.currentTimeMillis() / 50l;
    private static final int MIDNIGHT_STAMP;
    private static final AtomicBoolean lockQuery = new AtomicBoolean(false);
    private static final AtomicBoolean lockSecond = new AtomicBoolean(false);
    private static final Map<Integer, RemoteDB.Qinfo> map;
    private static final List<Task> tasks;
    private static int count;

    static {
        cd = new ConcurrentHashMap<>();
        Ostrov.calendar.setTimeInMillis(System.currentTimeMillis());
        Ostrov.calendar.set(Calendar.DAY_OF_YEAR, Ostrov.calendar.get(Calendar.DAY_OF_YEAR) + 1);
        Ostrov.calendar.set(Calendar.HOUR_OF_DAY, 0);
        Ostrov.calendar.set(Calendar.MINUTE, 0);
        Ostrov.calendar.set(Calendar.SECOND, 0);
        MIDNIGHT_STAMP = (int) (Ostrov.calendar.getTimeInMillis() / 1000);
        map = new HashMap<>();
        tasks = new LinkedList<>();
    }

    private static final class Task {
        private final Pred fin;
        private final String id;
        private final int period;
        private int tries;

        private Task(final Pred fin, final String id, final int period, final int tries) {
            this.fin = fin;
            this.id = id;
            this.period = period;
            this.tries = tries;
        }
    }
    public interface Pred {boolean test();}
    public static void task(final Pred fin, final String id, final int period) {
        task(fin, id, period, Integer.MAX_VALUE);
    }
    public static void task(final Pred fin, final String id, final int period, final int tries) {
        if (fin.test()) return;
        tasks.add(new Task(fin, id, period, tries));
    }

    public static void init() {

        auto_restart = Cfg.getConfig().getBoolean("system.autorestart.use");
        rstHour = Cfg.getConfig().getInt("system.autorestart.hour", 3);
        if (rstHour < 0 || rstHour > 23) rstHour = 3;
        rstMin = Cfg.getConfig().getInt("system.autorestart.min", 30);
        if (rstMin < 0 || rstMin > 59) rstHour = 30;
        //restart_time = (rstHour<=9?"0"+rstHour:""+rstHour) + ":" + (rstMin<=9?"0"+rstMin:""+rstMin);
        if (auto_restart) Ostrov.log_ok("§6Установлено время авторестарта :" + rstHour + ":" + rstMin);

        perms_autoupdate = Cfg.getConfig().getBoolean("ostrov_database.auto_reload_permissions");
        reloadPermIntervalSec = Cfg.getConfig().getInt("ostrov_database.auto_reload_permissions_interval_min") * 60;
        if (reloadPermIntervalSec < 10 || reloadPermIntervalSec > 10800) reloadPermIntervalSec = 600;
        if (perms_autoupdate)
            Ostrov.log_ok("§5Автообновление прав с интервалом " + TimeUtil.secondToTime(reloadPermIntervalSec));
        
       /* Ostrov.async( ()-> {
            try {
                final NTPUDPClient timeClient = new NTPUDPClient();
                final InetAddress inetAddress = InetAddress.getByName("ntp.ubuntu.com");
                final TimeInfo timeInfo = timeClient.getTime(inetAddress);
                final long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
                time_delta=(int)(System.currentTimeMillis()-returnTime);
                Ostrov.calendar.setTimeInMillis(System.currentTimeMillis() - time_delta); 
                Ostrov.log_ok("время NTP:"+returnTime+", Время системное:"+System.currentTimeMillis()+", разница:"+time_delta);
            } catch (IOException ex) {
                Ostrov.log_err("Не удалось получить NTP time "+ex.getMessage());
            }
        }, 0);     */

        if (timer != null) timer.cancel();
        if (playerTimer != null) playerTimer.cancel();
        //if (timerAsync != null) timerAsync.cancel();

        if (Ostrov.MOT_D.length() == 3) { //pay, авторизация
            authMode = !Ostrov.MOT_D.equals("nb0"); //на новичках в authMode не работает боссбар!
        } else if (Ostrov.MOT_D.equals("jail")) { //jail 
            jailMode = true;
        } else {
            mission_tick = true;
        }

        timer = new BukkitRunnable() {
            int time_left = 300;
            int tick;

            @Override
            public void run() {
                tickTime = System.currentTimeMillis() / 50l;
                if (tick++ % 20 != 0) return;

                Ostrov.calendar.setTimeInMillis(tickTime * 50l);

                if (auto_restart && syncSecondCounter % 60 == 0) {
                    if (rstHour == Ostrov.calendar.get(Calendar.HOUR_OF_DAY) && rstMin == Ostrov.calendar.get(Calendar.MINUTE)) {
                        to_restart = true;
                        auto_restart = false;
                    }
                }
                if (to_restart) {
                    if (time_left == 300) {
                        Bukkit.getPluginManager().callEvent(new RestartWarningEvent(time_left));
                    }
                    if (time_left == 300 || time_left == 180 || time_left == 120 || time_left == 60) {
                        Bukkit.broadcast(TCUtil.form("§cВНИМАНИЕ! §cПерезапуск сервера через " + time_left / 60 + " мин.!"));
                    }
                    if (time_left == 15) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            PlayerLst.PREPARE_RESTART = true;
                            p.kick(Component.text("§eСервер перезагружается."), PlayerKickEvent.Cause.PLUGIN);
                        }
                    }
                    if (time_left == 0) {
                        this.cancel();
                        Ostrov.SHUT_DOWN = true;
                        Bukkit.shutdown();
                        return;
                    }
                    time_left -= 1;
                }

                if (lockSecond.compareAndSet(false, true)) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            asyncSecondJob(syncSecondCounter);
                        }
                    }.runTaskAsynchronously(Ostrov.instance);
                }

                if (!cd.isEmpty()) {
                    cd.entrySet().removeIf(entry -> entry.getValue() <= secTime());//чтобы точнее ловить если надо меньше секунды
                }

                syncSecondCounter++;


            }
        }.runTaskTimer(Ostrov.instance, 20, 1);

        //вынес отдельно - редис вечно багается
        //ex.printStackTrace();
        new BukkitRunnable() { //вынес отдельно - редис вечно багается
            int sec;

            @Override
            public void run() {
                OsQuery.heartBeat(sec);
                //if (sec % 20 == 0) {
                //    OsQuery.request("======= test request sec=" + sec);
                //}
                try {
                    //RDS.heartbeats();
                    if (sec % 43 == 0) { //заслать данные арен с этого серв без учёта игры (могут быть несколько миниигр на ядре)
                        GM.getGames().stream().forEach((gi -> {
                            gi.arenas().stream().filter(ai -> ai.server.equals(Ostrov.MOT_D)).forEach(ArenaInfo::sendData);
                        }));
                    }
                    if ((Ostrov.server_id > 0 && sec % 10 == 0) || GM.GAME.type == ServerType.ONE_GAME) { //на диагностике и одиночки
                        GameInfo gi = GM.getGameInfo(GM.GAME); //взять игру этого ядра
                        if (gi != null) {
                            gi.arenas().stream().findAny().ifPresent(ai -> {
                                ai.players = Bukkit.getOnlinePlayers().size();
                                ai.sendData();
                            });
                        }
                    }
                } catch (Exception ex) {
                    Ostrov.log_warn("redisTimer : " + ex.getMessage());
                    //ex.printStackTrace();
                }
                sec++;
            }
        }.runTaskTimerAsynchronously(Ostrov.instance, 33, 20);

        //обход игроков каждую секунду с разбросом по тикам для распределения нагрузки
        playerTimer = new BukkitRunnable() {
            int jailed;
            int syncTick;

            @Override
            public void run() {

                //отправить запросы в БД острова
                if (RemoteDB.useOstrovData && RemoteDB.ready && !RemoteDB.QUERY.isEmpty()) {
                    if (lockQuery.compareAndSet(false, true)) { //асинхронная задача не начиналась или выполнена
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                sendQuery();
                            }
                        }.runTaskAsynchronously(Ostrov.instance);
                    }
                }

                if (!authMode) {
                    PM.getOplayers().forEach(op -> {
                        op.tick++;
                        if (op.tick != 20) return;
                        op.tick = 0;
                        op.secondTick();
                        if (!jailMode || op.isStaff) return;
                        //op.getPlayer().sendMessage("BAN_TO="+op.getDataInt(Data.BAN_TO));
                        jailed = op.globalInt(Data.JAILED);// - getTime();
                        if (jailed > 0) jailed--;
                        op.globalInt(Data.JAILED, jailed);
                        if (jailed <= 0) {
                            ApiOstrov.sendToServer(op.getPlayer(), "lobby0", "");
                            return;
                        }
                        op.score.getSideBar().setTitle("§4Чистилище");
                        op.score.getSideBar().update(9, "§7До разбана:");
                        op.score.getSideBar().update(8, "§e" + TimeUtil.secondToTime(jailed));
                    });
                }

                if (!tasks.isEmpty()) {
                    tasks.removeIf(ts -> {
                        if (syncTick % ts.period != 0) return false;
                        if (ts.tries-- < 0) {
                            Ostrov.log_warn("Task " + ts.id + " timed out!");
                            return true;
                        }
                        return ts.fin.test();
                    });
                }
                syncTick++;
            }
        }.runTaskTimer(Ostrov.instance, 20, 1);

    }


    private static void asyncSecondJob(final int second) {

        try {
            if (!authMode) {
                if (second % 11 == 0 && PM.hasOplayers()) {  //11*20
                    SpigotChanellMsg.sendMessage(Bukkit.getOnlinePlayers().stream().findAny().get(), Operation.GET_ONLINE);
                }
                Informator.tickAsync();
            }

            //try {
            //    RDS.heartbeats();
            //} catch (Exception ex) {
            //     Ostrov.log_warn("RDS Timer asyncSecondJob : " + ex.getMessage());
            //ex.printStackTrace();
            // }

            //if (second % 43 == 0) {
            //    GM.getGames().stream().forEach( (gi -> {
            //        gi.arenas().stream().filter(ai -> ai.server.equals(Ostrov.MOT_D)).forEach(ArenaInfo::sendData);
            //    }));
            //}

            if (RemoteDB.useOstrovData) {

                if (second % 15 == 0) {
                    try {
                        final Connection conn = RemoteDB.getConnection();
                        if (conn == null || conn.isClosed() || !conn.isValid(1)) { //(!RemoteDB.ready) {
                            Ostrov.log_warn("Timer - восстанавливаем соединение с Ostrov DB...");
                            RemoteDB.connect();
                        }
                    } catch (SQLException ex) {
                        Ostrov.log_warn("Timer - проверка соединения с Ostrov DB : " + ex.getMessage());
                    }
                }

                if (RemoteDB.ready) {
                    if (Ostrov.server_id > 0 && second % 10 == 0) { //нашел себя в таблице - писать состояние каждые 10 сек
                        //RemoteDB.writeThisServerStateToRemoteDB();
                        final String querry = "UPDATE " + Table.BUNGEE_SERVERS.table_name +
                            " SET `online`='" + Bukkit.getOnlinePlayers().size() + "', `onlineLimit`='"
                            + Bukkit.getMaxPlayers() + "', `tps`='" + Nms.getTps() + "', `memory`='"
                            + (int) (Runtime.getRuntime().totalMemory() / 1024 / 1024) + "', `memoryLimit`='"
                            + (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "', `freeMemory`='"
                            + (int) (Runtime.getRuntime().freeMemory() / 1024 / 1024)
                            + "',`stamp`='" + Timer.secTime() + "',`ts`= NOW()+0 WHERE `serverId`='" + Ostrov.server_id + "'; ";
                        RemoteDB.executePstAsync(Bukkit.getConsoleSender(), querry);
                    }

                    if (!authMode) {

                        if (perms_autoupdate && !to_restart && second % reloadPermIntervalSec == 0) {
                            Perm.loadGroupsDB(false);
                        }

                        //если игры еще не пытались грузиться, fromStamp = 0
                        //если игры уже прогрузились, fromStamp > 0
                        //если была ошибка при первой загрузке, fromStamp = -1
                        if (GM.state != GM.State.STARTUP && second % GM.LOAD_INTERVAL == 0) {//if (GM.fromStamp!=0 && second%GM.LOAD_INTERVAL==0) {
                            GM.loadArenaInfo(); //там же подгрузит обновы Lang
                        }

                        if (mission_tick) {
                            if (second % 63 == 0 || second == 10) { //на 10 секунде после старта и каждую минуту
                                MissionManager.loadMissions();
                            }
                            MissionManager.tickAsync();
                        }

                    }
                }
            }

            if (LocalDB.useLocalData && second % 14 == 0) {
                try {
                    if (LocalDB.connection == null || LocalDB.connection.isClosed() || !LocalDB.connection.isValid(1)) {
                        Ostrov.log_warn("Timer - восстанавливаем соединение с Local DB...");
                        LocalDB.connect();
                    }
                } catch (SQLException ex) {
                    Ostrov.log_err("Timer - соединение с Local DB восстановить не удалось!");
                }
            }

        } catch (Exception ex) { //обязательно все Exception - надо вернуть флаг lockSecond, или блокируется запись!
            Ostrov.log_err("Timer asyncSecondJob : " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            lockSecond.set(false);
        }


    }


    private static void sendQuery() {
//final long l = System.currentTimeMillis();
        Statement stmt = null;
        RemoteDB.Qinfo qInfo;
        try {
            stmt = RemoteDB.getConnection().createStatement();
            while ((qInfo = RemoteDB.QUERY.poll()) != null) {
                try {
                    stmt.execute(qInfo.query);
                } catch (SQLException | NullPointerException ex) {
                    CommandSender cs = qInfo.senderName == null ? null : qInfo.senderName.equals("console") ? Bukkit.getConsoleSender() : Bukkit.getPlayerExact(qInfo.senderName);
                    if (cs != null)
                        cs.sendMessage("§cОшибка выполнения запроса " + qInfo.query + " : " + ex.getMessage());
                    Ostrov.log_err("Timer executeQuery " + qInfo.query + " : " + ex.getMessage());
                }
            }
        } catch (Exception ex) {
            Ostrov.log_err("Timer sendQuery createStatement : " + ex.getMessage());
        } finally {
            try {
                if (stmt != null && !stmt.isClosed()) {
                    stmt.close();
                }
            } catch (SQLException ex) {
                Ostrov.log_err("Timer sendQuery stmt.close : " + ex.getMessage());
            }
            lockQuery.set(false);
        }

    }


    public static void add(final String name, final String type, final int seconds) { //getEntityId - нельзя, после перезахода другой!!
        if (seconds <= 0) return;
        cd.put(name.hashCode() ^ type.hashCode(), secTime() + seconds);
    }

    public static void del(final String name, final String type) {
        cd.remove(name.hashCode() ^ type.hashCode());
    }

    public static boolean has(final String name, final String type) {
        //final int id = name.hashCode() ^ type.hashCode();
        //Integer endStamp = cd.get(id);
        return cd.containsKey(name.hashCode() ^ type.hashCode());
        //return endStamp == null ? false : currentTime > endStamp;
    }

    public static int getLeft(final String name, final String type) {
        int left = cd.getOrDefault(name.hashCode() ^ type.hashCode(), 0);
        return left == 0 ? 0 : left - Timer.getTime();
    }


    public static void add(final Player p, final String type, final int seconds) { //getEntityId - нельзя, после перезахода другой!!
        if (seconds <= 0) return;
        cd.put(p.getName().hashCode() ^ type.hashCode(), secTime() + seconds);
    }

    public static void del(final Player p, final String type) { //вроде не работает, чекнуть потом
        cd.remove(p.getName().hashCode() ^ type.hashCode());
    }

    public static boolean has(final Player p, final String type) {
        return cd.containsKey(p.getName().hashCode() ^ type.hashCode());
    }

    public static int getLeft(final Player p, final String type) {
        int left = cd.getOrDefault(p.getName().hashCode() ^ type.hashCode(), 0);
        return left == 0 ? 0 : left - Timer.getTime();
    }

    public static void add(final int id, final int seconds) {
        if (seconds <= 0) return;
        cd.put(id, secTime() + seconds);
    }

    public static void del(final int id) {
        cd.remove(id);
    }

    public static boolean has(final int id) {
        return cd.containsKey(id);
    }

    public static int getLeft(final int id) {
        int left = cd.getOrDefault(id, 0);
        return left == 0 ? 0 : left - Timer.getTime();
    }
    @Deprecated
    public static int getTime() { //штам текущей секунды сервера в UNIX time
        return secTime();
    }

    public static int secTime() {
        return (int) (tickTime / 20l);
    }

    public static long tickTime() {
        return tickTime;
    }
    @Deprecated
    public static int leftBeforeResetDayly() {
        return leftBeforeResetDaily();
    }

    public static int leftBeforeResetDaily() {
        return MIDNIGHT_STAMP - secTime();
    }

    @Deprecated
    public static long getTimeStamp() {
        //return System.currentTimeMillis() - time_delta;
        return System.currentTimeMillis();
    }

    public static class Test {
        private final long start;
        private long last;
        public Test() {
            start = System.currentTimeMillis();
            last = start;
        }

        public void run(final CommandSender cs, final String msg, final boolean total) {
            final long now = System.currentTimeMillis();
            cs.sendMessage(msg + (total ? ", last=" + (now-last) + ", total=" + (now-start) : ", last=" + (now-last)));
            last = now;
        }
    }
}

 


















    
/*
    private static void startAuthMode () {
          
                    
            timerAsync =  new BukkitRunnable() {
                int asyncSecondCounter;
                
                @Override
                public void run() {
                    
                    //checkRemoteDBConnection(asyncSecondCounter);
                        
                    if (RemoteDB.useOstrovData ) {//if (RemoteDB.useOstrovData && RemoteDB.connection!=null) {-не поставт флаг RemoteDBErrors!

                        if (Ostrov.server_id>0 && asyncSecondCounter%10==0 ) { //нашел себя в таблице - писать состояние каждые 10 сек
                            RemoteDB.writeThisServerStateToRemoteDB();
                        }

                    }

                    //checkLocalDBConnection(asyncSecondCounter);
                    
                }


            }.runTaskTimerAsynchronously(Ostrov.instance, 21, 20);

        }*/
    
    
    
    
    

  /*  public static  void checkRemoteDBConnection() {
//Ostrov.log("checkRemoteDBConnection useOstrovData="+RemoteDB.useOstrovData+" asyncSecondCounter="+asyncSecondCounter);
       // if (asyncSecondCounter%55==0 ) {

//Ostrov.log("RemoteDBErrors="+RemoteDBErrors);
            // try {
                 //if (RemoteDB.errors>=10 || RemoteDB.connection==null || RemoteDB.connection.isClosed() || !RemoteDB.connection.isValid(3)) {
                 if (!RemoteDB.ready) {
                    //RemoteDBErrors = false;
                    Ostrov.log_warn("Timer - восстанавливаем соединение с Ostrov DB...");
                    RemoteDB.connect();
                 }
            // } catch (SQLException ex) {
           //      Ostrov.log_err("Timer - соединение с Ostrov DB восстановить не удалось!");
          //   }                        
     //    }
     //                      
    }   
    
    
    
    public static  void checkLocalDBConnection(int asyncSecondCounter) {

    if (LocalDB.useLocalData && asyncSecondCounter%14==0 ) {

        try {
            if (LocalDB.connection==null || LocalDB.connection.isClosed() || !LocalDB.connection.isValid(1)) {
               Ostrov.log_warn("Timer - восстанавливаем соединение с Local DB...");
               LocalDB.connect();
            }
        } catch (SQLException ex) {
            Ostrov.log_err("Timer - соединение с Local DB восстановить не удалось!");
        }                        

    }
                           
    }    
    
        */ 
    
    
    
            
      
/*        timer =  new BukkitRunnable() {

            int time_left = 300;

                @Override
                public void run() {

                    currentTime =  (int) ((System.currentTimeMillis()-time_delta)/1000);
                    Ostrov.calendar.setTimeInMillis(System.currentTimeMillis()-time_delta);
//System.out.println("auto_restart?"+auto_restart+" rstHour="+rstHour+" rstMin="+rstMin+" now="+Ostrov.calendar.get(Calendar.HOUR_OF_DAY)+" "+Ostrov.calendar.get(Calendar.MINUTE));
                    
                    if (auto_restart && syncSecondCounter%60==0 ) {
//System.out.println("time_delta="+time_delta+" currentTime="+currentTime+" rstHour="+rstHour+" rstMin="+rstMin+" время="+Ostrov.calendar.get(Calendar.HOUR_OF_DAY)+":"+Ostrov.calendar.get(Calendar.MINUTE));
                        if (rstHour == Ostrov.calendar.get(Calendar.HOUR_OF_DAY) && rstMin == Ostrov.calendar.get(Calendar.MINUTE)) {
                            to_restart=true;
                            auto_restart = false;
                        }
                    } 
                    if (to_restart) {
                        if (time_left==300) {
                            Bukkit.getPluginManager().callEvent(new RestartWarningEvent ( time_left ) );
                        }
                        if (time_left==300 || time_left==180 || time_left==120 || time_left==60) {
                            Bukkit.broadcast(TCUtils.format("§cВНИМАНИЕ! §cПерезапуск сервера через "+time_left/60+" мин.!"));
                        }
                        if (time_left==0) {
                            this.cancel();
                            //синхронный дисконнект от БД, чтобы не висело соединение
                            if (RemoteDB.useOstrovData) {
                                RemoteDB.Disconnect();
                            }                            
                            if (LocalDB.useLocalData) {
                                LocalDB.Disconnect();
                            }                            
                            Bukkit.shutdown();
                            return;
                        }
                        time_left-=1;
                    }

                    cd.values().removeIf(value -> value <= currentTime);
                    
                    MissionManager.tick();
                    
                    syncSecondCounter++;

//Ostrov.log_warn("disable? "+SpigotConfig.disableAdvancementSaving+" list="+Arrays.toString(SpigotConfig.disabledAdvancements.toArray()));

                }}.runTaskTimer(Ostrov.instance, 20, 20);
*/
        
        

                    
   
    /*    timer =  new BukkitRunnable() {

            boolean to_restart = false;
            int time_left = 300;

                @Override
                public void run() {

                    currentTime =  (int) ((System.currentTimeMillis()-time_delta)/1000);
                    Ostrov.calendar.setTimeInMillis(System.currentTimeMillis()-time_delta);
//System.out.println("currentTime="+currentTime+" dateFromStamp="+Ostrov.dateFromStamp(currentTime)+" hour_min="+Ostrov.getCurrentHourMin());
                    
                    if (auto_restart && syncSecondCounter%60==0 ) {
//System.out.println("time_delta="+time_delta+" currentTime="+currentTime+" rs="+rs+" restart_time="+restart_time+" время="+Ostrov.getCurrentHourMin()+" equals?"+(restart_time.equals(Ostrov.getCurrentHourMin()) ));
                        if (rstHour == Ostrov.calendar.get(Calendar.HOUR_OF_DAY) && rstMin == Ostrov.calendar.get(Calendar.MINUTE)) {//if (restart_time.equals(Ostrov.getCurrentHourMin())) {
                            to_restart=true;
                            auto_restart = false;
                        }

                    } 
                    if (to_restart) {
                        if (time_left==300) {
                            Bukkit.getPluginManager().callEvent(new RestartWarningEvent ( time_left ) );
                        }
                        if (time_left==300 || time_left==180 || time_left==120 || time_left==60) {
                            Bukkit.broadcast(TCUtils.format("§cВНИМАНИЕ! §cПерезапуск сервера через "+time_left/60+" мин.!"));
                        }
                        if (time_left==0) {
                            this.cancel();
                            Bukkit.shutdown();
                            return;
                        }
                        time_left-=1;
                    }

                    cd.values().removeIf(value -> value <= currentTime);

                    syncSecondCounter++;

                }}.runTaskTimer(Ostrov.instance, 20, 20);*/


// load0 = Title.title( Component.text(""), Component.text("§fЗагрузка данных §7[§a|§7]"),  Title.Times.of(Duration.ZERO, Duration.ofMillis(5*50), Duration.ofMillis(10*50)) );
//  load1 = Title.title( Component.text(""), Component.text("§fЗагрузка данных §7[§a||§7]"),  Title.Times.of(Duration.ZERO, Duration.ofMillis(5*50), Duration.ofMillis(10*50)) );
//  load2 = Title.title( Component.text(""), Component.text("§fЗагрузка данных §7[§a|||§7]"),  Title.Times.of(Duration.ZERO, Duration.ofMillis(5*50), Duration.ofMillis(10*50)) );
//  load3 = Title.title( Component.text(""), Component.text("§fЗагрузка данных §7[§a||||§7]"),  Title.Times.of(Duration.ZERO, Duration.ofMillis(5*50), Duration.ofMillis(10*50)) );
// load4 = Title.title( Component.text(""), Component.text("§fЗагрузка данных §7[§a|||||§7]"),  Title.Times.of(Duration.ZERO, Duration.ofMillis(5*50), Duration.ofMillis(10*50)) );
//  load5 = Title.title( Component.text(""), Component.text("§fЗагрузка данных §7[§a||||||§7]"),  Title.Times.of(Duration.ZERO, Duration.ofMillis(5*50), Duration.ofMillis(10*50)) );
//   load6 = Title.title( Component.text(""), Component.text("§fЗагрузка данных §7[§a|||||||§7]"),  Title.Times.of(Duration.ZERO, Duration.ofMillis(5*50), Duration.ofMillis(10*50)) );
        /*errorMsg = Component.text()
            .append(Component.text("§cНе удадось загрузить данные из локальной БД! Вы можете играть, но процесс не будет сохранён! §8клик-вернуться в лобби"))
            .hoverEvent(HoverEvent.showText(Component.text("§aклик на сообщение - вернуться в лобби")))
            .clickEvent(ClickEvent.runCommand("/server lobby0"))
            .build();*/
