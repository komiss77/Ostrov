package ru.komiss77.utils.inventory;

import java.util.function.Consumer;

public class InventoryListener<T> {
    
    private final Class<T> type;
    private final Consumer<T> consumer;
    
    public InventoryListener(final Class<T> type, final Consumer<T> consumer) {
        this.type = type;
        this.consumer = consumer;
    }
    
    public void accept(final T t) {
        this.consumer.accept(t);
    }
    
    public Class<T> getType() {
        return this.type;
    }
    
}


/*
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import java.util.Iterator;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

public class InventoryAPIListener implements Listener {
    
    private InventoryManager manager;
    private JavaPlugin host;
    
    protected InventoryAPIListener() {
    }
    
    protected InventoryAPIListener(final InventoryManager manager, final JavaPlugin host) {
        this.manager = manager;
        this.host = host;
    }
    
    
    
    
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(final InventoryClickEvent e) {
        final Player player = (Player)e.getWhoClicked();
        if (!manager.getInventories().containsKey(player.getName())) {
            return;
        }
        if (e.getAction() == InventoryAction.NOTHING || e.getClickedInventory() == null) {
            e.setCancelled(true);
            return;
        }
        if (e.getClickedInventory().equals(player.getOpenInventory().getBottomInventory()) && (e.getAction() == InventoryAction.COLLECT_TO_CURSOR || e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
            e.setCancelled(true);
            return;
        }
        if (e.getClickedInventory() == player.getOpenInventory().getTopInventory()) {
            e.setCancelled(true);
            final int row = e.getSlot() / 9;
            final int column = e.getSlot() % 9;
            if (row < 0 || column < 0) {
                return;
            }
            final SmartInventory smartInventory = manager.getInventories().get(player.getName());
            if (row >= smartInventory.getRows() || column >= smartInventory.getColumns()) {
                return;
            }
            manager.getContents().get(player.getName()).get(row, column).ifPresent(clickableItem -> clickableItem.run(e));
            player.updateInventory();
        }
    }
    
    
    
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryDrag(final InventoryDragEvent e) {
        final Player player = (Player)e.getWhoClicked();
        if (!manager.getInventories().containsKey(player.getName())) {
            return;
        }
        final Iterator<Integer> iterator = e.getRawSlots().iterator();
        while (iterator.hasNext()) {
            if (iterator.next() >= player.getOpenInventory().getTopInventory().getSize()) {
                continue;
            }
            e.setCancelled(true);
            break;
        }
    }
    
    
    
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClose(final InventoryCloseEvent e) {
        final Player player = (Player)e.getPlayer();
        if (!manager.getInventories().containsKey(player.getName())) {
            return;
        }
        manager.getInventories().get(player.getName()).getProvider().onClose(player, manager.getContents().get(player.getName()));
        manager.getInventories().remove(player.getName());
        manager.getContents().remove(player.getName());
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(final PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        if (!manager.getInventories().containsKey(player.getName())) {
            return;
        }
        manager.getInventories().get(player.getName()).getProvider().onClose(player, manager.getContents().get(player.getName()));
        manager.getInventories().remove(player.getName());
        manager.getContents().remove(player.getName());
    }
    
    
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPluginDisable(final PluginDisableEvent e) {
        if (!e.getPlugin().getName().equals(host.getName())) {
            return;
        }
        manager.getInventories().clear();
        manager.getContents().clear();
    }
}
*/