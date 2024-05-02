package ru.komiss77;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import org.bukkit.GameMode;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.entities.EntityManager;
import ru.komiss77.modules.signProtect.SignProtectLst;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.utils.OstrovConfigManager;


public class Config {

    
    public static OstrovConfigManager manager;

    private static OstrovConfig ostrovConfig;
    private static OstrovConfig variable;
    
    //для PM
    public static boolean ostrovStatScore = false;    
    public static boolean tablist_header_footer = false;    
    public static boolean tablist_name = false;    
    public static boolean scale_health = false;
    //public static boolean nameTag = false;    

    //для ServerListener
    public static boolean clear_old_ents;
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

        ostrovStatScore = ostrovConfig.getBoolean("player.show_ostrov_info_on_scoreboard");
        tablist_header_footer = ostrovConfig.getBoolean("player.set_tab_list_header_footer");
        tablist_name = ostrovConfig.getBoolean("player.set_tab_list_name");
        scale_health = ostrovConfig.getBoolean("player.scale_health");
        block_nether_portal = ostrovConfig.getBoolean("world.block_nether_portal");
//        disable_weather = config.getBoolean("world.disable_weather");
        disable_blockspread = ostrovConfig.getBoolean("world.disable_blockspread");
        disable_ice_melt = ostrovConfig.getBoolean("world.disable_ice_melt");
        clear_old_ents = ostrovConfig.getBoolean("world.clear_old_ents");
        
        set_gm = ostrovConfig.getBoolean("player.change_gamemode_on_join");
        gm_on_join = GameMode.valueOf(ostrovConfig.getString("player.gamemode_set_to") );
        walkspeed_on_join = Float.parseFloat(ostrovConfig.getString("player.walkspeed_on_join") );
        clear_stats = ostrovConfig.getBoolean("player.clear_stats");
        disable_void = ostrovConfig.getBoolean("player.disable_void");
        disable_damage = ostrovConfig.getBoolean("player.disable_damage");
        disable_hungry = ostrovConfig.getBoolean("player.disable_hungry");
        disable_break_place = ostrovConfig.getBoolean("player.disable_break_place");
        disable_lava = ostrovConfig.getBoolean("player.disable_lava");
        
        home_command= ostrovConfig.getBoolean("modules.command.home.use");
        fly_command= ostrovConfig.getBoolean("modules.command.fly.use");
//        fly_block_atack_on_fly=config.getBoolean("modules.command.fly.disable_atack_on_fly");
//        fly_off_on_damage=config.getBoolean("modules.command.fly.fly_off_on_damage");
        tpa_command= ostrovConfig.getInt("modules.command.tpa");
        save_location_on_world_change= ostrovConfig.getBoolean("modules.save_location_on_world_change");
        tpr_command= ostrovConfig.getInt("modules.command.tpr");
        back_command= ostrovConfig.getBoolean("modules.command.back");
        settings_command= ostrovConfig.getBoolean("modules.command.settings");
        get_command= ostrovConfig.getBoolean("modules.command.get");
        world_command= ostrovConfig.getBoolean("modules.command.world");
        tppos_command= ostrovConfig.getBoolean("modules.command.tppos");
        tphere_command= ostrovConfig.getBoolean("modules.command.tphere");
        spawn_command= ostrovConfig.getBoolean("modules.command.spawn");
        gm_command= ostrovConfig.getBoolean("modules.command.gm");
        invsee_command= ostrovConfig.getBoolean("modules.command.invsee");
        speed_command= ostrovConfig.getBoolean("modules.command.speed");
        pweather_command= ostrovConfig.getBoolean("modules.command.pweather");
        ptime_command= ostrovConfig.getBoolean("modules.command.ptime");
        repair_command= ostrovConfig.getBoolean("modules.command.repair");
        heal_command= ostrovConfig.getBoolean("modules.command.heal");
        top_command= ostrovConfig.getBoolean("modules.command.top");
        spy_command= ostrovConfig.getBoolean("modules.command.top");
        //nameTag=config.getBoolean("modules.name_tag_manager");

