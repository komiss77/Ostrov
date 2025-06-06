package ru.komiss77.modules.world;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockType;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Ostrov;
import ru.komiss77.notes.ThreadSafe;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.version.Craft;


public class Schematic {

    private final String name;
    private String param = "";
    protected final HashMap<Integer, Material> blocks = new HashMap<>(); //только блоки, воздух пропускается. Исключение - STRUCTURE_VOID запоминается как AIR принудительно
    protected final HashMap<Integer, String> blockDatas = new HashMap<>();
    protected final HashMap<Integer, String> blockStates = new HashMap<>();
    private Environment createdEnvironment = Environment.NORMAL;
    private Biome createdBiome = Biome.DEEP_OCEAN;
    protected boolean ready; //флаг для вставки - пока false вставлять не начнёт	
    private int dX, dY, dZ;
    public int spawnAddX, spawnAddY, spawnAddZ, spawnYaw, spawnPitch;
    
    public static final String DEF_PATH = Ostrov.instance.getDataPath() + "/schematics";
    public static final String DEF_EXT = ".schem";

    public Schematic(final CommandSender cs, final String name, final String param, final Location min, final Location max, final boolean save) {
        this.name = name;
        create(cs, name, param, BVec.of(min), BVec.of(max), min, save, DEF_PATH, DEF_EXT, (Set<BlockType>) null);
    }

    public Schematic(final CommandSender cs, final String name, final String param, final Cuboid cuboid, final World world, final boolean save) {
        this.name = name;
        create(cs, name, param, BVec.of(cuboid.getLowerLocation(world)), BVec.of(cuboid.getHightesLocation(world)),
            cuboid.getSpawnLocation(world), save, DEF_PATH, DEF_EXT, (Set<BlockType>) null);
    }

    @Deprecated
    public Schematic(final CommandSender cs, final String name, final String param, final Location min, final Location max, final boolean save, final String folder, final String ext, final List<Material> skip) {
        this.name = name;
        create(cs, name, param, BVec.of(min), BVec.of(max), min, save, folder, ext, skip);
    }

    public Schematic(final CommandSender cs, final String name, final String param, final Location min, final Location max, final boolean save, final String folder, final String ext, final Set<BlockType> skip) {
        this.name = name;
        create(cs, name, param, BVec.of(min), BVec.of(max), min, save, folder, ext, skip);
    }
    //создание с местности -
    private void create(final CommandSender cs, final String name, final String param, final BVec min, final BVec max, final Location spawn, final boolean save, final String folder, final String ext, final Set<BlockType> skip) {
        final World world = spawn.getWorld();
        ServerLevel worldServer = Craft.toNMS(world);//VM.server().toNMS(world);

        this.param = param;
        final boolean hasSkipMat = skip != null && !skip.isEmpty();
        createdEnvironment = spawn.getWorld().getEnvironment();
        createdBiome = spawn.getBlock().getBiome();

        final long l = System.currentTimeMillis();
        final Cuboid cuboid = new Cuboid(min, max, spawn);
        dX = cuboid.maxX() - cuboid.minX();
        dY = cuboid.maxY() - cuboid.minY();
        dZ = cuboid.maxZ() - cuboid.minZ();
        spawnAddX = cuboid.spawnAddX;
        spawnAddY = cuboid.spawnAddY;
        spawnAddZ = cuboid.spawnAddZ;
        spawnYaw = cuboid.spawnYaw;
        spawnPitch = cuboid.spawnPitch;

        int count = 0;

        final BlockPos.MutableBlockPos mbp = new BlockPos.MutableBlockPos(0, 0, 0);

        @Deprecated
        final Iterator<XYZ> it = cuboid.iteratorXYZ(Rotate.r0);
        while (it.hasNext()) {
            @Deprecated
            final XYZ xyz = it.next();
            mbp.set(xyz.x, xyz.y, xyz.z);
            //BlockData nmsBlockState;
            final net.minecraft.world.level.block.state.BlockState nmsBlockState = worldServer.getBlockState(mbp);
            final Material mat = nmsBlockState.getBukkitMaterial();
            final BlockType bt = mat.asBlockType();

            if (bt == null || bt.isAir()) {
                count++;
                continue;
            }

            if (hasSkipMat && skip.contains(mat)) continue;

            //НЕ брать offSet(), в Iterator<XYZ> координаты уже другие!!
            final int xyzOffset = xyz.yaw;
            if (bt == BlockType.STRUCTURE_VOID) {
                blocks.put(xyzOffset, Material.AIR);
            } else if (bt == BlockType.WATER || bt == BlockType.LAVA) {
                if (Craft.fromNMS(nmsBlockState) instanceof final Levelled lv
                    && lv.getLevel() == 0) blocks.put(xyzOffset, mat);
            } else {
                final BlockEntity tileEntity = worldServer.getBlockEntity(mbp);
                if (tileEntity != null) {
                    final BlockState blockState = world.getBlockState(xyz.x, xyz.y, xyz.z);
                    //BlockState не даёт ASYNC если что!
                    final String bsAsString = getStringFromBlockState(blockState);
                    if (!bsAsString.isEmpty()) {
                        blockStates.put(xyzOffset, bsAsString);
                    }
                }
                final BlockData blockData = Craft.fromNMS(nmsBlockState);
                if (blockData != null) {
                    final String bdAsString = blockData.getAsString(true);
                    if (bdAsString.endsWith("]")) { //пишем только реальную дату!
                        blockDatas.put(xyzOffset, getStringFromBlockData(blockData));
                    }
                }
                blocks.put(xyzOffset, mat);
            }
        }

        if (save) Ostrov.async(() -> save(cs, folder, ext), 0);

        ready = true;
        if (cs == null) return;
        cs.sendMessage("§7=====================================================");
        cs.sendMessage("§fБлоки для схематика §b" + name + " §fотсканированы.");
        cs.sendMessage("§7Обработано блоков : §6" + count);
        cs.sendMessage("§7Hе пустых : §6" + blocks.size());
        cs.sendMessage("§7С blockData : §6" + blockDatas.size());
        cs.sendMessage("§7С blockState : §6" + blockStates.size());
        cs.sendMessage("Bремя: §5" + (System.currentTimeMillis() - l) + " мс.");
        cs.sendMessage("§7=====================================================");
    }

