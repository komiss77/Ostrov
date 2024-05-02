package ru.komiss77;

import java.sql.ResultSetMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.enums.Table;
import ru.komiss77.modules.games.ArenaInfo;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.version.Nms;


public class OstrovDB {

    public static boolean useOstrovData=false;
    public static boolean ready = false; //чтобы не пытались писать, пока нет соединения
    private static Connection connection;
    private static String url;
    public static final Queue<Qinfo> QUERY;// = new ConcurrentLinkedQueue<>(); //new ArrayDeque<>();  //.poll()  ConcurrentLinkedQueue
    
    static {
        QUERY = new ConcurrentLinkedQueue<>();
    }
    
    public static class Qinfo {
        final String senderName;
        final String query;
        public Qinfo (final String senderName, final String querry) {
            this.senderName = senderName;
            this.query = querry;
        }
    }
    
    
    //при старте сервера синхронно, или плагины пишут состояние, когда еще нет соединения!! 
    //при старте острова GM.this_server_name.length()>3
    //true при Cfg.ReLoadAllConfig
    //false при OreloadCmd
    public static void init (final boolean loadGrous, final boolean async) { 
        url = Config.getConfig().getString("ostrov_database.mysql_host")
        + "?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf-8&user=" 
        + Config.getConfig().getString("ostrov_database.mysql_user") 
        + "&password=" 
        + Config.getConfig().getString("ostrov_database.mysql_passw");

        useOstrovData = Config.getConfig().getBoolean("ostrov_database.connect");
//Ostrov.log("OstrovDB init useOstrovData?"+useOstrovData+" loadGrous?"+loadGrous);
        if (useOstrovData) {
            
            if (loadGrous) {
                if (async) {
                    Ostrov.async( ()->  load(), 0);
                } else {
                    load();
                }
            } else {
                Ostrov.async( ()->  {
                    connect();
                    getBungeeServerInfo();
                }, 0);
            }
        }
    }

    private static void load() {
        connect();
        if (connection!=null) {
            getBungeeServerInfo(); //1!!!  Perm.loadGroups(false);
            GM.load(GM.State.STARTUP);//+прогрузит Lang
            Perm.loadGroups(false);
        }
    }
    
