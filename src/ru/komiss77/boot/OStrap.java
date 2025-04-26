package ru.komiss77.boot;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.PreFlattenTagRegistrar;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockType;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import ru.komiss77.Ostrov;


public class OStrap implements PluginBootstrap {

    public static final String space = "ostrov";

    public static NamespacedKey key(final String key) {
        final int ix = key.indexOf(':');
        return ix == -1 ? new NamespacedKey(space, key.toLowerCase(Locale.ROOT))
            : new NamespacedKey(key.substring(0, ix).toLowerCase(Locale.ROOT),
            key.substring(ix + 1).toLowerCase(Locale.ROOT));
    }

    public static NamespacedKey key(final Key key) {
        return new NamespacedKey(key.namespace(), key.value());
    }

    private static LifecycleEventManager<BootstrapContext> mgr;

    @Override
    public void bootstrap(final BootstrapContext cntx) {
        mgr = cntx.getLifecycleManager();

        final TagMap tagMap = new TagMap();
        for (final RegTag<?> rt : RegTag.VALUES.values()) tagMap.add(rt);
        for (final RegTag<?>[] rts : tagMap.values()) regTags(rts, mgr);
    }

    public static void strap(final Consumer<LifecycleEventManager<BootstrapContext>> mcn) {
        if (mgr != null) mcn.accept(mgr);
    }

    private <T extends Keyed> void regTags(final RegTag<?>[] rts, final LifecycleEventManager<BootstrapContext> mgr) {
        if (rts.length == 0) return;
        @SuppressWarnings("unchecked")
        final RegTag<T>[] nrts = (RegTag<T>[]) rts;
        final RegistryKey<T> rk = nrts[0].registryKey();
        mgr.registerEventHandler(LifecycleEvents.TAGS.preFlatten(rk).newHandler(e -> {
            final PreFlattenTagRegistrar<T> reg = e.registrar();
            for (final RegTag<T> rt : nrts) {
                reg.setTag(rt.tagKey(), rt.entries());
            }
        }));
    }

    public static <T extends Keyed> RegistryKeySet<T> regSetOf(final Collection<Key> keys, final RegistryKey<T> reg) {
        return RegistrySet.keySet(reg, keys.stream().map(k -> TypedKey.create(reg, k)).toList());
    }

    @Deprecated
    public static <T extends Keyed> RegistryKeySet<T> regSetOf(final RegistryKey<T> reg, final Collection<T> keys) {
        return RegistrySet.keySetFromValues(reg, keys);
    }

    @Nullable
    @Deprecated
    public static <E extends Keyed> E retrieve(final RegistryKey<E> reg, final Key key) {
        return RegistryAccess.registryAccess().getRegistry(reg).get(key);
    }

    @Deprecated
    public static <E extends Keyed> E retrieve(final Key key, final E or) {
        return get(key, or);
    }

