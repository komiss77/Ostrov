package ru.komiss77.modules.bots;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.Cfg;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.player.profile.Skins;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.objects.IntHashMap;


public class BotManager implements Initiable, Listener {

    public static final NamespacedKey KEY = OStrap.key("bot");
    public static final IntHashMap<Botter> botById = new IntHashMap<>();
    protected static final CaseInsensitiveMap<Botter> botByName = new CaseInsensitiveMap<>();

    public BotManager() {

        if (!Cfg.bots) {
            Ostrov.log_ok("§6Боты выключены!");
            return;
        }
        //for (final Player pl : Bukkit.getOnlinePlayers()) {
        //    injectPlayer(pl); //пакетный слушатель не удалять, он для всех модулей один!
        // }
        BotManager.this.reload();

        new BukkitRunnable() {
            @Override
            public void run() {
                final ArrayList<Botter> rbs = new ArrayList<>();
                try {
                    for (final IntHashMap.Entry<Botter> en : botById.entrySet()) {
                        final Botter be = en.getValue();
                        if (!be.isDead()) {
                            final LivingEntity le = be.getEntity();
                            if (le == null || !le.isValid() || le.getEntityId() != en.getKey()) {
                                rbs.add(be);
                            }
                        }
                    }
                } catch (ConcurrentModificationException ex) {}

                if (!rbs.isEmpty()) {
                    Ostrov.sync(() -> {
                        for (final Botter be : rbs) {
                            Ostrov.log(be.name() + " bugged out");
                            be.bug();
                        }
                    });
                }
            }
        }.runTaskTimerAsynchronously(Ostrov.instance, 100, 100);
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }

    @Override
    public void reload() {
        HandlerList.unregisterAll(this);
        if (!Cfg.bots) {
            Ostrov.log_ok("§6Боты выключены!");
            return;
        }

        clearBots();
        Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
        Ostrov.log_ok("§2Боты включены!");
    }

    @Override
    public void onDisable() {
        clearBots();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onData(final PlayerJoinEvent e) {
        final Player pl = e.getPlayer();
        //   injectPlayer(pl); //подкидывается в РМ.bungeeDataHandle
        final UUID id = pl.getWorld().getUID();
        Ostrov.async(() -> {
            for (final Botter be : botByName.values()) {
                if (be.world().getUID().equals(id)) {
                    be.updateAll(pl);
                }
            }
        }, 4);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onWorld(final PlayerChangedWorldEvent e) {
        final Player pl = e.getPlayer();
        //   injectPlayer(pl); //подкидывается в РМ.bungeeDataHandle
        final UUID nwID = pl.getWorld().getUID();
        final UUID oldID = e.getFrom().getUID();
        Ostrov.async(() -> {
            for (final Botter be : botByName.values()) {
                final UUID uid = be.world().getUID();
                if (uid.equals(nwID)) {
                    be.updateAll(pl);
                } else if (uid.equals(oldID)) {
                    be.removeAll(pl);
                }
            }
        }, 4);
    }

    // @EventHandler
    // public void onLeave(final PlayerQuitEvent e) {
    //     removePlayer(e.getPlayer());
    //  }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(final PlayerInteractAtEntityEvent e) {
        final Botter be = botById.get(e.getRightClicked().getEntityId());
        if (be != null) {
            be.interact(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent e) {
        final Botter be = botById.get(e.getEntity().getEntityId());
        if (be != null) {
            be.damage(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDeath(final EntityDeathEvent e) {
        final Botter be = botById.get(e.getEntity().getEntityId());
        if (be != null) {
            be.death(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onTrans(final EntityTransformEvent e) {
        if (e.getEntity() instanceof final LivingEntity le && isBot(le)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onTeleport(final EntityTeleportEvent e) {
        if (e.getEntity() instanceof final LivingEntity le) {
            final Botter be = botById.get(le.getEntityId());
            if (be != null) {
                final Location to = e.getTo();
                if (to == null) return;
                ((BotEntity) be).teleport(le, to);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onLoad(final WorldLoadEvent e) {
        for (final Entity ent : e.getWorld().getEntities()) {
            if (!(ent instanceof final LivingEntity le)) continue;
            if (ent.getType() != Botter.TYPE
                || !le.getPersistentDataContainer().has(KEY)) continue;
            final Botter be = botById.get(ent.getEntityId());
            if (be == null) ent.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEnts(final EntitiesLoadEvent e) {
        for (final Entity ent : e.getEntities()) {
            if (!(ent instanceof final LivingEntity le)) continue;
            if (ent.getType() != Botter.TYPE
                || !le.getPersistentDataContainer().has(KEY)) continue;
            final Botter be = botById.get(ent.getEntityId());
            if (be == null) ent.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onFish(final EntityMountEvent e) {
        if (e.getEntity() instanceof FishHook) {
            final Botter be = botById.get(e.getMount().getEntityId());
            if (be != null) Ostrov.log_ok("bot hooked");
        }
    }

    public static Botter createBot(final String name, final World w, final Botter.Extent ext) {
        if (!Cfg.bots) {
            Ostrov.log_warn("BotManager Tried creating a Bot while the module is off!");
            return null;
        }

        final Botter be = botByName.get(name);
        if (be != null) return be;
        return new BotEntity(name, w, ext);
        /*try {
            return be != null && be.getClass().isAssignableFrom(botClass) ? botClass.cast(be) : creator.apply(name);
        } catch (IllegalArgumentException | SecurityException ex) {
            Ostrov.log_err("BotManager createBot : " + ex.getMessage());
            //e.printStackTrace();
            return null;
        }*/
    }

    public static Botter createBot(final String name, final World w, final Function<Botter, Botter.Extent> exs) {
        if (!Cfg.bots) {
            Ostrov.log_warn("BotManager Tried creating a Bot while the module is off!");
            return null;
        }

        final Botter be = botByName.get(name);
        if (be != null) return be;
        return new BotEntity(name, w, exs);
    }

    public static void clearBots() {
        final Set<Botter> en = new HashSet<>(botById.values());
        for (final Botter bt : en) bt.remove();
        for (final World w : Bukkit.getWorlds()) {
            for (final Entity ent : w.getEntities()) {
                if (!(ent instanceof final LivingEntity le)) continue;
                if (ent.getType() != Botter.TYPE
                    || !le.getPersistentDataContainer().has(KEY)) continue;
                ent.remove();
            }
        }
    }

    public static boolean isBot(final LivingEntity le) {
        return botById.containsKey(le.getEntityId());
    }

    @Nullable
    public static Botter getBot(final int rid) {
        return botById.get(rid);
    }

    @Nullable
    public static Botter getBot(final String name) {
        return botByName.get(name);
    }

    @Deprecated
    public static void regSkin(final String name) {
        if (!Cfg.bots) {
            Ostrov.log_warn("BotManager Tried setting skin while the module is off!");
            return;
        }
        Skins.future(name);
    }

    public static GameProfile profile(final String name) {
        return Skins.game(Skins.present(name));
    }
}