        enchants= ostrovConfig.getBoolean("modules.enchants");
        crafts= ostrovConfig.getBoolean("modules.crafts");
        displays= ostrovConfig.getBoolean("modules.displays");
        quests= ostrovConfig.getBoolean("modules.quests");
        BotManager.enable= ostrovConfig.getBoolean("modules.bots");
        EntityManager.enable= ostrovConfig.getBoolean("modules.entities");
        SignProtectLst.enable= ostrovConfig.getBoolean("modules.signProtect");

    }    
    



public static void loadConfigs () {
        
    ostrovConfig = manager.getNewConfig("config.yml", new String[]{"", "Ostrov77 config file", ""} );
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
    ostrovConfig.set("player.teleport_on_first_join", null);//config.addDefault("player.teleport_on_first_join", false, c0);
    ostrovConfig.addDefault("player.change_gamemode_on_join", false, c0);
    ostrovConfig.addDefault("player.gamemode_set_to", "ADVENTURE" );
    ostrovConfig.addDefault("player.walkspeed_on_join", "0.1F");
    ostrovConfig.addDefault("player.clear_stats", false);
    ostrovConfig.addDefault("player.disable_void", false);
    ostrovConfig.addDefault("player.disable_damage", false);
    ostrovConfig.addDefault("player.disable_hungry", false);
    ostrovConfig.addDefault("player.disable_break_place", false);
    ostrovConfig.addDefault("player.item_lobby_mode", false);
    ostrovConfig.addDefault("player.block_fly_pvp", false);
    ostrovConfig.addDefault("player.give_pipboy", false,"выдавать часики при входе");
    ostrovConfig.addDefault("player.give_pipboy_slot", 0);
    //config.addDefault("player.invulnerability_on_join_or_teleport", -1);
    ostrovConfig.addDefault("player.set_tab_list_header_footer", true);
    ostrovConfig.addDefault("player.set_tab_list_name", true);
    ostrovConfig.addDefault("player.scale_health", false);
    ostrovConfig.addDefault("player.disable_lava", false);
    ostrovConfig.addDefault("player.show_ostrov_info_on_scoreboard", false);

    
    String[] c1 = {"---------", "modules manager", "---------"}; 
//    config.addDefault("modules.name_tag_manager", false);
    ostrovConfig.addDefault("modules.enable_jump_plate", false, c1);
    ostrovConfig.addDefault("modules.teleport_gui", false);
    ostrovConfig.addDefault("modules.nbt_checker", false);
    
    ostrovConfig.addDefault("modules.enchants", false);
    ostrovConfig.addDefault("modules.crafts", false);
    ostrovConfig.addDefault("modules.displays", false);
    ostrovConfig.addDefault("modules.quests", false);
    ostrovConfig.addDefault("modules.bots", false);
    ostrovConfig.addDefault("modules.entities", false);
    ostrovConfig.addDefault("modules.signProtect", false);


    ostrovConfig.addDefault("modules.command.home.use", false);

    ostrovConfig.addDefault("modules.command.fly.use", false);
    ostrovConfig.addDefault("modules.command.fly.disable_atack_on_fly", false);
    ostrovConfig.addDefault("modules.command.fly.fly_off_on_damage", false);
    
    ostrovConfig.addDefault("modules.command.tpa", -1);
    ostrovConfig.addDefault("modules.save_location_on_world_change", false);
    ostrovConfig.addDefault("modules.command.tpr", -1, "random teleport. value - cooldown, -1 to disable.");

    ostrovConfig.addDefault("modules.command.hat", true);
    ostrovConfig.addDefault("modules.command.back", false);
    ostrovConfig.addDefault("modules.command.settings", false);
    ostrovConfig.addDefault("modules.command.get", false);
    ostrovConfig.addDefault("modules.command.world", false);
    ostrovConfig.addDefault("modules.command.tppos", false);
    ostrovConfig.addDefault("modules.command.tphere", false);
    ostrovConfig.addDefault("modules.command.spawn", false);
    ostrovConfig.addDefault("modules.command.gm", false);
    ostrovConfig.addDefault("modules.command.invsee", false);
    ostrovConfig.addDefault("modules.command.speed", false);
    ostrovConfig.addDefault("modules.command.pweather", false);
    ostrovConfig.addDefault("modules.command.ptime", false);

    ostrovConfig.addDefault("modules.command.heal", false);
    ostrovConfig.addDefault("modules.command.repair", false);
    ostrovConfig.addDefault("modules.command.spy", false);
    ostrovConfig.addDefault("modules.command.top", false);
    ostrovConfig.addDefault("modules.teleport_to_region_in_settings_menu", false);

    ostrovConfig.addDefault("modules.command.kit", false);
    ostrovConfig.addDefault("modules.command.menu", "serv");
    
    ostrovConfig.set("modules.command.warp.canSetPrivate", null);//config.addDefault("modules.command.warp.canSetPrivate", true);

    
    String[] c2 = {"---------", "world managment", "---------"}; 
    ostrovConfig.addDefault("world.disable_weather", false, c2);
    ostrovConfig.addDefault("world.disable_blockspread", false);
    ostrovConfig.addDefault("world.disable_ice_melt", false);
    ostrovConfig.addDefault("world.clear_old_ents", false);
    
    
    String[] c3 = {"---------", "system settings", "---------"}; 
    ostrovConfig.addDefault("system.autorestart.use", true, c3);
    ostrovConfig.addDefault("system.autorestart.hour", 3, "час рестарта. ");
    ostrovConfig.addDefault("system.autorestart.min", ApiOstrov.randInt(1, 59), "минута рестарта (при создании конфига-рандомная)");
    ostrovConfig.addDefault("system.pipboy_material", "CLOCK");
    ostrovConfig.addDefault("system.pipboy_name", "§a§lМеню сервера - нажми ПКМ!");
    ostrovConfig.addDefault("system.pipboy_rigth_click_command", "menu");
    ostrovConfig.addDefault("system.pipboy_left_click_command", "menu");
    ostrovConfig.addDefault("system.prefix.use_preffix_suffix_wothout_deluxechat", false); //работают когда нет делюксчата
    ostrovConfig.addDefault("system.prefix.prefix_name_space", "§2 "); //работают когда нет делюксчата
    ostrovConfig.addDefault("system.prefix.name_suffix_space", "§7 ");//работают когда нет делюксчата
    ostrovConfig.addDefault("system.prefix.suffix_message_space", "§7§o≫ §7");  //работают когда нет делюксчата
    ostrovConfig.addDefault("system.use_armor_equip_event", false);
   
    
    
    //работа с БД глобальной
    String[] c4 = {"---------", "ostrov_database", "---------"}; 
    ostrovConfig.addDefault("ostrov_database.connect", false, c4);
    ostrovConfig.addDefault("ostrov_database.auto_reload_permissions", false);
    ostrovConfig.addDefault("ostrov_database.auto_reload_permissions_interval_min", 15);
    ostrovConfig.addDefault("ostrov_database.mysql_host", "jdbc:mysql://localhost/ostrov");
    ostrovConfig.addDefault("ostrov_database.mysql_user", "user");
    ostrovConfig.addDefault("ostrov_database.mysql_passw", "pass");
    //config.addDefault("ostrov_database.write_server_state_to_bungee_table", false);
    ostrovConfig.addDefault("ostrov_database.games_info_for_server_menu_load", false);
    ostrovConfig.addDefault("ostrov_database.games_info_for_server_menu_send", false);
    
    
    String[] c5 = {"---------", "local database", "---------"}; 
    ostrovConfig.addDefault("local_database.use", false, c5);
    ostrovConfig.addDefault("local_database.mysql_host", "jdbc:mysql://localhost/server");
    ostrovConfig.addDefault("local_database.mysql_user", "user");
    ostrovConfig.addDefault("local_database.mysql_passw", "pass");
    

    
    ostrovConfig.saveConfig();

    



    
    
    
    variable.addDefault("last_day", getDay());
    variable.saveConfig();

    
    

    }    
 



   
    
    public static void ReLoadAllConfig() {

        loadConfigs();

        OstrovDB.init(true, true);
        LocalDB.init();

    }
















    public static  OstrovConfig getConfig ( ) {
         return ostrovConfig;
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
