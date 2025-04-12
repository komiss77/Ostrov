package ru.komiss77.boot;

import java.util.*;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.DamageTypeKeys;
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

    public static final Map<Key, RegTag<? extends Keyed>> VALUES = new HashMap<>();

    public static final RegTag<DamageType> BYPASSES_WEAPON = RegTag.create(Key.key("bypasses_weapon"), DamageTypeKeys.CACTUS,
        DamageTypeKeys.CAMPFIRE, DamageTypeKeys.DRY_OUT, DamageTypeKeys.FALLING_ANVIL, DamageTypeKeys.FALLING_STALACTITE, DamageTypeKeys.HOT_FLOOR,
        DamageTypeKeys.IN_FIRE, DamageTypeKeys.LAVA, DamageTypeKeys.LIGHTNING_BOLT, DamageTypeKeys.SWEET_BERRY_BUSH, DamageTypeKeys.CRAMMING,
        DamageTypeKeys.DRAGON_BREATH, DamageTypeKeys.DROWN, DamageTypeKeys.ENDER_PEARL, DamageTypeKeys.FALL, DamageTypeKeys.FLY_INTO_WALL,
        DamageTypeKeys.FREEZE, DamageTypeKeys.GENERIC, DamageTypeKeys.GENERIC_KILL, DamageTypeKeys.IN_WALL, DamageTypeKeys.INDIRECT_MAGIC,
        DamageTypeKeys.MAGIC, DamageTypeKeys.ON_FIRE, DamageTypeKeys.OUT_OF_WORLD, DamageTypeKeys.OUTSIDE_BORDER, DamageTypeKeys.SONIC_BOOM,
        DamageTypeKeys.STALAGMITE, DamageTypeKeys.STARVE, DamageTypeKeys.WITHER, DamageTypeKeys.MACE_SMASH);

    private final TagKey<T> key;
    private final Set<TypedKey<T>> keys;

    private RegTag(final Key key, final RegistryKey<T> rk, final Collection<TypedKey<T>> its) {
        this.key = TagKey.create(rk, key);
        this.keys = new HashSet<>(its);
        if (VALUES.put(key, this) != null) {
            Ostrov.log_warn("Tag " + key.key().value() + " is already registered!");
        }
    }

    public static <E extends Keyed> RegTag<E> create(final Key key, final Collection<TypedKey<E>> its) {
        if (its.isEmpty()) throw new IllegalArgumentException("Tried to create empty tag " + key.value());
        return new RegTag<>(key, its.iterator().next().registryKey(), its);
    }

    public static <E extends Keyed> RegTag<E> create(final Key key, final TypedKey<E>... its) {
        if (its.length == 0) throw new IllegalArgumentException("Tried to create empty tag " + key.value());
        return new RegTag<>(key, its[0].registryKey(), Arrays.asList(its));
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
