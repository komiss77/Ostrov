package ru.komiss77.modules.protocols;

import java.util.*;
import io.papermc.paper.math.Position;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.version.Nms;

public class Protocol77 implements Listener {

    public static boolean active = false;
    private static final ItemStack sub = new ItemStack(Material.REDSTONE);
    private static final BlockData[] bds = new BlockData[]{
        Material.BEDROCK.createBlockData(),
        Material.DEEPSLATE_COAL_ORE.createBlockData(),
        Material.SMOOTH_BASALT.createBlockData(),
        Material.TUFF.createBlockData()};
    private static final String[] title =
        {"§к§kAA",
            "§к§kAAAA",
            "§к§kAAAAAA",
             "§к<obf>AAAA<!obf>§cок§к<obf>AAAA",
            "§к<obf>AAAA<!obf>§cток§к<obf>AAAA",
             "§к<obf>AAA<!obf>§cтоко§к<obf>AAA",
            "§к<obf>AAA<!obf>§cотоко§к<obf>AAA",
            "§к<obf>AA<!obf>§cротоко§к<obf>AA",
            "§к<obf>AA<!obf>§cротокол§к<obf>AA",
             "§к<obf>A<!obf>§cротокол §к<obf>A",
             "§к<obf>A<!obf>§cротокол 7§к<obf>A",
                          "§cПротокол 7§к<obf>A",
            "§cПротокол 77",
            "§cПротокол 77",
            "§cПротокол 77",
            "§cПротокол 77",
            "§cПротокол 77",
            "§cПротокол 77",
            "§cПротокол 77",
            "§cПротокол 77",
            "§cПротокол 77",
            "§cПротокол 77",
            "§cПротокол 77",
            "§cротокол ",
            "§cотокол",
            "§cтоко",
            "§cок",
            ""};
    private static final String[] text =
        ("""
            public Protocol77(final Player p) {
              active = true;
              pid = p.getUniqueId();
              Bukkit.getPluginManager().registerEvents(this, Ostrov.instance);
              immune = new HashSet<>();
              for (final Player pl : Bukkit.getOnlinePlayers()) {
               if (PM.getOplayer(pl).hasGroup(ProtocolCmd.grp)) immune.add(pid);
               final PlayerInventory inv = pl.getInventory();
               final ItemStack it = inv.getItem(4);
               inv.setItem(4, sub);
               if (!ItemUtils.isBlank(it, false)) {
            	ItemUtils.giveItemsTo(p, it);
               }
               inv.setHeldItemSlot(4);
              }
             \s
              final Protocol77 pr = this;
              final String[] text =\s
              "".split("\\n");
             \s
              new BukkitRunnable() {
               int i = 0;
               @Override
               public void run() {
            	switch (i++) {
            	case 1:
            	 for (final Player pl : Bukkit.getOnlinePlayers()) {
            	 \s
            	 }
            	 break;
            	default:
            	 break;
            	}
               \s
            	if (!active) {
            	 HandlerList.unregisterAll(pr);
            	 cancel();
            	 return;
            	}
               \s
            	for (final Player pl : Bukkit.getOnlinePlayers()) {
            	 if (!immune.contains(pl.getUniqueId())) {
            	  pl.closeInventory();
            	 }
            	}
               }
              }.runTaskTimer(Ostrov.instance, 2, 1);
             }

             @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
             public void onMove(final PlayerMoveEvent e) {
              e.setCancelled(!immune.contains(e.getPlayer().getUniqueId()));
             }

             @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
             public void onInter(final PlayerInteractEvent e) {
            	e.setCancelled(!immune.contains(e.getPlayer().getUniqueId()));
             }

             @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
             public void onEnt(final PlayerInteractAtEntityEvent e) {
            	e.setCancelled(!immune.contains(e.getPlayer().getUniqueId()));
             }

             @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
             public void onDamage(final EntityDamageEvent e) {
              e.setCancelled(e.getEntityType() == EntityType.PLAYER && !immune.contains(e.getEntity().getUniqueId()));
             }

             @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
             public void onSwap(final PlayerSwapHandItemsEvent e) {
              e.setCancelled(!immune.contains(e.getPlayer().getUniqueId()));
             }

             @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
             public void onOpen(final InventoryOpenEvent e) {
              e.setCancelled(!immune.contains(e.getPlayer().getUniqueId()));
             }

             @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
             public void onInv(final InventoryInteractEvent e) {
              e.setCancelled(!immune.contains(e.getWhoClicked().getUniqueId()));
             }

             @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
             public void onHeld(final PlayerItemHeldEvent e) {
              e.setCancelled(!immune.contains(e.getPlayer().getUniqueId()));
             }

             @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
             public void onQuit(final PlayerQuitEvent e) {
              if (pid.equals(e.getPlayer().getUniqueId())) active = false;
             }

             @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
             public void onDrop(final PlayerDropItemEvent e) {
              final Player p = e.getPlayer();
              if (pid.equals(p.getUniqueId()) && p.getInventory().getHeldItemSlot() == 4) active = false;
              else e.setCancelled(!immune.contains(p.getUniqueId()));""").split("\n");

