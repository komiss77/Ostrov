package ru.komiss77.Managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
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
import org.bukkit.inventory.Inventory;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Enums.Action;
import ru.komiss77.Enums.Game;
import ru.komiss77.Enums.Game.GameType;
import ru.komiss77.Enums.Table;
import ru.komiss77.Enums.GameState;
import ru.komiss77.Events.GameInfoUpdateEvent;
import ru.komiss77.Initiable;
import ru.komiss77.Objects.ArenaInfo;
import ru.komiss77.Objects.GameInfo;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.OstrovDB;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.version.VM;



//+++переход по достижению уровня или пермишен!!
//+предмет "назад"



public final class SM extends Initiable implements Listener {   //не переименовывать!!!! другие плагины берут напрямую!
    
    public static Inventory main_inv;
    public static String main_inv_name="§2Сервера ";
    
    public static final String this_server_name = Bukkit.getMotd();
    public static final Game thisServerGame = Game.fromServerName(this_server_name);//Game.GLOBAL
    public static String chatLogo = "S"; //потом получает из таблицы
    
    private static EnumMap<Game, GameInfo> games; //  game (аркаим даария bw01 bb01 sg02), gameInfo (арены)
    public static int load_interval=3; //секунды
    
    //динамические
    public static Set<String> allBungeeServersName;
    private static int last_check=ApiOstrov.currentTimeSec();
    public static int bungee_online=0;
    //public static boolean send_data, write_server_state_to_bungee_table;

    public static Collection<GameInfo> getGames() {
        return games.values();
    }

    public SM() {
        games=new EnumMap(Game.class);
        allBungeeServersName=new HashSet<>();
        main_inv=Bukkit.createInventory(null, 54, main_inv_name);
        reload();
    }
    

    
    
