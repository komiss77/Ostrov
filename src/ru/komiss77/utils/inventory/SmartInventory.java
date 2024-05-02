package ru.komiss77.utils.inventory;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

//слоты сундука https://wiki.vg/Inventory
//https://github.com/MinusKube/SmartInvs

public class SmartInventory {
    
    
    private String id;
    private String title;
    private InventoryType type;
    private int rows, columns;
    private boolean closeable;
    private int updateFrequency;
    private InventoryProvider provider;
    private SmartInventory parent;
    private List<InventoryListener<? extends Event>> listeners;
    
    protected Inventory handle;
    //private final InventoryManager manager;

    
    
    private SmartInventory() {
        //this.manager = InventoryManager;//manager;
    }

    public Inventory open(Player player) {	
        return open(player, 0, Collections.emptyMap());	
    }	
    
    public Inventory open(Player player, int page) {	
        return open(player, page, Collections.emptyMap());	
    }	
    
    public Inventory open(Player player, Map<String, Object> properties) {	
        return open(player, 0, properties);	
    }	

    @SuppressWarnings("unchecked")
    public Inventory open(Player player, int page, Map<String, Object> properties) {
        Optional<SmartInventory> oldInv = InventoryManager.getInventory(player);

        oldInv.ifPresent(inv -> {
            inv.getListeners().stream()
                    .filter(listener -> listener.getType() == InventoryCloseEvent.class)
                    .forEach(listener -> ((InventoryListener<InventoryCloseEvent>) listener)
                            .accept(new InventoryCloseEvent(player.getOpenInventory())));

            InventoryManager.setInventory(player, null);
        });
        
        //fix - old
        //provider.preInit(player);
        //
        InventoryContent contents = new InventoryContent.Impl(this, player);
        contents.pagination().page(page);
        properties.forEach(contents::setProperty);
        
        InventoryManager.setContents(player, contents);
        
        

        InventoryOpener opener = InventoryManager.findOpener(type)
                .orElseThrow( () -> new IllegalStateException("No opener found for the inventory type " + type.name()) );
        //Inventory handle = opener.open(this, player);
        
        handle = opener.getInventory(this, player); //сначала получить образ баккит-инвентаря
        
        provider.init(player, contents); //вызвать init в классе меню (может наполнять свои иконки)
        
        opener.open(this, player);

        InventoryManager.setInventory(player, this);
        InventoryManager.scheduleUpdateTask(player, this);
        
        return handle;
    }

