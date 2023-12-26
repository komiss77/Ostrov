package ru.komiss77.utils.inventory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;



public interface InventoryContent {


    //SmartInventory inventory();
    SmartInventory getHost();
    
    Inventory getInventory();
    
    Pagination pagination();

    Optional<SlotIterator> iterator(String id);

    SlotIterator newIterator(String id, SlotIterator.Type type, int startRow, int startColumn);

    SlotIterator newIterator(SlotIterator.Type type, int startRow, int startColumn);

    SlotIterator newIterator(String id, SlotIterator.Type type, SlotPos startPos);

    SlotIterator newIterator(SlotIterator.Type type, SlotPos startPos);

    ClickableItem[][] all();

    List<SlotPos> slots();

    Optional<SlotPos> firstEmpty();

    Optional<ClickableItem> get(int index);

    Optional<ClickableItem> get(int row, int column);

    Optional<ClickableItem> get(SlotPos slotPos);

    InventoryContent applyRect(int fromRow, int fromColumn, int toRow, int toColumn, BiConsumer<Integer, Integer> apply);

    InventoryContent applyRect(int fromRow, int fromColumn, int toRow, int toColumn, Consumer<ClickableItem> apply);

    InventoryContent set(int index, ClickableItem item);

    InventoryContent set(int row, int column, ClickableItem item);

    InventoryContent set(SlotPos slotPos, ClickableItem item);

    InventoryContent add(ClickableItem item);

    InventoryContent updateItem(int index, ItemStack itemStack);

    InventoryContent updateItem(int row, int column, ItemStack itemStack);

    InventoryContent updateItem(SlotPos slotPos, ItemStack itemStack);

    Optional<SlotPos> findItem(ItemStack item);

    Optional<SlotPos> findItem(ClickableItem item);

    InventoryContent fill(ClickableItem item);

    InventoryContent fillRow(int row, ClickableItem item);

    InventoryContent fillColumn(int column, ClickableItem item);

    InventoryContent fillBorders(ClickableItem item);

    InventoryContent fillRect(int fromIndex, int toIndex, ClickableItem item);

    InventoryContent fillRect(int fromRow, int fromColumn, int toRow, int toColumn, ClickableItem item);

    InventoryContent fillRect(SlotPos fromPos, SlotPos toPos, ClickableItem item);

    InventoryContent fillSquare(int fromIndex, int toIndex, ClickableItem item);

    InventoryContent fillSquare(int fromRow, int fromColumn, int toRow, int toColumn, ClickableItem item);

    InventoryContent fillSquare(SlotPos fromPos, SlotPos toPos, ClickableItem item);

    InventoryContent fillPattern(Pattern<ClickableItem> pattern);

    InventoryContent fillPattern(Pattern<ClickableItem> pattern, int startIndex);

    InventoryContent fillPattern(Pattern<ClickableItem> pattern, int startRow, int startColumn);

    InventoryContent fillPattern(Pattern<ClickableItem> pattern, SlotPos startPos);

    InventoryContent fillPatternRepeating(Pattern<ClickableItem> pattern);

    InventoryContent fillPatternRepeating(Pattern<ClickableItem> pattern, int startIndex, int endIndex);

    InventoryContent fillPatternRepeating(Pattern<ClickableItem> pattern, int startRow, int startColumn, int endRow, int endColumn);

    InventoryContent fillPatternRepeating(Pattern<ClickableItem> pattern, SlotPos startPos, SlotPos endPos);

    <T> T property(String name);

    <T> T property(String name, T def);

    InventoryContent setProperty(String name, Object value);

    void setEditable(SlotPos slot, boolean editable);

    boolean isEditable(SlotPos slot);


    
    
    
    
    
    class Impl implements InventoryContent{

        private final SmartInventory inv;
        private final Player player;
        private final ClickableItem[][] contents;
        private final Pagination pagination = new Pagination.Impl();
        private final Map<String, SlotIterator> iterators = new HashMap<>();
        private final Map<String, Object> properties = new HashMap<>();
        private final Set<SlotPos> editableSlots = new HashSet<>();

        public Impl(SmartInventory inv, Player player) {
            this.inv = inv;
            this.player = player;
            this.contents = new ClickableItem[inv.getRows()][inv.getColumns()];
        }


        @Override
        @Deprecated
        public Inventory getInventory() {
            //if(!inv.getManager().getOpenedPlayers(inv).contains(player)) return;
            return inv.handle;//player.getOpenInventory().getTopInventory();
        }

        @Override
        public SmartInventory getHost() {
            return inv;
        }

        @Override
        public Pagination pagination() {
            return pagination;
        }

        @Override
        public Optional<SlotIterator> iterator(String id) {
            return Optional.ofNullable(this.iterators.get(id));
        }

