
package ru.komiss77.utils;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;


public class BlockUtils {
    
    
    public static Block getHighestBlock(final World world, final int x, final int z) {
        Block block;
        
        for ( block = world.getHighestBlockAt(x, z).getRelative(BlockFace.DOWN); 
                Tag.LEAVES.isTagged(block.getType()) || block.getType() == Material.AIR || block.getType() == Material.GRASS || block.getType() == Material.TALL_GRASS; block = block.getRelative(BlockFace.DOWN)) {}
        
        return block.getRelative(BlockFace.UP).getType()==Material.AIR ? block.getRelative(BlockFace.UP) : block.getLocation().clone().add(0, 2, 0).getBlock();
    }
    
    public static List<Block> getCuboidBorder (final World world, final Location minpoint, final int size) {
        final List<Block> queue = new ArrayList<>();
        queue.add( BlockUtils.getHighestBlock(world, minpoint.getBlockX(), minpoint.getBlockZ()) );
        queue.add( BlockUtils.getHighestBlock(world, minpoint.getBlockX()+size, minpoint.getBlockZ()) );
        queue.add( BlockUtils.getHighestBlock(world, minpoint.getBlockX(), minpoint.getBlockZ()+size) );
        queue.add( BlockUtils.getHighestBlock(world, minpoint.getBlockX()+size, minpoint.getBlockZ()+size) );
        
        for (int i = 1; i<size; i++) {
            queue.add( BlockUtils.getHighestBlock(world, minpoint.getBlockX()+i, minpoint.getBlockZ()) );
            queue.add( BlockUtils.getHighestBlock(world, minpoint.getBlockX(), minpoint.getBlockZ()+i) );
            queue.add( BlockUtils.getHighestBlock(world, minpoint.getBlockX()+size-i, minpoint.getBlockZ()+size) );
            queue.add( BlockUtils.getHighestBlock(world, minpoint.getBlockX()+size, minpoint.getBlockZ()+size-i) );
        }
        return queue;
    }
    
}
