package ru.komiss77.modules.enchants;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;


public class EnchantManager implements Initiable, Listener {

    protected static class Data {
      public final Map<CustomEnchant, Integer> enchs = new HashMap<>();
    }

    protected static final int BASE_COST = 12;
    protected static final char sep_lvl = '=';
    protected static final String sep_ench = ":";
    protected static final NamespacedKey key = NamespacedKey.minecraft("o.ench");
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
  };

  public static final Map<Entity, ItemStack> projWeapons = new WeakHashMap<>();

  public EnchantManager() {
      reload();
  }

  @Override
  public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
  }

  @Override
  public void reload() {
    HandlerList.unregisterAll(this);
    if (!Config.enchants) return;

    Ostrov.log_ok("§2Зачарования включены!");
    Bukkit.getPluginManager().registerEvents(this, Ostrov.instance);
  }

  @Override
  public void onDisable() {
    if (!Config.enchants) return;
    Ostrov.log_ok("§6Зачарования выключены!");
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onDamage (final EntityDamageEvent e) {
    if (e instanceof final EntityDamageByEntityEvent ee && ee.getDamager() instanceof LivingEntity) {
        final EntityEquipment eq = ((LivingEntity) ee.getDamager()).getEquipment();
        final ItemStack it = eq.getItemInMainHand();
        if (!ItemUtils.isBlank(it, true)) {
          final EnchantManager.Data eds = it.getItemMeta().getPersistentDataContainer()
            .get(EnchantManager.key, EnchantManager.data);
          if (eds == null || eds.enchs.isEmpty()) return;
          for (final CustomEnchant en : eds.enchs.keySet()) {
            final int ch = en.getChance(it);
            if (ch > 0 && Ostrov.random.nextInt(ch) == 0) en.getOnHit(ee);
          }
        }

    }

    if (e.getEntity() instanceof LivingEntity) {
      final EntityEquipment eq = ((LivingEntity) e.getEntity()).getEquipment();
      final HashSet<CustomEnchant> active = new HashSet<>();
      for (final ItemStack it : eq.getArmorContents()) {
        if (!ItemUtils.isBlank(it, true)) {
          final EnchantManager.Data eds = it.getItemMeta().getPersistentDataContainer()
            .get(EnchantManager.key, EnchantManager.data);
          if (eds == null || eds.enchs.isEmpty()) return;
          for (final CustomEnchant en : eds.enchs.keySet()) {
            final int ch = en.getChance(it);
            if (ch > 0 && Ostrov.random.nextInt(ch) == 0) active.add(en);
          }
        }
      }

      for (final CustomEnchant ce : active) {
        ce.getOnArm(e);
      }
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onProj (final ProjectileHitEvent e) {
    if (e.getHitEntity() != null && e.getEntity().getShooter() instanceof LivingEntity) {
      final ItemStack it = projWeapons.get(e.getEntity());
      if (!ItemUtils.isBlank(it, true)) {
        final EnchantManager.Data eds = it.getItemMeta().getPersistentDataContainer()
          .get(EnchantManager.key, EnchantManager.data);
        if (eds == null || eds.enchs.isEmpty()) return;
        for (final CustomEnchant en : eds.enchs.keySet()) {
          final int ch = en.getChance(it);
          if (ch > 0 && Ostrov.random.nextInt(ch) == 0) en.getOnPrj(e);
        }
      }
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onShoot (final EntityShootBowEvent e) {
    final ItemStack it = e.getBow();
    if (!ItemUtils.isBlank(it, true)) {
      projWeapons.put(e.getProjectile(), it);
      Ostrov.async(() -> projWeapons.remove(e.getProjectile()), 200);
      final EnchantManager.Data eds = it.getItemMeta().getPersistentDataContainer()
        .get(EnchantManager.key, EnchantManager.data);
      if (eds == null || eds.enchs.isEmpty()) return;
      for (final CustomEnchant en : eds.enchs.keySet()) {
        final int ch = en.getChance(it);
        if (ch > 0 && Ostrov.random.nextInt(ch) == 0) en.getOnSht(e);
      }
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onBreak (final BlockBreakEvent e) {
    final Player p = e.getPlayer();
    final ItemStack it = p.getInventory().getItemInMainHand();
    if (!ItemUtils.isBlank(it, true)) {
      final EnchantManager.Data eds = it.getItemMeta().getPersistentDataContainer()
        .get(EnchantManager.key, EnchantManager.data);
      if (eds == null || eds.enchs.isEmpty()) return;
      for (final CustomEnchant en : eds.enchs.keySet()) {
        final int ch = en.getChance(it);
        if (ch > 0 && Ostrov.random.nextInt(ch) == 0) en.getOnBrk(e);
      }
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  public void onInt (final PlayerInteractEvent e) {
    final Player p = e.getPlayer();
    final ItemStack it = p.getInventory().getItemInMainHand();
    if (!ItemUtils.isBlank(it, true)) {
      final EnchantManager.Data eds = it.getItemMeta().getPersistentDataContainer()
        .get(EnchantManager.key, EnchantManager.data);
      if (eds == null || eds.enchs.isEmpty()) return;
      for (final CustomEnchant en : eds.enchs.keySet()) {
        final int ch = en.getChance(it);
        if (ch > 0 && Ostrov.random.nextInt(ch) == 0) en.getOnInt(e);
      }
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
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
        im.displayName(TCUtils.format(TCUtils.toString(im.displayName()).replace('&', '§')));
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
  }
}