    @SuppressWarnings("unchecked")
	public void close(Player player) {
        listeners.stream()
                .filter(listener -> listener.getType() == InventoryCloseEvent.class)
                .forEach(listener -> ((InventoryListener<InventoryCloseEvent>) listener)
                        .accept(new InventoryCloseEvent(player.getOpenInventory())));

        InventoryManager.setInventory(player, null);
        player.closeInventory();

        InventoryManager.setContents(player, null);
        InventoryManager.cancelUpdateTask(player);
    }
    /**
     * Checks if this inventory has a slot at the specified position
     * @param row Slot row (starts at 0)
     * @param col Slot column (starts at 0)
     * @return 
     */
    public boolean checkBounds(int row, int col) {
        if(row < 0 || col < 0)
            return false;
        return row < this.rows && col < this.columns;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public InventoryType getType() { return type; }
    public int getRows() { return rows; }
    public int getColumns() { return columns; }

    public boolean isCloseable() { return closeable; }
    public void setCloseable(boolean closeable) { this.closeable = closeable; }
    
    public int getUpdateFrequency() { return updateFrequency; }

    public InventoryProvider getProvider() { return provider; }
    public Optional<SmartInventory> getParent() { return Optional.ofNullable(parent); }

    //public InventoryManager getManager() { return InventoryManager; }

    List<InventoryListener<? extends Event>> getListeners() { return listeners; }

    
    
    
    
    
    
    
    public static Builder builder() { return new Builder(); }
    
    public static final class Builder {

        private String id = "unknown";
        private String title = "";
        private InventoryType type = InventoryType.CHEST;
        private Optional<Integer> rows = Optional.empty();
        private Optional<Integer> columns = Optional.empty();
        private boolean closeable = true;
        private int updateFrequency = 1;
        //private InventoryManager manager;
        private InventoryProvider provider;
        private SmartInventory parent;

        private final List<InventoryListener<? extends Event>> listeners = new ArrayList<>();

        private Builder() {}

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder type(InventoryType type) {
            this.type = type;
            return this;
        }

        public Builder size(final int rows) {
            this.rows = Optional.of(rows);// = rows;
            this.columns = Optional.of(9);// = 9;
            return this;
        }

        public Builder size(int rows, int columns) {
            this.rows = Optional.of(rows);
            this.columns = Optional.of(columns);
            return this;
        }

        public Builder closeable(boolean closeable) {
            this.closeable = closeable;
            return this;
        }
        
        /**
         * This method is used to configure the frequency at which the {@link InventoryProvider#update(Player, InventoryContent)}
         * method is called.Defaults to 1
         * @param frequency The inventory update frequency, in ticks
         * @return 
         * @throws IllegalArgumentException If frequency is smaller than 1.
         */
        public Builder updateFrequency(int frequency) {
            Preconditions.checkArgument(frequency > 0, "frequency must be > 0");
            this.updateFrequency = frequency;
            return this;
        }

        public Builder provider(InventoryProvider provider) {
            this.provider = provider;
            return this;
        }

        public Builder parent(SmartInventory parent) {
            this.parent = parent;
            return this;
        }

        public Builder listener(InventoryListener<? extends Event> listener) {
            this.listeners.add(listener);
            return this;
        }

        public Builder manager(InventoryManager manager) {
            //this.manager = manager;
            return this;
        }
        
        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public InventoryType getType() {
            return type;
        }

        public Optional<Integer> getRows() {
            return rows;
        }

        public Optional<Integer> getColumns() {
            return columns;
        }

        public boolean isCloseable() {
            return closeable;
        }

        public int getUpdateFrequency() {
            return updateFrequency;
        }

       // public InventoryManager getManager() {
      //      return InventoryManager;//manager;
      //  }

        public InventoryProvider getProvider() {
            return provider;
        }

        public SmartInventory getParent() {
            return parent;
        }

        public List<InventoryListener<? extends Event>> getListeners() {
            return Collections.unmodifiableList(listeners);
        }

        public SmartInventory build() {
            if(this.provider == null)
                throw new IllegalStateException("The provider of the SmartInventory.Builder must be set.");

            //if(this.manager == null) {          // if it's null, use the default instance
              //  this.manager = InventoryManager.get();//SmartInvsPlugin.manager();   
             //   if(this.manager == null) {      // if it's still null, throw an exception
             //       throw new IllegalStateException("Manager of the SmartInventory.Builder must be set, or SmartInvs should be loaded as a plugin.");
             //   }
           // }

            SmartInventory inv = new SmartInventory();
            inv.id = this.id;
            inv.title = this.title;
            inv.type = this.type;
            inv.rows = this.rows.orElseGet(() -> getDefaultDimensions(type).getRow());
            inv.columns = this.columns.orElseGet(() -> getDefaultDimensions(type).getColumn());
            inv.closeable = this.closeable;
            inv.updateFrequency = this.updateFrequency;
            inv.provider = this.provider;
            inv.parent = this.parent;
            inv.listeners = this.listeners;
            return inv;
        }

        private SlotPos getDefaultDimensions(InventoryType type) {
            //InventoryOpener opener = this.manager.findOpener(type).orElse(null);
            InventoryOpener opener = InventoryManager.findOpener(type).orElse(null);
            if(opener == null)
                throw new IllegalStateException("Cannot find InventoryOpener for type " + type);
            
            SlotPos size = opener.defaultSize(type);
            if(size == null)
                throw new IllegalStateException(String.format("%s returned null for input InventoryType %s", opener.getClass().getSimpleName(), type));
            
            return size;
        }

    }
    
    
    /*
    
    private String id;
    private String title;
    private InventoryType type;
    private int rows;
    private int columns;
    private SmartInventory parent;
    private InventoryProvider provider;
    
    private SmartInventory() {
    }
    
    public Inventory open(final Player player) {
        //if (!Bukkit.isPrimaryThread()) {
        //    Ostr
        //} else {
            return this.open(player, 0);
        //}
    }
    
    public Inventory open(final Player player, final int page) {
        final InventoryManager invManager = InventoryManager.get();
        invManager.getInventory(player).ifPresent( p2 -> invManager.setInventory(player, null) );
        provider.preInit(player);
        final InventoryContent inventoryContent = new InventoryContent(this, player);
        inventoryContent.pagination().page(page);
        invManager.setContents(player, inventoryContent);
        provider.init(player, inventoryContent);
        final Inventory open = invManager.findOpener(type).orElse(
            new ChestInventoryOpener()
        ).open(this, player);
        //final Object o;
        //final Inventory open = invManager.findOpener(this.type).orElseThrow(() -> {
        //    new IllegalStateException("No opener found for inventory type " + this.type.name());
        //    return null;
        //}).open(this, player);
        
        invManager.setInventory(player, this);
        return open;
    }
    
    public void close(final Player player) {
        final InventoryManager value = InventoryManager.get();
        value.setInventory(player, null);
        player.closeInventory();
        value.setContents(player, null);
    }
    
    public String getId() {
        return this.id;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public InventoryType getType() {
        return this.type;
    }
    
    public int getRows() {
        return this.rows;
    }
    
    public int getColumns() {
        return this.columns;
    }
    
    public InventoryProvider getProvider() {
        return this.provider;
    }
    
    public Optional<SmartInventory> getParent() {
        return Optional.ofNullable(this.parent);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        
        private String id;
        private String title;
        private InventoryType type;
        private int rows;
        private int columns;
        private InventoryProvider provider;
        private SmartInventory parent;
        
        private Builder() {
            this.id = "unknown";
            this.title = "unknown";
            this.type = InventoryType.CHEST;
            this.rows = 6;
            this.columns = 9;
        }
        
        public Builder id(final String id) {
            this.id = id;
            return this;
        }
        
        public Builder title(final String title) {
            this.title = title;
            return this;
        }

        public Builder type(final InventoryType type) {
            this.type = type;
            return this;
        }
        
        public Builder size(final int rows) {
            this.rows = rows;
            this.columns = 9;
            return this;
        }
        
        public Builder size(final int rows, final int columns) {
            this.rows = rows;
            this.columns = columns;
            return this;
        }
        
        public Builder provider(final InventoryProvider provider) {
            this.provider = provider;
            return this;
        }
        
        public Builder parent(final SmartInventory parent) {
            this.parent = parent;
            return this;
        }
        
        public SmartInventory build() {
            if (provider == null) {
                throw new IllegalStateException("The provider of the SmartInventory.Builder must be set.");
            }
            final SmartInventory smartInventory = new SmartInventory();
            smartInventory.id = id;
            smartInventory.title = title;
            smartInventory.type = type;
            smartInventory.rows = rows;
            smartInventory.columns = columns;
            smartInventory.provider = provider;
            smartInventory.parent = parent;
            return smartInventory;
        }
    }*/
}
