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


public class WallTask implements Runnable {

  private final World world;
  private final Material wallMat;
  private final BukkitTask task;
  private final List<Block> toFill;

  public WallTask(final World world, final Material wallMat, final Location minpoint, final int size) {
    toFill = BlockUtil.getCuboidBorder(world, minpoint, size);
    this.world = world;
    this.wallMat = wallMat;
    task = Bukkit.getScheduler().runTaskTimer(Ostrov.getInstance(), this, 20, 10L);
  }

  @Override
  public void run() {
    for (int i = 0; i <= 10; ++i) {
      if (this.toFill.isEmpty()) {
        this.task.cancel();
        return;
      }
      final Block block = toFill.remove(0);
      block.setType(wallMat, true);
      world.playEffect(block.getLocation(), Effect.STEP_SOUND, wallMat);
    }
  }


}
