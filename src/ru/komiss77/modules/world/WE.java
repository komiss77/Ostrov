package ru.komiss77.modules.world;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import ru.komiss77.Initiable;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.Ostrov;
import ru.komiss77.builder.SetupMode;
import ru.komiss77.modules.world.Schematic.Rotate;



//не перемещать!! использует регионГуи

public class WE implements Initiable {
    
    
    protected static final Map<Integer,PasteJob> JOBS;
    private static final CaseInsensitiveMap <Schematic> schematics;
    public static final List<Material> scipOnPasteDefault;
    public static int currentTask = -1;
    
    static {
         JOBS = new HashMap<Integer, PasteJob>();
         schematics = new CaseInsensitiveMap<Schematic>();
         scipOnPasteDefault = Arrays.asList(
                Material.DIAMOND_BLOCK, 
                Material.EMERALD_BLOCK,
                Material.IRON_BLOCK,
                Material.DIAMOND_ORE, 
                Material.IRON_ORE, 
                Material.EMERALD_ORE,
                Material.GOLD_ORE,
                Material.OBSIDIAN, 
                Material.ENCHANTING_TABLE,
                Material.BEACON,
                Material.SEA_LANTERN, 
                Material.ANCIENT_DEBRIS,
                Material.NETHERITE_BLOCK,
                Material.CRYING_OBSIDIAN, 
                Material.ENDER_CHEST, 
                Material.RESPAWN_ANCHOR,
                Material.DRAGON_HEAD,
                Material.WITHER_SKELETON_SKULL,
                Material.GOLD_BLOCK
            );
    }
    
    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
    }
    
    @Override
    public void reload() {
    }

    @Override
    public void onDisable() {
    }    

    
    
    
    public static int getBlockPerTick() {
        return 10000;
    }

    public static void endPaste(final int taskId) {
        JOBS.remove(taskId);
        currentTask = -1;
    }
    
    public static boolean wait (final int taskId) {
        if (currentTask>0 && currentTask!=taskId) { //что-то запущено
            if (JOBS.containsKey(currentTask)) { //запущен другой процес - проверить его на жизнь и паузу
                if (JOBS.get(currentTask)==null || JOBS.get(currentTask).isCanceled()) { //ключ есть, но процесс дохлый - очистить
                    JOBS.remove(currentTask);
                } else {
                    return !JOBS.get(currentTask).pause; //процесс на паузе - освободить очередь
                }
            }
        }
        return false; //работать. сразу подставится currentTask из следующего PasteJob
    }

 
    
    public static Schematic getSchematic(final CommandSender cs, final String schemName, final boolean deleteFile) {
        if (schematics.containsKey(schemName)) {
            if (deleteFile) { //не удаляет файл если есть в буфере!!
                final File file = new File(Ostrov.instance.getDataFolder() + "/schematics" , schemName+".schem");
                if (file.exists()) file.delete();
            }
            return schematics.get(schemName);
        }
        final File file = new File(Ostrov.instance.getDataFolder() + "/schematics" , schemName+".schem");
        if (!file.exists()) {
            if (cs!=null) cs.sendMessage("§cНет файла схематика "+schemName);
            return null;
        }        
        Schematic sh = new Schematic(cs, file, deleteFile);
        schematics.put(sh.getName(), sh);
        return sh;
    }    
    
    public static Schematic getSchematic(final CommandSender cs, final String schemName) {
        return getSchematic(cs, schemName, false);
    }
    
    
    
    
    //не убирать!! использует регионГуи
    public static boolean hasJob(final CommandSender cs) {
        for (PasteJob pj : JOBS.values()) {
            if (pj.pause || pj.isCanceled()) continue;
            if (pj.cs==cs) return true;
        }
        return false;
    }


    
    

    //простое сохранение местности. не менять, используют плагины!
    public static void save (final CommandSender cs, final Location loc1,  final Location loc2, final String schemName, final String param) {
        Schematic sh = new Schematic(cs, schemName, param, loc1, loc2, true);
        schematics.put(sh.getName(), sh);
    }
    
    //сохранение из редактора
    public static void save (final CommandSender cs, final SetupMode sm) {
        Schematic sh = new Schematic(cs, sm.schemName, sm.param, sm.cuboid, sm.min.getWorld(), true);//(sm);
        schematics.put(sh.getName(), sh);
    }    


    public static Cuboid paste (final CommandSender cs, final Schematic schem, final XYZ spawn, final Rotate rotate, final boolean pasteAir) {
        if (schem==null) {
            cs.sendMessage("Schematic==null!!");
            return null;
        }
        final Cuboid cuboid = new Cuboid(schem);
        cuboid.allign(spawn);
        cuboid.rotate(rotate);
        World world = null;
        if (spawn instanceof WXYZ wxyz) {
            world = wxyz.w;
        } else {
            if (spawn.worldName != null && !spawn.worldName.isEmpty()) world = Bukkit.getWorld(spawn.worldName);
        }
        if (world==null) {
            Ostrov.log_err("Cuboid paste - world==null : "+schem);
            return cuboid;
        }
        final PasteJob job = new PasteJob(cs, world, cuboid, schem, rotate, pasteAir); //вставка стартует с задержкой 1 тик
        JOBS.put(job.getId(), job);
        return cuboid;
    } 
    


    
    
    
    
    
    
    
    
    
    
    


    public static Set<ChunkSnapshot> getChunksBetween(final Location loc1,  final Location loc2) {
        Set<ChunkSnapshot> chunks = new HashSet<>();
        final int minX = Math.min(loc1.getChunk().getX(), loc2.getChunk().getX());
        final int maxX = Math.max(loc1.getChunk().getX(), loc2.getChunk().getX());
        final int minZ = Math.min(loc1.getChunk().getZ(), loc2.getChunk().getZ());
        final int maxZ = Math.max(loc1.getChunk().getZ(), loc2.getChunk().getZ());
//Ostrov.log("getChunksBetween minX="+minX+" maxX="+maxX+" minZ="+minZ+" maxZ="+maxZ);        
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if ( !loc1.getWorld().getChunkAt(x, z).isLoaded() ) loc1.getWorld().getChunkAt(x, z).load();
                chunks.add( loc1.getWorld().getChunkAt(x, z).getChunkSnapshot() );
            }
        }
        return chunks;
    }


    public static Set<Chunk> getChunks(final Location loc1,  final Location loc2) {
        Set<Chunk> chunks = new HashSet<>();
        final int minX = Math.min(loc1.getChunk().getX(), loc2.getChunk().getX());
        final int maxX = Math.max(loc1.getChunk().getX(), loc2.getChunk().getX());
        final int minZ = Math.min(loc1.getChunk().getZ(), loc2.getChunk().getZ());
        final int maxZ = Math.max(loc1.getChunk().getZ(), loc2.getChunk().getZ());
//Ostrov.log("getChunksBetween minX="+minX+" maxX="+maxX+" minZ="+minZ+" maxZ="+maxZ);        
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if ( !loc1.getWorld().getChunkAt(x, z).isLoaded() ) loc1.getWorld().getChunkAt(x, z).load();
                chunks.add( loc1.getWorld().getChunkAt(x, z) );
            }
        }
        return chunks;
    }    
    
    
    
    
  
 


}

