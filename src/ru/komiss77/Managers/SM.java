package ru.komiss77.Managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Enums.Action;
import ru.komiss77.Enums.Table;
import ru.komiss77.Enums.UniversalArenaState;
import ru.komiss77.Events.SignUpdateEvent;
import ru.komiss77.Initiable;
import ru.komiss77.Objects.Arena;
import ru.komiss77.Objects.CaseInsensitiveMap;
import ru.komiss77.Objects.S_info;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.OstrovDB;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.version.VM;



//+++переход по достижению уровня или пермишен!!
//+предмет "назад"



public final class SM extends Initiable implements Listener {   //не переименовывать!!!! другие плагины берут напрямую!
    
    public static Inventory main_inv;
    private static CaseInsensitiveMap<S_info> servers; //раздел+пункт гл.меню  
    public static Set<String> allBungeeServersName;
    private static BukkitTask load_timer, tick_timer;
    public static String main_inv_name="§2Сервера";
    public static String this_server_name = Bukkit.getMotd();
    public static String this_server_bungee_description = Bukkit.getMotd(); //потом получает из таблицы
    public static String this_server_bungee_logo = "S"; //потом получает из таблицы
    public static S_type this_server_type = S_type.MULTIPLE;
    private static int load_interval=20;
    private static int last_check=Timer.currentTimeSec();
    public static int bungee_online=0;
    public static boolean send_data, write_server_state_to_bungee_table;

    public SM() {
        servers=new CaseInsensitiveMap<>();
        allBungeeServersName=new HashSet<>();
        main_inv=Bukkit.createInventory(null, 54, main_inv_name);
        reload();
    }
    

    
    
    @Override
    public void reload() {
        
        HandlerList.unregisterAll(this);
        main_inv.clear();
        servers.clear();
        allBungeeServersName.clear();
        if (load_timer!=null) load_timer.cancel();
        if (tick_timer!=null) tick_timer.cancel();
        
        try {
            getBungeeServerInfo();
            if (!Cfg.GetCongig().getBoolean("ostrov_database.games_info_for_server_menu_load")) {
                Ostrov.log_warn("§eЗагрузка данных серверов выключена.");
                return;
            }
            load_interval = Cfg.GetCongig().getInt("ostrov_database.games_info_for_server_menu_load_interval_ticks");
            if (load_interval<5) load_interval=5; else if (load_interval>200) load_interval=200;
            write_server_state_to_bungee_table = Cfg.GetCongig().getBoolean("ostrov_database.write_server_state_to_bungee_table");
            send_data = Cfg.GetCongig().getBoolean("ostrov_database.games_info_for_server_menu_send");
            Bukkit.getPluginManager().registerEvents(this, Ostrov.GetInstance());
            loadServersAndArenas();
            main_inv.setItem(53, new ItemBuilder(Material.BARRIER).setName("§5Закрыть").build());
            Ostrov.log_ok ("§2Меню серверов загружено!");
        } catch (Exception ex) { 
            Ostrov.log_err("§4Не удалось загрузить настройки Меню серверов: "+ex.getMessage());
        }
    }

    
    //public void Reload () {
        //HandlerList.unregisterAll(this);
        //main_inv.clear();
        //servers.clear();
        //if (load_timer!=null) load_timer.cancel();
        //if (tick_timer!=null) tick_timer.cancel();
        //Load();
    //}
    
    




    
    public static CaseInsensitiveMap<S_info> getServers() {
        return servers;
    }
    
    public static Collection<String> getTypes() {
        return servers.keySet();
    }
    
    public static Inventory getTypeInventory(final String type) {
        return servers.get(type).arena_inv;
    }
    
    public static S_info getS_info(final String type) {
        return servers.get(type);
    }
    
    public static Collection<Arena> getArenas(final String gameType) {
        if (servers.containsKey(gameType)) return servers.get(gameType).getArenas();
        else return new ArrayList<>();
    }
    
    public static List<String> getArenasNames(final String gameType) {
//System.out.println("getArenasNames "+gameType);
        final List<String> list = new ArrayList<>();
        for (final S_info si : servers.values()) {
            if (si.server.equals(gameType)) {
//System.out.println("si server="+si.server+" arenas="+ApiOstrov.listToString(si.getArenas()," "));
                for (final Arena a:si.getArenas()) {
                    list.add(a.arena_name);
                }
            }
        }
        return list;
    }
    


    
    
    
    
