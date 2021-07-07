package ru.komiss77.modules.games;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.enums.Operation;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.Table;
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.ServerType;
import ru.komiss77.Initiable;
import ru.komiss77.Timer;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.events.BsignLocalArenaClick;
import ru.komiss77.utils.ColorUtils;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.version.VM;



//+++переход по достижению уровня или пермишен!!
//+предмет "назад"



public final class GM extends Initiable implements Listener {   //не переименовывать!!!! другие плагины берут напрямую!
    
    
    private static OstrovConfig gameSigns;

    public static final String this_server_name = Bukkit.getMotd();
    public static final Game thisServerGame = Game.fromServerName(this_server_name);//Game.GLOBAL
    public static String chatLogo = "S"; //потом получает из таблицы
    
    private static EnumMap<Game, GameInfo> games; //  game (аркаим даария bw01 bb01 sg02), gameInfo (арены)
    protected static HashMap<String,GameSign>signs;
    private static int load_interval=3; //секунды
    
    //динамические
    public static Set<String> allBungeeServersName;
    private static int lastCheck=ApiOstrov.currentTimeSec();
    public static int bungee_online=0;
    
    public static Inventory main_inv;
    public static final String main_inv_name="§2Сервера ";

    


    public GM() {
        games=new EnumMap(Game.class);
        signs=new HashMap<>();
        allBungeeServersName=new HashSet<>();
        main_inv=Bukkit.createInventory(null, 54, main_inv_name);
        reload();
    }
    

    
    
    @Override
    public void reload() {
        
        HandlerList.unregisterAll(this);
        
        gameSigns = Cfg.manager.getNewConfig("gameSigns.yml", new String[]{"", "Ostrov77 gameSigns config file", ""} );
        //gameSigns.addDefault("use", false);
        gameSigns.saveConfig();       
        

        
        
        main_inv.clear();
        games.clear();
        signs.clear();
        allBungeeServersName.clear();
//System.out.println("reload1");                                    
        
        Bukkit.getPluginManager().registerEvents(this, Ostrov.GetInstance());
        
        try {
            getBungeeServerInfo();
//System.out.println("reload2");                                    
            if (!Cfg.GetCongig().getBoolean("ostrov_database.games_info_for_server_menu_load")) {
                Ostrov.log_warn("§eЗагрузка данных серверов выключена.");
                if (thisServerGame.type==ServerType.ARENAS) { //фикс для локальных табличек нужна локальная gi
                    games.put(thisServerGame, new GameInfo(thisServerGame));
                } else {
                    games.put(thisServerGame, new GameInfo(thisServerGame));
                }
                //Bukkit.getPluginManager().callEvent(new GameInfoLoadEvent());
                loadGameSign();
                return;
            }
            load_interval = Cfg.GetCongig().getInt("ostrov_database.games_info_for_server_menu_load_interval_sec");
            if (load_interval<1) {
                load_interval=1;
            } else if (load_interval>60) {
                load_interval=60;
            }
//System.out.println("reload3 load_interval="+load_interval);                                    
            //write_server_state_to_bungee_table = Cfg.GetCongig().getBoolean("ostrov_database.write_server_state_to_bungee_table");
            //send_data = Cfg.GetCongig().getBoolean("ostrov_database.games_info_for_server_menu_send");
            Ostrov.async(()-> loadArenaInfo(0), 0);
//System.out.println("reload4");                                    
            main_inv.setItem(53, new ItemBuilder(Material.BARRIER).setName("§5Закрыть").build());
//System.out.println("reload5");                                    
            //Ostrov.log_ok ("§2Меню серверов загружено!");
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
            loadArenaInfo(lastCheck);
            lastCheck=ApiOstrov.currentTimeSec();
        }
    }



    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    // ================== Таблички ====================
    
