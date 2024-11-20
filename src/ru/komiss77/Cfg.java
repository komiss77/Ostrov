package ru.komiss77;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import org.bukkit.GameMode;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.entities.EntityManager;
import ru.komiss77.modules.items.ItemManager;
import ru.komiss77.modules.signProtect.SignProtectLst;


public class Cfg {


    public static final OConfigManager manager;

    private static OConfig cfg;
    private static OConfig variable;

    //для PM
    public static boolean ostrovStatScore = false;
    public static boolean tablist_header_footer = false;
    public static boolean tablist_name = false;
    public static boolean scale_health = false;
    public static int afk_sec = -1;

    //для ServerListener
    public static boolean clear_old_ents;
    public static boolean block_nether_portal;
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
    public static int tpa_command_delay;
    public static boolean save_location_on_world_change;
    public static int tpr_command_delay;
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
    //public static boolean quests; //как бы лишнее. какой-то плагин обратился - значит нужны. если нет адванскрези, будет работать заглушка

    static {
        manager = new OConfigManager(Ostrov.getInstance());
        init();
    }

    public static void init() {

        loadConfigs();

        int currentDay = getDay();
        Ostrov.newDay = getVariable().getInt("last_day") != currentDay;
        if (Ostrov.newDay) {
            getVariable().set("last_day", Cfg.getDay());
            getVariable().saveConfig();
        }

        ostrovStatScore = cfg.getBoolean("player.show_ostrov_info_on_scoreboard");
        tablist_header_footer = cfg.getBoolean("player.set_tab_list_header_footer");
        tablist_name = cfg.getBoolean("player.set_tab_list_name");
        scale_health = cfg.getBoolean("player.scale_health");
        block_nether_portal = cfg.getBoolean("world.block_nether_portal");
        disable_blockspread = cfg.getBoolean("world.disable_blockspread");
        disable_ice_melt = cfg.getBoolean("world.disable_ice_melt");
        clear_old_ents = cfg.getBoolean("world.clear_old_ents");

        afk_sec = cfg.getInt("player.afk_min") * 60;
        set_gm = cfg.getBoolean("player.change_gamemode_on_join");
        gm_on_join = GameMode.valueOf(cfg.getString("player.gamemode_set_to"));
        walkspeed_on_join = Float.parseFloat(cfg.getString("player.walkspeed_on_join"));
        clear_stats = cfg.getBoolean("player.clear_stats");
        disable_void = cfg.getBoolean("player.disable_void");
        disable_damage = cfg.getBoolean("player.disable_damage");
        disable_hungry = cfg.getBoolean("player.disable_hungry");
        disable_break_place = cfg.getBoolean("player.disable_break_place");
        disable_lava = cfg.getBoolean("player.disable_lava");

        home_command = cfg.getBoolean("modules.command.home.use");
        fly_command = cfg.getBoolean("modules.command.fly.use");
//        fly_block_atack_on_fly=config.getBoolean("modules.command.fly.disable_atack_on_fly");
//        fly_off_on_damage=config.getBoolean("modules.command.fly.fly_off_on_damage");
        tpa_command_delay = cfg.getInt("modules.command.tpa");
        save_location_on_world_change = cfg.getBoolean("modules.save_location_on_world_change");
        tpr_command_delay = cfg.getInt("modules.command.tpr");
        back_command = cfg.getBoolean("modules.command.back");
        settings_command = cfg.getBoolean("modules.command.settings");
        get_command = cfg.getBoolean("modules.command.get");
        world_command = cfg.getBoolean("modules.command.world");
        tppos_command = cfg.getBoolean("modules.command.tppos");
        tphere_command = cfg.getBoolean("modules.command.tphere");
        spawn_command = cfg.getBoolean("modules.command.spawn");
        gm_command = cfg.getBoolean("modules.command.gm");
        invsee_command = cfg.getBoolean("modules.command.invsee");
        speed_command = cfg.getBoolean("modules.command.speed");
        pweather_command = cfg.getBoolean("modules.command.pweather");
        ptime_command = cfg.getBoolean("modules.command.ptime");
        repair_command = cfg.getBoolean("modules.command.repair");
        heal_command = cfg.getBoolean("modules.command.heal");
        top_command = cfg.getBoolean("modules.command.top");
        spy_command = cfg.getBoolean("modules.command.top");
        //nameTag=config.getBoolean("modules.name_tag_manager");

        enchants = cfg.getBoolean("modules.enchants");
        crafts = cfg.getBoolean("modules.crafts");
        displays = cfg.getBoolean("modules.displays");
        //quests = cfg.getBoolean("modules.quests");
        BotManager.enable = cfg.getBoolean("modules.bots");
        EntityManager.enable = cfg.getBoolean("modules.entities");
        ItemManager.enable = cfg.getBoolean("modules.materials");
        SignProtectLst.enable = cfg.getBoolean("modules.signProtect");

    }


