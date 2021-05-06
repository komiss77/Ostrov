package ru.komiss77.utils.inventory;

import org.bukkit.inventory.meta.ItemMeta;
import java.util.Optional;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;

public class InventoryContent {
    
    private final Player holder;
    private final SmartInventory host;
    private final ClickableItem[][] contents;
    private final Inventory inventory;
    private final Pagination pagination;
    private final Map<String, Object> properties;
    private SlotIterator iterator;
    
    public InventoryContent(final SmartInventory inventory, final Player player) {
        this.pagination = new Pagination();
        this.properties = new HashMap();
        this.holder = player;
        this.host = inventory;
        if (this.host.getType() == InventoryType.CHEST || this.host.getType() == InventoryType.ENDER_CHEST) {
            this.inventory = Bukkit.createInventory((InventoryHolder)player, this.host.getColumns() * this.host.getRows(), this.host.getTitle());
        }
        else {
            this.inventory = Bukkit.createInventory((InventoryHolder)player, this.host.getType(), this.host.getTitle());
        }
        this.contents = new ClickableItem[this.host.getRows()][this.host.getColumns()];
    }
    
    public SmartInventory getHost() {
        return this.host;
    }
    
    public Pagination pagination() {
        return this.pagination;
    }
    
    public SlotIterator newIterator(final SlotIterator.Type type, final SlotPos startPos) {
        return this.iterator = new SlotIterator(this, this.host, type, startPos.getRow(), startPos.getColumn());
    }
    
    public Optional<SlotPos> firstEmpty() {
        for (int i = 0; i < this.contents[0].length; ++i) {
            for (int j = 0; j < this.contents.length; ++j) {
                if (!this.get(j, i).isPresent()) {
                    return Optional.of(new SlotPos(j, i));
                }
            }
        }
        return Optional.empty();
    }
    
    public ClickableItem[][] all() {
        return this.contents;
    }
    
    public Optional<ClickableItem> get(final int row, final int column) {
        if (row >= this.contents.length) {
            return Optional.empty();
        }
        if (column >= this.contents[row].length) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.contents[row][column]);
    }
    
    public Optional<ClickableItem> get(final SlotPos slotPos) {
        return this.get(slotPos.getRow(), slotPos.getColumn());
    }
    
    public InventoryContent set(final int row, final int column, final ClickableItem item) {
        if (row >= this.contents.length) {
            return this;
        }
        if (column >= this.contents[row].length) {
            return this;
        }
        this.contents[row][column] = item;
        this.update(row, column, (item != null) ? item : null);
        return this;
    }
    
    public InventoryContent set(final SlotPos slotPos, final ClickableItem item) {
        return this.set(slotPos.getRow(), slotPos.getColumn(), item);
    }
    
    public InventoryContent set(final int slot, final ClickableItem item) {
        return this.set(SlotPos.of(slot), item);
    }
    
    public InventoryContent add(final ClickableItem item) {
        for (int i = 0; i < this.contents.length; ++i) {
            for (int j = 0; j < this.contents[0].length; ++j) {
                if (this.contents[i][j] == null) {
                    this.set(i, j, item);
                    return this;
                }
            }
        }
        return this;
    }
    
    public InventoryContent fill(final ClickableItem item) {
        for (int i = 0; i < this.contents.length; ++i) {
            for (int j = 0; j < this.contents[i].length; ++j) {
                this.set(i, j, item);
            }
        }
        return this;
    }
    
    public InventoryContent fillRow(final int row, final ClickableItem item) {
        if (row >= this.contents.length) {
            return this;
        }
        for (int i = 0; i < this.contents[row].length; ++i) {
            this.set(row, i, item);
        }
        return this;
    }
    
    public InventoryContent fillColumn(final int column, final ClickableItem item) {
        for (int i = 0; i < this.contents.length; ++i) {
            this.set(i, column, item);
        }
        return this;
    }
    
    public InventoryContent fillBorders(final ClickableItem item) {
        this.fillRect(0, 0, this.host.getRows() - 1, this.host.getColumns() - 1, item);
        return this;
    }
    
    public InventoryContent fillRect(final int fromRow, final int fromColumn, final int toRow, final int toColumn, final ClickableItem item) {
        for (int i = fromRow; i <= toRow; ++i) {
            for (int j = fromColumn; j <= toColumn; ++j) {
                if (i == fromRow || i == toRow || j == fromColumn || j == toColumn) {
                    this.set(i, j, item);
                }
            }
        }
        return this;
    }
    
    public InventoryContent fillRect(final SlotPos fromPos, final SlotPos toPos, final ClickableItem item) {
        return this.fillRect(fromPos.getRow(), fromPos.getColumn(), toPos.getRow(), toPos.getColumn(), item);
    }
    
    public <T> T property(final String name) {
        return (T)this.properties.get(name);
    }
    
    public <T> T property(final String name, final T def) {
        return (T)(this.properties.containsKey(name) ? this.properties.get(name) : def);
    }
    
    public Map<String, Object> properties() {
        return this.properties;
    }
    
    public InventoryContent setProperty(final String name, final Object value) {
        this.properties.put(name, value);
        return this;
    }
    
    private void update(final int row, final int column, final ClickableItem item) {
        if (item == null || item.getItem() != null) {
            return;
        }
        this.inventory.setItem(this.host.getColumns() * row + column, item.getItem());
    }
    
    public InventoryContent updateMeta(final SlotPos pos, final ItemMeta meta) {
        this.inventory.getItem(this.host.getColumns() * pos.getRow() + pos.getColumn()).setItemMeta(meta);
        return this;
    }
    
    public Player getHolder() {
        return this.holder;
    }
    
    public Inventory getInventory() {
        return this.inventory;
    }
}
