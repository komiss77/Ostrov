package ru.komiss77;



import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import java.util.Collection;
import me.clip.deluxechat.DeluxeChat;
import net.citizensnpcs.Citizens;
import ru.komiss77.commands.RegisterCommands;
import ru.komiss77.commands.CMD;
import ru.komiss77.commands.WorldManagerCmd;
import ru.komiss77.enums.Chanell;
import ru.komiss77.modules.kits.KitManager;
import ru.komiss77.listener.ArmorEquipListener;
import ru.komiss77.listener.InvSeeListener;
import ru.komiss77.listener.LimiterListener;
import ru.komiss77.listener.MenuListener;
import ru.komiss77.listener.NbtListener;
import ru.komiss77.listener.PlayerListener;
import ru.komiss77.listener.ResourcePacks;
import ru.komiss77.listener.ServerListener;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.listener.TPAListener;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.profile.PassportHandler__;
import ru.komiss77.modules.DchatExpansion;
import ru.komiss77.modules.world.EmptyChunkGenerator;
import ru.komiss77.modules.world.WE;
import ru.komiss77.modules.warp.WarpManager;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.modules.Informator;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.modules.Pandora;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.inventory.InventoryAPI;
import ru.komiss77.version.VM;
import ru.ostrov77.factions.ApiFactions;
import ru.ostrov77.friends.ApiFriends;


public class Ostrov extends JavaPlugin {
    
    public static Ostrov instance;
    public static Citizens citizens=null;
    public static DeluxeChat deluxechatPlugin=null;
    public static WorldGuardPlatform worldguard_platform=null;
    public static Random random;
    public static ApiFriends api_friends=null;
    public static ApiFactions apiFactions=null;
    
    private static Map <Module,Object> modules;
    public static VM VM; //versionManager
    protected static WE worldEditor;
    protected static WorldManager worldManager;
    protected static GM gameManager;
    protected static MenuItemsManager menuItems;
    protected static KitManager kitManager;
    
    @Deprecated
    public static InventoryAPI inventoryAPI; //сделать приват
    @Deprecated
    public static PlayerInput playerChatInput; //сделать приват
    
    public static String prefix = "§2[§aОстров§2] §f";;
    public static int server_id=-1;
    public static boolean use_vault,powerNBT,langUtils;
    
    public static boolean новый_день;
    public static boolean first_start=true;
    
    
    public static Calendar calendar;
    
    private static Date date;
    private static SimpleDateFormat full_sdf;
    private static SimpleDateFormat hour_min_sdf;

    
    
    
    @Override
    public void onLoad() {
        instance = this;
        random=new Random();
        modules = new HashMap<>();//new CaseInsensitiveMap<>();
        calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        
        date = new java.util.Date(System.currentTimeMillis());
        full_sdf = new java.text.SimpleDateFormat("dd.MM.yy HH:mm");
        hour_min_sdf = new java.text.SimpleDateFormat("HH:mm");
        
        VM = new VM(this);
    }
    
  
    
