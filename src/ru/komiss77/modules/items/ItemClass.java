package ru.komiss77.modules.items;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

public class ItemClass {
	
	protected static final Map<String, ItemClass> VALUES = new HashMap<>();
	
	public static final ItemClass RANGED = new ItemClass("RANGED", Material.BOW, Material.CROSSBOW);
	public static final ItemClass MELEE = new ItemClass("MELEE", Material.DIAMOND_SWORD, 
			Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.WOODEN_SWORD, 
			Material.STONE_SWORD, Material.NETHERITE_SWORD, Material.TRIDENT);
	public static final ItemClass MELEE_AXE = new ItemClass("MELEE_AXE", Material.DIAMOND_SWORD, 
			Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.WOODEN_SWORD, 
			Material.STONE_SWORD, Material.NETHERITE_SWORD, Material.TRIDENT, 
			Material.NETHERITE_AXE, Material.STONE_AXE, Material.WOODEN_AXE, 
			Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE);
	public static final ItemClass MELEE_TOOL = new ItemClass("MELEE_TOOL", Material.DIAMOND_SWORD, 
			Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.WOODEN_SWORD, 
			Material.STONE_SWORD, Material.NETHERITE_SWORD, Material.TRIDENT, 
			Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, 
			Material.NETHERITE_AXE, Material.STONE_AXE, Material.WOODEN_AXE, 
			Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, 
			Material.NETHERITE_PICKAXE, Material.STONE_PICKAXE, Material.WOODEN_PICKAXE, 
			Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, 
			Material.NETHERITE_SHOVEL, Material.STONE_SHOVEL, Material.WOODEN_SHOVEL, 
			Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, 
			Material.NETHERITE_HOE, Material.STONE_HOE, Material.WOODEN_HOE, 
			Material.SHEARS, Material.FLINT_AND_STEEL, Material.FISHING_ROD);
	public static final ItemClass TOOL = new ItemClass("TOOL", Material.IRON_AXE, 
			Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE, 
			Material.STONE_AXE, Material.WOODEN_AXE, Material.IRON_PICKAXE, 
			Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE, 
			Material.STONE_PICKAXE, Material.WOODEN_PICKAXE, Material.IRON_SHOVEL, 
			Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL, 
			Material.STONE_SHOVEL, Material.WOODEN_SHOVEL, Material.IRON_HOE, 
			Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE, 
			Material.STONE_HOE, Material.WOODEN_HOE, Material.SHEARS, 
			Material.FLINT_AND_STEEL, Material.FISHING_ROD);
	public static final ItemClass ARMOR = new ItemClass("ARMOR", Material.DIAMOND_HELMET, 
			Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
			Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS,
			Material.GOLDEN_BOOTS, Material.IRON_HELMET, Material.IRON_CHESTPLATE,
			Material.IRON_LEGGINGS, Material.IRON_BOOTS, Material.NETHERITE_HELMET,
			Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
			Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS,
			Material.LEATHER_BOOTS, Material.TURTLE_HELMET, Material.CHAINMAIL_HELMET,
			Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS);
	public static final ItemClass ALL = new ItemClass("ALL", Material.DIAMOND_SWORD, 
			Material.GOLDEN_SWORD, Material.IRON_SWORD, Material.WOODEN_SWORD, 
			Material.STONE_SWORD, Material.NETHERITE_SWORD, Material.TRIDENT, 
			Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, 
			Material.NETHERITE_AXE, Material.STONE_AXE, Material.WOODEN_AXE, 
			Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, 
			Material.NETHERITE_PICKAXE, Material.STONE_PICKAXE, Material.WOODEN_PICKAXE, 
			Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, 
			Material.NETHERITE_SHOVEL, Material.STONE_SHOVEL, Material.WOODEN_SHOVEL, 
			Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, 
			Material.NETHERITE_HOE, Material.STONE_HOE, Material.WOODEN_HOE, 
			Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, 
			Material.DIAMOND_BOOTS, Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, 
			Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS, Material.IRON_HELMET, 
			Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS, 
			Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, 
			Material.NETHERITE_BOOTS, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, 
			Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, Material.TURTLE_HELMET, 
			Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, 
			Material.CHAINMAIL_BOOTS, Material.SHIELD, Material.BOW, Material.CROSSBOW, 
			Material.SHEARS, Material.FLINT_AND_STEEL, Material.FISHING_ROD);
	
	private final String name;
	private final Material[] mts;

	public ItemClass(final String name, final Material... mts) {
    	Arrays.sort(mts);
    	this.name = name;
		this.mts = mts;
		VALUES.put(name, this);
	}
	
	public static Map<String, ItemClass> values() {
		return VALUES;
	}
	
	public boolean has(final Material mt) {
		return Arrays.binarySearch(mts, mt) >= 0;
	}
	
	@Override
	public boolean equals(final Object o) {
		return o instanceof ItemClass && ((ItemClass) o).name.equals(name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
