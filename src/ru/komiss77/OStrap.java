package ru.komiss77;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
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
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.komiss77.modules.enchants.CustomEnchant;
import ru.komiss77.modules.enchants.EnchantManager;
import ru.komiss77.utils.TCUtil;


public class OStrap implements PluginBootstrap {

    public static final String space = "ostrov";

    public static NamespacedKey key(final String key) {
        return new NamespacedKey(space, key.toLowerCase(Locale.ROOT));
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
                            .primaryItems(ce.isCommon() ? ce.targets().regSet() : RegistrySet.keySet(RegistryKey.ITEM))
                            .supportedItems(ce.isCommon() ? RegistrySet.keySet(RegistryKey.ITEM) : ce.targets().regSet())
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

    public static <T extends Keyed> RegistryKeySet<T> regSetOf(final RegistryKey<T> reg, final Collection<Key> keys) {
        return RegistrySet.keySet(reg, keys.stream().map(k -> TypedKey.create(reg, k)).toList());
    }

    @Nullable
    public static <E extends Keyed> E retrieve(final RegistryKey<E> reg, final Key key) {
        final Registry<E> rg = RegistryAccess.registryAccess().getRegistry(reg);
        return rg.get(TypedKey.create(reg, key));
    }

    public static <E extends Keyed> List<E> retrieveAll(final RegistryKey<E> reg) {
        final Registry<E> rg = RegistryAccess.registryAccess().getRegistry(reg);
        return rg.stream().toList();
    }
}
