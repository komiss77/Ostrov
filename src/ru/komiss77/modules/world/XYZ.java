package ru.komiss77.modules.world;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import ru.komiss77.Ostrov;
import ru.komiss77.notes.Slow;
import ru.komiss77.utils.NumUtil;


public class XYZ implements Cloneable {

    public String worldName;
    public int x;
    public int y;
    public int z;

    //доп.поля
    @Deprecated //где это нужно?
    public BlockFace bf;

    public int yaw;
    public int pitch;

    public XYZ() {}

    public XYZ(final Location loc) {
        worldName = loc.getWorld().getName();
        x = loc.getBlockX();
        y = loc.getBlockY();
        z = loc.getBlockZ();
    }

    public XYZ(final String worldName, final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
    }

    @Slow(priority = 1)
    public static XYZ fromString(final String asString) {
        try {
            final String[] split = asString.split(",");
            if (split.length == 3) {
                return new XYZ("", Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            } else if (split.length > 3) {
                return new XYZ(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
            }
        } catch (NullPointerException | NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            Ostrov.log_err("XYZ fromString  =" + asString + " " + ex.getMessage());
        }
        return null;
    }

    public static XYZ of(long packedPos) {
        return new XYZ(null, (int) (packedPos >> 38), (int) ((packedPos << 52) >> 52), (int) ((packedPos << 26) >> 38)); // Paper - simplify/inline
    }

    public void w(final World w) {
        this.worldName = w.getName();
    }

    public int distSq(final Location to) {
        return NumUtil.square(to.getBlockX() - x) + NumUtil.square(to.getBlockY() - y) + NumUtil.square(to.getBlockZ() - z);
    }

    public int distSq(final XYZ to) {
        return NumUtil.square(x - to.x) + NumUtil.square(y - to.y) + NumUtil.square(z - to.z);
    }

    public int distAbs(final Location to) {
        return NumUtil.abs(to.getBlockX() - x) + NumUtil.abs(to.getBlockY() - y) + NumUtil.abs(to.getBlockZ() - z);
    }

    public int distAbs(final XYZ to) {
        return NumUtil.abs(x - to.x) + NumUtil.abs(y - to.y) + NumUtil.abs(z - to.z);
    }

    public int distAprx(final Location to) {
        return NumUtil.sqrt(distSq(to));
    }

    public int distAprx(final XYZ to) {
        return NumUtil.sqrt(distSq(to));
    }


    public boolean nearly(final Location loc, final int distance) { //проверить - точка в радиусе distance?
        return worldName.equals(loc.getWorld().getName()) && XYZ.this.distSq(loc) <= distance; //число подобрать точнее!
    }

    public Location getCenterLoc() {
        return getCenterLoc(Bukkit.getWorld(worldName));
    }

    public Location getCenterLoc(final World w) {
        return new Location(w, (double) x + .5d, (double) y + .5d, (double) z + .5d);
    }

    public XYZ add(final int x, final int y, final int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public XYZ add(final XYZ val) {
        return add(val.x, val.y, val.z);
    }

    public XYZ times(final int m) {
        this.x *= m;
        this.y *= m;
        this.z *= m;
        return this;
    }


    @Override
    public String toString() {
        return (worldName == null ? "" : worldName + ",") + x + "," + y + "," + z;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final XYZ xyz)) return false;
        return Objects.equals(xyz.worldName, worldName)
            && xyz.x == x && xyz.y == y && xyz.z == z;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(asLong());
    }

    /*@Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof final XYZ compare)) return false;
        return ((compare.worldName == null && worldName == null) || compare.worldName.hashCode() == worldName.hashCode()) //nullpointer
            && compare.x == x && compare.y == y && compare.z == z;
    }

    @Override
    public int hashCode() {
        long l = asLong();
        if (worldName == null) {
            return Long.hashCode(l);//return toString().hashCode();
        } else {
            return Long.hashCode(l) ^ worldName.hashCode();
        }
    }*/

    @Override
    public XYZ clone() {
        final XYZ cln;
        try {
            cln = (XYZ) super.clone();
//          cln.x = x; cln.y = y; cln.z = z;
            cln.worldName = worldName;
            return cln;
        } catch (CloneNotSupportedException e) {
            return new XYZ(worldName, x, y, z);
        }
    }

    public int getSLoc() { //координата в одном int для небольших значений, работает с '-'
        return y >> 31 << 30 ^ x >> 31 << 29 ^ z >> 31 << 28 ^ y << 20 ^ x << 10 ^ z;
    }

    public int offSet() { //координата кубоиде, используется в схематике. НЕ могут быть '-' !!
        return x << 20 | y << 10 | z;
    }

    public long asLong() {
        return (((long) x & (long) 67108863) << 38) | (((long) y & (long) 4095)) | (((long) z & (long) 67108863) << 12);
    }
}