        @Override
        public SlotIterator newIterator(String id, SlotIterator.Type type, int startRow, int startColumn) {
            SlotIterator iterator = new SlotIterator.Impl(this, inv,
                    type, startRow, startColumn);

            this.iterators.put(id, iterator);
            return iterator;
        }

        @Override
        public SlotIterator newIterator(String id, SlotIterator.Type type, SlotPos startPos) {
            return newIterator(id, type, startPos.getRow(), startPos.getColumn());
        }

        @Override
        public SlotIterator newIterator(SlotIterator.Type type, int startRow, int startColumn) {
            return new SlotIterator.Impl(this, inv, type, startRow, startColumn);
        }

        @Override
        public SlotIterator newIterator(SlotIterator.Type type, SlotPos startPos) {
            return newIterator(type, startPos.getRow(), startPos.getColumn());
        }

        @Override
        public ClickableItem[][] all() {
            return contents;
        }

        @Override
        public List<SlotPos> slots() {
            List<SlotPos> slotPos = new ArrayList<>();
            for (int row = 0; row < contents.length; row++) {
                for(int column = 0; column < contents[0].length; column++) {
                    slotPos.add(SlotPos.of(row, column));
                }
            }
            return slotPos;
        }

        @Override
        public Optional<SlotPos> firstEmpty() {
            for(int row = 0; row < contents.length; row++) {
                for(int column = 0; column < contents[0].length; column++) {
                    if(!this.get(row, column).isPresent())
                        return Optional.of(new SlotPos(row, column));
                }
            }

            return Optional.empty();
        }

        @Override
        public Optional<ClickableItem> get(int index) {
            int columnCount = this.inv.getColumns();

            return get(index / columnCount, index % columnCount);
        }

        @Override
        public Optional<ClickableItem> get(int row, int column) {
            if(row < 0 || row >= contents.length)
                return Optional.empty();
            if(column < 0 || column >= contents[row].length)
                return Optional.empty();

            return Optional.ofNullable(contents[row][column]);
        }

        @Override
        public Optional<ClickableItem> get(SlotPos slotPos) {
            return get(slotPos.getRow(), slotPos.getColumn());
        }

        @Override
        public InventoryContent applyRect(int fromRow, int fromColumn, int toRow, int toColumn, BiConsumer<Integer, Integer> apply) {
            for(int row = fromRow; row <= toRow; row++) {
                for(int column = fromColumn; column <= toColumn; column++) {
                    apply.accept(row, column);
                }
            }

            return this;
        }

        @Override
        public InventoryContent applyRect(int fromRow, int fromColumn, int toRow, int toColumn, Consumer<ClickableItem> apply) {
            applyRect(fromRow, fromColumn, toRow, toColumn, (row, column) -> get(row, column).ifPresent(apply));
            return this;
        }

        @Override
        public InventoryContent set(int index, ClickableItem item) {
            int columnCount = this.inv.getColumns();

            return set(index / columnCount, index % columnCount, item);
        }

        @Override
        public InventoryContent set(int row, int column, ClickableItem item) {
            if(row < 0 || row >= contents.length)
                return this;
            if(column < 0 || column >= contents[row].length)
                return this;

            contents[row][column] = item;
            update(row, column, item == null ? null : item.getItem(player));
            return this;
        }

        @Override
        public InventoryContent set(SlotPos slotPos, ClickableItem item) {
            return set(slotPos.getRow(), slotPos.getColumn(), item);
        }

        @Override
        public InventoryContent add(ClickableItem item) {
            for(int row = 0; row < contents.length; row++) {
                for(int column = 0; column < contents[0].length; column++) {
                    if(contents[row][column] == null) {
                        set(row, column, item);
                        return this;
                    }
                }
            }

            return this;
        }

        @Override
        public InventoryContent updateItem(int index, ItemStack itemStack) {
            int columnCount = this.inv.getColumns();

            return updateItem(index / columnCount, index % columnCount, itemStack);
        }

        @Override
        public InventoryContent updateItem(int row, int column, ItemStack itemStack) {
            Optional<ClickableItem> optional = get(row, column);

            if (!optional.isPresent()) {
                set(row, column, ClickableItem.empty(itemStack));
                return this;
            }

            ClickableItem newClickableItem = optional.get().cloneWithNewItem(itemStack);
            set(row, column, newClickableItem);
            return this;
        }

        @Override
        public InventoryContent updateItem(SlotPos slotPos, ItemStack itemStack) {
            return updateItem(slotPos.getRow(), slotPos.getColumn(), itemStack);
        }

