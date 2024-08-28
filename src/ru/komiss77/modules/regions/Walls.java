package ru.komiss77.modules.regions;

import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.Effect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Material;
import org.bukkit.World;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.BlockUtil;


public class Walls implements Runnable {

  private final World world;
  private final Material material;
  private final BukkitTask task;
  private final List<Block> toFill;

  public Walls(final World world, final Material wallMat, final Location minpoint, final int size) {
    this.toFill = BlockUtil.getCuboidBorder(world, minpoint, size);//new ArrayDeque<>();
    //minpoint.setY(0);

        /*toFill.add( new PairCoord(minpoint.getBlockX(), minpoint.getBlockZ()));
        toFill.add( new PairCoord(minpoint.getBlockX()+size, minpoint.getBlockZ()) );
        toFill.add( new PairCoord(minpoint.getBlockX(), minpoint.getBlockZ()+size) );
        toFill.add( new PairCoord(minpoint.getBlockX()+size, minpoint.getBlockZ()+size) );

        for (int i = 1; i<size; i++) {
            toFill.add( new PairCoord(minpoint.getBlockX()+i, minpoint.getBlockZ()));
            toFill.add( new PairCoord(minpoint.getBlockX(), minpoint.getBlockZ()+i));
            toFill.add( new PairCoord(minpoint.getBlockX()+size-i, minpoint.getBlockZ()+size));
            toFill.add( new PairCoord(minpoint.getBlockX()+size, minpoint.getBlockZ()+size-i));
        }*/

    this.world = world;
    this.material = wallMat;
    this.task = Bukkit.getScheduler().runTaskTimer(Ostrov.getInstance(), this, 20, 10L);
  }

  @Override
  public void run() {
    for (int i = 0; i <= 10; ++i) {
      if (this.toFill.isEmpty()) {
        this.task.cancel();
        return;
      }
      //final PairCoord pc = toFill.poll();
      final Block block = toFill.get(0);
      toFill.remove(0);
      //final Block highestBlock = BlockUtils.getHighestBlock(this.world, pc.x, pc.z);
      block.setType(material, true);
      world.playEffect(block.getLocation(), Effect.STEP_SOUND, material);
    }
  }


  private class PairCoord {
    public final int x;
    public final int z;

    private PairCoord(final int x, final int z) {
      this.x = x;
      this.z = z;
    }

  }


}
