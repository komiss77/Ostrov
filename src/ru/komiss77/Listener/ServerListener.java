 
package ru.komiss77.Listener;

import java.util.UnknownFormatConversionException;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.WorldLoadEvent;
import me.clip.deluxechat.DeluxeChat;
import net.citizensnpcs.Citizens;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.spigotmc.SpigotConfig;
import ru.komiss77.ApiOstrov;
import ru.ostrov77.factions.ApiFactions;
import ru.komiss77.Cfg;
import ru.komiss77.Commands.CMD;
import ru.komiss77.Commands.Pvp;
import ru.komiss77.Enums.Data;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.WorldManager;
import ru.komiss77.Objects.Oplayer;
import ru.komiss77.Ostrov;





public class ServerListener implements Listener {
    
    
    public static Location spawn;
    public static boolean block_nether_portal;
    public static boolean disable_weather;
    public static boolean disable_blockspread;
    public static boolean disable_ice_melt;


    
public static void Init () {
    
    block_nether_portal = Cfg.GetCongig().getBoolean("world.block_nether_portal");
    disable_weather = Cfg.GetCongig().getBoolean("world.disable_weather");
    disable_blockspread = Cfg.GetCongig().getBoolean("world.disable_blockspread");
    disable_ice_melt = Cfg.GetCongig().getBoolean("world.disable_ice_melt");

}    
   

public static void ReloadVars () {
    Init ();
}



    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDragonDeath (final EntityDeathEvent e) {
        if (e.getEntityType()==EntityType.ENDER_DRAGON && e.getEntity().getWorld().getEnvironment()==World.Environment.THE_END) {
            ApiOstrov.makeWorldEndToWipe(3*24*60*60);
            Bukkit.broadcastMessage("§bДракон побеждён, и край будет воссоздан через 3 дня!");
        }
    }
    

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onEntityDamageEvent ( EntityDamageEvent  e ) {
        if (e.getEntity().getType()==EntityType.PLAYER) return;
        if ( e.getCause()==EntityDamageEvent.DamageCause.VOID) {
            e.getEntity().remove();
            Ostrov.log_warn("Удалена бесконечно падающая в бездну сущность "+ e.getEntity());
        }
 
    }
/*
    @EventHandler
    public void onAsyncPlayerPreLoginEvent(AsyncPlayerPreLoginEvent e) {
System.out.println("------------> AsyncPlayerPreLoginEvent "); 
    }

    @EventHandler
    public void onPlayerPreLoginEvent(PlayerPreLoginEvent e) {
System.out.println("------------> PlayerPreLoginEvent "); 
    }

    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent e) {
System.out.println("------------> PlayerLoginEvent "); 
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpawnLocationEvent(PlayerSpawnLocationEvent e) {
System.out.println("------------> PlayerSpawnLocationEvent "); 
            for (RegisteredListener listener : PlayerSpawnLocationEvent.getHandlerList().getRegisteredListeners() ) {
System.out.println("PlayerSpawnLocationEvent listener="+listener.getListener().toString());
            }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
System.out.println("------------> PlayerJoinEvent "); 
            for (RegisteredListener listener : PlayerJoinEvent.getHandlerList().getRegisteredListeners() ) {
System.out.println("PlayerJoinEvent listener="+listener.getListener().toString());
            }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGroupChangeEvent(GroupChangeEvent e) {
System.out.println("------------> GroupChangeEvent "); 
    }
*/


































