package ru.komiss77.hook;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.dynmap.DynmapChunk;
import org.dynmap.DynmapWorld;
import org.dynmap.common.BiomeMap;
import org.dynmap.common.chunk.GenericChunk;
import org.dynmap.common.chunk.GenericChunkCache;
import org.dynmap.common.chunk.GenericChunkSection;
import org.dynmap.common.chunk.GenericMapChunkCache;
import org.dynmap.renderer.DynmapBlockState;
import org.dynmap.utils.DataBitsPacked;
import ru.komiss77.version.Craft;

//https://github.com/webbukkit/dynmap

public class MapChunkCacheNms extends GenericMapChunkCache {

  private WeakReference<World> link;

  public MapChunkCacheNms(GenericChunkCache cc) {
    super(cc);
  }

  @Override
  protected Supplier<GenericChunk> getLoadedChunkAsync(DynmapChunk dc) {
    CompletableFuture<Optional<SerializableChunkData>> future = CompletableFuture.supplyAsync(() -> {
      if (link == null || link.get() == null || !link.get().isChunkLoaded(dc.x, dc.z)) {
        return Optional.empty();
      }
      final ServerLevel sl = Craft.toNMS(link.get());
      return Optional.of(SerializableChunkData.copyOf(sl, sl.getChunkIfLoaded(dc.x, dc.z)));
    }, ((CraftServer) Bukkit.getServer()).getServer());
    //return () -> future.join().map(SerializableChunkData::write).map(NBT.NBTCompound::new).map(this::parseChunkFromNBT).orElse(null); // SerializableChunkData::write
    return () -> future.join().map(SerializableChunkData::write).map(this::parseChunkFromNBT).orElse(null); // SerializableChunkData::write
  }

  @Override
  protected GenericChunk getLoadedChunk(DynmapChunk dc) {
    if (link == null || link.get() == null || !link.get().isChunkLoaded(dc.x, dc.z)) {
      return null;
    }
    final ServerLevel sl = Craft.toNMS(link.get());
    final SerializableChunkData serializableChunkData = SerializableChunkData.copyOf(sl, sl.getChunkIfLoaded(dc.x, dc.z));
    final CompoundTag chunkData = serializableChunkData.write();
    return chunkData != null ? parseChunkFromNBT(chunkData) : null;
  }

  @Override
  protected Supplier<GenericChunk> loadChunkAsync(DynmapChunk dc) {
    final ServerLevel sl = Craft.toNMS(link.get());
    ChunkPos cc = new ChunkPos(dc.x, dc.z);
    CompletableFuture<Optional<CompoundTag>> genericChunk = sl.getChunkSource().chunkMap.read(cc);
    //return () -> genericChunk.join().map(NBT.NBTCompound::new).map(this::parseChunkFromNBT).orElse(null);
    return () -> genericChunk.join().map(this::parseChunkFromNBT).orElse(null);
  }


  @Override
  public GenericChunk loadChunk(DynmapChunk dc) {
    if (link == null || link.get() == null || !link.get().isChunkLoaded(dc.x, dc.z)) {
      return null;
    }
    final ServerLevel sl = Craft.toNMS(link.get());
    final ChunkPos cc = new ChunkPos(dc.x, dc.z);
    Optional<CompoundTag> optional = sl.getChunkSource().chunkMap.read(cc).join();
    if (optional.isEmpty()) {
      return null;
    }
    CompoundTag chunkData = optional.get();
    return parseChunkFromNBT(chunkData);
  }