    @Deprecated
    //создание с местности -
    private void create(final CommandSender cs, final String name, final String param, final BVec min, final BVec max, final Location spawn, final boolean save, final String folder, final String ext, final List<Material> skip) {
        final World world = spawn.getWorld();
        ServerLevel worldServer = Craft.toNMS(world);//VM.server().toNMS(world);

        this.param = param;
        final boolean hasSkipMat = skip != null && !skip.isEmpty();
        createdEnvironment = spawn.getWorld().getEnvironment();
        createdBiome = spawn.getBlock().getBiome();

        final long l = System.currentTimeMillis();
        final Cuboid cuboid = new Cuboid(min, max, spawn);
        dX = cuboid.maxX() - cuboid.minX();
        dY = cuboid.maxY() - cuboid.minY();
        dZ = cuboid.maxZ() - cuboid.minZ();
        spawnAddX = cuboid.spawnAddX;
        spawnAddY = cuboid.spawnAddY;
        spawnAddZ = cuboid.spawnAddZ;
        spawnYaw = cuboid.spawnYaw;
        spawnPitch = cuboid.spawnPitch;

        Material mat;
        BlockData blockData;
        String bdAsString;
        Levelled lvl;
        String bsAsString;
        int xyzOffset;
        int count = 0;

        BlockState blockState;
        BlockPos.MutableBlockPos mutableBlockPosition = new BlockPos.MutableBlockPos(0, 0, 0);

        net.minecraft.world.level.block.state.BlockState nmsBlockState;//BlockData nmsBlockState;
        BlockEntity tileEntity;//TileEntity tileEntity;
        @Deprecated
        final Iterator<XYZ> it = cuboid.iteratorXYZ(Rotate.r0);
        while (it.hasNext()) {
            @Deprecated
            final XYZ xyz = it.next();
            mutableBlockPosition.set(xyz.x, xyz.y, xyz.z);
            nmsBlockState = worldServer.getBlockState(mutableBlockPosition);
            mat = nmsBlockState.getBukkitMaterial();
//Ostrov.log("x="+x+"/"+bps.u()+" y="+y+"/"+bps.v()+" z="+z+"/"+bps.w()+" mat="+mat.name());
//Ostrov.log("xyz= "+xyz.x+","+xyz.y+","+xyz.z);

            if (mat != Material.AIR) {
                if (hasSkipMat && skip.contains(mat)) continue;

                xyzOffset = xyz.yaw; //НЕ брать offSet(), в Iterator<XYZ> координаты уже другие!!

                switch (mat) {

                    case STRUCTURE_VOID -> //принудительно ставим воздух
                        blocks.put(xyzOffset, Material.AIR);

                    case WATER -> { //запоминаем только воду с уровнем 0

                        blockData = Craft.fromNMS(nmsBlockState);//VM.server().getBlockData(nmsBlockState);//nmsBlockState.createCraftBlockData();//VM.getBlockData(nmsBlockState);//CraftBlockData.fromData(nmsBlockState);
                        lvl = (Levelled) blockData;
                        if (lvl.getLevel() == 0) {
                            blocks.put(xyzOffset, mat);
                        }
                    }

                    default -> {
                        tileEntity = worldServer.getBlockEntity(mutableBlockPosition);//= worldServer.c_(mutableBlockPosition);
                        if (tileEntity != null) {
//Ostrov.log("tileEntity!! "+mat+" "+x+","+y+","+z);
                            //craftBlockState = CraftBlockStates.getBlockState(ws, bps);
                            //craftBlockState.setWorldHandle(ws);
                            blockState = world.getBlockState(xyz.x, xyz.y, xyz.z);
                            bsAsString = getStringFromBlockState(blockState); //BlockState не даёт ASYNC если что!
                            if (!bsAsString.isEmpty()) {
                                blockStates.put(xyzOffset, bsAsString);
                            }
                        }
                        blockData = Craft.fromNMS(nmsBlockState);//VM.server().getBlockData(nmsBlockState);//nmsBlockState.createCraftBlockData();//VM.getBlockData(nmsBlockState);//CraftBlockData.fromData(nmsBlockState);
                        //if (blockData!=null && !getStringFromBlockData(blockData).isEmpty()) {
                        if (blockData != null) {
                            bdAsString = blockData.getAsString(true);
                            if (bdAsString.endsWith("]")) { //пишем только реальную дату!
                                blockDatas.put(xyzOffset, getStringFromBlockData(blockData));
                            }
                        }
                        blocks.put(xyzOffset, mat);
                    }

                }

            }

            count++;
        }

        if (save) {
            Ostrov.async(() -> save(cs, folder, ext), 0);
        }

        ready = true;
        final boolean silent = name.endsWith("_rotate") || name.endsWith("_undo");
        if (!silent && cs != null) {
            cs.sendMessage("§7=====================================================");
            cs.sendMessage("§fБлоки для схематика §b" + name + " §fотсканированы.");
            cs.sendMessage("§7Обработано блоков : §6" + count);
            cs.sendMessage("§7Hе пустых : §6" + blocks.size());
            cs.sendMessage("§7С blockData : §6" + blockDatas.size());
            cs.sendMessage("§7С blockState : §6" + blockStates.size());
            cs.sendMessage("Bремя: §5" + (System.currentTimeMillis() - l) + " мс.");
            cs.sendMessage("§7=====================================================");
        }
    }


