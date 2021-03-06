package ru.komiss77.modules.world;

import ru.komiss77.modules.world.EmptyChunkGenerator;
import ru.komiss77.modules.world.LavaOceanGenerator;
import java.io.File;
import java.io.IOException;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ru.komiss77.Cfg;
import ru.komiss77.commands.WorldManagerCmd;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.wordBorder.DynMapFeatures;
import ru.komiss77.modules.wordBorder.WorldFillTask;
import ru.komiss77.modules.wordBorder.WorldTrimTask;
import ru.komiss77.utils.OstrovConfig;
import ru.komiss77.utils.TransLiter;


public class WorldManager extends Initiable {
    
    
    public static volatile WorldFillTask fillTask = null;
    public static volatile WorldTrimTask trimTask = null;   

    public static OstrovConfig config;
    public static boolean shapeRound = true;
    public static boolean dynmapEnable = true;
    public static String dynmapMessage;
    private static int remountDelayTicks = 0;
    public static int fillAutosaveFrequency = 30;
    public static int fillMemoryTolerance = 500;
    private static Runtime rt;

    
    
    
    public static void tryRestoreFill(final String worldName) {
        if (fillTask==null) {
            if (config.getConfigurationSection("fillTask")!=null && WorldManager.config.getString("fillTask.world").equals(worldName)) {
               //String worldName = config.getString("fillTask.world");
                int fillDistance = config.getInt("fillTask.fillDistance", 176);
                int chunksPerRun = config.getInt("fillTask.chunksPerRun", 5);
                int tickFrequency = config.getInt("fillTask.tickFrequency", 20);
                int fillX = config.getInt("fillTask.x", 0);
                int fillZ = config.getInt("fillTask.z", 0);
                int fillLength = config.getInt("fillTask.length", 0);
                int fillTotal = config.getInt("fillTask.total", 0);
                boolean forceLoad = config.getBoolean("fillTask.forceLoad", false);
                RestoreFillTask(worldName, fillDistance, chunksPerRun, tickFrequency, fillX, fillZ, fillLength, fillTotal, forceLoad);
            }
        } 
    }
    
    
    
    
    
    public WorldManager () {
        
        rt = Runtime.getRuntime();
        
        config = Cfg.manager.getNewConfig("worldManager.yml", new String[]{"", "Ostrov worldManager config file", ""} );
        config.addDefault("roundBorder", false);
        config.addDefault("remountDelayTicks", 0);
        config.addDefault("dynmapBorderEnabled", false);
        config.addDefault("dynmapBorderMessage", "?????????????? ????????.");
        config.addDefault("fillAutosaveFrequency", 30);
        config.addDefault("fillMemoryTolerance", 500);
        config.saveConfig();
        
        shapeRound = config.getBoolean("roundBorder", true);
        remountDelayTicks = config.getInt("remountDelayTicks", 0);
        dynmapEnable = config.getBoolean("dynmapBorderEnabled", true);
        dynmapMessage = config.getString("dynmapBorderMessage", "?????????????? ????????.");
        fillAutosaveFrequency = config.getInt("fillAutosaveFrequency", 30);
        fillMemoryTolerance = config.getInt("fillMemoryTolerance", 500);
        
//System.out.println("----------------- "+config.getConfigurationSection("fillTask"));
        
        DynMapFeatures.setup();
    }
    
    
    
    
    
    
    

