package ru.komiss77.modules.games;

import java.sql.*;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
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
import org.jetbrains.annotations.Nullable;
import ru.komiss77.*;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.Operation;
import ru.komiss77.enums.ServerType;
import ru.komiss77.enums.Table;
import ru.komiss77.events.BsignLocalArenaClick;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.redis.RDS;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.utils.TCUtils;


//не переименовывать!!!! другие плагины берут напрямую!
public final class GM {   
    
    private static final OstrovConfig gameSigns;

    @Deprecated //на одном ядре может быть несколько игр, надо придумать что-то другое
    public static final Game GAME; //не передвигать! не переименовывать!!!! другие плагины берут напрямую!
    private static Component chatLogo; //не передвигать! не переименовывать!!!! другие плагины берут напрямую!
    private static final EnumMap<Game, GameInfo> games; //  game (аркаим даария bw01 bb01 sg02), gameInfo (арены)
    public static final HashMap<String,GameSign>signs;
    public static final int LOAD_INTERVAL; //секунды
    //динамические
    public static final Set<String> allBungeeServersName;
    public static int bungee_online=0;
  public static long tsServer, tsArena, tsLang = 0;

  public static State state = State.STARTUP;


    static {
        gameSigns = Config.manager.getNewConfig("gameSigns.yml", new String[]{"", "Ostrov77 gameSigns config file", ""} );
        gameSigns.saveConfig();
        GAME = Game.fromServerName(Ostrov.MOT_D);//Game.GLOBAL
        setLogo(GAME.defaultlogo);
        games=new EnumMap<>(Game.class);
        for (Game g : Game.values()) {
          if (g==Game.GLOBAL) continue;
          games.put(g, new GameInfo(g));
        }
        signs=new HashMap<>();
        allBungeeServersName=new HashSet<>();
        
        switch (GAME.type) {
            case ARENAS -> LOAD_INTERVAL=60; //на аренах раз в минуту прогрузить требования уровня и репутации!
            case LOBBY -> LOAD_INTERVAL=10;
            case ONE_GAME -> LOAD_INTERVAL=30;
            default -> LOAD_INTERVAL=60;
        }
    }
    public enum State {STARTUP, COMPLETE, RELOAD, ERROR};

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
    public static void load(final State st) {
        //games.clear();
        //for (Game g : Game.values()) {
        //  if (g==Game.GLOBAL) continue;
        //  games.put(g, new GameInfo(g));
        //}
        for (GameInfo gi : games.values()) {
          gi.clear();
        }
        signs.clear();
        tsServer = 0;//.setTime(0);
        tsArena = 0;//.setTime(0);
        state = st;//fromStamp = 0;
        loadArenaInfo(); // 2 !!
    }




