package ru.komiss77;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
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
import ru.komiss77.events.RestartWarningEvent;
import ru.komiss77.listener.PlayerLst;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.Informator;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.modules.redis.RDS;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.games.GM;
import ru.komiss77.utils.TCUtils;




public class Timer {
    
    private static BukkitTask timer, playerTimer, timerAsync;
    private static int syncSecondCounter = 1; //начинаем с 1, чтобы сразу не срабатывало %x==0 

    public static boolean auto_restart,to_restart;
    private static int rstHour,rstMin;

    private static boolean perms_autoupdate, mission_tick, jailMode, authMode;
    private static int reloadPermIntervalSec;

    private static final ConcurrentHashMap <Integer, Integer> cd;

   // private static int time_delta = 0;
    private static int currentTime = (int) (System.currentTimeMillis()/1000);
    private static final int MIDNIGHT_STAMP;
    //private static final TextComponent errorMsg;
  //  private static final Title load0, load1, load2, load3, load4, load5, load6;

    private static final AtomicBoolean lockQuery = new AtomicBoolean(false);
    private static final AtomicBoolean lockSecond = new AtomicBoolean(false);
    //private static OstrovDB.Qinfo qInfo;
    private static final Map <Integer, OstrovDB.Qinfo> map;
    private static int count;
    
    static {
        cd = new ConcurrentHashMap<>();
        Ostrov.calendar.setTimeInMillis(System.currentTimeMillis());
        Ostrov.calendar.set(Calendar.DAY_OF_YEAR, Ostrov.calendar.get(Calendar.DAY_OF_YEAR)+1);
        Ostrov.calendar.set(Calendar.HOUR_OF_DAY, 0);
        Ostrov.calendar.set(Calendar.MINUTE, 0);
        Ostrov.calendar.set(Calendar.SECOND, 0);
        MIDNIGHT_STAMP = (int) (Ostrov.calendar.getTimeInMillis()/1000);
        map = new HashMap<>();
    }
            
