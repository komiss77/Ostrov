package ru.komiss77.modules.items;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Objects;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.komiss77.utils.ItemUtils;

public class CustomItems {
	
	private static final HashMap<Integer, CustomItems> VALUES = new HashMap<>();
	
	public static final CustomItems DEFAULT = new CustomItems(0);
	
	public final Integer cmd;
	private final EnumMap<Material, ItemStack> mits = new EnumMap<>(Material.class);
	
	public CustomItems(final Integer cmd, final ItemStack... its) {
		this.cmd = cmd;
		for (final ItemStack it : its) {
			if (ItemUtils.isBlank(it, false)) continue;
			mits.put(it.getType(), it);
		}
		
		VALUES.put(cmd, this);
	}
	
	public static CustomItems[] values() {
		return VALUES.values().toArray(new CustomItems[0]);
	}
	
	public @Nullable ItemStack getItem(final Material mt) {
		return mits.get(mt);
	}
    
    public static CustomItems getCstmItm(final ItemMeta im) {
        return im != null && im.hasCustomModelData() ? 
        	getCstmItm(im.getCustomModelData()) : DEFAULT;
    }
    
    public static CustomItems getCstmItm(final Integer cmd) {
        return cmd == null ? DEFAULT : VALUES.getOrDefault(cmd, DEFAULT);
    }

    public static ItemStack[] getCustomMats(final ItemMeta im) {
    	final CustomItems cis = getCstmItm(im);
        return cis.mits.values().toArray(new ItemStack[0]);
    }
    
    @Override
    public boolean equals(final Object o) {
    	return o instanceof CustomItems
        && Objects.equals(((CustomItems) o).cmd, cmd);
    }
	
    @Override
    public int hashCode() {
    	return cmd == null ? 0 : cmd;
    }
}
