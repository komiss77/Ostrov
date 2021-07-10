package ru.komiss77.modules.world;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.modules.world.Cuboid;
import ru.komiss77.modules.world.WE;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemUtils;



public class Schematic {
    
    private final String name;
    public int sizeX;
    public int sizeY;
    public int sizeZ;
    public HashMap<Integer, Material> blocks = new HashMap<>();
    public HashMap<Integer, BlockData> blockDatas = new HashMap<>();
    public HashMap<Integer, String> blockStates = new HashMap<>();
    //private List<String> raw;
    public boolean ready; //флаг для вставки - пока false вставлять не начнёт
    private Environment createdEnvironment = Environment.NORMAL;
    private Biome createdBiome = Biome.DEEP_OCEAN;
    //private boolean s;
    //private File regionDataFile;
    //private CommandSender cs;
 /*   public Schematic(int sizeX, int sizeY, int sizeZ, HashMap<String, Material> blockMap) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.blockMap = blockMap;
    }*/
    public Schematic(final CommandSender cs, final String name, final Location loc1, final Location loc2, final boolean save ) {
        this.name = name;
        createdEnvironment = loc1.getWorld().getEnvironment();
        createdBiome = loc1.getBlock().getBiome();
        create (cs, name, loc1, loc2, save, Ostrov.instance.getDataFolder() + "/schematics", ".schem", null);
    }
    public Schematic(final CommandSender cs, final String name, final Location loc1, final Location loc2, final boolean save, final String folderPath, final String extension, final List<Material> scipOnScan) {
        this.name = name;
        createdEnvironment = loc1.getWorld().getEnvironment();
        createdBiome = loc1.getBlock().getBiome();
        create (cs, name, loc1, loc2, save, folderPath, extension, scipOnScan);
    }
    
    
    //создание с местности - 
    private void create(final CommandSender cs, final String name, final Location loc1, final Location loc2, final boolean save, final String folderPath, final String extension, final List<Material> scipOnScan) {
        //this.name = name;
        
            
        final Cuboid cuboid = new Cuboid(loc1, loc2);

        sizeX = cuboid.getSizeX();
        sizeY = cuboid.getSizeY();
        sizeZ = cuboid.getSizeZ();

        
        //сначала получаем BlockState, т.к. не даёт ASYNC
        Block b;
        String bsAsString;
        final Iterator <Block> cuboidIterator = cuboid.iterator(loc1.getWorld());
        while (cuboidIterator.hasNext()) {
            b = cuboidIterator.next();
            if (b.getType()!=Material.AIR) {
                bsAsString = getStringFromBlockState(b.getState());
                if (!bsAsString.isEmpty()) {
                    int xyz = (b.getX()-cuboid.getLowerX())<<19 | (b.getY()-cuboid.getLowerY())<<11 | (b.getZ()-cuboid.getLowerZ());
                    blockStates.put(xyz, bsAsString);
                }
            }
            
        }
            
        final Set<ChunkSnapshot> csList = WE.getChunksBetween(loc1, loc2);
        Ostrov.async( ()-> {
            Material mat;
            BlockData blockData;
            //BlockState blockState;
            int x,z,count=0,countAll=0;
            
            final Set<Location> toReadBs = new HashSet<>();
            
            for (final ChunkSnapshot chunkSnapShot : csList) {
//System.out.println(" --- обработка чанка "+c.getX()+"*"+c.getZ()+" (всего:"+csList.size()+")");                    
                for (int chunk_x = 0; chunk_x<=15; chunk_x++) {
                    for (int chunk_z = 0; chunk_z<=15; chunk_z++) {
                        for (int y = 0; y<=255; y++) {

                            x = chunkSnapShot.getX()*16+chunk_x; //получаем X для локации
                            z = chunkSnapShot.getZ()*16+chunk_z;//получаем Z для локации. Y в кубоиде такой же как в чанке.
//System.out.println(" --- chunk_x="+chunk_x+" chunk_z="+chunk_z+"   X="+x+" y="+y+" Z="+z+" contains?"+cuboid.contains(x, y, z)+" mat="+c.getBlockType(chunk_x, y, chunk_z));                    

                            if (cuboid.contains(x, y, z)) { //если локация в кубоиде
//System.out.println(" --- chunk_x="+chunk_x+" chunk_z="+chunk_z+"   X="+x+" y="+y+" Z="+z+" contains?"+cuboid.contains(x, y, z)+" mat="+c.getBlockType(chunk_x, y, chunk_z));                    
                                mat = chunkSnapShot.getBlockType(chunk_x, y, chunk_z); //материал берём по x,y,z в ЧАНКЕ !!!
                                countAll++;
                                if (mat!=Material.AIR && (scipOnScan==null || !scipOnScan.contains(mat))) {                  //а запоминаем относително x1,y1,z1 в кубоиде!
                                    //String xyz = (x-cuboid.getLowerX())+"."+(y-cuboid.getLowerY())+"."+(z-cuboid.getLowerZ());
                                    int xyz = (x-cuboid.getLowerX())<<19 | (y-cuboid.getLowerY())<<11 | (z-cuboid.getLowerZ());
                                    
                                    blocks.put(xyz, mat);
                                    
                                    blockData = chunkSnapShot.getBlockData(chunk_x, y, chunk_z);
                                    if (!getStringFromBlockData(blockData).isEmpty()) {
                                        blockDatas.put(xyz, blockData);
                                    }
                                    //if (mat.) {
//System.out.println("blockData instanceof BlockState !!");
                                    //}
                                    //blockState = loc1.getWorld().getBlockAt(x, y, z).getState();
                                    //if (!getStringFromBlockState(blockState).isEmpty()) {
                                    //    blockStates.put(xyz, blockState);
                                    //}

                                    count++;
                                }
                            }

                        }
                    }
                }
            }            
            
            
            
            
            
            if (save) {
                save(cs, folderPath, extension);
            }
            
            ready = true;
            if (cs!=null) cs.sendMessage("§aБлоки для схематика "+name+" отсканированы."+" Обработано блоков:"+countAll+", не пустых:"+count);
            

        }, 0);
    }





    
    
