package ru.komiss77.utils.inventory;

import org.bukkit.event.inventory.InventoryType;
import com.google.common.base.Preconditions;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;

public class ChestInventoryOpener implements InventoryOpener
{
    @Override
    public Inventory open(final SmartInventory inv, final Player player) {
        Preconditions.checkArgument(inv.getColumns() == 9, "The column count for the chest inventory must be 9, found: %s.", inv.getColumns());
        Preconditions.checkArgument(inv.getRows() >= 1 && inv.getRows() <= 6, "The row count for the chest inventory must be between 1 and 6, found: %s", inv.getRows());
        final InventoryManager value = InventoryManager.get();
        final Inventory inventory = value.getContents(player).get().getInventory();
        this.fill(inventory, value.getContents(player).get());
        player.openInventory(inventory);
        return inventory;
    }
    
    @Override
    public boolean supports(final InventoryType type) {
        return type == InventoryType.CHEST || type == InventoryType.ENDER_CHEST;
    }
}
