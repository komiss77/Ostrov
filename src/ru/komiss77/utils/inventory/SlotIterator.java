package ru.komiss77.utils.inventory;

import java.util.Optional;
import java.util.HashSet;
import java.util.Set;

public class SlotIterator {
    
    private final InventoryContent content;
    private final SmartInventory inventory;
    private final Type type;
    private boolean started;
    private boolean allowOverride;
    private int row;
    private int column;
    private final Set<SlotPos> blacklisted;
    
    public SlotIterator(final InventoryContent content, final SmartInventory inventory, final Type type, final int startRow, final int startColumn) {
        this.started = false;
        this.allowOverride = true;
        this.blacklisted = new HashSet<>();
        this.content = content;
        this.inventory = inventory;
        this.type = type;
        this.row = startRow;
        this.column = startColumn;
    }
    
    public SlotIterator(final InventoryContent content, final SmartInventory inventory, final Type type) {
        this(content, inventory, type, 0, 0);
    }
    
    public Optional<ClickableItem> get() {
        return this.content.get(this.row, this.column);
    }
    
    public SlotIterator set(final ClickableItem item) {
        if (this.canPlace()) {
            this.content.set(this.row, this.column, item);
        }
        return this;
    }
    
    public SlotIterator previous() {
        if (this.row == 0 && this.column == 0) {
            this.started = true;
            return this;
        }
        do {
            if (!this.started) {
                this.started = true;
            }
            else {
                switch (this.type) {
                    case HORIZONTAL: {
                        --this.column;
                        if (this.column == 0) {
                            this.column = this.inventory.getColumns() - 1;
                            --this.row;
                            continue;
                        }
                        continue;
                    }
                    case VERTICAL: {
                        --this.row;
                        if (this.row == 0) {
                            this.row = this.inventory.getRows() - 1;
                            --this.column;
                            continue;
                        }
                        continue;
                    }
                    default: {
                        continue;
                    }
                }
            }
        } while (!this.canPlace() && (this.row != 0 || this.column != 0));
        return this;
    }
    
    public SlotIterator next() {
        if (this.ended()) {
            this.started = true;
            return this;
        }
        do {
            if (!this.started) {
                this.started = true;
            }
            else {
                switch (this.type) {
                    case HORIZONTAL: {
                        this.column = ++this.column % this.inventory.getColumns();
                        if (this.column == 0) {
                            ++this.row;
                            continue;
                        }
                        continue;
                    }
                    case VERTICAL: {
                        this.row = ++this.row % this.inventory.getRows();
                        if (this.row == 0) {
                            ++this.column;
                            continue;
                        }
                        continue;
                    }
                    default: {
                        continue;
                    }
                }
            }
        } while (!this.canPlace() && !this.ended());
        return this;
    }
    
    public SlotIterator blacklist(final int row, final int column) {
        this.blacklisted.add(SlotPos.of(row, column));
        return this;
    }
    
    public SlotIterator blacklist(final SlotPos slotPos) {
        return this.blacklist(slotPos.getRow(), slotPos.getColumn());
    }
    
    public int row() {
        return this.row;
    }
    
    public SlotIterator row(final int row) {
        this.row = row;
        return this;
    }
    
    public int column() {
        return this.column;
    }
    
    public SlotIterator column(final int column) {
        this.column = column;
        return this;
    }
    
    public boolean started() {
        return this.started;
    }
    
    public boolean ended() {
        return this.row == this.inventory.getRows() - 1 && this.column == this.inventory.getColumns() - 1;
    }
    
    public boolean doesAllowOverride() {
        return this.allowOverride;
    }
    
    public SlotIterator allowOverride(final boolean override) {
        this.allowOverride = override;
        return this;
    }
    
    private boolean canPlace() {
        return !this.blacklisted.contains(SlotPos.of(this.row, this.column)) && (this.allowOverride || !this.get().isPresent());
    }
    
    public enum Type
    {
        HORIZONTAL, 
        VERTICAL;
    }
}
