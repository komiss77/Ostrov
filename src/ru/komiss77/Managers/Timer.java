
package ru.komiss77.Managers;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.ApiOstrov;

import ru.komiss77.Cfg;
import ru.komiss77.Enums.Action;
import ru.komiss77.Events.RestartWarningEvent;
import ru.komiss77.Objects.DelayActionBar;
import ru.komiss77.Objects.DelayBossBar;
import ru.komiss77.Objects.DelayTitle;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.Informator;
import ru.komiss77.modules.OstrovDB;
import ru.komiss77.utils.ntptime.NTPUDPClient;
import ru.komiss77.utils.ntptime.TimeInfo;




public class Timer {
    
    private static BukkitTask timer=null;
    private static BukkitTask timerAsync=null;
    private static int counter = 1; //начинаем с 1, чтобы сразу не срабатывало %x==0 

    private static boolean auto_restart = false;
    private static String restart_time;
    private static int rs = 0;

    private static boolean perms_autoupdate = false;
    private static int reloadPermIntervalSec = 0;

    private static ConcurrentHashMap <Integer, Integer> cd;
    public static Set <Integer> timer_keyset;
    public static ConcurrentHashMap <String, DelayActionBar> delay_actionbars;
    public static ConcurrentHashMap <String, DelayTitle> delay_titles;
    public static ConcurrentHashMap <String, DelayBossBar> delay_bossbars;