    //создание с местности -
    @Deprecated
    private void create(final CommandSender cs, final String name, final String param, final XYZ min, final XYZ max,
        final Location spawn, final boolean save, final String folderPath, final String extension, List<Material> scipOnScan) {
        create(cs, name, param, BVec.of(min.worldName, min.x, min.y, min.z),
            BVec.of(max.worldName, max.x, max.y, max.z), spawn, save, folderPath, extension, scipOnScan);
    }


    private static String getStringFromBlockData(final BlockData bd) {
        //if (bd==null) return "";
        String bds = bd.getAsString(true);
        if (!bds.endsWith("]")) return "";
        //bds = bds.substring(bds.indexOf("[")+1).replaceFirst("]", ""); - не добавлять!!!!
        return bds;
    }

    private static String getStringFromBlockState(final BlockState bs) {
        return switch (bs) {
            case InventoryHolder inventoryHolder -> {
                StringBuilder sb = new StringBuilder("Inventory=");

                final Inventory inv = inventoryHolder.getInventory();
                for (final ItemStack is : inv.getContents()) {
                    sb.append(is == null || is.getType() == Material.AIR ? "null" : ItemUtil.write(is)).append(",");
                }

                if (bs instanceof Nameable nameable) {
                    final Component nm = nameable.customName();
                    if (nm instanceof TextComponent && ((TextComponent) nm).content().equals("RANDOM")) {
                        sb.append("RANDOM");
                    }
                }
                yield sb.toString();
            }
            case CreatureSpawner creatureSpawner ->
                "CreatureSpawner=" + creatureSpawner.getSpawnedType().toString();
            case null, default -> "";
        };
    }


