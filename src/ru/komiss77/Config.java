package ru.komiss77;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import org.bukkit.GameMode;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.utils.OstrovConfigManager;


public class Config {

    
    public static OstrovConfigManager manager;
    
    private static OstrovConfig config;
    private static OstrovConfig variable;
    
    //для PM
    public static boolean ostrovStatScore = false;    
    public static boolean tablist_header_footer = false;    
    public static boolean tablist_name = false;    
    public static boolean scale_health = false;
    //public static boolean nameTag = false;    

    //для ServerListener
    public static boolean block_nether_portal;
//    public static boolean disable_weather; уже в gamerule есть
    public static boolean disable_blockspread;
    public static boolean disable_ice_melt;
    
    //для PlayerListener
    public static boolean set_gm = false;
    public static GameMode gm_on_join = GameMode.ADVENTURE;
    public static float walkspeed_on_join = 0.2F;
    public static boolean clear_stats = false;
    public static boolean disable_void;
    public static boolean disable_damage;
    public static boolean disable_hungry;
    public static boolean disable_break_place;
    public static boolean disable_lava;   
    
    //для CMD
    public static boolean home_command;
    public static boolean fly_command;
//    public static boolean fly_block_atack_on_fly;
//    public static boolean fly_off_on_damage;
    public static int tpa_command;
    public static boolean save_location_on_world_change;
    public static int tpr_command;
    public static boolean back_command;
    public static boolean settings_command;
    public static boolean get_command;
    public static boolean world_command;
    public static boolean tppos_command;
    public static boolean tphere_command;
    public static boolean spawn_command;
    public static boolean gm_command;
    public static boolean invsee_command;
    public static boolean speed_command;
    public static boolean pweather_command;
    public static boolean ptime_command;
    public static boolean repair_command;
    public static boolean heal_command;
    public static boolean top_command;
    public static boolean spy_command;
    
