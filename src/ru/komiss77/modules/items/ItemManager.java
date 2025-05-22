package ru.komiss77.modules.items;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import ru.komiss77.Cfg;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.hook.WGhook;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;


public class ItemManager implements Initiable, Listener {

    public ItemManager() {
        reload();
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }

    @Override
    public void reload() {
        ItemRoll.loadAll();
        HandlerList.unregisterAll(this);
        if (!Cfg.items) return;

        Ostrov.log_ok("§2Предметы включены!");
        Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
    }

    @Override
    public void onDisable() {
        if (!Cfg.items) return;

        Ostrov.log_ok("§6Предметы выключены!");
    }

    private static final Set<DamageType> DESTROY = Set.of(DamageType.OUT_OF_WORLD, DamageType.OUTSIDE_BORDER);
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRemove(final EntityRemoveEvent e) {
        if (e.getEntity() instanceof final Item ie
            && ie.getLocation().getBlockY() < ie.getWorld().getMinHeight()) {
            final SpecialItem si = SpecialItem.get(ie.getItemStack());
            if (si != null) si.destroy();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent e) {
        process(e.getEntity(), new Processor() {
            public void onGroup(final EquipmentSlot[] ess, final ItemGroup cm) {cm.onDefense(ess, e);}
            public void onSpec(final EquipmentSlot es, final SpecialItem si) {si.onDefense(es, e);}
        });
        if (e instanceof final EntityDamageByEntityEvent ee) {
            process(e.getDamageSource().getCausingEntity(), new Processor() {
                public void onGroup(final EquipmentSlot[] ess, final ItemGroup cm) {cm.onAttack(ess, ee);}
                public void onSpec(final EquipmentSlot es, final SpecialItem si) {si.onAttack(es, ee);}
            });
        }

        if (e.getEntity() instanceof final Item ie) {
            final SpecialItem si = SpecialItem.get(ie.getItemStack());
            if (si != null) {
                if (DESTROY.contains(e.getDamageSource().getDamageType())) {
                    si.destroy();
                    return;
                }
                e.setDamage(0d);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent e) {
        process(e.getPlayer(), new Processor() {
            public void onGroup(final EquipmentSlot[] ess, final ItemGroup cm) {cm.onInteract(ess, e);}
            public void onSpec(final EquipmentSlot es, final SpecialItem si) {si.onInteract(es, e);}
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(final ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof final LivingEntity le) {
            process(le, new Processor() {
                public void onGroup(final EquipmentSlot[] ess, final ItemGroup cm) {cm.onShoot(ess, e);}
                public void onSpec(final EquipmentSlot es, final SpecialItem si) {si.onShoot(es, e);}
            });
        }
    }

    protected static void process(final Entity ent, final Processor pc) {
        if (!(ent instanceof final LivingEntity le)) return;
        final HashMap<ItemGroup, List<EquipmentSlot>> cmp = new HashMap<>();
        final EntityEquipment eq = le.getEquipment();
        if (eq == null) return;
        for (final EquipmentSlot es : EquipmentSlot.values()) {
            if (!le.canUseEquipmentSlot(es)) continue;
            final ItemStack is = eq.getItem(es);
            if (SpecialItem.exist) {
                final SpecialItem spi = SpecialItem.get(is);
                if (spi != null) {
                    pc.onSpec(es, spi);
                    continue;
                }
            }
            if (!ItemGroup.exist || ItemUtil.isBlank(is, true)) continue;
            final ItemGroup cm = ItemGroup.get(is);
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
        if (!ItemGroup.exist) return;
        for (final Map.Entry<ItemGroup, List<EquipmentSlot>> en : cmp.entrySet()) {
            pc.onGroup(en.getValue().toArray(new EquipmentSlot[0]), en.getKey());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(final EntityDropItemEvent e) {
        onDrop(e, e.getItemDrop());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(final PlayerDropItemEvent e) {
        onDrop(e, e.getItemDrop());
    }

    private static void onDrop(final Cancellable e, final Item drop) {
        final ItemStack it = drop.getItemStack();
        final SpecialItem si = SpecialItem.get(it);
        if (si == null) return;
        if (!si.crafted()) {
            drop.remove();
            e.setCancelled(true);
            SpecialItem.info("Uncrafted " + si.name() + " removed!");
            return;
        }
        if (si.dropped()) {
            drop.remove();
            e.setCancelled(true);
            SpecialItem.info("Undropped " + si.name() + " removed!");
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
            SpecialItem.info("Uncrafted " + si.name() + " removed!");
            return;
        }
        if (!si.dropped()) {
            drop.remove();
            e.setCancelled(true);
            SpecialItem.info("Undropped " + si.name() + " removed!");
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
            if (!(en instanceof final Item it)) continue;
            final SpecialItem si = SpecialItem.get(it);
            if (si == null) continue;
            if (!si.crafted()) {
                it.remove();
                SpecialItem.info("Uncrafted " + si.name() + " removed!");
                continue;
            }
            if (!si.dropped()) {
                it.remove();
                SpecialItem.info("Undropped " + si.name() + " removed!");
                continue;
            }

            if (si.own() instanceof final Item ii && ii.isValid()
                && ii.getEntityId() != it.getEntityId()) {
                it.remove();
                SpecialItem.info("Duplicate " + si.name() + " removed!");
                continue;
            }
            if (si.loc() == null) continue;
            final World w = si.loc().w();
            if (w == null) continue;
            final Location loc = si.loc().center(w);
            if (isInPrivateWG(loc)) {
                final World sw = SpecialItem.SPAWN.w();
                if (sw != null) si.spawn(SpecialItem.SPAWN.center(sw), it.getItemStack());
                it.remove();
            } else si.apply(it);
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
                    SpecialItem.info("Uncrafted " + si.name() + " removed!");
                    continue;
                }
                if (!si.dropped()) {
                    it.remove();
                    SpecialItem.info("Undropped " + si.name() + " removed!");
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

    @EventHandler
    public void onCraft(final InventoryClickEvent e) {
        if (!(e.getClickedInventory() instanceof CraftingInventory)) return;
        if (e.getSlotType() != InventoryType.SlotType.RESULT) return;
        final ItemStack fin = e.getCurrentItem();
        final SpecialItem si = SpecialItem.get(fin);
        if (si == null) return;
        if (si.crafted()) {
            e.setResult(Event.Result.DENY);
            e.setCurrentItem(ItemUtil.air);
            for (final HumanEntity he : e.getViewers()) {
                he.sendMessage(TCUtil.form(Ostrov.PREFIX + "<red>Эта реликвия уже создана!"));
            }
            return;
        }
        si.obtain(e.getWhoClicked(), fin);
    }

    protected interface Processor extends GroupProc, SpecProc {}
    public interface GroupProc {
        void onGroup(final EquipmentSlot[] ess, final ItemGroup cm);
    }
    public interface SpecProc {
        void onSpec(final EquipmentSlot es, final SpecialItem si);
    }

    public static boolean isCustom(final ItemStack it) {
        return it != null && ((ItemGroup.exist && ItemGroup.get(it) != null)
            || (SpecialItem.exist && SpecialItem.get(it) != null));
    }
}