    @Override
    public void onEnable() {
        
        
        
        for (final Chanell ch : Chanell.values()) {
            instance.getServer().getMessenger().registerOutgoingPluginChannel(Ostrov.GetInstance(), ch.name );
            instance.getServer().getMessenger().registerIncomingPluginChannel(Ostrov.GetInstance(), ch.name, new SpigotChanellMsg() );
        }
        log_ok ("§5Регистрация канала BungeeCord");
        
         //до проверки на Режим Auth !
        playerChatInput = new PlayerInput(); //регион ГУИ, скайблок
        inventoryAPI = new InventoryAPI();
        
        Cfg.Init(); // 1 !
        
        if (Bukkit.getMotd().length()==3) {
            log_warn("§bРежим Auth");
            OstrovDB.init();
            Timer.init(true);
            try {
                gameManager = (GM) Module.gameManager.clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException | NullPointerException ex) {
                log_err("инициализацяя SM : "+ex.getMessage());
                ex.printStackTrace();
                Bukkit.shutdown();
            }
            Bukkit.getPluginManager().registerEvents(new SpigotChanellMsg(), this);
            return;
        }




        новый_день=Cfg.GetVariable().getInt("last_day")!=Cfg.Get_day();
        if (новый_день) {
            Cfg.GetVariable().set("last_day", Cfg.Get_day());
            Cfg.GetVariable().saveConfig();
        }

        final int worldEndWipeAt = Cfg.GetVariable().getInt("worldEndMarkToWipe", 0);
        if (worldEndWipeAt>0 && worldEndWipeAt<ApiOstrov.currentTimeSec()) {
            Cfg.GetVariable().set("worldEndMarkToWipe",0);
            Cfg.GetVariable().saveConfig();
            
            final File endWorldFolder = new File(Bukkit.getWorldContainer().getPath()+"/world_the_end");
            WorldManagerCmd.deleteFile(endWorldFolder);
            //seed ??
            
            log_warn("Край обнулён.");
        }

        RegisterCommands.register(this);
        
        

        log_ok ("§5Регистрация слушателей:");
        //Bukkit.getPluginManager().registerEvents(new WE(this), this);
        Bukkit.getPluginManager().registerEvents(new SpigotChanellMsg(), this); //в режиме AUTH инициализация дубль выше
        Bukkit.getPluginManager().registerEvents(new TPAListener(), this);
        Bukkit.getPluginManager().registerEvents(new ServerListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new MenuListener(), this);
        Bukkit.getPluginManager().registerEvents(new InvSeeListener(), this);
        Bukkit.getPluginManager().registerEvents(new ArmorEquipListener(), this);
        Bukkit.getPluginManager().registerEvents(new PassportHandler__(this), this);

        if (Cfg.GetCongig().getBoolean("modules.nbt_checker")) {
            Bukkit.getPluginManager().registerEvents(new NbtListener(instance), instance);
            log_ok ("§eКонтроль NBT тэгов включен.");
        }
        
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
              new DchatExpansion(this).register();
        }
       
        
        if (!first_start) {
            Bukkit.getOnlinePlayers().stream().forEach((p) -> {
                PM.getOplayer(p);
            });
        }
        first_start = false;

        
        
        log_ok ("§5Инициализация модулей:");
        
        ItemUtils.LoadItem();
        CMD.Init();
        OstrovDB.init(); //выше есть для auth
        LocalDB.Init();// выше есть для auth
        ServerListener.Init();
        PlayerListener.Init();
        MenuListener.Init();
        TPAListener.Init();
        PM.Init();
        Timer.init(false); //выше, для auth
        
        for (final Module module : Module.values()) {
//System.out.println("-------------------- "+module);
            try {
                modules.put(module, module.clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException | NullPointerException ex) {
                log_err("инициализацяя "+module+" : "+ex.getMessage());
                ex.printStackTrace();
            }
        }
         
        gameManager = (GM) modules.get(Module.gameManager);//servers.on_start();
        menuItems = (MenuItemsManager) modules.get(Module.menuItems);
        kitManager = (KitManager) modules.get(Module.kitManager);
        worldManager = (WorldManager) modules.get(Module.worldManager);//servers.on_start();
        worldEditor = (WE) modules.get(Module.worldEditor);//servers.on_start();
        //playerChatInput = new PlayerInput(); //регион ГУИ, скайблок
        //inventoryAPI = new InventoryAPI(this, false);
        
        
        log_ok ("§2Остров готов к работе!");
        

    }
 
    
    
    
    public enum Module {
        resourcePacks (ResourcePacks.class),
        limiterListener (LimiterListener.class),
        gameManager (GM.class),
        menuItems (MenuItemsManager.class),
        kitManager (KitManager.class),
        pandora (Pandora.class),
        informator (Informator.class),
        warps (WarpManager.class),
        worldManager (WorldManager.class),
        worldEditor (WE.class),
        ;
        
        public Class clazz;
        
        private Module (final Class clazz) {
            this.clazz = clazz;
        }
    }