    //useOstrovData и соединение чекать до вызова! 
    //c 0 прогрузит всё принудительно
    public static void loadArenaInfo() {   //запускается после загрузки в loadServersAndArenas

        PreparedStatement pst = null;
        ResultSet rs = null;
        //long curr;

        try  {
          pst = OstrovDB.getConnection().prepareStatement(" SELECT `name`,`motd`,`online`,`type`,`ts` FROM "+Table.BUNGEE_SERVERS.table_name+" WHERE  `ts` > '"+tsServer+"';");
          //pst.setLong(1, tsServer);

          rs = pst.executeQuery();//stmt.executeQuery( " SELECT `name`,`motd`,`online`,`type`  FROM "+Table.BUNGEE_SERVERS.table_name+" WHERE `stamp` >= "+fromStamp );

            ServerType type;
            Game game;
            GameInfo gi;

            //прогрузка больших из BUNGEE_SERVERS
            //в таблице банжи могут быть только одиночные и лобби
            while (rs.next()) {
                
                type = ServerType.fromString(rs.getString("type"));
                if (type!=ServerType.ONE_GAME && type!=ServerType.LOBBY) continue; //или getGameInfo ругается на REG и прочие

                game = Game.fromServerName(rs.getString("name"));
                gi = getGameInfo(game);

                //if (gi!=null) {

                 //   if (gi.game.type==ServerType.ONE_GAME) {
                        
                        gi.update(
                          rs.getString("name"),
                          rs.getString("motd"),
                          rs.getInt("online")>=0 ? GameState.РАБОТАЕТ : GameState.ВЫКЛЮЧЕНА,
                          rs.getInt("online"),
                          "", "", "", ""
                        );
                    
                  /*  } else if (gi.game.type==ServerType.LOBBY) {
                        
                        ArenaInfo ai = gi.getArena(rs.getString("name"), rs.getString("motd"));
                        if (ai==null) {
                            ai = new ArenaInfo(gi, rs.getString("name"), rs.getString("motd"), 0, 0, Material.matchMaterial(Game.LOBBY.mat));
                            gi.arenas.put(ai.slot, ai);
                        }
                        gi.update(
                          rs.getString("name"),
                          rs.getString("motd"),
                          rs.getInt("online")>=0 ? GameState.РАБОТАЕТ : GameState.ВЫКЛЮЧЕНА,
                          rs.getInt("online"),
                          "", "", "", ""
                        );

                    }*/
                //}
              if (rs.getLong("ts") > tsServer) {//if (ts.compareTo( rs.getTimestamp("stamp") )<0) {//if (fromStamp < curr) { //запоминаем наибольший последний апдейт - в след.раз прогрузим с него
                tsServer = rs.getLong("ts");
              }
            }
            rs.close();


          //прогрузка по аренам

          //pst = OstrovDB.getConnection().prepareStatement(" SELECT *  FROM `arenaData`");
       /*   pst = OstrovDB.getConnection().prepareStatement(" SELECT *  FROM `arenaData` WHERE  `ts` > '"+tsArena+"';");
         // pst.setLong(1, tsArena);

          rs = pst.executeQuery(); //stmt.executeQuery( " SELECT *  FROM `arenaData` WHERE  `stamp` > " );

          while (rs.next()) {
// boolean n = rs.getTimestamp("ts").getTime() > tsArena.getTime();
//Ostrov.log("-----g="+rs.getString("g")+" s="+rs.getString("s")+" a="+rs.getString("a")+" time="+rs.getTimestamp("ts").getTime()+" new?"+n);
//if (!n) continue;
            game = Game.fromServerName(rs.getString("g"));
            gi = getGameInfo(game);
            if (gi.game.type==ServerType.ARENAS) {

              ArenaInfo ai = gi.getArena(rs.getString("s"), rs.getString("a"));
              if (ai==null) {
                ai = new ArenaInfo(
                  gi,
                  rs.getString("s"),
                  rs.getString("a"),
                  rs.getInt("l"),
                  rs.getInt("r"),
                  Material.matchMaterial(rs.getString("m"))
                );
                gi.arenas.put(ai.slot, ai);
              } else { //аренаинфо могла быть создана при загрузке табличек - обновить данные
                ai.mat = Material.matchMaterial(rs.getString("m"));
                ai.level = rs.getInt("l");
                ai.reputation = rs.getInt("r");
              }

              //далее простая обнова
//Ostrov.log_warn("SQL update "+rs.getString("s")+":"+rs.getString("a")+" st="+rs.getString("st")+" stamp="+rs.getLong("ts"));
              gi.update(
                rs.getString("s"),
                rs.getString("a"),
                GameState.fromString(rs.getString("st")),
                rs.getInt("p"),
                rs.getString("l0"), rs.getString("l1"), rs.getString("l2"), rs.getString("l3")
              );

            }


//Ostrov.log_warn("old="+tsArena+" curr="+rs.getLong("ts"));
            if (rs.getLong("ts") > tsArena) {//if (ts.compareTo( rs.getTimestamp("stamp") )<0) {//if (fromStamp < curr) { //запоминаем наибольший последний апдейт - в след.раз прогрузим с него
              tsArena = rs.getLong("ts");
//Ostrov.log_warn("SET fromStamp="+ rs.getLong("ts"));
            }
          }


          rs.close();*/
//Ostrov.log_warn("..........................");



          pst = OstrovDB.getConnection().prepareStatement(" SELECT `rus`, `eng`, `ts`  FROM `lang` WHERE  `ts` > '"+tsLang+"';");
          //pst.setLong(1, tsLang);
            rs = pst.executeQuery();//( " SELECT `rus`, `eng`  FROM `lang` WHERE  `stamp` >= "+Lang.updateStamp );
            Lang.updateBase(rs);
            rs.close();

            pst.close();

            if (state == State.STARTUP || state == State.RELOAD) {//if (fromStamp==0) {
                int a=0;
                for (final GameInfo gi_ : games.values()) {
                    a+=gi_.count();
                }
                Ostrov.log_ok("§2GM - Загружены данные игр: "+games.size()+", арен: "+a);
              if (state == State.RELOAD) {//if (reload) {
                //reload = false;
                Ostrov.sync( () -> onWorldsLoadDone());
              }
            }
            state = State.COMPLETE;//fromStamp = ApiOstrov.currentTimeSec();

//Ostrov.log("fromStamp="+fromStamp);
        } catch (SQLException | NullPointerException | ArrayIndexOutOfBoundsException ex) {
            
            Ostrov.log_warn("§4GM Не удалось загрузить данные серверов! update_sinfo "+ex.getMessage());
            state = State.ERROR;//fromStamp = -1; //c -1 будет пытаться прогрузить по таймеру
            
        } finally {
          try {
            if (rs != null && !rs.isClosed()) {
              rs.close();
            }
            if (pst != null && !pst.isClosed()) {
              pst.close();
            }
          } catch (SQLException ex) {
            Ostrov.log_warn("§4GM Не удалось закрыть соединения : "+ex.getMessage());
          }
        }

     }


/*
CREATE TABLE `arenaData` (
  `id` int(11) NOT NULL,
  `g` varchar(16) NOT NULL,
  `s` varchar(16) NOT NULL,
  `a` varchar(24) NOT NULL,
  `st` varchar(24) NOT NULL DEFAULT 'НЕОПРЕДЕЛЕНО',
  `p` smallint(6) NOT NULL DEFAULT '0',
  `d` varchar(512) NOT NULL DEFAULT '',
  `l` smallint(6) NOT NULL DEFAULT '0',
  `r` tinyint(4) NOT NULL DEFAULT '-77',
  `m` varchar(32) NOT NULL DEFAULT 'BLACK_CONCRETE',
  `ts` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 */
  private static void writeArenaStateToMySql (final Game game, final String arenaName, final GameState state, final int players, final String line0, final String line1, final String line2, final String line3) {
//Ostrov.log("==writeArenaStateToMySql useOstrovData?"+OstrovDB.useOstrovData);
    if (!OstrovDB.useOstrovData) return;
    final Connection conn = OstrovDB.getConnection();
    if (conn==null) {
      Ostrov.log_warn("writeThisServerStateToOstrovDB - нет соединения с БД!");
      return;
    }
    final int id = game.ordinal() + Ostrov.MOT_D.hashCode() + arenaName.hashCode();

  /*  final StringBuffer sb = new StringBuffer("INSERT INTO `arenaData` (`id`, `g`,`s`,`a`, `ts`) VALUES ('")
      .append(id).append("','").append(game.name()).append("','").append(Ostrov.MOT_D).append("','").append(arenaName)
      .append("',NOW()+0) ON DUPLICATE KEY UPDATE st='").append(state.name()).append("',p='").append(players)
      .append("',l0='").append(line0).append("',l1='").append(line1).append("',l2='").append(line2).append("',l3='").append(line3)
      .append("', ts=NOW()+0; ");

    if (Ostrov.SHUT_DOWN) { //при выключении - синхронно
      if (OstrovDB.useOstrovData && OstrovDB.getConnection()!=null) {
          try  (PreparedStatement pst = OstrovDB.getConnection().prepareStatement(sb.toString())){
            pst.executeUpdate();
          } catch (SQLException e) {
            Ostrov.log_err("§cGM writeArenaStateToMySql error - " + e.getMessage());
          }
      }
    } else {
      OstrovDB.executePstAsync(Bukkit.getConsoleSender(), sb.toString());
    }

    if (!OstrovDB.useOstrovData) return;
    final Connection conn = OstrovDB.getConnection();
    if (conn==null) {
      Ostrov.log_warn("writeThisServerStateToOstrovDB - нет соединения с БД!");
      return;
    }*/

    try {
      //поле с для принудительной обновы, илил вернёт 0 если данные идентичны
      PreparedStatement pst = conn.prepareStatement("INSERT INTO "+Table.ARENAS.table_name
        +" (id, server, game, arenaName, state, line0, line1, line2, line3, players, stamp)"
        +" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ON DUPLICATE KEY UPDATE "
        + "state=VALUES(state), line0=VALUES(line0), line1=VALUES(line1), line2=VALUES(line2), line3=VALUES(line3),"
        + "players=VALUES(players), stamp=VALUES(stamp) ;");

      pst.setInt(1, (Ostrov.MOT_D+arenaName).hashCode());
      pst.setString(2, Ostrov.MOT_D);
      pst.setString(3, game.name());//Game.fromServerName(Ostrov.MOT_D).name());
      pst.setString(4, arenaName);
      pst.setString(5, state.toString());
      pst.setString(6, line0);
      pst.setString(7, line1);
      pst.setString(8, line2);
      pst.setString(9, line3);
      pst.setInt(10, players);
      pst.setInt(11, ApiOstrov.currentTimeSec()+1 ); //для  надёжности, пусть прогрузит 2 раза



    } catch (SQLException e) {
      Ostrov.log_err("§cGM writeArenaStateToMySql error - "+e.getMessage());
      //e.printStackTrace();
    }

  }





