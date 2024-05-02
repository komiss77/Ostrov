package ru.komiss77.utils.inventory;



public class SlotPos {

    private final int row;
    private final int column;

    public SlotPos(int row, int column) {
        this.row = row;
        this.column = column;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null || getClass() != obj.getClass())
            return false;

        SlotPos slotPos = (SlotPos) obj;

        return row == slotPos.row && column == slotPos.column;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + column;

        return result;
    }

    @Override
    public String toString() {
        return "SlotPos{" +
                "row=" + row +
                ", column=" + column +
                '}';
    }

    public int getRow() { return row; }
    public int getColumn() { return column; }

    public static SlotPos of(int row, int column) {
        return new SlotPos(row, column);
    }
    
    public static SlotPos of(final int slot) {
        final String string = Integer.toString(slot, 9);
        return new SlotPos((string.length() == 1) ? 0 : Integer.valueOf("" + string.charAt(0)), Integer.valueOf("" + string.charAt((int)((string.length() != 1) ? 1 : 0))));
    }

}

