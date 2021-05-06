package ru.komiss77.utils.inventory;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;

public interface InventoryOpener {
    
    Inventory open(final SmartInventory inv, final Player player);
    
    boolean supports(final InventoryType type);
    
    default void fill(final Inventory handle, final InventoryContent contents) {
        final ClickableItem[][] items = contents.all();
        for (int row = 0; row < items.length; ++row) {
            for (int column = 0; column < items[row].length; ++column) {
                if (items[row][column] != null) {
                    handle.setItem(9 * row + column, items[row][column].getItem());
//System.out.println("fill setItem "+(9 * row + column)+":"+items[row][column].getItem());
                }
            }
        }
    }
}
