package ru.komiss77.utils.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ItemClickData {

    private final Event event;
    private final ClickType clickType;
    private final Player player;
    private final ItemStack item;
    private final SlotPos slot;

    public ItemClickData(final Player player, Event event, final ClickType clickType, ItemStack item, SlotPos slot) {
        this.player = player;
        this.clickType = clickType;
        this.event = event;
        this.item = item;
        this.slot = slot;
    }

    public Event getEvent() { return event; }
    public Player getPlayer() { return player; }
    public ItemStack getItem() { return item; }
    public SlotPos getSlot() { return slot; }
    public ClickType getClick() { return clickType; }

}