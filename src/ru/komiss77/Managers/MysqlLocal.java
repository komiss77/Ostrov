package ru.komiss77.Managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.bukkit.scheduler.BukkitRunnable;

import ru.komiss77.Cfg;
import ru.komiss77.Objects.Oplayer;
import ru.komiss77.Objects.Warp;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.LocationUtil;




public class MysqlLocal {

    public static boolean useLocalData=false;
    private static String host = "";
    private static String user = "";
    private static String passw = "";
    
    private static Connection connection;
    private static String url;
    



    public static void GetVar () {
        useLocalData = Cfg.GetCongig().getBoolean("local_database.use");
        host = Cfg.GetCongig().getString("local_database.mysql_host");
        user = Cfg.GetCongig().getString("local_database.mysql_user");
        passw = Cfg.GetCongig().getString("local_database.mysql_passw");
        url = host + "?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf-8&user=" + user + "&password=" + passw;
    }    


    public static void Init () {
        GetVar();
        if (useLocalData) SetupTable();
    }    

    public static void ReloadVars () {
        GetVar();
    }






    public static void moneyOffline (final String name, final int value, final String who) {
        if (!useLocalData) return;

        Ostrov.async( () -> {
            PreparedStatement pst = null;
            
            try {
                pst = GetConnection().prepareStatement ( "INSERT INTO `moneyOffline` ( `name`, `value`, `who` ) VALUES ( ?, ?, ? ) ");
                

                            
                    pst.setString(1, name);
                    pst.setInt(2, value );
                    pst.setString(3, who );

                    pst.executeUpdate();
                    
                    if (PM.exist(who)) {
                        Bukkit.getPlayer(who).sendMessage("§e"+name+" сейчас не на сервере, платёж будет выполнен при входе.");
                    }
                    
                } catch (SQLException e) { 
                    
                    Ostrov.log_err("§4Не удалось сохранить оффлайн-платёж "+name+" -> "+e.getMessage());
                    
                    if (PM.exist(who)) {
                        Bukkit.getPlayer(who).sendMessage("§e"+name+" сейчас не на сервере, платёж будет выполнен при входе.");
                    }
                    
                } finally {
                    if (pst!=null) try {
                        pst.close();
                    } catch (SQLException ex) {
                        
                    }
                }

            }, 0);
        }




    public static void Save_Home ( String nik, Oplayer op ) {

        if (!useLocalData) return;

        new BukkitRunnable(){ 
          @Override
            public void run() {

                String homes = "";
                for (String home : op.GetHomeData().keySet()) {
                    //homes = homes + i + "<>" + op.GetHomeData().get(i) + "<:>";
                    homes = homes + home + "<>" + LocationUtil.StringFromLoc(op.GetHomeData().get(home)) + "<:>";
                }

                    try ( Statement stmt = GetConnection().createStatement(); )

                        {
                           stmt.executeUpdate( "INSERT INTO `data` (`name`, `homes` ) VALUES "
                          + "(\'" + nik + "\',"
                          + "\'" + homes + "\' ) " +
                          "ON DUPLICATE KEY UPDATE "
                          + "`homes`=\'" + homes + "\' " ); 

                            stmt.close();

                            Ostrov.log_ok("§2точки дома "+nik+" сохранены!"); 

                        } catch (SQLException e) { 
                            Ostrov.log_err("§4Не удалось сохранить точку дома "+nik+" -> "+e.getMessage());
                        } 



            }}.runTaskAsynchronously( Ostrov.instance );   

        }













 
    
    


    static void Load_warps() {
        if (!useLocalData) return;

        new BukkitRunnable(){ 
          @Override
            public void run() {

        String name = "";
        String type= "";
        String owner= "";
        String desc= "";
        String loc = "";
        boolean open = true;
        boolean need_perm = false;
        int use_cost = 0;
        int use_counter = 0;
        long create_time = 0;

        int c=0;

                    try (   
                            Statement stmt = GetConnection().createStatement(); 
                            ResultSet rs = stmt.executeQuery( "SELECT * FROM `warps` " );
                        ) 
                        {
                                while (rs.next()) {
                                    name = rs.getString("name");
                                    type= rs.getString("type");
                                    owner= rs.getString("owner");
                                    desc= rs.getString("descr");
                                    loc= rs.getString("loc");
                                    open = rs.getBoolean("open");
                                    need_perm = rs.getBoolean("need_perm");
                                    use_cost = rs.getInt("use_cost");
                                    use_counter = rs.getInt("use_counter");
                                    create_time = rs.getLong("create_time");
                                        c++;

                                    Warps.Load_warp( name, type, owner, desc, loc, open, need_perm, use_cost, use_counter, create_time );
                                }
                                rs.close();
                                stmt.close();
                                Ostrov.log_ok("§2Загружено варпов: §l"+c);

                        } catch (SQLException e) {
                            Ostrov.log_err("§4Не удалось загрузить варпы -> "+e.getMessage());
                        } 

            }}.runTaskAsynchronously( Ostrov.instance );   
        }

