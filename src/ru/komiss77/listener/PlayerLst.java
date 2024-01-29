package ru.komiss77.listener;

import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import io.papermc.paper.event.player.PlayerUntrackEntityEvent;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandException;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.builder.menu.EntitySetup;
import ru.komiss77.commands.PvpCmd;
import ru.komiss77.events.FriendTeleportEvent;
import ru.komiss77.modules.menuItem.MenuItem;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.TeleportLoc;
import ru.komiss77.utils.inventory.SmartInventory;
import ru.komiss77.version.VM;


public class PlayerLst implements Listener {

    private static final CaseInsensitiveMap <String> bungeeDataCache;
    public static boolean PREPARE_RESTART;
    
    static {
        bungeeDataCache = new CaseInsensitiveMap<>();
    }
    

    @EventHandler
    public void bungeeJoin(AsyncPlayerPreLoginEvent e) {
        if (PREPARE_RESTART) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("§6§lСервер перезагружается, попробуйте через 30 сек." ) );
        }
    }

    
    @EventHandler ( ignoreCancelled = true, priority = EventPriority.LOW )
    public void Command(PlayerCommandPreprocessEvent e) throws CommandException {
        //final String[] args = e.getMessage().replaceFirst("/", "").split(" ");
       // final String cmd = args[0].toLowerCase();
        final Player p = e.getPlayer();
        if (ApiOstrov.canBeBuilder(p) && !ApiOstrov.isLocalBuilder(p)) {
            final String cmd = e.getMessage().replaceFirst("/", "");
            if (cmd.startsWith("builder") || cmd.startsWith("gm")) return;
            final Oplayer op = PM.getOplayer(p);
            op.lastCommand =  cmd;
        }
    }

    
    //вызывается из SpigotChanellMsg
    public static void onBungeeData(final String name, final String raw) { 
        final Player p = Bukkit.getPlayerExact(name);
        if (p==null) { //данные пришли раньше PlayerJoinEvent
            bungeeDataCache.put(name, raw);
        } else { //если уже был PlayerJoinEvent
            PM.bungeeDataHandle(p, PM.getOplayer(p), raw); //просто прогрузить данные
        }
    }


    @EventHandler(priority = EventPriority.LOWEST) 
    public void onPlayerJoin(PlayerJoinEvent e) {
        e.joinMessage(null);
        final Player p = e.getPlayer();
        //LOCALE тут не получить!!! ловить PlayerLocaleChangeEvent
        final Oplayer op = PM.createOplayer(p);
        p.setShieldBlockingDelay(2);
        p.setNoDamageTicks(20);
        
        if (LocalDB.useLocalData) {
            
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0));
            Ostrov.async( () ->  { 
                LocalDB.loadLocalData(p.getName());//локальные данные на загрузку независимо от данных с банжи!
            }, 10 );
            
        } else {
            if ( Config.set_gm && !p.isOp() ) p.setGameMode(Config.gm_on_join);
            if ( Config.walkspeed_on_join>0)  p.setWalkSpeed(Config.walkspeed_on_join);
        }
        
        final String bungeeData = bungeeDataCache.remove(p.getName());
        if (bungeeData!=null) { //данные пришли ранее, берём из кэша
            Ostrov.sync( ()-> PM.bungeeDataHandle(p, op, bungeeData) ,1); //- без задержки не выдавало предметы лобби!
        }
        if (Ostrov.MOT_D.equals("jail")) {
            ApiOstrov.sendTabList(p,  "§4ЧИСТИЛИЩЕ", "");
        } else {
            ApiOstrov.sendTabList(p, "", "");
        }
        
        //for (final Oplayer otherOp : PM.getOplayers()) {
            //otherOp.score.onJoin(op);
            //if (otherOp.score.hideNameTags) {
            //    otherOp.score.getTeam().addEntry(op.nik);
            //}
          //  VM.getNmsNameTag().updateTag( otherOp, p); //закинуть тэги других игроков вошедшему
       // }

    }

    
    @EventHandler(priority = EventPriority.MONITOR) 
    public void PlayerQuit(PlayerQuitEvent e) {
        e.quitMessage(null);
        final Player p = e.getPlayer();
        final Oplayer op = PM.remove(p.getName());
//Ostrov.log("PlayerQuit "+p.getName()+" op="+op);
        if (op!=null) { //сохраняем, если было реально загружено!
            onLeave(p, op, true);
        }
    }
    
    //отдельным методом, вызов при PlayerQuitEvent или при Plugin.Disable
    public static void onLeave(final Player p, final Oplayer op, final boolean async) {
        VM.getNmsServer().removePacketSpy(p);
        
        //в saveLocalData инвентарь не сохранит
        if (PvpCmd.getFlag(PvpCmd.PvpFlag.drop_inv_inbattle) &&  PvpCmd.getFlag(PvpCmd.PvpFlag.antirelog) && op.pvp_time>0) {      //если удрал во время боя
            final List<ItemStack> drop = new ArrayList<>();
            for (ItemStack is : p.getInventory().getContents()) {
                if (is != null && !MenuItemsManager.isSpecItem(is)) {
                    drop.add(is.clone());
                }
            }
            Ostrov.sync( () -> {
                for (ItemStack is : drop) {
                    p.getWorld().dropItemNaturally(p.getLocation(), is).setPickupDelay(40);
                }
            }, 10);
        }
        
    	if (!op.mysqlError && !op.mysqlData.isEmpty() && LocalDB.useLocalData) {
            if (async) {
                Ostrov.async(()->LocalDB.saveLocalData(p, op), 0); //op.mysqlData не должна быть пустой, если загружало!
            } else {
                LocalDB.saveLocalData(p, op);
            }
    	}
        op.customTag.visible(false);
        op.score.remove();
    }
    
 
    

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void toggleSneak(PlayerToggleSneakEvent e) {
        if (!e.getPlayer().isInsideVehicle()) {
            PM.getOplayer(e.getPlayer()).customTag.setTargetEntitySneaking(e.isSneaking());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void startTrack(PlayerTrackEntityEvent e) {
        if (e.getEntity().getType()!=EntityType.PLAYER) return;
        final Oplayer targetOp = PM.getOplayer(e.getEntity().getUniqueId()); //UpperName cn = nameStorage.get(event.getEntity().getUniqueId());
        final Oplayer trackerOp = PM.getOplayer(e.getPlayer().getUniqueId()); //nameStorage.get(event.getEntity().getUniqueId());
//Ostrov.log("startTrack "+e.getPlayer().getName()+" -> "+e.getEntity().getName()+" targetOp="+targetOp);
        if (targetOp != null) {
            Ostrov.sync(() -> {
                targetOp.customTag.showTo(e.getPlayer());
                targetOp.score.startTrack(e.getPlayer().getName());
                if (trackerOp != null) {
                    trackerOp.score.startTrack(e.getEntity().getName());
                }
            }, 1);
        }
    }
//после респавне не меняется
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void stopTrack(PlayerUntrackEntityEvent e) {
        if (e.getEntity().getType()!=EntityType.PLAYER) return;
        final Oplayer targetOp = PM.getOplayer(e.getEntity().getUniqueId()); //nameStorage.get(event.getEntity().getUniqueId());
//Ostrov.log("stopTrack "+e.getPlayer().getName()+" -> "+e.getEntity().getName()+" targetOp="+targetOp);
        if (targetOp != null) {
            targetOp.customTag.hideFor(e.getPlayer());
            targetOp.score.stopTrack(e.getPlayer().getName());
        }
        final Oplayer trackerOp = PM.getOplayer(e.getPlayer().getUniqueId()); //nameStorage.get(event.getEntity().getUniqueId());
        if (trackerOp != null) {
            trackerOp.score.stopTrack(e.getEntity().getName());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void mountEntity(EntityMountEvent e) {
        final Oplayer op = PM.getOplayer(e.getMount().getUniqueId()); //UpperName cn = nameStorage.get(event.getMount().getUniqueId());
        if (op != null) {
            op.customTag.visible(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void dismountEntity(EntityDismountEvent e) {
        if (e.getDismounted().getPassengers().size() == 1) {
            final Oplayer op = PM.getOplayer(e.getDismounted().getUniqueId()); //UpperName cn = nameStorage.get(event.getDismounted().getUniqueId());
            if (op!=null) {
                // Run 2 ticks later, we need to ensure that the game sends the packets to update the passengers.
                Ostrov.sync(() -> op.customTag.visible(true), 2);
            }
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    

    
    @EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
    public void FriendTeleport(FriendTeleportEvent e) {
        if (e.source!=null && e.source.isOnline() && !e.source.isDead() && PM.inBattle(e.source.getName())) {
            e.setCanceled(true, "§cбитва.");
        }
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if ( ItemUtils.compareItem(e.getItemDrop().getItemStack(), InteractLst.passport, true)) {
            e.getItemDrop().remove();
            e.getPlayer().updateInventory();
        }
    }
    

        
        
 // ----------------------------- ACTION ----------------------
            
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)    
    public void onPlace(BlockPlaceEvent e) {
        //PM.getOplayer(e.getPlayer().getName()).last_breack=Timer.Единое_время();
        if ( Config.disable_break_place && !ApiOstrov.isLocalBuilder(e.getPlayer()) ) e.setCancelled(true);
        //else if (!clear_stats) PM.Addbplace(e.getPlayer().getName());
    }
    
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)    
    public void onBreak(BlockBreakEvent e) {
      //  PM.getOplayer(e.getPlayer().getName()).last_breack=Timer.Единое_время();
        if ( Config.disable_break_place && !ApiOstrov.isLocalBuilder(e.getPlayer()) ) e.setCancelled(true);
        //else if (!clear_stats) PM.get(e.getPlayer().getName());
    }
 
        
        
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)    
    public void onHangingBreakByEntityEvent(HangingBreakByEntityEvent e) {
        if ( e.getRemover()!=null && e.getRemover().getType()==EntityType.PLAYER && PM.exist(e.getRemover().getName())) {
                if ( Config.disable_break_place &&  !ApiOstrov.isLocalBuilder(e.getRemover()) ) e.setCancelled(true);
        } 

    }
       
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)    
    public void onHangingBreakEvent(HangingBreakEvent e) {
        if ( e.getEntity() instanceof Player player) {
                if ( Config.disable_break_place &&   !ApiOstrov.isLocalBuilder(player) ) e.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onPlayerItemFrameChangeEvent(final PlayerItemFrameChangeEvent e) {
        if (Config.disable_break_place && !ApiOstrov.isLocalBuilder(e.getPlayer(), true)) {
            e.setCancelled(true);
            //return;
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent e) {

        final Player p = e.getPlayer();
       
        if (ApiOstrov.isLocalBuilder(p, false)) {
            if (p.isSneaking()) {
                Ostrov.sync(() -> {
                    SmartInventory.builder()
                        . provider(new EntitySetup(e.getRightClicked()))
                        . size(6, 9)
                        . title("§2Характеристики сущности").build()
                        .open(p);
                }, 1); //через тик, илил открывает меню торговли
            }
        }

        switch (e.getRightClicked().getType()) {
            
            case ARMOR_STAND -> e.setCancelled(Config.disable_break_place && !ApiOstrov.isLocalBuilder(p, true));
            
            case ITEM_FRAME, GLOW_ITEM_FRAME -> {
                if (Config.disable_break_place && !ApiOstrov.isLocalBuilder(p, true)) {
                    e.setCancelled(true);
                    return;
                }
                final ItemStack it = p.getInventory().getItemInMainHand();
                if (ItemUtils.isBlank(it, false)) {
                    break;
                }
                final ItemFrame ent;
                switch (it.getType()) {
                    case GLOWSTONE_DUST -> {
                        ent = (ItemFrame) e.getRightClicked();
                        if (!ent.isGlowing()) {
                            ent.setGlowing(true);
                            p.getInventory().setItemInMainHand(it.subtract());
                        }
                    }
                    case GUNPOWDER -> {
                        ent = (ItemFrame) e.getRightClicked();
                        if (ent.isVisible()) {
                            ent.setVisible(false);
                            p.getInventory().setItemInMainHand(it.subtract());
                        }
                    }
                    case SUGAR -> {
                        ent = (ItemFrame) e.getRightClicked();
                        if (!ent.isVisible() || ent.isGlowing()) {
                            ent.setVisible(true);
                            ent.setGlowing(false);
                            p.getInventory().setItemInMainHand(it.subtract());
                        }
                    }
                    default -> {}
                }
            }

            default -> {}
        }

    }


   

    @EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void PlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent e){
        if ( Config.disable_break_place && !e.getPlayer().isOp()) e.setCancelled(true);
    }    
        
    
//---------------------------------------------------
       
        
        
        
        
        
        
        
        
        
        
        
        
        
// ----------------------------------- MOVE --------------------------------


        
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerRespawn(PlayerRespawnEvent e) {
            final Oplayer op = PM.getOplayer(e.getPlayer());
            if(op==null) return;
            
            if (Config.home_command && op.homes.containsKey("home")) {
                final Player p = e.getPlayer();
                Location loc = LocationUtil.LocFromString(op.homes.get("home"));
                if (!TeleportLoc.isSafeLocation(loc)) {
                    loc = TeleportLoc.findNearestSafeLocation(loc, null);
                }
                if (loc==null) {
                    p.sendMessage("§7Не получилось респавниться дома - точка дома может быть опасна.");
                } else {
                    e.setRespawnLocation(loc);
                }
            }
        
        if (op.pvp_time>0) {
            Ostrov.sync(()-> {
                PvpCmd.pvpEndFor(op, e.getPlayer()); //восстановить настроки до начала битвы, убрать тэги
            }, 5);
        }

        
    }      
    
    
    @EventHandler ( ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onWorldChange(PlayerTeleportEvent e) {
        
        final Player p = e.getPlayer();
        final Oplayer op = PM.getOplayer(e.getPlayer());
        if (op==null) return;
        
        final String world_from = e.getFrom().getWorld().getName();
        final String world_to = e.getTo().getWorld().getName();
        
        if (!world_from.equals(world_to)) {
    		if (!ApiOstrov.isLocalBuilder(p, false) && world_to.endsWith(WorldManager.buildWorldName)) {
    			e.setCancelled(e.getCause() == TeleportCause.SPECTATE);
				//ApiOstrov.sendToServer(p, "lobby0", "");
    		}
    		
            if (PvpCmd.no_damage_on_tp > 0) {
               op.setNoDamage(PvpCmd.no_damage_on_tp, true);//no_damage=PvpCmd.no_damage_on_tp;
            }
            op.world_positions.put(world_from, LocationUtil.toDirString(p.getLocation()));   //op.PM.OP_Set_world_position(e.getPlayer(), world_from);                      //сохраняем точку выхода
        }
        
    }
        
        
    
 //------------------------------------------------------------------------   
 
        

    
 

    
    
    
    
    
    
    
    
// ------------------------------- ITEM -------------------------------------------    
 
    

    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void EntityBowShoot(EntityShootBowEvent e) {
        if (e.getEntityType()==EntityType.PLAYER) {

            final Player p = (Player) e.getEntity();
            if (!PM.exist(p.getName())) return;
            
            if (MenuItemsManager.hasItem("tpbow")) {
                final MenuItem si = MenuItemsManager.fromItemStack(e.getBow());
                if (si!=null) {
                    if (Timer.has(p, "bow_teleport")) {//if (PM.getOplayer(p.getName()).bow_teleport_cooldown>0) {
                        p.sendMessage("§cПерезарядка лука.. осталось §4"+Timer.getLeft(p, "bow_teleport")+" сек.");
                        e.setCancelled(true);
                        e.getProjectile().remove();
                    } else {
                        Timer.add(p, "bow_teleport", 4);
                        e.getProjectile().setMetadata("bowteleport", new FixedMetadataValue(Ostrov.instance, "ostrov"));
                    }
                }
            }
            
            if (!p.isOp() && p.getGameMode().equals(GameMode.CREATIVE)) {
                ApiOstrov.sendActionBar(p, "§cПВП в креативе заблокирован!");
                e.setCancelled(true);
            }
        }
    }  
   
   /* @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void ProjectileHitEvent(final ProjectileHitEvent e) {
        
        if (MenuItemsManager.hasItem("tpbow") && e.getEntity().getShooter() instanceof Player && e.getEntity().hasMetadata("bowteleport")) {
            Location destination =  (e.getEntity()).getLocation().clone();
            e.getEntity().remove();
            final Player p = (Player)e.getEntity().getShooter();
            destination.setPitch(p.getLocation().getPitch());
            destination.setYaw(p.getLocation().getYaw());
            p.teleport(destination, PlayerTeleportEvent.TeleportCause.COMMAND);
            p.playSound(p.getLocation(),Sound.ENTITY_BAT_HURT, 2, 1);
        }

    }*/
        
// ------------------------------------------------------------------------
    
    
    
    
    
    
    
    
    
    
    
// ---------------------------- Режимы битвы ---------------------------------
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) { 
        if ( e.getEntityType()==EntityType.PLAYER ) {
            if (!PM.exist(e.getEntity().getName())) return; //защита от бота
            switch (e.getCause()) {
                case VOID:
                    if (Config.disable_void) {
                        e.setDamage(0);
                        e.getEntity().teleport(Bukkit.getWorlds().get(0).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                    }
                    return;
                //чары шипы на оружие-ранит нападающего
                //молния
                //дыхание дракона
                //кактусы
                //огонь
                //горение
                //BlockMagma
                //EntityVex
                //утопление
                //голод
                case FALL, THORNS, LIGHTNING, DRAGON_BREATH, 
                CONTACT, FIRE, FIRE_TICK, HOT_FLOOR, CRAMMING, 
                DROWNING, STARVATION, LAVA:
                default:
                    if ( Config.disable_damage ) e.setCancelled(true);
                    //return;
            }
        } else {
            if (e.getCause()==EntityDamageEvent.DamageCause.VOID) {
                e.getEntity().remove();
                Ostrov.log_warn("Удалена бесконечно падающая в бездну сущность "+ e.getEntity());
                return;
            }
            if ( Config.disable_damage ) e.setCancelled(true);
        }
    }
//------------------------------------------------------------------------------ 
    
    
    
    

    
    
    @EventHandler(ignoreCancelled = true)
    public void onPlayerLoseFood(FoodLevelChangeEvent e) { 
        if ( Config.disable_hungry ) {
            e.setCancelled(true);
            (e.getEntity()).setFoodLevel(20);
        }
    }
  
    
    
}
