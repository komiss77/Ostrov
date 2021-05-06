package ru.komiss77.utils.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import java.util.function.Consumer;
import org.bukkit.inventory.ItemStack;

public class ClickableItem
{
    private final ItemStack item;
    private final Consumer<InventoryClickEvent> consumer;
    
    public ClickableItem(final ItemStack item, final Consumer<InventoryClickEvent> consumer) {
        this.item = item;
        this.consumer = consumer;
    }
    
    public static ClickableItem empty(final ItemStack item) {
        return of( item, p0 -> {} );
    }
    
    public static ClickableItem of(final ItemStack item, final Consumer<InventoryClickEvent> consumer) {
        return new ClickableItem(item, consumer);
    }
    
    public void run(final InventoryClickEvent e) {
        consumer.accept(e);
    }
    
    public ItemStack getItem() {
        return item;
    }
}