    //загрузка из файла
    public Schematic(final CommandSender cs, final File schemFile, final boolean deleteFile) {
//Ostrov.log("new Schematic deleteFile="+deleteFile);
        //  Ostrov.async( ()-> { асинхронная загрузка - головняк для редактора!
        name = schemFile.getName().substring(0, schemFile.getName().lastIndexOf("."));

        Material mat;
        String blockDataAsString;

        try (final Stream<String> lns = Files.lines(schemFile.toPath())) {
            final List<String> lines = lns.toList();
            int version;
            int x, y, z; //для конверсии sLoc

            int line = 0; //line0
            if (lines.get(line).startsWith("version: ")) {
                version = Integer.parseInt(lines.get(line).replaceFirst("version: ", ""));

                switch (version) {

                    case 4:
                    case 3:
                        line++;
                        spawnAddX = Integer.parseInt(lines.get(line)); //line1
                        line++;
                        spawnAddY = Integer.parseInt(lines.get(line)); //line2
                        line++;
                        spawnAddZ = Integer.parseInt(lines.get(line)); //line3
                        line++;
                        spawnYaw = Integer.parseInt(lines.get(line)); //line4
                        line++;
                        spawnPitch = Integer.parseInt(lines.get(line)); //line5

                    case 2:
                        line++;
                        param = lines.get(line); //line6

                    case 1:
                        line++;
                        dX = Integer.parseInt(lines.get(line)); //line7
                        line++;
                        dY = Integer.parseInt(lines.get(line)); //line8
                        line++;
                        dZ = Integer.parseInt(lines.get(line)); //line9
                        line++;
                        createdEnvironment = Environment.valueOf(lines.get(line)); //line10

                        line++;
                        for (Biome b : Ostrov.registries.BIOMES) {
                            if (b.key().value().equalsIgnoreCase(lines.get(line))) {
                                createdBiome = b; //line11
                            }
                        }

                        line++;

                        //final int fourSize = (lines.size()/4) * 4;
                        for (; line < lines.size(); line += 4) {
                            if (line + 4 > lines.size()) {
                                break;
                            }
                            int xyzOffset = Integer.parseInt(lines.get(line));
                            if (version <= 3) { //переконвертировать координату!!
                                //в версии 3 делалось так: xyz.yaw = x<<19 | y<<11 | z; - было переполнение Y !!!
                                x = (xyzOffset >> 19) & 0x7FF; //xxxxxxxx xxxxx000 00000000 00000000   лимит 2047
                                y = (xyzOffset >> 11) & 0xFF;  //00000000 00000yyy yyyyy000 00000000   лимит 256
                                z = xyzOffset & 0x7FF;       //00000000 00000000 00000zzz zzzzzzzz   лимит 2047
                                xyzOffset = x << 20 | y << 10 | z;  //переконвертировать по новой, как в методе XYZ.offSet()
                            }
                            mat = Material.matchMaterial(lines.get(line + 1));
                            if (mat == null) continue;
                            blocks.put(xyzOffset, mat);
                            if (!lines.get(line + 2).isEmpty()) {
                                blockDataAsString = lines.get(line + 2);
                                if (!blockDataAsString.startsWith("minecraft:")) { //как-то сохранял неправильно, делал фикс
                                    blockDataAsString = "minecraft:" + mat.name().toLowerCase() + "[" + blockDataAsString + "]";
                                }
                                blockDatas.put(xyzOffset, blockDataAsString);
                            }
                            if (!lines.get(line + 3).isEmpty()) {
                                blockStates.put(xyzOffset, lines.get(line + 3));
                            }
                        }
                        break;
                }

            } else { //конверсия старого типа
                dX = Integer.parseInt(lines.get(0).replaceFirst("sizeX:", ""));
                dY = Integer.parseInt(lines.get(1).replaceFirst("sizeY:", ""));
                dZ = Integer.parseInt(lines.get(2).replaceFirst("sizeZ:", ""));
                String[] split;
                String[] split2;
                for (int i = 3; i < lines.size(); i++) {
                    split = lines.get(i).split(":");
                    mat = Material.matchMaterial(split[1]);
                    if (mat != null) {
                        split2 = split[0].split("\\.");
                        int xyz = (Integer.parseInt(split2[0])) << 19 | (Integer.parseInt(split2[1])) << 11 | (Integer.parseInt(split2[2]));
                        blocks.put(xyz, mat);
                    }
                }
            }

            if (spawnAddX < 0 || spawnAddX > dX) spawnAddX = 0;
            if (spawnAddY < 0 || spawnAddY > dY) spawnAddY = 0;
            if (spawnAddZ < 0 || spawnAddZ > dZ) spawnAddZ = 0;

        } catch (IOException ex) {

            if (cs != null) cs.sendMessage("§cОшибка чтения схематика из файла " + name + " : " + ex.getMessage());
            Ostrov.log_err("§cОшибка чтения схематика из файла " + name + " : " + ex.getMessage());

        } finally {
//Ostrov.log("schemFile finally deleteFile="+deleteFile);
            ready = true;
            if (deleteFile) {
                schemFile.delete();
//Ostrov.log("schemFile delete "+schemFile.getName()+":"+schemFile.getAbsolutePath());
            }
        }

    }