    private static int time_delta = 0;
    private static int currentTime = (int) (System.currentTimeMillis()/1000);

    
    public static void LoadVars() {
        auto_restart = Cfg.GetCongig().getBoolean("system.autorestart.use");
        int rstHour =  Cfg.GetCongig().getInt("system.autorestart.hour", 3);
        if (rstHour<0 || rstHour>23) rstHour = 3;
        int rstMin =  Cfg.GetCongig().getInt("system.autorestart.min", 30);
        if (rstMin<0 || rstMin>59) rstHour = 30;
        restart_time = (rstHour<=9?"0"+rstHour:""+rstHour) + ":" + (rstMin<=9?"0"+rstMin:""+rstMin);
        if (auto_restart) Ostrov.log_ok ("§6Установлено время авторестарта :"+restart_time);

        perms_autoupdate = Cfg.GetCongig().getBoolean("ostrov_database.auto_reload_permissions");
        reloadPermIntervalSec = Cfg.GetCongig().getInt("ostrov_database.auto_reload_permissions_interval_min")*60;
        if (reloadPermIntervalSec<10 || reloadPermIntervalSec > 10800) reloadPermIntervalSec = 600;
        if (perms_autoupdate) Ostrov.log_ok ("§5Автообновление прав каждые "+reloadPermIntervalSec/60+" мин.!!");

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    final NTPUDPClient timeClient = new NTPUDPClient();
                    final InetAddress inetAddress = InetAddress.getByName("ntp.ubuntu.com");
                    final TimeInfo timeInfo = timeClient.getTime(inetAddress);
                    final long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
                    time_delta=(int)(System.currentTimeMillis()-returnTime);
                    Ostrov.calendar.setTimeInMillis(System.currentTimeMillis() - time_delta); 
                    Ostrov.log_ok("время NTP:"+returnTime+", Время системное:"+System.currentTimeMillis()+", разница:"+time_delta);
                } catch (Exception ex) {
                    Ostrov.log_err("Не удалось получить NTP time "+ex.getMessage());
                }
            }
        }.runTaskAsynchronously(Ostrov.instance);

    }


    public static void init(final boolean authMode) {
        cd = new ConcurrentHashMap<>();
        delay_actionbars = new ConcurrentHashMap<>();
        delay_titles = new ConcurrentHashMap<>();
        delay_bossbars = new ConcurrentHashMap<>();
        timer_keyset = new HashSet<>();

        LoadVars();
        if (authMode) {
            startAuthMode();
        } else {
            start();
        }
    }

    public static void ReLoadVars() {
        LoadVars();
    }



    public static void start () {

        if (timer != null) timer.cancel();
        if (timerAsync != null) timerAsync.cancel();
        
        timer =  new BukkitRunnable() {

            //int i = rp_int*60;
            boolean to_restart = false;
            int time_left = 300;
            int server_update=0;

                @Override
                public void run() {

                    currentTime =  (int) ((System.currentTimeMillis()-time_delta)/1000);
                    Ostrov.calendar.setTimeInMillis(System.currentTimeMillis()-time_delta);
//System.out.println("currentTime="+currentTime+" dateFromStamp="+Ostrov.dateFromStamp(currentTime)+" hour_min="+Ostrov.getCurrentHourMin());
                    
                    if (auto_restart) {
//System.out.println("time_delta="+time_delta+" currentTime="+currentTime+" rs="+rs+" restart_time="+restart_time+" время="+Ostrov.getCurrentHourMin()+" equals?"+(restart_time.equals(Ostrov.getCurrentHourMin()) ));
                        if (rs == 60) {
                            rs=0;
                            if (restart_time.equals(Ostrov.getCurrentHourMin())) {
                                to_restart=true;
                                auto_restart = false;


                            }
                        }
                        rs+=1;
                    } 
                    if (to_restart) {
                        if (time_left==300) {
                            Bukkit.getPluginManager().callEvent(new RestartWarningEvent ( time_left ) );
                        }
                        if (time_left==300 || time_left==180 || time_left==120 || time_left==60) {
                            Bukkit.broadcastMessage("§cВНИМАНИЕ! §cПерезапуск сервера через "+time_left/60+" мин.!");
                        }
                        if (time_left==0) {
                            this.cancel();
                            Bukkit.shutdown();
                            return;
                        }
                        time_left-=1;
                    }



                    if (perms_autoupdate && !to_restart && counter%reloadPermIntervalSec==0) {
                        //try {
                            OstrovDB.loadGroups();
                       // } catch (Exception ex) {
                       //     Ostrov.log_err("Timer loadGroups : "+ex.getMessage());
                       // }
                    }


                    try {
                        if (!delay_actionbars.isEmpty()) {
                            delay_actionbars.values().stream().forEach((ab) -> {
                                ab.DoTick();
                            });
                        }
                        if (!delay_titles.isEmpty()) {
                            delay_titles.values().stream().forEach((ti) -> {
                                ti.DoTick();
                            });
                        }
                        if (!delay_bossbars.isEmpty()) {
                            delay_bossbars.values().stream().forEach((bb) -> {
                                bb.DoTick();
                            });
                        }
                    } catch (Exception ex) {
                        Ostrov.log_err("Timer delay action/title/bossbar : "+ex.getMessage());
                    }

                    //try {
                        timer_keyset.clear();
                        timer_keyset.addAll(cd.keySet());
                        timer_keyset.stream().forEach( (key) -> {
                            int sec_left = cd.get(key);
                            sec_left--;
                            if (sec_left<=0) {
                                cd.remove(key);
                            } else {
                                cd.put(key, sec_left);
                            }
                        });
                    //} catch (Exception ex) {
                    //    Ostrov.log_err("Timer timer_keyset : "+ex.getMessage());
                    //}
 //System.out.println("cd: "+cd);                    
//System.out.println("delay_actionbars : " +delay_actionbars.keySet());
//System.out.println("delay_titles : " +delay_titles);
//System.out.println("delay_bossbar : " +delay_bossbars);
                    //try {
                    //    PM.tickOplayers();
                   // } catch (Exception ex) {
                    //    Ostrov.log_err("Timer tickOplayers : "+ex.getMessage());
                   // }


                    
                    
                    //PM.getOplayers().stream().forEach( (op) -> {
                    Bukkit.getOnlinePlayers().stream().forEach( (p) -> {
                            PM.getOplayer(p).Tick_every_second(p, counter);
                        }
                    );
                    
                    //if (PM.ostrovStatScore && counter%10==0 && PM.hasOplayers()) {
                    //    ApiOstrov.sendMessage(Bukkit.getOnlinePlayers().stream().findAny().get(), Action.GET_BUNGEE_ONLINE, "");
                    //}
                    
                    //try {
                       // Informator.tick();
                    ////} catch (Exception ex) {
                    //    Ostrov.log_err("Timer Informator.tick : "+ex.getMessage());
                    //}

                    counter++;

                }}.runTaskTimer(Ostrov.instance, 20, 20);

                    
                    
                    
                    
                    
            timerAsync =  new BukkitRunnable() {
                @Override
                public void run() {


                    Bukkit.getOnlinePlayers().stream().forEach( (p) -> {
                            PM.getOplayer(p).tickAsync(p, counter);
                        }
                    );

                    if (counter%10==0 && PM.hasOplayers()) {
                        ApiOstrov.sendMessage(Action.GET_ONLINE);
                    }
                    
                    Informator.tick();
                    
                    SM.tickAsync(counter);
                    

                }}.runTaskTimerAsynchronously(Ostrov.instance, 21, 20);

        }



    public static void startAuthMode () {

        if (timer != null) timer.cancel();
        if (timerAsync != null) timerAsync.cancel();
        
        timer =  new BukkitRunnable() {

            //int i = rp_int*60;
            boolean to_restart = false;
            int time_left = 300;
            int server_update=0;

                @Override
                public void run() {

                    currentTime =  (int) ((System.currentTimeMillis()-time_delta)/1000);
                    Ostrov.calendar.setTimeInMillis(System.currentTimeMillis()-time_delta);
//System.out.println("currentTime="+currentTime+" dateFromStamp="+Ostrov.dateFromStamp(currentTime)+" hour_min="+Ostrov.getCurrentHourMin());
                    
                    if (auto_restart) {
//System.out.println("time_delta="+time_delta+" currentTime="+currentTime+" rs="+rs+" restart_time="+restart_time+" время="+Ostrov.getCurrentHourMin()+" equals?"+(restart_time.equals(Ostrov.getCurrentHourMin()) ));
                        if (rs == 60) {
                            rs=0;
                            if (restart_time.equals(Ostrov.getCurrentHourMin())) {
                                to_restart=true;
                                auto_restart = false;


                            }
                        }
                        rs+=1;
                    } 
                    if (to_restart) {
                        if (time_left==300) {
                            Bukkit.getPluginManager().callEvent(new RestartWarningEvent ( time_left ) );
                        }
                        if (time_left==300 || time_left==180 || time_left==120 || time_left==60) {
                            Bukkit.broadcastMessage("§cВНИМАНИЕ! §cПерезапуск сервера через "+time_left/60+" мин.!");
                        }
                        if (time_left==0) {
                            this.cancel();
                            Bukkit.shutdown();
                            return;
                        }
                        time_left-=1;
                    }

                        timer_keyset.clear();
                        timer_keyset.addAll(cd.keySet());
                        timer_keyset.stream().forEach( (key) -> {
                            int sec_left = cd.get(key);
                            sec_left--;
                            if (sec_left<=0) {
                                cd.remove(key);
                            } else {
                                cd.put(key, sec_left);
                            }
                        });


                    counter++;

                }}.runTaskTimer(Ostrov.instance, 20, 20);

                    
                    
                    
                    
                    
            timerAsync =  new BukkitRunnable() {
                @Override
                public void run() {
                    
                    SM.tickAsync(counter);

                }}.runTaskTimerAsynchronously(Ostrov.instance, 21, 20);

        }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Deprecated
    public static void CD_add ( final String nik, final String type, final int seconds ) {
        cd.put(nik.hashCode()^type.hashCode(), seconds);//cd.put(nik+"<>"+type, seconds);
    }
    @Deprecated
    public static void CD_del ( final String nik, final String type ) {
        if (cd.containsKey(nik.hashCode()^type.hashCode())) cd.remove(nik.hashCode()^type.hashCode());//if (cd.containsKey(nik+"<>"+type)) cd.remove(nik+"<>"+type);
    }
    @Deprecated
    public static boolean CD_has ( final String nik, final String type ) {
        return cd.containsKey(nik.hashCode()^type.hashCode());//return cd.containsKey(nik+"<>"+type);
    }
    @Deprecated
    public static int CD_left ( final String nik, final String type ) {
        if (cd.containsKey(nik.hashCode()^type.hashCode())) return cd.get(nik.hashCode()^type.hashCode());//if (cd.containsKey(nik+"<>"+type)) return cd.get(nik+"<>"+type);
        else return 0;
    }
    
    public static void add ( final Player p, final String type, final int seconds ) { //getEntityId - нельзя, после перезахода другой!!
//System.out.println("++++Timer.add() "+(p.getName().hashCode()^type.hashCode())+"    "+seconds);
        cd.put(p.getName().hashCode()^type.hashCode(), seconds);//cd.put(nik+"<>"+type, seconds);
    }
    public static void del ( final Player p, final String type ) {
        if (cd.containsKey(p.getName().hashCode()^type.hashCode())) cd.remove(p.getName().hashCode()^type.hashCode());//if (cd.containsKey(nik+"<>"+type)) cd.remove(nik+"<>"+type);
    }
    public static boolean has ( final Player p, final String type ) {
//System.out.println("++++Timer.has() "+(p.getName().hashCode()^type.hashCode())+"    ?"+(cd.containsKey(p.getName().hashCode()^type.hashCode())));
        return cd.containsKey(p.getName().hashCode()^type.hashCode());//return cd.containsKey(nik+"<>"+type);
    }
    public static int getLeft ( final Player p, final String type ) {
        if (cd.containsKey(p.getName().hashCode()^type.hashCode())) return cd.get(p.getName().hashCode()^type.hashCode());//if (cd.containsKey(nik+"<>"+type)) return cd.get(nik+"<>"+type);
        else return 0;
    }



    @Deprecated
    public static void add ( final int id, final int type, final int seconds ) {
        cd.put(id^type, seconds);//cd.put(nik+"<>"+type, seconds);
    }
    @Deprecated
    public static void del ( final int id, final int type ) {
        if (cd.containsKey(id^type)) cd.remove(id^type);//if (cd.containsKey(nik+"<>"+type)) cd.remove(nik+"<>"+type);
    }
    @Deprecated
    public static boolean has (  final int id, final int type ) {
        return cd.containsKey(id^type);//return cd.containsKey(nik+"<>"+type);
    }
    @Deprecated
    public static int getLeft (  final int id, final int type ) {
        if (cd.containsKey(id^type)) return cd.get(id^type);//if (cd.containsKey(nik+"<>"+type)) return cd.get(nik+"<>"+type);
        else return 0;
    }


    public static void add ( final int id, final int seconds ) {
        cd.put(id, seconds);//cd.put(nik+"<>"+type, seconds);
    }
    public static void del ( final int id ) {
        if (cd.containsKey(id)) cd.remove(id);//if (cd.containsKey(nik+"<>"+type)) cd.remove(nik+"<>"+type);
    }
    public static boolean has (  final int id ) {
        return cd.containsKey(id);//return cd.containsKey(nik+"<>"+type);
    }
    public static int getLeft (  final int id ) {
        if (cd.containsKey(id)) return cd.get(id);//if (cd.containsKey(nik+"<>"+type)) return cd.get(nik+"<>"+type);
        else return 0;
    }

    public static int getTime() {
        return currentTime;
    }















    
}