    //загрузка из файла
    public Schematic(final CommandSender cs, final File schemFile, final boolean deleteFile) {
        
//System.out.println("new Schematic deleteFile="+deleteFile);
        //ready = false;
      //  Ostrov.async( ()-> { асинхронная загрузка - головняк для редактора!
            
//System.out.println("<init> "+schemFile.getName());
            name = schemFile.getName().substring(0, schemFile.getName().lastIndexOf("."));

            
            Material mat;
    //System.out.println("scipOnPaste="+scipOnPaste);                        
            try {
                final Stream <String> lineStream = Files.lines(schemFile.toPath());
                //Scanner scanner = new Scanner(regionDataFile);
                final List<String> lines = lineStream.collect(Collectors.toList());
                int version = 0;
                
                if (lines.get(0).startsWith("version: ")) {
                    version = Integer.parseInt(lines.get(0).replaceFirst("version: ",""));
                    
                    if (version==1) {
                        sizeX = Integer.parseInt(lines.get(1));
                        sizeY = Integer.parseInt(lines.get(2));
                        sizeZ = Integer.parseInt(lines.get(3));

                        createdEnvironment = Environment.valueOf(lines.get(4));
                        
                        for (Biome b : Biome.values()) {
                            if (String.valueOf(b).equalsIgnoreCase(lines.get(5))) {
                                createdBiome = b;
                            }
                        }
                        
                        //String[] split;
                        int xyz;
                        for (int i = 6; i<lines.size(); i+=4) {
                            mat = Material.matchMaterial(lines.get(i+1));
                            if (mat!=null) {
                                xyz = Integer.parseInt(lines.get(i));
                                blocks.put(xyz, mat);
                                if (!lines.get(i+2).isEmpty()) {
                                    blockDatas.put(xyz, Bukkit.createBlockData(lines.get(i+2)));
                                    //blockDatas.put(xyz, getBlockDataFromString(mat, lines.get(i+2)));
                                }
                                if (!lines.get(i+3).isEmpty()) {
                                    blockStates.put(xyz, lines.get(i+3));
                                    //blockDatas.put(xyz, getBlockDataFromString(mat, lines.get(i+2)));
                                }
                            }
                        }
                    }
                    
                    
                } else { //конверсия старого типа
                    
                    sizeX = Integer.parseInt(lines.get(0).replaceFirst("sizeX:", ""));
                    sizeY = Integer.parseInt(lines.get(1).replaceFirst("sizeY:", ""));
                    sizeZ = Integer.parseInt(lines.get(2).replaceFirst("sizeZ:", ""));
                    
                    String[] split;
                    String[] split2;
                    for (int i = 3; i<lines.size(); i++) {
                        split = lines.get(i).split(":");
                        mat = Material.matchMaterial(split[1]);
                        if (mat!=null) {
//System.out.println("mat="+mat+" sp="+split[0]);
                            split2 = split[0].split("\\.");
//System.out.println(" sp2="+Arrays.toString(split2));
                            int xyz = (Integer.parseInt(split2[0]))<<19 | (Integer.parseInt(split2[1]))<<11 | (Integer.parseInt(split2[2]));
                            blocks.put(xyz, mat);
                        }
                    }
                }
                
                lineStream.close();

            } catch (IOException ex) {
                
                if (cs!=null) cs.sendMessage("§cОшибка чтения схематика из файла "+name+" : "+ex.getMessage());
                Ostrov.log_err("§cОшибка чтения схематика из файла "+name+" : "+ex.getMessage());
                
            } finally {
                
//System.out.println("schemFile finally deleteFile="+deleteFile);
                ready = true;
                
                if (deleteFile) {
                    schemFile.delete();
//System.out.println("schemFile delete "+schemFile.getName()+":"+schemFile.getAbsolutePath());
                }
                //if (consumer!=null) {
                //    Ostrov.sync(()->consumer.accept(cs),0);
                //}
            }
        
       // }, 0);
        
    }