    @ThreadSafe
    public void save(final CommandSender cs) {
        save(cs, DEF_PATH, DEF_EXT);
    }

    @ThreadSafe
    public void save(final CommandSender cs, final String folder) {
        save(cs, folder, DEF_EXT);
    }

    @ThreadSafe
    public void save(final CommandSender cs, final String folder, final String ext) {
        final List<String> lines = new ArrayList<>();

        lines.add("version: 4"); //line0
        lines.add(String.valueOf(spawnAddX)); //line1
        lines.add(String.valueOf(spawnAddY));//line2
        lines.add(String.valueOf(spawnAddZ));//line3
        lines.add(String.valueOf(spawnYaw));//line4
        lines.add(String.valueOf(spawnPitch));//line5
        lines.add(param); //line6
        lines.add(String.valueOf(dX)); //line7
        lines.add(String.valueOf(dY));//line8
        lines.add(String.valueOf(dZ));//line9

        lines.add(String.valueOf(createdEnvironment)); //line10
        lines.add(String.valueOf(createdBiome)); //line11

        //for (int xyz:posData.keys()) {
        for (final int xyz : blocks.keySet()) {
            lines.add(String.valueOf(xyz));
            lines.add(String.valueOf(blocks.get(xyz)));
            //lines.add(getStringFromBlockData(blockDatas.get(xyz)));
            lines.add(blockDatas.getOrDefault(xyz, ""));
            lines.add(blockStates.getOrDefault(xyz, ""));
        }

        final String fld = (folder == null || folder.isEmpty()) ? DEF_PATH : folder;
        final File schemFld = new File(fld);
        if (!schemFld.exists() || !schemFld.isDirectory()) {
            schemFld.mkdir();
        }
        final File schemFile = new File(fld, name + ((ext == null || ext.isEmpty()) ? DEF_EXT : ext));

        try {
            if (schemFile.delete()) {
                schemFile.createNewFile();
            }
            Files.write(schemFile.toPath(), lines);
            if (cs != null) cs.sendMessage("§aСхематик " + name + " сохранён.");
        } catch (IOException ex) {
            Ostrov.log_err("Не удалось сохранить схематик " + name + " : " + ex.getMessage());
            if (cs != null) cs.sendMessage("§cНе удалось сохранить схематик " + name + " : " + ex.getMessage());
        }
    }

    //простая прямая вставка
    public Cuboid paste(final CommandSender cs, final BVec spawn, final boolean pasteAir) {
        return paste(cs, spawn, Rotate.r0, pasteAir);
    }

