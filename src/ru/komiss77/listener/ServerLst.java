package ru.komiss77.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.event.world.*;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.Module;
import ru.komiss77.enums.ServerType;
import ru.komiss77.events.RestartWarningEvent;
import ru.komiss77.hook.*;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.world.Land;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.version.Nms;


public class ServerLst implements Listener {


    @EventHandler(priority = EventPriority.LOWEST)
    public void onDisable(PluginDisableEvent e) { //надо отловить SHUT_DOWN, т.к. зависимые плагины отгружаются первыми!!
//Ostrov.log_warn("PluginDisableEvent isServerStopped?"+Nms.isServerStopped()+" SHUT_DOWN="+Ostrov.SHUT_DOWN);
        if (!Ostrov.SHUT_DOWN && Nms.isServerStopped()) {
            Ostrov.SHUT_DOWN = true;
        }
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onRestartWarning(final RestartWarningEvent e) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "clean");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawnEvent(CreatureSpawnEvent e) {
        if (GM.GAME == Game.LOBBY && e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            e.setCancelled(true);
        }
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDragonDeath(final EntityDeathEvent e) {
        if (GM.GAME == Game.AR || GM.GAME == Game.DA || GM.GAME == Game.MI) {
            if (e.getEntityType() == EntityType.ENDER_DRAGON && e.getEntity().getWorld().getEnvironment() == World.Environment.THE_END) {
                ApiOstrov.makeWorldEndToWipe(3 * 24 * 60 * 60);
                Bukkit.broadcast(TCUtil.form("§bДракон побеждён, и край будет воссоздан через 3 дня!"));
            }
        }
    }


    @EventHandler
    public void onChannelRegister(PlayerRegisterChannelEvent e) {
        if (e.getPlayer().getListeningPluginChannels().size() > 120) {
            e.getPlayer().kick(TCUtil.form("Лимит регистрации каналов (max= 120)"));
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPluginEnable(PluginEnableEvent e) {
        //if (e.getPlugin().getDescription().getCommands()!=null) {
        // e.getPlugin().getDescription().getCommands().keySet().stream().forEach((command) -> {
        //     CMD.all_server_commands.add(command);
        //  });
        //}
//Ostrov.log_warn("=== PluginEnableEvent "+e.getPlugin().getName());
        switch (e.getPlugin().getName()) {

            case "Matrix" -> {
                Bukkit.getPluginManager().registerEvents(new MatrixLst(), Ostrov.instance);
                Ostrov.log_ok("§5Используем Matrix!");
            }

            case "WorldGuard" -> {
                WGhook.hook(e.getPlugin());
            }

            case "CrazyAdvancementsAPI" -> Ostrov.getModule(Module.quests).reload();

            case "ProCosmetics" -> {
                ProCosmeticsHook.hook(e.getPlugin());
            }

            case "dynmap" -> DynmapHook.hook(e.getPlugin());

            case "EasyPayments" -> EasyPaymentsHook.hook(e.getPlugin());

            case "TradeSystem" -> TradeSystemHook.hook(e.getPlugin());
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent e) {

        switch (e.getPlugin().getName()) {

            case "CrazyAdvancementsAPI" -> Ostrov.getModule(Module.quests).reload();

        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onServerLoad(final ServerLoadEvent e) { //прилетает 1: после загрузки всех миров server.enablePlugins(PluginLoadOrder.POSTWORLD); либо после перезагрузки командой
        if (Ostrov.STARTUP && e.getType() == ServerLoadEvent.LoadType.STARTUP) {
            Ostrov.sync(() -> Ostrov.postWorld(), 60);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onWorldLoaded(final WorldLoadEvent e) {
        final World w = e.getWorld();
        WorldManager.tryRestoreFill(w.getName());

        if (GM.GAME.type == ServerType.LOBBY) {
            Nms.pathWorld(w);
        }

        w.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);

        if (Ostrov.MOT_D.length() <= 4) {
            w.setGameRule(GameRule.SPAWN_CHUNK_RADIUS, 8);
            w.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, true);
            w.setGameRule(GameRule.DISABLE_RAIDS, true);
            w.setGameRule(GameRule.KEEP_INVENTORY, false);
            w.setGameRule(GameRule.DO_ENTITY_DROPS, false);
            w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true); //- сразу респавн, возможен косяк с spigot.respawn?
            w.setGameRule(GameRule.DO_INSOMNIA, false);
            w.setGameRule(GameRule.DO_MOB_LOOT, false);
            w.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
            w.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
            w.setGameRule(GameRule.FORGIVE_DEAD_PLAYERS, true);
            w.setGameRule(GameRule.MOB_GRIEFING, false);
            w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            w.setGameRule(GameRule.SPAWN_RADIUS, 0);
            w.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
            Ostrov.log_ok("Настройки мира " + w.getName() + " инициализированы для лобби или миниигры");
        }

        Land.load(w);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(final WorldUnloadEvent e) {
        Land.unload(e.getWorld());
    }

    @EventHandler
    public void onChunkUnloadEvent(ChunkUnloadEvent e) {
        Land.unload(e.getChunk());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntitySpawn(final EntitySpawnEvent e) {
        if (Cfg.clear_old_ents) {
            setClearable(e.getEntity(), true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntitiesLoad(final EntitiesLoadEvent e) {
        if (Cfg.clear_old_ents) {
            for (final Entity ent : e.getEntities()) {
                if (isClearable(ent)) ent.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntitiesUnload(final EntitiesUnloadEvent e) {
        if (Cfg.clear_old_ents) {
            for (final Entity ent : e.getEntities()) {
                if (isClearable(ent)) ent.remove();
            }
        }
    }

    public boolean isClearable(final Entity ent) {
        return switch (ent) {
            case null -> true;
            case final Item ignored -> false;
            case final Projectile ignored -> true;
            case final LivingEntity le -> le.getRemoveWhenFarAway();
            default -> ent.isPersistent();
        };
    }

    public void setClearable(final Entity ent, final boolean clear) {
        switch (ent) {
            case null: break;
            case final Item ignored: break;
            case final Projectile ignored: break;
            case final LivingEntity le: le.setRemoveWhenFarAway(clear); break;
            default: ent.setPersistent(clear); break;
        };
    }

        
        
        
     /*
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockFade(BlockFadeEvent e) {
        if (Config.disable_ice_melt) {
            switch (e.getBlock().getType()) {
                case ICE, PACKED_ICE, SNOW, SNOW_BLOCK:
                    e.setCancelled(true);
                default:
                    break;
            }
        }
    }


        
        
 // --------------------------- WORLD --------------------------       
       
    @EventHandler(ignoreCancelled = true)
    public void onNetherCreate(PortalCreateEvent event) {
        if ( Config.block_nether_portal ) event.setCancelled(true);
    }
   
    @EventHandler(ignoreCancelled = true,priority = EventPriority.LOWEST)
    public void onBlockSpread(BlockSpreadEvent e) { 
        if ( Config.disable_blockspread ) e.setCancelled(true);
        else if (e.getSource().getType()==Material.VINE) e.setCancelled(true);
    }  
        
    @EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void onBlockGrowth(BlockGrowEvent e) { 
        if ( Config.disable_blockspread ) e.setCancelled(true);
    }
*/

// ----------------------------------------------------------


}