  //может быть async!!
  public static void sendArenaData (
    final Game game,
    final String arenaName,
    final GameState state,
    final int players,
    final String line0,
    final String line1,
    final String line2,
    final String line3
  ) {
    //if (!Bukkit.isPrimaryThread()) {
   //   Ostrov.log_warn("sendArenaData должен быть SYNC : §f"+arenaName+" : "+state);
   // }
//Ostrov.log("==sendArenaData "+arenaName+" : "+state+" SHUT_DOWN?"+Ostrov.SHUT_DOWN+" STARTUP?"+Ostrov.STARTUP);
    if (game.type==ServerType.ARENAS) {  //на миниигре вызываем локальные эвент для табличек этого сервера! (с банжи не получит)
      //если игры с острова не прогрузились, но локальные арены уже шлют данные -
      //создать локальныю запись, чтобы могли стартовать таблички
      GameInfo gi = games.get(game);
      if (gi==null) {
        gi = new GameInfo(game);
        games.put(game, gi);
      }
      gi.update(Ostrov.MOT_D, arenaName, state, players, line0, line1, line2, line3);
    }

    final StringBuffer sb = new StringBuffer(game.name()).append(LocalDB.W_SPLIT)
      .append(Ostrov.MOT_D).append(LocalDB.W_SPLIT)
      .append(arenaName).append(LocalDB.W_SPLIT)
      .append(state.name()).append(LocalDB.W_SPLIT)
      .append(players).append(LocalDB.W_SPLIT)
      .append(line0).append(LocalDB.W_SPLIT)
      .append(line1).append(LocalDB.W_SPLIT)
      .append(line2).append(LocalDB.W_SPLIT)
      .append(line3).append(" ").append(LocalDB.W_SPLIT)
      ;
    RDS.sendMessage("arenadata", sb.toString());

    if (Ostrov.SHUT_DOWN) {

      writeArenaStateToMySql(game, arenaName, state, players, line0, line1, line2, line3);

    } else if (Ostrov.STARTUP) {
      writeArenaStateToMySql(game, arenaName, state, players, line0, line1, line2, line3);
      //Ostrov.async( () -> writeArenaStateToMySql(game, arenaName, state, players, line0, line1, line2, line3), 20);

    } else if (Bukkit.getOnlinePlayers().isEmpty()) {
      writeArenaStateToMySql(game, arenaName, state, players, line0, line1, line2, line3);
      //Ostrov.async( () -> writeArenaStateToMySql(game, arenaName, state, players, line0, line1, line2, line3), 0);

    } else {
//Ostrov.log("SpigotChanellMsg.sendMessage");
     // SpigotChanellMsg.sendMessage(Bukkit.getOnlinePlayers().stream().findAny().get(),
     //   Operation.GAME_INFO_TO_BUNGEE,
     //   Ostrov.MOT_D,
     //   state.tag, players, 0,
    //    arenaName, line0, line1, line2, line3, game.name() );

    }


  }




