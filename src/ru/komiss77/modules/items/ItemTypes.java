package ru.komiss77.modules.items;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import org.bukkit.Material;
import org.bukkit.inventory.ItemType;
import ru.komiss77.OStrap;

public class ItemTypes {

    protected static final Map<String, ItemTypes> VALUES = new HashMap<>();

    public static final ItemTypes EMPTY = new ItemTypes("EMPTY", new ItemType[0]);
    public static final ItemTypes SWORDS = new ItemTypes("SWORDS", ItemType.STONE_SWORD,
        ItemType.WOODEN_SWORD, ItemType.DIAMOND_SWORD, ItemType.GOLDEN_SWORD, ItemType.IRON_SWORD, ItemType.NETHERITE_SWORD);

    private final String name;
    private final RegistryKeySet<ItemType> itemKeys;

    public ItemTypes(final String name, final ItemType... its) {
        this.name = name;
        final RegistryKey<ItemType> reg = RegistryKey.ITEM;
        this.itemKeys = OStrap.regSetOf(reg, Arrays.stream(its)
            .map(ItemType::getKey).collect(Collectors.toSet()));

        VALUES.put(name, this);
    }

    @SafeVarargs
    public ItemTypes(final String name, final TypedKey<ItemType>... keys) {
        this.name = name;
        this.itemKeys = RegistrySet.keySet(RegistryKey.ITEM, keys);

        VALUES.put(name, this);
    }

    @Deprecated
    public boolean has(final Material mt) {
        return itemKeys.contains(TypedKey.create(itemKeys.registryKey(), mt.asItemType().getKey()));
    }

    public boolean has(final ItemType it) {
        return itemKeys.contains(TypedKey.create(itemKeys.registryKey(), it.getKey()));
    }

    public RegistryKeySet<ItemType> regSet() {
        return itemKeys;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof ItemTypes && ((ItemTypes) o).name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
