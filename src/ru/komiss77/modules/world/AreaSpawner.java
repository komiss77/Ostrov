package ru.komiss77.modules.world;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class AreaSpawner {

    protected abstract int radius();

    protected abstract int near();

    protected abstract int offset();

    protected abstract LocFinder.MatCheck[] checks();

    public <E extends LivingEntity> List<E> trySpawn(final WXYZ from, final Class<E> entCls) {
        final WXYZ loc = LocFinder.findInArea(from, radius(), near(), checks(), offset());
        if (loc == null) return List.of();
        final SpawnCondition sc = getCondition(loc, entCls);
        if (sc == null) return List.of();
        final ArrayList<E> els = new ArrayList<>(sc.amt);
        for (int i = 0; i != sc.amt; i++) {
            els.add(loc.w.spawn(loc.getCenterLoc(), entCls, CreatureSpawnEvent.SpawnReason.NATURAL, false, e -> {
            }));
        }
        return els;
    }

    protected abstract <E extends LivingEntity> SpawnCondition getCondition(final WXYZ loc, final Class<E> entCls);

    public static final SpawnCondition NONE = new SpawnCondition(0, CreatureSpawnEvent.SpawnReason.DEFAULT);

    public record SpawnCondition(int amt, CreatureSpawnEvent.SpawnReason reason) {
    }
}
