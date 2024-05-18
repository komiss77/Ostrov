package ru.komiss77.modules.entities;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.*;
import ru.komiss77.modules.world.AreaSpawner;

import javax.annotation.Nullable;

public abstract class CustomEntity {

    protected int cd = spawnCd();

    protected CustomEntity() {
        if (EntityManager.enable)
            EntityManager.register(this);
    }

    protected abstract String id();

    protected abstract @Nullable AreaSpawner spawner();

    protected abstract Class<? extends LivingEntity> getEntClass();

    protected abstract int spawnCd();

//  @OverrideMe
//  protected abstract void goal(final E e);

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
}
