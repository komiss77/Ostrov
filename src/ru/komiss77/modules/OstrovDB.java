package ru.komiss77.modules;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ru.komiss77.Cfg;
import ru.komiss77.Enums.Table;
import ru.komiss77.Objects.CaseInsensitiveMap;
import ru.komiss77.Objects.Group;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.OstrovConfig;




public class OstrovDB {

    public static boolean useOstrovData=false;
    private static Connection connection;
    private static String url;
    public static OstrovConfig default_perms;
    
    
    public static Map <String,Group> groups;
    public static Set <String> default_permissions;
    
    public static void init () {
        groups = new CaseInsensitiveMap();
        default_permissions = new HashSet<>();
        
        default_perms = Cfg.manager.getNewConfig("default_perms.yml", new String[]{"", "Права по умолчанию для всех игроков на этом сервере", ""} );
        default_perms.addDefault("default_permissions", Arrays.asList( "deluxechat.utf","deluxechat.pm", "deluxechat.bungee.chat", "deluxechat.bungee.toggle",
            "chestcommands.command.open", "chestcommands.open.menu.yml") );
        default_perms.saveConfig();
        
        
        url = Cfg.GetCongig().getString("ostrov_database.mysql_host")
                + "?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf-8&user=" 
                + Cfg.GetCongig().getString("ostrov_database.mysql_user") 
                + "&password=" 
                + Cfg.GetCongig().getString("ostrov_database.mysql_passw");

        useOstrovData = Cfg.GetCongig().getBoolean("ostrov_database.connect");
//System.out.println("---------- OstrovDB useOstrovData="+useOstrovData);
        if (useOstrovData) {
            SetupTable();
            loadGroups();
        }
        
        for (String perm : default_perms.getStringList("default_permissions") ) {
            default_permissions.add(perm);
        }
        
        Ostrov.log_ok("Загружено прав по умолчанию: §a"+default_permissions.size());
        
        
        //for (Oplayer op:PM.getOplayers()) {
        //    op.calculatePerms(false);
        //}
    }    


    
    public static void reload () {
        init();
      /*  default_permissions.clear();
        
        url = Conf.GetCongig().getString("ostrov_database.mysql_host")
                + "?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf-8&user=" 
                + Conf.GetCongig().getString("ostrov_database.mysql_user") 
                + "&password=" 
                + Conf.GetCongig().getString("ostrov_database.mysql_passw");

        if (Conf.GetCongig().getBoolean("ostrov_database.connect")) loadGroups();
        
        for (Oplayer op:PM.getOplayers()) {
            op.calculatePerms(false);
        }*/
    }


    public static Group groupByItemName(String group_chat_name) {
        for (Group gr:groups.values()) {
            if (gr.chat_name.equals(group_chat_name)) return gr;
        }
        return null;
    }
    

    
    
    


    
    public static void loadGroups () {
        if (!useOstrovData) return;

        Ostrov.async(()-> {
            //if (GetConnection() == null) return;
            Statement stmt = null;
            ResultSet rs = null;
            
            try {
                stmt = GetConnection().createStatement();
                
                rs = stmt.executeQuery( "SELECT * FROM  "+Table.PEX_GROUPS.table_name ); //кинуло на home1 attempted  duplicate class definition
                    while (rs.next()) {
                        groups.put(rs.getString("gr"), new Group (rs.getString("gr"), rs.getString("name"), rs.getString("inh"), rs.getString("type"), rs.getInt("price"), rs.getInt("inv_slot"), rs.getString("mat"), rs.getString("group_desc") ) );
                    } 
                rs.close();
                
                
                rs = stmt.executeQuery( "SELECT * FROM "+Table.PEX_GROUP_PERMS.table_name );

                    while (rs.next()) {
                        if (groups.containsKey(rs.getString("gr"))) {
                            groups.get(rs.getString("gr")).permissions.add(rs.getString("perm"));
                        }
                    }
                    
                rs.close();
                            
                    
//rs.close();
//System.out.println("LoadGroups 111 groups_donat="+groups_donat+"   groups_staff="+groups_staff);            

            } catch (SQLException e) { 
                Ostrov.log_err("§с LoadPlayerGroups error - "+e.getMessage()); 
            } finally {
                try{
                    if (rs!=null) rs.close();
                    if (stmt!=null) stmt.close();
                } catch (SQLException ex) {
                    Ostrov.log_err("§ LoadPlayerGroups close error - "+ex.getMessage());
                }
                //пересчитать наследование!
                final List<String> gr_names = new ArrayList<>();
                gr_names.addAll(groups.keySet());
                
                Group current_group;
                for (String g : gr_names) {
                    current_group=groups.get(g);
                    for (String child_group:current_group.inheritance) {
                        if ( !child_group.equals(current_group.name) && groups.containsKey(child_group) ) current_group.permissions.addAll(groups.get(child_group).permissions);
                    }
                }
                
//groups.values().stream().forEach((g) -> {
//    System.out.println("группа "+g.chat_name+" perm="+g.permissions);
//}); 
                Ostrov.log_ok("Database: Загружены группы+права групп! ("+groups.size()+"групп)");
            }
                
        }, 0);
    
    }
    
    
   












    

    
    
    
    private static void SetupTable() {
    //if (GetConnection() == null) return;
       // try {
            
          //  GetConnection().createStatement().executeUpdate(
           //          "create table if not exists `prefix` ( `name` varchar(32) PRIMARY KEY ,"+
          //         "  `prefix` varchar(64) NOT NULL DEFAULT ''," +
           //         "  `suffix` varchar(64) NOT NULL DEFAULT '' )");
            
      //  } catch (SQLException sqlexception) {}

    }

 
    private static Connection CreateConn() {
        try {
            Disconnect();
            Class.forName("com.mysql.jdbc.Driver");
            //Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url);
            return connection;
        } catch (SQLException | ClassNotFoundException e) {
            Ostrov.log_err("§4MySql: соединение с базой ostrov не удалось !"+e.getMessage());
            return null;
        }
    }
    
    
    public static Connection GetConnection() {
        if (!useOstrovData) return null;
        try {
            if ( connection != null && !connection.isClosed() && connection.isValid(6)) return connection;
            else {
                Ostrov.log_ok("§6MySQL - создаём подключение ostrov...");
                CreateConn();
            }
        } catch (SQLException e) {
            Ostrov.log_err("§4MySql: соединение ostrov сломалось !"+e.getMessage());
        }
        
        return connection;// = CreateConn();
    }


    public static void Disconnect() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            Ostrov.log_err("§4MySql: Disconnect ostrov не удалось !"+e.getMessage());
        }
    }

    
    
    
     
    
    
    
    
    
    
    
    
    
    
    
}
