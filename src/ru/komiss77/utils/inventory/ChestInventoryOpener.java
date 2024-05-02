package ru.komiss77.utils.inventory;

import com.google.common.base.Preconditions;

import ru.komiss77.utils.TCUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class ChestInventoryOpener implements InventoryOpener {


    @Override
    public Inventory getInventory(SmartInventory inv, Player player) {
        Preconditions.checkArgument(inv.getColumns() == 9,
                "The column count for the chest inventory must be 9, found: %s.", inv.getColumns());
        Preconditions.checkArgument(inv.getRows() >= 1 && inv.getRows() <= 6,
                "The row count for the chest inventory must be between 1 and 6, found: %s", inv.getRows());

        Inventory handle = Bukkit.createInventory(player, inv.getRows() * inv.getColumns(), TCUtils.format(inv.getTitle()));

        return handle;
    }    
    
    @Override
    public Inventory open(SmartInventory inv, Player player) {
        //Preconditions.checkArgument(inv.getColumns() == 9,
        //        "The column count for the chest inventory must be 9, found: %s.", inv.getColumns());
        //Preconditions.checkArgument(inv.getRows() >= 1 && inv.getRows() <= 6,
        //        "The row count for the chest inventory must be between 1 and 6, found: %s", inv.getRows());

       // InventoryManager manager = inv.getManager();
        Inventory handle = inv.handle;//Bukkit.createInventory(player, inv.getRows() * inv.getColumns(), inv.getTitle());

        fill(handle, InventoryManager.getContents(player).get(), player);

        player.openInventory(handle);
        return handle;
    }

    @Override
    public boolean supports(InventoryType type) {
        return type == InventoryType.CHEST || type == InventoryType.ENDER_CHEST;
    }


}

/*
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
*/
