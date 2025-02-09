package ru.komiss77.modules.world;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import ru.komiss77.Ostrov;


public abstract class AreaSpawner {

    private static final int NEAR = 3;

    protected abstract int radius();

    protected abstract int offset();

    protected abstract int yDst();

    protected abstract LocFinder.Check[] checks();

    public <E extends LivingEntity> List<E> trySpawn(final BVec from, final Class<E> entCls, final @Nullable Consumer<E> pre) {
        final BVec loc = LocFinder.findInArea(from, radius(), offset(), NEAR, checks(), yDst());
        if (loc == null) return List.of();
        final SpawnCondition sc = condition(loc);
        if (sc == null || sc.amt < 1) return List.of();
        final ArrayList<E> els = new ArrayList<>(sc.amt);
        final World w = loc.w();
        if (w == null) return els;
        for (int i = Ostrov.random.nextInt(sc.amt) + 1; i != 0; i--) {
            els.add(w.spawn(loc.center(w).add(shift(), 0d, shift()),
                entCls, sc.reason, false, pre));
        }
        return els;
    }

    private static final double MAX_SHF = 0.4d;
    private static final double MAX_SHF_D2 = MAX_SHF * 0.5d;
    private double shift() {
        return Ostrov.random.nextFloat() * MAX_SHF - MAX_SHF_D2;
    }

    public abstract <E extends LivingEntity> SpawnCondition condition(final BVec loc);

    public static final SpawnCondition NONE = new SpawnCondition(0, CreatureSpawnEvent.SpawnReason.DEFAULT);
    public static final SpawnCondition DEFAULT = new SpawnCondition(1, CreatureSpawnEvent.SpawnReason.NATURAL);

    public record SpawnCondition(int amt, CreatureSpawnEvent.SpawnReason reason) {}
}
