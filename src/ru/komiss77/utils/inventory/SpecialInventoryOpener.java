package ru.komiss77.utils.inventory;

import com.google.common.collect.ImmutableList;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import java.util.List;

public class SpecialInventoryOpener implements InventoryOpener
{
    private static final List<InventoryType> SUPPORTED;
    
    @Override
    public Inventory open(final SmartInventory inv, final Player player) {
        final InventoryManager invManager = InventoryManager.get();
        final Inventory inventory = invManager.getContents(player).get().getInventory();
//System.out.println("open  getContents="+invManager.getContents(player).get());
        fill(inventory, invManager.getContents(player).get());
        player.openInventory(inventory);
        return inventory;
    }
    
    @Override
    public boolean supports(final InventoryType type) {
//System.out.println("--------supports ? "+SpecialInventoryOpener.SUPPORTED.contains(type));
        return SpecialInventoryOpener.SUPPORTED.contains(type);
    }
    
    static {
        SUPPORTED = (List)ImmutableList.of( 
                (Object)InventoryType.FURNACE, 
                (Object)InventoryType.WORKBENCH, 
                (Object)InventoryType.DISPENSER, 
                (Object)InventoryType.DROPPER, 
                (Object)InventoryType.ENCHANTING, 
                (Object)InventoryType.BREWING, 
                (Object)InventoryType.ANVIL, 
                (Object)InventoryType.BEACON, 
                (Object)InventoryType.HOPPER,
                //CRAFTING ??
                (Object)InventoryType.SMITHING, 
                (Object)InventoryType.MERCHANT, 
                (Object)InventoryType.SHULKER_BOX, 
                (Object)InventoryType.BARREL, 
                (Object)InventoryType.BLAST_FURNACE, 
                (Object)InventoryType.LECTERN, 
                (Object)InventoryType.SMOKER, 
                (Object)InventoryType.LOOM, 
                (Object)InventoryType.CARTOGRAPHY, 
                (Object)InventoryType.GRINDSTONE, 
                (Object)InventoryType.STONECUTTER
        );
    }
}
