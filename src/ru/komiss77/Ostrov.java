package ru.komiss77;



import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import me.clip.deluxechat.DeluxeChat;
import net.citizensnpcs.Citizens;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ru.komiss77.Commands.RegisterCommands;
import ru.komiss77.Commands.CMD;
import ru.komiss77.Commands.Nbtcheck;
import ru.komiss77.Commands.Nbtfind;
import ru.komiss77.Commands.Tpr;
import ru.komiss77.Kits.KitManager;
import ru.komiss77.Listener.ArmorEquipListener;
import ru.komiss77.Listener.InvSeeListener;
import ru.komiss77.Listener.LimiterListener;
import ru.komiss77.Listener.MenuListener;
import ru.komiss77.Listener.NbtListener;
import ru.komiss77.Listener.OstrovChanelListener;
import ru.komiss77.Listener.PlayerListener;
import ru.komiss77.Listener.ResourcePacks;
import ru.komiss77.Listener.ServerListener;
import ru.komiss77.Listener.SpigotChanellMsg;
import ru.komiss77.Listener.TPAListener;
import ru.komiss77.Listener.V110_Listener;
import ru.komiss77.Managers.MysqlLocal;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.SM;
import ru.komiss77.ProfileMenu.PassportHandler;
import ru.komiss77.modules.DchatExpansion;
import ru.komiss77.Managers.EmptyChunkGenerator;
import ru.komiss77.Managers.Timer;
import ru.komiss77.Managers.WE;
import ru.komiss77.Managers.Warps;
import ru.komiss77.Objects.CaseInsensitiveMap;
import ru.komiss77.modules.Informator;
import ru.komiss77.modules.LobbyItems;
import ru.komiss77.modules.OstrovDB;
import ru.komiss77.modules.Pandora;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.inventory.InventoryAPI;
import ru.komiss77.version.VM;
import ru.ostrov77.friends.ApiFriends;


public class Ostrov extends JavaPlugin {
    
    public static Ostrov instance;
    public static Citizens citizens=null;      //античит
    public static DeluxeChat deluxechatPlugin=null;
    public static WorldGuardPlatform worldguard_platform=null;
    public static Random random;
    //public static EffectManager effect_manager=null;
    public static ApiFriends api_friends=null;
    
    public static CaseInsensitiveMap <Initiable> modules;
    
    public static VM VM;
    public static WE WE;
    public static Pandora pandora;
    public static SM servers;
    public static LobbyItems lobby_items;
    public static KitManager kitManager;
    public static InventoryAPI inventoryAPI;
    public static PlayerInput playerChatInput;
    
    public static String prefix = "§2[§aОстров§2] §f";;
    public static int server_id=-1;
    public static boolean use_vault,powerNBT,langUtils,aac,новый_день,uskyblock,sedna,parkur;
    public static boolean first_start=true;
    private static Date date;
    private static SimpleDateFormat full_sdf;
    private static SimpleDateFormat hour_min_sdf;

    
    
    
    @Override
    public void onLoad() {
        instance = this;
        modules = new CaseInsensitiveMap<>();
        date = new java.util.Date(System.currentTimeMillis());
        full_sdf = new java.text.SimpleDateFormat("HH:mm dd.MM.yy");
        hour_min_sdf = new java.text.SimpleDateFormat("HH:mm");
        VM = new VM(this);
    }
    
  
    
