
package ru.komiss77.utils.inventory;

import java.util.Objects;

public class SlotPos {
    
    private final int row;
    private final int column;
    
    public SlotPos(final int row, final int column) {
        this.row = row;
        this.column = column;
    }
    
    public int getRow() {
        return this.row;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.column, this.row);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SlotPos)) {
            return false;
        }
        final SlotPos slotPos = (SlotPos)obj;
        return this.column == slotPos.column && this.row == slotPos.row;
    }
    
    public int getColumn() {
        return this.column;
    }
    
    public static SlotPos of(final int row, final int column) {
        return new SlotPos(row, column);
    }
    
    public static SlotPos of(final int slot) {
        final String string = Integer.toString(slot, 9);
        return new SlotPos((string.length() == 1) ? 0 : Integer.valueOf("" + string.charAt(0)), Integer.valueOf("" + string.charAt((int)((string.length() != 1) ? 1 : 0))));
    }
}