    private static void loadGameSign() {
        if (gameSigns.getConfigurationSection("signs") !=null)   {
            //Bsign.config.getConfigurationSection("signs").getKeys(false).stream().forEach((loc_string) -> {
            GameInfo gi;
            String serverName;
            ArenaInfo ai=null;
            
            for (String loc_string : gameSigns.getConfigurationSection("signs").getKeys(false)) {
            
                if (this_server_name.length()==4 && !gameSigns.getString("signs." + loc_string).startsWith(this_server_name)) {
                    Ostrov.log_warn("в локальном режиме эта табличка работать не будет: "+loc_string);
                    continue;
                }
                
                final Location loc=ApiOstrov.locFromString(loc_string);
                if (loc==null) {
                    Ostrov.log_err("loadGameSign -> Нет такой локации: "+loc_string+" для таблички "+gameSigns.getString("signs." + loc_string));
                    continue;
                } 
                
                if (!Tag.SIGNS.isTagged(loc.getBlock().getType()) && !Tag.STANDING_SIGNS.isTagged(loc.getBlock().getType())) {
                    //Ostrov.log_err("На локации не табличка: "+gameSigns.getString("signs." + loc_string)+" -> "+loc_string);
                    Ostrov.log_err("loadGameSign -> На локации не табличка: "+loc_string);
                    continue;
                }
                
                serverName = gameSigns.getString("signs."+loc_string+".server");
                gi = games.get(Game.fromServerName(serverName));
                if (gi==null) {
                    Ostrov.log_err("loadGameSign -> Нет игры для сервера "+serverName+", табличка "+ loc_string);
                    continue;
                }
                
                String arenaName = "";
                
                if ( gi.game.type==ServerType.ONE_GAME ) {
                    
                    ai = gi.arenas.get(0);
                    if (ai==null) {
                        Ostrov.log_err("loadGameSign -> Нет ArenaInfo для сервера "+serverName+", табличка "+ loc_string);
                        continue;
                    }
                    
                } else if ( gi.game.type==ServerType.ARENAS || gi.game.type==ServerType.LOBBY ) {
                    
                    arenaName = gameSigns.getString("signs."+loc_string+".arena", "");
                    if (arenaName.isEmpty()) {
                        Ostrov.log_err("loadGameSign -> тип сервера "+serverName+"="+gi.game.type+", но аренна не указана; табличка "+ loc_string);
                        continue;
                    }
                    ai = gi.getArena(serverName, arenaName);
                    if (ai==null) {
                        Ostrov.log_err("loadGameSign -> Нет ArenaInfo для сервера "+serverName+", арена "+arenaName+" табличка "+ loc_string);
                        continue;
                    }
                }
                
                signs.put(loc_string, new GameSign(loc, serverName, arenaName));
                ai.signs.add(loc_string); //в арене храним ссылку для обновы таблички
//System.out.println("createSign "+loc_string+" ai="+ai.server+":"+ai.arenaName+" line0="+ai.line0);
                updateSigns(ai);
                
            }
        }
    }

    
    protected static void updateSigns(final ArenaInfo ai) {
        if (Bukkit.isPrimaryThread()) {
            updateSign(ai);
        } else {
            Ostrov.sync(()-> updateSign(ai), 0);
        }
    }
    private static void updateSign(final ArenaInfo ai) {
        //не ставит сразу строки одиночки!
        GameSign gs;
        Block b;
        for (final String loc_string : ai.signs) {
            gs = GM.signs.get(loc_string);
            if (gs!=null) {
                b = gs.signLoc.getBlock();
                if ( Tag.SIGNS.isTagged(b.getType()) || Tag.STANDING_SIGNS.isTagged(b.getType()) ) {
                    Sign sign = (Sign)b.getState();
                    sign.setLine(0, ai.line0);
                    sign.setLine(1, ai.line1);
                    sign.setLine(2, ai.line2);
                    sign.setLine(3, ai.line3);
                    sign.update();
                }
                if (gs.attachement_loc!=null) {
                    final BlockData bd = ColorUtils.changeColor(gs.attachement_mat, ai.state.attachedColor).createBlockData();
                    for (final Player p : gs.attachement_loc.getWorld().getPlayers()) {
                        p.sendBlockChange(gs.attachement_loc, bd);
                    }
                }
            }
        }
    }
    
    
    
    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignCreate(final SignChangeEvent e) {

        if (e.getLine(0).equalsIgnoreCase("bs")) {
            final Player p = e.getPlayer();

            if (!ApiOstrov.isLocalBuilder(p, true)) return;

            final String serverName = e.getLine(1);
            if (serverName.isEmpty()) {
                e.setLine(3, "§4Ошибка!");
                p.sendMessage( "§4Строка 2 - сервер " );
                return;
            }

            final Game game = Game.fromServerName(serverName);
            final GameInfo gi = games.get(game);
            if (gi==null) {
                e.setLine(3, "§4Ошибка!");
                p.sendMessage( "§4Нет игры для сервера "+serverName );
                return;
            }

            if (!allBungeeServersName.contains(serverName)) {
                e.setLine(3, "§4Ошибка!");
                p.sendMessage( "§4строка 2 - §fсервер. Доступные:" );
                //final EnumSet<Game> all= EnumSet.allOf(Game.class);
                //all.remove(Game.GLOBAL);
                //all.remove(Game.LOBBY);
                //p.sendMessage( "§e"+ApiOstrov.enumSetToString(all) );
                p.sendMessage( "§e"+ApiOstrov.listToString(allBungeeServersName, ",") );
                return;
            }

            
            final String arenaName = e.getLine(2);
            if (game.type==ServerType.ARENAS && arenaName.isEmpty()) {
                e.setLine(3, "§4Ошибка!");
                p.sendMessage( "§аДля сервера с аренами §bстрока 2 §f- название арены с учётом регистра." ); 
                p.sendMessage( "§аНайдены арены для сервера "+serverName+" :" ); 
                p.sendMessage( "§e"+ApiOstrov.listToString(gi.getArenaNames(serverName), ",") );
                return;
            }

           // if (arena.isEmpty()) {
                //e.setLine(2, "§4Ошибка!");
               //player.sendMessage( "§4line 3 - арена не указана! табличка для всех арен сервера!"); 
               // e.getPlayer().sendMessage( "§4строка 2 - §fназвание арены с учётом регистра," );
               // e.getPlayer().sendMessage( "или §bany §f(Будет направлять просто на сервер)." );
              //  e.getPlayer().sendMessage( "§eВозможные арены для этой игры: (могут быть на разных серверах!)" );
             //   e.getPlayer().sendMessage("§a"+ApiOstrov.listToString(GM.getArenasNames(bungeeServerName.substring(0, 2))," " ) );
                //e.getPlayer().sendMessage( "§eВнимание: §4строка 3 - не указана. Будет направлять просто на сервер!"); 
             //   return;
           // }

            //e.setLine(0, Main.signprefix);                              //ставим первую строчку
            //e.setLine(1, bungeeServerName);                              //ставим первую строчку
            // e.setLine(2, arena);                              //ставим первую строчку
            //e.setLine(3, "§b Ожидаем данные...");
           // e.setLine(3, "§6Ожидаем данные...");

            final String locAsString = ApiOstrov.stringFromLoc(e.getBlock().getLocation());
            
            signs.put( locAsString, new GameSign(e.getBlock().getLocation(), serverName, arenaName));
            //добав в инфоб обновить
            final ArenaInfo ai = gi.getArena(serverName, arenaName);
            //if ( game.type==ServerType.ONE_GAME ) {
           //     ai = gi.arenas.get(0);//.signs.add(locAsString);
           // } else if ( game.type==ServerType.ARENAS || game.type==ServerType.LOBBY ) {
             //   ai = gi.getArena(e.getLine(1), e.getLine(2));
           // }
            if (ai!=null) {
                ai.signs.add(locAsString);
                //updateSigns(ai);
                e.setLine(0, ai.line0);
                e.setLine(1, ai.line1);
                e.setLine(2, ai.line2);
                e.setLine(3, ai.line3);
            }
            gameSigns.set("signs."+locAsString+".server", serverName);
            gameSigns.set("signs."+locAsString+".arena", arenaName);
            gameSigns.saveConfig();
            
            if (e.getLine(2).isEmpty()) {
                e.getPlayer().sendMessage( "§6табличка для сервера §b" +  serverName+"§6 создана на локации "+ locAsString );
            } else {
                e.getPlayer().sendMessage( "§6табличка для сервера §b" +  serverName +" §6и арены §b"+arenaName+"§6 создана на локации "+ locAsString );
            }

        }
    }        


    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignClick(final PlayerInteractEvent e) {
        //if (e.getAction()==Action.PHYSICAL || e.getAction()==Action.LEFT_CLICK_AIR || e.getAction()==Action.RIGHT_CLICK_AIR ) return;
        if (e.getClickedBlock()==null || signs.isEmpty()) return;
        //if (e.getClickedBlock().getType()==Material.SIGN || e.getClickedBlock().getType()==Material.SIGN_POST || e.getClickedBlock().getType()==Material.WALL_SIGN ) {
        if (Tag.WALL_SIGNS.isTagged(e.getClickedBlock().getType()) || Tag.STANDING_SIGNS.isTagged(e.getClickedBlock().getType()) ) {
            
            final String locAsString = ApiOstrov.stringFromLoc(e.getClickedBlock().getLocation());
            
            if (signs.containsKey(locAsString)) {

                final Player p = e.getPlayer();
                
                
                if (Timer.has(p, "gameSign")) {
                    p.sendMessage("§8подождите 2 секунды..");
                    return;
                }
                Timer.add(p, "gameSign", 2);
                
                e.setUseInteractedBlock(Event.Result.DENY);
                
                final GameSign gameSign = signs.get(locAsString);
                
                if (e.getAction()==Action.LEFT_CLICK_BLOCK && ApiOstrov.isLocalBuilder(p, false)) {
                    //breack
                    e.getClickedBlock().breakNaturally();
                    signs.remove(locAsString);  
                    gameSigns.set("signs." + locAsString, null);
                    gameSigns.saveConfig(); 
                    p.sendMessage("§6табличка для §b"+ gameSign.server+" : " + gameSign.arena+" §4удалена!");
                    final GameInfo gi = games.get(Game.fromServerName(gameSign.server));
                    if (gi!=null) {
                        if ( gi.game.type==ServerType.ONE_GAME ) {
                            gi.arenas.get(0).signs.remove(locAsString);
                        } else if ( gi.game.type==ServerType.ARENAS || gi.game.type==ServerType.LOBBY ) {
                            gi.getArena(gameSign.server, gameSign.arena).signs.remove(locAsString);
                        }
                    }
                    return;
                }
                
                
                

                if (thisServerGame.type==ServerType.ARENAS) {
                    Bukkit.getPluginManager().callEvent(new BsignLocalArenaClick( p, gameSign.arena ));
                } else {
                    ApiOstrov.sendToServer (p, gameSign.server, gameSign.arena);
                }
//p.sendMessage("info!!!!!");

                  //  new BukkitRunnable() {
                //    @Override
                //    public void run() {
                //        if (p!=null && p.isOnline()) ssign.send(p);
                //    }
               // }.runTaskLater(Main.instance, 1);

            }
            
        }
        
        
    
    }