    @Override
    public void onEnable() {
        
        try {
            GetInstance().getServer().getMessenger().registerOutgoingPluginChannel(Ostrov.GetInstance(), Cfg.chanelName );
            GetInstance().getServer().getMessenger().registerIncomingPluginChannel(Ostrov.GetInstance(), Cfg.chanelName, new SpigotChanellMsg() );
            log_ok ("§5Регистрация канала BungeeCord");
        } catch (Exception ex) {
            log_err("§5Регистрация канала BungeeCord: "+ex.getMessage());
        }
        
        random=new Random();
        
        if (Bukkit.getMotd().length()==3) {
            log_err("§bРежим Auth");
            return;
        }


        Cfg.Init(); // 1 !
        //Cfg.InitModules(); //2 !


        новый_день=Cfg.GetVariable().getInt("last_day")!=Cfg.Get_day();
            if (новый_день) {
                Cfg.GetVariable().set("last_day", Cfg.Get_day());
                Cfg.GetVariable().saveConfig();
            }

        RegisterCommands.register(this);
        

        log_ok ("§5Регистрация слушателей:");
        Bukkit.getPluginManager().registerEvents(new WE(this), this);
        Bukkit.getPluginManager().registerEvents(new OstrovChanelListener(), this);
        Bukkit.getPluginManager().registerEvents(new TPAListener(), this);
        Bukkit.getPluginManager().registerEvents(new ServerListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new MenuListener(), this);
        Bukkit.getPluginManager().registerEvents(new InvSeeListener(), this);
        Bukkit.getPluginManager().registerEvents(new ArmorEquipListener(), this);
        Bukkit.getPluginManager().registerEvents(new V110_Listener(), this);
        Bukkit.getPluginManager().registerEvents(new PassportHandler(this), this);

        if (Cfg.GetCongig().getBoolean("modules.nbt_checker")) {
            Bukkit.getPluginManager().registerEvents(new NbtListener(instance), instance);
            log_ok ("§eКонтроль NBT тэгов включен.");
        }
        
        instance.getCommand("nbtfind").setExecutor(new Nbtfind(instance));
        instance.getCommand("nbtcheck").setExecutor(new Nbtcheck(instance));

        
        if (!first_start) {
            Bukkit.getOnlinePlayers().stream().forEach((p) -> {
                PM.getOplayer(p);
            });
        }
        first_start = false;

        
        
        log_ok ("§5Инициализация модулей:");
        
        ItemUtils.LoadItem();
        CMD.Init();
        OstrovDB.init();
        ServerListener.Init();
        PlayerListener.Init();
        MenuListener.Init();
        TPAListener.Init();
        Tpr.Init();
        PM.Init();
        MysqlLocal.Init();
        Warps.Init();
        Timer.Init();
        

        modules.put("resourcePacks", new ResourcePacks());
        modules.put("limiterListener", new LimiterListener(this));//new LimiterListener(this);
        servers = new SM(this); modules.put("serverManager", servers);//servers.on_start();
        lobby_items=new LobbyItems(this); modules.put("lobbyItems", lobby_items);
        kitManager = new KitManager(this); modules.put("kits", kitManager);
        pandora=new Pandora(this); modules.put("pandora", pandora);
        modules.put("informator", new Informator());
        
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
              new DchatExpansion(this).register();
        }
        
