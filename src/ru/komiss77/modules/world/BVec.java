package ru.komiss77.modules.world;

import javax.annotation.Nullable;
import java.util.Objects;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.StringUtil;

public class BVec implements BlockPosition, Cloneable {

    public static final String SPLIT = StringUtil.SPLIT_1;

    private static final byte[] EIA = {};

    public int x, y, z;

    protected BVec(final int x, final int y, final int z) {
        this.x = x; this.y = y; this.z = z;
    }

    public int blockX() {return x;}
    public int blockY() {return y;}
    public int blockZ() {return z;}

    public @Nullable World w() {return null;}
    public @Nullable String wname() {return null;}
    public BVec w(final String wname) {
        return of(wname, x, y, z);
    }
    public BVec w(final World w) {
        return of(w, x, y, z);
    }

    public byte[] vals() {return EIA;}
    public BVec wals(final World w, final byte[] vals) {
        return of(w, x, y, z, vals);
    }

    public BVec add(final int x, final int y, final int z) {
        return of(this.x + x, this.y + y, this.z + z);
    }

    public BVec add(final BVec bv) {
        return of(this.x + bv.x, this.y + bv.y, this.z + bv.z);
    }

    public BVec mul(final int m) {
        return of(this.x * m, this.y * m, this.z * m);
    }

    public BVec mul(final float m) {
        return of((int) (x * m), (int) (y * m), (int) (z * m));
    }

    public int distSq(final Location to) {
        return NumUtil.square(to.getBlockX() - x) + NumUtil.square(to.getBlockY() - y) + NumUtil.square(to.getBlockZ() - z);
    }

    public int distSq(final BVec to) {
        return NumUtil.square(x - to.x) + NumUtil.square(y - to.y) + NumUtil.square(z - to.z);
    }

    public int distAbs(final Location to) {
        return NumUtil.abs(to.getBlockX() - x) + NumUtil.abs(to.getBlockY() - y) + NumUtil.abs(to.getBlockZ() - z);
    }

    public int distAbs(final BVec to) {
        return NumUtil.abs(x - to.x) + NumUtil.abs(y - to.y) + NumUtil.abs(z - to.z);
    }

    public int dist(final Location to) {
        return NumUtil.sqrt(distSq(to));
    }

    public int dist(final BVec to) {
        return NumUtil.sqrt(distSq(to));
    }

    /*public @Nullable Location center() {return null;}
    public @Nullable Block block() {return null;}*/
    public Location center(final World w) {
        return new Location(w, x + 0.5d, y + 0.5d, z + 0.5d);
    }
    public Block block(final World w) {
        return w.getBlockAt(x, y, z);
    }

    public int thin() { //координата в одном int для небольших значений, работает с '-'
        return y >> 31 << 30 ^ x >> 31 << 29 ^ z >> 31 << 28 ^ y << 20 ^ x << 10 ^ z;
    }

    public boolean equals(final Object o) {
        if (!(o instanceof BVec bv)) return false;
        return x == bv.x && y == bv.y && z == bv.z;
    }

    public int hashCode() {
        return thin();
    }
    
    public String toString() {
        return x + SPLIT + y + SPLIT + z;
    }

    public BVec clone() {
        try {return (BVec) super.clone();}
        catch (CloneNotSupportedException e)
            {return new BVec(x, y, z);}
    }

    public static BVec of() {
        return new BVec(0, 0, 0);
    }

    public static BVec of(final int x, final int y, final int z) {
        return new BVec(x, y, z);
    }

    public static BVec of(final Position ps) {
        return new BVec(ps.blockX(), ps.blockY(), ps.blockZ());
    }

    /*private static class BVecDir extends BVec {
        private final int pit, yaw;
        protected BVecDir(final int x, final int y, final int z, final int pit, final int yaw) {
            super(x, y, z);
            this.pit = pit;
            this.yaw = yaw;
        }

        public int pit() {return pit;}
        public BVec pit(final int pit) {
            return of(x, y, z, pit, yaw);
        }
        public int yaw() {return yaw;}
        public BVec yaw(final int yaw) {
            return of(x, y, z, pit, yaw);
        }

        public BVec add(final int x, final int y, final int z) {
            return of(this.x + x, this.y + y, this.z + z, pit, yaw);
        }

        public BVec add(final BVec bv) {
            return of(this.x + bv.x, this.y + bv.y, this.z + bv.z, pit, yaw);
        }

        public BVec mul(final int m) {
            return of(this.x * m, this.y * m, this.z * m, pit, yaw);
        }

        public BVec mul(final float m) {
            return of((int) (x * m), (int) (y * m), (int) (z * m), pit, yaw);
        }

        public BVec w(final String wname) {
            return of(wname, x, y, z, pit, yaw);
        }
        public BVec w(final World w) {
            return of(w, x, y, z, pit, yaw);
        }

        public boolean equals(final Object o) {
            if (!(o instanceof BVecDir bv)) return false;
            return super.equals(bv) && pit == bv.pit && yaw == bv.yaw;
        }

        public String toString() {
            return super.toString() + SPLIT + pit + SPLIT + yaw;
        }

        @Override
        public BVec clone() {
            final BVec cln = super.clone();
            return BVec.of(cln.x, cln.y, cln.z, pit, yaw);
        }
    }

    public static BVec of(final int x, final int y, final int z, final int pit, final int yaw) {
        return new BVecDir(x, y, z, pit, yaw);
    }*/

    private static class WBVec extends BVec {
        private final String world;
        protected WBVec(final String world, final int x, final int y, final int z) {
            super(x, y, z);
            this.world = world;
            this.world.hashCode();
        }

