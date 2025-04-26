package ru.komiss77.modules.enchants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.event.WritableRegistry;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import ru.komiss77.Cfg;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.boot.OStrap;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;

public class EnchantManager implements Initiable, Listener {

    /*protected static class Data {
      public final Map<CustomEnchant, Integer> enchs = new HashMap<>();
    }*/

//    protected static final int BASE_COST = 12;
//    protected static final char sep_lvl = '=';
//    protected static final String sep_ench = ":";
    /*protected static final NamespacedKey key = NamespacedKey.minecraft("o.ench");
    protected static final PersistentDataType<String, Data> data = new PersistentDataType<>() {

    @Override
    public Class<String> getPrimitiveType() {
      return String.class;
    }

    @Override
    public Class<Data> getComplexType() {
      return Data.class;
    }

    @Override
    public String toPrimitive(final Data cds, final PersistentDataAdapterContext cont) {
      final StringBuilder sb = new StringBuilder();
      for (final Map.Entry<CustomEnchant, Integer> en : cds.enchs.entrySet())
        sb.append(sep_ench).append(en.getKey().key().value()).append(sep_lvl).append(en.getValue());
      return sb.isEmpty() ? "" : sb.substring(1);
    }

    @Override
    public Data fromPrimitive(final String data, final PersistentDataAdapterContext cont) {
      final String[] sds = data.split(sep_ench);
      final Data cds = new Data();
      for (int i = 0; i != sds.length; i++) {
        final String es = sds[i];
        final int sep = es.indexOf(sep_lvl);
        if (sep < 1) continue;
        final CustomEnchant ce = CustomEnchant.getByKey(NamespacedKey.minecraft(es.substring(0, sep)));
        if (ce == null) continue;
        final int lvl = ApiOstrov.getInteger(es.substring(sep + 1), 0);
        if (lvl < 1) continue;
        cds.enchs.put(ce, lvl);
      }
      return cds;
    }
  };*/

    private static final RegistryKeySet<ItemType>
        noIts = RegistrySet.keySet(RegistryKey.ITEM);
    public static void register() {
        OStrap.strap(mgr -> mgr.registerEventHandler(RegistryEvents.ENCHANTMENT
            .freeze().newHandler(e -> {
                final WritableRegistry<Enchantment, EnchantmentRegistryEntry.Builder> rg = e.registry();
                for (final CustomEnchant ce : CustomEnchant.VALUES.values()) {
                    if (ce.isReg()) continue;
                    rg.register(TypedKey.create(RegistryKey.ENCHANTMENT, ce.getKey()),
                        b -> b.description(TCUtil.form(ce.name()))
                            .primaryItems(ce.isInTable() ? ce.targets() : noIts)
                            .supportedItems(ce.isInTable() ? noIts : ce.targets())
                            .anvilCost(ce.anvilCost())
                            .maxLevel(ce.maxLevel())
                            .weight(ce.weight())
                            .exclusiveWith(ce.conflicts())
                            .minimumCost(ce.minCost())
                            .maximumCost(ce.maxCost())
                            .activeSlots(ce.slots()));
                    ce.setReg();
                }
            })));
    }

    public EnchantManager() {
        reload();
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }

    @Override
    public void reload() {
        HandlerList.unregisterAll(this);
        if (!Cfg.enchants) return;

        Ostrov.log_ok("§2Зачарования включены!");
        Bukkit.getPluginManager().registerEvents(this, Ostrov.instance);
    }