    public static void writeThisServerStateToOstrovDB() {  //вызывается из Timer каждые 5 сек. если write_server_state_to_bungee_table=true
        if (!OstrovDB.useOstrovData) return;
        new BukkitRunnable(){
            @Override     
            public void run() {

            PreparedStatement pst = null;
            try {
//System.out.println("query="+"UPDATE  SET `online`='"+Bukkit.getOnlinePlayers().size()+"',`tps`='"+(int) MinecraftServer.getServer().recentTps[0]+"',`memory`='"+(int)(Runtime.getRuntime().maxMemory()/1024/1024 )+"',`memory_max`='"+(int)(Runtime.getRuntime().totalMemory()/1024/1024)+"',`stamp`='"+Main.Единое_время()+"' WHERE UPPER `id`='"+Main.id+"' ");                
                pst = ApiOstrov.getOstrovConnection().prepareStatement("UPDATE "+Table.BUNGEE_SERVERS.table_name+" SET "
                        + "`online`='"+Bukkit.getOnlinePlayers().size()+"',"
                        + "`online_max`='"+Bukkit.getMaxPlayers()+"',"
                        //+ "`tps`='"+NmsUtils.getTps()+"',"
                        + "`tps`='"+VM.getNmsServer().getTps()+"',"
                        + "`memory`='"+(int)(Runtime.getRuntime().totalMemory()/1024/1024 )+"',"
                        + "`memory_max`='"+(int)(Runtime.getRuntime().maxMemory()/1024/1024)+"',"
                        + "`stamp`='"+Timer.currentTimeSec()+"'"
                        + " WHERE `id`='"+Ostrov.server_id+"' ");
                pst.executeUpdate();

            } catch (SQLException ex) {
                Ostrov.log_err("§c updServerState err ex="+ex.getMessage());
                //e.printStackTrace();
            }finally {
                try{
                    if (pst!=null) pst.close();
                } catch (SQLException ex) {
                    Ostrov.log_err("§c updServerState close err ex="+ex.getMessage());
                }
            }
        }}.runTaskAsynchronously( Ostrov.instance ); 
        
    }

    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static void sendArenaData (
            String arena_name,
            String line0,
            String line1,
            String line2,
            String line3,
            String extra,
            final int players,
            final UniversalArenaState state,
            boolean mysql,
            final boolean async
        ){
//System.out.println("--Send_data arena:"+arena_name);  

        if (arena_name.isEmpty()) arena_name="any"; 
        else {
            if (arena_name.length()>24) arena_name=arena_name.substring(0,24);
        }
        final String serv_arena=this_server_name+"<:>"+arena_name;

        if (line0.length()>24)line0=line0.substring(0,24);
        if (line1.length()>24)line1=line1.substring(0,24);
        if (line2.length()>24)line2=line2.substring(0,24);
        if (line3.length()>24)line3=line3.substring(0,24);
        if (extra.length()>256)extra=extra.substring(0,256);
        //if (state==null) state = UniversalArenaState.НЕОПРЕДЕЛЕНО;
        
        
        final String mysqlRaw = line0+"<:>"+line1+"<:>"+line2+"<:>"+line3+"<:>"+extra+"<:>";
        
        if (this_server_name.length()==4) {  //на миниигре вызываем локальные эвент для табличек этого сервера! (с банжи не получит)
            final String a_name = arena_name;
            new BukkitRunnable() {
                @Override
                public void run() {
                    final String[]split = mysqlRaw.split("<:>");
                    Bukkit.getPluginManager().callEvent(new SignUpdateEvent(
                            this_server_name,
                            a_name,
                            (split.length==0?"":split[0]), //защита от пустых данных
                            (split.length<=1?"":split[1]),
                            (split.length<=2?"":split[2]),
                            (split.length<=3?"":split[3]),
                            (split.length<=4?"":split[4]),
                            state
                        )
                    );
                }
            }.runTask(Ostrov.GetInstance());
        }
        
        if (!mysql && Bukkit.getOnlinePlayers().isEmpty()) mysql=true;
        
        if (mysql) {
           if (async) {
                new BukkitRunnable(){ 
                @Override
                public void run() {
                    writeArenaStateToMySql(serv_arena, players, mysqlRaw, state );
                }}.runTaskAsynchronously( Ostrov.instance );  
            } else {
                writeArenaStateToMySql(serv_arena, players, mysqlRaw, state );
            }
        } else {
            //не использовать mysqlRaw, получается два раза <:><:> в середине!
            ApiOstrov.sendMessage(serv_arena, Action.ARENA_INFO_FROM_GAME, line0+"<:>"+line1+"<:>"+line2+"<:>"+line3+"<:>"+extra+"<:>"+players+"<:>"+state.toString() );
                    
        }
    } 

    
    
    
    
    
  

