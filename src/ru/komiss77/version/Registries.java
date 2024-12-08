package ru.komiss77.version;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public class Registries {

    public final Registry<Sound> SOUNDS;
    public final Registry<Biome> BIOMES;
    public final Registry<ItemType> ITEMS;
    public final Registry<TrimMaterial> TRIM_TYPES;
    public final Registry<TrimPattern> TRIM_PATTS;
    public final Registry<EntityType> ENTITIES;

    public Registries() {
        final RegistryAccess rac = RegistryAccess.registryAccess();
        SOUNDS = rac.getRegistry(RegistryKey.SOUND_EVENT);
        BIOMES = rac.getRegistry(RegistryKey.BIOME);
        ITEMS = rac.getRegistry(RegistryKey.ITEM);
        TRIM_TYPES = rac.getRegistry(RegistryKey.TRIM_MATERIAL);
        TRIM_PATTS = rac.getRegistry(RegistryKey.TRIM_PATTERN);
        ENTITIES = rac.getRegistry(RegistryKey.ENTITY_TYPE);
    }

}