   // public void save(final CommandSender cs) {
   //     save(cs, Ostrov.instance.getDataFolder() + "/schematics", "schem");
   // }

    private void save(final CommandSender cs, final String folderPath, final String extension) {
        
        //ASYNC!!
        
        final List<String> lines = new ArrayList<>();
        
        lines.add("version: 1");
        lines.add(String.valueOf(sizeX));
        lines.add(String.valueOf(sizeY));
        lines.add(String.valueOf(sizeZ));
        
        lines.add (String.valueOf(createdEnvironment));
        lines.add (String.valueOf(createdBiome));
        
        for (int xyz:blocks.keySet()) {
            lines.add(String.valueOf(xyz));
            lines.add(String.valueOf(blocks.get(xyz)));
            lines.add(getStringFromBlockData(blockDatas.get(xyz)));
            lines.add(blockStates.containsKey(xyz) ? blockStates.get(xyz) : "");
        }
        
        final File schemFolder = new File( (folderPath==null || folderPath.isEmpty()) ? Ostrov.instance.getDataFolder() + "/schematics" : folderPath);
        if (!schemFolder.exists() || !schemFolder.isDirectory()) {
            schemFolder.mkdir();
        }
        final File schemFile = new File( (folderPath==null || folderPath.isEmpty()) ? Ostrov.instance.getDataFolder() + "/schematics" : folderPath, name + ((extension==null || extension.isEmpty())? ".schem" : extension));
        
        try {
            if (schemFile.delete()){
                schemFile.createNewFile();
            }
            Files.write(schemFile.toPath(), lines);

            if (cs!=null) cs.sendMessage("§aСхематик "+name+" сохранён.");

        } catch (IOException ex) {

           Ostrov.log_err("Не удалось сохранить схематик "+name+" : "+ex.getMessage());
           if (cs!=null) cs.sendMessage("§cНе удалось сохранить схематик "+name+" : "+ex.getMessage());

        } finally {
            //raw.clear();
            //lines = null;
           // regionDataFile = null;
           // cs = null;
        }
    }

    public void paste (final CommandSender cs, final Location loc, final boolean pasteAir) {
        WE.paste(cs, loc, this, pasteAir);
    }     
    
    
    /*
    private static BlockData getBlockDataFromString (final Material mat, final String bdAsString) {
         BlockData bd = Bukkit.createBlockData(bdAsString);
System.out.println("bd = "+bd);            
        return bd;
    }
*/
    private static String getStringFromBlockData (final BlockData bd) {
        if (bd==null) return "";
        String bds = bd.getAsString(true);
        if (!bds.endsWith("]")) return "";
        //bds = bds.substring(bds.indexOf("[")+1).replaceFirst("]", "");
        return bds;
    }

    private static String getStringFromBlockState (final BlockState bs) {
//System.out.println("bs="+bs);
        //String bsString = "";
        if (bs==null) return "";
        
        if (bs instanceof InventoryHolder ) {
            StringBuilder sb = new StringBuilder("Inventory=");
            final Inventory inv = ((InventoryHolder)bs).getInventory();
            for (final ItemStack is : inv.getContents()) {
                sb.append ( is==null || is.getType()==Material.AIR ? "null" : ItemUtils.itemStackToString(is, ";") ) .append(",");
            }
            
//System.out.println("bs=InventoryHolder");
            return sb.toString();
            
        } else if (bs instanceof CreatureSpawner) {
            
            return "CreatureSpawner="+((CreatureSpawner) bs).getSpawnedType().toString();
//System.out.println("bs=CreatureSpawner");
            
            
        }

        //String bds = bs.toString();
        //bds = bds.substring(bds.indexOf("[")+1).replaceFirst("]", "");
        return "";
    }

    public String getName() {
        return name;
    }

    public Biome getCreatedBiome() {
        return createdBiome;
    }

    public Environment getCreatedEnvironment() {
        return createdEnvironment;
    }


    
}
