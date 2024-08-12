package ru.komiss77.modules.bots;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.objects.IntHashMap;


public class BotManager implements Initiable, Listener {

    public static boolean enable;
    public static final IntHashMap<Botter> botById = new IntHashMap<>();
    protected static final CaseInsensitiveMap<Botter> botByName = new CaseInsensitiveMap<>();
    protected static final CaseInsensitiveMap<String[]> skin = new CaseInsensitiveMap<>();

    public BotManager() {

        if (!enable) {
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
                for (final IntHashMap.Entry<Botter> en : botById.entrySet()) {
                    final Botter be = en.getValue();
                    if (!be.isDead()) {
                        final LivingEntity le = be.getEntity();
                        if (le == null || !le.isValid() || le.getEntityId() != en.getKey()) {
                            rbs.add(be);
                        }
                    }
                }

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
        if (!enable) {
            Ostrov.log_ok("§6Боты выключены!");
            return;
        }

        clearBots();
        Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
        Ostrov.log_ok("§2Боты включены!");
    }

    @Override
    public void onDisable() {
        if (enable) {
            clearBots();
        }
        //for (final Player p : Bukkit.getOnlinePlayers()) {
        //    removePlayer(p);
        // }
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
        final UUID id = pl.getWorld().getUID();
        Ostrov.async(() -> {
            for (final Botter be : botByName.values()) {
                if (be.world().getUID().equals(id)) {
                    be.updateAll(pl);
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

    public static Botter createBot(final String name, final World w, final Botter.Extent ext) {
        if (!enable) {
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
        if (!enable) {
            Ostrov.log_warn("BotManager Tried creating a Bot while the module is off!");
            return null;
        }

        final Botter be = botByName.get(name);
        if (be != null) return be;
        return new BotEntity(name, w, exs);
    }

    public static void clearBots() {
        final Set<Botter> en = new HashSet<>(botById.values());
        for (final Botter bt : en) {
            bt.remove();
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

    public static void regSkin(final String name) {
        if (!enable) {
            Ostrov.log_warn("BotManager Tried setting skin while the module is off!");
            return;
        }
        Ostrov.async(() -> {
            try {
                final InputStreamReader irn = new InputStreamReader(URI
                    .create("https://api.mojang.com/users/profiles/minecraft/" + name).toURL().openStream());
                final String id = (String) ((JSONObject) new JSONParser().parse(irn)).get("id");

                final InputStreamReader tsr = new InputStreamReader(URI
                    .create("https://sessionserver.mojang.com/session/minecraft/profile/" + id + "?unsigned=false").toURL().openStream());
                final JSONObject ppt = ((JSONObject) ((JSONArray) ((JSONObject) new JSONParser().parse(tsr)).get("properties")).get(0));
                skin.put(name, new String[]{(String) ppt.get("value"), (String) ppt.get("signature")});
            } catch (NullPointerException | IOException | ParseException e) {
            }
        });
    }
}
