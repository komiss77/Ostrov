package ru.komiss77.utils;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;


public class BlockUtil {

    public static final BlockData air = BlockType.AIR.createBlockData();

    public static boolean is(final Block b, final BlockType bt) {
        return b.getType().asBlockType().equals(bt);
    }

    public static void set(final Block b, final BlockType bt, final boolean upd) {
        if (BlockType.AIR.equals(bt)) {
            b.setBlockData(air, upd);
            return;
        }
        b.setBlockData(bt.createBlockData(), upd);
    }

    public static Block getSignAttachedBlock(final Block b) {
        if (b.getState() instanceof final Sign sign
                && sign.getBlockData() instanceof final WallSign signData) {
            return b.getRelative(signData.getFacing().getOppositeFace());

        }
        return b.getRelative(BlockFace.DOWN);
    }

    public static Block getHighestBlock(final World world, final int x, final int z) { //не отдавать null!!
        final Location loc = LocUtil.getHighestLoc(world, x, z);
        return loc == null ? world.getHighestBlockAt(x, z, HeightMap.MOTION_BLOCKING) : loc.getBlock().getRelative(BlockFace.UP);
    }

    //для регионГуи
    public static List<Block> getCuboidBorder(final World world, final Location minpoint, final int size) {
        final List<Block> queue = new ArrayList<>();
        queue.add(getHighestBlock(world, minpoint.getBlockX(), minpoint.getBlockZ()));
        queue.add(getHighestBlock(world, minpoint.getBlockX() + size, minpoint.getBlockZ()));
        queue.add(getHighestBlock(world, minpoint.getBlockX(), minpoint.getBlockZ() + size));
        queue.add(getHighestBlock(world, minpoint.getBlockX() + size, minpoint.getBlockZ() + size));

        for (int i = 1; i < size; i++) {
            queue.add(getHighestBlock(world, minpoint.getBlockX() + i, minpoint.getBlockZ()));
            queue.add(getHighestBlock(world, minpoint.getBlockX(), minpoint.getBlockZ() + i));
            queue.add(getHighestBlock(world, minpoint.getBlockX() + size - i, minpoint.getBlockZ() + size));
            queue.add(getHighestBlock(world, minpoint.getBlockX() + size, minpoint.getBlockZ() + size - i));
        }
        return queue;
    }

}
