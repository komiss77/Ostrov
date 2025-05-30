package ru.komiss77.modules.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.Cfg;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.BVec;

public class EntityManager implements Initiable, Listener {

    protected static BukkitTask task = null;
    protected static final HashMap<String, CustomEntity> custom = new HashMap<>();
    protected static final List<CustomEntity> spawns = new ArrayList<>();

    public EntityManager() {
        reload();
    }

    public static void register(final CustomEntity ce) {
        if (!Cfg.entities) return;
        custom.put(ce.key.value(), ce);
        if (ce.spawner() != null) spawns.add(ce);
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }

    @Override
    public void reload() {
        if (task != null) task.cancel();
        HandlerList.unregisterAll(this);
        if (!Cfg.entities) return;

        Ostrov.log_ok("§2Сущности включены!");
        Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
        task = new BukkitRunnable() {
            @Override
            public void run() {
                final Collection<? extends Player> pls = Bukkit.getOnlinePlayers();
                if (pls.isEmpty()) return;
                final ArrayList<BVec> locs = new ArrayList<>(pls.size());
                for (final Player p : pls) locs.add(BVec.of(p.getLocation()));

                for (final CustomEntity ce : spawns) {
                    if (ce.cd < 0) continue;
                    if (ce.cd == 0) {
                        ce.cd = ce.spawnCd();
                        for (final BVec lc : locs) {
                            ce.spawner().trySpawn(lc, ce.getEntClass(), ce::apply);
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
        if (!Cfg.entities) return;
        Ostrov.log_ok("§6Сущности выключены!");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(final CreatureSpawnEvent e) {
        final Entity ent = e.getEntity();
        final CustomEntity he = CustomEntity.get(ent);
        if (he != null) return;
        for (final CustomEntity ce : custom.values()) {
            if (ce.getEntClass().isInstance(ent)
                && ce.canBe(ent, e.getSpawnReason())) {
                ce.apply(ent);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent e) {
        final CustomEntity he = CustomEntity.get(e.getEntity());
        if (he != null) he.onHurt(e);
        final Entity dmgr = e.getDamageSource().getCausingEntity();
        if (dmgr != null && e instanceof EntityDamageByEntityEvent) {
            final CustomEntity de = CustomEntity.get(dmgr);
            if (de != null) de.onAttack((EntityDamageByEntityEvent) e);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(final EntityDeathEvent e) {
        final CustomEntity he = CustomEntity.get(e.getEntity());
        if (he != null) he.onDeath(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTarget(final EntityTargetEvent e) {
        final CustomEntity he = CustomEntity.get(e.getEntity());
        if (he != null) he.onTarget(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShoot(final ProjectileLaunchEvent e) {
        final CustomEntity he = CustomEntity.get(e.getEntity());
        if (he != null) he.onShoot(e);
    }

    /*@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExtra(final ProjectileHitEvent e) {
        if (e.getEntity().getShooter() instanceof final Mob mb) {
            final CustomEntity he = CustomEntity.get(mb);
            if (he != null) he.onExtra(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPot(final EntityPotionEffectEvent e) {
        extraEvent(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExtra(final EntityExplodeEvent e) {
        extraEvent(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExtra(final EntityLoadCrossbowEvent e) {
        extraEvent(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExtra(final EntitySpellCastEvent e) {
        extraEvent(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExtra(final EntityBreedEvent e) {
        extraEvent(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExtra(final EntityFertilizeEggEvent e) {
        extraEvent(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExtra(final EntityPickupItemEvent e) {
        extraEvent(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExtra(final EntityTransformEvent e) {
        extraEvent(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExtra(final PiglinBarterEvent e) {
        extraEvent(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExtra(final VillagerAcquireTradeEvent e) {
        extraEvent(e);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExtra(final WitchReadyPotionEvent e) {
        extraEvent(e);
    }

    private static void extraEvent(final EntityEvent e) {
        final CustomEntity he = CustomEntity.get(e.getEntity());
        if (he != null) he.onExtra(e);
    }*/
}