    @EventHandler
    public void onChannelRegister(PlayerRegisterChannelEvent e) {
        if (e.getPlayer().getListeningPluginChannels().size() > 120) {
            e.getPlayer().kickPlayer("Лимит регистрации каналов (max= 120)");
        }
    }
  
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent e) {

        //if (e.getPlugin().getDescription().getCommands()!=null) {
            e.getPlugin().getDescription().getCommands().keySet().stream().forEach((command) -> {
                CMD.all_server_commands.add(command);
//System.out.println("------------> Command add "+command); 
            });
        //}
            
        switch ( e.getPlugin().getName() ) {

                
             case "Citizens":
                Ostrov.citizens=(Citizens) Bukkit.getPluginManager().getPlugin("Citizens");
                Ostrov.log_ok ("§2Используем Citizens!");
                break;
             //case "EffectLib":
            //    Ostrov.effect_manager= new EffectManager(Ostrov.instance);
                //Ostrov.pandora=new Pandora();
             //   Ostrov.log_ok ("§2Используем EffectLib!");
             //   break;
            
                 
            //case "ProtocolLib":
              //  Ostrov.protocollib=true;
                //ProtocolListener.Register_listener();
             //   Ostrov.log_ok ("§2ProtocolListener активен !");
                //if ( nbt_checker && !nbt ) {
                //    nbt=true;
               //     Bukkit.getPluginManager().registerEvents(new NbtListener(), Ostrov.instance);
               //     Ostrov.GetInstance().getCommand("nbtfind").setExecutor(new Nbtfind(Ostrov.instance));
                //    Ostrov.GetInstance().getCommand("nbtcheck").setExecutor(new Nbtcheck(Ostrov.instance));
                //    Ostrov.log_ok ("§2nbt_checker активен !");
                //}
              //  if ( Cfg.GetPacks().getBoolean("use") ) {
                   // Bukkit.getPluginManager().registerEvents(new ResourcePacks(), Ostrov.instance);
                   // Ostrov.resourcePacks = new ResourcePacks();
                   // Ostrov.log_ok ("§2resoucepacks активен !");
             //   }
             //   break;

            //case "Skills":
            //    Ostrov.sedna= true;
            //    Ostrov.log_ok ("§2Найдены скилы! Режим Седны!");
            //    break;
            
            // case "Parkour":
            //    Ostrov.parkur= true;
           //     Ostrov.log_ok ("§2Найдены паркуры! Режим паркуры!");
           //     break;
            
             //case "uSkyBlock":
            //    Ostrov.uskyblock= true;
             //   Ostrov.log_ok ("§2Найден uSkyBlock!");
            //    break;
            
            case "WorldGuard":
                Ostrov.WorldGuard_get();
                Ostrov.log_ok ("§2Найден WorldGuard!");
                break;
                
            case "DeluxeChat":
                Ostrov.deluxechatPlugin = (DeluxeChat) Bukkit.getPluginManager().getPlugin("DeluxeChat");
                Ostrov.log_ok ("§2Найден DeluxeChat!");
                break;
                
            case "Factions":
                Ostrov.apiFactions = new ApiFactions();//(Factions) Bukkit.getPluginManager().getPlugin("Factions");
                Ostrov.log_ok ("§2Найден DeluxeChat!");
                break;
                
            case "PowerNBT":
                Ostrov.powerNBT= true;
                Ostrov.log_ok ("§2Найден PowerNBT!");
                break;
                
            case "LangUtils":
                Ostrov.langUtils= true;
                //Ostrov.langHelper = new LanguageHelper();
                Ostrov.log_ok ("§2Найден LangUtils!");
                break;
                
            //case "AAC":
            //    Ostrov.aac= true;
            //    Ostrov.log_ok ("§2Найден Advanced Anti Cheat!");
            //    Bukkit.getPluginManager().registerEvents(new AAC_listener(), Ostrov.instance);
            //    AAC_listener.Init();
            //    break;
        }
        
    }



    
  /*  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnloaded(final WorldUnloadEvent e) {
        
        if (e.getWorld().getName().equals("world_the_end")) {
            for (final Entity entity : e.getWorld().getEntities()) {
    System.out.println(entity.getType());
                if (entity.getType()==EntityType.ENDER_DRAGON) {
    System.out.println("+++++++++++++++++++++++++++++++++++");
                }
            }
        }
    }*/

    
    
    
    
    
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoaded(final WorldLoadEvent e) {

    final World bukkitWorld = e.getWorld();
    
    WorldManager.tryRestoreFill(bukkitWorld.getName());
    

   // if (bukkitWorld.getName().equals("world_the_end")) {
    //    for (final Entity entity : bukkitWorld.getEntities()) {
     //       if (entity.getType()==EntityType.ENDER_DRAGON) {
//System.out.println("+++++++++++++++++++++++++++++++++++ SpigotConfig.disabledAdvancements="+SpigotConfig.disabledAdvancements);
        //    }
       // }
   // }
    
    //final String level_name=VM.getNmsServer().getlevelName(bukkitWorld); //только для мира 0
       // if (bukkitWorld.getName().equals(level_name) ) {

          //  if (Cfg.GetCongig().getBoolean("world.spawn.set_spawn_point_world0")) {
          //      spawn = new Location(bukkitWorld, Cfg.GetCongig().getInt("world.spawn.set_spawn_point.x")+0.5, Cfg.GetCongig().getInt("world.spawn.set_spawn_point.y")+0.5, Cfg.GetCongig().getInt("world.spawn.set_spawn_point.z")+0.5, Cfg.GetCongig().getInt("world.spawn.set_spawn_point.p"), 0);
          //      bukkitWorld.setSpawnLocation(spawn);
          //  }
            
        PM.postWorldLoadInit();
      //  }
        if (Bukkit.getServer().getMotd().length()==4 || Bukkit.getServer().getMotd().startsWith("lobby")) {
        
            bukkitWorld.setKeepSpawnInMemory(true);

            if (!SpigotConfig.disabledAdvancements.contains("*")) SpigotConfig.disabledAdvancements.add("*");
            bukkitWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);                                                                    
            bukkitWorld.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, true);                                                                    
            bukkitWorld.setGameRule(GameRule.DISABLE_RAIDS, true);                                                                    
            bukkitWorld.setGameRule(GameRule.KEEP_INVENTORY, false);
            bukkitWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            bukkitWorld.setGameRule(GameRule.DO_ENTITY_DROPS, false);
            bukkitWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true); //- сразу респавн, возможен косяк с spigot.respawn?
            bukkitWorld.setGameRule(GameRule.DO_INSOMNIA, false);
            bukkitWorld.setGameRule(GameRule.DO_MOB_LOOT, false);
            bukkitWorld.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
            bukkitWorld.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
            bukkitWorld.setGameRule(GameRule.FORGIVE_DEAD_PLAYERS, true);
            bukkitWorld.setGameRule(GameRule.MOB_GRIEFING, false);
            bukkitWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            bukkitWorld.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);                                                                    
            bukkitWorld.setGameRule(GameRule.SPAWN_RADIUS, 0);                                                                    
            bukkitWorld.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);                                                                    

            Ostrov.log_ok("Настройки мира "+ bukkitWorld.getName() +" инициализированы для лобби или миниигры");
        }