    public static void RestoreFillTask(String world, int fillDistance, int chunksPerRun, int tickFrequency, int x, int z, int length, int total, boolean forceLoad) {
        fillTask = new WorldFillTask(world);
//System.out.println("===========RestoreFillTask valid?"+fillTask.valid());
        if (fillTask.valid()) {
            fillTask.continueProgress(x, z, length, total);
            int task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Ostrov.instance, fillTask, 20, tickFrequency);
            fillTask.setTaskID(task);
        }
    }

    public static void StopTrimTask() {
        if (trimTask != null && trimTask.valid()) trimTask.cancel();
    }
        
        

    public static void save(boolean storeFillTask) {	// save config to file
        if (config == null) return;

        config.set("roundBorder", shapeRound);
        config.set("remountDelayTicks", remountDelayTicks);
        config.set("dynmapBorderEnabled", dynmapEnable);
        config.set("dynmapBorderMessage", dynmapMessage);
        config.set("fillAutosaveFrequency", fillAutosaveFrequency);
        config.set("fillMemoryTolerance", fillMemoryTolerance);

        if (storeFillTask && fillTask != null && fillTask.valid()) {
            config.set("fillTask.world", fillTask.refWorld());
            config.set("fillTask.fillDistance", fillTask.refFillDistance());
            config.set("fillTask.chunksPerRun", fillTask.refChunksPerRun());
            config.set("fillTask.tickFrequency", fillTask.refTickFrequency());
            config.set("fillTask.x", fillTask.refX());
            config.set("fillTask.z", fillTask.refZ());
            config.set("fillTask.length", fillTask.refLength());
            config.set("fillTask.total", fillTask.refTotal());
            config.set("fillTask.forceLoad", fillTask.refForceLoad());
        } else {
            config.set("fillTask", fillMemoryTolerance);
        }

        config.saveConfig();

    }
        
	public static void StoreFillTask() {
		save(true);
	}
	public static void UnStoreFillTask() {
		save(false);
	}    
	public static int AvailableMemory() {
		return (int)((rt.maxMemory() - rt.totalMemory() + rt.freeMemory()) / 1048576);  // 1024*1024 = 1048576 (bytes in 1 MB)
	}

	public static boolean AvailableMemoryTooLow() {
		return AvailableMemory() < fillMemoryTolerance;
	}    
    
        
        
        
        
    @Override
    public void onDisable() {
        DynMapFeatures.removeAllBorders();
        StoreFillTask();
        if (fillTask != null && fillTask.valid()) fillTask.cancel();
    }

