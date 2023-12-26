package ru.komiss77.utils.inventory;

import com.google.common.collect.ImmutableList;

import ru.komiss77.utils.TCUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class SpecialInventoryOpener implements InventoryOpener {

    private static final List<InventoryType> SUPPORTED = ImmutableList.of(
        InventoryType.FURNACE,
        InventoryType.WORKBENCH,
        InventoryType.DISPENSER,
        InventoryType.DROPPER,
        InventoryType.ENCHANTING,
        InventoryType.BREWING,
        InventoryType.ANVIL,
        InventoryType.BEACON,
        InventoryType.HOPPER
    );

    
    @Override
    public Inventory getInventory(SmartInventory inv, Player player) {
        Inventory handle = Bukkit.createInventory(player, inv.getType(), TCUtils.format(inv.getTitle()));
        return handle;
    }    
    
    @Override
    public Inventory open(SmartInventory inv, Player player) {
        //InventoryManager manager = inv.getManager();
        Inventory handle = inv.handle;//Bukkit.createInventory(player, inv.getType(), inv.getTitle());

        fill(handle, InventoryManager.getContents(player).get(), player);

        player.openInventory(handle);
        return handle;
    }

    @Override
    public boolean supports(InventoryType type) {
        return SUPPORTED.contains(type);
    }

}

/*
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
*/
