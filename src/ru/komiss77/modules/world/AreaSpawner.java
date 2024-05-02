package ru.komiss77.modules.world;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import ru.komiss77.Ostrov;

import java.util.ArrayList;
import java.util.List;

public abstract class AreaSpawner {

  private static final int NEAR = 3;

  protected abstract int radius();
  protected abstract int offset();
  protected abstract int yDst();

  protected abstract LocFinder.MatCheck[] checks();

  public <E extends LivingEntity> List<E> trySpawn(final WXYZ from, final Class<E> entCls) {
    final WXYZ loc = LocFinder.findInArea(from, radius(), offset(), NEAR, checks(), yDst());
    if (loc == null) return List.of();
    final SpawnCondition sc = getCondition(loc, entCls);
    if (sc == null || sc.amt < 1) return List.of();
    final ArrayList<E> els = new ArrayList<>(sc.amt);
    for (int i = Ostrov.random.nextInt(sc.amt) + 1; i != 0; i--) {
      els.add(loc.w.spawn(loc.getCenterLoc(), entCls, sc.reason, false, e -> {}));
    }
    return els;
  }

  public abstract <E extends LivingEntity> SpawnCondition getCondition(final WXYZ loc, final Class<E> entCls);

  public static final SpawnCondition NONE = new SpawnCondition(0, CreatureSpawnEvent.SpawnReason.DEFAULT);
  public static final SpawnCondition DEFAULT = new SpawnCondition(1, CreatureSpawnEvent.SpawnReason.NATURAL);
  public record SpawnCondition(int amt, CreatureSpawnEvent.SpawnReason reason) {}
}
