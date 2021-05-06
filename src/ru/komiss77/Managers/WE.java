package ru.komiss77.Managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;









public class WE implements Listener {
    
    private static ConcurrentHashMap<String,BukkitTask> jobs;
    private static Ostrov plugin;
    public static List<Material> scipOnPasteDefault = Arrays.asList(
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

    
    public WE(final Ostrov plugin) {
        //jobs = new ConcurrentHashMap();
        WE.plugin = plugin;
        jobs = new ConcurrentHashMap();
    }

    
    
    
   
    
    /*
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTest(PlayerInteractEvent e) {
        if (  e.getItem()!=null && e.getAction()==Action.RIGHT_CLICK_BLOCK) {
                //e.setCancelled(true);
                final Player p = e.getPlayer();
                
                if (e.getItem().getType() == Material.NETHERITE_INGOT) {
                    e.setCancelled(true);
                    if (p.isSneaking()) {
                        paste(p, e.getClickedBlock().getLocation(), "test1x1", true, true); 
                    } else {
                        save(p, e.getClickedBlock().getLocation(), e.getClickedBlock().getLocation(), "test1x1", true);
                    }
                } else if (e.getItem().getType() == Material.IRON_INGOT) {
                    e.setCancelled(true);
                    if (p.isSneaking()) {
                        paste(p, e.getClickedBlock().getLocation(), "test2x2", true, true); 
                    } else {
                        save(p, e.getClickedBlock().getLocation(), e.getClickedBlock().getLocation().clone().add(1, 1, 1), "test2x2", true);
                    }
                    //paste(p, p.getLocation().clone().subtract(5, 5, 5), "test"); 
                    //p.sendMessage("§eПересчёт уровня острова, результат: "+is.stats.blockPrice);
                } else if (e.getItem().getType() == Material.GOLD_INGOT) {
                    e.setCancelled(true);
                    if (p.isSneaking()) {
                        paste(p, e.getClickedBlock().getLocation(), "test10x10", true, true); 
                    } else {
                        save(p, e.getClickedBlock().getLocation(), e.getClickedBlock().getLocation().clone().add(9, 9, 9), "test10x10", true);
                    }
                    //paste(p, p.getLocation().clone().subtract(5, 5, 5), "test"); 
                    //p.sendMessage("§eПересчёт уровня острова, результат: "+is.stats.blockPrice);
                }
        }
    }    
   */ 
    
    
    
    
    
    
    
    
    

    public static boolean hasJob(final CommandSender cs) {
        return jobs.containsKey(cs.getName());
    }



    public static void save (final CommandSender cs, final Location loc1,  final Location loc2, final String folderPath, final String fileName, final boolean notify) {
        if ( loc1==null || loc2==null || fileName==null || fileName.isEmpty() ) return;
        //if (cs==null) cs = Bukkit.getConsoleSender();
        //if (folderPath==null || folderPath.isEmpty()) folderPath = Ostrov.instance.getDataFolder() + "/schematics";
        
        final Set<ChunkSnapshot> csList = getChunksBetween(loc1, loc2);
        
        final BukkitTask task = (new BukkitRunnable() {
            @Override
            public void run() {
                
                final Cuboid cuboid = new Cuboid(loc1, loc2);
                Material mat;
                //String raw = "";
                final List<String> raw = new ArrayList();
                raw.add("sizeX:"+cuboid.getSizeX());
                raw.add("sizeY:"+cuboid.getSizeY());
                raw.add("sizeZ:"+cuboid.getSizeZ());
                
                int x,z,count=0,countAll=0;

                for (final ChunkSnapshot c : csList) {
//System.out.println(" --- обработка чанка "+c.getX()+"*"+c.getZ()+" (всего:"+csList.size()+")");                    
                    for (int chunk_x = 0; chunk_x<=15; chunk_x++) {
                        for (int chunk_z = 0; chunk_z<=15; chunk_z++) {
                            for (int y = 0; y<=255; y++) {

                                x = c.getX()*16+chunk_x; //получаем X для локации
                                z = c.getZ()*16+chunk_z;//получаем Z для локации. Y в кубоиде такой же как в чанке.
//System.out.println(" --- chunk_x="+chunk_x+" chunk_z="+chunk_z+"   X="+x+" y="+y+" Z="+z+" contains?"+cuboid.contains(x, y, z)+" mat="+c.getBlockType(chunk_x, y, chunk_z));                    

                                if (cuboid.contains(x, y, z)) { //если локация в кубоиде
//System.out.println(" --- chunk_x="+chunk_x+" chunk_z="+chunk_z+"   X="+x+" y="+y+" Z="+z+" contains?"+cuboid.contains(x, y, z)+" mat="+c.getBlockType(chunk_x, y, chunk_z));                    
                                    mat = c.getBlockType(chunk_x, y, chunk_z); //материал берём по x,y,z в ЧАНКЕ !!!
                                    countAll++;
                                    if (mat!=Material.AIR) {                  //а запоминаем относително x1,y1,z1 в кубоиде!
                                        raw.add((x-cuboid.getLowerX())+"."+(y-cuboid.getLowerY())+"."+(z-cuboid.getLowerZ())+":"+mat.toString());
                                        //raw = raw + ", "+(x-cuboid.getLowerX())+"."+(y-cuboid.getLowerY())+"."+(z-cuboid.getLowerZ())+":"+mat.toString(); //отнимаем x1,z1 из кубоида, чтобы начиналось с 0,0,0
                                        count++;
                                    }
                                }

                            }
                        }
                    }
                }

                try {  //Ostrov.instance.getDataFolder() + "/schematics"
                    final File regionDataFile = new File( (folderPath==null || folderPath.isEmpty()) ? Ostrov.instance.getDataFolder() + "/schematics" : folderPath, fileName+".region");
                    if (regionDataFile.delete()){
                        regionDataFile.createNewFile();
                    }
                    Files.write(regionDataFile.toPath(), raw);

                    if (notify) cs.sendMessage("§aРегион "+fileName+" сохранён."+" Обработано блоков:"+countAll+", сохранено:"+count);
                   
                } catch (IOException ex) {
                    
                   Ostrov.log_err("Не удалось сохранить регион "+fileName+" : "+ex.getMessage());
                   if (notify) cs.sendMessage("§cНе удалось сохранить регион "+fileName+" : "+ex.getMessage());
                   
                } finally {
                    raw.clear();
                }

                if (jobs.containsKey(cs.getName())) jobs.remove(cs.getName());
            
            }
        }).runTaskLaterAsynchronously(plugin, 1);

        jobs.put(cs.getName(), task);                        
        
    }   



    @Deprecated
    public static void paste (final CommandSender cs, final Location loc, final String folderPath, final String fileName, final boolean notify, final boolean deleteFile, final Runnable onDone) {
        paste(cs, loc, folderPath, fileName, notify, deleteFile, onDone, new ArrayList<>());
    }    
    
    
    
    
    
    
    public static void paste (final CommandSender cs, final Location loc, final String folderPath, final String fileName, final boolean notify, final boolean deleteFile, final Runnable onDone, final List<Material>scipOnPaste) {
        if (cs==null || loc==null || fileName==null || fileName.isEmpty()) return;
        
                
        final File regionDataFile = new File( (folderPath==null || folderPath.isEmpty()) ? Ostrov.instance.getDataFolder() + "/schematics" : folderPath , fileName+".region");
        if (!regionDataFile.exists()) {
            if (notify) cs.sendMessage("§cНет сохранения для региона "+fileName);
            return;
        }
        

        final HashMap <String, Material> blockMap = new HashMap<>();
        String[] split;
        Material mat;
        int sizeX=0,sizeY=0,sizeZ=0;
//System.out.println("scipOnPaste="+scipOnPaste);                        
        try {
            final Stream <String> lineStream = Files.lines(regionDataFile.toPath());
            //Scanner scanner = new Scanner(regionDataFile);
            for (final String line : lineStream.collect(Collectors.toList()) ) {
                
                split = line.split(":");
                mat = Material.matchMaterial(split[1]);
                
                if (mat==null) {
                    
                    switch (split[0]) {
                        case "sizeX":
                            sizeX = Integer.parseInt(split[1]);
                            break;
                        case "sizeY":
                            sizeY = Integer.parseInt(split[1]);
                            break;
                        case "sizeZ":
                            sizeZ = Integer.parseInt(split[1]);
                            break;
                        default:
                            break;
                    }
                    
                } else {
                    
                    if ( !scipOnPaste.contains(mat) ) {
                        blockMap.put(split[0], mat);
                    } else {
                        
                    }
                    
                }
                
            }

            lineStream.close();

        } catch (IOException ex) {
            if (notify) cs.sendMessage("§cОшибка чтения файла "+fileName+" : "+ex.getMessage());
        }
                
        final Cuboid cuboid = new Cuboid ( loc, sizeX, sizeY, sizeZ );
        
        final Iterator <Block> cuboidIterator = cuboid.iterator(loc.getWorld()); //в кубоиде отсчёт по местности
        final double cuboidSize = (double)cuboid.getSize();
        final int block_per_tick = 10000;
      
        
        final BukkitTask task = (new BukkitRunnable() {
            
            int checked = 0;
            int current = 0;
            int ticks = 0;

            @Override
            public void run() {

                Block block;
                String xyz;

                for( ; cuboidIterator.hasNext() && current < block_per_tick; ++current) {
                    block = cuboidIterator.next();
                    
                    //создаём относительную строку
                    xyz = (block.getLocation().getBlockX()-loc.getBlockX())+"."+(block.getLocation().getBlockY()-loc.getBlockY())+"."+(block.getLocation().getBlockZ()-loc.getBlockZ());
//System.out.println("xyz="+xyz);        
                            
                    if (blockMap.containsKey(xyz)) {
                        if (blockMap.get(xyz) != block.getType()) {
                            block.setType(blockMap.get(xyz), false);
                        }
                        blockMap.remove(xyz); //облегчаем массив
                    } else if (block.getType() != Material.AIR) {
                        block.setType(Material.AIR);
                    }
                }

                checked += current;
                current = 0;
                ++ticks;
                if (ticks%5 == 0) {
                    final int regenPercent = (int)((double)checked / cuboidSize * 100.0D);
                    if (notify) {
                        if ( Bukkit.getPlayer(cs.getName())!=null ) {
                            ApiOstrov.sendActionBarDirect(Bukkit.getPlayer(cs.getName()), "§eВыполнено: §f"+regenPercent+"%");
                        } else {
                            if (ticks%20 == 0) cs.sendMessage("§eВыполнено: "+regenPercent+"%");
                        }
                    }
                    //ticks = 0;
                }

                if (!cuboidIterator.hasNext()) {

                    this.cancel();

                    for (Entity e : loc.getWorld().getEntities()) {
                      //if ( cuboid.contains(e.getLocation()) && (e instanceof LivingEntity || e.getType()==EntityType.DROPPED_ITEM) && e.getType()!=EntityType.PLAYER) e.remove();
                        if ( cuboid.contains(e.getLocation()) && e.getType()!=EntityType.PLAYER) e.remove();
                    }
 
                    if (notify) if ( Bukkit.getPlayer(cs.getName())!=null ) {
                            ApiOstrov.sendActionBarDirect(Bukkit.getPlayer(cs.getName()), "§eВыполнено: "+100+"%");
                        } else {
                            cs.sendMessage("§aВставка закончена, сущности очищены!");
                        }
                    if (jobs.containsKey(cs.getName())) jobs.remove(cs.getName());
                    if (deleteFile) regionDataFile.delete();
                    
                    onDone.run();
                }

            }
            
        }).runTaskTimer(plugin, 1, 1);
        
        jobs.put(cs.getName(), task);

      
    }
            
    
    
    
    
    
    
    
    
    
    
    
    
    


    public static Set<ChunkSnapshot> getChunksBetween(final Location loc1,  final Location loc2) {
        Set<ChunkSnapshot> chunks = new HashSet<>();
        final int minX = Math.min(loc1.getChunk().getX(), loc2.getChunk().getX());
        final int maxX = Math.max(loc1.getChunk().getX(), loc2.getChunk().getX());
        final int minZ = Math.min(loc1.getChunk().getZ(), loc2.getChunk().getZ());
        final int maxZ = Math.max(loc1.getChunk().getZ(), loc2.getChunk().getZ());
//.out.println("getChunksBetween loc="+loc1+" loc2="+loc2);        
//System.out.println("getChunksBetween minX="+minX+" maxX="+maxX+" minZ="+minZ+" maxZ="+maxZ);        
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if ( !loc1.getWorld().getChunkAt(x, z).isLoaded() ) loc1.getWorld().getChunkAt(x, z).load();
                chunks.add( loc1.getWorld().getChunkAt(x, z).getChunkSnapshot() );
            }
        }
        return chunks;
    }


    
    
    
    
    
    
  
 


}