    private static void writeArenaStateToMySql (final String serv_arena, final int players, final String raw, final UniversalArenaState state ) {
        if (!OstrovDB.useOstrovData) return;
//System.out.println("bsign.spigot.Bsign.write()");        
        try ( 
                
            PreparedStatement prepStmt = ApiOstrov.getOstrovConnection().prepareStatement("INSERT INTO "+ApiOstrov.getArenasTabbleName()+" ( "
                    + "`арена`, "
                    + "`состояние`, "
                    + "`игроки`, "
                    + "`данные`, "
                    + "`штамп` ) VALUES "
                    + " ( ?, ?, ?, ?, ? ) "+
                    "ON DUPLICATE KEY UPDATE "
                    + "состояние=VALUES(состояние), "
                    + "игроки=VALUES(игроки), "
                    + "данные=VALUES(данные), "
                    + "штамп=VALUES(штамп)" )
            )
        {
            prepStmt.setString(1, serv_arena);
            prepStmt.setString(2, state.toString());
            prepStmt.setInt(3, players);
            prepStmt.setString(4, raw);
            prepStmt.setInt(5, Timer.currentTimeSec()+1 ); //для  надёжности, пусть прогрузит 2 раза

            prepStmt.executeUpdate();
            prepStmt.close();



        } catch (SQLException e) {
            Ostrov.log_err("§writeArenaStateToMySql error - "+e.getMessage());
            //e.printStackTrace();
        }
    }
  
    
    
    
    
    
    
    
    


    
    public static void ArenaInfoFromBungeeHandler (final String raw) {
//System.out.println("-- ArenaInfoFromBungeeHandler  "+" "+raw);  
        //        0         1      2       3      4        5       6        7       8       
        //raw = server<:>arena<:>line0<:>line1<:>line2<:>line3<:>extra<:>players<:>state.toString()
        String[]split=raw.split("<:>");
        if (split.length!=9 || !Ostrov.isInteger(split[7]) ) {
            Ostrov.log_err("ArenaInfoFromBungeeHandler: Формат raw data неверен : "+raw);
            return;
        }
        //final UniversalArenaState state = UniversalArenaState.fromString(split[8]);
//System.out.println("--2  state="+state);     

        for (String type:servers.keySet()) {
            //if (serv_arena.startsWith(servers.get(type).server)) {
            if (split[0].startsWith(servers.get(type).server)) {

                servers.get(type).updateArena(
                    //serv_arena.split("<:>")[0],
                    split[0],
                    //serv_arena.split("<:>")[1],
                    split[1],
                    UniversalArenaState.fromString(split[8]),
                    Integer.valueOf(split[7]),
                    split[2]+"<:>"+split[3]+"<:>"+split[4]+"<:>"+split[5]+"<:>"+split[6]+"<:>"
                );
                break;
            }
        }
    }
    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    



    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public static void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().getType()!=InventoryType.CHEST  || e.getSlot() <0 || e.getSlot() > 53 ) return;
        if (!e.getView().getTitle().startsWith(main_inv_name)) return;
        e.setCancelled(true);
//System.out.println("1 Click "+main_inv_name);
        if (e.getCurrentItem()==null || e.getCurrentItem().getType()==Material.AIR) return;
        
        Player p = (Player) e.getWhoClicked();
        
        if (Timer.CD_has(p.getName(), "sm")) {
            return;
        }
        Timer.CD_add(p.getName(), "sm", 1);
        
        String type=e.getView().getTitle().replaceFirst("§2Сервера", "").trim();