    public static void Add_warp (  Player p, String name, Warp w, boolean notify ) {
        if (!useLocalData) return;

        new BukkitRunnable(){ 
          @Override
            public void run() {

                    try (
                            PreparedStatement pst = GetConnection().prepareStatement ( "INSERT INTO `warps` (`name`, `type`, `owner`, `descr`, `loc`, `open`, `need_perm`, `use_cost`, `use_counter`, `create_time` ) VALUES "
                                    + "( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) " /*+
                                    "ON DUPLICATE KEY UPDATE "
                                    + "name=VALUES(name), "
                                    + "type=VALUES(type), "
                                    + "owner=VALUES(owner), "
                                    + "descr=VALUES(descr), "
                                    + "loc=VALUES(loc), "
                                    + "open=VALUES(open), "
                                    + "need_perm=VALUES(need_perm), "
                                    + "use_cost=VALUES(use_cost), "
                                    + "use_counter=VALUES(use_counter), "
                                    + "create_time=VALUES(create_time) "*/)
                            )

                            {            
                                    pst.setString(1, name);
                                    pst.setString(2, w.Get_type() );
                                    pst.setString(3, w.Get_owner() );
                                    pst.setString(4, w.Get_desc() );
                                    pst.setString(5, Warps.Loc_to_String(w.Get_loc() ) );
                                    pst.setBoolean(6, w.Is_open() );
                                    pst.setBoolean(7, w.Need_perm() );
                                    pst.setInt(8, w.Get_cost() );
                                    pst.setInt(9, w.Get_counter() );
                                    pst.setInt(10, Timer.currentTimeSec() );

                            int res = pst.executeUpdate();
                            if (notify) {
                                if (res == 1 ) p.sendMessage("§2Данные варпа "+name+" сохранены в БД!");
                                else p.sendMessage("§4Не удалось сохранить данные варпа "+name+" - отказ БД!");
                            }
                            if (res == 1) Ostrov.log_ok("§2Данные варпа "+name+" сохранены"); 

                        } catch (SQLException e) { 
                            Ostrov.log_err("§4Не удалось сохранить данные варпа "+name+" -> "+e.getMessage());
                        } 

            }}.runTaskAsynchronously( Ostrov.instance );   
        }

    public static void Del_warp ( Player p, String name ) {
        if (!useLocalData) return;

        new BukkitRunnable(){ 
          @Override
            public void run() {

                    try ( Statement stmt = GetConnection().createStatement(); )

                        {
                           int res = stmt.executeUpdate( "DELETE FROM `warps` WHERE `name` LIKE '" + name + "'" ); 

                            stmt.close();

                            if (res == 1) p.sendMessage("§2Варп "+name+" удалён!");
                            else p.sendMessage("§4Варп "+name+" не удалён - запись не найдена");
                            if (res == 1) Ostrov.log_ok("§2Варп "+name+" удалён!"); 

                        } catch (SQLException e) { 
                            Ostrov.log_err("§4Не удалось удалить "+name+" -> "+e.getMessage());
                        } 

            }}.runTaskAsynchronously( Ostrov.instance );   
        }

    public static void Add_count ( String warp ) {
        if (!useLocalData) return;

        new BukkitRunnable(){ 
          @Override
            public void run() {
                    try ( Statement stmt = GetConnection().createStatement(); )
                        {
                           stmt.executeUpdate( "UPDATE `warps` SET `use_counter`=`use_counter`+1 WHERE `name` LIKE \'" + warp + "\'  LIMIT 1" ); 
                           stmt.close();
                        } catch (SQLException e) { 
                            Ostrov.log_err("§4Не удалось добавить счётчик "+warp+" -> "+e.getMessage());
                        } 
            }}.runTaskAsynchronously( Ostrov.instance );   
        }

    public static void Set_cost ( String warp, int cost ) {
        if (!useLocalData) return;

        new BukkitRunnable(){ 
          @Override
            public void run() {
                    try ( Statement stmt = GetConnection().createStatement(); )
                        {
                           stmt.executeUpdate( "UPDATE `warps` SET `use_cost`= \'" + cost + "\'  WHERE `name` LIKE \'" + warp + "\'  LIMIT 1" ); 
                           stmt.close();
                        } catch (SQLException e) { 
                            Ostrov.log_err("§4Не удалось установить плату для "+warp+" -> "+e.getMessage());
                        } 
            }}.runTaskAsynchronously( Ostrov.instance );   
        }

