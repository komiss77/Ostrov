package ru.komiss77.boot;

import java.util.*;
import java.util.stream.Collectors;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.event.WritableRegistry;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockType;
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
import ru.komiss77.modules.enchants.CustomEnchant;
import ru.komiss77.modules.enchants.EnchantManager;
import ru.komiss77.utils.TCUtil;


public class OStrap implements PluginBootstrap {

    public static final String space = "ostrov";
    private static final RegistryKeySet<ItemType>
        noIts = RegistrySet.keySet(RegistryKey.ITEM);

    public static NamespacedKey key(final String key) {
        final int ix = key.indexOf(':');
        if (ix != -1) {
            return new NamespacedKey(key.substring(0, ix).toLowerCase(Locale.ROOT),
                key.substring(ix + 1).toLowerCase(Locale.ROOT));
        }
        return new NamespacedKey(space, key.toLowerCase(Locale.ROOT));
    }

    public static NamespacedKey key(final Key key) {
        return new NamespacedKey(key.namespace(), key.value());
    }

    @Override
    public void bootstrap(@NotNull BootstrapContext cntx) {
        final @NotNull LifecycleEventManager<BootstrapContext> mgr = cntx.getLifecycleManager();
        mgr.registerEventHandler(RegistryEvents.ENCHANTMENT
            .freeze().newHandler(e -> {
                EnchantManager.init();
                final @NonNull WritableRegistry<Enchantment, EnchantmentRegistryEntry.Builder> rg = e.registry();
                for (final CustomEnchant ce : CustomEnchant.VALUES.values()) {
                    rg.register(TypedKey.create(RegistryKey.ENCHANTMENT, ce.getKey()),
                        b -> b.description(TCUtil.form(ce.name()))
                            .primaryItems(ce.isInTable() ? ce.targets() : noIts)
                            .supportedItems(ce.isInTable() ? noIts : ce.targets())
                            .anvilCost(ce.anvilCost())
                            .maxLevel(ce.maxLevel())
                            .weight(ce.weight())
                            .exclusiveWith(ce.conflicts())
                            .minimumCost(ce.minCost())
                            .maximumCost(ce.maxCost())
                            .activeSlots(ce.slots()));
                }
            }));
    }

    public static <T extends Keyed> RegistryKeySet<T> regSetOf(final Collection<Key> keys, final RegistryKey<T> reg) {
        return RegistrySet.keySet(reg, keys.stream().map(k -> TypedKey.create(reg, k)).toList());
    }

    public static <T extends Keyed> RegistryKeySet<T> regSetOf(final RegistryKey<T> reg, final Collection<T> keys) {
        return RegistrySet.keySetFromValues(reg, keys);
    }

    @Nullable
    public static <E extends Keyed> E retrieve(final RegistryKey<E> reg, final Key key) {
        final Registry<E> rg = RegistryAccess.registryAccess().getRegistry(reg);
        return rg.get(TypedKey.create(reg, key));
    }

    public static <E extends Keyed> E retrieve(final Key key, final E or) {
        final Registry<E> reg = regOf(or);
        final E val = reg == null ? null : reg.get(key);
        return val == null ? or : val;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <E extends Keyed> Registry<E> regOf(final E val) {
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
            default -> {
                Ostrov.log_warn("Registry of " + val.getClass().getSimpleName() + " is not defined");
                yield null;
            }
        };
        return reg == null ? null : (Registry<E>) reg;
    }

    public static <E extends Keyed> NamespacedKey keyOf(final E val) {
        final Registry<E> reg = regOf(val);
        final NamespacedKey nk = reg == null ? null : reg.getKey(val);
        return nk == null ? key(val.key()) : nk;
    }

    public static <E extends Keyed> List<E> retrieveAll(final RegistryKey<E> reg) {
        final Registry<E> rg = RegistryAccess.registryAccess().getRegistry(reg);
        return rg.stream().toList();
    }

    private static final Map<RegistryKey<? extends Keyed>, List<Tag<? extends @NotNull Keyed>>> tags = new HashMap<>();

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
}
