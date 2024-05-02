package ru.komiss77.listener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.google.common.collect.ArrayListMultimap;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.events.LocalDataLoadEvent;
import ru.komiss77.hook.WGhook;
import ru.komiss77.modules.bots.BotEntity;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.EntityUtil.EntityGroup;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.version.Nms;


//просто скинул сюда всё из двух мелких плагинов
public class ArcaimLst implements Listener {

    private static final String admin = "komiss77";

    public ArcaimLst() {
        BotManager.regSkin(admin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public static void onInteract(PlayerInteractEvent e) {
        final Player p = e.getPlayer();

        if (p.getGameMode() == GameMode.SPECTATOR && (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK)) {
            if (p.getOpenInventory().getType() != InventoryType.CHEST) {
              if (PM.getOplayer(p.getUniqueId()).setup==null) {
                p.performCommand("menu");
              }
            }
        }
    }



    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(final PlayerBucketEmptyEvent e) {
        if (ApiOstrov.isLocalBuilder(e.getPlayer(), false)) {
            return;
        }
        //if (nobuild.wg.getRegionManager(e.getPlayer().getWorld()).getApplicableRegions(e.getBlock().getLocation()).size()==0 ) {
        if (WGhook.getRegionsOnLocation(e.getBlock().getLocation()).size() == 0) {
            e.setCancelled(true);
//            e.setCancelled(true);
            e.getPlayer().sendMessage("§cСтроить можно только в приватах!");
        }
    }

    //--------------------------------
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFromTo(final BlockFromToEvent e) {
        if (e.getBlock().getType() == Material.LAVA || e.getBlock().getType() == Material.WATER) {
            final ApplicableRegionSet fromRegionSet = WGhook.getRegionsOnLocation(e.getBlock().getLocation());
            final ApplicableRegionSet toRegionSet = WGhook.getRegionsOnLocation(e.getToBlock().getLocation());
            if (fromRegionSet.size() == 1 && toRegionSet.size() == 1) { //из привата в приват, обычная ситуация
                e.setCancelled(!fromRegionSet.getRegions().contains(toRegionSet.getRegions().iterator().next()));
            } else {
                e.setCancelled(e.getFace() != BlockFace.DOWN && e.getToBlock().getRelative(BlockFace.DOWN).getType().isAir());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onlavaPlaceEntity(PlayerInteractAtEntityEvent e) {
        final ItemStack is = e.getPlayer().getInventory().getItem(e.getHand());//ItemInOffHand();
        switch (is.getType()) {
            case WATER_BUCKET ->
                e.setCancelled(EntityUtil.group(e.getRightClicked().getType()) != EntityGroup.WATER_AMBIENT);
            case LAVA, LAVA_BUCKET, WATER, AXOLOTL_BUCKET, COD_BUCKET, PUFFERFISH_BUCKET, SALMON_BUCKET, TADPOLE_BUCKET, TROPICAL_FISH_BUCKET ->
                e.setCancelled(true);
            default -> {
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSpawn(final CreatureSpawnEvent e) {
       
        switch (e.getEntityType()) {
            case ENDER_DRAGON -> {
              if (e.getSpawnReason()!= CreatureSpawnEvent.SpawnReason.NATURAL) e.getEntity().remove();
            }

            default -> {
                 if (e.getSpawnReason()==CreatureSpawnEvent.SpawnReason.DISPENSE_EGG || e.getSpawnReason()==CreatureSpawnEvent.SpawnReason.EGG) {
                     e.setCancelled(WGhook.getRegionsOnLocation(e.getEntity().getLocation()).size() == 0);
                }
            }
                
        }
    }

   /* @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSpawn(final EntitySpawnEvent e) {
        switch (e.getEntityType()) {
            case ENDER_DRAGON -> e.getEntity().remove();
            default -> e.setCancelled(WGhook.getRegionsOnLocation(e.getEntity().getLocation()).size() == 0);
        }
    }*/

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplode(final EntityExplodeEvent e) {
        e.blockList().removeIf(block -> WGhook.getRegionsOnLocation(block.getLocation()).size() == 0);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreative(final InventoryCreativeEvent e) {
        final ItemStack cr = e.getCursor();
        if (ItemUtils.isBlank(cr, true) || ApiOstrov.isLocalBuilder(e.getWhoClicked(), true)) return;
        final ItemMeta met = cr.getItemMeta();
        switch (cr.getType()) {
            case POTION, SPLASH_POTION, LINGERING_POTION, TIPPED_ARROW:
                if (met instanceof final PotionMeta pm) {
                    final List<PotionEffect> bad_effects = new ArrayList<>();
                    for (PotionEffect effect : pm.getCustomEffects()) {
                        if (effect.getAmplifier() > 10) {
                            bad_effects.add(effect);
                        }
                    }
                    for (PotionEffect potionEffect : pm.getCustomEffects()) {
                        if (bad_effects.contains(potionEffect)) {
                            pm.removeCustomEffect(potionEffect.getType());
                            PotionEffect pf = new PotionEffect(potionEffect.getType(), potionEffect.getDuration(), 10, potionEffect.isAmbient(), potionEffect.hasParticles(), potionEffect.hasIcon());
                            pm.addCustomEffect(pf, true);
                        }
                    }
                }
                met.setAttributeModifiers(ArrayListMultimap.create());
                cr.setItemMeta(met);
                break;
            case ENCHANTED_BOOK:
                if (met instanceof EnchantmentStorageMeta) {
                    for (Map.Entry<Enchantment, Integer> en :
                        ((EnchantmentStorageMeta) met).getStoredEnchants().entrySet()) {
                        if (en.getValue() > 10) en.setValue(10);
                    }
                }
                break;
            default:
                e.setCursor(new ItemStack(cr.getType(), cr.getAmount()));
                e.getWhoClicked().sendMessage(Ostrov.PREFIX + "§cДанные предмета были очищены!");
                break;
        }
    }


    @EventHandler
    public void onFirst(final LocalDataLoadEvent e) {
        final Oplayer op = e.getOplayer();
        if (op.firstJoin) {
            final Player p = e.getPlayer();
            p.setGameMode(GameMode.SURVIVAL);
            op.firstJoin = false;
            Ostrov.sync(() -> {
                if (!p.isValid() || !p.isOnline()) return;
                final Location loc = new Location(p.getWorld(), 130, 73, -281);
                p.teleport(loc);
                final AdminBot ab = BotManager.createBot(admin, AdminBot.class, nm -> new AdminBot(p));
                if (ab != null) {
                    ab.telespawn(loc, null);
                    ab.tab("", ChatLst.NIK_COLOR, " §7(§eСисАдмин§7)");
                    ab.tag("", ChatLst.NIK_COLOR, " §7(§eСисАдмин§7)");
                    ab.getEntity().setGravity(false);
                    p.playSound(loc, Sound.ENTITY_WANDERING_TRADER_AMBIENT, 2f, 0.8f);
                    p.sendMessage(GM.getLogo().append(TCUtils.format(
                        "§2komiss77 §7» О, привет, " + p.getName() + "! ты тут новичек?")));
                    Ostrov.sync(() -> {
                        if (!p.isValid() || !p.isOnline()) return;
                        p.playSound(ab.getEntity().getEyeLocation(), Sound.ENTITY_WANDERING_TRADER_TRADE, 2f, 0.8f);
                        p.sendMessage(GM.getLogo().append(TCUtils.format(
                            "§2komiss77 §7» Я тут заскучал строить уже, может ты мне сможешь помочь?")));
                        Ostrov.sync(() -> {
                            if (!p.isValid() || !p.isOnline()) return;
                            p.playSound(ab.getEntity().getEyeLocation(), Sound.ENTITY_WANDERING_TRADER_YES, 2f, 0.8f);
                            p.sendMessage(GM.getLogo().append(TCUtils.format(
                                "§2komiss77 §7» Вот! Бери креатив, и иди построй что пожелаешь в этом мире!")));
                            p.sendMessage("Ваш игроаой режим был изменен на Творческий режим");
                            p.setGameMode(GameMode.CREATIVE);
                            Ostrov.sync(() -> {
                                if (!p.isValid() || !p.isOnline()) return;
                                p.teleport(p.getWorld().getSpawnLocation());
                                ab.remove();
                            }, 80);
                        }, 120);
                    }, 80);
                }
            }, 80);
        }
    }


  private static class AdminBot extends BotEntity {

    protected final Player tgt;

    protected AdminBot(final Player tgt) {
      super(admin, tgt.getWorld());
      this.tgt = tgt;
    }

    @Override
    public void onDamage(final EntityDamageEvent e) {
      e.setCancelled(true);
      e.setDamage(0d);
    }

    @Override
    public Goal<Mob> getGoal(final Mob org) {
      return new AdminGoal(this);
    }
  }

  private static class AdminGoal implements Goal<Mob> {

    private static final GoalKey<Mob> key = GoalKey.of(Mob.class, new NamespacedKey(Ostrov.instance, "bot"));

    private final AdminBot bot;
    private final WeakReference<Player> trf;

    private int tick;

    public AdminGoal(final AdminBot bot) {
      this.trf = new WeakReference<>(bot.tgt);
      this.bot = bot;
      this.tick = 0;
    }

    @Override
    public boolean shouldActivate() {
      return true;
    }

    @Override
    public boolean shouldStayActive() {
      return true;
    }

    @Override
    public void start() {}

    @Override
    public void stop() {bot.remove();}

    @Override
    public void tick() {
      final Mob rplc = (Mob) bot.getEntity();
      if (rplc == null || !rplc.isValid()) {
        bot.remove();
        return;
      }

      final Player tgt = trf.get();
      if (tgt == null || !tgt.isValid() || !tgt.isOnline()) {
        bot.remove();
        return;
      }
      //Bukkit.broadcast(Component.text("le-" + rplc.getName()));
      final Location loc = rplc.getLocation();

      final Vector vc;
      if ((tick++ & 7) == 0 && Ostrov.random.nextBoolean()) {
        vc = tgt.getLocation().add(Ostrov.random.nextDouble() - 0.5d,
            Ostrov.random.nextDouble() - 0.5d, Ostrov.random.nextDouble() - 0.5d)
          .subtract(loc).toVector();
        if (vc.lengthSquared() < 10) {
          Nms.sendWorldPackets(bot.world, new ClientboundAnimatePacket(bot, 0));
          tgt.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_WEAK, 1f, 1f);
        }
      } else vc = tgt.getLocation().subtract(loc).toVector();
      bot.move(loc, vc.normalize(), true);
    }

    @Override
    public @NotNull GoalKey<Mob> getKey() {
      return key;
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
      return EnumSet.of(GoalType.MOVE, GoalType.LOOK);
    }
  }




  // ------------ No build outside -------------
  //@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
/*  public void onPlace(final BlockPlaceEvent e) {
    if (ApiOstrov.isLocalBuilder(e.getPlayer(), false)) {
      return;
    }
    //if (nobuild.wg.getRegionManager(e.getPlayer().getWorld()).getApplicableRegions(e.getBlock().getLocation()).size()==0 ) {
    if (WGhook.getRegionsOnLocation(e.getBlock().getLocation()).size() == 0) {
      e.setBuild(false);
//            e.setCancelled(true);
      e.getPlayer().sendMessage("§cСтроить можно только в приватах!");
    }
  }
  //@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBreak(final BlockBreakEvent e) {
    if (ApiOstrov.isLocalBuilder(e.getPlayer(), false)) {
      return;
    }
    if (WGhook.getRegionsOnLocation(e.getBlock().getLocation()).size() == 0) {
      e.setCancelled(true);
      e.getPlayer().sendMessage("§cСтроить можно только в приватах!");
    }
  }*/

   /* @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDrop(final EntityDropItemEvent e) {
        if (e.getEntityType() == EntityType.PLAYER) {
            final ItemStack it = e.getItemDrop().getItemStack();
            if (ItemUtils.isBlank(it, true) || ApiOstrov.isLocalBuilder(e.getEntity(), true)) return;
            if (!ItemUtils.isBlank(it, true)) {
                switch (it.getType()) {
                    case POTION, SPLASH_POTION, LINGERING_POTION, TIPPED_ARROW, ENCHANTED_BOOK:
                      break;
                    default:
                        e.getItemDrop().setItemStack(new ItemStack(it.getType(), it.getAmount()));
                        e.getEntity().sendMessage(Ostrov.PREFIX + "§cДанные предмета были очищены!");
                        break;
                }
            }
        }
    }*/

   /*@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void Interact(PlayerInteractEvent e) { 
        if (ApiOstrov.isLocalBuilder(e.getPlayer(), false) ) return;
//System.out.println("size="+WGutils.getRegionsOnLocation(e.getClickedBlock().getLocation()).size()+" --"+WGutils.getRegionsOnLocation(e.getClickedBlock().getLocation()) );
        if (e.getAction()==Action.LEFT_CLICK_BLOCK || e.getAction()==Action.RIGHT_CLICK_BLOCK) {
            //if (nobuild.wg.getRegionManager(e.getPlayer().getWorld()).getApplicableRegions(e.getClickedBlock().getLocation()).size()==0 ) {
            if (WGhook.getRegionsOnLocation(e.getClickedBlock().getLocation()).size()==0 ) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cСтроить можно только в приватах!");
            }
        }
    }*/
 /*@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onlavaPlace(final PlayerInteractEvent e) {
//System.out.println("action="+e.getAction()+"  item="+e.getItem()+" mat="+e.getMaterial());
        if (e.getAction()==Action.RIGHT_CLICK_BLOCK && e.getItem()!=null) {
            if ( (e.getItem().getType().name().contains("LAVA") && e.getPlayer().getWorld().getEnvironment()!=World.Environment.NETHER) || e.getItem().getType().name().contains("WATER") ) {
                final ApplicableRegionSet regionSet = WGhook.getRegionsOnLocation(e.getClickedBlock().getLocation());//rm.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));
                if (regionSet.size()==0) {
                    e.setUseItemInHand(Event.Result.DENY);
                    return;
                }
                final LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(e.getPlayer());
                for (final ProtectedRegion rg : regionSet) {
                    if (!rg.isOwner(lp) && !rg.isMember(lp)) {
                        e.setUseItemInHand(Event.Result.DENY);
                        return;
                    }
                }
            }
        }
    }*/
 /*@EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        final Location loc = e.getBlock().getLocation();
        final int y = loc.getBlockY();
        //final ApplicableRegionSet setUp = NoLavaWater.getRegionsOnLocation(loc);
        loc.setY(70);
        final ApplicableRegionSet setDown = WGhook.getRegionsOnLocation(loc);
        if (setDown.size()>=1) {
            final ProtectedRegion rg = setDown.getRegions().stream().findFirst().get();
            if (rg.getMaximumPoint().getBlockY()<=y) {
//System.out.println("- onBucketEmpty выше привата!");
                e.getPlayer().sendMessage("§cНельзя разливать над приватом!");
                e.setCancelled(true);
                //e.setItemStack(new ItemStack(Material.BUCKET));
                if (e.getPlayer().getInventory().getItemInMainHand().getType()==e.getBucket()) {
                    e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.BUCKET));
                } else {
                   e.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.BUCKET));
                }
            }
        }
        
        //if (!can(e.getBlockClicked().getLocation(),1)) {
        //    e.setCancelled(true);
//System.out.println("-- onBucketEmpty cancel!!");
        //}
    }*/
    //doFireTick moment
    /*@EventHandler(priority = EventPriority.NORMAL,ignoreCancelled = true)
    public void onBurn( BlockBurnEvent e) { //распространение огня
//System.out.println("BlockBurnEvent "+e.getIgnitingBlock());        
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void igniteFire(BlockIgniteEvent e) {
        if (e.getCause() == BlockIgniteEvent.IgniteCause.SPREAD ) {
            e.setCancelled(true);
        }
    }*/
    //зачем + неработает правильно
    /*@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleMoveEvent(VehicleMoveEvent e) {
        if (e.getFrom().getBlockX()==e.getTo().getBlockX() && e.getFrom().getBlockY()==e.getTo().getBlockY() && e.getFrom().getBlockZ()==e.getTo().getBlockZ()  ) return;
        //if ( Ostrov.getWorldGuard()==null ) return;
//System.out.println("-- VehicleMoveEvent");
        final RegionQuery query = WGhook.worldguard_platform.getRegionContainer().createQuery();
        final ApplicableRegionSet regionSetTo = query.getApplicableRegions(BukkitAdapter.adapt(e.getTo()));
        final ApplicableRegionSet regionSetFrom = query.getApplicableRegions(BukkitAdapter.adapt(e.getFrom()));
        if (regionSetFrom.size()==0 || regionSetTo.size()==0 ) {
            final Vector back = e.getTo().getDirection().multiply( - (3 * 0.1) );
            e.getVehicle().setVelocity(back);
        }
        for (final ProtectedRegion rg : regionSetFrom.getRegions()) {
            if (regionSetTo.getRegions().contains(rg)) return;
        }
        final Vector back = e.getTo().getDirection().multiply( - (3 * 0.1) );
        e.getVehicle().setVelocity(back);
//System.out.println("size="+WGutils.getRegionsOnLocation(e.getBlock().getLocation()).size()+" --"+WGutils.getRegionsOnLocation(e.getBlock().getLocation()) );
    }*/
}