// !!!!!!!!!!!!!!!!!!  ???? ????????????????????! ?????????????????? ??????????????!!









    
    
    @Deprecated
    public static boolean load (CommandSender sender, String world_name) {
        return load(sender, world_name, Environment.NORMAL, Generator.Empty) !=null;
    }
    
    
    public static World load (CommandSender sender, String world_name, Environment environment,  Generator generator) {
        
        if (sender==null) sender = Bukkit.getConsoleSender();
        
        if (world_name==null || world_name.isEmpty()) {
            sender.sendMessage(Ostrov.prefix+"??c???????????????????????? ???????????????? ???????? : "+world_name);
            return null;
        }
        
        final String translitName = TransLiter.cyr2lat(world_name);
        if (!world_name.equals(translitName)) {
            sender.sendMessage(Ostrov.prefix+"??e*???????????????? ???????????????????????????? ?? "+translitName);
            world_name=translitName;
        }
        
        if (Bukkit.getWorld(world_name) != null) {
            //sender.sendMessage(Ostrov.prefix+"WorldManager : ???????? ?????? ?????? ????????????????!");
            final TextComponent msg=new TextComponent(Ostrov.prefix+"??e???????? ?????? ?????? ????????????????! ??7???? ?? ?????? - /ostrov wm tp " + world_name + " <????????");
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wm tp " + world_name));
            sender.spigot().sendMessage(msg);
            return null;
        }
        
        
        final File worldFoldersDirectory = new File(Bukkit.getWorldContainer().getPath()+"/"+world_name);
        
        if (!worldFoldersDirectory.exists() || !worldFoldersDirectory.isDirectory()) {
            sender.sendMessage(Ostrov.prefix+"??c?????????? ???????? ?? ?????????? ?????????? ???? ??????????????!");
            return null;
        }
            
        final File configFile = new File(worldFoldersDirectory, "ostrov.cfg");
        if (configFile.exists() && !configFile.isDirectory()) {
            StringBuilder sb = new StringBuilder (Ostrov.prefix);
            sb.append("??a?????????????? ???????????????????????? ?????? ????????! ");
            //sender.sendMessage(Ostrov.prefix+"??a?????????????? ???????????????????????? ?????? ????????!");
            final YamlConfiguration yml = YamlConfiguration.loadConfiguration(configFile);
            
            for (World.Environment env : World.Environment.values()) {
                if (env.toString().equalsIgnoreCase(yml.getString("environment", "NORMAL"))) {
                    environment=Environment.valueOf(yml.getString("environment", "NORMAL").toUpperCase());
                    sb.append(environment.toString()).append(", ");
                    break;
                }
            }
            
            generator = Generator.fromString( yml.getString("generator", "empty") );
            sb.append(generator.toString()).append(", ");
            
        }
        
        if (environment==null) environment = Environment.NORMAL;
        if (generator==null) generator = Generator.Empty;
        
        boolean valid_level_dat = false;
        boolean valid_regions = false;

        final String regionFolderName = environment==Environment.NORMAL ? "region" : 
                environment==Environment.NETHER ? "DIM-1" : "DIM1";

        for (final File f : worldFoldersDirectory.listFiles()) {
            if (f.isDirectory()) {
//System.out.println("folder="+f.getName());
                if (f.getName().equals(regionFolderName) && f.listFiles().length!=0) {
                    valid_regions = true;
                }

            } else {
//System.out.println("file="+f.getName());
                if (f.getName().equals("level.dat")) {
                    valid_level_dat = true;
                }
            }
            if (valid_level_dat && valid_regions) {
                break;
            }
        }

        if (!valid_level_dat) {
            sender.sendMessage(Ostrov.prefix+"??c?? ???????????????????? "+worldFoldersDirectory.getName()+" ?????? level.dat");
            return null;
        }

        if (!valid_regions) {
            sender.sendMessage(Ostrov.prefix+"??c?? ???????????????????? "+worldFoldersDirectory.getName()+" ?????? ?????????? "+regionFolderName+", ?????? ?????? ????????????.");
            return null;
        }


        sender.sendMessage(Ostrov.prefix+"??f???????????????? ???????? "+world_name+" ??7(??????????????????: "+environment.toString()+", ??????????????????: "+generator.toString()+")");
        final long currentTimeMillis5 = System.currentTimeMillis();

        final WorldCreator wc = new WorldCreator(world_name)
        .environment(environment)
        .seed(Ostrov.random.nextLong())
        ;
        
        applyGenerator(wc, generator);
        
        final World world = wc.createWorld();

        final TextComponent msg=new TextComponent(Ostrov.prefix+"?????? ???????????????? ???? ??5"+(System.currentTimeMillis() - currentTimeMillis5) + "ms" +"??7, ???? ?? ?????? - /ostrov wm tp " + world_name + " <????????");
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wm tp " + world_name));
        sender.spigot().sendMessage(msg);

        return world;


    }
    
    



















    
    
    @Deprecated
    public static boolean create(final CommandSender sender, final String world_name, final String world_type) {
        WorldType type = WorldType.fromString(world_type);
        if (!world_type.equalsIgnoreCase(type.toString())) {
            sender.sendMessage("??c?????? ???????? ?????????? ???????? normal, nether, the_end, empty");
            return false;
        }
        return create(sender, world_name, type, true);
    }
    
    
    @Deprecated
    public static boolean create(CommandSender sender, String world_name, final WorldType world_type, final boolean suggestTp) {
        return create(sender, world_name, Environment.NORMAL, Generator.Empty, suggestTp)!=null;
    }
    
    
    public static World create(CommandSender sender, String world_name, final Environment environment, final Generator generator, final boolean suggestTp) {
        if (sender==null) sender = Bukkit.getConsoleSender();
        
        if (world_name== null || world_name.isEmpty()) {
            sender.sendMessage(Ostrov.prefix+"???????????????? ???????? >"+world_name+"<????????????????????????!");
            return null;
        }
        if (Bukkit.getWorld(world_name) != null) {
            sender.sendMessage(Ostrov.prefix+"???????? ?????? ?????? ???????????? ?? ????????????????!");
            return Bukkit.getWorld(world_name);
        }
        final String translitName = TransLiter.cyr2lat(world_name);
        if (!world_name.equals(translitName)) {
            sender.sendMessage(Ostrov.prefix+"??e*???????????????? ???????????????????????????? ?? "+translitName);
            world_name=translitName;
        }
        if (!checkWorldName(world_name)) {
            sender.sendMessage(Ostrov.prefix+"???????????????????? ?????????????? [ a-z0-9/._- ]");
            //return false;
        }
        if (Bukkit.getWorld(world_name) != null) {
            sender.sendMessage(Ostrov.prefix+"?????????? ?????? ?????? ????????!");
            return Bukkit.getWorld(world_name);
        }
        
        String[] list2;
        for (int length = (list2 = Bukkit.getWorldContainer().list()).length, i = 0; i < length; ++i) {
            if (list2[i].equalsIgnoreCase(world_name)) {
                sender.sendMessage(Ostrov.prefix+"??c?????? ????????????????????, ???? ???? ????????????????! ??????????????????: ??e/ostrov wm import <Name>");
                return null;
            }
        }
        
        sender.sendMessage(Ostrov.prefix+"?????????????? ??????!");
        final long currentTimeMillis3 = System.currentTimeMillis();
        
                
        final WorldCreator wc = new WorldCreator(world_name)
        //.environment(environment).generateStructures(true);
        .environment(environment);
        
        applyGenerator(wc, generator);
        
        final World world = wc.createWorld();
        world.setSpawnLocation(0, 65, 0);
        
        
        final File configFile = new File(world.getWorldFolder(), "ostrov.cfg");
        final YamlConfiguration cfg = YamlConfiguration.loadConfiguration(configFile);

        cfg.set("environment", environment.toString());
        cfg.set("generator", generator.toString());

        try {
            cfg.save(configFile);
        } catch (IOException ex) {
            Ostrov.log_err("???? ?????????????? ?????????????????? ?????????????????? ????????: "+ex.getMessage());
        }
        
        
        if (suggestTp) {
            TextComponent msg=new TextComponent(Ostrov.prefix+"?????? ???????????? ???? ??5"+(System.currentTimeMillis() - currentTimeMillis3) + "ms" +"??7, ???? ?? ?????? - /ostrov wm tp " + world_name + " <????????");
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wm tp " + world_name));
            sender.spigot().sendMessage(msg);
        } 
        
        return world;
    }

    






















    
    
    public static boolean checkWorldName (final String message) {
      String allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_0123456789-./";
      for(int i = 0; i < message.length(); ++i) {
         if(!allowed.contains(String.valueOf(message.charAt(i)))) {
            return false;
         }
      }
      return true;
   }    
    
    
    
    
    
    public static boolean delete(CommandSender sender, String world_name) {
        if (sender==null) sender = Bukkit.getConsoleSender();
        
            final World world = Bukkit.getWorld(world_name.toLowerCase());

            final String translitName = TransLiter.cyr2lat(world_name);
            if (!world_name.equals(translitName)) {
                sender.sendMessage(Ostrov.prefix+"??e*???????????????? ???????????????????????????? ?? "+translitName);
                world_name=translitName;
            }
            
            if (world != null) {
                
                if (!world.getPlayers().isEmpty()) {
                    sender.sendMessage(Ostrov.prefix+"?????? ???????????? ???????????? ???????????????? ?????? ?????????? ??????????????????!");
                    for (Player p : world.getPlayers()) {
                        sender.sendMessage(Ostrov.prefix+"- " + p.getName());
                    }
                    return false;
                }
                Bukkit.unloadWorld(world, false); //?????? ???? ???????? ?????????????????? - ???? ????????????????!
                
                final long currentTimeMillis4 = System.currentTimeMillis();
                WorldManagerCmd.deleteFile(world.getWorldFolder());
                sender.sendMessage(Ostrov.prefix+"?????? ????????????????, ?????? ?????????? ?????????????? ???? ??5"+(System.currentTimeMillis() - currentTimeMillis4) + "ms!");
                return true;

                
            } else {
                
                sender.sendMessage(Ostrov.prefix+"?????????????????? ?????? ???? ????????????????, ???????? ?????????? ????????...");
                
                final File worldFolder = new File(Bukkit.getWorldContainer().getPath()+"/"+world_name);
                
                if (worldFolder.exists() && worldFolder.isDirectory()) {
                    final long currentTimeMillis4 = System.currentTimeMillis();
                    WorldManagerCmd.deleteFile(worldFolder);
                    sender.sendMessage(Ostrov.prefix+"?????????? ???????? ?????????????? ???? ??5"+(System.currentTimeMillis() - currentTimeMillis4) + "ms!");
                    return true;
                } else {
                    sender.sendMessage(Ostrov.prefix+"?????????? ???????? ?? ?????????? ?????????? ???? ??????????????!");
                    return false;
                }
            }
            
    }
    
    
    