    @Override
    public void onDisable() {
        if (!Cfg.enchants) return;
        Ostrov.log_ok("§6Зачарования выключены!");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onDamage(final EntityDamageEvent e) {
        if (e instanceof final EntityDamageByEntityEvent ee
            && ee.getDamager() instanceof LivingEntity) {
            enchAct((LivingEntity) ee.getDamager(), ee);
        }

        if (e.getEntity() instanceof LivingEntity) {
            enchAct((LivingEntity) e.getEntity(), e);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onProj(final ProjectileHitEvent e) {
        if (e.getEntity().getShooter() instanceof final LivingEntity le) {
            enchAct(le, e);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onShoot(final EntityShootBowEvent e) {
        enchAct(e.getEntity(), e);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBreak(final BlockBreakEvent e) {
        enchAct(e.getPlayer(), e);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onInt(final PlayerInteractEvent e) {
        enchAct(e.getPlayer(), e);
    }

  /*@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onGrind (final PrepareGrindstoneEvent e) {
    final ItemStack it = e.getResult();
    if (!ItemUtils.isBlank(it, true)) {
      final EnchantManager.Data eds = it.getItemMeta().getPersistentDataContainer()
        .get(EnchantManager.key, EnchantManager.data);
      if (eds == null || eds.enchs.isEmpty()) return;
      for (final CustomEnchant en : eds.enchs.keySet()) en.remove(it);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onAnvil (final PrepareAnvilEvent e) {
    final ItemStack it = e.getResult();
    if (!ItemUtils.isBlank(it, false)) {
      final ItemMeta im = it.getItemMeta();
      if (im.hasDisplayName()) {
        im.displayName(TCUtils.form(TCUtils.deform(im.displayName()).replace('&', '§')));
      }
      if (im instanceof Repairable && ((Repairable) im).hasRepairCost()) {
        ((Repairable) im).setRepairCost(0);
      }

      it.setItemMeta(im);
      e.setResult(it);
      final ItemStack fst = e.getInventory().getFirstItem();
      final Map<CustomEnchant, Integer> fens;
      if (fst.hasItemMeta()) {
        final Data fdt = fst.getItemMeta()
          .getPersistentDataContainer().get(key, data);
        fens = fdt == null ? Map.of() : fdt.enchs;
      } else fens = Map.of();

      final ItemStack scd = e.getInventory().getSecondItem();
      if (ItemUtils.isBlank(scd, true)) return;
      final Data sdt = scd.getItemMeta().getPersistentDataContainer().get(key, data);
      final Map<CustomEnchant, Integer> sens = sdt == null ? Map.of() : sdt.enchs;

      final AnvilInventory ainv = e.getInventory();
      ainv.setMaximumRepairCost(Integer.MAX_VALUE);
      int cost = ainv.getRepairCost();
      final HashMap<CustomEnchant, Integer> enchs = new HashMap<>();
      for (final Map.Entry<CustomEnchant, Integer> en : fens.entrySet()) {
        final CustomEnchant ce = en.getKey();
        ce.level(im, 0, false);
        final Integer i = sens.get(en.getKey());
        if (i == null) {
          enchs.put(en.getKey(), en.getValue());
        } else {
          final int mxLvl = en.getKey().getMaxLevel();
          final int lvl;
          if (i.equals(en.getValue())) {
            lvl = Math.min(mxLvl, i + 1);
            cost += BASE_COST * lvl / (lvl + mxLvl);
            enchs.put(en.getKey(), lvl);
          } else if (i < en.getValue()) {
            lvl = en.getValue();
            cost += BASE_COST * lvl / (lvl + mxLvl);
            enchs.put(en.getKey(), lvl);
          } else {
            cost += BASE_COST * i / (i + mxLvl);
            enchs.put(en.getKey(), i);
          }
        }
      }

      final Map<Enchantment, Integer> finMap = im instanceof EnchantmentStorageMeta
        ? ((EnchantmentStorageMeta) im).getStoredEnchants() : im.getEnchants();

      if (!sens.isEmpty()) {
        final boolean check = fst.getType() != Material.ENCHANTED_BOOK && scd.getType() == Material.ENCHANTED_BOOK
          && (ainv.getViewers().isEmpty() || !ApiOstrov.isLocalBuilder(ainv.getViewers().get(0)));
        for (final Map.Entry<CustomEnchant, Integer> en : sens.entrySet()) {
          final CustomEnchant set = en.getKey();
          if (check && !set.canEnchantItem(it)) continue;
          if (!enchs.containsKey(set)) {
            boolean can = true;
            for (final CustomEnchant oe : enchs.keySet()) {
              if (set.conflictsWith(oe)) {
                can = false; break;
              }
            }
            for (final Enchantment oe : finMap.keySet()) {
              if (set.conflictsWith(oe)) {
                can = false; break;
              }
            }
            if (can) {
              final int lvl = en.getValue();
              cost += BASE_COST * lvl / (lvl + set.getMaxLevel());
              enchs.put(set, lvl);
            }
          }
        }
      }

      for (final Enchantment en : finMap.keySet()) {
        enchs.keySet().removeIf(ce -> ce.conflictsWith(en));
      }

      CustomEnchant.unmask(im);
      if (!enchs.isEmpty()) {
        for (final Map.Entry<CustomEnchant, Integer> en : enchs.entrySet()) {
          en.getKey().level(im, en.getValue(), false);
        }
      }
      ainv.setRepairCost(cost);
      it.setItemMeta(im);
    }

    e.setResult(it);
  }*/

    private void enchAct(final LivingEntity le, final Event e) {
        final Map<CustomEnchant, List<EnchData>> active = new HashMap<>();
        final EntityEquipment eq = le.getEquipment();
        for (final EquipmentSlot es : EquipmentSlot.values()) {
            if (!le.canUseEquipmentSlot(es) /*|| (le instanceof Player && switch (es) {
                case BODY, SADDLE -> true; default -> false;
            })*/) continue;
            final ItemStack it = eq.getItem(es);
            if (!ItemUtil.isBlank(it, true)) {

                for (final Map.Entry<Enchantment, Integer> en : it.getEnchantments().entrySet()) {
                    final Enchantment ench = en.getKey();
                    final CustomEnchant ce = CustomEnchant.get(ench.getKey());
                    if (ce == null) continue;

                    for (final EquipmentSlotGroup gr : ench.getActiveSlotGroups()) {
                        if (gr.test(es)) {
                            final List<EnchData> eds = active.get(ce);
                            if (eds == null) {
                                final List<EnchData> neds = new ArrayList<>();
                                neds.add(new EnchData(es, it, en.getValue()));
                                active.put(ce, neds);
                            } else {
                                eds.add(new EnchData(es, it, en.getValue()));
                            }
                            break;
                        }
                    }
                }
            }
        }

        for (final Map.Entry<CustomEnchant, List<EnchData>> en : active.entrySet()) {
            for (final EnchData ed : en.getKey().act(e, en.getValue())) {
                eq.setItem(ed.es(), ed.it(), true);
            }
        }
    }
}
