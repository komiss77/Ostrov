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
        return host;
    }
    
    public Pagination pagination() {
        return pagination;
    }
    
    public SlotIterator newIterator(final SlotIterator.Type type, final SlotPos startPos) {
        return iterator = new SlotIterator(this, host, type, startPos.getRow(), startPos.getColumn());
    }
    
    public Optional<SlotPos> firstEmpty() {
        for (int i = 0; i < contents[0].length; ++i) {
            for (int j = 0; j < contents.length; ++j) {
                if (!this.get(j, i).isPresent()) {
                    return Optional.of(new SlotPos(j, i));
                }
            }
        }
        return Optional.empty();
    }
    
    public ClickableItem[][] all() {
        return contents;
    }
    
    public Optional<ClickableItem> get(final int row, final int column) {
        if (row >= contents.length) {
            return Optional.empty();
        }
        if (column >= contents[row].length) {
            return Optional.empty();
        }
        return Optional.ofNullable(contents[row][column]);
    }
    
    public Optional<ClickableItem> get(final SlotPos slotPos) {
        return get(slotPos.getRow(), slotPos.getColumn());
    }
    
    public InventoryContent set(final int row, final int column, final ClickableItem item) {
//System.out.println(">>>>>> set "+row+" "+column);
        if (row >= contents.length) {
            return this;
        }
//System.out.println("1");
        if (column >= contents[row].length) {
            return this;
        }
//System.out.println("2");
        contents[row][column] = item;
        update(row, column, (item != null) ? item : null);
        return this;
    }
    
    public InventoryContent set(final SlotPos slotPos, final ClickableItem item) {
        return set(slotPos.getRow(), slotPos.getColumn(), item);
    }
    
    public InventoryContent set(final int slot, final ClickableItem item) {
        return set(SlotPos.of(slot), item);
    }
    
    public InventoryContent add(final ClickableItem item) {
        for (int i = 0; i < contents.length; ++i) {
            for (int j = 0; j < contents[0].length; ++j) {
                if (this.contents[i][j] == null) {
                    set(i, j, item);
                    return this;
                }
            }
        }
        return this;
    }
    
    public InventoryContent fill(final ClickableItem item) {
        for (int i = 0; i < contents.length; ++i) {
            for (int j = 0; j < contents[i].length; ++j) {
                set(i, j, item);
            }
        }
        return this;
    }
    
    public InventoryContent fillRow(final int row, final ClickableItem item) {
        if (row >= contents.length) {
            return this;
        }
        for (int i = 0; i < contents[row].length; ++i) {
            set(row, i, item);
        }
        return this;
    }
    
    public InventoryContent fillColumn(final int column, final ClickableItem item) {
        for (int i = 0; i < contents.length; ++i) {
            set(i, column, item);
        }
        return this;
    }
    
    public InventoryContent fillBorders(final ClickableItem item) {
        fillRect(0, 0, host.getRows() - 1, host.getColumns() - 1, item);
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
        return fillRect(fromPos.getRow(), fromPos.getColumn(), toPos.getRow(), toPos.getColumn(), item);
    }
    
    public <T> T property(final String name) {
        return (T)properties.get(name);
    }
    
    public <T> T property(final String name, final T def) {
        return (T)(properties.containsKey(name) ? properties.get(name) : def);
    }
    
    public Map<String, Object> properties() {
        return properties;
    }
    
    public InventoryContent setProperty(final String name, final Object value) {
        properties.put(name, value);
        return this;
    }
    
    private void update(final int row, final int column, final ClickableItem item) {
//System.out.println(">>>>>> update setItem "+(host.getColumns() * row + column)+" item="+item);
//System.out.println("is="+item.getItem());
       // if (item == null || item.getItem() != null) { ?? ошибка ??
        if (item == null || item.getItem() == null) {
            return;
        }
        inventory.setItem(host.getColumns() * row + column, item.getItem());
    }
    
    public InventoryContent updateMeta(final SlotPos pos, final ItemMeta meta) {
        inventory.getItem(host.getColumns() * pos.getRow() + pos.getColumn()).setItemMeta(meta);
        return this;
    }
    /*кинуло размер
    public InventoryContent setLore (final int row, final int column, final List<String> lore) {
        final ItemStack is = inventory.getItem(host.getColumns() * row + column);
        if (is==null || !is.hasItemMeta()) return this;
        final ItemMeta im = is.getItemMeta();
        im.setLore(lore);
        is.setItemMeta(im);
        return this;
    }*/
    
    public Player getHolder() {
        return holder;
    }
    
    public Inventory getInventory() {
        return inventory;
    }
}