  public GenericChunk parseChunkFromNBT(final CompoundTag chunkData) {
    boolean hasLitState = false;
    ChunkStatus chunkStatus = chunkData.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY);
    if (chunkStatus == ChunkStatus.LIGHT || chunkStatus == ChunkStatus.SPAWN || chunkStatus == ChunkStatus.FULL) {
      hasLitState = true;//hasLitState = litStates.contains(status); "light", "spawn", "heightmaps", "full"
    }
    int version = 0;//ChunkStorage.getVersion(chunkData); //+ chunkData.getIntOr("DataVersion", 0);
    boolean isLightOn = chunkData.getBooleanOr("isLightOn", false); //+
    boolean hasLight = false; // pessimistic: only has light if we see it, due to WB and other flawed chunk generation hasLitState;	// Assume good light in a isLightOn state
    // Assume skylight is only trustworthy in a isLightOn state
    if (!hasLitState || !isLightOn) {
      hasLight = false;
    }
    // Start generic chunk builder
    GenericChunk.Builder chunkBuilder = new GenericChunk.Builder(link.get().getMinHeight(), link.get().getMaxHeight());//dw.minY,  dw.worldheight);
    int x = chunkData.getIntOr("xPos", 0);//dataVersion > 2842 dc.x;//chunkData.getIntOr("xPos");
    int z = chunkData.getIntOr("zPos", 0);//dataVersion > 2842 dc.z;//chunkData.getIntOr("zPos");

    // Set chunk info
    chunkBuilder
        .coords(x, z)
        .chunkStatus(chunkStatus.getName().toLowerCase())
        .dataVersion(version);