    @Override
    public void reload() {
        
        HandlerList.unregisterAll(this);
        main_inv.clear();
        games.clear();
        allBungeeServersName.clear();
        
        try {
            getBungeeServerInfo();
            if (!Cfg.GetCongig().getBoolean("ostrov_database.games_info_for_server_menu_load")) {
                Ostrov.log_warn("§eЗагрузка данных серверов выключена.");
                return;
            }
            load_interval = Cfg.GetCongig().getInt("ostrov_database.games_info_for_server_menu_load_interval_ticks");
            if (load_interval<1) load_interval=1; else if (load_interval>60) load_interval=60;
            //write_server_state_to_bungee_table = Cfg.GetCongig().getBoolean("ostrov_database.write_server_state_to_bungee_table");
            //send_data = Cfg.GetCongig().getBoolean("ostrov_database.games_info_for_server_menu_send");
            Bukkit.getPluginManager().registerEvents(this, Ostrov.GetInstance());
            loadServersAndArenas();
            main_inv.setItem(53, new ItemBuilder(Material.BARRIER).setName("§5Закрыть").build());
            Ostrov.log_ok ("§2Меню серверов загружено!");
        } catch (Exception ex) { 
            Ostrov.log_err("инициализация SM: "+ex.getMessage());
        }
    }

    
    //ASYNC!!!
    public static void tickAsync(final int counter) {
        //if (OstrovDB.useOstrovData && thisServerGame.type==GameType.SINGLE && Ostrov.server_id>0 && counter%5==0 ) { //нашел себя в таблице - писать состояние каждые 5 сек
        if (Ostrov.server_id>0 && counter%10==0 ) { //нашел себя в таблице - писать состояние каждые 10 сек
            writeThisServerStateToOstrovDB();
        }
        if (OstrovDB.useOstrovData && Bukkit.getOnlinePlayers().size()>0 && counter%load_interval==0) {
            
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
    
    
    public static void getBungeeServerInfo() {
        if (!OstrovDB.useOstrovData) return;
        Ostrov.async( ()-> {

            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt =ApiOstrov.getOstrovConnection().createStatement();

                rs = stmt.executeQuery( "SELECT `serverId`, `motd`, `type`, `logo` FROM "+Table.BUNGEE_SERVERS.table_name+" WHERE  `name`='"+this_server_name+"' AND `type` NOT LIKE 'NONE'" );
                if (rs.next()) {
                    Ostrov.server_id = rs.getInt("serverId");
                    //this_server_bungee_description = rs.getString("motd");
                    chatLogo = rs.getString("logo");
    //System.out.print(" ----- this_server_bungee_description="+this_server_bungee_description+" this_server_bungee_logo="+this_server_bungee_logo);
                    //if (rs.getString("motd").equalsIgnoreCase("LOBBY")) {
                    //    this_server_type=GameType.LOBBY;
                    //}
                    Ostrov.log_ok("§bИД сервера = "+Ostrov.server_id+". Запись состояния в таблицу каждые 10 секунд.");
                } else {
                    Ostrov.log_ok("§eИД сервера для имени "+this_server_name+" не получен, состояние сервера в таблицу писаться не будет.");
                }
                rs.close();
                
                //вычитать все банжи-имена серверов
                rs = stmt.executeQuery( "SELECT `name` FROM "+Table.BUNGEE_SERVERS.table_name );
                while (rs.next()) {
                    allBungeeServersName.add(rs.getString("name"));
                }
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


   
    
    //вызывается из async!!
    private static void writeThisServerStateToOstrovDB() {  //вызывается из Timer каждые 5 сек. если write_server_state_to_bungee_table=true
        if (!OstrovDB.useOstrovData) return;
        //Ostrov.async( () -> {

        PreparedStatement pst = null;
        try {
//System.out.println("query="+"UPDATE  SET `online`='"+Bukkit.getOnlinePlayers().size()+"',`tps`='"+(int) MinecraftServer.getServer().recentTps[0]+"',`memory`='"+(int)(Runtime.getRuntime().maxMemory()/1024/1024 )+"',`memory_max`='"+(int)(Runtime.getRuntime().totalMemory()/1024/1024)+"',`stamp`='"+Main.Единое_время()+"' WHERE UPPER `id`='"+Main.id+"' ");                
            pst = ApiOstrov.getOstrovConnection().prepareStatement("UPDATE "+Table.BUNGEE_SERVERS.table_name+" SET "
                    + "`online`='"+Bukkit.getOnlinePlayers().size()+"',"
                    + "`onlineLimit`='"+Bukkit.getMaxPlayers()+"',"
                    + "`tps`='"+VM.getNmsServer().getTps()+"',"
                    + "`memory`='"+(int)(Runtime.getRuntime().totalMemory()/1024/1024 )+"',"
                    + "`memoryLimit`='"+(int)(Runtime.getRuntime().maxMemory()/1024/1024)+"',"
                    + "`stamp`='"+ApiOstrov.currentTimeSec()+"'"
                    + " WHERE `serverId`='"+Ostrov.server_id+"' ");
            pst.executeUpdate();

        } catch (SQLException ex) {
            Ostrov.log_err("§cSM  updServerState err ex="+ex.getMessage());
            //e.printStackTrace();
        } finally {
            try{
                if (pst!=null) pst.close();
            } catch (SQLException ex) {
                Ostrov.log_err("§cSM updServerState close err ex="+ex.getMessage());
            }
        }
      //  }, 0);
        
    }



    
    
    
    
    


    private static void startSinfoTimers() {   //запускается после загрузки в loadServersAndArenas
    
        Statement stmt=null;
        ResultSet rs = null;
        Game game;

        try {
            stmt = ApiOstrov.getOstrovConnection().createStatement(); 
//System.out.println(" SELECT `сервер`,`игроки`  FROM "+Table.GAMES_MAIN.table_name+" WHERE `тип` LIKE '"+GameType.SINGLE.toString()+"' AND `штамп` > "+last_check);
            //rs = stmt.executeQuery( " SELECT `game`,`online`  FROM "+Table.GAMES.table_name+" WHERE `type`='"+GameType.SINGLE.toString()+"' AND `stamp` > "+last_check ); 
            rs = stmt.executeQuery( " SELECT `name`,`online`  FROM "+Table.BUNGEE_SERVERS.table_name+" WHERE `type`='DIAG' AND `stamp` > "+last_check ); 
            while (rs.next()) {
                game  = Game.fromServerName(rs.getString("name"));
//System.out.println("rs.next servers.containsKey?"+servers.containsKey(rs.getString("сервер")));                                    
                //if (games.containsKey(rs.getString("game"))) {  //arcaim daaria bw bb sg
                if (!games.containsKey(game)) {
                    games.put( game, new GameInfo(game, rs.getString("name")) );
                }
                games.get(game).updateSingle(GameState.РАБОТАЕТ, rs.getInt("online"));//updatePlayerCount(rs.getInt("online"));
                //}
            }
            rs.close();



            rs = stmt.executeQuery( " SELECT *  FROM "+Table.ARENAS.table_name+" WHERE  `stamp` > "+last_check ); 

            while (rs.next()) {
                game = Game.fromString(rs.getString("game"));
                if (game!=null) { //выше все игры прогружаются в gamesб так что будут все возможные
                    games.get(game).updateArena(rs.getString("server"),// - есть в арене, но может быть одинаковая арена на разных серверах!
                            rs.getString("arenaName"),
                            GameState.fromString(rs.getString("state")),
                            rs.getInt("players"),
                            rs.getString("line0"),
                            rs.getString("line1"),
                            rs.getString("line2"),
                            rs.getString("line3"),
                            rs.getString("extra")
                    );
                } else {
                    Ostrov.log_err("arena load_timer - нет игры "+rs.getString("game"));
                }

//Bukkit.broadcastMessage("SELECT "+rs.getString("арена"));                                    
            }

            rs.close();
            stmt.close();

            last_check=ApiOstrov.currentTimeSec();

            games.values().stream().forEach((si) -> {
                si.updateIcon();
            });
                //main_inv.getViewers().stream().forEach( p -> {((Player)p).updateInventory();} );
            //Ostrov.log_ok("§2Данные серверов загружена! Запуск таймера.");

        } catch (SQLException ex) { 
            Ostrov.log_err("§4SM Не удалось загрузить данные серверов! update_sinfo "+ex.getMessage());
        } finally {
            try {
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            } catch (SQLException ex) {
                Ostrov.log_err("§4SM Не удалось закрыть соединение! update_sinfo "+ex.getMessage());
            }
        }


     }    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    
    //public static CaseInsensitiveMap<GameInfo> getServers() {
   //     return servers;
   // }
    
   // public static Collection<String> getTypes() {
    //    return games.keySet();
    //}
    
    public static Inventory getGameInventory(final Game game) {
        return games.get(game).arena_inv;
    }
    
    public static GameInfo getGameInfo(final String serverName) {
        return games.get(Game.fromServerName(serverName));
        //return servers.get(serverName.substring(0, 2)); - substring не катит, может быть arcaim daaria
    }
    public static GameInfo getGameInfo(final Game game) {
        return games.get(game);
        //return servers.get(serverName.substring(0, 2)); - substring не катит, может быть arcaim daaria
    }
    

    
    public static Collection<ArenaInfo> getArenas(final Game game) { //аркаим даария bw01 bb01 sg02
        if (games.containsKey(game)) return games.get(game).arenas.values();
        else return new ArrayList<>();
    }
    
    public static List<String> getArenasNames(final Game game) {  //аркаим даария bw01 bb01 sg02
//System.out.println("getArenasNames "+game);
        final List<String> list = new ArrayList<>();
        //for (final GameInfo gi : games.values()) {
            if (games.containsKey(game)) {
//System.out.println("si server="+si.server+" arenas="+ApiOstrov.listToString(si.getArenas()," "));
                for (final ArenaInfo ai : games.get(game).arenas.values()) {
                    list.add(ai.arenaName);
                }
            }
        //}
        return list;
    }
    


    
    
    
    
    
    
    
    
    
    
    
    
    
     
    
    
    
    
    
    
    //может быть async!!
    
    public static void sendArenaData (
            final String arenaName,
            final GameState state,
            final int players,
            final String line0,
            final String line1,
            final String line2,
            final String line3,
            final String extra
        ) {
        
        if (thisServerGame.type==GameType.MULTIPLE) {  //на миниигре вызываем локальные эвент для табличек этого сервера! (с банжи не получит)
            if (Bukkit.isPrimaryThread()) {
                Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent( thisServerGame, this_server_name, arenaName, state, players, line0, line1, line2, line3, extra));
            } else {
                Ostrov.sync(()-> Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent( thisServerGame, this_server_name, arenaName, state, players, line0, line1, line2, line3, extra)) ,0);
            }
        }
        
        if (Bukkit.getOnlinePlayers().isEmpty() || state==GameState.ОЖИДАНИЕ) {
            
            if (Bukkit.isPrimaryThread()) {
                Ostrov.async(()-> writeArenaStateToMySql(arenaName, state, players, line0, line1, line2, line3, extra), 0);
            } else {
                writeArenaStateToMySql(arenaName, state, players, line0, line1, line2, line3, extra);
            }
            
        } else if (state==GameState.ВЫКЛЮЧЕНА || state==GameState.ПЕРЕЗАПУСК) {
            
            if (Bukkit.isPrimaryThread()) {
                writeArenaStateToMySql(arenaName, state, players, line0, line1, line2, line3, extra);
            } else {
                Ostrov.sync( ()-> writeArenaStateToMySql(arenaName, state, players, line0, line1, line2, line3, extra), 0);
            }
            
        } else {

            ApiOstrov.sendMessage( Action.GAME_INFO_TO_BUNGEE, SM.this_server_name, state.tag, players, 0, arenaName, line0, line1, line2, line3, extra );
                    
        }
    } 
  
    
    private static void writeArenaStateToMySql (final String arenaName, final GameState state, final int players, final String line0, final String line1, final String line2, final String line3, final String extra ) {
        if (!OstrovDB.useOstrovData) return;
//System.out.println("bsign.spigot.Bsign.write()");   
//UPDATE `arenas` SET `line0` = 'sd' WHERE `arenaName` = 'vvv'; 
//записывать только изменённые! запрос формировать как??
        try {
                
            PreparedStatement pst = ApiOstrov.getOstrovConnection().prepareStatement("UPDATE "+Table.ARENAS.table_name+" SET `state`='"+String.valueOf(state)+"', `line0`='"+line0+"', `line1`='"+line1+"', `line2`='"+line2+"', `line3`='"+line3+"', `extra`='"+extra+"', `players`='"+players+"' WHERE `arenaName`='"+arenaName+"' AND `server`='"+this_server_name+"'; ");

            int res = pst.executeUpdate();
            pst.close();
            
            if (res==0) {
                pst = ApiOstrov.getOstrovConnection().prepareStatement("INSERT INTO "+Table.ARENAS.table_name+" ( "
                    + "`server`, "
                    + "`game`, "
                    + "`arenaName`, "
                    + "`state`, "
                    + "`line0`, "
                    + "`line1`, "
                    + "`line2`, "
                    + "`line3`, "
                    + "`extra`, "
                    + "`players`, "
                    //+ "`material`, "
                    //+ "`level`, "
                    //+ "`reputation`, "
                    + "`stamp` ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) " );
            
        
            pst.setString(1, SM.this_server_name);
            pst.setString(2, Game.fromServerName(this_server_name).name());
            pst.setString(3, arenaName);
            pst.setString(4, state.toString());
            pst.setString(5, line0);
            pst.setString(6, line1);
            pst.setString(7, line2);
            pst.setString(8, line3);
            pst.setString(9, extra);
            pst.setInt(10, players);
            pst.setInt(11, ApiOstrov.currentTimeSec()+1 ); //для  надёжности, пусть прогрузит 2 раза

            pst.executeUpdate();
            pst.close();   
        }


        } catch (SQLException e) {
            Ostrov.log_err("§cSM writeArenaStateToMySql error - "+e.getMessage());
            //e.printStackTrace();
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
        
        String gameDisplayName=e.getView().getTitle().replaceFirst(main_inv_name, "").trim();
//System.out.println("2 type="+type);     

        if (gameDisplayName.isEmpty()) { //гл.меню игр 
            
            if (e.getSlot()==53) {
                p.closeInventory();
                return;
            }
            
            games.values().stream().forEach( (gameInfo) -> {
                if (gameInfo.game.menuSlot==e.getSlot()) {
                    
                    if (gameInfo.game.type==GameType.SINGLE) {
                        p.closeInventory();
                        ApiOstrov.sendToServer(p, gameInfo.game.name(), "");
//System.out.println("to server "+si.server); 

                    } else {
//System.out.println("sinfo="+si.server); 
                        
                        if(e.isLeftClick()) {
                            
                            if (ApiOstrov.getWarpManager().exist(gameInfo.game.name())) {
                                p.closeInventory();
                                ApiOstrov.teleportSave(p, ApiOstrov.getWarpManager().getWarp(gameInfo.game.name()).loc, false);
                                p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 2);
                            } else {
//System.out.println("openInventory "+si.server);
                                Ostrov.sync( () -> p.openInventory(gameInfo.arena_inv), 2 );
                                //p.openInventory(si.arena_inv);
                                p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1, 2);
                            }
                            
                        } else {
                            
                            p.openInventory(gameInfo.arena_inv);
                            p.playSound(p.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1, 2);
                            
                        }
                        
                    }
                }
            });
            
        } else { //меню арен - тп на арену
            for (Game game : Game.values()) {
                if (game.displayName.equals(gameDisplayName)) {
                    games.get(game).invClick(p, e.getSlot());
                }
            }
            //if (games.containsKey(type)) {
            //    games.get(type).invClick(p, e.getSlot());
           // }
            
        }

    }





    

    
    //public void on_shut_down() {
    //     if (send_data) write(-1);
    //}
    
    
    
    
    
    /*
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
    }*/
    
    

    
    //только для SINGLE
  /*  private static void write (final int count ) { - переделал на BUNGEE_SERVERS
        if (!OstrovDB.useOstrovData) return;
        Ostrov.async(()-> {
            //if (ApiOstrov.getOstrovConnection()== null) return;
    //System.out.println(" --- write online="+count+" type = "+this_server_type);
            if (this_server_type==GameType.SINGLE) { //не пишем в GAMES_MAIN, там не будет сервера с таким именем (MULTIPLE - две буквы от названия игры) !

                try ( 
                    PreparedStatement prepStmt = ApiOstrov.getOstrovConnection().prepareStatement("UPDATE "+Table.GAMES.table_name+" SET `online` = '"+count+"',`stamp` = '"+ApiOstrov.currentTimeSec()+"' WHERE `game` = '"+this_server_name+"' ; " )
                    )
                {
                    prepStmt.executeUpdate();
                    prepStmt.close();

                } catch (SQLException e) {
                    Ostrov.log_err("§сSign write - "+e.getMessage());
                    //e.printStackTrace();
                }

            } else if (this_server_name.startsWith("lobby")) { //для лобби!
                
                //writeArenaStateToMySql(this_server_name+"<:>any", count, this_server_bungee_description, UniversalArenaState.РАБОТАЕТ);
                writeArenaStateToMySql("", UniversalArenaState.РАБОТАЕТ, count, this_server_bungee_description, "", "", "", "");

            }
        },0 );  
    }*/
    
    
    



    
    
    
    public static void loadServersAndArenas() {
        
        if (!OstrovDB.useOstrovData) return;
        
        for (final Game game : Game.values()) {
            if (game==Game.GLOBAL || game.type==GameType.SINGLE) continue; //большие пропускаем, добавятся в startSinfoTimers
            games.put(game, new GameInfo(game, null) );
        }
        
        
        Ostrov.async(()-> {

            Statement stmt = null;
            ResultSet rs = null;
            
            try {
                
                stmt =ApiOstrov.getOstrovConnection().createStatement();
                
               /* rs = stmt.executeQuery( " SELECT *  FROM "+Table.GAMES.table_name ); 
                while (rs.next()) {
                    //if (!rs.getString("сервер").isEmpty()) разделы.put(rs.getString("сервер"), rs.getString("раздел")); //для быстрого обновления по event
                    gi=new GameInfo(
                        rs.getInt("slot"),
                        rs.getString("game"), //arcaim daaria bw bb sg
                        GameType.valueOf(rs.getString("type")), //SINGLE, MULTIPLE, LOBBY
                        rs.getString("displayName").replaceAll("&", "§"),
                        rs.getString("descr").replaceAll("&", "§"),
                        rs.getInt("level"),
                        rs.getInt("reputation"),
                        Material.matchMaterial(rs.getString("material")),
                        rs.getInt("playerInGame")
                    );
                    games.put(gi.game, gi);//games.put(rs.getString("game"), gi);*/

                   // if (gi.game.equalsIgnoreCase(this_server_name)) { //определяем тип, если мотд совпадает с назв.игры
                    //    this_server_type=GameType.SINGLE;
                  //  } 
//
                    //if (gi.type==GameType.SINGLE) {
                    //    allBungeeServersName.add(rs.getString("game"));
                    //}
               // }
              //  rs.close();

                
                Game game;
                
                rs = stmt.executeQuery( " SELECT *  FROM "+Table.ARENAS.table_name ); 
                //GameInfo gi;
                while (rs.next()) {
                    game = Game.fromString(rs.getString("game"));
                    if (game!=null) {
                        final ArenaInfo ai = new ArenaInfo(games.get(game), 
                                rs.getString("server"), //arcaim daaria bw01 bb01 sg02
                                rs.getString("arenaName"),
                                GameState.fromString(rs.getString("state")), 
                                rs.getInt("players"),
                                rs.getString("line0"), rs.getString("line1"), rs.getString("line2"), rs.getString("line3"), rs.getString("extra"),
                                rs.getInt("level"), rs.getInt("reputation"),
                                games.get(game).arenas.size(), 
                                Material.matchMaterial(rs.getString("material"))
                        );
                        games.get(game).arenas.put(ai.slot, ai);
                    } else {
                        Ostrov.log_err("SM loadServersAndArenas - нет игры "+rs.getString("game"));
                    }
                  //  gi = games.get(Gars.getString("game"));
                  //  if (gi!=null && gi.type==GameType.MULTIPLE) {
                    /*    final ArenaInfo ai = new ArenaInfo(gi, 
                                rs.getString("server"), //arcaim daaria bw01 bb01 sg02
                                rs.getString("arenaName"),
                                UniversalArenaState.fromString(rs.getString("state")), 
                                rs.getInt("players"),
                                rs.getString("line0"), rs.getString("line1"), rs.getString("line2"), rs.getString("line3"), rs.getString("extra"),
                                rs.getInt("level"), rs.getInt("reputation"),
                                gi.arenas.size(), 
                                Material.matchMaterial(rs.getString("material"))
                        );
                        gi.arenas.put(ai.slot, ai);
                  //  }
                    
                    
                  /*  for (String si:servers.keySet()) {
                        if (servers.get(si).type==GameType.MULTIPLE && rs.getString("арена").startsWith(servers.get(si).server)) {
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
                           //     this_server_type=GameType.MULTIPLE;
                            //    this_server_lines = rs.getString("строки").replaceAll("&", "§");
                           // } 
                            break;
                        }
                    }*/

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
    
    


  

    
    
    
}
