package ru.komiss77.listener;

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import io.papermc.paper.event.player.PlayerUntrackEntityEvent;
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
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.komiss77.*;
import ru.komiss77.builder.menu.EntitySetup;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.ServerType;
import ru.komiss77.events.FriendTeleportEvent;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.modules.entities.PvPManager;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.menuItem.MenuItem;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.modules.world.LocFinder;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.utils.StringUtil;


public class PlayerLst implements Listener {

    private static final CaseInsensitiveMap<String> bungeeDataCache;
    public static boolean PREPARE_RESTART;

    static {
        bungeeDataCache = new CaseInsensitiveMap<>();
    }


    @EventHandler
    public void bungeeJoin(AsyncPlayerPreLoginEvent e) {
        if (PREPARE_RESTART) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("§6§lСервер перезагружается, попробуйте через 30 сек."));
        }
    }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onLocalDataLoad(LocalDataLoadEvent e) {
    if (ResourcePacksLst.use) {
      ResourcePacksLst.execute(e.getPlayer());
    }
  }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void Command(final PlayerCommandPreprocessEvent e) throws CommandException {
      final String[] args = e.getMessage().replaceFirst("/", "").split(" ");
      final String cmd = args[0].toLowerCase();
//Ostrov.log_warn("PlayerCommandPreprocessEvent cmd="+cmd+" msg="+e.getMessage());
      final Player p = e.getPlayer();

      if (ApiOstrov.isLocalBuilder(p, false)) {
//            if (cmd.startsWith("builder") || cmd.startsWith("gm")) return;
            final Oplayer op = PM.getOplayer(p);
            if (op.setup == null) { //запоминаем только если не активен билдер!
                op.lastCommand = e.getMessage().replaceFirst("/", "");
            }
            return;
        }
        if (Ostrov.wg) {
            if (e.getMessage().startsWith("/rg") || e.getMessage().startsWith("/region")) {
                if (e.getMessage().contains("claim") || e.getMessage().contains("define")) {
                    e.setCancelled(true);
                    PM.getOplayer(p).menu.openRegions(p);
                    //e.setMessage("land");
                }
            }
        }
    }

    //вызывается из SpigotChanellMsg
    public static void onBungeeData(final String name, final String raw) {
        final Player p = Bukkit.getPlayerExact(name);
        if (p == null) { //данные пришли раньше PlayerJoinEvent
            bungeeDataCache.put(name, raw);
        } else { //если уже был PlayerJoinEvent
            final Oplayer op = PM.getOplayer(p);
            PM.bungeeDataHandle(p, op, raw); //просто прогрузить данные
            if (!LocalDB.useLocalData) { //чтобы игры без локальной БД получали WANT_ARENA_JOIN - отсылать эвент после bungeeDataHandle
                Bukkit.getPluginManager().callEvent(new LocalDataLoadEvent(p, op, null)); //длф миниигр, которые не юзают локальную БД
            }
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(final PlayerJoinEvent e) {
        e.joinMessage(null);
        final Player p = e.getPlayer();
        //LOCALE тут не получить!!! ловить PlayerLocaleChangeEvent
        final Oplayer op = PM.createOplayer(p);

        if (GM.GAME == Game.JL) {
            ScreenUtil.sendTabList(p, "§4ЧИСТИЛИЩЕ", "");
        } else {
            ScreenUtil.sendTabList(p, "", "");//!! перед loadLocalData, или сбрасывает то, что поставила игра
        }

        if (LocalDB.useLocalData) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0));
            Ostrov.async(() -> {
                LocalDB.loadLocalData(p.getName());//локальные данные на загрузку независимо от данных с банжи!
            }, 10); //в таком варианте WANT_ARENA_JOIN будет точно после данных с прокси!

        }// else {
        //    Bukkit.getPluginManager().callEvent(new LocalDataLoadEvent(p, op, null)); //длф миниигр, которые не юзают локальную БД
        //    if (Config.set_gm && !p.isOp()) p.setGameMode(Config.gm_on_join);
        //    if (Config.walkspeed_on_join > 0) p.setWalkSpeed(Config.walkspeed_on_join);
        //}

        final String bungeeData = bungeeDataCache.remove(p.getName());
        if (bungeeData != null) { //данные пришли ранее, берём из кэша
            Ostrov.sync(() -> {
                PM.bungeeDataHandle(p, op, bungeeData);
                if (!LocalDB.useLocalData) { //чтобы игры без локальной БД получали WANT_ARENA_JOIN - отсылать эвент после bungeeDataHandle
                    Bukkit.getPluginManager().callEvent(new LocalDataLoadEvent(p, op, null)); //длф миниигр, которые не юзают локальную БД
                }
            }, 1); //- без задержки не выдавало предметы лобби!
        }

        //player modifications
        p.setShieldBlockingDelay(2);
        p.setNoDamageTicks(20);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerQuit(PlayerQuitEvent e) {
        e.quitMessage(null);
        PM.onLeave(e.getPlayer(), true);
    }

    //отдельным методом, вызов при PlayerQuitEvent или при Plugin.Disable

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void toggleFly(final PlayerToggleFlightEvent e) {
        final Player p = e.getPlayer();
        PM.getOplayer(p).tag.seeThru(e.isFlying() || !p.isSneaking());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void toggleSneak(final PlayerToggleSneakEvent e) {
        final Player p = e.getPlayer();
        if (p.isInsideVehicle() || p.isFlying()) return;
        PM.getOplayer(p).tag.seeThru(!e.isSneaking());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void startTrack(final PlayerTrackEntityEvent e) {
        if (!e.getEntity().getType().isAlive()) return;
        final Player p = e.getPlayer();
        final Oplayer targetOp = PM.getOplayer(e.getEntity().getUniqueId()); //UpperName cn = nameStorage.get(event.getEntity().getUniqueId());
        if (targetOp != null) Ostrov.sync(() -> targetOp.tag.showTo(p), 1);
    }

    //после респавне не меняется
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void stopTrack(final PlayerUntrackEntityEvent e) {
        if (!e.getEntity().getType().isAlive()) return;
        final Player p = e.getPlayer();
        final Oplayer targetOp = PM.getOplayer(e.getEntity().getUniqueId()); //nameStorage.get(event.getEntity().getUniqueId());
        if (targetOp != null) targetOp.tag.hideTo(p);
    }
    
    /*@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void mountEntity(final EntityMountEvent e) {
        final Oplayer op = PM.getOplayer(e.getMount().getUniqueId()); //UpperName cn = nameStorage.get(event.getMount().getUniqueId());
        if (op != null) {
//            op.tag.visible(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void dismountEntity(final EntityDismountEvent e) {
        if (e.getDismounted().getPassengers().size() == 1) {
            final Oplayer op = PM.getOplayer(e.getDismounted().getUniqueId()); //UpperName cn = nameStorage.get(event.getDismounted().getUniqueId());
            if (op!=null) {
                // Run 2 ticks later, we need to ensure that the game sends the packets to update the passengers.
//                Ostrov.sync(() -> op.tag.visible(true), 2);
            }
        }
    }*/


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void FriendTeleport(FriendTeleportEvent e) {
        if (e.source != null && e.source.isOnline() && !e.source.isDead() && PM.inBattle(e.source.getName())) {
            e.setCanceled(true, "§cбитва.");
        }
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (ItemUtil.compare(e.getItemDrop().getItemStack(), InteractLst.passport, ItemUtil.Stat.TYPE, ItemUtil.Stat.NAME)) {
            e.getItemDrop().remove();
            e.getPlayer().updateInventory();
        }
    }


    // ----------------------------- ACTION ----------------------

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent e) {
        //PM.getOplayer(e.getPlayer().getName()).last_breack=Timer.Единое_время();
        if (Cfg.disable_break_place && !ApiOstrov.isLocalBuilder(e.getPlayer())) e.setCancelled(true);
        //else if (!clear_stats) PM.Addbplace(e.getPlayer().getName());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent e) {
        //  PM.getOplayer(e.getPlayer().getName()).last_breack=Timer.Единое_время();
        if (Cfg.disable_break_place && !ApiOstrov.isLocalBuilder(e.getPlayer())) e.setCancelled(true);
        //else if (!clear_stats) PM.get(e.getPlayer().getName());
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHangingBreakByEntityEvent(HangingBreakByEntityEvent e) {
        if (e.getRemover().getType() == EntityType.PLAYER && PM.exist(e.getRemover().getName())) {
            if (Cfg.disable_break_place && !ApiOstrov.isLocalBuilder(e.getRemover())) e.setCancelled(true);
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHangingBreakEvent(HangingBreakEvent e) {
        if (e.getEntity() instanceof Player player) {
            if (Cfg.disable_break_place && !ApiOstrov.isLocalBuilder(player)) e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onPlayerItemFrameChangeEvent(final PlayerItemFrameChangeEvent e) {
        if (Cfg.disable_break_place && !ApiOstrov.isLocalBuilder(e.getPlayer(), true)) {
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
                    EntitySetup.openSetupMenu(p, e.getRightClicked());
                }, 1); //через тик, илил открывает меню торговли
            }
        }

        switch (e.getRightClicked().getType()) {

            case ARMOR_STAND -> e.setCancelled(Cfg.disable_break_place && !ApiOstrov.isLocalBuilder(p, true));

            case ITEM_FRAME, GLOW_ITEM_FRAME -> {
                if (Cfg.disable_break_place && !ApiOstrov.isLocalBuilder(p, true)) {
                    e.setCancelled(true);
                    return;
                }
                final ItemStack it = p.getInventory().getItemInMainHand();
                if (ItemUtil.isBlank(it, false)) break;
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
                }
            }
        }

    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void PlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent e) {
        if (Cfg.disable_break_place && !e.getPlayer().isOp()) e.setCancelled(true);
    }


//---------------------------------------------------


// ----------------------------------- MOVE --------------------------------


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerRespawn(final PlayerRespawnEvent e) {
        final Oplayer op = PM.getOplayer(e.getPlayer());
        if (op == null) return;

        if (Cfg.home_command && op.homes.containsKey("home")) {
            final Player p = e.getPlayer();
            final Location loc = LocUtil.stringToLoc(op.homes.get("home"), false, true);
            if (loc == null) {
                p.sendMessage("§cТочка респавна дома сохранена неправильно\n- " + op.homes.get("home"));
                return;
            }
            final BVec flc = new LocFinder(BVec.of(loc)).find(LocFinder.DYrect.BOTH, 3, 1);
            if (flc == null) {
                p.sendMessage("§7Не получилось респавниться дома - точка дома может быть опасна.");
            } else {
                e.setRespawnLocation(flc.center(loc.getWorld()));
            }
        }

        Ostrov.sync(() -> {
            op.tag.showTo(e.getPlayer());
            PvPManager.pvpEndFor(op, e.getPlayer()); //восстановить настроки до начала битвы, убрать тэги
        }, 5);
    }


  //@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
  //public void onTeleport(final PlayerStartSpectatingEntityEvent e) {
  //  e.getPlayer().sendMessage("PlayerStartSpectatingEntityEvent");
  //}

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
  public void onSpectateEnd(final PlayerStopSpectatingEntityEvent e) {
    final Player p = e.getPlayer();
    final Oplayer op = PM.getOplayer(p);
    if (op == null) return;
    if (op.spyOrigin != null && e.getSpectatorTarget().getType() == EntityType.PLAYER) {
      p.setGameMode(op.spyOldGm);
      final Location loc = op.spyOrigin;
      op.spyOrigin = null; //обнулить до ТП или не даст в PlayerTeleportEvent
      p.teleport(loc);
      //Ostrov.sync(() -> p.setGameMode(oldGamemode), 1);
      final Player target = (Player) e.getSpectatorTarget();
      target.showPlayer(Ostrov.instance, p);
      op.tag.visible(true);
      //p.resetTitle();
      p.sendMessage("§6Наблюдение закончено");
      //e.setCancelled(true); //шпиону не давать ТП
      //p.sendMessage("§6В режиме шпиона нельзя вселяться.");
    }
  }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onTeleport(final PlayerTeleportEvent e) {//перед тп в другой мир
        if (GM.GAME.type == ServerType.ARENAS || GM.GAME.type == ServerType.LOBBY) return;

        final Player p = e.getPlayer();
        final Oplayer op = PM.getOplayer(p);
        if (op == null) return;

      if (op.spyOrigin != null) {// && e.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
        e.setCancelled(true); //шпиону не давать ТП
        p.sendMessage("§6В режиме шпиона нельзя перемещаться");
        return;
      }
        final String world_from = e.getFrom().getWorld().getName();
        final String world_to = e.getTo().getWorld().getName();

        if (!world_from.equals(world_to)) {
            if (!ApiOstrov.isLocalBuilder(p, false) && world_to.endsWith(WorldManager.buildWorldSuffix)) {
                p.sendMessage(Ostrov.PREFIX + "§cТебе не разрешено заходить на этот мир!");
                e.setCancelled(true);
                return;
            }

            if (PvPManager.no_damage_on_tp > 0) {
                op.setNoDamage(PvPManager.no_damage_on_tp, true);//no_damage=PvpCmd.no_damage_on_tp;
            }
          if (Game.storeWorldPosition()) {
            op.world_positions.put(world_from, LocUtil.toDirString(p.getLocation()));//op.PM.OP_Set_world_position(e.getPlayer(), world_from);
          }
            // сохраняем точку выхода
        }
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onChangeWorld(final PlayerChangedWorldEvent e) {//после тп в другой мир
        final Player p = e.getPlayer();
        for (final Player pl : p.getWorld().getPlayers()) {
            PM.getOplayer(pl).tag.showTo(p);
        }
    }


    //------------------------------------------------------------------------


// ------------------------------- ITEM -------------------------------------------    


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void EntityBowShoot(EntityShootBowEvent e) {
        if (e.getEntityType() == EntityType.PLAYER) {

            final Player p = (Player) e.getEntity();
            if (!PM.exist(p.getName())) return;

            if (MenuItemsManager.hasItem("tpbow")) {
                final MenuItem si = MenuItemsManager.fromItemStack(e.getBow());
                if (si != null) {
                    if (Timer.has(p, "bow_teleport")) {//if (PM.getOplayer(p.getName()).bow_teleport_cooldown>0) {
                        p.sendMessage("§cПерезарядка лука.. осталось §4" + Timer.getLeft(p, "bow_teleport") + " сек.");
                        e.setCancelled(true);
                        e.getProjectile().remove();
                    } else {
                        Timer.add(p, "bow_teleport", 4);
                        e.getProjectile().setMetadata("bowteleport", new FixedMetadataValue(Ostrov.instance, "ostrov"));
                    }
                }
            }

            if (!p.isOp() && p.getGameMode().equals(GameMode.CREATIVE)) {
                ScreenUtil.sendActionBar(p, "§cПВП в креативе заблокирован!");
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
        if (e.getEntityType() == EntityType.PLAYER) {
            if (!PM.exist(e.getEntity().getName())) return; //защита от бота
            switch (e.getCause()) {
                case VOID:
                    if (Cfg.disable_void) {
                        e.setDamage(0);
                        e.getEntity().teleport(Bukkit.getWorlds().getFirst().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
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
                    if (Cfg.disable_damage) e.setCancelled(true);
                    //return;
            }
        } else {
            if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                e.getEntity().remove();
                Ostrov.log_warn("Удалена бесконечно падающая в бездну сущность " + e.getEntity());
                return;
            }
            if (Cfg.disable_damage) e.setCancelled(true);
        }
    }
//------------------------------------------------------------------------------ 


    @EventHandler(ignoreCancelled = true)
    public void onPlayerLoseFood(FoodLevelChangeEvent e) {
        if (Cfg.disable_hungry) {
            e.setFoodLevel(20);
            return;
        }
        final Player p = (Player) e.getEntity();
        switch (GM.GAME.type) {
            case LOBBY:
                e.setFoodLevel(20);
                break;
            case ARENAS:
                switch (GM.GAME) {
                    //BB-таблички нужны
                    case WZ: break;
                    case TW, SN, HS, QU:
                        e.setFoodLevel(20);
                        break;
                    default:
                        if (StringUtil.isLobby(p.getWorld()))
                            e.setFoodLevel(20);
                        break;
                }
                break;
            case ONE_GAME:
                switch (GM.GAME) {
                    case PA -> e.setFoodLevel(20);
                    case SK, OB, SG -> {
                        if (StringUtil.isLobby(p.getWorld()))
                            e.setFoodLevel(20);
                    }
                }
        }
    }


}