    public static Collection<Object> getModules() {
        return modules.values();
    }

    public static Object getModule(final Module mod) {
        return modules.get(mod);
    }



    
    
    
    
    @Override
    public void onDisable() {
        if (Bukkit.getMotd().length()==3) return;
        PM.onDisable();
        //servers.on_shut_down();
        OstrovDB.Disconnect();
        LocalDB.Disconnect();

        modules.values().stream().forEach( (module)->((Initiable)module).onDisable());
        
        log_ok("§4Остров выгружен!");
    }  

    public static void WorldGuard_get(){
        if(Bukkit.getPluginManager().getPlugin("WorldGuard") != null){
            log_ok("Подключен WorldGuard !");
            worldguard_platform =  WorldGuard.getInstance().getPlatform();
        } else {
            log_ok("§eWorldGuard не найден!");
            worldguard_platform = null;
        }
    } 
     
     
    @Override
    public boolean onCommand(CommandSender cs, Command comm, String s, String[] arg) {
        if (Bukkit.getMotd().length()==3) return false;
        return CMD.CommandHamdler(cs, comm, s, arg);
    }
     
    public static final Ostrov GetInstance() {
            return Ostrov.instance;
    }  


    
    
    @Override  //вызывается из CraftServer  public ChunkGenerator getGenerator(String world), если указать в bukkit.yml worlds: world: generator: Ostrov
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new EmptyChunkGenerator(this);
    }  
    
     
    public static void log_ok(String s) {   Bukkit.getConsoleSender().sendMessage(prefix +"§2"+ s); }
    public static void log_warn(String s) {   Bukkit.getConsoleSender().sendMessage(prefix +"§6"+ s); }
    public static void log_err(String s) {
        Bukkit.getConsoleSender().sendMessage(prefix +"§c"+ s);
        if (LocalDB.useLocalData) {
            final Connection connection = LocalDB.getConnectionDirect();
            if (connection==null) return;
            try {
                final PreparedStatement pst1 = connection.prepareStatement("INSERT INTO `errors` (`msg`) VALUES (?);");
    //System.out.println("1");
                pst1.setString(1, s);
                pst1.execute();
                pst1.close();

            } catch (SQLException ex) {
                //SW.log_err("не удалось сохранить статистику острова "+owner+" : "+ex.getMessage());  !!Нельзя, зациклит!
                ex.printStackTrace();
            }
        }
    }




    
    

    public static WorldGuardPlatform getWorldGuard() {
        return worldguard_platform;
    }


    public static boolean isCitizen(Entity e) {
       return citizens == null ? false : (e.hasMetadata("NPC") ? true : citizens.getNPCRegistry().isNPC(e));
    }
    
    
    public static boolean isInteger(final String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }


    public static String dateFromStamp(int stamp_in_second) {
        date.setTime(stamp_in_second*1000L);
        return full_sdf.format(date);
    }
    
    public static String getCurrentHourMin() {
        //date.setTime(System.currentTimeMillis());
        //return hour_min_sdf.format(date);
        return calendar.get(Calendar.HOUR)+":"+String.format("%02d", calendar.get(Calendar.MINUTE));
        //return hour_min_sdf.format(calendar.getTime());
    }


    protected static void makeWorldEndToWipe(final int afterSecond) {
        //WorldManager.
        Cfg.GetVariable().set("worldEndMarkToWipe", ApiOstrov.currentTimeSec()+afterSecond);
        Cfg.GetVariable().saveConfig();
        log_warn("Край помечен на вайп через "+ApiOstrov.secondToTime(afterSecond));
    }

    public static Calendar getCalendar() {
        return calendar;
    }














    public static void sync(final Runnable runnable, final int delayTicks) { //sync ( ()->{} ,1 );
        if (runnable==null) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskLater(instance, delayTicks);
    }
    
    public static void async(final Runnable runnable, final int delayTicks) { //sync ( ()->{} ,1 );
        if (runnable==null) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskLaterAsynchronously(instance, delayTicks);
    }
    


    public static void soundDeny(final Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1);
    }


}

