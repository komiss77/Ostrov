package ru.komiss77.modules.entities;

import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.*;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.AreaSpawner;
import ru.komiss77.modules.world.WXYZ;

import javax.annotation.Nullable;

public abstract class CustomEntity implements Keyed {

  protected int cd = spawnCd();

  protected final NamespacedKey key;

  protected CustomEntity() {
    key = new NamespacedKey(Ostrov.instance, this.getClass().getSimpleName());
    if (EntityManager.enable)
      EntityManager.register(this);
  }

  protected abstract @Nullable AreaSpawner spawner();
  protected abstract Class<? extends LivingEntity> getEntClass();
  protected abstract int spawnCd();

//  @OverrideMe
//  protected abstract void goal(final E e);

  public LivingEntity spawn(final Location loc) {
    final AreaSpawner.SpawnCondition cnd = spawner().getCondition(new WXYZ(loc), getEntClass());
    return loc.getWorld().spawn(loc, getEntClass(), cnd.reason(), false, this::apply);
  }

  public void apply(final Entity ent) {
    ent.getPersistentDataContainer().set(EntityManager.key, EntityManager.data, this);
    modify(ent);
//    goal(e);
  }

  protected abstract boolean canBe(final Entity ent,
    final CreatureSpawnEvent.SpawnReason reason);
  protected abstract void modify(final Entity ent);

  protected abstract void onAttack(final EntityDamageByEntityEvent e);
  protected abstract void onHurt(final EntityDamageEvent e);
  protected abstract void onDeath(final EntityDeathEvent e);
  protected abstract void onTarget(final EntityTargetEvent e);
  protected abstract void onShoot(final ProjectileLaunchEvent e);
  protected abstract void onPot(final EntityPotionEffectEvent e);

  protected abstract void onExtra(final EntityEvent e);

  @Override
  public NamespacedKey getKey() {
    return key;
  }
}