//System.out.println("2 type="+type);     

        if (type.isEmpty()) { //гл.меню
            
            if (e.getSlot()==53) {
                p.closeInventory();
                return;
            }
            
            servers.values().stream().forEach((si) -> {
                if (si.position==e.getSlot()) {
                    
                    if (si.type==S_type.SINGLE) {
                        p.closeInventory();
                        ApiOstrov.sendToServer(p, si.server, "");
//System.out.println("to server "+si.server); 

                    } else {
//System.out.println("sinfo="+si.server); 
                        
                        if(e.isLeftClick()) {
                            
                            if (Ostrov.getWarpManager().exist(si.server)) {
                                p.closeInventory();
                                ApiOstrov.teleportSave(p, Ostrov.getWarpManager().getWarp(si.server).loc, false);
                                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 2);
                            } else {
//System.out.println("openInventory "+si.server);
                                Ostrov.sync( () -> p.openInventory(si.arena_inv), 2 );
                                //p.openInventory(si.arena_inv);
                                p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1, 2);
                            }
                            
                        } else {
                            
                            p.openInventory(si.arena_inv);
                            p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1, 2);
                            
                        }
                        
                    }
                }
            });
            
        } else { //меню арен - тп на арену
            if (servers.containsKey(type)) {
                servers.get(type).invClick(p, e.getSlot());
            }
        }

    }





    private static void startSinfoTimers() {   //запускается после загрузки в loadServersAndArenas
        
     /*   tick_timer=new BukkitRunnable() {
            @Override
            public void run() {
//Bukkit.broadcastMessage("last_check="+last_check/1000);
                
                if (Bukkit.getOnlinePlayers().size()>0) {
                    servers.values().stream().forEach((si) -> {
                        si.do_Tick();
                    });
                }
                
            }
        }.runTaskTimerAsynchronously(Ostrov.instance, 11, 11);*/

        
        load_timer=new BukkitRunnable() {
            @Override
            public void run() {
//Bukkit.broadcastMessage("last_check="+last_check/1000);
                
                if (OstrovDB.useOstrovData && Bukkit.getOnlinePlayers().size()>0) {
                    Statement stmt=null;
                    ResultSet rs = null;
                    try {
                            stmt = ApiOstrov.getOstrovConnection().createStatement(); 
                            //S_info s_info;
//System.out.println(" SELECT `сервер`,`игроки`  FROM "+Table.GAMES_MAIN.table_name+" WHERE `тип` LIKE '"+S_type.SINGLE.toString()+"' AND `штамп` > "+last_check);
                            rs = stmt.executeQuery( " SELECT `сервер`,`игроки`  FROM "+Table.GAMES_MAIN.table_name+" WHERE `тип` LIKE '"+S_type.SINGLE.toString()+"' AND `штамп` > "+last_check ); 
                                while (rs.next()) {
//System.out.println("rs.next servers.containsKey?"+servers.containsKey(rs.getString("сервер")));                                    
                                    if (servers.containsKey(rs.getString("сервер"))) {
                                        servers.get(rs.getString("сервер")).updatePlayerCount(rs.getInt("игроки"));
                                    }
                                }
                            rs.close();

                            rs = stmt.executeQuery( " SELECT *  FROM "+Table.GAMES_ARENAS.table_name+" WHERE  `штамп` > "+last_check ); 
                            
                            String arena_name;
                            String server;
                                while (rs.next()) {
//Bukkit.broadcastMessage("SELECT "+rs.getString("арена"));                                    
                                    for (String si:servers.keySet()) {
                                        if (rs.getString("арена").startsWith(servers.get(si).server)) {
                                            
                                            server=rs.getString("арена").split("<:>")[0];
                                            arena_name=rs.getString("арена").split("<:>")[1];
                                            if (arena_name.isEmpty()) arena_name="any";
                                        
                                            servers.get(si).updateArena(
                                                server,
                                                arena_name,
                                                UniversalArenaState.fromString(rs.getString("состояние")),
                                                rs.getInt("игроки"),
                                                rs.getString("данные")
                                            );
                                            break;
                                        }
                                    }

                                }

                            rs.close();
                            stmt.close();
                            
                            last_check=Timer.currentTimeSec();
                            
                            servers.values().stream().forEach((si) -> {
                                si.update();
                            });
                            //main_inv.getViewers().stream().forEach( p -> {((Player)p).updateInventory();} );
                        //Ostrov.log_ok("§2Данные серверов загружена! Запуск таймера.");

                    } catch (SQLException ex) { 
                        Ostrov.log_err("§4Не удалось загрузить данные серверов! update_sinfo "+ex.getMessage());
                    } finally {
                        try {
                            if (rs!=null) rs.close();
                            if (stmt!=null) stmt.close();
                        } catch (SQLException ex) {
                            Ostrov.log_err("§4Не удалось закрыть соединение! update_sinfo "+ex.getMessage());
                        }
                    }
                
                }
                
            }
        }.runTaskTimerAsynchronously(Ostrov.instance, 20, load_interval);
     }

    

    
    //public void on_shut_down() {
    //     if (send_data) write(-1);
    //}
    
    public void on_start() {
         if (send_data) write(0);
    }

    @EventHandler(priority = EventPriority.MONITOR)    
    public static void join (PlayerJoinEvent e) {
        if (send_data) {
            write(PM.getOnlineCount());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)    
    public static void leave (PlayerQuitEvent e) {
        if (send_data) {
            write(PM.getOnlineCount()-1); //Oplayer удаляется позже как монитор
        }
    }
    
    

    
    //только для SINGLE
    private static void write (final int count ) {
        if (!OstrovDB.useOstrovData) return;
        Ostrov.async(()-> {
            //if (ApiOstrov.getOstrovConnection()== null) return;
    //System.out.println(" --- write online="+count+" type = "+this_server_type);
            if (this_server_type==S_type.SINGLE) { //не пишем в GAMES_MAIN, там не будет сервера с таким именем (MULTIPLE - две буквы от названия игры) !

                try ( 
                    PreparedStatement prepStmt = ApiOstrov.getOstrovConnection().prepareStatement("UPDATE "+Table.GAMES_MAIN.table_name+" SET `игроки` = '"+count+"',`штамп` = '"+Timer.currentTimeSec()+"' WHERE "+Table.GAMES_MAIN.table_name+".`сервер` LIKE '"+this_server_name+"' " )
                    )
                {
                    prepStmt.executeUpdate();
                    prepStmt.close();

                } catch (SQLException e) {
                    Ostrov.log_err("§сSign write - "+e.getMessage());
                    //e.printStackTrace();
                }

            } else if (this_server_name.startsWith("lobby")) { //для лобби!
                writeArenaStateToMySql(this_server_name+"<:>any", count, this_server_bungee_description, UniversalArenaState.РАБОТАЕТ);
               /* try ( 
                    PreparedStatement prepStmt = ApiOstrov.getOstrovConnection().prepareStatement("INSERT INTO "+Table.GAMES_ARENAS.table_name+" ( "
                            + "`арена`, "
                            + "`состояние`, "
                            + "`игроки`, "
                            + "`данные`, "
                            + "`штамп` ) VALUES "
                            + " ( ?, ?, ?, ?, ? ) "+
                            "ON DUPLICATE KEY UPDATE "
                            + "игроки=VALUES(игроки), "
                            + "данные=VALUES(данные), "
                            + "состояние=VALUES(состояние), "
                            + "штамп=VALUES(штамп)" )
                    )
                {
                    prepStmt.setString(1, this_server_name+"<:>any<:>");
                    prepStmt.setInt(2, count);
                    prepStmt.setString(3, this_server_bungee_description);
                    prepStmt.setInt(4, 15);
                    prepStmt.setLong(5, Timer.Единое_время()+249 ); //для  надёжности, пусть прогрузит 2 раза

                    prepStmt.executeUpdate();
                    prepStmt.close();



                } catch (SQLException e) {
                    Ostrov.log_err("§сSaveNew error - "+e.getMessage());
                    //e.printStackTrace();
                }*/

            }
        },0 );  
    }
    
    
    



    
    
    
 public static void loadServersAndArenas() {
    if (!OstrovDB.useOstrovData) return;
    Ostrov.async(()-> {
            
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt =ApiOstrov.getOstrovConnection().createStatement();
            S_info s_info;
            rs = stmt.executeQuery( " SELECT *  FROM "+Table.GAMES_MAIN.table_name ); 
            while (rs.next()) {
                //if (!rs.getString("сервер").isEmpty()) разделы.put(rs.getString("сервер"), rs.getString("раздел")); //для быстрого обновления по event
                s_info=new S_info(
                    S_type.valueOf(rs.getString("тип")),
                    rs.getString("сервер"),
                    rs.getString("уровень"),
                    rs.getString("имя").replaceAll("&", "§"),
                    rs.getString("описание").replaceAll("&", "§"),
                    rs.getInt("позиция"),
                    rs.getString("предмет"),
                    rs.getInt("игроки")
                );
                servers.put(rs.getString("сервер"), s_info);

                if (rs.getString("сервер").equalsIgnoreCase(this_server_name)) { //определяем тип, если мотд совпадает с назв.игры
                    this_server_type=S_type.SINGLE;
                } 

                if (rs.getString("тип").equalsIgnoreCase(S_type.SINGLE.toString())) allBungeeServersName.add(rs.getString("сервер"));
            }
            rs.close();

            rs = stmt.executeQuery( " SELECT *  FROM "+Table.GAMES_ARENAS.table_name+" " ); 

            Arena arena;
            String arena_name;

            while (rs.next()) {
                for (String si:servers.keySet()) {
                    if (servers.get(si).type==S_type.MULTIPLE && rs.getString("арена").startsWith(servers.get(si).server)) {
//System.out.println("load si="+si+" rs.getString(арена)= "+rs.getString("арена")+" arenas="+servers.get(si).arenas);
                        arena_name=rs.getString("арена").split("<:>")[1];
                        if (arena_name.isEmpty()) arena_name="any";

                        arena = new Arena(
                            servers.get(si),
                            servers.get(si).getArenaCount(),
                            rs.getString("арена").split("<:>")[0], //server
                            arena_name,
                            UniversalArenaState.fromString(rs.getString("состояние")),    
                            rs.getInt("игроки"),
                            rs.getString("данные").replaceAll("&", "§"),
                            rs.getString("требования"),
                            rs.getString("предмет")
                        );
                        servers.get(si).addArena(arena_name, arena);
                        allBungeeServersName.add(rs.getString("арена").split("<:>")[0]);

                        //if (arena.server.equalsIgnoreCase(this_server_name)) { //прилепил для лобби
                       //     this_server_type=S_type.MULTIPLE;
                        //    this_server_lines = rs.getString("строки").replaceAll("&", "§");
                       // } 
                        break;
                    }
                }

            }

            rs.close();
            stmt.close();

            Ostrov.log_ok("§2Таблица серверов загружена! Запуск таймера.");
            startSinfoTimers();

        } catch (SQLException ex) { 
            Ostrov.log_err("§4Не удалось загрузить таблицу серверов! "+ex.getMessage());
        } finally {
            try{
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            } catch (SQLException ex) {
                Ostrov.log_err("§c Load_servers close err ex="+ex.getMessage());
            }
        }

                
        }, 0);
    
    }
    
    
 public static void getBungeeServerInfo() {
    if (!OstrovDB.useOstrovData) return;
    Ostrov.async(()-> {
            
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt =ApiOstrov.getOstrovConnection().createStatement();

            rs = stmt.executeQuery( "SELECT `id`, `motd`, `type`, `logo` FROM "+Table.BUNGEE_SERVERS.table_name+" WHERE  `name`='"+this_server_name+"' AND `type` NOT LIKE 'NONE'" );
            if (rs.next()) {
                Ostrov.server_id = rs.getInt("id");
                this_server_bungee_description = rs.getString("motd");
                this_server_bungee_logo = rs.getString("logo");
//System.out.print(" ----- this_server_bungee_description="+this_server_bungee_description+" this_server_bungee_logo="+this_server_bungee_logo);
                if (rs.getString("motd").equalsIgnoreCase("LOBBY")) this_server_type=S_type.LOBBY;
                Ostrov.log_ok("§bИД сервера = "+Ostrov.server_id+". Запись состояния в таблицу каждые 10 секунд.");
            } else Ostrov.log_ok("§eИД сервера для имени "+this_server_name+" не получен, состояние сервера в таблицу писаться не будет.");




                rs.close();
                stmt.close();


            } catch (SQLException | NullPointerException ex) { 
                Ostrov.log_err("§4Не удалось загрузить BungeeServerInfo! "+ex.getMessage());
            } finally {
                try{
                    if (rs!=null) rs.close();
                    if (stmt!=null) stmt.close();
                } catch (SQLException ex) {
                    Ostrov.log_err("§c BungeeServerInfo close err ex="+ex.getMessage());
                }
            }
    
                
        }, 40);
    
    }
    
    
   

public static enum S_type {
    SINGLE, MULTIPLE, LOBBY
    ;
}   

    
    
    
}