    //вызывать ASYNC!!
    protected static void connect() {
        ready = false;
        final long l = System.currentTimeMillis();
        Disconnect();
        Ostrov.log_ok("§6MySQL - создаём ostrov подключение..."); //не ставить log_err, или зацикливает!!!

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url);
            //errors = 0;
            Ostrov.log_ok("§6MySQL - ostrov подключение создано за "+(System.currentTimeMillis()-l)+"мс.");
            ready = true;
        } catch (SQLException | ClassNotFoundException e) {
            Ostrov.log_warn("MySql: соединение с базой ostrov  не удалось, "+(System.currentTimeMillis()-l)+"мс. -> "+e.getMessage()); //не ставить log_err, или зацикливает!!!
            connection = null;
            ready = false;
        }
    }


    
    public static void executePstAsync(final CommandSender cs, final String querry) {
        //if (!useOstrovData || connection==null) return;
        if (!useOstrovData) return;
        final String senderName = cs==null ? null : (cs instanceof ConsoleCommandSender) ? "console" : (cs instanceof Player) ? cs.getName() : null;
        //QUERRY.add( sender+querry );
        //Qinfo qi = new Qinfo(senderName, querry);
        QUERY.add( new Qinfo(senderName, querry) );
//Ostrov.log("executePstAsync "+sender+querry);
        /*
        Ostrov.async( ()-> {
            PreparedStatement pst = null;
            try {
                pst = connection.prepareStatement(querry);
                pst.execute();
            } catch (SQLException ex) { 
                Ostrov.log_err("OstrovDB executePstAsync "+querry+" : "+ex.getMessage());
                if (cs!=null) cs.sendMessage("§cОшибка выполнения запроса "+querry);
            } finally {
                try {
                    if (pst!=null) pst.close();
                } catch (SQLException ex) {
                    Ostrov.log_err("OstrovDB executePstAsync close "+ex.getMessage());
                }
            }
            
        }, 0);
        */
    }  

    
    //вызывать ASYNC!!
    protected static void getBungeeServerInfo() {

        if (connection==null) {
            Ostrov.log_warn("getBungeeServerInfo - нет соединения с БД!");
            return;
        }
        Statement stmt = null;
        ResultSet rs = null;
        PreparedStatement pst = null;
        
         GM.allBungeeServersName.clear();
         GM.allBungeeServersName.add("lobby0"); //на случай, если нет коннекта к БД
         GM.allBungeeServersName.add("lobby1");
        
        try {
            stmt =connection.createStatement();

            //rs = stmt.executeQuery( "SELECT `serverId`, `motd`, `type`, `logo` FROM "+Table.BUNGEE_SERVERS.table_name+" WHERE  `name`='"+this_server_name+"' AND `type` NOT LIKE 'NONE'" );
            rs = stmt.executeQuery( "SELECT `serverId`, `motd`, `type`, `diag`, `logo` FROM "+Table.BUNGEE_SERVERS.table_name+" WHERE  `name`='"+Ostrov.MOT_D+"';" );
            if (rs.next()) {
                if (rs.getBoolean("diag"))  {
                    Ostrov.server_id = rs.getInt("serverId");
                    Ostrov.log_ok("§bИД сервера = "+Ostrov.server_id+". Запись состояния в таблицу каждые 10 секунд.");
                } else {
                    Ostrov.log_ok("§eИД сервера для имени "+Ostrov.MOT_D+" не получен, состояние сервера в таблицу писаться не будет.");
                }
                GM.setLogo(rs.getString("logo")+" ");
            } else {
                Ostrov.log_warn("Для указанного motd нет записи в таблице серверов!");
                GM.setLogo("§7S ");

            }
            rs.close();

            //вычитать все банжи-имена серверов
            rs = stmt.executeQuery( "SELECT `name` FROM "+Table.BUNGEE_SERVERS.table_name );
            while (rs.next()) {
                if (rs.getString("name").length()==3) {
                    continue;
                }
                if (rs.getString("name").startsWith("sedna")) {
                    GM.allBungeeServersName.add("sedna");
                    continue;
                }
                GM.allBungeeServersName.add(rs.getString("name"));
            }
            //GM.allBungeeServersName.
            rs.close();
            stmt.close();

            GM.allBungeeServersName.remove("bungee");

            //удалять со штампом старее 10 минут, или обнуляет только что записанные арены!!!
            pst = connection.prepareStatement("DELETE FROM "+Table.ARENAS.table_name+" WHERE `server`='"+Ostrov.MOT_D+"' AND `stamp` < "+(Timer.getTime()-600)); //1*60*60
            pst.execute();

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


    }
    
    //вызывается из Timer async!! useOstrovData и соединение чекать до вызова! 
    public static void writeThisServerStateToOstrovDB() {  //вызывается из Timer каждые 5 сек. если write_server_state_to_bungee_table=true
        //if (!OstrovDB.useOstrovData) return;
//Ostrov.log("--writeThisServerStateToOstrovDB--");
       // if (connection==null) {
      //      Ostrov.log_warn("writeThisServerStateToOstrovDB - нет соединения с БД!");
       //     return;
       // }

       final String querry = "UPDATE "+Table.BUNGEE_SERVERS.table_name+
         " SET `online`='"+Bukkit.getOnlinePlayers().size()+"', `onlineLimit`='"
         +Bukkit.getMaxPlayers()+"', `tps`='"+ Nms.getTps()+"', `memory`='"
         +(int)(Runtime.getRuntime().totalMemory()/1024/1024 )+"', `memoryLimit`='"
         +(int)(Runtime.getRuntime().maxMemory()/1024/1024)+"', `freeMemory`='"
         +(int)(Runtime.getRuntime().freeMemory()/1024/1024)
         +"',`stamp`='"+ApiOstrov.currentTimeSec()+"',`ts`= NOW()+0 WHERE `serverId`='"+Ostrov.server_id+"'; ";
        executePstAsync(Bukkit.getConsoleSender(), querry);


      GameInfo gi = GM.getGameInfo(GM.GAME);
      if (gi!=null) {
          ArenaInfo ai = gi.arenas().stream().findAny().get();//gi.getArena(Ostrov.MOT_D);
        if (ai!=null) {
          ai.players = Bukkit.getOnlinePlayers().size();
          ai.sendData();
        }
      }


      /*  PreparedStatement pst = null;
        try {
//System.out.println("query="+"UPDATE  SET `online`='"+Bukkit.getOnlinePlayers().size()+"',`tps`='"+(int) MinecraftServer.getServer().recentTps[0]+"',`memory`='"+(int)(Runtime.getRuntime().maxMemory()/1024/1024 )+"',`memory_max`='"+(int)(Runtime.getRuntime().totalMemory()/1024/1024)+"',`stamp`='"+Main.Единое_время()+"' WHERE UPPER `id`='"+Main.id+"' ");                
            pst = connection.prepareStatement("UPDATE "+Table.BUNGEE_SERVERS.table_name+" SET "
                    + "`online`='"+Bukkit.getOnlinePlayers().size()+"',"
                    + "`onlineLimit`='"+Bukkit.getMaxPlayers()+"',"
                    + "`tps`='"+VM.getNmsServer().getTps()+"',"
                    + "`memory`='"+(int)(Runtime.getRuntime().totalMemory()/1024/1024 )+"',"
                    + "`memoryLimit`='"+(int)(Runtime.getRuntime().maxMemory()/1024/1024)+"',"
                    + "`freeMemory`='"+(int)(Runtime.getRuntime().freeMemory()/1024/1024)+"',"
                    + "`stamp`='"+ApiOstrov.currentTimeSec()+"'"
                    + " WHERE `serverId`='"+Ostrov.server_id+"' ");
            pst.executeUpdate();

        } catch (SQLException ex) {
            Ostrov.log_warn("§cSM  updServerState err_"+" ex="+ex.getMessage());
            ready = false;
            //e.printStackTrace();
        } finally {
            try{
                if (pst!=null) pst.close();
            } catch (SQLException ex) {
                Ostrov.log_warn("§cSM updServerState close err_"+" ex="+ex.getMessage());
            }
        }*/
        
    }


    //вызывать ASYNC!!!
    public static @Nullable HashMap<String,Object> getResultSet (final CommandSender cs, final String querry, final Consumer <HashMap <String, Object>> consumer) {
        
        if (!useOstrovData) return null;
            
        //final Connection conn = GetConnection();
        if (connection==null) return null;
        
        Statement stmt = null;
        ResultSet rs = null;

        try {
            
            stmt = connection.createStatement();
            rs = stmt.executeQuery( querry );
            final ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            final HashMap <String, Object> row = new HashMap<>(columns);
            while (rs.next()){
                for(int i=1; i<=columns; ++i){           
                    row.put(md.getColumnName(i),rs.getObject(i));
                }
            }
            rs.close();
            
            if (consumer!=null) {
                consumer.accept(row);
            }
            
            return row;

        } catch (SQLException ex) {

            Ostrov.log_err("§сOstrovDB getResultSet "+querry+" : "+ex.getMessage());
            if (cs!=null) cs.sendMessage("§cОшибка выполнения запроса "+querry+" : "+ex.getMessage());

        } finally {

            try {
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            } catch (SQLException ex) {
                Ostrov.log_err("§сOstrovDB getResultSet close "+ex.getMessage());
            }

        }

        return null;
        
    }    

    //вызывать ASYNC!!!
    public static @Nullable List<HashMap<String,Object>> getMultiResultSetAsync(final CommandSender cs, final String querry, final Consumer <List<HashMap<String,Object>>> consumer) {
        if (!useOstrovData) return null;
            
        //final Connection conn = GetConnection();
        if (connection==null) return null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<HashMap<String, Object>> resultSetList = null;

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery( querry );
            final ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            resultSetList = new ArrayList<>(50);
            while (rs.next()){
                final HashMap <String, Object> row = new HashMap<>(columns);
                for(int i=1; i<=columns; ++i){           
                    row.put(md.getColumnName(i),rs.getObject(i));
                }
                resultSetList.add(row);
            }
            rs.close();
            
            if (consumer!=null) {
                consumer.accept(resultSetList);
            }
            
        } catch (SQLException ex) {

            Ostrov.log_err("§сOstrovDB getResultSetAsync "+querry+" : "+ex.getMessage());
            if (cs!=null) cs.sendMessage("§cОшибка выполнения запроса "+querry+" : "+ex.getMessage());

        } finally {

            try {
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            } catch (SQLException ex) {
                Ostrov.log_err("§сOstrovDB getResultSetAsync close "+ex.getMessage());
            }

        }

        return resultSetList;
        
    }    

    //вызывать ASUNC!!!
    public static @Nullable HashMap<String,Object> getResultSetAsync(final CommandSender cs, final String querry) {
        if (!useOstrovData) return null;
            
        //final Connection conn = GetConnection();
        if (connection==null) return null;
        Statement stmt = null;
        ResultSet rs = null;
        final HashMap <String, Object> map = new HashMap<>();

        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery( querry );
            final ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            if (rs.next()){
                for(int i=1; i<=columns; ++i){           
                    map.put(md.getColumnName(i),rs.getObject(i));
                }
            }
            rs.close();

        } catch (SQLException ex) {

            Ostrov.log_err("§сOstrovDB getResultSetAsync "+querry+" : "+ex.getMessage());
            if (cs!=null) cs.sendMessage("§cОшибка выполнения запроса "+querry);

        } finally {

            try {
                if (rs!=null) rs.close();
                if (stmt!=null) stmt.close();
            } catch (SQLException ex) {
                Ostrov.log_err("§сOstrovDB getResultSetAsync close "+ex.getMessage());
            }

        }

        return map;
        
    }    












    public static Connection getConnection() {
        return connection;
    }

    public static void Disconnect() {
        try {
            //if (connection != null) {
            //if (connection != null && !connection.isClosed() && connection.isValid(3)) {
            //if (connection != null && !connection.isClosed()) {
            if (connection != null) {
                connection.close();
            }
            //connection = null;
        } catch (SQLException e) {
            Ostrov.log_warn("§cMySql: Disconnect ostrov не удалось !"+e.getMessage());
        } finally {
            connection = null;
        }
    }    


    
     
    

    
    
    
    
    
    
    
    
}