    @Deprecated
    public Cuboid paste(final CommandSender cs, final WXYZ spawn, final boolean pasteAir) {
        return paste(cs, spawn, Rotate.r0, pasteAir);
    }

    public Cuboid paste(final CommandSender cs, final BVec spawn, final Rotate rotate, final boolean pasteAir) {
        return WE.paste(cs, this, spawn, rotate, pasteAir);
    }

    @Deprecated
    public Cuboid paste(final CommandSender cs, final WXYZ spawn, final Rotate rotate, final boolean pasteAir) {
        return WE.paste(cs, this, spawn, rotate, pasteAir);
    }


    public String getName() {
        return name;
    }

    public String getParam() {
        return param;
    }

    public Biome getCreatedBiome() {
        return createdBiome;
    }

    public Environment getCreatedEnvironment() {
        return createdEnvironment;
    }

    public Cuboid getCuboid() { //отдаст дельту между макс и мин (на 1 меньше!)
        return new Cuboid(this);
    }

    public int getSizeX() { //отдаст дельту между макс и мин (на 1 меньше!)
        return dX + 1;
    }

    public int getSizeY() { //отдаст дельту между макс и мин (на 1 меньше!)
        return dY + 1;
    }

    public int getSizeZ() { //отдаст дельту между макс и мин (на 1 меньше!)
        return dZ + 1;
    }

    public int getSpawnAddX() {
        return spawnAddX;
    }

    public int getSpawnAddY() {
        return spawnAddY;
    }

    public int getSpawnAddZ() {
        return spawnAddZ;
    }

    public int getSpawnYaw() {
        return spawnYaw;
    }

    public int getSpawnPitch() {
        return spawnPitch;
    }

    @Deprecated
    public BlockData getBlockData(final XYZ xyzOffset) {
        final String str = blockDatas.get(xyzOffset.offSet());
        return str == null ? null : Bukkit.createBlockData(str);
    }

    public @Nullable BlockData blockData(final BVec offset) {
        final String str = blockDatas.get(offset.offSet());
        return str == null ? null : Bukkit.createBlockData(str);
    }

    public @Nullable BlockData blockData(final BVec offset, final BlockData bd) {
        final String str = blockDatas.get(offset.offSet());
        return str == null ? null : Bukkit.createBlockData(str);
    }

    @Deprecated
    public Material getMaterial(final XYZ shemCoord) {
        final Material mat = blocks.get(shemCoord.offSet());
        return mat == null ? Material.AIR : mat;
    }

    @Deprecated
    public Material setMaterial(final XYZ xyzOffset, final Material mat) {
        final int sLoc = xyzOffset.offSet();
        blockDatas.remove(sLoc);
        blockStates.remove(sLoc);
        return blocks.replace(sLoc, mat);
    }

    public @Nullable BlockType blockType(final BVec offset) {
        final Material mat = blocks.get(offset.offSet());
        return mat == null ? BlockType.AIR : mat.asBlockType();
    }

    public void blockType(final BVec offset, final BlockType bt) {
        final int sLoc = offset.offSet();
        final String bds = blockDatas.remove(sLoc);
        if (bds != null) {
            final BlockData bd = bt.createBlockData();
            Bukkit.createBlockData(bds).copyTo(bd);
            blockDatas.put(sLoc, bd.getAsString());
        }
        blockStates.remove(sLoc);
        @Deprecated
        final Material mat = bt.asMaterial();
        blocks.replace(sLoc, mat);
    }


