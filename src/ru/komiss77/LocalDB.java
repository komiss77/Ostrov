package ru.komiss77;

import ru.komiss77.modules.player.PM;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.Bukkit;

import org.bukkit.scheduler.BukkitRunnable;

import ru.komiss77.Cfg;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.LocationUtil;




public class LocalDB {

    public static boolean useLocalData = false;
    public static boolean ready = false;
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
                    " CREATE TABLE IF NOT EXISTS `warps` ( " +
                    "  `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT, " +
                    "  `name` varchar(16) NOT NULL, " +
                    "  `dispalyMat` varchar(32) NOT NULL DEFAULT '', " +
                    "  `owner` varchar(16) NOT NULL DEFAULT '', " +
                    "  `descr` varchar(128) NOT NULL DEFAULT '', " +
                    "  `loc` varchar(64) NOT NULL DEFAULT '', " +
                    "  `system` tinyint(1) NOT NULL DEFAULT '1', " +
                    "  `open` tinyint(1) NOT NULL DEFAULT '1', " +
                    "  `need_perm` tinyint(1) NOT NULL DEFAULT '0', " +
                    "  `use_cost` int NOT NULL DEFAULT '0', " +
                    "  `use_counter` int NOT NULL DEFAULT '0', " +
                    "  `create_time` int NOT NULL DEFAULT '0' " +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;");

            } catch (SQLException e) {
                Ostrov.log_err("§4Не удалось создать таблицу warps -> "+e.getMessage());
            }

            try {
                GetConnection().createStatement().executeUpdate(
                    " CREATE TABLE IF NOT EXISTS `errors` ( " +
                    "`id` int NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                    "`msg` varchar(512) NOT NULL," +
                    "`stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP " +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;");

            } catch (SQLException e) {
                Ostrov.log_err("§4Не удалось создать таблицу errors -> "+e.getMessage());
            }

//фикс!! потом убрать
//ALTER TABLE `errors` CHANGE `id` `id` INT(11) NOT NULL AUTO_INCREMENT; 
//ALTER TABLE `errors` ADD PRIMARY KEY (`id`) AUTO_INCREMENT;

//try {  
 //   GetConnection().createStatement().executeUpdate(
 //       " ALTER TABLE `errors` CHANGE `id` `id` INT(11) NOT NULL  PRIMARY KEY AUTO_INCREMENT; ");
//} catch (SQLException e) {
 //   Ostrov.log_err("§4ALTER TABLE `errors` ADD PRIMARY KEY (`id`) -> "+e.getMessage());
//}

            try {
                GetConnection().createStatement().executeUpdate(
                    " CREATE TABLE IF NOT EXISTS `moneyOffline` ( " +
                    " `id` int NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                    "  `name` varchar(16) NOT NULL," +
                    "  `value` int NOT NULL," +
                    "  `who` varchar(256) NOT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;");

            } catch (SQLException e) {
                Ostrov.log_err("§4Не удалось создать таблицу moneyOffline -> "+e.getMessage());
            }
//фикс!! потом убрать
//try {
 //   GetConnection().createStatement().executeUpdate(
  //      " ALTER TABLE `moneyOffline` CHANGE `id` `id` INT NOT NULL  PRIMARY KEY AUTO_INCREMENT; ");
//} catch (SQLException e) {
 //   Ostrov.log_err("§4ALTER TABLE `moneyOffline` ADD PRIMARY KEY (`id`) -> "+e.getMessage());
//}

        }

 
    private static Connection CreateConn() {
        try {
            Disconnect();
            Class.forName("com.mysql.jdbc.Driver");
            //Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url);
        } catch (SQLException | ClassNotFoundException e) {
            ready = false;
            Ostrov.log_warn("§4MySql: соединение с локальной БД не удалось !"+e.getMessage()); //не ставить log_err, или зацикливает!!!
            return null;
        }
    }
    public static Connection getConnectionDirect() {
        return connection;
    }
    
    public static Connection GetConnection() {
        if (!useLocalData) return null;
        try {
//System.out.println( "GetConnection null?" + (connection == null)+" valid?"+(connection != null && connection.isValid(1)));
            if ( connection != null && connection.isValid(1)) {
                ready = true;
                return connection;
            } else {
                ready = false;
                Ostrov.log_warn("§6MySQL - создаём local подключение..."); //не ставить log_err, или зацикливает!!!
                //return CreateConn();
            }
        } catch (SQLException e) {
            ready = false;
            Ostrov.log_warn("§4MySql: соединение local сломалось !"+e.getMessage());   //не ставить log_err, или зацикливает!!!
        }
        
        return connection = CreateConn();
    }


    public static void Disconnect() {
        if (!useLocalData) return;
        try {
            if (connection != null) connection.close();
            ready = false;
        } catch (SQLException e) {
            ready = false;
            Ostrov.log_warn("§4MySql: Disconnect local не удалось !"+e.getMessage());
        }
    }

    
    
    
     
    
    
    
    
    
    
    
    
    
    
    
}
