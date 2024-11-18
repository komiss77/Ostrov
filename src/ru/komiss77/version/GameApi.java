package ru.komiss77.version;

import net.minecraft.server.level.ServerLevel;
import org.bukkit.Material;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import ru.komiss77.modules.world.WXYZ;


public class GameApi {


  //для избавления твиста от НМС. - ок, но когда Material задепрекатят, я это уберу)
  public static void setFastMat(final WXYZ wxyz, final int sizeX, final int sizeY, final int sizeZ, final Material mat) {
    final ServerLevel sl = Craft.toNMS(wxyz.w);
    final net.minecraft.world.level.block.state.BlockState bs = ((CraftBlockData) mat.createBlockData()).getState();
    for (int x_ = 0; x_ != sizeX; x_++) {
      for (int z_ = 0; z_ != sizeZ; z_++) {
        for (int y_ = 0; y_ != sizeY; y_++) {
          Nms.mutableBlockPosition.set(wxyz.x + x_, wxyz.y + y_, wxyz.z + z_);
          //CraftBlock.setTypeAndData(sl, Nms.mutableBlockPosition, sl.getBlockState(Nms.mutableBlockPosition), bs, false);
          Nms.setNmsData(sl, Nms.mutableBlockPosition, sl.getBlockState(Nms.mutableBlockPosition), bs);
          //sl.setBlock(mutableBlockPosition, bs,)
        }
      }
    }
  }

}
