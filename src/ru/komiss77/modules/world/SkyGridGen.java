// 
// Decompiled by Procyon v0.5.36
// 

package ru.komiss77.modules.world;

import org.bukkit.generator.ChunkGenerator;


public class SkyGridGen extends ChunkGenerator
{
 /*   private final int islandHeight = 255;
    private final Map<World.Environment, BiomeGenerator> biomeGenerator;
    private final BlockPopulator populator;
    private final SkyGridChunks preMade;
    
    public SkyGridGen() {
        this.populator = new SkyGridPop();
        this.preMade = new SkyGridChunks();
        biomeGenerator = new EnumMap<>(World.Environment.class);
    }
    
    @Override
    public ChunkGenerator.ChunkData generateChunkData(final World world, final Random random, final int chunkX, final int chunkZ, final ChunkGenerator.BiomeGrid biomeGrid) {
        final ChunkGenerator.ChunkData result = this.createChunkData(world);
        preMade.getSkyGridChunk(world.getEnvironment()).forEach(b -> result.setBlock(b.getX(), b.getY(), b.getZ(), b.getBd()));
        //if (addon.getSettings().isCreateBiomes()) {
            for (int x = 0; x < 16; x += 4) {
                for (int z = 0; z < 16; z += 4) {
                    final int realX = x + chunkX * 16;
                    final int realZ = z + chunkZ * 16;
                    final Biome b2 = biomeGenerator.computeIfAbsent(world.getEnvironment(), k -> new BiomeGenerator(world)).getDominantBiome(realX, realZ);
                    for (int y = 0; y < world.getMaxHeight(); y += 4) {
                        biomeGrid.setBiome(x, y, z, b2);
                    }
                }
            }
        //}
        return result;
    }
    
    @Override
    public List<BlockPopulator> getDefaultPopulators(final World world) {
        return Collections.singletonList(this.populator);
    }
    
    @Override
    public Location getFixedSpawnLocation(final World world, final Random random) {
        return new Location(world, 0.0, islandHeight + 2.0, 0.0);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    


    private static final List<Material> NEEDS_DIRT;
    static {
        NEEDS_DIRT = List.of(new Material[] { Material.ACACIA_SAPLING, Material.ALLIUM, Material.AZURE_BLUET, Material.BEETROOTS, Material.BIRCH_SAPLING, Material.BLUE_ORCHID, Material.BROWN_MUSHROOM, Material.DANDELION, Material.DARK_OAK_SAPLING, Material.DEAD_BUSH, Material.FERN, Material.GRASS, Material.JUNGLE_SAPLING, Material.LARGE_FERN, Material.LILAC, Material.OAK_SAPLING, Material.ORANGE_TULIP, Material.OXEYE_DAISY, Material.PEONY, Material.PINK_TULIP, Material.POPPY, Material.RED_MUSHROOM, Material.RED_TULIP, Material.ROSE_BUSH, Material.SPRUCE_SAPLING, Material.SUGAR_CANE, Material.SUNFLOWER, Material.TALL_GRASS, Material.WHEAT, Material.WHITE_TULIP });
    }

    public class SkyGridChunks
    {
        private static final int PRE_MADE_CHUNKS_NUMBER = 100;
        private final Random random;
        private final List<List<SkyGridBlock>> chunks;
        private final List<List<SkyGridBlock>> chunksEnd;
        private final List<List<SkyGridBlock>> chunksNether;

        public SkyGridChunks() {
            this.random = new Random(System.currentTimeMillis());
            this.chunks = new ArrayList<>();
            this.chunksEnd = new ArrayList<>();
            this.chunksNether = new ArrayList<>();
            final BlockProbability prob = addon.getWorldStyles().get(World.Environment.NORMAL).getProb();
            final BlockProbability probNether = addon.getWorldStyles().get(World.Environment.NETHER).getProb();
            final BlockProbability probEnd = addon.getWorldStyles().get(World.Environment.THE_END).getProb();
            Ostrov.log_ok("Making chunks for SkyGrid");
            for (int i = 0; i < 100; ++i) {
                this.chunks.add(this.getChunk(prob));
                this.chunksNether.add(this.getChunk(probNether));
                this.chunksEnd.add(this.getChunk(probEnd));
            }
            Ostrov.log_ok("Done making chunks");
        }

        private List<SkyGridBlock> getChunk(final BlockProbability prob) {
            final List<SkyGridBlock> result = new ArrayList<>();
            for (int x = 1; x < 16; x += 4) {
                for (int z = 1; z < 16; z += 4) {
                    for (int y = 0; y <= islandHeight; y += 4) {
                        this.setBlock(prob, x, y, z, result);
                    }
                }
            }
            return result;
        }

        public List<SkyGridBlock> getSkyGridChunk(final World.Environment env) {
            List<SkyGridBlock> list;// = null;
            switch (env) {
                case NETHER: {
                    list = this.chunksNether.get(this.random.nextInt(this.chunksNether.size()));
                    break;
                }
                case THE_END: {
                    list = this.chunksEnd.get(this.random.nextInt(this.chunksEnd.size()));
                    break;
                }
                default: {
                    list = this.chunks.get(this.random.nextInt(this.chunks.size()));
                    break;
                }
            }
            return list;
        }

        private void setBlock(final BlockProbability prob, final int x, final int y, final int z, final List<SkyGridBlock> result) {
            Material blockMat = prob.getBlock(this.random, y == 0, false);
            if (!blockMat.isAir() && !blockMat.isBlock()) {
                blockMat = Material.STONE;
            }
            if (SkyGridChunks.NEEDS_DIRT.contains(blockMat)) {
                result.add(new SkyGridBlock(x, y, z, Material.DIRT.createBlockData()));
                final BlockData blockData;
                final BlockData dataBottom = blockData = blockMat.createBlockData();
                if (blockData instanceof Bisected) {
                    final Bisected bisected = (Bisected)blockData;
                    bisected.setHalf(Bisected.Half.BOTTOM);
                    final BlockData dataTop = blockMat.createBlockData();
                    bisected.setHalf(Bisected.Half.TOP);
                    result.add(new SkyGridBlock(x, y + 1, z, dataBottom));
                    result.add(new SkyGridBlock(x, y + 2, z, dataTop));
                }
                else {
                    result.add(new SkyGridBlock(x, y + 1, z, blockMat.createBlockData()));
                }
                if (blockMat.equals((Object)Material.SUGAR_CANE)) {
                    result.add(new SkyGridBlock(x + 1, y, z, Material.WATER));
                }
            }
            else {
                switch (blockMat) {
                    case CACTUS: {
                        result.add(new SkyGridBlock(x, y, z, Material.SAND));
                        result.add(new SkyGridBlock(x, y - 1, z, Material.SANDSTONE));
                        result.add(new SkyGridBlock(x, y + 1, z, blockMat));
                        break;
                    }
                    case NETHER_WART: {
                        result.add(new SkyGridBlock(x, y, z, Material.SOUL_SAND));
                        result.add(new SkyGridBlock(x, y + 1, z, blockMat));
                        break;
                    }
                    case END_ROD:
                    case CHORUS_PLANT: {
                        result.add(new SkyGridBlock(x, y, z, Material.END_STONE));
                        result.add(new SkyGridBlock(x, y + 1, z, blockMat));
                        break;
                    }
                    default: {
                        result.add(new SkyGridBlock(x, y, z, blockMat));
                        break;
                    }
                }
            }
        }


    }














    
    
    public class SkyGridBlock
    {
        private final Biome biome;
        private final BlockData bd;
        private final int x;
        private final int y;
        private final int z;

        public SkyGridBlock(final int x, final int y, final int z, final BlockData blockData) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.bd = blockData;
            this.biome = Biome.BADLANDS;
        }

        public SkyGridBlock(final int x, final int y, final int z, final Material m) {
            this(x, y, z, m.createBlockData());
        }

        public Biome getBiome() {
            return this.biome;
        }

        public BlockData getBd() {
            return this.bd;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getZ() {
            return this.z;
        }
    }    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    public class BlockProbability
    {
        private final NavigableMap<Integer, Material> probMap;
        private int total;

        public BlockProbability() {
            this.probMap = new TreeMap<>();
            this.total = 0;
        }

        public void addBlock(final Material material, final int prob) {
            this.total += prob;
            this.probMap.put(this.total, material);
        }

        public Material getBlock(final Random random, final boolean bottom, final boolean noLiquid) {
            Material temp = this.probMap.get(random.nextInt(this.total));
            if (temp == null) {
                temp = this.probMap.ceilingEntry(random.nextInt(this.total)).getValue();
            }
            if (temp == null) {
                temp = this.probMap.firstEntry().getValue();
            }
            if (bottom && temp.equals((Object)Material.CACTUS)) {
                return this.getBlock(random, true, noLiquid);
            }
            if (noLiquid && (temp.equals((Object)Material.WATER) || temp.equals((Object)Material.LAVA))) {
                return this.getBlock(random, bottom, true);
            }
            return temp;
        }

        public int getSize() {
            return this.probMap.size();
        }

        public boolean isEmpty() {
            return this.probMap.isEmpty();
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public class BiomeGenerator
    {
        private final PerlinOctaveGenerator temperatureGen;
        private final PerlinOctaveGenerator rainfallGen;
        private final World.Environment env;

        public BiomeGenerator(final World world) {
            (this.temperatureGen = new PerlinOctaveGenerator(world.getSeed(), 16)).setScale(0.01);
            (this.rainfallGen = new PerlinOctaveGenerator(world.getSeed() + 1L, 15)).setScale(0.01);
            this.env = world.getEnvironment();
        }

        public Biome getDominantBiome(final int realX, final int realZ) {
            final Map<Biomes, Double> biomes = Biomes.getBiomes(this.env, Math.abs(this.temperatureGen.noise((double)realX, (double)realZ, 0.5, 0.5) * 100.0), Math.abs(this.rainfallGen.noise((double)realX, (double)realZ, 0.5, 0.5) * 100.0));
            double maxNoiz = 0.0;
            Biomes maxBiome = null;
            for (final Map.Entry<Biomes, Double> biome : biomes.entrySet()) {
                if (biome.getValue() >= maxNoiz) {
                    maxNoiz = biome.getValue();
                    maxBiome = biome.getKey();
                }
            }
            return Objects.requireNonNull(maxBiome).biome;
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public enum Biomes
    {
        SNOWY_TUNDRA(World.Environment.NORMAL, Biome.SNOWY_TUNDRA, 0.0, 100.0), 
        SNOWY_TAIGA(World.Environment.NORMAL, Biome.SNOWY_TAIGA, 0.0, 100.0), 
        FROZEN_RIVER(World.Environment.NORMAL, Biome.FROZEN_RIVER, 0.0, 10.0), 
        SNOWY_BEACH(World.Environment.NORMAL, Biome.SNOWY_BEACH, 0.0, 100.0), 
        MOUNTAINS(World.Environment.NORMAL, Biome.MOUNTAINS, 20.0, 60.0), 
        WOODED_MOUNTAINS(World.Environment.NORMAL, Biome.WOODED_MOUNTAINS, 20.0, 60.0), 
        DESERT(World.Environment.NORMAL, Biome.DESERT, 60.0, 4.0), 
        FOREST(World.Environment.NORMAL, Biome.FOREST, 50.0, 60.0), 
        PLAINS(World.Environment.NORMAL, Biome.PLAINS, 40.0, 30.0), 
        SWAMP(World.Environment.NORMAL, Biome.SWAMP, 40.0, 70.0), 
        JUNGLE(World.Environment.NORMAL, Biome.JUNGLE, 60.0, 50.0), 
        SAVANNA(World.Environment.NORMAL, Biome.SAVANNA, 40.0, 10.0), 
        DESERT_HILLS(World.Environment.NORMAL, Biome.DESERT_HILLS, 60.0, 5.0), 
        TAIGA(World.Environment.NORMAL, Biome.TAIGA, 30.0, 5.0), 
        NETHER_WASTES(World.Environment.NETHER, Biome.NETHER_WASTES, 40.0, 30.0), 
        SOUL_SAND_VALLEY(World.Environment.NETHER, Biome.SOUL_SAND_VALLEY, 40.0, 70.0), 
        CRIMSON_FOREST(World.Environment.NETHER, Biome.CRIMSON_FOREST, 50.0, 60.0), 
        WARPED_FOREST(World.Environment.NETHER, Biome.WARPED_FOREST, 20.0, 60.0), 
        BASALT_DELTAS(World.Environment.NETHER, Biome.BASALT_DELTAS, 20.0, 50.0), 
        THE_END(World.Environment.THE_END, Biome.THE_END, 40.0, 30.0), 
        SMALL_END_ISLANDS(World.Environment.THE_END, Biome.SMALL_END_ISLANDS, 0.0, 100.0), 
        END_MIDLANDS(World.Environment.THE_END, Biome.END_MIDLANDS, 50.0, 60.0), 
        END_HIGHLANDS(World.Environment.THE_END, Biome.END_HIGHLANDS, 20.0, 60.0), 
        END_BARRENS(World.Environment.THE_END, Biome.END_BARRENS, 60.0, 4.0);

        public final World.Environment env;
        public final Biome biome;
        public final double optimumTemperature;
        public final double optimumRainfall;

        private Biomes(final World.Environment env, final Biome biome, final double temp, final double rain) {
            this.env = env;
            this.biome = biome;
            this.optimumTemperature = temp;
            this.optimumRainfall = rain;
        }

        public static Map<Biomes, Double> getBiomes(final World.Environment env, final double temp, final double rain) {
            final Map<Biomes, Double> biomes = new EnumMap<>(Biomes.class);
            Biomes closestBiome = null;
            Biomes secondClosestBiome = null;
            Biomes thirdClosestBiome = null;
            double closestDist = 1.0E7;
            double secondClosestDist = 1.0E7;
            double thirdClosestDist = 1.0E7;
            for (final Biomes biome : values()) {
                if (env.equals((Object)biome.env)) {
                    final double dist = getSquaredDistance(biome, temp, rain);
                    if (dist <= closestDist) {
                        thirdClosestDist = secondClosestDist;
                        thirdClosestBiome = secondClosestBiome;
                        secondClosestDist = closestDist;
                        secondClosestBiome = closestBiome;
                        closestDist = dist;
                        closestBiome = biome;
                    }
                    else if (dist <= secondClosestDist) {
                        thirdClosestDist = secondClosestDist;
                        thirdClosestBiome = secondClosestBiome;
                        secondClosestDist = dist;
                        secondClosestBiome = biome;
                    }
                    else if (dist <= thirdClosestDist) {
                        thirdClosestDist = dist;
                        thirdClosestBiome = biome;
                    }
                }
            }
            biomes.put(closestBiome, 10.0 / Math.sqrt(closestDist));
            biomes.put(secondClosestBiome, 10.0 / Math.sqrt(secondClosestDist));
            biomes.put(thirdClosestBiome, 10.0 / Math.sqrt(thirdClosestDist));
            return biomes;
        }

        private static double getSquaredDistance(final Biomes biome, final double temp, final double rain) {
            return Math.abs((biome.optimumTemperature - temp) * (biome.optimumTemperature - temp) + (biome.optimumRainfall - rain) * (biome.optimumRainfall - rain));
        }


    }
    
    



















    private static final Material[] SAPLING_TYPE;
    private static final RandomSeries slt;

    static {
        slt = new RandomSeries(27);
        SAPLING_TYPE = new Material[] { Material.ACACIA_SAPLING, Material.BIRCH_SAPLING, Material.DARK_OAK_SAPLING, Material.JUNGLE_SAPLING, Material.OAK_SAPLING, Material.SPRUCE_SAPLING };
    }




public class SkyGridPop extends BlockPopulator
{
    private final int size;
    private final List<Material> chestItemsWorld;
    private final List<Material> chestItemsNether;
    private final List<Material> chestItemsEnd;
    private Random random;
    private Chunk chunk;
    private static final String LOADED = "Loaded ";
    
    public SkyGridPop() {
        this.size = islandHeight;
        chestItemsWorld = addon.getSettings().getChestItemsOverworld().stream().map((Function<? super Object, ? extends Material>)Material::matchMaterial).filter(Objects::nonNull).toList();
        chestItemsNether = addon.getSettings().getChestItemsNether().stream().map((Function<? super Object, ? extends Material>)Material::matchMaterial).filter(Objects::nonNull).toList();
        chestItemsEnd = addon.getSettings().getChestItemsEnd().stream().map((Function<? super Object, ? extends Material>)Material::matchMaterial).filter(Objects::nonNull).toList();
        Ostrov.log_ok("сундуки для мира: "+chestItemsWorld.size());//addon.log(invokedynamic(makeConcatWithConstants:(I)Ljava/lang/String;, this.chestItemsWorld.size()));
        Ostrov.log_ok("сундуки для ада: "+chestItemsNether.size());//addon.log(invokedynamic(makeConcatWithConstants:(I)Ljava/lang/String;, this.chestItemsNether.size()));
        Ostrov.log_ok("сундуки для края: "+chestItemsEnd.size());//addon.log(invokedynamic(makeConcatWithConstants:(I)Ljava/lang/String;, this.chestItemsEnd.size()));
    }
    
    @Deprecated
    public void populate(final World world, final Random random, final Chunk chunk) {
        this.random = random;
        this.chunk = chunk;
        for (int x = 1; x < 16; x += 4) {
            for (int z = 1; z < 16; z += 4) {
                for (int y = 0; y <= this.size; y += 4) {
                    this.alterBlocks(chunk.getBlock(x, y, z));
                }
            }
        }
        //if (this.addon.getSettings().isEndGenerate() && world.getEnvironment().equals((Object)World.Environment.NORMAL) && random.nextDouble() < this.addon.getSettings().getEndFrameProb()) {
            this.makeEndPortal();
        //}
    }
    
    private void alterBlocks(final Block b) {
        switch (b.getType()) {
            case CHEST: {
                this.setChest(b);
                break;
            }
            case SPAWNER: {
                this.setSpawner(b);
                break;
            }
            case DIRT: {
                if (!(b.getRelative(BlockFace.UP).getBlockData() instanceof Sapling)) {
                    break;
                }
                if (b.getBiome().equals((Object)Biome.DESERT)) {
                    b.setType(Material.SAND, false);
                    break;
                }
                this.setSaplingType(b.getRelative(BlockFace.UP));
                break;
            }
        }
    }
    
    private void makeEndPortal() {
        for (int xx = 1; xx < 6; ++xx) {
            for (int zz = 1; zz < 6; ++zz) {
                if (xx != zz && (xx != 1 || zz != 5) && (xx != 5 || zz != 1)) {
                    if (xx <= 1 || xx >= 5 || zz <= 1 || zz >= 5) {
                        this.setFrame(xx, zz, this.chunk.getBlock(xx, 0, zz));
                    }
                }
            }
        }
    }
    
    private void setFrame(final int xx, final int zz, final Block frame) {
        frame.setType(Material.END_PORTAL_FRAME, false);
        final EndPortalFrame endFrame = (EndPortalFrame)frame.getBlockData();
        endFrame.setEye(this.random.nextDouble() < 0.8);
        if (zz == 1) {
            endFrame.setFacing(BlockFace.SOUTH);
        }
        else if (zz == 5) {
            endFrame.setFacing(BlockFace.NORTH);
        }
        else if (xx == 1) {
            endFrame.setFacing(BlockFace.EAST);
        }
        else {
            endFrame.setFacing(BlockFace.WEST);
        }
        frame.setBlockData((BlockData)endFrame, false);
    }
    
    private void setSaplingType(final Block b) {
        switch (b.getBiome()) {
            case JUNGLE: {
                b.setType(Material.JUNGLE_SAPLING, false);
                break;
            }
            case PLAINS: {
                if (this.random.nextBoolean()) {
                    b.setType(Material.BIRCH_SAPLING, false);
                    break;
                }
                break;
            }
            case TAIGA: {
                b.setType(Material.SPRUCE_SAPLING, false);
                break;
            }
            case SWAMP: {
                break;
            }
            case DESERT:
            case DESERT_HILLS: {
                b.setType(Material.DEAD_BUSH, false);
                break;
            }
            case SAVANNA: {
                b.setType(Material.ACACIA_SAPLING, false);
                break;
            }
            default: {
                b.setType(SAPLING_TYPE[this.random.nextInt(6)], false);
                break;
            }
        }
    }
    
    private void setSpawner(final Block b) {
        final CreatureSpawner spawner = (CreatureSpawner)b.getState();
        final NavigableMap<Integer, EntityType> spawns = this.addon.getWorldStyles().get(b.getWorld().getEnvironment()).getSpawns();
        final int randKey = this.random.nextInt(spawns.lastKey());
        final EntityType type = spawns.ceilingEntry(randKey).getValue();
        spawner.setDelay(120);
        spawner.setSpawnedType(type);
        spawner.update(true, false);
    }
    
    private void setChest(final Block b) {
        final Chest chest = (Chest)b.getState();
        final Inventory inv = chest.getBlockInventory();
        slt.reset();
        switch (b.getWorld().getEnvironment()) {
            case NETHER: {
                for (int i = 0; !this.chestItemsNether.isEmpty() && i < this.addon.getSettings().getChestFillNether() && i < 27; ++i) {
                    final ItemStack item = new ItemStack((Material)this.chestItemsNether.get(this.random.nextInt(this.chestItemsNether.size())));
                    inv.setItem(slt.next(), item);
                }
                break;
            }
            case THE_END: {
                for (int i = 0; !this.chestItemsNether.isEmpty() && i < this.addon.getSettings().getChestFillEnd() && i < 27; ++i) {
                    final ItemStack item = new ItemStack((Material)this.chestItemsEnd.get(this.random.nextInt(this.chestItemsEnd.size())));
                    inv.setItem(slt.next(), item);
                }
                break;
            }
            default: {
                for (int i = 0; !this.chestItemsNether.isEmpty() && i < this.addon.getSettings().getChestFill() && i < 27; ++i) {
                    final ItemStack item = new ItemStack((Material)this.chestItemsWorld.get(this.random.nextInt(this.chestItemsWorld.size())));
                    inv.setItem(slt.next(), item);
                }
                break;
            }
        }
    }
    

}
    
    */
}









