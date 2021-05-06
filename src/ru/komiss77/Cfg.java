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
import ru.komiss77.Managers.Warps;
import ru.komiss77.modules.OstrovDB;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.utils.OstrovConfigManager;







public class Cfg {

    
    public static OstrovConfigManager manager;
    private static OstrovConfig config;
    
    //private static OstrovConfig messages;
    
    //private static OstrovConfig shop;
    
    private static OstrovConfig variable;
    //public static OstrovConfig default_perms;
    
    public static String chanelName="ostrov:ostrov"; //не переносить, ругается банжи-часть на букит!! - дубль в OstrovBungee
    
    public static void Init () {

        File kit = new File(Ostrov.instance.getDataFolder(), "kits.yml");
        if (!kit.exists()) {
            copy(Ostrov.instance.getResource("kits.yml"), kit);
        }
        
        manager = new OstrovConfigManager(Ostrov.GetInstance());

        LoadConfigs();

    }    
    















public static void LoadConfigs () {
        
    config = manager.getNewConfig("config.yml", new String[]{"", "Ostrov77 config file", ""} );
    
    //messages = manager.getNewConfig("messages.yml", new String[]{"", "Ostrov77 config file", ""});
        
   // shop = manager.getNewConfig("shop.yml", new String[]{"", "Ostrov77 shop file", ""});
        
    
          
    variable = manager.getNewConfig("variable.yml");
    
    //default_perms = manager.getNewConfig("default_perms.yml", new String[]{"", "Права по умолчанию для всех игроков на этом сервере", ""} );
    
    
    
    
    
    
    //Remove
    config.removeKey("modules.score_board");
    config.removeKey("modules.block_commands.enable");
    config.removeKey("modules.block_commands.except" );
    config.removeKey("modules.command.pvp");

    
    
    
    
    
    

    String[] c0 = {"---------", "player settings", "---------", "gamemode_set_to - SURVIVAL ADVENTURE CREATIVE SPECTATOR",
    "walkspeed_on_join - from 0.1F to 0.9F ; -1 to disable", "item_lobby_mode - cancel move,drop,drag gived item", ""}; 
    config.addDefault("player.teleport_on_first_join", false, c0);
    config.addDefault("player.change_gamemode_on_join", false);
    config.addDefault("player.gamemode_set_to", "ADVENTURE" );
    config.addDefault("player.walkspeed_on_join", "0.1F");
    config.addDefault("player.clear_stats", false);
    config.addDefault("player.keep_inventory", false);
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

    config.addDefault("modules.command.warp.use", false);
    config.addDefault("modules.command.warp.сonsoleOnlyUse", false);
    config.addDefault("modules.command.warp.amount_per_group.default", 0);
    config.addDefault("modules.command.warp.amount_per_group.premium", 3);
    config.addDefault("modules.command.warp.amount_per_group.supermoder", 10);

    config.addDefault("modules.command.heal", false);
    config.addDefault("modules.command.repair", false);
    config.addDefault("modules.command.spy", false);
    config.addDefault("modules.command.top", false);
    config.addDefault("modules.teleport_to_region_in_settings_menu", false);

    config.addDefault("modules.command.kit", false);
    config.addDefault("modules.command.shop", false);
    config.addDefault("modules.command.menu", "serv");

    
    String[] c2 = {"---------", "world managment", "---------"}; 
    config.addDefault("world.spawn.set_spawn_point_world0", false, c2);
    //config.addDefault("world.spawn.set_spawn_point_for_world", "world");
    config.addDefault("world.spawn.set_spawn_point.x", 0);
    config.addDefault("world.spawn.set_spawn_point.y", 65);
    config.addDefault("world.spawn.set_spawn_point.z", 0);
    config.addDefault("world.spawn.set_spawn_point.y", 0);
    config.addDefault("world.spawn.set_spawn_point.p", 0);
    config.addDefault("world.block_nether_portal", false);
    config.addDefault("world.block_ender_portal", false);
    config.addDefault("world.block_day_night_change", false);
    config.addDefault("world.set_time_to", 1000);
    config.addDefault("world.disable_weather", false);
    config.addDefault("world.disable_blockspread", false);
    config.addDefault("world.disable_ice_melt", false);
    config.addDefault("world.disable_moob_griefing", false);
    config.addDefault("world.disable_moob_loot", false);
    config.addDefault("world.disable_entity_drops", false);
    //config.addDefault("world.empty_chunk_generator", false);
    
    
    String[] c3 = {"---------", "system settings", "---------"}; 
    //config.addDefault("system.reset_player_data_on_spigot_npf", false);
    config.addDefault("system.autorestart.use", false);
    config.addDefault("system.autorestart.time", "03:40");
    config.addDefault("system.pipboy_material", "CLOCK");
    config.addDefault("system.pipboy_name", "§a§lМеню сервера - нажми ПКМ!");
    config.addDefault("system.pipboy_rigth_click_command", "menu");
    config.addDefault("system.pipboy_left_click_command", "menu");
    //config.addDefault("system.cosmetic_name", "§6§lПримочки");
    config.addDefault("system.prefix.use_preffix_suffix_wothout_deluxechat", false); //работают когда нет делюксчата
    config.addDefault("system.prefix.prefix_name_space", "§2 "); //работают когда нет делюксчата
    config.addDefault("system.prefix.name_suffix_space", "§7 ");//работают когда нет делюксчата
    config.addDefault("system.prefix.suffix_message_space", "§7§o≫ §7");  //работают когда нет делюксчата  
    
    
    
    //работа с БД глобальной
    String[] c4 = {"---------", "ostrov_database", "---------"}; 
    config.addDefault("ostrov_database.connect", false);
    config.addDefault("ostrov_database.auto_reload_permissions", false);
    config.addDefault("ostrov_database.auto_reload_permissions_interval_min", 15);
    config.addDefault("ostrov_database.mysql_host", "jdbc:mysql://localhost/ostrov77");
    config.addDefault("ostrov_database.mysql_user", "user");
    config.addDefault("ostrov_database.mysql_passw", "pass");
    config.addDefault("ostrov_database.write_server_state_to_bungee_table", false);
    config.addDefault("ostrov_database.games_info_for_server_menu_load", false);
    config.addDefault("ostrov_database.games_info_for_server_menu_load_interval_ticks", 20);
    config.addDefault("ostrov_database.games_info_for_server_menu_send", false);
    
    
    String[] c5 = {"---------", "local database", "---------"}; 
    config.addDefault("local_database.use", false);
    config.addDefault("local_database.mysql_host", "jdbc:mysql://localhost/server");
    config.addDefault("local_database.mysql_user", "user");
    config.addDefault("local_database.mysql_passw", "pass");
    

    
    config.saveConfig();

    


    
    
    















    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    


    
    
    /*
    messages.addDefault("ostrov.command_deny_message", "§6§l/menu §f- открыть главне меню");
    //messages.addDefault("tpgui.name", "Телепортер");
    messages.addDefault("tpgui.name", "Телепортер");
    //messages.addDefault("tpgui.req", "§2TP request");
    messages.addDefault("tpgui.req", "§2Запрос на ТП");
    //messages.addDefault("tpgui.unban", "Разбан");
    messages.addDefault("tpgui.unban", "Разбан");
    //messages.addDefault("tpgui.disconn", " has disconnected!");
    messages.addDefault("tpgui.disconn", "§c %n отключился!");
    //messages.addDefault("tpgui.cant_rec", " can not accept your request! Try later!");
    messages.addDefault("tpgui.cant_rec", "§c %n не может принять ваш запрос! Попробуйте позже!");
    //messages.addDefault("tpgui.ban_msg_sender", "§cYoy banned %n ! For unban, type /tpa unban!");
    messages.addDefault("tpgui.ban_msg_sender", "§cВы забанили %n! Для разбана наберите §6/tpa unban");
    //messages.addDefault("tpgui.ban_msg_reciever", "§cYoy in banned %n !");
    messages.addDefault("tpgui.ban_msg_reciever", "§cВы больше не сможете ТП к %n! Он Вас забанил!");
    //messages.addDefault("tpgui.tpgui_banlist_empty", "§cYour ban-list is empty!");
    messages.addDefault("tpgui.tpgui_banlist_empty", "§cВаш список забаненных пуст!");
    //messages.addDefault("tpgui.tpgui_banlist_rem", "§a%n remove you from teleport ban-list!");
    messages.addDefault("tpgui.tpgui_banlist_rem", "§a%n удалил Вас из бан-листа!");
    //messages.addDefault("tpgui.exit", "§cexit");
    messages.addDefault("tpgui.exit", "§cexit");
    //messages.addDefault("tpgui.aacept", "§caacept");
    messages.addDefault("tpgui.aacept", "§cПринять");
    //messages.addDefault("tpgui.deny", "§cdeny");
    messages.addDefault("tpgui.deny", "§cОтказать");
    //messages.addDefault("tpgui.ban", "§cban");
    messages.addDefault("tpgui.ban", "§cЗаблокировать");
    
    
    
    
    
    messages.saveConfig();*/

    
    
    
    
    
    
    
    
    
    
    
    
    
  /*  
    String[] c6 = { "perm_name - request permissions ostrov.kits.<perm_name>,"," empty for no perm",  "acces_price = price for buy to acces to kit, -1 for free",
    "give_price = price for give kit, -1 for free", "delay_min - interval betveen give kit in min.", "first item - demo for gui-menu", "id<>ammount<>name<>lore<>enchant<>color<>effect<>end" ,"lore:&fline1:&bline2:...", "set durability, data or color- id:0-15<>ammount<>...",
    "enchant you can use https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html", "color- BLACK, AQUA, BLUE, FUCHSIA, GRAY, GREEN, LIME, MAROON,","NAVY, OLIVE, ORANGE, PURPLE, RED, SILVER, TEAL, WHITE, YELLOW",
    "effect:TYPE:duration:amplifier","all potionEffect https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html",
    "egg:TYPE  all eggtype https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html"};  
    
    kits.addDefault("kits", "help", c6 );
    
    kits.addDefault("kits.start.perm_name", "" );
    kits.addDefault("kits.start.acces_price", -1 );
    kits.addDefault("kits.start.give_price", 100 );
    kits.addDefault("kits.start.delay_min", 86400 );
    kits.addDefault("kits.start.items", Arrays.asList( "268<>1<>name:&2Стартовый<>lore:&7Набор, доступный всем:&7Получить можно:&7один раз в сутки.<>end", "17:1<>64<>end", "274<>1<>end", "272<>1<>end", "264<>1<>end", "364<>20<>end", "360<>3<>end" ) );

    kits.addDefault("kits.food.perm_name", "" );
    kits.addDefault("kits.food.acces_price", 10000 );
    kits.addDefault("kits.food.give_price", 1000 );
    kits.addDefault("kits.food.delay_min", 3600 );
    kits.addDefault("kits.food.items", Arrays.asList( "260<>1<>name:&5Обед с курицей<>lore:&7Набор еды:&7Интервал - час.<>end", "282:1<>64<>end", "297<>1<>end", "366<>1<>end", "391<>1<>end", "354<>1<>end", "260<>2<>end" ) );

    kits.addDefault("kits.vip.perm_name", "vip" );
    kits.addDefault("kits.vip.acces_price", -1 );
    kits.addDefault("kits.vip.give_price", 1000 );
    kits.addDefault("kits.vip.delay_min", 86400 );
    kits.addDefault("kits.vip.items", Arrays.asList( "283<>1<>name:&6Вип<>lore:&7Вип-Набор:&7Получить можно:&7один раз в сутки.<>enchant:ARROW_DAMAGE:1<>end", "264<>5<>end", "322<>1<>end", "388<>3<>end", "278<>1<>end", "266<>7<>end" ) );
    
    kits.saveConfig();
    */
   
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    //shop.addDefault("shop.server", "" );
    //shop.saveConfig();
    
    
    
    
    
    
      

    
    
    
    
    
    
    variable.addDefault("last_day", Get_day());
    variable.saveConfig();

    
    
    
    
    
  //  default_perms.addDefault("default_permissions", Arrays.asList( "deluxechat.utf","deluxechat.pm", "deluxechat.bungee.chat", "deluxechat.bungee.toggle",
   //         "chestcommands.command.open", "chestcommands.open.menu.yml") );
   // default_perms.saveConfig();
    
    
    }    
 



   
    
