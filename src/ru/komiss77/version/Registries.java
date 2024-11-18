package ru.komiss77.version;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;

public class Registries {

    public final Registry<Sound> SOUNDS;
    public final Registry<Biome> BIOMES;
    public final Registry<ItemType> ITEMS;
    public final Registry<EntityType> ENTITIES;

    public Registries() {
        final RegistryAccess rac = RegistryAccess.registryAccess();
        SOUNDS = rac.getRegistry(RegistryKey.SOUND_EVENT);
        BIOMES = rac.getRegistry(RegistryKey.BIOME);
        ITEMS = rac.getRegistry(RegistryKey.ITEM);
        ENTITIES = rac.getRegistry(RegistryKey.ENTITY_TYPE);
    }

}