    //сравнить схематик с местностью, совмещая точку спавна
    @Deprecated
    public CompareResult compare(final WXYZ spawn, final Rotate rotate, final boolean ignoreAir) {

        final long l = System.currentTimeMillis();
        final Cuboid cuboid = new Cuboid(this);
        cuboid.allign(spawn);
        cuboid.rotate(rotate);
        final World world = spawn.w;
        final String worldName = world.getName();

        BlockPos.MutableBlockPos mutableBlockPosition = new BlockPos.MutableBlockPos(0, 0, 0);//MutableBlockPosition mutableBlockPosition = new MutableBlockPosition(0, 0, 0);
        ServerLevel worldServer = Craft.toNMS(world);//WorldServer worldServer = VM.server().toNMS(world);
        net.minecraft.world.level.block.state.BlockState nmsBlockState;//IBlockData iBlockData;
        Material worldMaterial;
        Material schematicMaterial;
        int xyzOffset;
        XYZ xyz;

        final CompareResult cr = new CompareResult(cuboid, blocks.size());

        final Iterator<XYZ> it = cuboid.iteratorXYZ(rotate);
        while (it.hasNext()) {

            xyz = it.next();
            mutableBlockPosition.set(xyz.x, xyz.y, xyz.z);//mutableBlockPosition.d(xyz.x, xyz.y, xyz.z);
            nmsBlockState = worldServer.getBlockState(mutableBlockPosition);//iBlockData = worldServer.a_(mutableBlockPosition);
            worldMaterial = nmsBlockState.getBukkitMaterial();//= iBlockData.getBukkitMaterial();
//Ostrov.log("xyz= "+xyz.x+","+xyz.y+","+xyz.z);

            xyzOffset = xyz.yaw; //НЕ брать offSet(), в Iterator<XYZ> координаты уже другие!!
            schematicMaterial = blocks.get(xyzOffset);

            if (schematicMaterial == null) { //на этом месте долже быть воздух, но AIR не будет в posData, т.е. чекаем на null!
                if (!ignoreAir && worldMaterial != Material.AIR) {
                    final XYZ coord = new XYZ(worldName, xyz.x, xyz.y, xyz.z);
                    cr.mustBe.put(coord, Material.AIR);
                    cr.inWorld.put(coord, worldMaterial);
                }
            } else {
                if (schematicMaterial != worldMaterial) {
                    final XYZ coord = new XYZ(worldName, xyz.x, xyz.y, xyz.z);
                    cr.mustBe.put(coord, schematicMaterial);
                    cr.inWorld.put(coord, worldMaterial);
                }
            }
        }
        cr.ms = (int) (l - System.currentTimeMillis());
        return cr;
    }


    //сколько блоков в схематике с воздухом или без
    public int size(boolean ignoreAir) {
        if (ignoreAir) return blocks.size();
        else return (dX * dY * dZ) - blocks.size();
    }

    @Deprecated
    public static class CompareResult {

        public final HashMap<XYZ, Material> inWorld; //XYZ, что в мире
        public final HashMap<XYZ, Material> mustBe; //XYZ, что должно быть
        public final Cuboid cuboid;
        public final int blocksSize;
        public int ms;

        public CompareResult(final Cuboid cuboid, final int blocksSize) {
            this.cuboid = cuboid;
            this.blocksSize = blocksSize;
            inWorld = new HashMap<>();
            mustBe = new HashMap<>();
        }


        public static void print(final Player p, final CompareResult cr) {
            int limit = 20;
            p.sendMessage("§7координаты, §a[§2Должен быть§a] §7/ §c[§4Обнаружен§c]");
            for (XYZ xyz : cr.inWorld.keySet()) {
                p.sendMessage("§7" + xyz.x + ", " + xyz.y + ", " + xyz.z + "   §2" + cr.mustBe.get(xyz) + " §7/ §4" + cr.inWorld.get(xyz));
                limit--;
                if (limit == 0) {
                    if (cr.mustBe.size() > 20) {
                        p.sendMessage("§7... и еще " + (cr.mustBe.size() - 20));
                    }
                    break;
                }
            }
        }


    }


    public enum Rotate {
        r0(0), r90(90), r180(180), r270(270);

        public final int degree;

        Rotate(final int degree) {
            this.degree = degree;
        }
    }


    public Schematic copy() {
        final Schematic s = new Schematic(this.name);
        s.param = this.param;
        s.blocks.putAll(this.blocks);
        s.blockDatas.putAll(this.blockDatas);
        s.blockStates.putAll(this.blockStates);
        s.createdEnvironment = this.createdEnvironment;
        s.createdBiome = this.createdBiome;
        s.dX = this.dX;
        s.dY = this.dY;
        s.dZ = this.dZ;
        s.spawnAddX = this.spawnAddX;
        s.spawnAddY = this.spawnAddY;
        s.spawnAddZ = this.spawnAddZ;
        s.spawnYaw = this.spawnYaw;
        s.spawnPitch = this.spawnPitch;
        s.ready = true;
        return s;
    }

    private Schematic(final String name) {
        this.name = name;
    }

}