    // ======================================




















    
    
    
    
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



    
    private static GameInfo getGameInfo(final String serverName) {
        final Game game = Game.fromServerName(serverName);
        
        if (game==null || game==Game.GLOBAL) {
            Ostrov.log_warn("GameManager onGameData : нет игры для сервера "+serverName);
            return null;
        }
        
        if (!games.containsKey(game)) {
            switch (game.type) {

                case ONE_GAME:
                case LOBBY:
                case ARENAS:
                  games.put(game, new GameInfo(game ));
                    break;

                //case LOBBY:
              //  case ARENAS:
                   // games.put(game, new GameInfo(game));
                //    break;

                default:
                    return null;
            }
        }
        return games.get(game);
        //gi.update(serverName, arenaName, state, players, line0, line1, line2, line3, extra, mat);
        
    }
    
    
    

    //вызывать ASYNC!!
    private static void loadArenaInfo(final int fromStamp) {   //запускается после загрузки в loadServersAndArenas
    
        Statement stmt=null;
        ResultSet rs = null;
        //Game game;
//System.out.println("loadArenaInfo fromStamp="+fromStamp);                                    

        try {
            stmt = ApiOstrov.getOstrovConnection().createStatement(); 
//System.out.println(" SELECT `сервер`,`игроки`  FROM "+Table.GAMES_MAIN.table_name+" WHERE `тип` LIKE '"+GameType.SINGLE.toString()+"' AND `штамп` > "+last_check);
            rs = stmt.executeQuery( " SELECT `name`,`motd`,`online`,`type`  FROM "+Table.BUNGEE_SERVERS.table_name+" WHERE `stamp` >= "+fromStamp ); 
//System.out.println("loadArenaInfo 1");                                    
            
            ServerType type;

            GameInfo gi;
           // ServerType type;
            //в таблице банжи могут быть только одиночные и лобби
            while (rs.next()) {
                
                type = ServerType.fromString(rs.getString("type"));
                if (type!=ServerType.ONE_GAME && type!=ServerType.LOBBY) continue; //или getGameInfo ругается на REG и прочие
                
                //gi = getGameInfo(rs.getString("name"), rs.getString("motd"));
                gi = getGameInfo(rs.getString("name"));
                
                if (gi!=null) {
                    
                    if (gi.game.type==ServerType.ONE_GAME) {
                        
                        gi.update(rs.getString("name"), rs.getString("motd"), rs.getInt("online")>=0 ? GameState.РАБОТАЕТ : GameState.ВЫКЛЮЧЕНА, rs.getInt("online"), null, null, null, null, "");
                    
                    } else if (gi.game.type==ServerType.LOBBY) {
                        
                        ArenaInfo ai = gi.getArena(rs.getString("name"), rs.getString("motd"));
                        if (ai==null) {
                            ai = new ArenaInfo(gi, rs.getString("name"), rs.getString("motd"), 0, 0, Material.matchMaterial(Game.LOBBY.mat));
                            gi.arenas.put(ai.slot, ai);
                        }
                        gi.update(rs.getString("name"), rs.getString("motd"), rs.getInt("online")>=0 ? GameState.РАБОТАЕТ : GameState.ВЫКЛЮЧЕНА, rs.getInt("online"), null, null, null, null, "");

                    }
                }
                
             //   game  = Game.fromServerName(rs.getString("name"));
                
                //if (game.type==ServerType.ONE_GAME || game.type==ServerType.LOBBY) {
                //    gi = getGameInfo(game);
               //     if (gi!=null) {
               //         gi.update(rs.getString("name"), rs.getString("motd"), rs.getInt("online")>=0 ? GameState.РАБОТАЕТ : GameState.ВЫКЛЮЧЕНА, rs.getInt("online"), null, null, null, null, "");
              //      }
              //  }
                
              //  type = ServerType.fromString(rs.getString("type"));
                
             //   if (type==ServerType.LOBBY) {
                    ///onGameData(rs.getString("name"), rs.getString("motd"), rs.getInt("online")>=0 ? GameState.РАБОТАЕТ : GameState.ВЫКЛЮЧЕНА, rs.getInt("online"), "", "", "", "", "", null );
                    
               //     gi = getGameInfo(rs.getString("name"), rs.getString("motd"));
                    //if (gi==null) {
                    //    gi = new GameInfo(Game.LOBBY);
                    //    games.put( Game.LOBBY, gi);
                    //}
                    
                  //  ArenaInfo ai = gi.getArena(rs.getString("name"), rs.getString("motd"));
                  //  if (ai==null) {
                  //      ai = new ArenaInfo(gi, rs.getString("name"), rs.getString("motd"), 0, 0, Material.matchMaterial(Game.LOBBY.mat));
                  //      gi.arenas.put(ai.slot, ai);
                  //  }
                 //   gi.update(rs.getString("name"), rs.getString("motd"), rs.getInt("online")>=0 ? GameState.РАБОТАЕТ : GameState.ВЫКЛЮЧЕНА, rs.getInt("online"), null, null, null, null, "");
                    
            //    } else if (type==ServerType.ONE_GAME) {
                    
                  //  game  = Game.fromServerName(rs.getString("name"));
                 //   gi = getGameInfo(game);
                    //gi = games.get(game);
                   // if (gi==null) { //SINGLE
                  //      gi = new GameInfo(game, rs.getString("name"));
                  //      games.put( game, gi);
                  //  }
                 //   gi.update(rs.getString("name"), rs.getString("motd"), rs.getInt("online")>=0 ? GameState.РАБОТАЕТ : GameState.ВЫКЛЮЧЕНА, rs.getInt("online"), null, null, null, null, "");
                    //gi.updateSingle(rs.getInt("online")>=0 ? GameState.РАБОТАЕТ : GameState.ВЫКЛЮЧЕНА, rs.getInt("online"));//updatePlayerCount(rs.getInt("online"));
              //  }

            }
            rs.close();

//System.out.println("loadArenaInfo 2");                                    


            rs = stmt.executeQuery( " SELECT *  FROM "+Table.ARENAS.table_name+" WHERE  `stamp` >= "+fromStamp ); 
            
//System.out.println("loadArenaInfo 3");                                    
            while (rs.next()) {
                
                gi = getGameInfo(rs.getString("server"));
                
                if (gi!=null) {
                    
                    if (gi.game.type==ServerType.ARENAS) {
                        
                        ArenaInfo ai = gi.getArena(rs.getString("server"), rs.getString("arenaName"));
                        if (ai==null) {
                            ai = new ArenaInfo(
                                gi, 
                                rs.getString("server"),
                                rs.getString("arenaName"), 
                                rs.getInt("level"),
                                rs.getInt("reputation"),
                                Material.matchMaterial(rs.getString("material"))
                            );
                            gi.arenas.put(ai.slot, ai);
                        }
                        
                        gi.update(
                            rs.getString("server"),
                            rs.getString("arenaName"), 
                            GameState.fromString(rs.getString("state")), 
                            rs.getInt("players"),
                            rs.getString("line0"),
                            rs.getString("line1"),
                            rs.getString("line2"),
                            rs.getString("line3"),
                            rs.getString("extra")
                        );

                    }
                }
                
               // game = Game.fromString(rs.getString("game"));
//System.out.println("loadArenaInfo game="+game+" server="+rs.getString("server")+" arenaName="+rs.getString("arenaName"));                                    
                
               // if (game==null) {
                //    Ostrov.log_err("loadArenaInfo1 - нет игры "+rs.getString("game"));
              //      continue;
              //  }
                
            /*    if (game.type==ServerType.ARENAS) {
                    
                   // gi = games.get(game);

                   // if (gi==null) {
                  //      gi = new GameInfo(game);
                  //      games.put( game, gi);
                 //   }
                
                    ArenaInfo ai = gi.getArena(rs.getString("server"), rs.getString("arenaName"));
                    if (ai==null) {
                        ai = new ArenaInfo(
                                gi, 
                                rs.getString("server"),
                                rs.getString("arenaName"), 
                                rs.getInt("level"),
                                rs.getInt("reputation"),
                                Material.matchMaterial(rs.getString("material"))
                        );
                        gi.arenas.put(ai.slot, ai);
                    }
//System.out.println("loadArenaInfo 4");                                    
                    gi.updateMulti(
                        rs.getString("server"),
                        rs.getString("arenaName"), 
                        GameState.fromString(rs.getString("state")), 
                        rs.getInt("players"),
                        rs.getString("line0"),
                        rs.getString("line1"),
                        rs.getString("line2"),
                        rs.getString("line3"),
                        rs.getString("extra")
                    );
                   /* ai.update(
                        GameState.fromString(rs.getString("state")), 
                        rs.getInt("players"),
                        rs.getString("line0"),
                        rs.getString("line1"),
                        rs.getString("line2"),
                        rs.getString("line3"),
                        rs.getString("extra")
                    );
                    //games.get(game).updateArena(rs.getString("name"), rs.getString("motd"), GameState.РАБОТАЕТ, rs.getInt("online"), "", "", "", "", "");//updatePlayerCount(rs.getInt("online"));
//System.out.println("loadArenaInfo 5");                                    

                }*/
                //games.get(game).updateArena(rs.getString("server"),// - есть в арене, но может быть одинаковая арена на разных серверах!
                //        rs.getString("arenaName"),
               //         GameState.fromString(rs.getString("state")),
               //         rs.getInt("players"),
               //         rs.getString("line0"),
               //         rs.getString("line1"),
               //         rs.getString("line2"),
                //        rs.getString("line3"),
                //        rs.getString("extra")
               // );

//Bukkit.broadcastMessage("SELECT "+rs.getString("арена"));                                    
            }

            rs.close();
            stmt.close();
//System.out.println("loadArenaInfo 6");                                    

            games.values().stream().forEach( (si) -> {
                si.updateIcon();
            });
//System.out.println("loadArenaInfo 7");                                    
                //main_inv.getViewers().stream().forEach( p -> {((Player)p).updateInventory();} );
            if (fromStamp==0) {
                int a=0;
                for (final GameInfo gi_ : games.values()) {
                    a+=gi_.arenas.size();
                }
                Ostrov.log_ok("§2SM - §7Загружены данные игр: §a"+games.size()+"§7, арен: §a"+a);
                //Ostrov.sync(() -> 
                //    Bukkit.getPluginManager().callEvent(new GameInfoLoadEvent(  ))
                //,0);
            }

        } catch (SQLException ex) { 
            
            Ostrov.log_err("§4SM Не удалось загрузить данные серверов! update_sinfo "+ex.getMessage());
            
        } finally {
            try {
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            } catch (SQLException ex) {
                Ostrov.log_err("§4SM Не удалось закрыть соединение! update_sinfo "+ex.getMessage());
            }
            if (fromStamp==0) {
                Ostrov.sync(() -> 
                    //Bukkit.getPluginManager().callEvent(new GameInfoLoadEvent(  ))
                    loadGameSign()
                ,0);
            }
        }


     }    
    
    
    
        
    
    
    
    
    
    
    