    public static void loadConfigs() {

        cfg = manager.getNewConfig("config.yml", new String[]{"", "Ostrov77 config file", ""});
        variable = manager.getNewConfig("variable.yml");

        //Remove
      //cfg.removeKey("");
      cfg.set("ostrov_database.mysql_host", null);
      cfg.set("ostrov_database.mysql_user", null);
      cfg.set("ostrov_database.mysql_passw", null);
      cfg.set("ostrov_database.games_info_for_server_menu_load", null);
      cfg.set("ostrov_database.games_info_for_server_menu_send", null);



        String[] c0 = {"---------", "player settings", "---------", "gamemode_set_to - SURVIVAL ADVENTURE CREATIVE SPECTATOR",
            "walkspeed_on_join - from 0.1F to 0.9F ; -1 to disable", "item_lobby_mode - cancel move,drop,drag gived item", ""};
        cfg.addDefault("player.change_gamemode_on_join", false, c0);
        cfg.addDefault("player.gamemode_set_to", "ADVENTURE");
        cfg.addDefault("player.walkspeed_on_join", "0.1F");
        cfg.addDefault("player.afk_min", -1);
        cfg.addDefault("player.clear_stats", false);
        cfg.addDefault("player.disable_void", false);
        cfg.addDefault("player.disable_damage", false);
        cfg.addDefault("player.disable_hungry", false);
        cfg.addDefault("player.disable_break_place", false);
        cfg.addDefault("player.item_lobby_mode", false);
        cfg.addDefault("player.block_fly_pvp", false);
        cfg.addDefault("player.give_pipboy", false, "выдавать часики при входе");
        cfg.addDefault("player.give_pipboy_slot", 0);
        //config.addDefault("player.invulnerability_on_join_or_teleport", -1);
        cfg.addDefault("player.set_tab_list_header_footer", true);
        cfg.addDefault("player.set_tab_list_name", true);
        cfg.addDefault("player.scale_health", false);
        cfg.addDefault("player.disable_lava", false);
        cfg.addDefault("player.show_ostrov_info_on_scoreboard", false);


        String[] c1 = {"---------", "modules manager", "---------"};
//    config.addDefault("modules.name_tag_manager", false);
        cfg.addDefault("modules.enable_jump_plate", false, c1);
        cfg.addDefault("modules.teleport_gui", false);
        cfg.addDefault("modules.nbt_checker", false);

        cfg.addDefault("modules.enchants", false);
        cfg.addDefault("modules.crafts", false);
        cfg.addDefault("modules.displays", false);
        cfg.addDefault("modules.quests", false);
        cfg.addDefault("modules.bots", false);
        cfg.addDefault("modules.entities", false);
        cfg.addDefault("modules.materials", false);
        cfg.addDefault("modules.signProtect", false);


        cfg.addDefault("modules.command.home.use", false);

        cfg.addDefault("modules.command.fly.use", false);
        cfg.addDefault("modules.command.fly.disable_atack_on_fly", false);
        cfg.addDefault("modules.command.fly.fly_off_on_damage", false);

        cfg.addDefault("modules.command.tpa", -1);
        cfg.addDefault("modules.save_location_on_world_change", false);
        cfg.addDefault("modules.command.tpr", -1, "random teleport. value - cooldown, -1 to disable.");

        cfg.addDefault("modules.command.hat", true);
        cfg.addDefault("modules.command.back", false);
        cfg.addDefault("modules.command.settings", false);
        cfg.addDefault("modules.command.get", false);
        cfg.addDefault("modules.command.world", false);
        cfg.addDefault("modules.command.tppos", false);
        cfg.addDefault("modules.command.tphere", false);
        cfg.addDefault("modules.command.spawn", false);
        cfg.addDefault("modules.command.gm", false);
        cfg.addDefault("modules.command.invsee", false);
        cfg.addDefault("modules.command.speed", false);
        cfg.addDefault("modules.command.pweather", false);
        cfg.addDefault("modules.command.ptime", false);

        cfg.addDefault("modules.command.heal", false);
        cfg.addDefault("modules.command.repair", false);
        cfg.addDefault("modules.command.spy", false);
        cfg.addDefault("modules.command.top", false);
        cfg.addDefault("modules.teleport_to_region_in_settings_menu", false);

        cfg.addDefault("modules.command.kit", false);
        cfg.addDefault("modules.command.menu", "serv");

        cfg.set("modules.command.warp.canSetPrivate", null);//config.addDefault("modules.command.warp.canSetPrivate", true);


        String[] c2 = {"---------", "world managment", "---------"};
        cfg.addDefault("world.disable_weather", false, c2);
        cfg.addDefault("world.disable_blockspread", false);
        cfg.addDefault("world.disable_ice_melt", false);
        cfg.addDefault("world.clear_old_ents", false);


        String[] c3 = {"---------", "system settings", "---------"};
        cfg.addDefault("system.autorestart.use", true, c3);
        cfg.addDefault("system.autorestart.hour", 3, "час рестарта. ");
        cfg.addDefault("system.autorestart.min", ApiOstrov.randInt(1, 59), "минута рестарта (при создании конфига-рандомная)");
        cfg.addDefault("system.pipboy_material", "CLOCK");
        cfg.addDefault("system.pipboy_name", "§a§lМеню сервера - нажми ПКМ!");
        cfg.addDefault("system.pipboy_rigth_click_command", "menu");
        cfg.addDefault("system.pipboy_left_click_command", "menu");
        cfg.addDefault("system.prefix.use_preffix_suffix_wothout_deluxechat", false); //работают когда нет делюксчата
        cfg.addDefault("system.prefix.prefix_name_space", "§2 "); //работают когда нет делюксчата
        cfg.addDefault("system.prefix.name_suffix_space", "§7 ");//работают когда нет делюксчата
        cfg.addDefault("system.prefix.suffix_message_space", "§7§o≫ §7");  //работают когда нет делюксчата
        cfg.addDefault("system.use_armor_equip_event", false);


        //работа с БД глобальной
        String[] c4 = {"---------", "ostrov_database", "---------"};
        cfg.addDefault("ostrov_database.connect", false, c4);
        cfg.addDefault("ostrov_database.auto_reload_permissions", false);
        cfg.addDefault("ostrov_database.auto_reload_permissions_interval_min", 15);
      //cfg.addDefault("ostrov_database.mysql_host", "jdbc:mysql://localhost/ostrov");
      //cfg.addDefault("ostrov_database.mysql_user", "user");
      //cfg.addDefault("ostrov_database.mysql_passw", "pass");
        //config.addDefault("ostrov_database.write_server_state_to_bungee_table", false);
      //cfg.addDefault("ostrov_database.games_info_for_server_menu_load", false);
      //cfg.addDefault("ostrov_database.games_info_for_server_menu_send", false);


        String[] c5 = {"---------", "local database", "---------"};
        cfg.addDefault("local_database.use", false, c5);
        cfg.addDefault("local_database.mysql_host", "jdbc:mysql://localhost/server");
        cfg.addDefault("local_database.mysql_user", "user");
        cfg.addDefault("local_database.mysql_passw", "pass");


        cfg.saveConfig();


        variable.addDefault("last_day", getDay());
        variable.saveConfig();


    }


    public static void ReLoadAllConfig() {

        loadConfigs();

        RemoteDB.init(true, true);
        LocalDB.init();

    }


    public static OConfig getConfig() {
        return cfg;
    }


    public static OConfig getVariable() {
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
            Ostrov.log_err("Config copy error! " + e.getMessage());
        }
    }


    public static int getDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }


}
