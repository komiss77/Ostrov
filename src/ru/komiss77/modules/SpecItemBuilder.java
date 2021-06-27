package ru.komiss77.modules;

import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Objects.SpecItem;
import ru.komiss77.Ostrov;





public class SpecItemBuilder {
    
    private final SpecItem si;
    
    public SpecItemBuilder (final String name, final ItemStack is) {
        si = new SpecItem(name, is);
        //закидываем типовые значения
        si.slot = 8;
        si.anycase = false;
        si.can_drop = true;
        si.can_move = true;
        si.can_pickup = true;
        si.duplicate = false;
        si.give_on_join = true;
        si.give_on_respavn = true;
        si.give_on_world_change = true;
    }
    
    public SpecItemBuilder slot(final int slot) {
        si.slot = slot;
        return this;
    }
    
    public SpecItemBuilder anycase(final boolean value) {
        si.anycase = value;
        return this;
    }
    public SpecItemBuilder canDrop(final boolean value) {
        si.can_drop = value;
        return this;
    }
    public SpecItemBuilder canMove(final boolean value) {
        si.can_move = value;
        return this;
    }
    public SpecItemBuilder canPickup(final boolean value) {
        si.can_pickup = value;
        return this;
    }
    public SpecItemBuilder duplicate(final boolean value) {
        si.duplicate = value;
        return this;
    }
    
    public SpecItemBuilder giveOnJoin(final boolean value) {
        si.give_on_join = value;
        return this;
    }
    
    public SpecItemBuilder giveOnRespavn(final boolean value) {
        si.give_on_respavn = value;
        return this;
    }
    
    public SpecItemBuilder giveOnWorld_change(final boolean value) {
        si.give_on_world_change = value;
        return this;
    }
    
    public SpecItemBuilder rightClickCmd(final String cmd) {
        si.on_right_click = p -> p.performCommand(cmd);
        return this;
    }
    public SpecItemBuilder rightShiftClickCmd(final String cmd) {
        si.on_right_sneak_click = p -> p.performCommand(cmd);
        return this;
    }
    public SpecItemBuilder leftClickCmd(final String cmd) {
        si.on_left_click = p -> p.performCommand(cmd);
        return this;
    }
    public SpecItemBuilder leftShiftClickCmd(final String cmd) {
        si.on_left_sneak_click = p -> p.performCommand(cmd);
        return this;
    }
    
    public void add() {
        ApiOstrov.getMenuItemManager().addItem(si);
    }
    
}