  public static GameInfo getGameInfo(final Game game) {
    if (game==null) return null;
    return games.get(game);
  }

  public static Collection<GameInfo> getGames() {
    return games.values();
  }




  public static void randomPlay(final Player p, final Game game, @Nullable final String serverName) {
    if (Timer.has(p, "randomPlay")) return;
    Timer.add(p, "randomPlay", 2);

    final GameInfo gi = getGameInfo(game);
    String serv = game.defaultServer;
    String arena = "";

    if (gi == null) {
      p.sendMessage("§cНет данных для игры "+game.displayName+"§r§c, пробуем подключиться наугад...");
      serv = game.defaultServer;
    } else {
      if (gi.count()==0) {
        p.sendMessage("§cНе найдено арен для игры " + game.displayName + "§r§c, пробуем подключиться наугад...");
      } else {
        ArenaInfo arenaInfo = null;
        int max = -1;
        for (ArenaInfo ai : gi.arenas()) {
          if (serverName != null && !ai.server.equalsIgnoreCase(serverName)) continue;
          if (ai.state == GameState.СТАРТ) {
            arenaInfo = ai;
            break;
          }
          if (ai.state == GameState.ОЖИДАНИЕ || ai.state == GameState.РАБОТАЕТ) {
            if (ai.players > max) {
              max = ai.players;
              arenaInfo = ai;
            }
          }
        }
        if (arenaInfo == null) {
          //p.sendMessage("§cНе найдено арены, подходящей для быстрой игры, попробуйте найти на табличке!");
          arenaInfo = gi.arenas().stream().findAny().get();
          arena = arenaInfo.arenaName;
        }
      }
    }

    if (serv.equalsIgnoreCase(Ostrov.MOT_D)) {
      if (!arena.isEmpty()) Bukkit.getPluginManager().callEvent(new BsignLocalArenaClick( p, arena) );
    } else {
      ApiOstrov.sendToServer(p, serv, arena);
      //p.performCommand("server "+arenaInfo.server+" "+arenaInfo.arenaName);
    }

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




    public static void onWorldsLoadDone() {
        signs.clear();

        if (gameSigns.getConfigurationSection("signs") !=null)   {
            GameInfo gi;
            Game game;
            String gameName, serverName;

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

                gameName = gameSigns.getString("signs."+loc_string+".game", "");
                serverName = gameSigns.getString("signs."+loc_string+".server");
                if (gameName.isEmpty()) { //фикс для старых значений
                  game = Game.fromServerName(serverName);
                } else {
                  game = Game.fromServerName(gameName);
                }

                if (game == Game.GLOBAL) {
                  Ostrov.log_err("loadGameSign -> Не удалось определить игру для таблички "+ loc_string+", gameName="+gameName+", serverName="+serverName);
                  continue;
                }

                gi = getGameInfo(game);//games.get(Game.fromServerName(serverName));
                if (gi==null) {
                    Ostrov.log_err("loadGameSign -> Нет GameInfo для игры "+game+", табличка "+ loc_string);
                    continue;
                }

                String arenaName = "";
                ArenaInfo ai = null;

                if ( gi.game.type==ServerType.ONE_GAME ) {

                  ai = gi.getArena(serverName, arenaName);//ai = gi.arenas.get(gi.game);
                    if (ai==null) { //по идее, для больших создаётся нулевая арена автоматом в new GameInfo. Но для перестраховки проверяем.
                        Ostrov.log_err("loadGameSign -> Нет ArenaInfo для игры "+game+", табличка "+ loc_string);
                    }

                } else if ( gi.game.type==ServerType.ARENAS || gi.game.type==ServerType.LOBBY ) {

                    arenaName = gameSigns.getString("signs."+loc_string+".arena", "");
                    if (arenaName.isEmpty()) {
                        Ostrov.log_err("loadGameSign -> тип сервера ="+gi.game.type+", но аренна не указана; табличка "+ loc_string);
                        continue;
                    }

                    ai = gi.getArena(serverName, arenaName);
                }

                signs.put(loc_string, new GameSign(loc, game, serverName, arenaName));
                if (ai!=null) {
                    ai.signs.add(loc_string);
                    updateSigns(ai);
                }


            }
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
              ai = gi.getArena(gameSign.server, gameSign.arena);//ai = gi.arenas.get(0);
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

    
    public static void addGameSign (final Player p, final Sign sign, final Game game, final String serverName, final String arenaName) {
        p.closeInventory();
        final String locAsString = LocationUtil.toString(sign.getBlock().getLocation());

        GM.signs.put( locAsString, new GameSign(sign.getBlock().getLocation(), game, serverName, arenaName));

        //добав в инфоб обновить
        //final Game game = Game.fromServerName(serverName);
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
      GM.gameSigns.set("signs."+locAsString+".game", game.name());
      GM.gameSigns.set("signs."+locAsString+".server", serverName);
        GM.gameSigns.set("signs."+locAsString+".arena", arenaName);
        GM.gameSigns.saveConfig();

        if (arenaName.isEmpty()) {
            p.sendMessage( "§6табличка для сервера §b" +  serverName+"§6 создана на локации "+ locAsString );
        } else {
            p.sendMessage( "§6табличка для сервера §b" +  serverName +" §6и арены §b"+arenaName+"§6 создана на локации "+ locAsString );
        }
        
    }


    public static ArenaInfo lookup(final String serverName, final String arenaMane) {
      for (GameInfo gi : games.values()) {
        for (ArenaInfo ai : gi.arenas()) {
          if (serverName.isEmpty()) {
            if (ai.arenaName.equalsIgnoreCase(arenaMane)) {
              return ai;
            }
          } else {
            if (ai.server.equalsIgnoreCase(serverName) && ai.arenaName.equalsIgnoreCase(arenaMane)) {
              return ai;
            }
          }

        }
      }
      return null;
    }


}




   /* if (Bukkit.getOnlinePlayers().isEmpty()) { //async

      if (Ostrov.SHUT_DOWN || Ostrov.STARTUP) { //state==GameState.ВЫКЛЮЧЕНА || state==GameState.ПЕРЕЗАПУСК) { //sync!!

        if (Bukkit.isPrimaryThread()) {
          writeArenaStateToMySql(game, arenaName, state, players, line0, line1, line2, line3);
        } else {
         // Ostrov.sync( ()-> writeArenaStateToMySql(game, arenaName, state, players, line0, line1, line2, line3), 0);
        }

      } else {

        if (Bukkit.isPrimaryThread()) {
          Ostrov.async(() -> writeArenaStateToMySql(game, arenaName, state, players, line0, line1, line2, line3), 0);
        } else {
          writeArenaStateToMySql(game, arenaName, state, players, line0, line1, line2, line3);
        }
      }

    } else {

      SpigotChanellMsg.sendMessage(Bukkit.getOnlinePlayers().stream().findAny().get(),
        Operation.GAME_INFO_TO_BUNGEE,
        Ostrov.MOT_D,
        state.tag, players, 0,
        arenaName, line0, line1, line2, line3, game.name() );

    }*/



   /* if (!OstrovDB.useOstrovData) return;
    final Connection conn = OstrovDB.getConnection();
    if (conn==null) {
      Ostrov.log_warn("writeThisServerStateToOstrovDB - нет соединения с БД!");
      return;
    }

    try {

      final int id = game.ordinal() + Ostrov.MOT_D.hashCode() + arenaName.hashCode();

      //поле с для принудительной обновы, илил вернёт 0 если данные идентичны
      final StringBuffer sb = new StringBuffer("UPDATE `arenaData` SET c=c+1, st='").append(state.name())
      .append("', d='").append(line0).append(LocalDB.W_SPLIT).append(line1).append(LocalDB.W_SPLIT).append(line2).append(LocalDB.W_SPLIT).append(line3)
      .append("', p='").append(players)
      .append("' WHERE id='").append(id).append("'; ");

      PreparedStatement pst = conn.prepareStatement(sb.toString());

      int res = pst.executeUpdate();

      if (res==0) { //0 может вернуть если данные по факту не изменились, поэтому 1 раз может накидать duplicateException
        pst = conn.prepareStatement("INSERT INTO `arenaData` (`id`, `g`, `s`, `a`, `p`, `d`) VALUES ( ?, ?, ?, ?, ?, ?) ;");
        pst.setInt(1, id);
        pst.setString(2, game.name());//Game.fromServerName(Ostrov.MOT_D).name());
        pst.setString(3, Ostrov.MOT_D);
        pst.setString(4, arenaName);
        pst.setInt(5, players);
        pst.setString(6, line0+LocalDB.WORD_SPLIT+line1+LocalDB.WORD_SPLIT+line2+LocalDB.WORD_SPLIT+line3);
      }

      pst.executeUpdate();

      pst.close();
      //final int hash = (game.name()+Ostrov.MOT_D+arenaName).hashCode();
      //PreparedStatement pst = conn.prepareStatement("UPDATE `arenaData` SET count=count=1, st='"
      //  +state.name()+"', d='"+line0+LocalDB.WORD_SPLIT+line1+LocalDB.WORD_SPLIT+line2+LocalDB.WORD_SPLIT+line3+"', p='"+String.valueOf(players)
      //  +"' WHERE g='"+game.name()+"' AND  s='"+Ostrov.MOT_D+"' AND  a='"+arenaName+"'; ");


      /*PreparedStatement pst = conn.prepareStatement("INSERT INTO "+Table.ARENAS.table_name
        +" (id, server, game, arenaName, state, line0, line1, line2, line3, players, stamp)"
        +" VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ON DUPLICATE KEY UPDATE "
        + "state=VALUES(state), line0=VALUES(line0), line1=VALUES(line1), line2=VALUES(line2), line3=VALUES(line3),"
        + "players=VALUES(players), stamp=VALUES(stamp) ;");

      pst.setInt(1, (Ostrov.MOT_D+arenaName).hashCode());
      pst.setString(2, Ostrov.MOT_D);
      pst.setString(3, game.name());//Game.fromServerName(Ostrov.MOT_D).name());
      pst.setString(4, arenaName);
      pst.setString(5, state.toString());
      pst.setString(6, line0);
      pst.setString(7, line1);
      pst.setString(8, line2);
      pst.setString(9, line3);
      pst.setInt(10, players);
      pst.setInt(11, ApiOstrov.currentTimeSec()+1 ); //для  надёжности, пусть прогрузит 2 раза



    } catch (SQLException e) {
      Ostrov.log_err("§cGM writeArenaStateToMySql error - "+e.getMessage());
      //e.printStackTrace();
    }*/

           /* rs = stmt.executeQuery( " SELECT *  FROM "+Table.ARENAS.table_name+" WHERE  `stamp` >= "+fromStamp );

            while (rs.next()) {
                game = Game.fromServerName(rs.getString("game"));
                gi = getGameInfo(game);
//Ostrov.log("----- "+rs.getString("game")+" game="+game+" gi="+gi);
                //if (gi!=null) {
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

                        //далее простая обнова
                        gi.update(
                            rs.getString("server"),
                            rs.getString("arenaName"),
                            GameState.fromString(rs.getString("state")),
                            rs.getInt("players"),
                            rs.getString("line0"),
                            rs.getString("line1"),
                            rs.getString("line2"),
                            rs.getString("line3")
                        );

                    }
                //}

            }

            rs.close();*/
