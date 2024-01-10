package ru.komiss77.modules.games;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.Operation;
import ru.komiss77.enums.ServerType;
import ru.komiss77.enums.Table;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.utils.TCUtils;



//не переименовывать!!!! другие плагины берут напрямую!
public final class GM {   
    
    private static final OstrovConfig gameSigns;
    public static final Game GAME; //не передвигать! не переименовывать!!!! другие плагины берут напрямую!
    private static Component chatLogo; //не передвигать! не переименовывать!!!! другие плагины берут напрямую!
    private static final EnumMap<Game, GameInfo> games; //  game (аркаим даария bw01 bb01 sg02), gameInfo (арены)
    public static final HashMap<String,GameSign>signs;
    public static final int LOAD_INTERVAL; //секунды
    //динамические
    public static final Set<String> allBungeeServersName;
    public static int bungee_online=0;
    public static int fromStamp; //при первом чтении вычитает все арены, дальше только обновы статусов
        
    
    static {
        gameSigns = Config.manager.getNewConfig("gameSigns.yml", new String[]{"", "Ostrov77 gameSigns config file", ""} );
        gameSigns.saveConfig();
        GAME = Game.fromServerName(Ostrov.MOT_D);//Game.GLOBAL
        setLogo(GAME.defaultlogo);
        games=new EnumMap<>(Game.class);
        signs=new HashMap<>();
        allBungeeServersName=new HashSet<>();
        
        switch (GAME.type) {
            case ARENAS -> LOAD_INTERVAL=60; //на аренах раз в минуту прогрузить требования уровня и репутации!
            case LOBBY -> LOAD_INTERVAL=3;
            case ONE_GAME -> LOAD_INTERVAL=10;
            default -> LOAD_INTERVAL=60;
        }
    }
    
    public static void setLogo(final String logo) {
        chatLogo = Component.text()
            .append(TCUtils.format(logo))
            .hoverEvent(HoverEvent.showText(Component.text("Клик - перейти на сервер")))
            .clickEvent(ClickEvent.suggestCommand("/server "+Ostrov.MOT_D))
            .build();
    }
    
    public static Component getLogo() {
        return chatLogo;
    }
    
    //-из OstrovDB.init() после getBungeeServerInfo
    //-OreloadCmd
    //Вызывать async!!!
    public static void reload() {
        games.clear();
        signs.clear();
        loadArenaInfo(); // 2 !!
    }




