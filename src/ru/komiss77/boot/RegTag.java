package ru.komiss77.boot;

import java.util.*;
import java.util.stream.Collectors;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.damage.DamageType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.komiss77.Ostrov;

public class RegTag<T extends Keyed> implements Tag<T> {

    public static final RegTag<DamageType> BYPASSES_WEAPON = RegTag.create(Key.key("bypasses_weapon"), DamageType.CACTUS,
        DamageType.CAMPFIRE, DamageType.DRY_OUT, DamageType.FALLING_ANVIL, DamageType.FALLING_STALACTITE, DamageType.HOT_FLOOR,
        DamageType.IN_FIRE, DamageType.LAVA, DamageType.LIGHTNING_BOLT, DamageType.SWEET_BERRY_BUSH, DamageType.CRAMMING,
        DamageType.DRAGON_BREATH, DamageType.DROWN, DamageType.ENDER_PEARL, DamageType.FALL, DamageType.FLY_INTO_WALL,
        DamageType.FREEZE, DamageType.GENERIC, DamageType.GENERIC_KILL, DamageType.IN_WALL, DamageType.INDIRECT_MAGIC,
        DamageType.MAGIC, DamageType.ON_FIRE, DamageType.OUT_OF_WORLD, DamageType.OUTSIDE_BORDER, DamageType.SONIC_BOOM,
        DamageType.STALAGMITE, DamageType.STARVE, DamageType.WITHER, DamageType.MACE_SMASH);

    public static final Map<Key, RegTag<? extends Keyed>> VALUES = new HashMap<>();

    private final TagKey<T> key;
    private final Set<TypedKey<T>> keys;

    private RegTag(final Key key, final RegistryKey<T> rk, final Collection<T> its) {
        this.key = TagKey.create(rk, key);
        this.keys = its.stream().map(i -> TypedKey.create(rk, i.key()))
            .collect(Collectors.toUnmodifiableSet());
        if (VALUES.put(key, this) != null) {
            Ostrov.log_warn("Tag " + key.key().value() + " is already registered!");
        }
    }

    public static <E extends Keyed> RegTag<E> create(final Key key, final Collection<E> its) {
        if (its.isEmpty()) throw new IllegalArgumentException("Tried to create empty tag " + key.value());
        final RegistryKey<E> rk = OStrap.regKeyOf(its.iterator().next());
        if (rk == null) throw new IllegalArgumentException("Cannot register type "
            + its.iterator().next().getClass().getSimpleName());
        return new RegTag<>(key, rk, its);
    }

    public static <E extends Keyed> RegTag<E> create(final Key key, final E... its) {
        if (its.length == 0) throw new IllegalArgumentException("Tried to create empty tag " + key.value());
        final RegistryKey<E> rk = OStrap.regKeyOf(its[0]);
        if (rk == null) throw new IllegalArgumentException("Cannot register tag " + key.value());
        return new RegTag<>(key, rk, Arrays.asList(its));
    }

    public @NonNull TagKey<T> tagKey() {return key;}
    public @NonNull RegistryKey<T> registryKey() {return key.registryKey();}
    public boolean contains(final @NonNull TypedKey<T> tk) {return keys.contains(tk);}
    public @NonNull @Unmodifiable Collection<TypedKey<T>> values() {return keys;}
    public @NonNull @Unmodifiable Collection<T> resolve(final @NonNull Registry<T> reg) {
        return keys.stream().map(t -> reg.get(t)).toList();
    }
    public Collection<TagEntry<T>> entries() {
        return keys.stream().map(t -> TagEntry.valueEntry(t)).toList();
    }
}