        @Override
        public Optional<SlotPos> findItem(ItemStack itemStack) {
            Preconditions.checkNotNull(itemStack, "The itemstack to look for cannot be null!");
            for(int row = 0; row < contents.length; row++) {
                for(int column = 0; column < contents[0].length; column++) {
                    if(contents[row][column] != null &&
                            itemStack.isSimilar(contents[row][column].getItem(this.player))) {
                        return Optional.of(SlotPos.of(row, column));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public Optional<SlotPos> findItem(ClickableItem clickableItem) {
            Preconditions.checkNotNull(clickableItem, "The clickable item to look for cannot be null!");
            return findItem(clickableItem.getItem(this.player));
        }

        @Override
        public InventoryContent fill(ClickableItem item) {
            for(int row = 0; row < contents.length; row++)
                for(int column = 0; column < contents[row].length; column++)
                    set(row, column, item);

            return this;
        }

        @Override
        public InventoryContent fillRow(int row, ClickableItem item) {
            if(row < 0 || row >= contents.length)
                return this;

            for(int column = 0; column < contents[row].length; column++)
                set(row, column, item);

            return this;
        }

        @Override
        public InventoryContent fillColumn(int column, ClickableItem item) {
            if(column < 0 || column >= contents[0].length)
                return this;

            for(int row = 0; row < contents.length; row++)
                set(row, column, item);

            return this;
        }

        @Override
        public InventoryContent fillBorders(ClickableItem item) {
            fillRect(0, 0, inv.getRows() - 1, inv.getColumns() - 1, item);
            return this;
        }

        @Override
        public InventoryContent fillRect(int fromIndex, int toIndex, ClickableItem item) {
            int columnCount = this.inv.getColumns();

            return fillRect(
                    fromIndex / columnCount, fromIndex % columnCount,
                    toIndex / columnCount, toIndex % columnCount,
                    item
            );
        }

        @Override
        public InventoryContent fillRect(int fromRow, int fromColumn, int toRow, int toColumn, ClickableItem item) {
            applyRect(fromRow, fromColumn, toRow, toColumn, (row, column) -> {
                if(row != fromRow && row != toRow && column != fromColumn && column != toColumn)
                    return;

                set(row, column, item);
            });

            return this;
        }

        @Override
        public InventoryContent fillRect(SlotPos fromPos, SlotPos toPos, ClickableItem item) {
            return fillRect(fromPos.getRow(), fromPos.getColumn(), toPos.getRow(), toPos.getColumn(), item);
        }

        @Override
        public InventoryContent fillSquare(int fromIndex, int toIndex, ClickableItem item) {
            int columnCount = this.inv.getColumns();

            return fillSquare(
                    fromIndex / columnCount, fromIndex % columnCount,
                    toIndex / columnCount, toIndex % columnCount,
                    item
            );
        }

        @Override
        public InventoryContent fillSquare(int fromRow, int fromColumn, int toRow, int toColumn, ClickableItem item) {
            Preconditions.checkArgument(fromRow < toRow, "The start row needs to be lower than the end row");
            Preconditions.checkArgument(fromColumn < toColumn, "The start column needs to be lower than the end column");

            for(int row = fromRow; row <= toRow; row++) {
                for(int column = fromColumn; column <= toColumn; column++) {
                    set(row, column, item);
                }
            }
            return this;
        }

        @Override
        public InventoryContent fillSquare(SlotPos fromPos, SlotPos toPos, ClickableItem item) {
            return fillSquare(fromPos.getRow(), fromPos.getColumn(), toPos.getRow(), toPos.getColumn(), item);
        }

        @Override
        public InventoryContent fillPattern(Pattern<ClickableItem> pattern) {
            return fillPattern(pattern, 0, 0);
        }

        @Override
        public InventoryContent fillPattern(Pattern<ClickableItem> pattern, int startIndex) {
            int columnCount = this.inv.getColumns();

            return fillPattern(pattern, startIndex / columnCount, startIndex % columnCount);
        }

        @Override
        public InventoryContent fillPattern(Pattern<ClickableItem> pattern, SlotPos startPos) {
            return fillPattern(pattern, startPos.getRow(), startPos.getColumn());
        }

        @Override
        public InventoryContent fillPatternRepeating(Pattern<ClickableItem> pattern) {
            return fillPatternRepeating(pattern, 0, 0, -1, -1);
        }

        @Override
        public InventoryContent fillPatternRepeating(Pattern<ClickableItem> pattern, int startIndex, int endIndex) {
            int columnCount = this.inv.getColumns();
            boolean maxSize = endIndex < 0;

            return fillPatternRepeating(pattern, startIndex / columnCount, startIndex % columnCount, (maxSize ? -1 : endIndex / columnCount), (maxSize ? -1 : endIndex % columnCount));
        }

        @Override
        public InventoryContent fillPatternRepeating(Pattern<ClickableItem> pattern, int startRow, int startColumn, int endRow, int endColumn) {
            Preconditions.checkArgument(pattern.isWrapAround(), "To fill in a repeating pattern wrapAround needs to be enabled for the pattern to work!");

            if(endRow < 0)
                endRow = this.inv.getRows();
            if(endColumn < 0)
                endColumn = this.inv.getColumns();

            Preconditions.checkArgument(startRow < endRow, "The start row needs to be lower than the end row");
            Preconditions.checkArgument(startColumn < endColumn, "The start column needs to be lower than the end column");

            int rowDelta = endRow - startRow, columnDelta = endColumn - startColumn;
            for(int row = 0; row <= rowDelta; row++) {
                for(int column = 0; column <= columnDelta; column++) {
                    ClickableItem item = pattern.getObject(row, column);

                    if(item != null)
                        set(startRow + row, startColumn + column, item);
                }
            }
            return this;
        }

        @Override
        public InventoryContent fillPatternRepeating(Pattern<ClickableItem> pattern, SlotPos startPos, SlotPos endPos) {
            return fillPatternRepeating(pattern, startPos.getRow(), startPos.getColumn(), endPos.getRow(), endPos.getColumn());
        }

        @Override
        public InventoryContent fillPattern(Pattern<ClickableItem> pattern, int startRow, int startColumn) {
            for(int row = 0; row < pattern.getRowCount(); row++) {
                for(int column = 0; column < pattern.getColumnCount(); column++) {
                    ClickableItem item = pattern.getObject(row, column);

                    if(item != null)
                        set(startRow + row, startColumn + column, item);
                }
            }

            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T property(String name) {
            return (T) properties.get(name);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T property(String name, T def) {
            return properties.containsKey(name) ? (T) properties.get(name) : def;
        }

        @Override
        public InventoryContent setProperty(String name, Object value) {
            properties.put(name, value);
            return this;
        }

        private void update(int row, int column, ItemStack item) {
            if(!InventoryManager.getOpenedPlayers(inv).contains(player))
                return;

            Inventory topInventory = player.getOpenInventory().getTopInventory();
            topInventory.setItem(inv.getColumns() * row + column, item);
        }

        @Override
        public void setEditable(SlotPos slot, boolean editable) {
            if(editable)
                editableSlots.add(slot);
            else
                editableSlots.remove(slot);
        }

        @Override
        public boolean isEditable(SlotPos slot) {
            return editableSlots.contains(slot);
        }


    }

}


/*
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Optional;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;

public class InventoryContent_ {
    
    private final Player holder;
    private final SmartInventory host;
    private final ClickableItem[][] contents;
    private final Inventory inventory;
    private final Pagination pagination;
    private final Map<String, Object> properties;
    private SlotIterator iterator;
    
    public InventoryContent_(final SmartInventory inventory, final Player player) {
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
    
    public InventoryContent_ set(final int row, final int column, final ClickableItem item) {
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
    
    public InventoryContent_ set(final SlotPos slotPos, final ClickableItem item) {
        return set(slotPos.getRow(), slotPos.getColumn(), item);
    }
    
    public InventoryContent_ set(final int slot, final ClickableItem item) {
        return set(SlotPos.of(slot), item);
    }
    
    public InventoryContent_ add(final ClickableItem item) {
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
    
    public InventoryContent_ fill(final ClickableItem item) {
        for (int i = 0; i < contents.length; ++i) {
            for (int j = 0; j < contents[i].length; ++j) {
                set(i, j, item);
            }
        }
        return this;
    }
    
    public InventoryContent_ fillRow(final int row, final ClickableItem item) {
        if (row >= contents.length) {
            return this;
        }
        for (int i = 0; i < contents[row].length; ++i) {
            set(row, i, item);
        }
        return this;
    }
    
    public InventoryContent_ fillColumn(final int column, final ClickableItem item) {
        for (int i = 0; i < contents.length; ++i) {
            set(i, column, item);
        }
        return this;
    }
    
    public InventoryContent_ fillBorders(final ClickableItem item) {
        fillRect(0, 0, host.getRows() - 1, host.getColumns() - 1, item);
        return this;
    }
    
    public InventoryContent_ fillRect(final int fromRow, final int fromColumn, final int toRow, final int toColumn, final ClickableItem item) {
        for (int i = fromRow; i <= toRow; ++i) {
            for (int j = fromColumn; j <= toColumn; ++j) {
                if (i == fromRow || i == toRow || j == fromColumn || j == toColumn) {
                    this.set(i, j, item);
                }
            }
        }
        return this;
    }
    
    public InventoryContent_ fillRect(final SlotPos fromPos, final SlotPos toPos, final ClickableItem item) {
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
    
    public InventoryContent_ setProperty(final String name, final Object value) {
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
    
    public InventoryContent_ updateMeta(final SlotPos pos, final ItemMeta meta) {
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
    }/
    
    public Player getHolder() {
        return holder;
    }
    
    public Inventory getInventory() {
        return inventory;
    }
}
*/