    if (chunkData.contains("InhabitedTime")) {
      chunkBuilder.inhabitedTicks(chunkData.getLongOr("InhabitedTime", 0)); //+
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
            BiomeMap[] smap = new BiomeMap[64];
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
    GenericChunkSection.Builder sectionBuilder = new GenericChunkSection.Builder();
    /* Get sections */
    //GenericNBTList sect = chunkData.contains("sections") ? chunkData.getList("sections", 10) : chunkData.getList("Sections", 10);
    ListTag sections = chunkData.getListOrEmpty(SerializableChunkData.SECTIONS_TAG);//chunkData.getListOrEmpty("Sections");
//Ostrov.log_warn(x+":"+z+" stat="+chunkStatus.getName().toLowerCase()+" sections="+sections.size()+" hasLitState="+hasLitState+" hasLitState="+isLightOn+(hasLight?" hasLight":" generateSky"));
    //List<SerializableChunkData.SectionData> sectData = new ArrayList<>(sect.size());
    // And process sections
    for (int i = 0; i < sections.size(); i++) {
      Optional<CompoundTag> optional = sections.getCompound(i);
//if (compound.isEmpty()) Ostrov.log("sectionBuilder "+x+":"+z+" sect="+i+" compound.isEmpty()");
      if (optional.isEmpty()) {
        continue;
      }
      CompoundTag sectionData = optional.get();
        //GenericNBTCompound sec = sect.getCompound(i);
      int secnum = sectionData.getByteOr("Y", (byte) 0);//sec.getByte("Y");

//Ostrov.log("sectionBuilder "+x+":"+z+" sect="+secnum+" block_states?"+sectionData.contains("block_states")
//    +" BlockLight?"+sectionData.contains("BlockLight")
//    +" SkyLight?"+sectionData.contains("SkyLight")
//    +" biomes?"+sectionData.contains("biomes"));

//StringBuilder t = new StringBuilder("sectionBuilder "+x+":"+z+" sect="+secnum+" tags:");
//for (String s : sectionData.keySet()) {
//  t.append(s).append(",");//.append(sectionData.getStringOr(s,"")).append(",");
//}

      DynmapBlockState[] palette = null;

      if (sectionData.contains("block_states")) {
        CompoundTag block_states = sectionData.getCompoundOrEmpty("block_states");
//Ostrov.log("---block_states PALETTE_TAG?"+block_states.contains(StructureTemplate.PALETTE_TAG));
        // If we've got palette, process non-empty section
//t.append(" block_states:");
//for (String s : block_states.keySet()) {
//  t.append(s).append(",");//.append(sectionData.getStringOr(s,"")).append(",");
//}
        if (block_states.contains(StructureTemplate.PALETTE_TAG)) {//StructureTemplate.PALETTE_TAG if (block_states.contains("palette")) {//, GenericNBTCompound.TAG_LIST)) {
          ListTag plist = block_states.getListOrEmpty(StructureTemplate.PALETTE_TAG);//StructureTemplate.PALETTE_TAG
//t.append("palette").append(" size=").append(plist.size());
          //long[] statelist = block_states.contains("data", GenericNBTCompound.TAG_LONG_ARRAY) ? block_states.getLongArray("data") : new long[4096 / 64]; // Handle zero bit palette (all same)
          long[] statelist = block_states.contains("data") ? block_states.getLongArray("data").get() : new long[4096 / 64]; // Handle zero bit palette (all same)
          palette = new DynmapBlockState[plist.size()];
          for (int pi = 0; pi < plist.size(); pi++) {
            optional = plist.getCompound(pi);
            if (optional.isEmpty()) continue;
            CompoundTag tc = optional.get();
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
//t.append(" pname=").append(pname).append("/DBS=").append(palette[pi].blockName).append(",");
          }

          SimpleBitStorage db = null;//GenericBitStorage db = null;
          DataBitsPacked dbp = null;
          int bitsperblock = (statelist.length * 64) / 4096;
          int expectedStatelistLength = (4096 + (64 / bitsperblock) - 1) / (64 / bitsperblock);
          if (statelist.length == expectedStatelistLength) {
            db = new SimpleBitStorage(bitsperblock, 4096, statelist);//chunkData.makeBitStorage(bitsperblock, 4096, statelist);
          } else {
            bitsperblock = (statelist.length * 64) / 4096;
            dbp = new DataBitsPacked(bitsperblock, 4096, statelist);
          }
          //if (bitsperblock > 8) {    // Not palette
          //	for (int j = 0; j < 4096; j++) {
          //		int v = db != null ? db.get(j) : dbp.getAt(j);
          //    	sectionBuilder.xyzBlockState(j & 0xF, (j & 0xF00) >> 8, (j & 0xF0) >> 4, DynmapBlockState.getStateByGlobalIndex(v));
          //	}
          //}
          //else {
          sectionBuilder.xyzBlockStatePalette(palette);  // Set palette
          for (int j = 0; j < 4096; j++) {
            int v = db != null ? db.get(j) : dbp.getAt(j);
            sectionBuilder.xyzBlockStateInPalette(j & 0xF, (j & 0xF00) >> 8, (j & 0xF0) >> 4, (short) v);
          }
          //}
        }
      }
//t.append(", BlockLight=").append(sectionData.contains("BlockLight"));
      if (sectionData.contains("BlockLight")) {
        sectionBuilder.emittedLight(sectionData.getByteArray("BlockLight").get());
      }
//t.append(", SkyLight=").append(sectionData.contains("SkyLight"));
      if (sectionData.contains("SkyLight")) {
        sectionBuilder.skyLight(sectionData.getByteArray("SkyLight").get());
        hasLight = true;
      }

      // If section biome palette
//t.append(", biomes=").append(sectionData.contains("biomes"));
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
          sectionBuilder.xyzBiome(j & 0x3, (j & 0x30) >> 4, (j & 0xC) >> 2, BiomeMap.byBiomeName(bpalette.getStringOr(b, "")));
        }
      } else {  // Else, apply legacy biomes
        if (old3d != null) {
          BiomeMap[] m = old3d.get((secnum > 0) ? ((secnum < old3d.size()) ? secnum : old3d.size() - 1) : 0);
          if (m != null) {
            for (int j = 0; j < 64; j++) {
              sectionBuilder.xyzBiome(j & 0x3, (j & 0x30) >> 4, (j & 0xC) >> 2, m[j]);
            }
          }
        } else if (old2d != null) {
          for (int j = 0; j < 256; j++) {
            sectionBuilder.xzBiome(j & 0xF, (j & 0xF0) >> 4, old2d[j]);
          }
        }
      }
//Ostrov.log(t.toString());
      // Finish and add section
      chunkBuilder.addSection(secnum, sectionBuilder.build());
      sectionBuilder.reset();
    }
    // If no light, do simple generate
    if (!hasLight) {
      chunkBuilder.generateSky();
    }