//System.out.println("SpigotConfig.disabledAdvancements="+SpigotConfig.disabledAdvancements);
        
        
        
        //if (Ostrov.pandora!=null) Ostrov.pandora.Load();
    }

    

    
@EventHandler ( ignoreCancelled = true, priority = EventPriority.HIGHEST )
    public void Command(PlayerCommandPreprocessEvent e) throws CommandException {
        
        boolean cancel = checkCommand(e.getPlayer(), e.getMessage());
//System.out.println("------------> Command "+e.getMessage()+ " cancel?"+cancel);
        e.setCancelled(cancel);

       

        
       

    }
 
    
    
    public static boolean checkCommand (final Player p, final String command) {
        String c = command.replaceFirst("/", "");
       
       if (c.contains(" ")) c=c.split(" ")[0];
       
      
      /* if (c.equals("me")||c.equals("bukkit")||c.equals("op")) {
            ChatMsgUtil.Help(p,0);
            return true;
       }
       
       if (c.contains(":")) c=c.split(":")[0];
       if (c.equals("minecraft")||c.equals("bukkit")||c.equals("spigot")) {
            ChatMsgUtil.Help(p,0);
            return true;
       }
       
       if (c.equals("d") && command.toLowerCase().split(" ").length>=2 && command.toLowerCase().split(" ")[1].startsWith("player") ){
            p.sendMessage("§cМаскировка под игрока невозможна!");
            return true;
       }
       
       if (c.equals("m")||c.equals("r")||c.equals("msg")) return false;

       
       if ( p.isOp() || p.hasPermission("ostrov.ignorecmdblock")) return false;*/
       
        if ( Pvp.pvp_battle_time > 1 && PM.inBattle(p.getName()) ) {
            p.sendMessage( "§cРежим боя - команды заблокированы! Осталось " + PM.getOplayer(p.getName()).pvp_time + " сек." );
            return true;
        }
        
       
//System.out.println("------------> Command 222 "+c); 
//System.out.println("------------> Command 333 all_server_commands="+CMD.all_server_commands); 
       
     //  if ( !CMD.all_server_commands.contains(c) && !block_commands_except.contains(c) ) { //если совсем неизвестная команда
      //      p.sendMessage("");
      //      if (Ostrov.lobby_items.hasItem("pipboy")) ApiOstrov.sendTitle(p, "§fКоманда §b§l/menu","§f- открыть главное меню");
       //     ChatMsgUtil.Help(p,0);
       //     p.sendMessage("");
      //      return true;
      // }
       
       //if (c.equals("me")||c.equals("bukkit")) deny = true;
//System.out.println("------------> Command2 "+c); 
      //  if ( block_commands && !block_commands_except.contains(c) ) {
      //      p.sendMessage("");
      //      p.sendMessage(deny_msg);
            //V_110_util.Help(e.getPlayer(),0);
      //      p.sendMessage("");
      //      return true;
     //   }
        
        return false;
    }  
    
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!PM.use_preffix_suffix_wothout_deluxechat) return;
        final Oplayer op = PM.getOplayer(e.getPlayer());
        if (op==null) return;
            String format = 
                op.getDataString(Data.PREFIX)+ 
                    PM.prefix_p_n + 
                        e.getPlayer().getName() + 
                            PM.prefix_n_s + 
                                op.getDataString(Data.SUFFIX) + 
                                    PM.prefix_s_m + 
                                        e.getMessage().replaceAll("%", "");

            format = replaceColours(format);
            //try {
                e.setFormat(format);
            //} catch (UnknownFormatConversionException ex) {
            //    Ostrov.log_err("AsyncPlayerChatEvent name="+e.getPlayer().getName()+" pref="+PM.OP_GetPrefix(e.getPlayer().getName())+" suff="+PM.OP_GetSuffix(e.getPlayer().getName())+" msg="+e.getMessage());
            //}

    }
