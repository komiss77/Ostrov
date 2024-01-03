package ru.komiss77.modules.world;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import ru.komiss77.Ostrov;
import ru.komiss77.notes.Slow;
import ru.komiss77.utils.FastMath;


public class XYZ implements Cloneable {

    public String worldName;
    public int x;
    public int y;
    public int z;
    
    //доп.поля
    public BlockFace bf;
    public int yaw;
    public int pitch;
    
    public XYZ() {
    }    
    
    @Slow
    public static XYZ fromString(final String asString) {
        try {
            final String[] split = asString.split(",");
            final XYZ xyz= new XYZ (split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
            return xyz;
        } catch (NullPointerException | NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            Ostrov.log_err("XYZ fromString  ="+asString+" "+ex.getMessage());
            return null;
        }    
    }
    
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
    
    
    
    public int distSq(final Location to) {
        return FastMath.square(to.getBlockX() - x) + FastMath.square(to.getBlockY() - y) + FastMath.square(to.getBlockZ() - z);
    }
	
    public int distSq(final XYZ to) {
        return FastMath.square(x - to.x) + FastMath.square(y - to.y) + FastMath.square(z - to.z);
    }
	
    public int distAbs(final Location to) {
        return FastMath.absInt(to.getBlockX() - x) + FastMath.absInt(to.getBlockY() - y) + FastMath.absInt(to.getBlockZ() - z);
    }
	
    public int distAbs(final XYZ to) {
        return FastMath.absInt(x - to.x) + FastMath.absInt(y - to.y) + FastMath.absInt(z - to.z);
    }
    
    public int distAprx(final Location to) {
        return FastMath.sqrtAprx(distSq(to));
    }
	
    public int distAprx(final XYZ to) {
        return FastMath.sqrtAprx(distSq(to));
    }
	
    @Deprecated
    public int getDistance(final Location to) {
        return distSq(to);
    }
	
    @Deprecated
    public int getDistance(final XYZ to) {
        return distSq(to);
    }

    public boolean nearly(final Location loc, final int distance) { //проверить - точка в радиусе distance?
        return worldName.equals(loc.getWorld().getName()) && XYZ.this.distSq(loc) <= distance; //число подобрать точнее!
    }

    public Location getCenterLoc() {
        return getCenterLoc(Bukkit.getWorld(worldName));
    }
    
    public Location getCenterLoc(final World w) {
        return new Location(w, (double)x+.5d, (double)y+.5d, (double)z+.5d);
    }
    
    public XYZ add(final int x, final int y, final int z) {
        this.x += x; this.y += y; this.z += z;
        return this;
    }

    public XYZ add(final XYZ val) {
        return add(val.x, val.y, val.z);
    }

    public XYZ times(final int m) {
        this.x *= m; this.y *= m; this.z *= m;
        return this;
    }


    
    
    
    
    
    
    
    
    
    @Override
    public String toString() {
        return (worldName==null ? "" : worldName+",") +x+","+y+","+z;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof XYZ)) {
            return false;
        }
        final XYZ compare = (XYZ) obj;
        return ( (compare.worldName==null && worldName==null) || (compare.worldName.hashCode()==worldName.hashCode()) ) //nullpointer
        	&& compare.x==x && compare.y==y && compare.z==z;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
    
    @Override
    public XYZ clone() {
        return new XYZ(worldName, x, y, z);
    }

    public int getSLoc() { //координата в одном int для небольших значений, работает с '-'
        return  y>>31<<30 ^ x>>31<<29 ^ z>>31<<28 ^ y<<20 ^ x<<10 ^ z;
    }

}