    public static void ReLoadAllConfig() {

        LoadConfigs();
        //GetVar();

        CMD.ReLoadVars();
        OstrovDB.reload();
        ServerListener.ReloadVars();
        PlayerListener.ReloadVars();
        MenuListener.ReloadVars();
        TPAListener.ReloadVars();

        PM.ReLoadVars();
        MysqlLocal.ReloadVars();
        Warps.ReLoadVars();
        

        Timer.ReLoadVars();
        Pvp.reload();


        //if (Ostrov.pandora!=null) Ostrov.pandora.Reload();

        //if (Ostrov.servers!=null) Ostrov.servers.Reload();

        //LimiterListener.init();
        //if ( spawn_limiter.getBoolean("mob_limiter.enable") ) {
        //    LimiterListener.use = true;
       //     Ostrov.log_ok ("§2Моб-лмитер активен!");
        //} else LimiterListener.use = false;

        //if ( config.getBoolean("world.block_ender_portal") ) {
       //     EnderPortalListener.use = true;
        //    Ostrov.log_ok ("§2Эндэр-порталы заблокированы!");
       // } else EnderPortalListener.use = false;

    }
















    public static  OstrovConfig GetCongig ( ) {
         return config;
     }  

    //public static  String GetMsg ( String m ) {
    //     return messages.getString(m).replaceAll("&", "§");
    // }  


  //  public static  OstrovConfig GetShop ( ) {
  //       return shop;
   //  }  


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
    
 /*   
private static void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) { Ostrov.log_err("saveConfig error! "+e.getMessage()); }
     }
  */  
}
