package ru.komiss77.modules.world;

import java.util.Random;
import javax.annotation.Nonnull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

//https://hub.spigotmc.org/javadocs/spigot/org/bukkit/generator/ChunkGenerator.ChunkData.html
public class EmptyChunkGenerator extends ChunkGenerator {

    
    
    //@Override
    //public void generateNoise(WorldInfo worldInfo, Random r, int x, int z, ChunkData result) {
   //     result.setRegion(0, worldInfo.getMinHeight(), 0, 16, worldInfo.getMaxHeight(), 16, Material.AIR); //очистка
   //     if (x>16 || x<-16 || z>16 || z<-16) return;
        //далее - заполнение контентом из зараннее подготовленного чанка
        //preMade.getSkyGridChunk(worldInfo.getEnvironment()).forEach(b -> result.setBlock(b.getX(), b.getY(), b.getZ(), b.getBd()));
   // }
    
    
    @Override
    @Nonnull
    @SuppressWarnings("deprecation")   //         мир               случайность    чакн х  чанк z          биом
    public ChunkData generateChunkData(@Nonnull World world, @Nonnull Random random, int x, int z, @Nonnull BiomeGrid biomeGrid) {

        final ChunkData cd = createChunkData(world);
        
        if (x==0 && z==0) { //нулевой чанк - строим блок бедрока
//System.out.print("--        generateChunkData 1 x="+x+" z="+z);
            final Location loc = new Location(world, 0, 65, 0);
            cd.setBlock(0, 64, 0, Material.BEDROCK);
            world.setSpawnLocation(loc);
            return cd;
            
        }// else {
        //}
        return cd;
    }
    

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }
    
    
    
    
        
    @Override
    public Location getFixedSpawnLocation(@Nonnull World world, @Nonnull Random random) {
      return new Location(world, 0, 100, 0);
      //return null;
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


}