    public static Inventory getGameInventory(final Game game) {
        if (game==null) return null;
        return games.get(game).arena_inv;
    }
    
    //public static GameInfo getGameInfo(final String serverName) {
    //    return games.get(Game.fromServerName(serverName));
        //return servers.get(serverName.substring(0, 2)); - substring не катит, может быть arcaim daaria
   // }
    public static GameInfo getGameInfo(final Game game) {
        if (game==null) return null;
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
    
    public static Collection<GameInfo> getGames() {
        return games.values();
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
        
        if (thisServerGame.type==ServerType.ARENAS) {  //на миниигре вызываем локальные эвент для табличек этого сервера! (с банжи не получит)
            games.get(thisServerGame).update(this_server_name, arenaName, state, players, line0, line1, line2, line3, extra);
            //if (Bukkit.isPrimaryThread()) {
            //    Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent( thisServerGame, this_server_name, arenaName, state, players, line0, line1, line2, line3, extra));
           // } else {
            //    Ostrov.sync(()-> Bukkit.getPluginManager().callEvent(new GameInfoUpdateEvent( thisServerGame, this_server_name, arenaName, state, players, line0, line1, line2, line3, extra)) ,0);
           // }
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

            ApiOstrov.sendMessage(Operation.GAME_INFO_TO_BUNGEE, GM.this_server_name, state.tag, players, 0, arenaName, line0, line1, line2, line3, extra );
                    
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
            
        
            pst.setString(1, GM.this_server_name);
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
        
        if (Timer.has(p, "sm")) {
            return;
        }
        Timer.add(p, "sm", 1);
        
        String gameDisplayName=e.getView().getTitle().replaceFirst(main_inv_name, "").trim();
//System.out.println("Sm click gameDisplayName="+gameDisplayName);     

        if (gameDisplayName.isEmpty()) { //гл.меню игр 
            
            if (e.getSlot()==53) {
                p.closeInventory();
                return;
            }
            
            games.values().stream().forEach( (gameInfo) -> {
                if (gameInfo.game.menuSlot==e.getSlot()) {
                    
                    if (gameInfo.game.type==ServerType.ONE_GAME) {
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
//System.out.println("find game="+game+" equals?"+game.displayName.equals(gameDisplayName));     
                if (game.displayName.equals(gameDisplayName)) {
                    games.get(game).invClick(p, e.getSlot());
                    break;
                }
            }
            //if (games.containsKey(type)) {
            //    games.get(type).invClick(p, e.getSlot());
           // }
            
        }

    }



    



    

  

    
    
    
}
