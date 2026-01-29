package ru.komiss77.hook;

import java.util.HashMap;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.Biome;
import org.dynmap.renderer.DynmapBlockState;

public class DynmapNms {



  public static void initializeBlockStates() {
    //dataToState = new IdentityHashMap<>();
    HashMap<String, DynmapBlockState> lastBlockState = new HashMap<>();
    //Registry<IBlockData> bsids = Block.q;
    IdMapper<BlockState> bsids = Block.BLOCK_STATE_REGISTRY;
    Block baseBlock;
    //ArrayList<String> names = new ArrayList<String>();
    DynmapBlockState.Builder bld = new DynmapBlockState.Builder();

    for (BlockState bs : bsids) {//while (iter.hasNext()) {
      baseBlock = bs.getBlock();
      //ResourceKey id = BuiltInRegistries.BLOCK.getResourceKey(baseBlock).get();
      Identifier id = BuiltInRegistries.BLOCK.getKey(baseBlock);
      String minecraftName = id.toString();

      DynmapBlockState lastbs = lastBlockState.get(minecraftName);  // See if we have seen this one
      int idx = 0;
      if (lastbs != null) {  // Yes
        idx = lastbs.getStateCount();  // Get number of states so far, since this is next
      }

      // Build state name
      String sb = "";
      String fname = bs.toString();
      int off1 = fname.indexOf('[');
      if (off1 >= 0) {
        int off2 = fname.indexOf(']');
        sb = fname.substring(off1 + 1, off2);
      }

      int lightAtten = bs.getLightBlock();  // getLightBlock

      bld.setBaseState(lastbs)
          .setStateIndex(idx)
          .setBlockName(minecraftName)
          .setStateName(sb)
          .setAttenuatesLight(lightAtten);

      if (bs.isSolidRender()) bld.setSolid(); // isSolid
      if (bs.isAir()) bld.setAir(); // isAir
      if (bs.is(BlockTags.OVERWORLD_NATURAL_LOGS)) bld.setLog(); // is(OVERWORLD_NATURAL_LOGS)
      if (bs.is(BlockTags.LEAVES)) bld.setLeaves(); // is(LEAVES)
      if (!bs.getFluidState().isEmpty() && !(baseBlock instanceof SimpleWaterloggedBlock)) {  // getFluidState.isEmpty(), getBlock
        bld.setWaterlogged(); //Log.info("statename=" + bname + "[" + sb + "] = waterlogged");
      }

      DynmapBlockState dbs = bld.build(); // Build state

      //dataToState.put(bs, dbs);
      lastBlockState.put(minecraftName, (lastbs == null) ? dbs : lastbs);
//Ostrov.log_warn("Dynmap initializeBlockStates minecraftName=" + minecraftName + ", idx=" + idx + ", state=" + sb + ", waterlogged=" + dbs.isWaterlogged());
    }
  }




}