    public static boolean enchants;
    public static boolean crafts;
    public static boolean displays;
    public static boolean quests;

    
    public static void init () {

        manager = new OstrovConfigManager(Ostrov.getInstance());

        loadConfigs();
        
        int currentDay = getDay();
        Ostrov.newDay = getVariable().getInt("last_day") != currentDay;
        if (Ostrov.newDay) {
            getVariable().set("last_day", Config.getDay());
            getVariable().saveConfig();
        }

        ostrovStatScore = config.getBoolean("player.show_ostrov_info_on_scoreboard");
        tablist_header_footer = config.getBoolean("player.set_tab_list_header_footer");
        tablist_name = config.getBoolean("player.set_tab_list_name");
        scale_health = config.getBoolean("player.scale_health");
        block_nether_portal = config.getBoolean("world.block_nether_portal");
//        disable_weather = config.getBoolean("world.disable_weather");
        disable_blockspread = config.getBoolean("world.disable_blockspread");
        disable_ice_melt = config.getBoolean("world.disable_ice_melt");
        
        set_gm = config.getBoolean("player.change_gamemode_on_join");
        gm_on_join = GameMode.valueOf(config.getString("player.gamemode_set_to") );
        walkspeed_on_join = Float.parseFloat(config.getString("player.walkspeed_on_join") );
        clear_stats = config.getBoolean("player.clear_stats");
        disable_void = config.getBoolean("player.disable_void");
        disable_damage = config.getBoolean("player.disable_damage");
        disable_hungry = config.getBoolean("player.disable_hungry");
        disable_break_place = config.getBoolean("player.disable_break_place");
        disable_lava = config.getBoolean("player.disable_lava");
        
        home_command=config.getBoolean("modules.command.home.use");
        fly_command=config.getBoolean("modules.command.fly.use");
//        fly_block_atack_on_fly=config.getBoolean("modules.command.fly.disable_atack_on_fly");
//        fly_off_on_damage=config.getBoolean("modules.command.fly.fly_off_on_damage");
        tpa_command=config.getInt("modules.command.tpa");
        save_location_on_world_change=config.getBoolean("modules.save_location_on_world_change");
        tpr_command=config.getInt("modules.command.tpr");
        back_command=config.getBoolean("modules.command.back");
        settings_command=config.getBoolean("modules.command.settings");
        get_command=config.getBoolean("modules.command.get");
        world_command=config.getBoolean("modules.command.world");
        tppos_command=config.getBoolean("modules.command.tppos");
        tphere_command=config.getBoolean("modules.command.tphere");
        spawn_command=config.getBoolean("modules.command.spawn");
        gm_command=config.getBoolean("modules.command.gm");
        invsee_command=config.getBoolean("modules.command.invsee");
        speed_command=config.getBoolean("modules.command.speed");
        pweather_command=config.getBoolean("modules.command.pweather");
        ptime_command=config.getBoolean("modules.command.ptime");
        repair_command=config.getBoolean("modules.command.repair");
        heal_command=config.getBoolean("modules.command.heal");
        top_command=config.getBoolean("modules.command.top");
        spy_command=config.getBoolean("modules.command.top");
        //nameTag=config.getBoolean("modules.name_tag_manager");

        enchants=config.getBoolean("modules.enchants");
        crafts=config.getBoolean("modules.crafts");
        displays=config.getBoolean("modules.displays");
        quests=config.getBoolean("modules.quests");
        BotManager.enable.set(config.getBoolean("modules.bots"));
        
    }    
    



public static void loadConfigs () {
        
    config = manager.getNewConfig("config.yml", new String[]{"", "Ostrov77 config file", ""} );
    variable = manager.getNewConfig("variable.yml");
    
    
    
    
    
    
    
    //Remove
   // config.removeKey("modules.command.pvp");
    //config.removeKey("player.keep_inventory");
   // config.removeKey("modules.command.warp");
    //config.removeKey("modules.command.shop");
    //config.removeKey("world.spawn");
    //config.removeKey("");""



    

    String[] c0 = {"---------", "player settings", "---------", "gamemode_set_to - SURVIVAL ADVENTURE CREATIVE SPECTATOR",
    "walkspeed_on_join - from 0.1F to 0.9F ; -1 to disable", "item_lobby_mode - cancel move,drop,drag gived item", ""}; 
    config.set("player.teleport_on_first_join", null);//config.addDefault("player.teleport_on_first_join", false, c0);
    config.addDefault("player.change_gamemode_on_join", false, c0);
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
    //config.addDefault("player.invulnerability_on_join_or_teleport", -1);
    config.addDefault("player.set_tab_list_header_footer", true);
    config.addDefault("player.set_tab_list_name", true);
    config.addDefault("player.scale_health", false);
    config.addDefault("player.disable_lava", false);
    config.addDefault("player.show_ostrov_info_on_scoreboard", false);

    
    String[] c1 = {"---------", "modules manager", "---------"}; 
//    config.addDefault("modules.name_tag_manager", false);
    config.addDefault("modules.enable_jump_plate", false, c1);
    config.addDefault("modules.teleport_gui", false);
    config.addDefault("modules.nbt_checker", false);
    
    config.addDefault("modules.enchants", false);
    config.addDefault("modules.crafts", false);
    config.addDefault("modules.displays", false);
    config.addDefault("modules.quests", false);
    config.addDefault("modules.bots", false);
    

    config.addDefault("modules.command.home.use", false);

    config.addDefault("modules.command.fly.use", false);
    config.addDefault("modules.command.fly.disable_atack_on_fly", false);
    config.addDefault("modules.command.fly.fly_off_on_damage", false);
    
    config.addDefault("modules.command.tpa", -1);
    config.addDefault("modules.save_location_on_world_change", false);
    config.addDefault("modules.command.tpr", -1, "random teleport. value - cooldown, -1 to disable.");

    config.addDefault("modules.command.hat", true);
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
    
    config.set("modules.command.warp.canSetPrivate", null);//config.addDefault("modules.command.warp.canSetPrivate", true);

    
    String[] c2 = {"---------", "world managment", "---------"}; 
    config.addDefault("world.disable_weather", false, c2);
    config.addDefault("world.disable_blockspread", false);
    config.addDefault("world.disable_ice_melt", false);
    
    
    String[] c3 = {"---------", "system settings", "---------"}; 
    config.addDefault("system.autorestart.use", true, c3);
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
    config.addDefault("system.use_armor_equip_event", false);
   
    
    
    //работа с БД глобальной
    String[] c4 = {"---------", "ostrov_database", "---------"}; 
    config.addDefault("ostrov_database.connect", false, c4);
    config.addDefault("ostrov_database.auto_reload_permissions", false);
    config.addDefault("ostrov_database.auto_reload_permissions_interval_min", 15);
    config.addDefault("ostrov_database.mysql_host", "jdbc:mysql://localhost/ostrov");
    config.addDefault("ostrov_database.mysql_user", "user");
    config.addDefault("ostrov_database.mysql_passw", "pass");
    //config.addDefault("ostrov_database.write_server_state_to_bungee_table", false);
    config.addDefault("ostrov_database.games_info_for_server_menu_load", false);
    config.addDefault("ostrov_database.games_info_for_server_menu_send", false);
    
    
    String[] c5 = {"---------", "local database", "---------"}; 
    config.addDefault("local_database.use", false, c5);
    config.addDefault("local_database.mysql_host", "jdbc:mysql://localhost/server");
    config.addDefault("local_database.mysql_user", "user");
    config.addDefault("local_database.mysql_passw", "pass");
    

    
    config.saveConfig();

    



    
    
    
    variable.addDefault("last_day", getDay());
    variable.saveConfig();

    
    

    }    
 



   
    
    public static void ReLoadAllConfig() {

        loadConfigs();

        OstrovDB.init(true, true);
        LocalDB.init();

    }
















    public static  OstrovConfig getConfig ( ) {
         return config;
     }  



    public static  OstrovConfig getVariable ( ) {
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
        } catch (Exception e) {
            Ostrov.log_err("Config copy error! "+e.getMessage());
        }
    }


    public static int getDay () {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }
    

}
