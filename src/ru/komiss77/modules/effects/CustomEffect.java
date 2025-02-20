package ru.komiss77.modules.effects;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import com.destroystokyo.paper.ParticleBuilder;
import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import ru.komiss77.Cfg;
import ru.komiss77.Timer;
import ru.komiss77.boot.OStrap;
import ru.komiss77.utils.EntityUtil;


public abstract class CustomEffect<I extends CustomEffect<I>.Instance> implements Keyed {

//    private static final String SPLIT = StringUtil.SPLIT_1;

    /*protected final PersistentDataType<String, Instance> data = new PersistentDataType<>() {
        public Class<String> getPrimitiveType() {
            return String.class;
        }
        public Class<Instance> getComplexType() {
            return Instance.class;
        }
        public String toPrimitive(final Instance in, final PersistentDataAdapterContext cont) {
            return Math.min(in.end - Timer.tickTime(), 0l) + SPLIT + in.pow + SPLIT + in.vis;
        }
        public Instance fromPrimitive(final String str, final PersistentDataAdapterContext cont) {
            final String[] parts = str.split(SPLIT);
            if (parts.length != 3) return new Instance(0, 0d, false);
            return new Instance(NumUtil.intOf(parts[0], 0),
                NumUtil.doubleOf(parts[1], 0d), Boolean.parseBoolean(parts[2]));
        }
    };*/

    protected final NamespacedKey key;

    private static int Effect_ID = 0;
    private final int id = Effect_ID++;

    protected CustomEffect() {
        key = OStrap.key(this.getClass().getSimpleName());
        if (Cfg.effects) EffectManager.register(this);
    }

    protected abstract int period(final LivingEntity tgt, final I in);

    protected abstract void affect(final LivingEntity tgt, final I in);

    protected abstract Color color();

    public abstract Class<I> inst();

    protected void animate(final LivingEntity tgt, final I in) {
        final double hd2 = tgt.getHeight() * 0.55d;
        final double wd2 = tgt.getWidth() * 0.6d;
        final Location loc = EntityUtil.center(tgt);
        new ParticleBuilder(Particle.ENTITY_EFFECT).location(loc).count((int) (hd2 * wd2 * 10d))
            .offset(wd2, hd2, wd2).extra(0.01d).data(color()).receivers(40).spawn();
    }

    protected boolean isDone(final LivingEntity tgt, final I in) {return false;}

    protected void onStart(final LivingEntity tgt, final I in) {
//        tgt.getPersistentDataContainer().set(key, data, in);
    }
    protected void onEnd(final LivingEntity tgt, final I in) {
//        tgt.getPersistentDataContainer().remove(key);
    }

    public void apply(final LivingEntity tgt, final I in) {
        final I last = get(tgt);
        final I nin = last == null ? in : in.merge(last);
        final List<CustomEffect<?>.Instance> ins = EffectManager.insts
            .computeIfAbsent(tgt, t -> new ArrayList<>());
        ins.removeIf(i -> i.effect().equals(this)); ins.add(nin);
        onStart(tgt, nin);
    }

    public boolean isOn(final LivingEntity ent) {
        final Instance in = get(ent);
        return in != null && in.ticks() != 0;
    }

    @Nullable
    public I get(final LivingEntity ent) {
        final List<CustomEffect<?>.Instance> ins = EffectManager.insts.get(ent);
        if (ins == null) return null;
        final Class<I> inst = inst();
        for (final CustomEffect<?>.Instance in : ins) {
            if (inst.isInstance(in)) return inst.cast(in);
        }
        return null;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        return o instanceof final CustomEffect<?> ce
            && key.equals(ce.key);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return key.toString();
    }

    @Nullable
    public static CustomEffect<?> get(final Key key) {
        return EffectManager.VALUES.get(key);
    }

    public abstract class Instance {

        protected final double pow;
        protected final long end;
        protected final boolean vis;

        protected Instance(final int ticks, final double power, final boolean visible) {
            end = Timer.tickTime() + ticks;
            pow = power;
            vis = visible;
        }

        public double power() {
            return pow;
        }

        public boolean vis() {
            return vis;
        }

        public int ticks() {
            return Math.max(0, (int) (end - Timer.tickTime()));
        }

        public abstract I merge(final I in);

        public abstract CustomEffect<I> effect();
    }

}