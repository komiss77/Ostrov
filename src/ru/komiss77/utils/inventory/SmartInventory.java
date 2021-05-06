package ru.komiss77.utils.inventory;

import java.util.Optional;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public class SmartInventory {
    
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
        return this.open(player, 0);
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
        /**
         * @param type  
         * https://wiki.vg/Inventory  - нумерация слотов
         * @return       
        */
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
    }
}
