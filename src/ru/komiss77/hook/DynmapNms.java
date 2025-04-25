package ru.komiss77.hook;

import java.util.*;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.bukkit.World;
import org.dynmap.DynmapChunk;
import org.dynmap.common.BiomeMap;
import org.dynmap.common.chunk.*;
import org.dynmap.renderer.DynmapBlockState;
import org.dynmap.utils.DataBitsPacked;
import ru.komiss77.Ostrov;
import ru.komiss77.version.Craft;

public class DynmapNms {

  private static final List<String> litStates = List.of("light", "spawn", "heightmaps", "full");

  public static IdentityHashMap<BlockState, DynmapBlockState> dataToState;

  public static void initializeBlockStates() {
    dataToState = new IdentityHashMap<>();
    HashMap<String, DynmapBlockState> lastBlockState = new HashMap<>();
    //Registry<IBlockData> bsids = Block.q;
    IdMapper<BlockState> bsids = Block.BLOCK_STATE_REGISTRY;
    Block baseb;
    //ArrayList<String> names = new ArrayList<String>();
    DynmapBlockState.Builder bld = new DynmapBlockState.Builder();

    for (BlockState bs : bsids) {//while (iter.hasNext()) {
      baseb = bs.getBlock();
      //ResourceKey id = BuiltInRegistries.BLOCK.getResourceKey(baseb).get();
      ResourceLocation id = BuiltInRegistries.BLOCK.getKey(baseb);
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

      if (bs.isSolid()) bld.setSolid(); // isSolid
      if (bs.isAir()) bld.setAir(); // isAir
      if (bs.is(BlockTags.OVERWORLD_NATURAL_LOGS)) bld.setLog(); // is(OVERWORLD_NATURAL_LOGS)
      if (bs.is(BlockTags.LEAVES)) bld.setLeaves(); // is(LEAVES)
      if (!bs.getFluidState().isEmpty() && ((baseb instanceof SimpleWaterloggedBlock) == false)) {  // getFluidState.isEmpty(), getBlock
        bld.setWaterlogged(); //Log.info("statename=" + bname + "[" + sb + "] = waterlogged");
      }

      DynmapBlockState dbs = bld.build(); // Build state

      dataToState.put(bs, dbs);
      lastBlockState.put(minecraftName, (lastbs == null) ? dbs : lastbs);
//Ostrov.log_warn("Dynmap initializeBlockStates minecraftName=" + minecraftName + ", idx=" + idx + ", state=" + sb + ", waterlogged=" + dbs.isWaterlogged());
    }
  }


  public static GenericChunk loadChunk(World w, DynmapChunk dc) {
    final ServerLevel sl = Craft.toNMS(w);
    ChunkPos cc = new ChunkPos(dc.x, dc.z);
    final LevelChunk lc = sl.getChunkIfLoaded(dc.x, dc.z);
    GenericChunk gc = null;
    //SerializableChunkData.copyOf(sl,lc);
    CompoundTag chunkData = sl.getChunkSource().chunkMap.read(cc).join().get();
    // sl.getChunkSource().chunkMap.read(cc).join().ifPresentOrElse( nbt -> parseChunkFromNBT(nbt), null);
    return parseChunkFromNBT(w, chunkData);
  }


  public static GenericChunk parseChunkFromNBT(World w, CompoundTag chunkData) {
    //GenericNBTCompound chunkData = orignbt;
    //if ((chunkData != null) && chunkData.contains("Level", GenericNBTCompound.TAG_COMPOUND)) {
    //	chunkData = chunkData.getCompound("Level");
    //}
    //if (chunkData == null) return null;
    ChunkStatus chunkStatus = chunkData.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY);
    String status = chunkStatus.getName().toLowerCase();//chunkData.getStringOr("Status", "");
    int version = ChunkStorage.getVersion(chunkData);//+ chunkData.getIntOr("DataVersion", 0);
    boolean lit = chunkData.getBooleanOr("isLightOn", false); //+
    boolean hasLitState = litStates.contains(status);

    //if (status != null) {
    //  for (int i = 0; i < litStates.length; i++) {
    //    if (status.equals(litStates[i])) { hasLitState = true; }
    //  }
    //}
    boolean hasLight = false; // pessimistic: only has light if we see it, due to WB and other flawed chunk generation hasLitState;	// Assume good light in a lit state