/*
        
    
    public static void pack(final Island is, final String zipFilePath) {
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean update = false;
                try {
                    final File f = new File(zipFilePath);
                    if (f.exists()) {
                        f.delete();
                        update = true;
                    }
                    Path p = Files.createFile(Paths.get(zipFilePath));
                    final ZipOutputStream zipFile = new ZipOutputStream(Files.newOutputStream(p));
                    //try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
                        //final Path worldPath = Paths.get( "world/"+is.getWorld(WorldType.World).getWorldFolder().getAbsolutePath() );
                        
                    addToZip(is, zipFile, WorldType.World);

                    if (is.settings.sizeNether>0 && is.getWorld(WorldType.Nether)!=null) {
                        addToZip(is, zipFile, WorldType.Nether);
                    }

                    if (is.settings.sizeEnd>0 && is.getWorld(WorldType.End)!=null) {
                        addToZip(is, zipFile, WorldType.End);
                    }
                    //}
                    
                    is.broadcastMessage(update ? "??a?????????????????? ???????????????? ??????????????????!" : "??a?????????????????? ???????????????? ??????????????!");
                    
                } catch (IOException ex) {
                    SW.log_err("???? ?????????????? ?????????????? ?????????????????? ?????????? "+is.islandID+" : "+ex.getMessage());
                    is.broadcastMessage("??c???? ?????????????? ?????????????? ?????????????????? ?????????? - ???????????????? ??????????????????????????!");
                }
            }

        }.runTaskAsynchronously(SW.plugin);
    }
    
    
    private static void addToZip(final Island is, final ZipOutputStream zipFile, final WorldType type) {
        try {
            //final Path worldPath = Paths.get( type.toString()+"/"+is.getWorld(WorldType.World).getWorldFolder().getAbsolutePath() );
            final Path worldPath = Paths.get( is.getWorld(type).getWorldFolder().getAbsolutePath() );
            Files.walk(worldPath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        //ZipEntry zipEntry = new ZipEntry(worldPath.relativize(path).toString());
                        final ZipEntry zipEntry = new ZipEntry(type.toString()+"/"+worldPath.relativize(path).toString());
                        try {
                            zipFile.putNextEntry(zipEntry);
                            Files.copy(path, zipFile);
                            zipFile.closeEntry();
                        } catch (IOException e) {
                            SW.log_err("???????????? ???????????????????? "+is.getWorld(type).getName()+" ?? ?????????? : "+e.getMessage());
                        }
                    });
        } catch (IOException ex) {
            SW.log_err("???? ?????????????? ???????????????? ?????????? ???????? "+is.getWorld(type).getName()+" ?? ?????????? : "+ex.getMessage());
            //is.broadcastMessage("??c???? ?????????????? ?????????????? ?????????????????? ?????????? - ???????????????? ??????????????????????????!");
        }
    }
    

    */

    private static void applyGenerator(WorldCreator wc, Generator generator) {
        
       switch (generator) {
            
            case Empty : 
                wc.generator(new EmptyChunkGenerator(Ostrov.instance));
                wc.type(org.bukkit.WorldType.FLAT); //Void darkness - start at around Y=64, if you want them to start at Y=0, set the level-type in the server.properties file to FLAT. 
//System.out.println("=================== applyGenerator generateStructures(false)");
                wc.generateStructures(false);
                return;
                
            case LavaOcean : wc.generator(new LavaOceanGenerator(Ostrov.instance));
                wc.type(org.bukkit.WorldType.FLAT);
                wc.generateStructures(false);
                return;
                
            default:
                wc.type(org.bukkit.WorldType.valueOf(generator.toString().toUpperCase()));
                return;
                
        }

    }



















    
    
    
    
    @Deprecated
    public enum WorldType {
        Normal, Nether, The_end, Empty;
        
        public static WorldType fromString(final String type) {
            if (type==null) return Empty;
            for (WorldType wt:values()) {
                if (type.equalsIgnoreCase(wt.toString())) return wt;
            }
            return Empty;
        }
    }
    
    public enum Generator {
        Normal, Flat, Large_biomes, Amplified, Empty, LavaOcean;
        
        public static Generator fromString(final String type) {
            if (type==null) return Empty;
            for (Generator wt:values()) {
                if (type.equalsIgnoreCase(wt.toString())) return wt;
            }
            return Empty;
        }
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static String possibleGenerator() {
        String res = "";
        for (Generator g:Generator.values()) {
            res=res+", "+g.toString();
        }
        res=res.replaceFirst(", ", "");
        return res;
    }

    public static String possibleEnvironment() {
        String res = "";
        for (World.Environment g:World.Environment.values()) {
            res=res+", "+g.toString();
        }
        res=res.replaceFirst(", ", "");
        return res;
    }
    


    
    
    
    
    
}




