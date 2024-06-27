package ru.komiss77.modules.entities;

import com.destroystokyo.paper.event.entity.WitchReadyPotionEvent;
import io.papermc.paper.event.entity.EntityFertilizeEggEvent;
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.AreaSpawner;
import ru.komiss77.modules.world.WXYZ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class EntityManager implements Initiable, Listener {

  public static boolean enable;
  public static BukkitTask spawnTask = null;
  protected static final HashMap<String, CustomEntity> custom = new HashMap<>();
  protected static final ArrayList<CustomEntity> spawns = new ArrayList<>();
  protected static final NamespacedKey key = NamespacedKey.minecraft("o.ent");
  protected static final PersistentDataType<String, CustomEntity> data = new PersistentDataType<>() {
    @Override
    public Class<String> getPrimitiveType() {
      return String.class;
    }

    @Override
    public Class<CustomEntity> getComplexType() {
      return CustomEntity.class;
    }

    @Override
    public String toPrimitive(final CustomEntity ce, final PersistentDataAdapterContext cont) {
      return ce.key.value();
    }

    @Override
    public CustomEntity fromPrimitive(final String nm, final PersistentDataAdapterContext cont) {
      return EntityManager.custom.get(nm);
    }
  };

  public EntityManager() {
		reload();
	}

  public static void register(final CustomEntity ce) {
    custom.put(ce.key.value(), ce);
    if (ce.spawner() != null) spawns.add(ce);
  }

  @Override
  public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
  }
    
	@Override
	public void reload() {
    if (spawnTask != null) spawnTask.cancel();
    HandlerList.unregisterAll(this);
    if (!enable) return;

    Ostrov.log_ok("§2Сущности включены!");
    Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
    spawnTask = new BukkitRunnable() {
      @Override
      public void run() {
        final Collection<? extends Player> pls = Bukkit.getOnlinePlayers();
        if (pls.isEmpty()) return;
        final ArrayList<WXYZ> locs = new ArrayList<>(pls.size());
        for (final Player p : pls) locs.add(new WXYZ(p.getLocation()));

        for (final CustomEntity ce : spawns) {
          final AreaSpawner as = ce.spawner();
          if (as == null || ce.cd < 0) continue;
          if (ce.cd == 0) {
            ce.cd = ce.spawnCd();
            for (final WXYZ lc : locs) {
              as.trySpawn(lc, ce.getEntClass());
            }
            continue;
          }
          ce.cd--;
        }
      }
    }.runTaskTimer(Ostrov.instance, 1, 1);
	}
	
	@Override
	public void onDisable() {
    if (!enable) return;
    Ostrov.log_ok("§6Сущности выключены!");
	}

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onSpawn(final CreatureSpawnEvent e) {
    final Entity ent = e.getEntity();
    final CustomEntity he = ent.getPersistentDataContainer()
      .get(key, data);
    if (he == null) {
      for (final CustomEntity ce : custom.values()) {
        if (ent.getClass().isAssignableFrom(ce.getEntClass())
          && ce.canBe(ent, e.getSpawnReason())) ce.apply(ent);
      }
    }
  }
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onDamage(final EntityDamageEvent e) {
    final CustomEntity he = e.getEntity().getPersistentDataContainer()
      .get(key, data);
    if (he != null) he.onHurt(e);
    final Entity dmgr = e.getDamageSource().getCausingEntity();
    if (dmgr != null && e instanceof EntityDamageByEntityEvent) {
      final CustomEntity de = dmgr.getPersistentDataContainer()
        .get(key, data);
      if (de != null) de.onAttack((EntityDamageByEntityEvent) e);
    }
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onDeath(final EntityDeathEvent e) {
    final CustomEntity he = e.getEntity().getPersistentDataContainer()
      .get(key, data);
    if (he != null) he.onDeath(e);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onTarget(final EntityTargetEvent e) {
    final CustomEntity he = e.getEntity().getPersistentDataContainer()
      .get(key, data);
    if (he != null) he.onTarget(e);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onShoot(final ProjectileLaunchEvent e) {
    final CustomEntity he = e.getEntity().getPersistentDataContainer()
      .get(key, data);
    if (he != null) he.onShoot(e);
	}

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPot(final EntityPotionEffectEvent e) {
    final CustomEntity he = e.getEntity().getPersistentDataContainer()
      .get(key, data);
    if (he != null) he.onPot(e);
  }



  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onExtra(final EntityExplodeEvent e) {extraEvent(e);}

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onExtra(final EntityLoadCrossbowEvent e) {extraEvent(e);}

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onExtra(final EntitySpellCastEvent e) {extraEvent(e);}

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onExtra(final EntityBreedEvent e) {extraEvent(e);}

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onExtra(final EntityFertilizeEggEvent e) {extraEvent(e);}

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onExtra(final EntityPickupItemEvent e) {extraEvent(e);}

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onExtra(final EntityTransformEvent e) {extraEvent(e);}

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onExtra(final PiglinBarterEvent e) {extraEvent(e);}

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onExtra(final ProjectileHitEvent e) {extraEvent(e);}

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onExtra(final VillagerAcquireTradeEvent e) {extraEvent(e);}

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onExtra(final WitchReadyPotionEvent e) {extraEvent(e);}

  private static void extraEvent(final EntityEvent e) {
    final CustomEntity he = e.getEntity().getPersistentDataContainer()
      .get(key, data);
    if (he != null) he.onExtra(e);
  }

  public static boolean isCustom(final Entity ent) {
    return ent.getPersistentDataContainer().has(key);
  }
}