    // Start generic chunk builder
    GenericChunk.Builder bld = new GenericChunk.Builder(w.getMinHeight(), w.getMaxHeight());//dw.minY,  dw.worldheight);
    int x = chunkData.getIntOr("xPos", 0);//dataVersion > 2842 dc.x;//chunkData.getIntOr("xPos");
    int z = chunkData.getIntOr("zPos", 0);//dataVersion > 2842 dc.z;//chunkData.getIntOr("zPos");

    // Set chunk info
    bld.coords(x, z).chunkStatus(status).dataVersion(version);

    if (chunkData.contains("InhabitedTime")) {
      bld.inhabitedTicks(chunkData.getLongOr("InhabitedTime", 0)); //+
    }

    // Check for 2D or old 3D biome data from chunk level: need these when we build old sections
    List<BiomeMap[]> old3d = null;  // By section, then YZX list
    BiomeMap[] old2d = null;
    if (chunkData.contains("Biomes")) {
      int[] bb = chunkData.getIntArray("Biomes").get();
      if (bb != null) {
        // If v1.15+ format
        if (bb.length > 256) {
          old3d = new ArrayList<BiomeMap[]>();
          // Get 4 x 4 x 4 list for each section
          for (int sect = 0; sect < (bb.length / 64); sect++) {
            BiomeMap smap[] = new BiomeMap[64];
            for (int i = 0; i < 64; i++) {
              smap[i] = BiomeMap.byBiomeID(bb[sect * 64 + i]);
            }
            old3d.add(smap);
          }
        } else { // Else, older chunks
          old2d = new BiomeMap[256];
          for (int i = 0; i < bb.length; i++) {
            old2d[i] = BiomeMap.byBiomeID(bb[i]);
          }
        }
      }
    }
    // Start section builder
    GenericChunkSection.Builder sbld = new GenericChunkSection.Builder();
    /* Get sections */
    //GenericNBTList sect = chunkData.contains("sections") ? chunkData.getList("sections", 10) : chunkData.getList("Sections", 10);
    ListTag sections = chunkData.getListOrEmpty(SerializableChunkData.SECTIONS_TAG);//chunkData.getListOrEmpty("Sections");
    //List<SerializableChunkData.SectionData> sectData = new ArrayList<>(sect.size());
    // And process sections
    for (int i = 0; i < sections.size(); i++) {
      Optional<CompoundTag> compound = sections.getCompound(i);
      if (compound.isEmpty()) continue;
      CompoundTag compoundTag = compound.get();
      final CompoundTag sectionData = compoundTag;
      //GenericNBTCompound sec = sect.getCompound(i);
      int secnum = compoundTag.getByteOr("Y", (byte) 0);//sec.getByte("Y");

      DynmapBlockState[] palette = null;

      // If we've got palette and block states list, process non-empty section
    /*  if (sectionData.contains("Palette") && sectionData.contains("BlockStates")) {
      //if (sec.contains("Palette", 9) && sec.contains("BlockStates", 12)) {
        GenericNBTList plist = sec.getList("Palette", 10);
        long[] statelist = sec.getLongArray("BlockStates");
        palette = new DynmapBlockState[plist.size()];
        for (int pi = 0; pi < plist.size(); pi++) {
          GenericNBTCompound tc = plist.getCompound(pi);
          String pname = tc.getString("Name");
          if (tc.contains("Properties")) {
            StringBuilder statestr = new StringBuilder();
            GenericNBTCompound prop = tc.getCompound("Properties");
            for (String pid : prop.getAllKeys()) {
              if (statestr.length() > 0) statestr.append(',');
              statestr.append(pid).append('=').append(prop.getAsString(pid));
            }
            palette[pi] = DynmapBlockState.getStateByNameAndState(pname, statestr.toString());
          }
          if (palette[pi] == null) {
            palette[pi] = DynmapBlockState.getBaseStateByName(pname);
          }
          if (palette[pi] == null) {
            palette[pi] = DynmapBlockState.AIR;
          }
        }
        int recsperblock = (4096 + statelist.length - 1) / statelist.length;
        int bitsperblock = 64 / recsperblock;
        GenericBitStorage db = null;
        DataBitsPacked dbp = null;
        try {
          db = chunkData.makeBitStorage(bitsperblock, 4096, statelist);
        } catch (Exception ex) {	// Handle legacy encoded
          bitsperblock = (statelist.length * 64) / 4096;
          dbp = new DataBitsPacked(bitsperblock, 4096, statelist);
        }
        if (bitsperblock > 8) {	// Not palette
          for (int j = 0; j < 4096; j++) {
            int v = (dbp != null) ? dbp.getAt(j) : db.get(j);
            sbld.xyzBlockState(j & 0xF, (j & 0xF00) >> 8, (j & 0xF0) >> 4, DynmapBlockState.getStateByGlobalIndex(v));
          }
        }
        else {
          sbld.xyzBlockStatePalette(palette);	// Set palette
          for (int j = 0; j < 4096; j++) {
            int v = db != null ? db.get(j) : dbp.getAt(j);
            sbld.xyzBlockStateInPalette(j & 0xF, (j & 0xF00) >> 8, (j & 0xF0) >> 4, (short)v);
          }
        }

      } else */
      if (sectionData.contains("block_states")) {
        //if (sec.contains("block_states", GenericNBTCompound.TAG_COMPOUND)) {	// 1.18
        CompoundTag block_states = sectionData.getCompoundOrEmpty("block_states");
        //GenericNBTCompound block_states = sec.getCompound("block_states");
        // If we've got palette, process non-empty section
        if (block_states.contains(StructureTemplate.PALETTE_TAG)) {//if (block_states.contains("palette")) {//, GenericNBTCompound.TAG_LIST)) {
          //GenericNBTList plist = block_states.getList("palette", GenericNBTCompound.TAG_COMPOUND);
          ListTag plist = block_states.getListOrEmpty(StructureTemplate.PALETTE_TAG);
          //long[] statelist = block_states.contains("data", GenericNBTCompound.TAG_LONG_ARRAY) ? block_states.getLongArray("data") : new long[4096 / 64]; // Handle zero bit palette (all same)
          long[] statelist = block_states.contains("data") ? block_states.getLongArray("data").get() : new long[4096 / 64]; // Handle zero bit palette (all same)
          palette = new DynmapBlockState[plist.size()];
          for (int pi = 0; pi < plist.size(); pi++) {
            Optional<CompoundTag> cp = sections.getCompound(pi);
            if (cp.isEmpty()) continue;
            CompoundTag tc = cp.get();
            //GenericNBTCompound tc = plist.getCompound(pi);
            String pname = tc.getStringOr(StateHolder.NAME_TAG, "");//"Name", "");
            if (tc.contains(StateHolder.PROPERTIES_TAG)) { //"Properties")) {
              StringBuilder sb = new StringBuilder();
              CompoundTag prop = tc.getCompoundOrEmpty(StateHolder.PROPERTIES_TAG);//"Properties");
              //GenericNBTCompound prop = tc.getCompound("Properties");
              for (String pid : prop.keySet()) {//getAllKeys()) {
                if (sb.length() > 0) sb.append(',');
                //sb.append(pid).append('=').append(prop.getAsString(pid));
                sb.append(pid).append('=').append(prop.getStringOr(pid, ""));//getAsString(pid));
              }
              palette[pi] = DynmapBlockState.getStateByNameAndState(pname, sb.toString());
            }
            if (palette[pi] == null) {
              palette[pi] = DynmapBlockState.getBaseStateByName(pname);
            }
            if (palette[pi] == null) {
              palette[pi] = DynmapBlockState.AIR;
            }
          }
          SimpleBitStorage db = null;//GenericBitStorage db = null;
          DataBitsPacked dbp = null;

          int bitsperblock = (statelist.length * 64) / 4096;
          int expectedStatelistLength = (4096 + (64 / bitsperblock) - 1) / (64 / bitsperblock);
          if (statelist.length == expectedStatelistLength) {
            db = new SimpleBitStorage(bitsperblock, 4096, statelist);//chunkData.makeBitStorage(bitsperblock, 4096, statelist);
            //db = chunkData.makeBitStorage(bitsperblock, 4096, statelist);
            //GenericNBTCompound cc;cc.makeBitStorage()
          } else {
            bitsperblock = (statelist.length * 64) / 4096;
            dbp = new DataBitsPacked(bitsperblock, 4096, statelist);
          }
          //if (bitsperblock > 8) {    // Not palette
          //	for (int j = 0; j < 4096; j++) {
          //		int v = db != null ? db.get(j) : dbp.getAt(j);
          //    	sbld.xyzBlockState(j & 0xF, (j & 0xF00) >> 8, (j & 0xF0) >> 4, DynmapBlockState.getStateByGlobalIndex(v));
          //	}
          //}
          //else {
          sbld.xyzBlockStatePalette(palette);  // Set palette
          for (int j = 0; j < 4096; j++) {
            int v = db != null ? db.get(j) : dbp.getAt(j);
            sbld.xyzBlockStateInPalette(j & 0xF, (j & 0xF00) >> 8, (j & 0xF0) >> 4, (short) v);
          }
          //}
        }
      }

      if (sectionData.contains("BlockLight")) {
        sbld.emittedLight(sectionData.getByteArray("BlockLight").get());
      }
      if (sectionData.contains("SkyLight")) {
        sbld.skyLight(sectionData.getByteArray("SkyLight").get());
        hasLight = true;
      }

      // If section biome palette
      if (sectionData.contains("biomes")) {
        CompoundTag nbtbiomes = sectionData.getCompound("biomes").get();
        //GenericNBTCompound nbtbiomes = sec.getCompound("biomes");
        long[] bdataPacked;// = nbtbiomes.getLongArray("data").get();
        if (nbtbiomes.contains("data")) {
          bdataPacked = nbtbiomes.getLongArray("data").get();
        } else {
          bdataPacked = new long[0];
        }
        ListTag bpalette = nbtbiomes.getListOrEmpty("palette");
        //GenericNBTList bpalette = nbtbiomes.getList("palette", 8);
        SimpleBitStorage bdata = null;//GenericBitStorage db = null;GenericBitStorage bdata = null;
        if (bdataPacked.length > 0) {
          int valsPerLong = (64 / bdataPacked.length);
          bdata = new SimpleBitStorage((64 + valsPerLong - 1) / valsPerLong, 64, bdataPacked);//
          //bdata = chunkData.makeBitStorage((64 + valsPerLong - 1) / valsPerLong, 64, bdataPacked);
        }
        for (int j = 0; j < 64; j++) {
          int b = bdata != null ? bdata.get(j) : 0;
          sbld.xyzBiome(j & 0x3, (j & 0x30) >> 4, (j & 0xC) >> 2, BiomeMap.byBiomeResourceLocation(bpalette.getStringOr(b, "")));
        }
      } else {  // Else, apply legacy biomes
        if (old3d != null) {
          BiomeMap m[] = old3d.get((secnum > 0) ? ((secnum < old3d.size()) ? secnum : old3d.size() - 1) : 0);
          if (m != null) {
            for (int j = 0; j < 64; j++) {
              sbld.xyzBiome(j & 0x3, (j & 0x30) >> 4, (j & 0xC) >> 2, m[j]);
            }
          }
        } else if (old2d != null) {
          for (int j = 0; j < 256; j++) {
            sbld.xzBiome(j & 0xF, (j & 0xF0) >> 4, old2d[j]);
          }
        }
      }
      // Finish and add section
      bld.addSection(secnum, sbld.build());
      sbld.reset();
    }
    // Assume skylight is only trustworthy in a lit state
    if ((!hasLitState) || (!lit)) {
      hasLight = false;
    }
    // If no light, do simple generate
    if (!hasLight) {
      //Log.info(String.format("generateSky(%d,%d)", x, z));
      bld.generateSky();
    }
    return bld.build();
  }

  public static class OurBitStorage implements GenericBitStorage {
    private final SimpleBitStorage bs;

    public OurBitStorage(int bits, int count, long[] data) {
      bs = new SimpleBitStorage(bits, count, data);
    }

    @Override
    public int get(int idx) {
      return bs.get(idx);
    }
  }
}