    public static void Set_open ( String warp, int on ) {
        if (!useLocalData) return;

        new BukkitRunnable(){ 
          @Override
            public void run() {
                    try ( Statement stmt = GetConnection().createStatement(); )
                        {
                           stmt.executeUpdate( "UPDATE `warps` SET `open`= \'" + on + "\'  WHERE `name` LIKE \'" + warp + "\'  LIMIT 1" ); 
                           stmt.close();
                        } catch (SQLException e) { 
                            Ostrov.log_err("§4Не удалось установить плату для "+warp+" -> "+e.getMessage());
                        } 
            }}.runTaskAsynchronously( Ostrov.instance );   
        }







    
    
    
    
    
    
    
    
    
    
    



    

    


    private static void SetupTable() {

        if (!useLocalData) return;
         try {
                GetConnection().createStatement().executeUpdate(
                    " CREATE TABLE IF NOT EXISTS `data` (" +
                    "  `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                    "  `name` varchar(16) NOT NULL UNIQUE KEY," +
                    "  `homes` varchar(512) NOT NULL DEFAULT ''," +
                    "  `world_pos` varchar(512) NOT NULL DEFAULT ''," +
                    "  `fly` tinyint(1) NOT NULL DEFAULT '0'," +
                    "  `flyspeed` tinyint(2) NOT NULL DEFAULT '-1'," +
                    "  `walkspeed` tinyint(2) NOT NULL DEFAULT '-1'," +
                    "  `pvp` tinyint(1) NOT NULL DEFAULT '1'," +
                    "  `pweather` tinyint(1) NOT NULL DEFAULT '-1'," +
                    "  `rtime` tinyint(1) NOT NULL DEFAULT '0'," +
                    "  `ptime` tinyint(2) NOT NULL DEFAULT '-1'," +
                    "  `bplace` int(11) NOT NULL DEFAULT '0'," +
                    "  `bbreak` int(11) NOT NULL DEFAULT '0'," +
                    "  `mobkill` int(11) NOT NULL DEFAULT '0'," +
                    "  `monsterkill` int(11) NOT NULL DEFAULT '0'," +
                    "  `pkill` int(11) NOT NULL DEFAULT '0'," +
                    "  `dead` int(11) NOT NULL DEFAULT '0'," +
                    "  `kits` varchar(535) NOT NULL DEFAULT ''" +
                    ") ENGINE=InnoDB AUTO_INCREMENT=121 DEFAULT CHARSET=utf8; ");

            } catch (SQLException e) {
                Ostrov.log_err("§4Не удалось создать таблицу data -> "+e.getMessage());
            }

         try {
                GetConnection().createStatement().executeUpdate(
                    " CREATE TABLE IF NOT EXISTS `warps` (" +
                    "  `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                    "  `name` varchar(16) NOT NULL UNIQUE KEY," +
                    "  `type` varchar(16) NOT NULL DEFAULT 'player'," +
                    "  `owner` varchar(16) NOT NULL DEFAULT ''," +
                    "  `descr` varchar(128) NOT NULL DEFAULT ''," +
                    "  `loc` varchar(64) NOT NULL DEFAULT ''," +
                    "  `open` tinyint(1) NOT NULL DEFAULT '1'," +
                    "  `need_perm` tinyint(1) NOT NULL DEFAULT '0'," +
                    "  `use_cost` int(11) NOT NULL DEFAULT '0'," +
                    "  `use_counter` int(11) NOT NULL DEFAULT '0'," +
                    "  `create_time` int(11) NOT NULL DEFAULT '0'" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8; ");

            } catch (SQLException e) {
                Ostrov.log_err("§4Не удалось создать таблицу warps -> "+e.getMessage());
            }
         
            try {
                GetConnection().createStatement().executeUpdate(
                    " CREATE TABLE IF NOT EXISTS `moneyOffline` (" +
                    "  `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                    "  `name` varchar(16) NOT NULL," +
                    "  `value` int(11) NOT NULL," +
                    "  `who` varchar(256) NOT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8; ");

            } catch (SQLException e) {
                Ostrov.log_err("§4Не удалось создать таблицу moneyOffline -> "+e.getMessage());
            }

        }

 
    private static Connection CreateConn() {
        try {
            Disconnect();
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(url);
        } catch (SQLException | ClassNotFoundException e) {
            Ostrov.log_err("§4MySql: соединение с базой данных сервера не удалось !"+e.getMessage());
            return null;
        }
    }
    
    
    public static Connection GetConnection() {
        if (!useLocalData) return null;
        try {
//System.out.println( "GetConnection null?" + (connection == null)+" valid?"+(connection != null && connection.isValid(1)));
            if ( connection != null && connection.isValid(1)) {
                return connection;
            } else {
                Ostrov.log_ok("§6MySQL - создаём local подключение...");
                //return CreateConn();
            }
        } catch (SQLException e) {
            Ostrov.log_err("§4MySql: соединение local сломалось !"+e.getMessage());
        }
        
        return connection = CreateConn();
    }


    public static void Disconnect() {
        if (!useLocalData) return;
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            Ostrov.log_err("§4MySql: Disconnect local не удалось !"+e.getMessage());
        }
    }

    
    
    
     
    
    
    
    
    
    
    
    
    
    
    
}