//Ostrov.log("");
    return chunkBuilder.build();
  }


  public void setChunks(DynmapWorld dw, List<DynmapChunk> chunks) {
    this.link = dw.worldLink;//new WeakReference<>(dw.world());
    super.setChunks(dw, chunks);
  }

  @Override
  public int getFoliageColor(BiomeMap bm, int[] colormap, int x, int z) {
    return colormap[bm.biomeLookup()];//bm.<BiomeBase>getBiomeObject().map(BiomeBase::h).flatMap(BiomeFog::e).orElse(colormap[bm.biomeLookup()]); // BiomeBase::getSpecialEffects, BiomeFog::skyColor
  }

  @Override
  public int getGrassColor(BiomeMap bm, int[] colormap, int x, int z) {
    //BiomeFog fog = bm.<BiomeBase>getBiomeObject().map(BiomeBase::h).orElse(null); // BiomeBase::getSpecialEffects
    //if (fog == null)
    return colormap[bm.biomeLookup()];
    //return fog.g().a(x, z, fog.f().orElse(colormap[bm.biomeLookup()])); // BiomeFog.getGrassColorModifier, BiomeFog.getGrassColorOverride
  }


}


// If we've got palette and block states list, process non-empty section
    /*  if (sectionData.contains("Palette") && sectionData.contains("BlockStates")) {
      //if (sec.contains("Palette", 9) && sec.contains("BlockStates", 12)) {
        ListTag plist = sectionData.getListOrEmpty("Palette");//GenericNBTList plist = sec.getList("Palette", 10);
        long[] statelist = sectionData.getLongArray("BlockStates").get();
        palette = new DynmapBlockState[plist.size()];
        for (int pi = 0; pi < plist.size(); pi++) {
          compound = sections.getCompound(i);//GenericNBTCompound tc = plist.getCompound(pi);
          if (compound.isEmpty()) continue;
          compoundTag = compound.get();
          String pname = compoundTag.getStringOr("Name", "");
          if (compoundTag.contains("Properties")) {
            StringBuilder statestr = new StringBuilder();
            CompoundTag prop = compoundTag.getCompound("Properties").get();
            for (String pid : prop.keySet()) {
              if (statestr.length() > 0) statestr.append(',');
              statestr.append(pid).append('=').append(prop.getStringOr(pid,""));//statestr.append(pid).append('=').append(prop.getAsString(pid));
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
        SimpleBitStorage db = null;
        DataBitsPacked dbp = null;
        try {
          db = new SimpleBitStorage(bitsperblock, 4096, statelist);//chunkData.makeBitStorage(bitsperblock, 4096, statelist);
        } catch (Exception ex) {	// Handle legacy encoded
          bitsperblock = (statelist.length * 64) / 4096;
          dbp = new DataBitsPacked(bitsperblock, 4096, statelist);
        }
        if (bitsperblock > 8) {	// Not palette
          for (int j = 0; j < 4096; j++) {
            int v = (dbp != null) ? dbp.getAt(j) : db.get(j);
            sectionBuilder.xyzBlockState(j & 0xF, (j & 0xF00) >> 8, (j & 0xF0) >> 4, DynmapBlockState.getStateByGlobalIndex(v));
          }
        }
        else {
          sectionBuilder.xyzBlockStatePalette(palette);	// Set palette
          for (int j = 0; j < 4096; j++) {
            int v = db != null ? db.get(j) : dbp.getAt(j);
            sectionBuilder.xyzBlockStateInPalette(j & 0xF, (j & 0xF00) >> 8, (j & 0xF0) >> 4, (short)v);
          }
        }

      } else */