    public static <E extends Keyed> E get(final Key key, final E or) {
        final Registry<E> reg = regOf(or);
        final E val = reg == null ? null : reg.get(key);
        return val == null ? or : val;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <E extends @NotNull Keyed> Registry<E> regOf(final E val) {
        final Registry<?> reg = switch (val) {
            case final Sound ignored -> Ostrov.registries.SOUNDS;
            case final Enchantment ignored -> Ostrov.registries.ENCHANTS;
            case final ItemType ignored -> Ostrov.registries.ITEMS;
            case final BlockType ignored -> Ostrov.registries.BLOCKS;
            case final TrimMaterial ignored -> Ostrov.registries.TRIM_TYPES;
            case final TrimPattern ignored -> Ostrov.registries.TRIM_PATTS;
            case final EntityType ignored -> Ostrov.registries.ENTITIES;
            case final Attribute ignored -> Ostrov.registries.ATTRIBS;
            case final Biome ignored -> Ostrov.registries.BIOMES;
            case final DamageType ignored -> Ostrov.registries.DAMAGES;
            case final DataComponentType ignored -> Ostrov.registries.COMPS;
            default -> {
                Ostrov.log_warn("Registry of " + val.getClass().getSimpleName() + " is not defined");
                yield null;
            }
        };
        return reg == null ? null : (Registry<E>) reg;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <E extends @NotNull Keyed> RegistryKey<E> regKeyOf(final E val) {
        final RegistryKey<?> reg = switch (val) {
            case final Sound ignored -> RegistryKey.SOUND_EVENT;
            case final Enchantment ignored -> RegistryKey.ENCHANTMENT;
            case final ItemType ignored -> RegistryKey.ITEM;
            case final BlockType ignored -> RegistryKey.BLOCK;
            case final TrimMaterial ignored -> RegistryKey.TRIM_MATERIAL;
            case final TrimPattern ignored -> RegistryKey.TRIM_PATTERN;
            case final EntityType ignored -> RegistryKey.ENTITY_TYPE;
            case final Attribute ignored -> RegistryKey.ATTRIBUTE;
            case final Biome ignored -> RegistryKey.BIOME;
            case final DamageType ignored -> RegistryKey.DAMAGE_TYPE;
            case final DataComponentType ignored -> RegistryKey.DATA_COMPONENT_TYPE;
            default -> {
                Ostrov.log_warn("Registry of " + val.getClass().getSimpleName() + " is not defined");
                yield null;
            }
        };
        return reg == null ? null : (RegistryKey<E>) reg;
    }

    public static <E extends Keyed> NamespacedKey keyOf(final E val) {
        final Registry<E> reg = regOf(val);
        final NamespacedKey nk = reg == null ? null : reg.getKey(val);
        return nk == null ? key(val.key()) : nk;
    }

    @Deprecated
    public static <E extends Keyed> List<E> retrieveAll(final RegistryKey<E> reg) {
        return getAll(reg);
    }

    public static <E extends Keyed> List<E> getAll(final RegistryKey<E> reg) {
        final Registry<E> rg = RegistryAccess.registryAccess().getRegistry(reg);
        return rg.stream().toList();
    }

    private static final Map<RegistryKey<? extends Keyed>, List<Tag<? extends @NotNull Keyed>>> tags = new HashMap<>();
    @Deprecated
    public static <T extends Keyed> Tag<T> regTag(final TagKey<T> key, final Collection<T> def) {
        final Registry<T> reg = RegistryAccess.registryAccess().getRegistry(key.registryKey());
        if (reg.hasTag(key)) return reg.getTag(key);
        final Tag<T> tag = new Tag<T>() {
            private final Set<TypedKey<T>> keys = def.stream()
                .map(i -> TypedKey.create(key.registryKey(), i.key()))
                .collect(Collectors.toUnmodifiableSet());

            public @NonNull TagKey<T> tagKey() {return key;}
            public @NonNull RegistryKey<T> registryKey() {return key.registryKey();}
            public boolean contains(final @NonNull TypedKey<T> tk) {return keys.contains(tk);}
            public @NonNull @Unmodifiable Collection<TypedKey<T>> values() {return keys;}
            public @NonNull @Unmodifiable Collection<T> resolve(final @NonNull Registry<T> reg) {
                return keys.stream().map(t -> reg.get(t)).toList();
            }
        };
        tags.computeIfAbsent(tag.registryKey(), t -> new ArrayList<>()).add(tag);
        return tag;
    }

    public static <T extends Keyed> boolean hasTag(final TagKey<T> tk, final T val) {
        final Registry<T> reg = regOf(val);
        final TypedKey<T> tp = TypedKey.create(tk.registryKey(), val.key());
        if (!tags.isEmpty()) {
            for (final Tag<?> tg : tags.get(tk.registryKey())) {
                if (has(tg, tp)) return true;
            }
        }
        if (reg == null) return false;
        return reg.getTag(tk).contains(tp);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Keyed> boolean has(final Tag<T> tag, final TypedKey<?> val) {
        return tag.contains((TypedKey<T>) val);
    }

    @Deprecated
    public static <T extends Keyed> Set<T> getAll(final TagKey<T> tk, final RegistryKey<T> ignored) {
        return getAll(tk);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Keyed> Set<T> getAll(final TagKey<T> tk) {
        final RegistryKey<T> rk = tk.registryKey();
        final Registry<T> reg = RegistryAccess.registryAccess().getRegistry(rk);
        if (!tags.isEmpty()) {
            final List<Tag<?>> tags = OStrap.tags.get(tk.registryKey());
            if (tags != null) {
                for (final Tag<?> tg : tags) if (tk.equals(tg.tagKey()))
                    return new HashSet<>(((Tag<T>) tg).resolve(reg));
            }
        }
        return new HashSet<>(reg.getTag(tk).resolve(reg));
    }
}
