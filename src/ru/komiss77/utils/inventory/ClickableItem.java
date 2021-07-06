package ru.komiss77.utils.inventory;

import java.util.List;
import org.bukkit.event.inventory.InventoryClickEvent;
import java.util.function.Consumer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ClickableItem {
    
    private final ItemStack item;
    private final ItemMeta meta;
    private final Consumer<InventoryClickEvent> consumer;
    
    public ClickableItem(final ItemStack item, final Consumer<InventoryClickEvent> consumer) {
        this.item = item;
        meta = item.getItemMeta();
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

    public void setLore(final List<String> lore) {
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
    
    public void setLine(final int line, final String str) {
        if (!meta.hasLore()) return;
        final List<String> lore = meta.getLore();
        lore.set(line, str);
        setLore(lore);
    }
    
    
    
    
    
    
    
    
    
}
