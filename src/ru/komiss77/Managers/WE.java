package ru.komiss77.Managers;

import builder.PasteJob;
import builder.Schematic;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import ru.komiss77.Objects.CaseInsensitiveMap;
import ru.komiss77.Ostrov;







//не перемещать!! использует регионГуи

public class WE implements Listener {
    
    public static Map<Integer,PasteJob> jobs;
    public static int currentTask = -1;
    
    private static final CaseInsensitiveMap <Schematic> schematics = new CaseInsensitiveMap();
    
    //private static Ostrov plugin;
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
        //WE.plugin = plugin;
        jobs = new HashMap();
    }

    public static int getBlockPerTick() {
        return 10000;
    }

    public static void endPaste(final int taskId) {
        jobs.remove(taskId);
        currentTask = -1;
    }
    
    public static boolean wait (final int taskId) {
        if (currentTask>0 && currentTask!=taskId) { //что-то запущено
            if (jobs.containsKey(currentTask)) { //запущен другой процес - проверить его на жизнь и паузу
                if (jobs.get(currentTask)==null || jobs.get(currentTask).isCanceled()) { //ключ есть, но процесс дохлый - очистить
                    jobs.remove(currentTask);
                } else {
                    return !jobs.get(currentTask).pause; //процесс на паузе - освободить очередь
                }
            }
        }
        return false; //работать. сразу подставится currentTask из следующего PasteJob
    }

 
    
    /*
    private static Schematic sh100x100;
    
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTest(PlayerInteractEvent e) {
        if (  e.getItem()!=null && e.getAction()==Action.RIGHT_CLICK_BLOCK) {
                //e.setCancelled(true);
                final Player p = e.getPlayer();
                
                if (e.getItem().getType() == Material.NETHERITE_INGOT) {
                    e.setCancelled(true);
                    if (p.isSneaking()) {
                        WE.paste(
                            p,
                            e.getClickedBlock().getLocation(),
                            Ostrov.instance.getDataFolder() + "/schematics",
                            "test1x1",
                            true,
                            false,
                            () -> {
                                Bukkit.broadcastMessage("готово");
                            },
                            WE.scipOnPasteDefault
                        );
                    } else {
                        save(
                            p, 
                            e.getClickedBlock().getLocation(), 
                            e.getClickedBlock().getLocation(), 
                            Ostrov.instance.getDataFolder() + "/schematics",
                            "test1x1",
                            true
                        );
                    }
                } else if (e.getItem().getType() == Material.IRON_INGOT) {
                    e.setCancelled(true);
                    if (p.isSneaking()) {
                        WE.paste(
                            p,
                            e.getClickedBlock().getLocation(),
                            Ostrov.instance.getDataFolder() + "/schematics",
                            "test10x10",
                            true,
                            false,
                            () -> {
                                Bukkit.broadcastMessage("готово");
                            },
                            WE.scipOnPasteDefault
                        );
                    } else {
                        save(
                            p, 
                            e.getClickedBlock().getLocation(), 
                            e.getClickedBlock().getLocation().clone().add(10, 10, 10), 
                            Ostrov.instance.getDataFolder() + "/schematics",
                            "test10x10",
                            true
                        );
                        //save(p, e.getClickedBlock().getLocation(), e.getClickedBlock().getLocation().clone().add(1, 1, 1), "test2x2", true);
                    }
                    //paste(p, p.getLocation().clone().subtract(5, 5, 5), "test"); 
                    //p.sendMessage("§eПересчёт уровня острова, результат: "+is.stats.blockPrice);
                } else if (e.getItem().getType() == Material.GOLD_INGOT) {
                    e.setCancelled(true);
                    if (p.isSneaking()) {
                        
                        if (sh100x100!=null) {
                            WE.paste(
                                p,
                                e.getClickedBlock().getLocation(),
                                sh100x100,
                                true
                            );
                        }
                       /* WE.paste(
                            p,
                            e.getClickedBlock().getLocation(),
                            Ostrov.instance.getDataFolder() + "/schematics",
                            "test100x100",
                            true,
                            false,
                            () -> {
                                Bukkit.broadcastMessage("готово");
                            },
                            WE.scipOnPasteDefault
                        );/
                        //paste(p, e.getClickedBlock().getLocation(), "test10x10", true, true); 
                    } else {
                        
                        sh100x100 = new Schematic(p,
                                        "test100x100",
                                        e.getClickedBlock().getLocation(),
                                        e.getClickedBlock().getLocation().clone().add(100, 100, 100),
                                        true
                                    );
                        //sh.save(p);
                        /*save(
                            p, 
                            e.getClickedBlock().getLocation(), 
                            e.getClickedBlock().getLocation().clone().add(100, 100, 100), 
                            Ostrov.instance.getDataFolder() + "/schematics",
                            "test100x100",
                            true
                        );/
                        //save(p, e.getClickedBlock().getLocation(), e.getClickedBlock().getLocation().clone().add(9, 9, 9), "test10x10", true);
                    }
                    //paste(p, p.getLocation().clone().subtract(5, 5, 5), "test"); 
                    //p.sendMessage("§eПересчёт уровня острова, результат: "+is.stats.blockPrice);
                }
        }
    }    
    */
    
    
    
    
    public static Schematic getSchematic(final CommandSender cs, final String schemName) {
        if (schematics.containsKey(schemName)) return schematics.get(schemName);
        final File file = new File(Ostrov.instance.getDataFolder() + "/schematics" , schemName+".schem");
        if (!file.exists()) {
            if (cs!=null) cs.sendMessage("§cНет файла схематика "+schemName);
            return null;
        }        
        Schematic sh = new Schematic(cs, file, false);
        schematics.put(sh.name, sh);
        return sh;
    }
    
    
    
    
    //не убирать!! использует регионГуи
    public static boolean hasJob(final CommandSender cs) {
        for (PasteJob pj : jobs.values()) {
            if (pj.pause || pj.isCanceled()) continue;
            if (pj.cs==cs) return true;
        }
        return false;
    }


    
    
    
    public static void save (final CommandSender cs, final Location loc1,  final Location loc2, final String schemName) {
        Schematic sh = new Schematic(cs, schemName, loc1, loc2, true);
        schematics.put(sh.name, sh);
    }
    
    
    
    
    @Deprecated //не убирать!!  использует регионГуи
    public static void save (final CommandSender cs, final Location loc1,  final Location loc2, final String folderPath, final String fileName, final boolean notify) {
        if ( loc1==null || loc2==null || fileName==null || fileName.isEmpty() ) return;
        Schematic sh = new Schematic(cs, fileName, loc1, loc2, true, folderPath, ".region", scipOnPasteDefault);
        schematics.put(sh.name, sh);        

    }   



    
    
    public static void paste (final CommandSender cs, final Location loc, final Schematic schem, final boolean pasteAir) {
        PasteJob job = new PasteJob(cs, loc, schem, pasteAir);
        jobs.put(job.getId(), job);
    } 
    
    
    @Deprecated  //не убирать!! использует регионГуи
    public static void paste (final CommandSender cs, final Location loc, final String folderPath, final String fileName, final boolean notify, final boolean deleteFile, final Runnable onDone) {
        paste(cs, loc, folderPath, fileName, notify, deleteFile, onDone, new ArrayList<>());
    }
    
    @Deprecated //не убирать!! использует регионГуи
    public static void paste (final CommandSender cs, final Location loc, final String folderPath, final String fileName, final boolean notify, final boolean deleteFile, final Runnable onDone, final List<Material>scipOnPaste) {
        if (cs==null || loc==null || fileName==null || fileName.isEmpty()) return;
        final File regionDataFile = new File( (folderPath==null || folderPath.isEmpty()) ? Ostrov.instance.getDataFolder() + "/schematics" : folderPath , fileName+".region");
        if (!regionDataFile.exists()) {
            if (notify) cs.sendMessage("§cНет сохранения для региона "+fileName);
            return;
        }
        final Schematic schem = new Schematic(cs, regionDataFile, deleteFile);
        paste (cs, loc, schem, true);
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

