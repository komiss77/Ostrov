package ru.komiss77.modules.world;


import java.util.Random;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;



//https://hub.spigotmc.org/javadocs/spigot/org/bukkit/generator/ChunkGenerator.ChunkData.html



public class LavaOceanGenerator extends ChunkGenerator {

    Plugin plugin;

    public LavaOceanGenerator(Plugin plugin) {
//System.out.print("-------------- new EmptyChunkGenerator 1");
        this.plugin = plugin;
    }

    
    
    
	@Override
    @Nonnull
    @SuppressWarnings("deprecation")    //         мир               случайность    чакн х  чанк z          биом
    public ChunkData generateChunkData(@Nonnull World world, @Nonnull Random random, int x, int z, @Nonnull BiomeGrid biomeGrid) {

        final ChunkData cd = createChunkData(world);
        
        if (x==0 && z==0) { //нулевой чанк - строим блок бедрока
//System.out.print("--        generateChunkData 1 x="+x+" z="+z);
            final Location loc = new Location(world, 0, 65, 0);
            cd.setBlock(0, 64, 0, Material.BEDROCK);
            world.setSpawnLocation(loc);
            //return cd;
            
        }// else {
            
            
            
       // }
        
        //Set a region of this chunk from xMin, yMin, zMin (inclusive) to xMax, yMax, zMax (exclusive) to material
        cd.setRegion(0, 0, 0, 16, 1, 16, Material.BEDROCK);
        cd.setRegion(0, 1, 0, 16, 11, 16, Material.LAVA);

        return cd;
        
        
    }
    

    
    
    
        
    @Override
    public Location getFixedSpawnLocation(@Nonnull World world, @Nonnull Random random) {
      return new Location(world, 0, 64, 0);
      //return null;
    }    
       
    
    
    
    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }
    
    
    
    
    
    
    
    
    
       // @Override
   // public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid) {
   //     byte[][] result = new byte[world.getMaxHeight() / 16][];
        
      /*  Iterator it = this.plugin.blocks.iterator();

        while (it.hasNext()) {
            BedrockCoords block = (BedrockCoords) it.next();

            if (block.x >= chunkX * 16 && block.x < (chunkX + 1) * 16 && block.z >= chunkZ * 16 && block.z < (chunkZ + 1) * 16) {
                int x = block.x % 16;

                if (x < 0) {
                    x += 16;
                }

                int z = block.z % 16;

                if (z < 0) {
                    z += 16;
                }

                this.setBlock(result, x, block.y, z, (byte) 7);
                it.remove();
            }
        }*/

  //      return result;
  //  }

    /*@Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0, 100, 0);
    }*/

  /*  private void setBlock(byte[][] result, int x, int y, int z, byte blkid) {
        if (result[y >> 4] == null) {
            result[y >> 4] = new byte[4096];
        }

        result[y >> 4][(y & 15) << 8 | z << 4 | x] = blkid;
    }*/
}