        playerChatInput = new PlayerInput(); //регион ГУИ, скайблок
        inventoryAPI = new InventoryAPI(this, false);
        

        
        log_ok ("§2Остров готов к работе!");
        

    }
 

    
    
    
    
    @Override
    public void onDisable() {
        if (Bukkit.getMotd().length()==3) return;
        PM.onDisable();
        OstrovDB.Disconnect();
        MysqlLocal.Disconnect();
        servers.on_shut_down();

        
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
    public static void log_err(String s) {   Bukkit.getConsoleSender().sendMessage(prefix +"§c"+ s); }




    
    

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


    public static String dateFromStamp(long stamp_in_second) {
        date.setTime(stamp_in_second*1000L);
        return full_sdf.format(date);
    }
    
    public static String getCurrentHourMin() {
        date.setTime(System.currentTimeMillis());
        return hour_min_sdf.format(date);
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


/*


    @Deprecated
    public static boolean HasGroup (final String nik, final String group_name) {
        return PM.getOplayer(nik).hasGroup(group_name);
    }
    @Deprecated
    public static String[] getGroups(final String nik) {
        return (String[]) PM.getOplayer(nik).getGroups().toArray(new String[0]);
    }
    @Deprecated
    public static boolean inGroup(final String nik, final String group_name) {
        return PM.getOplayer(nik).hasGroup(group_name);
    }
    @Deprecated
    public static boolean hasPermission(final String worldName, final String nik, String perm) {
        Player p = Bukkit.getPlayer(nik);
        if (p==null) return false;
        if (p.hasPermission(perm)) return true;                               //если есть в явном виде - ДА
        boolean has  = false;
        int dotPos;
        for (int q=1; q<perm.length(); q++ ) {
            dotPos  = perm.lastIndexOf(".");                               //проверяем, заменяя последнее слово на *
            if (dotPos > -1) {
    //System.out.println("2222 "+perm+"("+p.hasPermission(perm)+") -> "+perm.substring(0, dotPos)+".* ("+(p.hasPermission(perm.substring(0, dotPos)+".*")+")") );    
                perm = perm.substring(0, dotPos);
                    if ( p.hasPermission(perm+".*") ) {
                        has = true;
                        break;
                    }
            } else break;
        }    
        return has;
    }
    @Deprecated
    public static String GetGroups(final Player player){ 
        return PM.getOplayer(player.getName()).chat_group;
    }
    @Deprecated
    public static String GetPlayTime(final Player player){ 
        return Utils.IntToTime( PM.getOplayer(player.getName()).getBungeeIntData(Data.PLAY_TIME) );
    }
    @Deprecated
    public static String GetPrefix(final Player player){ 
        return PM.getOplayer(player.getName()).getBungeeData(Data.PREFIX);
    }
    @Deprecated
    public static String GetSuffix(final Player player){ 
         return PM.getOplayer(player.getName()).getBungeeData(Data.SUFFIX);
    }
    @Deprecated
    public static boolean inBattle (String name)  {
        return PM.inBattle(name);
    }      
    @Deprecated
    public static void moneyChange ( final Player p, final int value, final String who ) {
        PluginMsg.sendMessage(p, Chanell.OSTROV, Action.OSTROV_BUNGEE_MONEY_CHANGE, String.valueOf(value)+"<>"+who);
    } 
    @Deprecated
    public static void moneyChange ( final String name, final int value, final String who ) {
        if (PM.exist(name)) {
            moneyChange(Bukkit.getPlayer(name), value, who);
        } else {
            //запомнить и дать при входе
        }
    } 
    @Deprecated
    public static int moneyGetBalance ( final Player p ) {
        return moneyGetBalance(p.getName());
    }  
    @Deprecated
    public static int moneyGetBalance ( final String name ) {
        if (PM.exist(name)) return PM.getOplayer(name).getBungeeIntData(Data.MONEY);
        else return 0;
    }  
    @Deprecated
    public static String GetBalString ( Player p ) {
        if (PM.exist(p.getName()) ) {
            final int m = PM.getOplayer(p.getName()).getBungeeIntData(Data.MONEY); 
            if ( m<=1000 ) return "Нищеброд";
            else if (m>1000 && m<=10000)return "Бедняк";
            else if (m>10000 && m<=100000)return "Малоимущий";
            else if (m>100000 && m<=1000000)return "В достатке";
            else if (m>1000000 && m<=10000000)return "Хозяин жизни";
            else if (m>10000000 && m<=100000000)return "Богач";
            else return "Олигарх";
        } else return "§cигрок оффлайн";
    }  
    @Deprecated
    public static int GetBalance ( Player p ) {return moneyGetBalance(p);}  
    @Deprecated
    public static int GetBalance ( String nik ) {return moneyGetBalance(nik);}  
    @Deprecated
    public static void SetBalance ( Player p, int m ) {} 
    @Deprecated
    public static void Money_change ( final Player p, final int m  ) { moneyChange(p, m, "");} 
    @Deprecated
    public static void Money_change ( final String nik, final int m  ) { moneyChange(nik, m, "");}  
@Deprecated    
    public static void sendMessage11(final Player p, String ch, String msg ) {
//System.out.println(">>>>SENDMESSAGE: "+ch+" "+msg);    
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            log_err("sendMessage :"+e.getMessage());
            //ioexception.printStackTrace();
        }

        p.sendPluginMessage(instance, ch, stream.toByteArray());
    }
    @Deprecated
    public static boolean Teleport_check_location(Location loc) {
        return ! TeleportLoc.isBlockUnsafe (loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    @Deprecated
    public static Location Teleport_get_save_location ( Location loc) {
        return TeleportLoc.getSafeDestination(loc);
    }
    @Deprecated
    public static boolean Teleport_save_to_location ( Player p , Location loc) {
        if (Teleport_check_location(loc))  {
            p.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
            return true;
        }
        else {
            Location save = Teleport_get_save_location(loc);
            if (save != null){
                p.teleport(save, PlayerTeleportEvent.TeleportCause.COMMAND);
            return true;
            }
            else {
                p.sendMessage("§4Телепорт не удался - указанная локация опасна для жизни!");
                return false;
            }
        }
    }
    @Deprecated
    public static void Give_pipboy(Player p) {
        //ItemUtils.Add_to_inv(p, 8, ItemUtils.pipboy, true, false);
        lobby_items.giveItem(p, "pipboy");
    }
    @Deprecated
    public static boolean Текстуры_утановлены(Player p) {
        return ResourcePacks.Текстуры_утановлены(p);
    }
    @Deprecated
    public static Connection Get_mysql_connection() {
        return Datas.GetConnection();
    }
    @Deprecated
    public static Connection Get_pex_connection() {
        return Perm.GetConnection();
    }
    @Deprecated
    public static boolean Настал_новый_день() {
        return новый_день;
    }
     @Deprecated
   public static void sendTitle(final Player p, final String title, final String subtitle) {
        sendTitle(p, title, subtitle, 20, 40, 20);
    }
    @Deprecated
    public static void sendTitle(final Player p, final String title, final String subtitle, final int fadein, final int stay, final int fadeout ) {
        Utils.sendTitle(p.getName(), title, subtitle, fadein, stay, fadeout);
    }
    @Deprecated
    public static void sendActionBar(final Player p, final String text) {
        Utils.sendActionBar(p, text);
    }
    @Deprecated
    public static void sendActionBar(final String nik, final String text) {
        Utils.sendActionBar(nik, text);
    }
    @Deprecated
    public static void sendActionBarDirect(final Player p, final String text) {
        NmsUtils.sendActionBar(p, text);
    }
     @Deprecated
   public static void sendBossbar (Player p, String text, int seconds, BarColor bar_color, BarStyle bar_style, boolean show_progress) {
        Utils.sendBossBar(p, text, seconds, bar_color, bar_style, show_progress);
    } 
    @Deprecated
    public static int randInt(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }
    @Deprecated
    public static void SendToServer(final Player player, final String server, final String arena) {
        PluginMsg.SendToServer(player, server, arena);
    }  

    
    
*/

    
    

}