        public BVec add(final int x, final int y, final int z) {
            return of(world, this.x + x, this.y + y, this.z + z);
        }

        public BVec add(final BVec bv) {
            return of(world, this.x + bv.x, this.y + bv.y, this.z + bv.z);
        }

        public BVec mul(final int m) {
            return of(world, this.x * m, this.y * m, this.z * m);
        }

        public BVec mul(final float m) {
            return of(world, (int) (x * m), (int) (y * m), (int) (z * m));
        }

        public @Nullable World w() {return Bukkit.getWorld(world);}
        public @Nullable String wname() {return world;}

        public boolean equals(final Object o) {
            if (!(o instanceof WBVec bv)) return false;
            return super.equals(bv) && Objects.equals(world, bv.world);
        }

        public String toString() {
            return super.toString() + SPLIT + world;
        }

        public BVec clone() {
            final BVec cln = super.clone();
            return BVec.of(world, cln.x, cln.y, cln.z);
        }
    }

    public static BVec of(final World w, final int x, final int y, final int z) {
        if (w == null) return new BVec(x, y, z);
        return new WBVec(w.getName(), x, y, z);
    }

    public static BVec of(final String wname, final int x, final int y, final int z) {
        if (wname == null) return new BVec(x, y, z);
        return new WBVec(wname, x, y, z);
    }

    public static BVec of(final Block bl) {
        return new WBVec(bl.getWorld().getName(), bl.getX(), bl.getY(), bl.getZ());
    }

    private static class WBVecVals extends BVec {
        private final String world;
        private final byte[] vals;
        protected WBVecVals(final String world, final int x, final int y, final int z, final byte... vals) {
            super(x, y, z);
            this.world = world;
            this.world.hashCode();
            this.vals = vals;
        }

        public byte[] vals() {return vals;}

        public BVec add(final int x, final int y, final int z) {
            return of(world, this.x + x, this.y + y, this.z + z, vals);
        }

        public BVec add(final BVec bv) {
            return of(world, this.x + bv.x, this.y + bv.y, this.z + bv.z, vals);
        }

        public BVec mul(final int m) {
            return of(world, this.x * m, this.y * m, this.z * m, vals);
        }

        public BVec mul(final float m) {
            return of(world, (int) (x * m), (int) (y * m), (int) (z * m), vals);
        }

        public @Nullable World w() {return Bukkit.getWorld(world);}
        public @Nullable String wname() {return world;}
        public BVec w(final String wname) {
            return of(wname, x, y, z, vals);
        }
        public BVec w(final World w) {
            return of(w, x, y, z, vals);
        }

        public boolean equals(final Object o) {
            if (!(o instanceof WBVecVals bv)) return false;
            if (bv.vals.length != vals.length) return false;
            for (int i = 0; i != vals.length; i++)
                if (bv.vals[i] != vals[i]) return false;
            return super.equals(bv) && Objects.equals(world, bv.world);
        }

        public String toString() {
            final StringBuilder sb = new StringBuilder(vals.length << 1);
            for (final int n : vals) sb.append(SPLIT).append(n);
            return super.toString() + SPLIT + world + sb.toString();
        }

        public BVec clone() {
            final BVec cln = super.clone();
            return BVec.of(world, cln.x, cln.y, cln.z, vals);
        }
    }

    public static BVec of(final World w, final int x, final int y, final int z, final byte... vals) {
        if (w == null) return new BVec(x, y, z);
        if (vals.length == 0) return new WBVec(w.getName(), x, y, z);
        return new WBVecVals(w.getName(), x, y, z, vals);
    }

    public static BVec of(final String world, final int x, final int y, final int z, final byte... vals) {
        if (world == null) return new BVec(x, y, z);
        if (vals.length == 0) return new WBVec(world, x, y, z);
        return new WBVecVals(world, x, y, z, vals);
    }

    public static BVec of(final Location loc) {
        return new WBVecVals(loc.getWorld().getName(), loc.getBlockX(),
            loc.getBlockY(), loc.getBlockZ(), (byte) loc.getPitch(), (byte) loc.getYaw());
    }

    public static BVec of(final Entity ent) {
        return of(EntityUtil.center(ent));
    }

    public static BVec parse(final String bVec) {
        BVec bv = of();
        if (!bVec.contains(SPLIT)) {
            final XYZ xyz = XYZ.fromString(bVec);
            if (xyz != null) {
                Ostrov.log_warn("Parsing BVec " + SPLIT + " from XYZ " + bVec);
                return of(xyz.worldName, xyz.x, xyz.y, xyz.z);
            }
            Ostrov.log_err("Error parsing BVec " + SPLIT + " for " + bVec);
        }
        final String[] parts = bVec.split(SPLIT);
        switch (parts.length) {
            case 4:
                bv = bv.w(parts[3]);
            case 3:
                bv.z = NumUtil.intOf(parts[2], 0);
                bv.y = NumUtil.intOf(parts[1], 0);
                bv.x = NumUtil.intOf(parts[0], 0);
                break;
            default:
                if (parts.length < 3) {
                    Ostrov.log_err("BVec " + SPLIT + " parse < 3 for " + bVec + " len=" + parts.length);
                    return bv;
                }
                final byte[] vals = new byte[parts.length - 4];
                for (int i = 0; i != vals.length; i++) vals[i] = (byte) NumUtil.intOf(parts[i + 4], 0);
                return of(parts[3], NumUtil.intOf(parts[0], 0),
                    NumUtil.intOf(parts[1], 0), NumUtil.intOf(parts[2], 0), vals);
        }
        return bv;
    }
}