    public static void init() {

        auto_restart = Config.getConfig().getBoolean("system.autorestart.use");
        rstHour =  Config.getConfig().getInt("system.autorestart.hour", 3);
        if (rstHour<0 || rstHour>23) rstHour = 3;
        rstMin =  Config.getConfig().getInt("system.autorestart.min", 30);
        if (rstMin<0 || rstMin>59) rstHour = 30;
        //restart_time = (rstHour<=9?"0"+rstHour:""+rstHour) + ":" + (rstMin<=9?"0"+rstMin:""+rstMin);
        if (auto_restart) Ostrov.log_ok ("§6Установлено время авторестарта :"+rstHour+":"+rstMin);

        perms_autoupdate = Config.getConfig().getBoolean("ostrov_database.auto_reload_permissions");
        reloadPermIntervalSec = Config.getConfig().getInt("ostrov_database.auto_reload_permissions_interval_min")*60;
        if (reloadPermIntervalSec<10 || reloadPermIntervalSec > 10800) reloadPermIntervalSec = 600;
        if (perms_autoupdate) Ostrov.log_ok ("§5Автообновление прав с интервалом "+ApiOstrov.secondToTime(reloadPermIntervalSec));
        
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
        if (timerAsync != null) timerAsync.cancel();
        
        if (Ostrov.MOT_D.length()==3) { //pay, авторизация 
            authMode = true; //startAuthMode();
        } else if (Ostrov.MOT_D.equals("jail")) { //jail 
            jailMode = true;
        } else {
            mission_tick = true;
        }
        start();
        
        timer =  new BukkitRunnable() {

            int time_left = 300;

                @Override
                public void run() {

                  //currentTime =  (int) ((System.currentTimeMillis()-time_delta)/1000);
                  currentTime =  (int) ((System.currentTimeMillis())/1000);
                  //Ostrov.calendar.setTimeInMillis(System.currentTimeMillis()-time_delta);
                  Ostrov.calendar.setTimeInMillis(System.currentTimeMillis());

                    if (auto_restart && syncSecondCounter%60==0 ) {
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
                        if (time_left==15) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                PlayerLst.PREPARE_RESTART = true;
                                p.kick(Component.text("§eСервер перезагружается."), PlayerKickEvent.Cause.PLUGIN);
                            }
                        }
                        if (time_left==0) {
                            this.cancel();
                            Ostrov.SHUT_DOWN = true;
                            Bukkit.shutdown();
                            return;
                        }
                        time_left-=1;
                    }
                    
                    if (lockSecond.compareAndSet(false, true)) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                asyncSecondJob(syncSecondCounter);
                            }
                        }.runTaskAsynchronously(Ostrov.instance);
                    }
                    
                    syncSecondCounter++;


                }}.runTaskTimer(Ostrov.instance, 20, 20);        
        
        
    }

  





    
    
    
    private static void start () {

        //обход игроков каждую секунду с разбросом по тикам для распределения нагрузки
        playerTimer = new BukkitRunnable() {
            int banLeft;
            int syncTick;
            
            @Override
            public void run() {

                cd.entrySet().removeIf(entry -> entry.getValue() <= currentTime);//чтобы точнее ловить если надо меньше секунды
                
                //отправить запросы в БД острова
                if (OstrovDB.useOstrovData && OstrovDB.ready && !OstrovDB.QUERY.isEmpty()) {
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
                    PM.getOplayers().stream().forEach( (op) -> {
                            op.tick++;
                            if (op.tick==20) {
                                op.tick = 0;
                                op.secondTick();
                                if (jailMode && !op.isStaff) {
    //op.getPlayer().sendMessage("BAN_TO="+op.getDataInt(Data.BAN_TO));
                                    banLeft = op.getDataInt(Data.BAN_TO)-getTime();
                                    if (banLeft<=0) {
                                        ApiOstrov.sendToServer(op.getPlayer(), "lobby0", "");
                                    } else {
                                        op.score.getSideBar().setTitle("§4Чистилище");
                                        op.score.getSideBar().updateLine(9, "§7До разбана:");
                                        op.score.getSideBar().updateLine(8, "§e"+ApiOstrov.secondToTime(banLeft));
                                    }
                                }
                            }
                        }
                    );
                }

                syncTick++;

                    
        }}.runTaskTimer(Ostrov.instance, 20, 1);

    }


    
    private static void asyncSecondJob(final int second) {
        
       // if (asyncTick % 20==0) {
            
            if (!authMode) {
                if (second%11==0 && PM.hasOplayers()) {  //11*20
                    SpigotChanellMsg.sendMessage(Bukkit.getOnlinePlayers().stream().findAny().get(), Operation.GET_ONLINE);
                }
                Informator.tickAsync();
            }

            RDS.heartbeats();
            if (second%43==0) {
              GM.getGames().stream().forEach( (gi -> {
                gi.arenas().stream().filter( ai-> ai.server == Ostrov.MOT_D).forEach(ai -> ai.sendData());
              }));
            }

            if (OstrovDB.useOstrovData ) {//if (OstrovDB.useOstrovData && OstrovDB.connection!=null) {-не поставт флаг ostrovDbErrors!

                if (second%15==0 ) {   //checkOstrovDBConnection(asyncSecondCounter);
                    try {
                        final Connection conn = OstrovDB.getConnection();
                        if (conn == null || conn.isClosed() || !conn.isValid(1)) { //(!OstrovDB.ready) {
                            Ostrov.log_warn("Timer - восстанавливаем соединение с Ostrov DB...");
                            OstrovDB.connect();
                        }
                    } catch (SQLException ex) {
                        Ostrov.log_warn("Timer - проверка соединения с Ostrov DB : "+ex.getMessage());
                    }
                }
                
                if (OstrovDB.ready) {

                    if (Ostrov.server_id>0 && second%10==0 ) { //нашел себя в таблице - писать состояние каждые 10 сек
                        OstrovDB.writeThisServerStateToOstrovDB();
                    }

                    if (!authMode) {
                        
                        if (perms_autoupdate && !to_restart && second%reloadPermIntervalSec==0) {
                            Perm.loadGroupsDB(false);
                        }

                        //если игры еще не пытались грузиться, fromStamp = 0
                        //если игры уже прогрузились, fromStamp > 0
                        //если была ошибка при первой загрузке, fromStamp = -1
                      if (GM.state!= GM.State.STARTUP && second%GM.LOAD_INTERVAL==0) {//if (GM.fromStamp!=0 && second%GM.LOAD_INTERVAL==0) {
                            GM.loadArenaInfo(); //там же подгрузит обновы Lang
                        }
                        
                        if (mission_tick) {
                            if (second%63==0 || second==10) { //на 10 секунде после старта и каждую минуту
                                MissionManager.loadMissions();
                            }
                            MissionManager.tickAsync();
                        }
                        
                    }
                }

            }

           
            if (LocalDB.useLocalData && second%14==0 ) {  //checkLocalDBConnection(asyncSecondCounter); 
                try {
                    if (LocalDB.connection==null || LocalDB.connection.isClosed() || !LocalDB.connection.isValid(1)) {
                       Ostrov.log_warn("Timer - восстанавливаем соединение с Local DB...");
                       LocalDB.connect();
                    }
                } catch (SQLException ex) {
                    Ostrov.log_err("Timer - соединение с Local DB восстановить не удалось!");
                }                        

            }
            //asyncSecond++;

       // }

       /* if (OstrovDB.useOstrovData && OstrovDB.ready && !OstrovDB.QUERY.isEmpty()) {
//final long l = System.currentTimeMillis();

            try (Statement stmt = OstrovDB.getConnection().createStatement()) {
                while ((qInfo = OstrovDB.QUERY.poll()) != null) {
                    stmt.addBatch(qInfo.query);
                    map.put(count, qInfo);
                    count++;
                }
                stmt.executeBatch();

            } catch (BatchUpdateException  ex) {
//Ostrov.log_warn("BatchUpdateException!! "+ex.getMessage());
                int[] batchArray = ex.getUpdateCounts();
                count = 0;
                for (int batchResult : batchArray) {
//Ostrov.log_warn("count="+count+" batchResult="+batchResult);
                    if (batchResult == Statement.EXECUTE_FAILED) {
                        qInfo = map.get(count);
                        cs = qInfo.senderName==null ? null : qInfo.senderName.equals("console") ? Bukkit.getConsoleSender() : Bukkit.getPlayerExact(qInfo.senderName);
                        if (cs!=null) cs.sendMessage("§cОшибка выполнения запроса "+qInfo.query);
                        Ostrov.log_err("Timer querry executePstAsync "+qInfo.query+" : "+ex.getMessage());
                    } else {
                        //запрос выполнен, batchResult=колл-во изменённых строк
                    }
                    count++;
                }
            } catch (SQLException ex) {
                Ostrov.log_err("Timer querry executePstAsync : "+ex.getMessage());
            }
            
            count=0;
            map.clear();
//Ostrov.log_ok("POLL time:"+(System.currentTimeMillis()-l)+"мс.");
        }*/
        
        //asyncTick++;
        lockSecond.set(false);//lock = false;

//Ostrov.log("tick="+asyncTick+" asyncSecondCounter="+asyncSecond);
       // lock.set(false);//lock = false;

    }
    
    
    

    private static void sendQuery() {
//final long l = System.currentTimeMillis();
      Statement stmt = null;
      OstrovDB.Qinfo qInfo;
      try {
        stmt = OstrovDB.getConnection().createStatement();

        while ( (qInfo = OstrovDB.QUERY.poll() ) != null) {

          try {

            stmt.execute(qInfo.query);
//Ostrov.log_warn("execute "+qInfo.query);

          } catch (SQLException | NullPointerException ex) {

            CommandSender cs = qInfo.senderName==null ? null : qInfo.senderName.equals("console") ? Bukkit.getConsoleSender() : Bukkit.getPlayerExact(qInfo.senderName);
            if (cs!=null) cs.sendMessage("§cОшибка выполнения запроса "+qInfo.query+" : "+ex.getMessage());
            Ostrov.log_err("Timer executeQuery "+qInfo.query+" : "+ex.getMessage());

          }
        }

      } catch (SQLException ex) {

        Ostrov.log_err("Timer sendQuery createStatement : "+ex.getMessage());

      } finally {
        try {
          if (stmt!=null && !stmt.isClosed()) {
            stmt.close();
          }
        } catch (SQLException ex) {
          Ostrov.log_err("Timer sendQuery stmt.close : "+ex.getMessage());
        }
        lockQuery.set(false);//lock = false;
      }


      /*try (Statement stmt = OstrovDB.getConnection().createStatement()) {
            while ((qInfo = OstrovDB.QUERY.poll()) != null) {
                stmt.addBatch(qInfo.query);
                map.put(count, qInfo);
                count++;
            }
            stmt.executeBatch();

        } catch (BatchUpdateException  ex) {
//Ostrov.log_warn("BatchUpdateException!! "+ex.getMessage());
            int[] batchArray = ex.getUpdateCounts();
            count = 0;
            for (int batchResult : batchArray) {
//Ostrov.log_warn("count="+count+" batchResult="+batchResult);
                if (batchResult == Statement.EXECUTE_FAILED) {
                    qInfo = map.get(count);
                    cs = qInfo.senderName==null ? null : qInfo.senderName.equals("console") ? Bukkit.getConsoleSender() : Bukkit.getPlayerExact(qInfo.senderName);
                    if (cs!=null) cs.sendMessage("§cОшибка выполнения запроса "+qInfo.query);
                    Ostrov.log_err("Timer querry executePstAsync "+qInfo.query+" : "+ex.getMessage());
                } else {
                    //запрос выполнен, batchResult=колл-во изменённых строк
                }
                count++;
            }
        } catch (SQLException ex) {
            Ostrov.log_err("Timer querry executePstAsync : "+ex.getMessage());
        }
        count=0;
        map.clear();*/

//Ostrov.log_ok("POLL time:"+(System.currentTimeMillis()-l)+"мс.");

    }
    
     
    
    
    public static void add ( final String name, final String type, final int seconds ) { //getEntityId - нельзя, после перезахода другой!!
        if (seconds<=0) return;
        cd.put(name.hashCode()^type.hashCode(), currentTime + seconds);
    }    
    public static void del ( final String name, final String type ) {
        cd.remove(name.hashCode()^type.hashCode());
    }
    public static boolean has ( final String name, final String type ) {
        return cd.containsKey(name.hashCode()^type.hashCode());
    }
    public static int getLeft (final String name, final String type ) {
        int left = cd.getOrDefault(name.hashCode()^type.hashCode(), 0);
        return left==0 ? 0 : left - Timer.getTime();
    } 
    
    
    
    public static void add ( final Player p, final String type, final int seconds ) { //getEntityId - нельзя, после перезахода другой!!
        if (seconds<=0) return;
        cd.put(p.getName().hashCode()^type.hashCode(), currentTime + seconds);
    }
    public static void del ( final Player p, final String type ) {
        cd.remove(p.getName().hashCode()^type.hashCode());
    }
    public static boolean has ( final Player p, final String type ) {
        return cd.containsKey(p.getName().hashCode()^type.hashCode());
    }
    public static int getLeft ( final Player p, final String type ) {
        int left = cd.getOrDefault(p.getName().hashCode()^type.hashCode(), 0);
        return left==0 ? 0 : left - Timer.getTime();
    }

    public static void add ( final int id, final int seconds ) {
        if (seconds<=0) return;
        cd.put(id, currentTime + seconds);
    }
    public static void del ( final int id ) {
        cd.remove(id);
    }
    public static boolean has (  final int id ) {
        return cd.containsKey(id);
    }
    public static int getLeft (  final int id ) {
        int left = cd.getOrDefault(id, 0);
        return left==0 ? 0 : left - Timer.getTime();
    }

    
    
    public static int getTime() {
        return currentTime;
    }

    public static int leftBeforeResetDayly() {
        return MIDNIGHT_STAMP - currentTime;
    }

    public static long getTimeStamp() {
      //return System.currentTimeMillis() - time_delta;
      return System.currentTimeMillis();
    }















    
}

 


















    
/*
    private static void startAuthMode () {
          
                    
            timerAsync =  new BukkitRunnable() {
                int asyncSecondCounter;
                
                @Override
                public void run() {
                    
                    //checkOstrovDBConnection(asyncSecondCounter);
                        
                    if (OstrovDB.useOstrovData ) {//if (OstrovDB.useOstrovData && OstrovDB.connection!=null) {-не поставт флаг ostrovDbErrors!

                        if (Ostrov.server_id>0 && asyncSecondCounter%10==0 ) { //нашел себя в таблице - писать состояние каждые 10 сек
                            OstrovDB.writeThisServerStateToOstrovDB();
                        }

                    }

                    //checkLocalDBConnection(asyncSecondCounter);
                    
                }


            }.runTaskTimerAsynchronously(Ostrov.instance, 21, 20);

        }*/
    
    
    
    
    

  /*  public static  void checkOstrovDBConnection() {
//Ostrov.log("checkOstrovDBConnection useOstrovData="+OstrovDB.useOstrovData+" asyncSecondCounter="+asyncSecondCounter);
       // if (asyncSecondCounter%55==0 ) {

//Ostrov.log("ostrovDbErrors="+ostrovDbErrors);
            // try {
                 //if (OstrovDB.errors>=10 || OstrovDB.connection==null || OstrovDB.connection.isClosed() || !OstrovDB.connection.isValid(3)) {
                 if (!OstrovDB.ready) {
                    //ostrovDbErrors = false;
                    Ostrov.log_warn("Timer - восстанавливаем соединение с Ostrov DB...");
                    OstrovDB.connect();
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
                            if (OstrovDB.useOstrovData) {
                                OstrovDB.Disconnect();
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
