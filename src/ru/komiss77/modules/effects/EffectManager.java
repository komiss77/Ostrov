package ru.komiss77.modules.effects;

import java.util.*;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.Cfg;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.modules.effects.CustomEffect.Instance;

public class EffectManager implements Initiable, Listener {

    private static int tick;
    protected static BukkitTask task = null;
    protected static final Map<Key, CustomEffect<?>> VALUES = new HashMap<>();
    protected static final Map<LivingEntity, List<CustomEffect<?>.Instance>> insts = new WeakHashMap<>();

    public EffectManager() {
        reload();
    }

    public static void register(final CustomEffect<?> ce) {
        if (!Cfg.effects) return;
        VALUES.put(ce.key, ce);
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }

    @Override
    public void reload() {
        if (task != null) task.cancel();
        HandlerList.unregisterAll(this);
        if (!Cfg.effects) return;

        Ostrov.log_ok("§2Эффекты включены!");
        Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
        task = new BukkitRunnable() {
            @Override
            public void run() {
                final Iterator<Map.Entry<LivingEntity, List<CustomEffect<?>.Instance>>> it = insts.entrySet().iterator();
                while (it.hasNext()) {
                    final Map.Entry<LivingEntity, List<CustomEffect<?>.Instance>> en = it.next();
                    final LivingEntity tgt = en.getKey();
                    if (tgt == null || !tgt.isValid() || en.getValue().isEmpty()) {
                        it.remove(); continue;
                    }
                    en.getValue().removeIf(in -> process(tgt, in, in.effect().inst()));
                }
            }
        }.runTaskTimer(Ostrov.instance, 0, 0);
    }

    private static final int PART_TIME = 5;
    private static <I extends CustomEffect<I>.Instance> boolean process(final LivingEntity tgt, final Instance oin, final Class<I> inc) {
        if (!inc.isInstance(oin)) return true;
        final I in = inc.cast(oin);
        final CustomEffect<I> eff = in.effect();
        if (in.ticks() == 0 || eff.isDone(tgt, in)) {
            eff.onEnd(tgt, in);
            return true;
        }
        if (Timer.tickTime() % eff.period(tgt, in) == 0l) eff.affect(tgt, in);
        if (Timer.tickTime() % PART_TIME == 0l && in.vis) eff.animate(tgt, in);
        return false;
    }

    @Override
    public void onDisable() {
        if (!Cfg.effects) return;
        Ostrov.log_ok("§6Эффекты выключены!");
    }

    /*@EventHandler
    public void onLoad(final EntitiesLoadEvent e) {
        for (final Entity ent : e.getEntities()) {
            if (!(ent instanceof final LivingEntity le)) continue;
            final PersistentDataContainer pdc = le.getPersistentDataContainer();

            insts.remove(le);
            final List<Instance> ins = new ArrayList<>();
            for (final CustomEffect ce : effects.values()) {
                final Instance in = pdc.get(ce.key, ce.data);
                if (in == null) continue; ins.add(in);
            }
            if (ins.isEmpty()) continue;
            insts.put(le, ins);
        }
    }

    @EventHandler
    public void onUnload(final EntitiesUnloadEvent e) {
        for (final Entity ent : e.getEntities()) {
            if (!(ent instanceof final LivingEntity le)) continue;
            final PersistentDataContainer pdc = le.getPersistentDataContainer();
            final List<Instance> ins = insts.remove(le);
            if (ins.isEmpty()) continue;
            for (final Instance in : ins) {
                pdc.set(in.effect().key, in.effect().data, in);
            }
        }
    }*/
}
