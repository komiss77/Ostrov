package ru.komiss77.modules.menuItem;

import java.util.function.Consumer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;


public class MenuItemBuilder {
    
    private final MenuItem si;
    
    public MenuItemBuilder (final String name, final ItemStack is) {
        si = new MenuItem(name, is);
        //закидываем типовые значения
        si.slot = 8;
        si.anycase = false;
        si.can_interact = false;
        si.can_drop = true;
        si.can_move = true;
        si.can_pickup = true;
        si.duplicate = false;
        si.give_on_join = true;
        si.give_on_respavn = true;
        si.give_on_world_change = true;
    }
    
    public MenuItemBuilder slot(final int slot) {
        si.slot = slot;
        return this;
    }
    
    public MenuItemBuilder anycase(final boolean value) {
        si.anycase = value;
        return this;
    }
    public MenuItemBuilder canDrop(final boolean value) {
        si.can_drop = value;
        return this;
    }
    public MenuItemBuilder canMove(final boolean value) {
        si.can_move = value;
        return this;
    }
    public MenuItemBuilder canPickup(final boolean value) {
        si.can_pickup = value;
        return this;
    }
    public MenuItemBuilder duplicate(final boolean value) {
        si.duplicate = value;
        return this;
    }
    
    public MenuItemBuilder giveOnJoin(final boolean value) {
        si.give_on_join = value;
        return this;
    }
    
    public MenuItemBuilder giveOnRespavn(final boolean value) {
        si.give_on_respavn = value;
        return this;
    }
    
    public MenuItemBuilder canInteract(final boolean value) {
        si.can_interact = value;
        return this;
    }
    
    public MenuItemBuilder giveOnWorld_change(final boolean value) {
        si.give_on_world_change = value;
        return this;
    }
    
    public MenuItemBuilder rightClickCmd(final String cmd) {
        si.on_right_click = p -> p.performCommand(cmd);
        return this;
    }
    public MenuItemBuilder rightShiftClickCmd(final String cmd) {
        si.on_right_sneak_click = p -> p.performCommand(cmd);
        return this;
    }
    public MenuItemBuilder leftClickCmd(final String cmd) {
        si.on_left_click = p -> p.performCommand(cmd);
        return this;
    }
    public MenuItemBuilder leftShiftClickCmd(final String cmd) {
        si.on_left_sneak_click = p -> p.performCommand(cmd);
        return this;
    }
    
    public MenuItemBuilder inventoryClick(final Consumer<InventoryClickEvent> consumer) {
        si.on_inv_click = consumer;
        return this;
    }
    
    public MenuItemBuilder interact (final Consumer<PlayerInteractEvent> consumer) {
        si.on_interact = consumer;
        return this;
    }
    
    
    public MenuItem create() {
//Ostrov.log("MenuItem create can_interact="+si.can_interact);
        MenuItemsManager.addItem(si);
        return si;
    }
    
}
