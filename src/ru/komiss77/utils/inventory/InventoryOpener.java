package ru.komiss77.utils.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public interface InventoryOpener {

    Inventory getInventory(SmartInventory inv, Player player);

    Inventory open(SmartInventory inv, Player player);

    boolean supports(InventoryType type);

    default void fill(Inventory handle, InventoryContent contents, Player player) {
        ClickableItem[][] items = contents.all();

      final SlotPos size = defaultSize(handle.getType());
      //for (int row = 0; row < items.length; row++) {
      //    for (int column = 0; column < items[row].length; column++) {
      //        if (items[row][column] != null)
      //            handle.setItem(size.column() * row + column, items[row][column].getItem(player));
      //   }
      //}
      ClickableItem[] row;
        for (int r = 0; r < items.length; r++) {
          row = items[r];
            for (int c = 0; c < row.length; c++) {
                if (row[c] != null) {
                    handle.setItem(row.length * r + c, row[c].getItem(player));
                }
            }
        }
    }

    /**
     * This method is used to configure the default inventory size(s)
     * for inventories supported by this opener. These values will only
     * be applied if the size is not set explicitly. (See {@link SmartInventory.Builder#size(int, int)}).
     * <p>
     * This method must return a non-null value for all supported inventory types.
     *
     * @param type inventory type
     * @return The desired default dimensions, this default implementation returns
     * (3x9) for type (ender)chest, (3x3) for dispenser & dropper and
     * (1x_sizeOfInventoryType_) for everything else.
     */
    default SlotPos defaultSize(final InventoryType type) {
        return switch (type) {
            case CHEST, ENDER_CHEST -> SlotPos.of(3, 9);
            case DISPENSER, DROPPER -> SlotPos.of(3, 3);
            default -> SlotPos.of(1, type.getDefaultSize());
        };
    }


}



/*
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
*/