protected String replaceColours(String message) { return message.replaceAll("(?i)&([a-f0-9])", "\u00A7$1");}



    


 
        
        
        

        
        
        
        
        
        
        
        
        
        
        
    
    
@EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void onBlockFade(BlockFadeEvent e) {
        if (disable_ice_melt) {
            if( e.getBlock().getType() == Material.ICE || e.getBlock().getType() == Material.PACKED_ICE || e.getBlock().getType() == Material.SNOW || e.getBlock().getType() == Material.SNOW_BLOCK) 
                e.setCancelled(true);
        }
    }

    
    
    
        
        
 // --------------------------- WORLD --------------------------       
            
@EventHandler(ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) { if( disable_weather && event.toWeatherState() )  event.setCancelled(true);}
 
@EventHandler(ignoreCancelled = true)
    public void onThunderChange(ThunderChangeEvent event) { if( disable_weather && event.toThunderState() ) event.setCancelled(true); }     
          
@EventHandler(ignoreCancelled = true)
   public void onNetherCreate(PortalCreateEvent event) { if ( block_nether_portal ) event.setCancelled(true); }
   
@EventHandler(ignoreCancelled = true,priority = EventPriority.LOWEST)
    public void onBlockSpread(BlockSpreadEvent e) { 
        if ( disable_blockspread ) e.setCancelled(true);
        else if (e.getSource().getType()==Material.VINE) e.setCancelled(true);
    }  
        
@EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void onBlockGrowth(BlockGrowEvent e) { 
        if ( disable_blockspread ) e.setCancelled(true);
    }

  
// ----------------------------------------------------------

   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
    

    
    
    
    
}