    private final UUID pid;
    private final HashSet<UUID> immune;

    public Protocol77(final Player p) {
        active = true;
        pid = p.getUniqueId();
        Bukkit.getPluginManager().registerEvents(this, Ostrov.instance);
        immune = new HashSet<>();
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            if (Perm.isStaff(PM.getOplayer(pl), 2)) immune.add(pl.getUniqueId());
            final PlayerInventory inv = pl.getInventory();
            final ItemStack it = inv.getItem(4);
            inv.setItem(4, sub);
            if (!ItemUtil.isBlank(it, false)) {
                ItemUtil.giveItemsTo(pl, it);
            }
            inv.setHeldItemSlot(4);
        }

        final Protocol77 pr = this;

        new BukkitRunnable() {
            private int i = 0, ti = 0, li = 0, bi = 0;
            private final Location lc = p.getLocation();

            @Override
            public void run() {
                if (ti < text.length) {
                    final String tx = text[ti];
                    switch (i++) {
                        case 2, 12, 20, 24, 26:
                            ti++;
                            for (final Player pl : Bukkit.getOnlinePlayers()) {
                                pl.playSound(pl.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1f, 0.02f * i + 0.5f);
                                pl.sendMessage(tx);
                            }
                            break;
                        default:
                            if (i > 28) {
                                if (li < title.length) {
                                    final String ttl = title[li];
                                    for (final Player pl : Bukkit.getOnlinePlayers()) {
                                        pl.playSound(pl.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1f, 0.02f * i + 0.5f);
                                        ScreenUtil.sendTitleDirect(pl, ttl, "", 0, 8, 4);
                                        pl.sendMessage(tx);
                                    }
                                } else {
                                    for (final Player pl : Bukkit.getOnlinePlayers()) {
                                        pl.playSound(pl.getLocation(), Sound.BLOCK_IRON_TRAPDOOR_OPEN, 1f, 0.02f * i + 0.5f);
                                        pl.sendMessage(tx);
                                    }
                                }

                                ti++;
                                li++;
                                final boolean dark = (bi++) >> 4 == 0;
                                if (bi < 32) {
                                    final int dl = dark ? bi : bi - 15;
                                    final Collection<? extends Player> pls = Bukkit.getOnlinePlayers();
                                    final Map<Position, BlockData> bdm = new HashMap<>();
                                    for (int x = -dl; x <= dl; x++) {
                                        for (int y = -dl; y <= dl; y++) {
                                            for (int z = -dl; z <= dl; z++) {
                                                if (((NumUtil.abs(x) - dl) >> 31) + ((NumUtil.abs(y) - dl) >> 31)
                                                    + ((NumUtil.abs(z) - dl) >> 31) + 2 < 0) continue;
                                                final BVec cl = BVec.of(lc).add(x, y, z);
                                                if (Nms.fastType(lc.getWorld(), cl.x, cl.y, cl.z).hasCollision()) {
                                                    bdm.put(cl, dark ? ClassUtil.rndElmt(bds) : cl.w().getBlockData(cl.x, cl.y, cl.z));
                                                }
                                            }
                                        }
                                    }

                                    for (final Player pl : pls) pl.sendMultiBlockChange(bdm);
                                }
                            }
                            break;
                    }
                }

                if (!active) {
                    HandlerList.unregisterAll(pr);
                    for (final Player pl : Bukkit.getOnlinePlayers()) {
                        pl.playSound(pl.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 4f, 0.6f);
                        pl.clearTitle();
                    }
                    cancel();
                    return;
                }

                for (final Player pl : Bukkit.getOnlinePlayers()) {
                    if (!immune.contains(pl.getUniqueId())) {
                        pl.closeInventory();
                    }
                }
            }
        }.runTaskTimer(Ostrov.instance, 2, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onMove(final PlayerMoveEvent e) {
        e.setCancelled(!immune.contains(e.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInter(final PlayerInteractEvent e) {
        e.setCancelled(!immune.contains(e.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEnt(final PlayerInteractAtEntityEvent e) {
        e.setCancelled(!immune.contains(e.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDamage(final EntityDamageEvent e) {
        e.setCancelled(e.getEntityType() == EntityType.PLAYER && !immune.contains(e.getEntity().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onSwap(final PlayerSwapHandItemsEvent e) {
        e.setCancelled(!immune.contains(e.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onOpen(final InventoryOpenEvent e) {
        e.setCancelled(!immune.contains(e.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onInv(final InventoryInteractEvent e) {
        e.setCancelled(!immune.contains(e.getWhoClicked().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onHeld(final PlayerItemHeldEvent e) {
        e.setCancelled(!immune.contains(e.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onQuit(final PlayerQuitEvent e) {
        if (pid.equals(e.getPlayer().getUniqueId())) active = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDrop(final PlayerDropItemEvent e) {
        final Player p = e.getPlayer();
        if (pid.equals(p.getUniqueId()) && p.getInventory().getHeldItemSlot() == 4) active = false;
        else e.setCancelled(!immune.contains(p.getUniqueId()));
    }
}
