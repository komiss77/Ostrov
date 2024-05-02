package ru.komiss77.modules.bots;

import org.bukkit.Bukkit;
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

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;


public class BotManager implements Initiable, Listener {

    public static boolean enable;
    public static final IntHashMap<BotEntity> botById;
    protected static final CaseInsensitiveMap<BotEntity> botByName;
    protected static final CaseInsensitiveMap<String[]> skin;
    //protected static final HashMap<String, String> skinSignatures;
 
    static {
        botById = new IntHashMap<>();
        botByName = new CaseInsensitiveMap<>();
        skin = new CaseInsensitiveMap<>();
        //skinSignatures = new HashMap<>();
    }
    
    
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
                final ArrayList<BotEntity> rbs = new ArrayList<>();
                for (final IntHashMap.Entry<BotEntity> en : botById.entrySet()) {
                    final BotEntity be = en.getValue();
                    if (!be.isDead()) {
                        final LivingEntity le = be.getEntity();
                        if (le == null || !le.isValid() || le.getEntityId() != en.getKey()) {
                            rbs.add(be);
                        }
                    }
                }

                if (!rbs.isEmpty()) {
                    Ostrov.sync(() -> {
                        for (final BotEntity be : rbs) {
                            Ostrov.log(be.name() + " bugged out");
                            be.onBug();
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
            for (final BotEntity be : botByName.values()) {
                if (be.world.getUID().equals(id)) {
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
            for (final BotEntity be : botByName.values()) {
                if (be.world.getUID().equals(id)) {
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
        final BotEntity be = botById.get(e.getRightClicked().getEntityId());
        if (be != null) {
            be.onInteract(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent e) {
        final BotEntity be = botById.get(e.getEntity().getEntityId());
        if (be != null) {
            be.onDamage(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDeath(final EntityDeathEvent e) {
        final BotEntity be = botById.get(e.getEntity().getEntityId());
        if (be != null) {
            be.onDeath(e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onTrans(final EntityTransformEvent e) {
        if (e.getEntity() instanceof final LivingEntity le && isBot(le)) {
            e.setCancelled(true);
        }
    }
    
    
    
    
    
    
    
    
    
    @Nullable
    public static <B extends BotEntity> B createBot(final String name, final Class<B> botClass, final Function<String, B> creator) {
        if (!enable) {
            Ostrov.log_warn("BotManager Tried creating a Bot while the module is off!");
            return null;
        }

        final BotEntity be = botByName.get(name);
        try {
            return be != null && be.getClass().isAssignableFrom(botClass) ? botClass.cast(be) : creator.apply(name);
        } catch (IllegalArgumentException | SecurityException ex) {
            Ostrov.log_err("BotManager createBot : "+ex.getMessage());
            //e.printStackTrace();
            return null;
        }
    }
    @Nullable
    @Deprecated
    public static <B extends BotEntity> B createBot(final String name, final Class<B> botClass, final Supplier<B> onCreate) {
        return createBot(name, botClass, nm -> onCreate.get());
    }
    
    public static void clearBots() {
        final Set<BotEntity> en = new HashSet<>(botById.values());
        for (final BotEntity bt : en) {
            bt.remove();
        }
    }

    public static boolean isBot(final LivingEntity le) {
        return botById.containsKey(le.getEntityId());
    }

    @Nullable
    public static <B extends BotEntity> B getBot(final int rid, final Class<B> cls) {
        final BotEntity be = botById.get(rid);
        return be == null ? null : cls.cast(be);
    }

    @Nullable
    public static <B extends BotEntity> B getBot(final String name, final Class<B> cls) {
        final BotEntity be = botByName.get(name);
        return be == null ? null : cls.cast(be);
    }

    public static void regSkin(final String name) {
        if (!enable) {
            Ostrov.log_warn("BotManager Tried setting skin while the module is off!");
            return;
        }
        Ostrov.async(() -> {
            try {
                final InputStreamReader irn = new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream());
                final String id = (String) ((JSONObject) new JSONParser().parse(irn)).get("id");

                final InputStreamReader tsr = new InputStreamReader(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + id + "?unsigned=false").openStream());
                final JSONObject ppt = ((JSONObject) ((JSONArray) ((JSONObject) new JSONParser().parse(tsr)).get("properties")).get(0));
                skin.put(name, new String []{ (String) ppt.get("value"), (String) ppt.get("signature")} );
            } catch (NullPointerException | IOException | ParseException e) {}
        });
    }
}
