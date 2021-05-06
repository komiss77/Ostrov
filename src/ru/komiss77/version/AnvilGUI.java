package ru.komiss77.version;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import java.util.function.BiFunction;

import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemBuilder;




public class AnvilGUI {

    private final Plugin plugin;
    private final Player holder;
    private final BiFunction<Player, String, String> biFunction;
    private final ItemStack insert;
    private IAnwillWrapper wrapper=null;
    private int containerId=0;
    private Inventory inventory=null;
    private boolean open;

    private final ListenUp listener = new ListenUp();


    
    
    /*
    @param plugin
    */
    public AnvilGUI(final Plugin plugin, final Player holder, final String current_value, final BiFunction<Player, String, String> biFunction) {
        this.plugin = plugin;
        this.holder = holder;
        this.biFunction = biFunction;
        insert = new ItemBuilder(Material.PAPER).setName(current_value).build();
        
        try {
            wrapper = VM.loadModule("AnwillWrapper");
        } catch (ReflectiveOperationException ex) {
            Ostrov.log_err("VM load module : "+ex.getMessage());
            return;
        }

        wrapper.handleInventoryCloseEvent(holder);
        wrapper.setActiveContainerDefault(holder);
        final Object container = wrapper.newContainerAnvil(holder);
        inventory = wrapper.toBukkitInventory(container);
        inventory.setItem(0, this.insert);
        containerId = wrapper.getNextContainerId(holder);
        wrapper.sendPacketOpenWindow(holder, containerId);
        wrapper.setActiveContainer(holder, container);
        wrapper.setActiveContainerId(container, containerId);
        wrapper.addActiveContainerSlotListener(container, holder);
        
        Bukkit.getPluginManager().registerEvents(listener, this.plugin);

        open = true;
    }

    
    
    
    
    
    
    
    
    

    public void closeInventory() {
        HandlerList.unregisterAll(listener);
        
        //Validate.isTrue(open, "You can't close an inventory that isn't open!");
        if (!this.open) return;
        open = false;

        wrapper.handleInventoryCloseEvent(holder);
        wrapper.setActiveContainerDefault(holder);
        wrapper.sendPacketCloseWindow(holder, containerId);
    }
    
    

    /**
     * Simply holds the listeners for the GUI
     */
    private class ListenUp implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if(e.getInventory().equals(inventory)) {
                e.setCancelled(true);
                final Player clicker = (Player) e.getWhoClicked();
                if(e.getRawSlot() == Slot.OUTPUT) {
                    final ItemStack clicked = inventory.getItem(e.getRawSlot());
                    if(clicked == null || clicked.getType() == Material.AIR) return;
                    final String ret = biFunction.apply(clicker, clicked.hasItemMeta() ? clicked.getItemMeta().getDisplayName() : clicked.getType().toString());
                    if(ret != null) {
                        final ItemMeta meta = clicked.getItemMeta();
                        meta.setDisplayName(ret);
                        clicked.setItemMeta(meta);
                        inventory.setItem(e.getRawSlot(), clicked);
                    } else closeInventory();
                }
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if(open && e.getInventory().equals(inventory)) closeInventory();
        }

    }


    
    public static class Slot {
        /**
         * The slot on the far left, where the first input is inserted. An {@link ItemStack} is always inserted
         * here to be renamed
         */
        public static final int INPUT_LEFT = 0;
        /**
         * Not used, but in a real anvil you are able to put the second item you want to combine here
         */
        public static final int INPUT_RIGHT = 1;
        /**
         * The output slot, where an item is put when two items are combined from {@link #INPUT_LEFT} and
         * {@link #INPUT_RIGHT} or {@link #INPUT_LEFT} is renamed
         */
        public static final int OUTPUT = 2;

    }
    
    

}
