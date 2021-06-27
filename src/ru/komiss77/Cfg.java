package ru.komiss77;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import ru.komiss77.Commands.CMD;
import ru.komiss77.Commands.Pvp;
import ru.komiss77.Listener.MenuListener;
import ru.komiss77.Listener.PlayerListener;
import ru.komiss77.Listener.ServerListener;
import ru.komiss77.Listener.TPAListener;
import ru.komiss77.Managers.MysqlLocal;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.Timer;
import ru.komiss77.modules.OstrovDB;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.utils.OstrovConfigManager;







public class Cfg {

    
    public static OstrovConfigManager manager;
    
    private static OstrovConfig config;
    private static OstrovConfig variable;
    
   // public static String chanelName="ostrov:ostrov"; //не переносить, ругается банжи-часть на букит!! - дубль в OstrovBungee
    
    
    
    public static void Init () {

        manager = new OstrovConfigManager(Ostrov.GetInstance());

        LoadConfigs();

    }    
    















public static void LoadConfigs () {
        
    config = manager.getNewConfig("config.yml", new String[]{"", "Ostrov77 config file", ""} );
    variable = manager.getNewConfig("variable.yml");
    
    
    
    
    
    
    
    //Remove
    config.removeKey("ostrov_database.write_server_state_to_bungee_table");
    config.removeKey("ostrov_database.games_info_for_server_menu_load_interval_ticks" );
   // config.removeKey("modules.command.pvp");
    //config.removeKey("player.keep_inventory");
   // config.removeKey("modules.command.warp");
    //config.removeKey("modules.command.shop");
    //config.removeKey("world.spawn");
    //config.removeKey("");""


    
    
    
    
    
    

    String[] c0 = {"---------", "player settings", "---------", "gamemode_set_to - SURVIVAL ADVENTURE CREATIVE SPECTATOR",
    "walkspeed_on_join - from 0.1F to 0.9F ; -1 to disable", "item_lobby_mode - cancel move,drop,drag gived item", ""}; 
    config.addDefault("player.teleport_on_first_join", false, c0);
    config.addDefault("player.change_gamemode_on_join", false);
    config.addDefault("player.gamemode_set_to", "ADVENTURE" );
    config.addDefault("player.walkspeed_on_join", "0.1F");
    config.addDefault("player.clear_stats", false);
    config.addDefault("player.disable_void", false);
    config.addDefault("player.disable_damage", false);
    config.addDefault("player.disable_hungry", false);
    config.addDefault("player.disable_break_place", false);
    config.addDefault("player.item_lobby_mode", false);
    config.addDefault("player.block_fly_pvp", false);
    config.addDefault("player.give_pipboy", false,"выдавать часики при входе");
    config.addDefault("player.give_pipboy_slot", 0);
    config.addDefault("player.give_bow_teleport", false);
    config.addDefault("player.invulnerability_on_join_or_teleport", -1);
    config.addDefault("player.set_tab_list_header_footer", true);
    config.addDefault("player.disable_lava", false);
    config.addDefault("player.show_ostrov_info_on_scoreboard", false);

    
    String[] c1 = {"---------", "modules manager", "---------"}; 
    config.addDefault("modules.name_tag_manager", false);
    config.addDefault("modules.enable_jump_plate", false, c1);
    config.addDefault("modules.teleport_gui", false);
    config.addDefault("modules.nbt_checker", false);
    
    config.addDefault("modules.pvp.use_pvp_command", false);
    config.addDefault("modules.pvp.battle_mode_time", -1);
    config.addDefault("modules.pvp.kill_on_relog", false);
    config.addDefault("modules.pvp.drop_inv_inbattle", false);
    config.addDefault("modules.pvp.display_pvp_tag", false);
    config.addDefault("modules.pvp.disable_creative_attack_to_mobs", false);
    config.addDefault("modules.pvp.disable_creative_attack_to_player", false);
   

    config.addDefault("modules.command.home.use", false);
    config.addDefault("modules.command.home.amount_per_group.default", 1);
    config.addDefault("modules.command.home.amount_per_group.vip", 3);
    config.addDefault("modules.command.home.amount_per_group.premium", 7);

    config.addDefault("modules.command.fly.use", false);
    config.addDefault("modules.command.fly.disable_atack_on_fly", false);
    config.addDefault("modules.command.fly.fly_off_on_damage", false);
    
    config.addDefault("modules.command.tpa", -1);
    config.addDefault("modules.save_location_on_world_change", false);
    config.addDefault("modules.command.tpr", -1, "random teleport. value - cooldown, -1 to disable.");

    config.addDefault("modules.command.back", false);
    config.addDefault("modules.command.settings", false);
    config.addDefault("modules.command.get", false);
    config.addDefault("modules.command.world", false);
    config.addDefault("modules.command.tppos", false);
    config.addDefault("modules.command.tphere", false);
    config.addDefault("modules.command.spawn", false);
    config.addDefault("modules.command.gm", false);
    config.addDefault("modules.command.invsee", false);
    config.addDefault("modules.command.speed", false);
    config.addDefault("modules.command.pweather", false);
    config.addDefault("modules.command.ptime", false);

    config.addDefault("modules.command.heal", false);
    config.addDefault("modules.command.repair", false);
    config.addDefault("modules.command.spy", false);
    config.addDefault("modules.command.top", false);
    config.addDefault("modules.teleport_to_region_in_settings_menu", false);

    config.addDefault("modules.command.kit", false);
    config.addDefault("modules.command.menu", "serv");

    
    String[] c2 = {"---------", "world managment", "---------"}; 
    config.addDefault("world.block_day_night_change", false);
    config.addDefault("world.set_time_to", 1000);
    config.addDefault("world.disable_weather", false);
    config.addDefault("world.disable_blockspread", false);
    config.addDefault("world.disable_ice_melt", false);
    config.addDefault("world.disable_moob_griefing", false);
    config.addDefault("world.disable_moob_loot", false);
    config.addDefault("world.disable_entity_drops", false);
    
    
    String[] c3 = {"---------", "system settings", "---------"}; 
    config.addDefault("system.autorestart.use", true);
    config.addDefault("system.autorestart.hour", 3, "час рестарта. ");
    config.addDefault("system.autorestart.min", ApiOstrov.randInt(1, 59), "минута рестарта (при создании конфига-рандомная)");
    config.addDefault("system.pipboy_material", "CLOCK");
    config.addDefault("system.pipboy_name", "§a§lМеню сервера - нажми ПКМ!");
    config.addDefault("system.pipboy_rigth_click_command", "menu");
    config.addDefault("system.pipboy_left_click_command", "menu");
    config.addDefault("system.prefix.use_preffix_suffix_wothout_deluxechat", false); //работают когда нет делюксчата
    config.addDefault("system.prefix.prefix_name_space", "§2 "); //работают когда нет делюксчата
    config.addDefault("system.prefix.name_suffix_space", "§7 ");//работают когда нет делюксчата
    config.addDefault("system.prefix.suffix_message_space", "§7§o≫ §7");  //работают когда нет делюксчата  
    
    
    
    //работа с БД глобальной
    String[] c4 = {"---------", "ostrov_database", "---------"}; 
    config.addDefault("ostrov_database.connect", false);
    config.addDefault("ostrov_database.auto_reload_permissions", false);
    config.addDefault("ostrov_database.auto_reload_permissions_interval_min", 15);
    config.addDefault("ostrov_database.mysql_host", "jdbc:mysql://localhost/ostrov");
    config.addDefault("ostrov_database.mysql_user", "user");
    config.addDefault("ostrov_database.mysql_passw", "pass");
    //config.addDefault("ostrov_database.write_server_state_to_bungee_table", false);
    config.addDefault("ostrov_database.games_info_for_server_menu_load", false);
    config.addDefault("ostrov_database.games_info_for_server_menu_load_interval_sec", 3);
    config.addDefault("ostrov_database.games_info_for_server_menu_send", false);
    
    
    String[] c5 = {"---------", "local database", "---------"}; 
    config.addDefault("local_database.use", false);
    config.addDefault("local_database.mysql_host", "jdbc:mysql://localhost/server");
    config.addDefault("local_database.mysql_user", "user");
    config.addDefault("local_database.mysql_passw", "pass");
    

    
    config.saveConfig();

    



    
    
    
    variable.addDefault("last_day", Get_day());
    variable.saveConfig();

    
    

    }    
 



   
    
    public static void ReLoadAllConfig() {

        LoadConfigs();

        CMD.ReLoadVars();
        OstrovDB.reload();
        ServerListener.ReloadVars();
        PlayerListener.ReloadVars();
        MenuListener.ReloadVars();
        TPAListener.ReloadVars();

        PM.ReLoadVars();
        MysqlLocal.ReloadVars();
        

        Timer.ReLoadVars();
        Pvp.reload();

    }
















    public static  OstrovConfig GetCongig ( ) {
         return config;
     }  



    public static  OstrovConfig GetVariable ( ) {
         return variable;
     }  


    
    


    public static void copy(InputStream in, File file) {
        try {
            try (OutputStream out = new FileOutputStream(file)) {
                byte[] buf = new byte[1024];
                
                int len;
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);
            }
            in.close();
        } catch (Exception e) { Ostrov.log_err("Config copy error! "+e.getMessage()); }
    }


    public static int Get_day () {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }
    

}
