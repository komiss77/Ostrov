package ru.komiss77.modules.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockType;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.hook.WGhook;
import ru.komiss77.utils.ItemUtil;


public class ItemManager implements Initiable, Listener {

    public static boolean enable;

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }

    @Override
    public void reload() {
        ItemRoll.loadAll();
        HandlerList.unregisterAll(this);
        if (!enable) return;

        Ostrov.log_ok("§2Предметы включены!");
        Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
    }

    @Override
    public void onDisable() {
        if (!enable) return;

        Ostrov.log_ok("§6Предметы выключены!");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent e) {
        process(e.getEntity(), new Processor() {
            public void onMats(final EquipmentSlot[] ess, final CustomMats cm) {cm.onDefense(ess, e);}
            public void onSpec(final EquipmentSlot es, final SpecialItem si) {si.onDefense(es, e);}
        });
        if (e instanceof final EntityDamageByEntityEvent ee) {
            process(e.getDamageSource().getCausingEntity(), new Processor() {
                public void onMats(final EquipmentSlot[] ess, final CustomMats cm) {cm.onAttack(ess, ee);}
                public void onSpec(final EquipmentSlot es, final SpecialItem si) {si.onAttack(es, ee);}
            });
        }

        if (e.getEntity() instanceof final Item ie &&
            DamageType.OUT_OF_WORLD.equals(e.getDamageSource().getDamageType())) {
            final SpecialItem si = SpecialItem.get(ie.getItemStack());
            if (si == null) return;
            si.destroy();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent e) {
        process(e.getPlayer(), new Processor() {
            public void onMats(final EquipmentSlot[] ess, final CustomMats cm) {cm.onInteract(ess, e);}
            public void onSpec(final EquipmentSlot es, final SpecialItem si) {si.onInteract(es, e);}
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(final BlockBreakEvent e) {
        process(e.getPlayer(), new Processor() {
            public void onMats(final EquipmentSlot[] ess, final CustomMats cm) {cm.onBreak(ess, e);}
            public void onSpec(final EquipmentSlot es, final SpecialItem si) {si.onBreak(es, e);}
        });
        if (!SpecialItem.exist) return;
        if (!BlockType.BARRIER.equals(e.getBlock().getType().asBlockType())) return;
        final ItemStack hnd = e.getPlayer().getInventory().getItemInMainHand();
        final SpecialItem spi = SpecialItem.get(hnd);
        if (spi != null) spi.saveAll(hnd);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(final BlockPlaceEvent e) {
        process(e.getPlayer(), new Processor() {
            public void onMats(final EquipmentSlot[] ess, final CustomMats cm) {cm.onPlace(ess, e);}
            public void onSpec(final EquipmentSlot es, final SpecialItem si) {si.onPlace(es, e);}
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(final ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof final LivingEntity le) {
            process(le, new Processor() {
                public void onMats(final EquipmentSlot[] ess, final CustomMats cm) {cm.onShoot(ess, e);}
                public void onSpec(final EquipmentSlot es, final SpecialItem si) {si.onShoot(es, e);}
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerJumpEvent e) {
        process(e.getPlayer(), extraProc(e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerToggleFlightEvent e) {
        process(e.getPlayer(), extraProc(e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerPickupExperienceEvent e) {
        process(e.getPlayer(), extraProc(e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerChangedMainHandEvent e) {
        process(e.getPlayer(), extraProc(e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerStopUsingItemEvent e) {
        process(e.getPlayer(), extraProc(e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerRiptideEvent e) {
        process(e.getPlayer(), extraProc(e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerItemConsumeEvent e) {
        process(e.getPlayer(), extraProc(e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExtra(final PlayerElytraBoostEvent e) {
        process(e.getPlayer(), extraProc(e));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(final PlayerInteractEntityEvent e) {
        process(e.getPlayer(), extraProc(e));
    }

    private static void process(final Entity ent, final Processor pc) {
        if (ent instanceof final LivingEntity le) {
            final HashMap<CustomMats, List<EquipmentSlot>> cmp = new HashMap<>();
            final EntityEquipment eq = le.getEquipment();
            if (eq == null) return;
            for (final EquipmentSlot es : EquipmentSlot.values()) {
                final ItemStack is = eq.getItem(es);
                if (!SpecialItem.exist) {
                    final SpecialItem spi = SpecialItem.get(is);
                    if (spi != null) {
                        pc.onSpec(es, spi);
                        continue;
                    }
                }
                if (!CustomMats.exist || ItemUtil.isBlank(is, true)) continue;
                final CustomMats cm = CustomMats.get(is);
                if (cm == null) continue;
                final List<EquipmentSlot> ess = cmp.get(cm);
                if (ess == null) {
                    final List<EquipmentSlot> nes = new ArrayList<>();
                    nes.add(es);
                    cmp.put(cm, nes);
                } else {
                    ess.add(es);
                }
            }
            if (!CustomMats.exist) return;
            for (final Map.Entry<CustomMats, List<EquipmentSlot>> en : cmp.entrySet()) {
                pc.onMats(en.getValue().toArray(new EquipmentSlot[0]), en.getKey());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(final EntityDropItemEvent e) {
        final Item drop = e.getItemDrop();
        final ItemStack it = drop.getItemStack();
        final SpecialItem si = SpecialItem.get(it);
        if (si == null) return;
        if (!si.crafted()) {
            drop.remove();
            e.setCancelled(true);
            Ostrov.log_warn("Uncrafted SpecialItem " + si.name() + " removed!");
            return;
        }
        if (si.dropped()) {
            drop.remove();
            Ostrov.log_warn("Duplicate SpecialItem " + si.name() + " removed!");
            return;
        }
        si.apply(drop);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPick(final EntityPickupItemEvent e) {
        final Item drop = e.getItem();
        final ItemStack it = drop.getItemStack();
        final SpecialItem si = SpecialItem.get(it);
        if (si == null) return;
        if (!si.crafted()) {
            drop.remove();
            e.setCancelled(true);
            Ostrov.log_warn("Uncrafted SpecialItem " + si.name() + " removed!");
            return;
        }
        if (!si.dropped()) {
            drop.remove();
            e.setCancelled(true);
            Ostrov.log_warn("Duplicate SpecialItem " + si.name() + " removed!");
            return;
        }

        if (e.getEntityType() != EntityType.PLAYER) {
            drop.setPickupDelay(20);
            e.setCancelled(true);
            return;
        }

        si.obtain(e.getEntity(), it);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoad(final EntitiesLoadEvent e) {
        for (final Entity en : e.getEntities()) {
            if (en instanceof final Item it) {
                final SpecialItem si = SpecialItem.get(it);
                if (si == null) continue;
                if (!si.crafted()) {
                    it.remove();
                    Ostrov.log_warn("Uncrafted SpecialItem " + si.name() + " removed!");
                    continue;
                }
                if (!si.dropped()) {
                    it.remove();
                    Ostrov.log_warn("Duplicate SpecialItem " + si.name() + " removed!");
                    continue;
                }

                if (si.loc() == null) continue;
                final Location loc = si.loc().getCenterLoc();
                if (isInPrivateWG(loc)) {
                    si.spawn(SpecialItem.SPAWN.getCenterLoc(), it.getItemStack());
                    it.remove();
                } else si.apply(it);
            }
        }
    }

    private static boolean isInPrivateWG(final Location loc) {
        return Ostrov.wg && WGhook.getRegionsOnLocation(loc).size() != 0;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUnLoad(final EntitiesUnloadEvent e) {
        for (final Entity en : e.getEntities()) {
            if (en instanceof final Item it) {
                final SpecialItem si = SpecialItem.get(it);
                if (si == null) continue;
                if (!si.crafted()) {
                    it.remove();
                    Ostrov.log_warn("Uncrafted SpecialItem " + si.name() + " removed!");
                    continue;
                }
                if (!si.dropped()) {
                    it.remove();
                    Ostrov.log_warn("Duplicate SpecialItem " + si.name() + " removed!");
                    continue;
                }

                it.setVelocity(new Vector());
                si.loc(it.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopper(final InventoryPickupItemEvent e) {
        final SpecialItem si = SpecialItem.get(e.getItem().getItemStack());
        if (si == null) return;
        e.getItem().setPickupDelay(20);
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemBreak(final PlayerItemBreakEvent e) {
        final SpecialItem si = SpecialItem.get(e.getBrokenItem());
        if (si == null) return;
        si.destroy();
    }

    public interface Processor {
        void onMats(final EquipmentSlot[] ess, final CustomMats cm);
        void onSpec(final EquipmentSlot es, final SpecialItem si);
    }

    private static Processor extraProc(final PlayerEvent e) {
        return new Processor() {
            public void onMats(final EquipmentSlot[] ess, final CustomMats cm) {cm.onExtra(ess, e);}
            public void onSpec(final EquipmentSlot es, final SpecialItem si) {si.onExtra(es, e);}
        };
    }
}
