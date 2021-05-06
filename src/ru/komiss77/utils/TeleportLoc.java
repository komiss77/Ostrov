package ru.komiss77.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;


public class TeleportLoc {

     /*   
public static final Vector3D[] VOLUME;  
public static final int RADIUS = 3;

	public static class Vector3D {
		public int x;
		public int y;
		public int z;

		public Vector3D(int x, int y, int z){
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	static	{
            List<Vector3D> pos = new ArrayList<>();
            for (int x = -RADIUS; x <= RADIUS; x++)	{
                    for (int y = -RADIUS; y <= RADIUS; y++)	{
                            for (int z = -RADIUS; z <= RADIUS; z++)	{
                                    pos.add(new Vector3D(x, y, z));
                            }
                    }
            }
            Collections.sort(pos, (Vector3D a, Vector3D b) -> (a.x * a.x + a.y * a.y + a.z * a.z) - (b.x * b.x + b.y * b.y + b.z * b.z));
            VOLUME = pos.toArray(new Vector3D[0]);
	}
        
        
    
    public static Location getSafeDestination(final Location loc) {
            final World world = loc.getWorld();
            int x = loc.getBlockX();
            int y = (int)Math.round(loc.getY());
            int z = loc.getBlockZ();
            final int origX = x;
            final int origY = y;
            final int origZ = z;
            while (isBlockAboveAir(world, x, y, z))
            {
                    y -= 1;
                    if (y < 0)
                    {
                            y = origY;
                            break;
                    }
            }
            if (isBlockUnsafe(world, x, y, z))
            {
                    x = Math.round(loc.getX()) == origX ? x - 1 : x + 1;
                    z = Math.round(loc.getZ()) == origZ ? z - 1 : z + 1;
            }
            int i = 0;
            while (isBlockUnsafe(world, x, y, z))
            {
                    i++;
                    if (i >= VOLUME.length)
                    {
                            x = origX;
                            y = origY + RADIUS;
                            z = origZ;
                            break;
                    }
                    x = origX + VOLUME[i].x;
                    y = origY + VOLUME[i].y;
                    z = origZ + VOLUME[i].z;
            }
            while (isBlockUnsafe(world, x, y, z))
            {
                    y += 1;
                    if (y >= world.getMaxHeight())
                    {
                            x += 1;
                            break;
                    }
            }
            while (isBlockUnsafe(world, x, y, z))
            {
                    y -= 1;
                    if (y <= 1)
                    {
                            x += 1;
                            y = world.getHighestBlockYAt(x, z);
                            if (x - 48 > loc.getBlockX())
                            {
                                    return null;
                            }
                    }
            }
            return new Location(world, x + 0.5, y, z + 0.5, loc.getYaw(), loc.getPitch());
	}


    
    
    */

    
    
    
    
    
    
        public static Location findNearestSafeLocation(Location loc, Location lookAt) {
        if (loc == null)  return null;
        if (!loc.getChunk().isLoaded()) loc.getChunk().load();
        World world = loc.getWorld();
        int px = loc.getBlockX();
        int pz = loc.getBlockZ();
        int py = loc.getBlockY();
        for (int dy = 1; dy <= 30; dy++) {
            for (int dx = 1; dx <= 30; dx++) {
                for (int dz = 1; dz <= 30; dz++) {
                    // Scans from the center and out
                    int x = px + (dx % 2 == 0 ? dx / 2 : -dx / 2);
                    int z = pz + (dz % 2 == 0 ? dz / 2 : -dz / 2);
                    int y = py + (dy % 2 == 0 ? dy / 2 : -dy / 2);
                    Location spawnLocation = new Location(world, x, y, z);
                    if (isSafeLocation(spawnLocation)) {
                        // look at the old location
                        spawnLocation = centerOnBlock(spawnLocation);
                        if (lookAt != null) {
                            Location d = centerOnBlock(lookAt).subtract(spawnLocation);
                            spawnLocation.setDirection(d.toVector());
                        } else {
                            spawnLocation.setYaw(loc.getYaw());
                            spawnLocation.setPitch(loc.getPitch());
                        }
                        //log(Level.FINER, "found safe location " + spawnLocation + " near " + loc + ", looking at " + lookAt);
                        return spawnLocation;
                    }
                }
            }
        }
        return null;
    }
    
    
    public static Location centerOnBlock(Location loc) {
        if (loc == null) {
            return null;
        }
        return new Location(loc.getWorld(),
                loc.getBlockX() + 0.5, loc.getBlockY() + 0.1, loc.getBlockZ() + 0.5,
                loc.getYaw(), loc.getPitch());
    }

    
    
    
    
    
    public static boolean isSafeLocation(final Location l) {
        if (l == null) return false;
        final Block ground = l.getBlock().getRelative(BlockFace.DOWN);
        final Block air1 = l.getBlock();
        final Block air2 = l.getBlock().getRelative(BlockFace.UP);
        return ground.getType().isSolid() && isBreathable(air1) && isBreathable(air2);
    }

    public static boolean isBreathable(Block block) {
        return !block.getType().isSolid() && !isFluid(block);
    }

    public static boolean isFluid(Block block) {
        return block.getType()==Material.WATER || block.getType().isFuel();
    }


    
    
    
    
    
    
    
    
    
    
    
    
    
    /*
    static boolean isBlockAboveAir(final World world, final int x, final int y, final int z) {
            if (y > world.getMaxHeight())	return true;
            return !world.getBlockAt(x, y - 1, z).getType().isSolid();
	}

        
	public static boolean isBlockUnsafe(final World world, final int x, final int y, final int z) {
            if (isBlockDamaging(world, x, y, z)) {
                    return true;
            }
            return isBlockAboveAir(world, x, y, z);
	}

        
 	public static boolean isBlockDamaging(final World world, final int x, final int y, final int z)	{
            final Block below = world.getBlockAt(x, y - 1, z);
            if (below.getType() == Material.LAVA || below.getType() == Material.FIRE || below.getType().toString().contains("_BED") ) return true;
            return ( world.getBlockAt(x, y, z).getType().isSolid()) || world.getBlockAt(x, y + 1, z).getType().isSolid();
	}
       */
        
}
