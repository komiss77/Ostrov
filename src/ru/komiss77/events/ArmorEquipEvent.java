package ru.komiss77.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.enums.ArmorType;


//@Deprecated
public final class ArmorEquipEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private final EquipMethod equipType;
    private final ArmorType type;
    private ItemStack oldArmorPiece, newArmorPiece;

    /**
     * Constructor for the ArmorEquipEvent.
     *
     * @param player        The player who put on / removed the armor.
     * @param equipType
     * @param type          The ArmorType of the armor added
     * @param oldArmorPiece The ItemStack of the armor removed.
     * @param newArmorPiece The ItemStack of the armor added.
     */
    //@Deprecated
    public ArmorEquipEvent(final Player player, final EquipMethod equipType, final ArmorType type, final ItemStack oldArmorPiece, final ItemStack newArmorPiece) {
        super(player);
        this.equipType = equipType;
        this.type = type;
        this.oldArmorPiece = oldArmorPiece;
        this.newArmorPiece = newArmorPiece;
    }

    /**
     * Gets a list of handlers handling this event.
     *
     * @return A list of handlers handling this event.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Gets a list of handlers handling this event.
     *
     * @return A list of handlers handling this event.
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Sets if this event should be cancelled.
     *
     * @param cancel If this event should be cancelled.
     */
    @Override
    public void setCancelled(final boolean cancel) {
        this.cancel = cancel;
    }

    /**
     * Gets if this event is cancelled.
     *
     * @return If this event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    public ArmorType getType() {
        return type;
    }

    /**
     * Returns the fin equipped armor piece, could be a piece of armor, {@link Material#AIR}, or null.
     *
     * @return
     */
    public ItemStack getOldArmorPiece() {
        return oldArmorPiece;
    }

    public void setOldArmorPiece(final ItemStack oldArmorPiece) {
        this.oldArmorPiece = oldArmorPiece;
    }

    /**
     * Returns the newly equipped armor, could be a piece of armor, {@link Material#AIR}, or null.
     *
     * @return
     */
    public ItemStack getNewArmorPiece() {
        return newArmorPiece;
    }

    public void setNewArmorPiece(final ItemStack newArmorPiece) {
        this.newArmorPiece = newArmorPiece;
    }

    /**
     * Gets the method used to either equip or unequip an armor piece.
     *
     * @return
     */
    public EquipMethod getMethod() {
        return equipType;
    }

    public enum EquipMethod {
        /**
         * When you shift click an armor piece to equip or unequip
         */
        CREATIVE,

        /**
         * When you shift click an armor piece to equip or unequip
         */
        SHIFT_CLICK,

        /**
         * When you drag and drop the item to equip or unequip
         */
        DRAG,

        /**
         * When you manually equip or unequip the item. Use to be DRAG
         */
        PICK_DROP,

        /**
         * When you right click an armor piece in the hotbar without the inventory open to equip.
         */
        HOTBAR, //ПКМ не открывая инвентаря

        /**
         * When you press the hotbar slot number while hovering over the armor slot to equip or unequip
         */
        HOTBAR_SWAP,

        /**
         * When in range of a dispenser that shoots an armor piece to equip.
         */
        DISPENSER,

        /**
         * When an armor piece breaks to unequip
         */
        BROKE,

        /**
         * When you die causing all armor to unequip
         */
        DEATH,
    }
}
