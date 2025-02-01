package ru.komiss77.modules.entities;

import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.*;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import ru.komiss77.Cfg;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.AreaSpawner;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.notes.OverrideMe;


public abstract class CustomEntity implements Keyed {

    protected static final NamespacedKey KEY = NamespacedKey.minecraft("o.ent");
    protected static final PersistentDataType<String, CustomEntity> DATA = new PersistentDataType<>() {
        @Override
        public Class<String> getPrimitiveType() {
            return String.class;
        }

        @Override
        public Class<CustomEntity> getComplexType() {
            return CustomEntity.class;
        }

        @Override
        public String toPrimitive(final CustomEntity ce, final PersistentDataAdapterContext cont) {
            return ce.key.value();
        }

        @Override
        public CustomEntity fromPrimitive(final String nm, final PersistentDataAdapterContext cont) {
            return EntityManager.custom.get(nm);
        }
    };

    protected int cd;

    protected final NamespacedKey key;

    protected CustomEntity() {
        key = new NamespacedKey(Ostrov.instance, this.getClass().getSimpleName());
        if (Cfg.entities) EntityManager.register(this);

        cd = spawnCd();
    }

    protected abstract AreaSpawner spawner();

    protected abstract Class<? extends LivingEntity> getEntClass();

    protected abstract int spawnCd();

//  @OverrideMe
//  protected abstract void goal(final E e);

    public LivingEntity spawn(final Location loc) {
        final AreaSpawner.SpawnCondition cnd = spawner().condition(new WXYZ(loc));
        return loc.getWorld().spawn(loc, getEntClass(), cnd.reason(), false, this::apply);
    }

    public void apply(final Entity ent) {
        ent.getPersistentDataContainer().set(KEY, DATA, this);
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

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        return o instanceof final CustomEntity ce && key.equals(ce.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return key.toString() + " of " + getEntClass().getSimpleName().toLowerCase();
    }

    @OverrideMe
    public static CustomEntity get(final Entity ent) {
        return ent.getPersistentDataContainer().get(KEY, DATA);
    }
}