    //useOstrovData и соединение чекать до вызова! 
    //c 0 прогрузит всё принудительно
    public static void loadArenaInfo() {   //запускается после загрузки в loadServersAndArenas
//System.out.println(" ++++++++++++++++++ loadArenaInfo fromStamp="+fromStamp);                                    
        
        //Statement stmt=null;
        ResultSet rs = null;
        try  (Statement stmt = OstrovDB.getConnection().createStatement()){
            //stmt = OstrovDB.getConnection().createStatement(); 
//System.out.println(" SELECT `сервер`,`игроки`  FROM "+Table.GAMES_MAIN.table_name+" WHERE `тип` LIKE '"+GameType.SINGLE.toString()+"' AND `штамп` > "+last_check);
            rs = stmt.executeQuery( " SELECT `name`,`motd`,`online`,`type`  FROM "+Table.BUNGEE_SERVERS.table_name+" WHERE `stamp` >= "+fromStamp ); 
//System.out.println("loadArenaInfo 1");                                    
            
            ServerType type;
            GameInfo gi;
            //прогрузка больших из BUNGEE_SERVERS
            //в таблице банжи могут быть только одиночные и лобби
            while (rs.next()) {
                
                type = ServerType.fromString(rs.getString("type"));
                if (type!=ServerType.ONE_GAME && type!=ServerType.LOBBY) continue; //или getGameInfo ругается на REG и прочие
                
                gi = getGameInfo(rs.getString("name"));
//Ostrov.log("");
                if (gi!=null) {
//System.out.println("loadArenaInfo 1 gi="+gi.game);
                    
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

            }
            rs.close();

//System.out.println("loadArenaInfo 2");                                    
            rs = stmt.executeQuery( " SELECT *  FROM "+Table.ARENAS.table_name+" WHERE  `stamp` >= "+fromStamp ); 
//System.out.println("loadArenaInfo 3"); 
            while (rs.next()) {
                
                gi = getGameInfo(rs.getString("server"));
//if (gi==null) System.out.println("--------loadArenaInfo gi=null!!"+rs.getString("server")+" from="+fromStamp);
                
                if (gi!=null) {
//System.out.println("loadArenaInfo 1 gi="+gi.game+">>"+rs.getString("server")+" from="+fromStamp);                    
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
                        } else { //аренаинфо могла быть создана при загрузке табличек - обновить данные
                            ai.mat = Material.matchMaterial(rs.getString("material"));
                            ai.level = rs.getInt("level");
                            ai.reputation = rs.getInt("reputation");
                        }
//System.out.println("loadArenaInfo 2 gi="+gi.game+" ai="+ai.arenaName+" from="+fromStamp);

                        //далее простая обнова
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
                
            }

            rs.close();
            
            
            rs = stmt.executeQuery( " SELECT `rus`, `eng`  FROM `lang` WHERE  `stamp` >= "+Lang.updateStamp );
            Lang.updateBase(rs);
            rs.close();
            
            //stmt.close();
//System.out.println("loadArenaInfo 6");                                    

            if (fromStamp==0) {
                int a=0;
                for (final GameInfo gi_ : games.values()) {
                    a+=gi_.arenas.size();
                }
                Ostrov.log_ok("§2GM - Загружены данные игр: "+games.size()+", арен: "+a);
            }
            fromStamp = ApiOstrov.currentTimeSec();
            
        } catch (SQLException ex) { 
            
            Ostrov.log_warn("§4GM Не удалось загрузить данные серверов! update_sinfo "+ex.getMessage());
            fromStamp = -1; //c -1 будет пытаться прогрузить по таймеру
            
        } /*finally {
            try {
                if (rs!=null) rs.close();
                //if (stmt!=null) stmt.close();
            } catch (SQLException ex) {
                Ostrov.log_warn("§4GM Не удалось закрыть соединение! update_sinfo "+ex.getMessage());
            }
        }*/


     }    
    
    
    
        
    
    
    
    
    
    
    
    
    
    
    
    

    static void updateSigns(final ArenaInfo ai) {
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
                    final SignSide ssd = sign.getSide(Side.FRONT);
                    ssd.line(0, TCUtils.format(ai.line0));
                    ssd.line(1, TCUtils.format(ai.line1));
                    ssd.line(2, TCUtils.format(ai.line2));
                    ssd.line(3, TCUtils.format(ai.line3));
                    sign.update();
                }
                if (gs.attachement_loc!=null) {
                    final BlockData bd = TCUtils.changeColor(gs.attachement_mat, ai.state.attachedColor).createBlockData();
                    for (final Player p : gs.attachement_loc.getWorld().getPlayers()) {
                        p.sendBlockChange(gs.attachement_loc, bd);
                    }
                }
            }
        }
    }    




    public static void OnWorldsLoadDone () {
//System.out.println("---------------loadGameSign");
//System.out.println("---------------loadGameSign games="+games.keySet());
        signs.clear();

        if (gameSigns.getConfigurationSection("signs") !=null)   {
            GameInfo gi;
            String serverName;

            for (String loc_string : gameSigns.getConfigurationSection("signs").getKeys(false)) {

                if (Ostrov.MOT_D.length()==4 && !gameSigns.getString("signs."+loc_string+".server").equals(Ostrov.MOT_D)) {
                    Ostrov.log_warn("в локальном режиме эта табличка работать не будет: "+loc_string);
                    continue;
                }

                final Location loc=ApiOstrov.locFromString(loc_string);
                if (loc==null) {
                    Ostrov.log_err("loadGameSign -> Нет такой локации: "+loc_string+" для таблички "+gameSigns.getString("signs." + loc_string));
                    continue;
                } 

                if (!Tag.SIGNS.isTagged(loc.getBlock().getType()) && !Tag.STANDING_SIGNS.isTagged(loc.getBlock().getType())) {
                    Ostrov.log_err("loadGameSign -> На локации не табличка: "+loc_string);
                    continue;
                }

                serverName = gameSigns.getString("signs."+loc_string+".server");
                gi = getGameInfo(serverName);//games.get(Game.fromServerName(serverName));
                if (gi==null) {
                    Ostrov.log_err("loadGameSign -> Нет игры для сервера "+serverName+", табличка "+ loc_string);
                    continue;
                }

                String arenaName = "";
                ArenaInfo ai = null;

                if ( gi.game.type==ServerType.ONE_GAME ) {

                    ai = gi.arenas.get(0);
                    if (ai==null) { //по идее, для больших создаётся нулевая арена автоматом в new GameInfo. Но для перестраховки проверяем.
                        Ostrov.log_err("loadGameSign -> Нет ArenaInfo для сервера "+serverName+", табличка "+ loc_string);
                    }

                } else if ( gi.game.type==ServerType.ARENAS || gi.game.type==ServerType.LOBBY ) {

                    arenaName = gameSigns.getString("signs."+loc_string+".arena", "");
                    if (arenaName.isEmpty()) {
                        Ostrov.log_err("loadGameSign -> тип сервера "+serverName+"="+gi.game.type+", но аренна не указана; табличка "+ loc_string);
                        continue;
                    }

                    ai = gi.getArena(serverName, arenaName);
//System.out.println("Arenas:");
//gi.getArenaNames().forEach(System.out::print);
                }

                signs.put(loc_string, new GameSign(loc, serverName, arenaName));
                if (ai!=null) {
                    ai.signs.add(loc_string);
                    updateSigns(ai);
                }

//System.out.println("createSign "+loc_string+" ai="+ai.server+":"+ai.arenaName+" line0="+ai.line0);

            }
        }
    }



  //  static class GmListener implements Listener {   


        // ================== Таблички ====================
        //вызывается из таймера каждую секунду пока один раз не вернёт true
        //прогружать из loadArenaInfo не получится - может не быть соединения с БД
        //в games что-то будет или после загрузки из БД, или если локальная игра отправида данные арен
        //public static void loadGameSign() {
      //  @EventHandler (priority = EventPriority.HIGHEST)
        //public static void OnWorldLoadDone (final WorldsLoadCompleteEvent e) {
       //     GM.OnWorldsLoadDone();
      //  }

        
        

     /*   @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onSignCreate(final SignChangeEvent e) {

            if (TCUtils.toString(e.line(0)).equalsIgnoreCase("bs")) {
                final Player p = e.getPlayer();

                if (!ApiOstrov.isLocalBuilder(p, true)) return;

                final String serverName = TCUtils.toString(e.line(1));
                if (serverName.isEmpty()) {
                    e.line(3, TCUtils.format("§4Ошибка!"));
                    p.sendMessage( "§4Строка 2 - сервер " );
                    return;
                }

                final Game game = Game.fromServerName(serverName);
                final GameInfo gi = games.get(game);
                if (gi==null) {
                    e.line(3, TCUtils.format("§4Ошибка!"));
                    p.sendMessage( "§4Нет игры для сервера "+serverName );
                    return;
                }

                if (!allBungeeServersName.contains(serverName)) {
                    e.line(3, TCUtils.format("§4Ошибка!"));
                    p.sendMessage( "§4строка 2 - §fсервер. Доступные:" );
                    p.sendMessage( "§e"+ApiOstrov.listToString(allBungeeServersName, ",") );
                    return;
                }


                final String arenaName = TCUtils.toString(e.line(2));
                if (game.type==ServerType.ARENAS && arenaName.isEmpty()) {
                    e.line(3, TCUtils.format("§4Ошибка!"));
                    p.sendMessage( "§аДля сервера с аренами §bстрока 2 §f- название арены с учётом регистра." ); 
                    p.sendMessage( "§аНайдены арены для сервера "+serverName+" :" ); 
                    p.sendMessage( "§e"+ApiOstrov.listToString(gi.getArenaNames(serverName), ",") );
                    return;
                }


                final String locAsString = ApiOstrov.stringFromLoc(e.getBlock().getLocation());

                signs.put( locAsString, new GameSign(e.getBlock().getLocation(), serverName, arenaName));
                //добав в инфоб обновить
                final ArenaInfo ai = gi.getArena(serverName, arenaName);
                if (ai!=null) {
                    ai.signs.add(locAsString);
                    e.line(0, TCUtils.format(ai.line0));
                    e.line(1, TCUtils.format(ai.line1));
                    e.line(2, TCUtils.format(ai.line2));
                    e.line(3, TCUtils.format(ai.line3));
                }
                gameSigns.set("signs."+locAsString+".server", serverName);
                gameSigns.set("signs."+locAsString+".arena", arenaName);
                gameSigns.saveConfig();

                if (arenaName.isEmpty()) {
                    e.getPlayer().sendMessage( "§6табличка для сервера §b" +  serverName+"§6 создана на локации "+ locAsString );
                } else {
                    e.getPlayer().sendMessage( "§6табличка для сервера §b" +  serverName +" §6и арены §b"+arenaName+"§6 создана на локации "+ locAsString );
                }

            }
        }*/        


        // ======================================




   /// }















    
    






    
    private static GameInfo getGameInfo(final String serverName) {
        final Game game = Game.fromServerName(serverName);
        
        if (game==null || game==Game.GLOBAL) {
            Ostrov.log_warn("GameManager onGameData : нет игры для сервера "+serverName);
            return null;
        }
        
        if (!games.containsKey(game)) {
            switch (game.type) {

                case ONE_GAME, LOBBY, ARENAS -> games.put(game, new GameInfo(game ));
                default -> {
                    return null;
                }
            }

        }
        return games.get(game);
        //gi.update(serverName, arenaName, state, players, line0, line1, line2, line3, extra, mat);
        
    }
    

    
    
    
    


    public static GameInfo getGameInfo(final Game game) {
        if (game==null) return null;
        return games.get(game);
        //return servers.get(serverName.substring(0, 2)); - substring не катит, может быть arcaim daaria
    }
    

    
    public static Collection<ArenaInfo> getArenas(final Game game) { //аркаим даария bw01 bb01 sg02
        if (games.containsKey(game)) return games.get(game).arenas.values();
        else return Collections.emptyList();//new ArrayList<>();
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

    //public static boolean hasGames() {
    //    return !games.isEmpty();
    //}


    
    
    
    
    
    
    
    
    
    
    
    
    
     
    
    
    
    
    
    
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
        
        if (GAME.type==ServerType.ARENAS) {  //на миниигре вызываем локальные эвент для табличек этого сервера! (с банжи не получит)
            //если игры с острова не прогрузились, но локальные арены уже шлют данные -
            //создать локальныю запись, чтобы могли стартовать таблички
            if (!games.containsKey(GAME)) {
                games.put(GAME, new GameInfo(GAME));
            }
            games.get(GAME).update(Ostrov.MOT_D, arenaName, state, players, line0, line1, line2, line3, extra);
            //эвент GameInfoUpdateEvent вызывается м GameInfo
        }
        
        if (Bukkit.getOnlinePlayers().isEmpty() || state==GameState.ОЖИДАНИЕ) { //async
            //плодит соединения!! передел на простой запрос
            if (Bukkit.isPrimaryThread()) {
                Ostrov.async(()-> writeArenaStateToMySql(arenaName, state, players, line0, line1, line2, line3, extra), 0);
            } else {
                writeArenaStateToMySql(arenaName, state, players, line0, line1, line2, line3, extra);
            }
            
        } else if (state==GameState.ВЫКЛЮЧЕНА || state==GameState.ПЕРЕЗАПУСК) { //sync!!
            
            if (Bukkit.isPrimaryThread()) {
                writeArenaStateToMySql(arenaName, state, players, line0, line1, line2, line3, extra);
            } else {
                Ostrov.sync( ()-> writeArenaStateToMySql(arenaName, state, players, line0, line1, line2, line3, extra), 0);
            }
            
        } else {

            SpigotChanellMsg.sendMessage(Bukkit.getOnlinePlayers().stream().findAny().get(), Operation.GAME_INFO_TO_BUNGEE, Ostrov.MOT_D, state.tag, players, 0, arenaName, line0, line1, line2, line3, extra );
            
        }
    } 
  
    
    private static void writeArenaStateToMySql (final String arenaName, final GameState state, final int players, final String line0, final String line1, final String line2, final String line3, final String extra ) {
        if (!OstrovDB.useOstrovData) return;
        final Connection conn = OstrovDB.getConnection();
        if (conn==null) {
            Ostrov.log_warn("writeThisServerStateToOstrovDB - нет соединения с БД!");
            return;
        }
//UPDATE `arenas` SET `line0` = 'sd' WHERE `arenaName` = 'vvv'; 
//записывать только изменённые! запрос формировать как??
        try {

            //PreparedStatement pst = conn.prepareStatement("UPDATE "+Table.ARENAS.table_name+" SET `state`='"+String.valueOf(state)+"', `line0`='"+line0+"', `line1`='"+line1+"', `line2`='"+line2+"', `line3`='"+line3+"', `extra`='"+extra+"', `players`='"+players+"' WHERE `id`='"+arenaName+":"+this_server_name+"'; ");
            PreparedStatement pst = conn.prepareStatement("INSERT INTO "+Table.ARENAS.table_name
                +" (id, server, game, arenaName, state, line0, line1, line2, line3, extra, players, stamp)"
                +" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ON DUPLICATE KEY UPDATE "
                + "state=VALUES(state), line0=VALUES(line0), line1=VALUES(line1), line2=VALUES(line2), line3=VALUES(line3), extra=VALUES(extra), "
                + "players=VALUES(players), stamp=VALUES(stamp) ;");

            pst.setInt(1, (Ostrov.MOT_D+arenaName).hashCode());
            pst.setString(2, Ostrov.MOT_D);
            pst.setString(3, Game.fromServerName(Ostrov.MOT_D).name());
            pst.setString(4, arenaName);
            pst.setString(5, state.toString());
            pst.setString(6, line0);
            pst.setString(7, line1);
            pst.setString(8, line2);
            pst.setString(9, line3);
            pst.setString(10, extra);
            pst.setInt(11, players);
            pst.setInt(12, ApiOstrov.currentTimeSec()+1 ); //для  надёжности, пусть прогрузит 2 раза

            pst.executeUpdate();
            
            
//System.out.println("----------------- writeArenaStateToMySql res="+pst.executeUpdate());   
                                
                                
            //int res = pst.executeUpdate();
            pst.close();
            
           /* if (res==0) {
                pst = conn.prepareStatement("INSERT INTO "+Table.ARENAS.table_name
                +" (`id`,`server`,`game`,`arenaName`,`state`,`line0`,`line1`,`line2`,`line3`,`extra`,`players`,`stamp` ) VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) " );
            
        
            pst.setString(1, arenaName+":"+this_server_name);
            pst.setString(2, GM.this_server_name);
            pst.setString(3, Game.fromServerName(this_server_name).name());
            pst.setString(4, arenaName);
            pst.setString(5, state.toString());
            pst.setString(6, line0);
            pst.setString(7, line1);
            pst.setString(8, line2);
            pst.setString(9, line3);
            pst.setString(10, extra);
            pst.setInt(11, players);
            pst.setInt(12, ApiOstrov.currentTimeSec()+1 ); //для  надёжности, пусть прогрузит 2 раза

            pst.executeUpdate();
            pst.close();   
        }*/


        } catch (SQLException e) {
            Ostrov.log_err("§cSM writeArenaStateToMySql error - "+e.getMessage());
            //e.printStackTrace();
        }
    }


    public static void deleteGameSign(final Player p, final String locAsString) {
        final GameSign gameSign = GM.signs.remove(locAsString);  
        GM.gameSigns.set("signs." + locAsString, null);
        GM.gameSigns.saveConfig(); 
        p.sendMessage("§6табличка для §b"+ gameSign.server+" : " + gameSign.arena+" §4удалена!");
        final GameInfo gi = GM.getGameInfo(Game.fromServerName(gameSign.server));
        if (gi!=null) {
            ArenaInfo ai = null;
            if ( gi.game.type==ServerType.ONE_GAME ) {
                ai = gi.arenas.get(0);
                //gi.arenas.get(0).signs.remove(locAsString);
            } else if ( gi.game.type==ServerType.ARENAS || gi.game.type==ServerType.LOBBY ) {
                ai = gi.getArena(gameSign.server, gameSign.arena);
                //gi.getArena(gameSign.server, gameSign.arena).signs.remove(locAsString);
            }
            if (ai!=null ) {
                ai.signs.remove(locAsString);
            }
        }
    }

    
    public static void addGameSign (final Player p, final Sign sign, final String serverName, final String arenaName) {
        p.closeInventory();
        final String locAsString = LocationUtil.toString(sign.getBlock().getLocation());

        GM.signs.put( locAsString, new GameSign(sign.getBlock().getLocation(), serverName, arenaName));

        //добав в инфоб обновить
        final Game game = Game.fromServerName(serverName);
        final GameInfo gi = GM.getGameInfo(game);
        final ArenaInfo ai = gi.getArena(serverName, arenaName);
        if (ai!=null) {
            ai.signs.add(locAsString);
            final SignSide ss = sign.getSide(Side.FRONT);
            ss.line(0, TCUtils.format(ai.line0));
            ss.line(1, TCUtils.format(ai.line1));
            ss.line(2, TCUtils.format(ai.line2));
            ss.line(3, TCUtils.format(ai.line3));
            sign.update();
        }
        GM.gameSigns.set("signs."+locAsString+".server", serverName);
        GM.gameSigns.set("signs."+locAsString+".arena", arenaName);
        GM.gameSigns.saveConfig();

        if (arenaName.isEmpty()) {
            p.sendMessage( "§6табличка для сервера §b" +  serverName+"§6 создана на локации "+ locAsString );
        } else {
            p.sendMessage( "§6табличка для сервера §b" +  serverName +" §6и арены §b"+arenaName+"§6 создана на локации "+ locAsString );
        }
        
    }
    
    
    


  
    
    
      
    
    
    
    
    
    


















    
    
    
    
    
    
    
    
    

  

    
    
    
}
