package ru.komiss77.modules.world;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.commands.WorldManagerCmd;
import ru.komiss77.Initiable;
import ru.komiss77.Ostrov;
import ru.komiss77.hook.DynmapFeatures;
import ru.komiss77.modules.translate.TransLiter;
import ru.komiss77.modules.wordBorder.WorldFillTask;
import ru.komiss77.modules.wordBorder.WorldTrimTask;
import ru.komiss77.utils.OstrovConfig;


public class WorldManager implements Initiable {
    
    
    public static volatile WorldFillTask fillTask = null;
    public static volatile WorldTrimTask trimTask = null;   

    public static OstrovConfig config;
    public static boolean shapeRound = true;
    public static boolean dynmapEnable = true;
    public static String dynmapMessage;
    public static String buildWorldSuffix;
    private static int remountDelayTicks = 0;
    public static int fillAutosaveFrequency = 30;
    public static int fillMemoryTolerance = 500;
    private static Runtime rt;

    
    public WorldManager () {
        
        rt = Runtime.getRuntime();
        
        config = Config.manager.getNewConfig("worldManager.yml", new String[]{"", "Ostrov worldManager config file", ""} );
        config.addDefault("roundBorder", false);
        config.addDefault("remountDelayTicks", 0);
        config.addDefault("dynmapBorderEnabled", false);
        config.addDefault("dynmapBorderMessage", "Граница мира.");
        config.addDefault("fillAutosaveFrequency", 30);
        config.addDefault("fillMemoryTolerance", 500);
        config.addDefault("buildWorldSuffix", "build");
        config.addDefault("autoload_worlds", Arrays.asList("some_world"));
        config.saveConfig();
        
        WorldManager.this.reload();
    }

    @Override
    public void postWorld() { //обход модулей после загрузки миров, т.к. не всё можно сделать onEnable
        reload();
    }
    
    
    @Override
    public void reload() {
        shapeRound = config.getBoolean("roundBorder", true);
        remountDelayTicks = config.getInt("remountDelayTicks", 0);
        dynmapEnable = config.getBoolean("dynmapBorderEnabled", true);
        dynmapMessage = config.getString("dynmapBorderMessage", "Граница мира.");
        fillAutosaveFrequency = config.getInt("fillAutosaveFrequency", 30);
        fillMemoryTolerance = config.getInt("fillMemoryTolerance", 500);
        buildWorldSuffix = config.getString("buildWorldSuffix", "build");
        
        final int worldEndWipeAt = Config.getVariable().getInt("worldEndMarkToWipe", 0);
        if (worldEndWipeAt>0 && worldEndWipeAt<ApiOstrov.currentTimeSec()) {
            Config.getVariable().set("worldEndMarkToWipe",0);
            Config.getVariable().saveConfig();
            
            final File endWorldFolder = new File(Bukkit.getWorldContainer().getPath()+"/world_the_end");
            WorldManagerCmd.deleteFile(endWorldFolder);
            //seed ??
            Ostrov.log_warn("Край обнулён.");
        }
//Ostrov.log("----------------- "+config.getConfigurationSection("fillTask"));
        
        DynmapFeatures.setup();
    }



    @Override
    public void onDisable() {
        DynmapFeatures.removeAllBorders();
        StoreFillTask();
        if (fillTask != null && fillTask.valid()) fillTask.cancel();
    }
    
    public static void makeWorldEndToWipe(final int afterSecond) {
        Config.getVariable().set("worldEndMarkToWipe", ApiOstrov.currentTimeSec()+afterSecond);
        Config.getVariable().saveConfig();
        Ostrov.log_warn("Край помечен на вайп через "+ApiOstrov.secondToTime(afterSecond));
    }


    
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

    
    //вызов из таймера один раз, когда загрузились баккит-миры
    public static void autoLoadWorlds() {
        for (final String worldName : config.getStringList("autoload_worlds")) {
            if (!worldName.equals("some_world")) {
                Ostrov.log_ok("WorldManager: мир "+worldName+" отправлен на загрузку.");
                load(Bukkit.getConsoleSender(), worldName, Environment.NORMAL, Generator.Normal);
            }
            //Bukkit.dispatchCommand(Bukkit.getConsoleSender(), );
        }
    }






    @Deprecated
    public static void RestoreFillTask(String world, int fillDistance, int chunksPerRun, int tickFrequency, int x, int z, int length, int total, boolean forceLoad) {
        fillTask = new WorldFillTask(world);
//Ostrov.log("===========RestoreFillTask valid?"+fillTask.valid());
        if (fillTask.valid()) {
            fillTask.continueProgress(x, z, length, total);
            int task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Ostrov.instance, fillTask, 20, tickFrequency);
            fillTask.setTaskID(task);
        }
    }

    public static void RestoreFillTask(String world, int tickFrequency, int x, int z, int length, int total) {
      fillTask = new WorldFillTask(world);
  //Ostrov.log("===========RestoreFillTask valid?"+fillTask.valid());
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
        config.set("buildWorldSuffix", buildWorldSuffix);

        if (storeFillTask && fillTask != null && fillTask.valid()) {
            config.set("fillTask.world", fillTask.worldName());
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
    
        
        
        
        


// !!!!!!!!!!!!!!!!!!  Не перемещать! ссылаются плагины!!










    
    public static World load (CommandSender sender, String world_name, Environment environment,  Generator generator) {
        
        if (sender==null) sender = Bukkit.getConsoleSender();
        
        if (world_name==null || world_name.isEmpty()) {
            sender.sendMessage(Ostrov.PREFIX+"§cНедопустимое название мира : "+world_name);
            return null;
        }
        
        final String translitName = TransLiter.cyr2lat(world_name);
        if (!world_name.equalsIgnoreCase(translitName)) {
            sender.sendMessage(Ostrov.PREFIX+"§e*Название перекодировано в "+translitName);
            world_name=translitName;
        }
        
        if (Bukkit.getWorld(world_name) != null) {

            sender.sendMessage(Component.text()
                .append(Component.text(Ostrov.PREFIX+"§eЭтот мир уже загружен! §7тп в мир - /ostrov wm tp " + world_name + " <клик"))
                .hoverEvent(HoverEvent.showText(Component.text("клик - ТП")))
                .clickEvent(ClickEvent.runCommand("/wm tp " + world_name))
                .build());//spigot().sendMessage(msg);
            return Bukkit.getWorld(world_name);
        }
        
        
        final File worldFoldersDirectory = new File(Bukkit.getWorldContainer().getPath()+"/"+world_name);
        
        if (!worldFoldersDirectory.exists() || !worldFoldersDirectory.isDirectory()) {
            sender.sendMessage(Ostrov.PREFIX+"§cПапка мира с таким путём не найдена!");
            return null;
        }
            
        final File configFile = new File(worldFoldersDirectory, "ostrov.cfg");
        if (configFile.exists() && !configFile.isDirectory()) {
            //sender.sendMessage(Ostrov.prefix+"§aнайдена конфигурация для мира!");
            final YamlConfiguration yml = YamlConfiguration.loadConfiguration(configFile);
            
            for (World.Environment env : World.Environment.values()) {
                if (env.toString().equalsIgnoreCase(yml.getString("environment", "NORMAL"))) {
                    environment=Environment.valueOf(yml.getString("environment", "NORMAL").toUpperCase());
                    break;
                }
            }
            generator = Generator.fromString( yml.getString("generator", "empty") );
        }
        
        if (environment==null) environment = Environment.NORMAL;
        if (generator==null) generator = Generator.Empty;
        
        boolean valid_level_dat = false;
        boolean valid_regions = false;

        final String regionFolderName = environment==Environment.NORMAL ? "region" : 
                environment==Environment.NETHER ? "DIM-1" : "DIM1";

        for (final File f : worldFoldersDirectory.listFiles()) {
            if (f.isDirectory()) {
//Ostrov.log("folder="+f.getName());
                if (f.getName().equals(regionFolderName) && f.listFiles().length!=0) {
                    valid_regions = true;
                }

            } else {
//Ostrov.log("file="+f.getName());
                if (f.getName().equals("level.dat")) {
                    valid_level_dat = true;
                }
            }
            if (valid_level_dat && valid_regions) {
                break;
            }
        }

        //если не убрать uid :
        // World auth-ru is a duplicate of another world and has been prevented from loading. 
        //Please delete the uid.dat file from auth-ru's world directory if you want to be able to load the duplicate world.
        final File uid = new File(worldFoldersDirectory, "uid.dat");
        if (uid.exists() && !uid.isDirectory()) {
            uid.delete();
        }
        
        if (!valid_level_dat) {
            sender.sendMessage(Ostrov.PREFIX+"§cв директории "+worldFoldersDirectory.getName()+" нет level.dat");
            return null;
        }

        if (!valid_regions) {
            sender.sendMessage(Ostrov.PREFIX+"§cв директории "+worldFoldersDirectory.getName()+" нет папки "+regionFolderName+", или она пустая.");
            return null;
        }

      //  Timer.lastWorldLoadCountDown = 10;
        
        if (sender instanceof ConsoleCommandSender) {
            Ostrov.log_ok("§fЗагрузка мира "+world_name+" (провайдер: "+environment.toString()+", генератор: "+generator.toString()+")");
        } else {
            sender.sendMessage(Ostrov.PREFIX+"§fЗагрузка мира "+world_name+" §7(провайдер: "+environment.toString()+", генератор: "+generator.toString()+")");
        }
        final long currentTimeMillis5 = System.currentTimeMillis();

        final WorldCreator wc = new WorldCreator(world_name)
        .environment(environment)
        .seed(Ostrov.random.nextLong());
        
        applyGenerator(wc, generator);
        
        final World world = wc.createWorld();

        if (sender instanceof ConsoleCommandSender) {
            Ostrov.log_ok("§2Мир загружен за "+(System.currentTimeMillis() - currentTimeMillis5) + "ms");
        } else {
            sender.sendMessage(Component.text()
                .append(Component.text(Ostrov.PREFIX+"Мир загружен за §5"+(System.currentTimeMillis() - currentTimeMillis5) + "ms" +"§7 §f>§lПЕРЕЙТИ§f<"))
                .hoverEvent(HoverEvent.showText(Component.text("клик-ТП")))
                .clickEvent(ClickEvent.runCommand("/wm tp " + world_name))
                .build());
        }


        return world;


    }
    
    


















    
    public static World create(CommandSender sender, String world_name, final Environment environment, final Generator generator, final boolean suggestTp) {
        if (sender==null) sender = Bukkit.getConsoleSender();
        
        if (world_name== null || world_name.isEmpty()) {
            sender.sendMessage(Ostrov.PREFIX+"Название мира >"+world_name+"<недопустимое!");
            return null;
        }
        if (Bukkit.getWorld(world_name) != null) {
            sender.sendMessage(Ostrov.PREFIX+"Этот мир уже создан и загружен!");
            return Bukkit.getWorld(world_name);
        }
        final String translitName = TransLiter.cyr2lat(world_name);
        if (!world_name.equalsIgnoreCase(translitName)) {
            sender.sendMessage(Ostrov.PREFIX+"§e*Название перекодировано в "+translitName);
            world_name=translitName;
        }
        if (!checkWorldName(world_name)) {
            sender.sendMessage(Ostrov.PREFIX+"Допустимые символы [ a-z0-9/._- ]");
            //return false;
        }
        if (Bukkit.getWorld(world_name) != null) {
            sender.sendMessage(Ostrov.PREFIX+"Такой мир уже есть!");
            return Bukkit.getWorld(world_name);
        }
        
        String[] list2;
        for (int length = (list2 = Bukkit.getWorldContainer().list()).length, i = 0; i < length; ++i) {
            if (list2[i].equalsIgnoreCase(world_name)) {
                sender.sendMessage(Ostrov.PREFIX+"§cМир существует, но не загружен! Загрузить: §e/ostrov wm import <Name>");
                return null;
            }
        }
        
        sender.sendMessage(Ostrov.PREFIX+"Создаём мир!");
        final long time = System.currentTimeMillis();
        
                
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
            Ostrov.log_err("не удалось сохранить настройки мира: "+ex.getMessage());
        }
        
        
        if (suggestTp) {
            sender.sendMessage(Component.text()
                .append(Component.text(Ostrov.PREFIX+"Мир создан за §5"+(System.currentTimeMillis() - time) + "ms" +"§7 §f>§lПЕРЕЙТИ§f<"))
                .hoverEvent(HoverEvent.showText(Component.text("клик-ТП")))
                .clickEvent(ClickEvent.runCommand("/wm tp " + world_name))
                .build());
        	//sender.sendMessage(Component.text(Ostrov.PREFIX+"Мир загружен за §5"+(System.currentTimeMillis() - time) + 
        	//		"ms" +"§7, тп в мир - /ostrov wm tp " + world_name + " <клик")
    		//	.clickEvent(ClickEvent.runCommand("/wm tp " + world_name)));
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
        if (!world_name.equalsIgnoreCase(translitName)) {
            sender.sendMessage(Ostrov.PREFIX+"§e*Название перекодировано в "+translitName);
            world_name=translitName;
        }

        if (world != null) {

            if (!world.getPlayers().isEmpty()) {
                sender.sendMessage(Ostrov.PREFIX+"Все игроки должны покинуть мир перед удалением!");
                for (Player p : world.getPlayers()) {
                    sender.sendMessage(Ostrov.PREFIX+"- " + p.getName());
                }
                return false;
            }
            Bukkit.unloadWorld(world, false); //тут не надо сохранять - на удаление!

            final long currentTimeMillis4 = System.currentTimeMillis();
            WorldManagerCmd.deleteFile(world.getWorldFolder());
            sender.sendMessage(Ostrov.PREFIX+"мир выгружен, его файлы удалёны за §5"+(System.currentTimeMillis() - currentTimeMillis4) + "ms!");
            return true;


        } else {

            sender.sendMessage(Ostrov.PREFIX+"указанный мир не загружен, ищем файлы мира...");

            final File worldFolder = new File(Bukkit.getWorldContainer().getPath()+"/"+world_name);

            if (worldFolder.exists() && worldFolder.isDirectory()) {
                final long currentTimeMillis4 = System.currentTimeMillis();
                WorldManagerCmd.deleteFile(worldFolder);
                sender.sendMessage(Ostrov.PREFIX+"файлы мира удалёны за §5"+(System.currentTimeMillis() - currentTimeMillis4) + "ms!");
                return true;
            } else {
                sender.sendMessage(Ostrov.PREFIX+"папки мира с таким путём не найдена!");
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
                    
                    is.broadcastMessage(update ? "§aРезервная островка обновлена!" : "§aРезервная островка создана!");
                    
                } catch (IOException ex) {
                    SW.log_err("Не удалось создать резервную копию "+is.islandID+" : "+ex.getMessage());
                    is.broadcastMessage("§cНе удалось создать резервную копию - сообщите администрации!");
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
                            SW.log_err("ошибка добавления "+is.getWorld(type).getName()+" в архив : "+e.getMessage());
                        }
                    });
        } catch (IOException ex) {
            SW.log_err("не удалось добавить копию мира "+is.getWorld(type).getName()+" в архив : "+ex.getMessage());
            //is.broadcastMessage("§cНе удалось создать резервную копию - сообщите администрации!");
        }
    }
    

    */

    private static void applyGenerator(WorldCreator wc, Generator generator) {
        
       switch (generator) {
            

            case Empty -> { 
                wc.generator(new EmptyChunkGenerator());
                wc.type(org.bukkit.WorldType.FLAT); //Void darkness - start at around Y=64, if you want them to start at Y=0, set the level-type in the server.properties file to FLAT. 
//Ostrov.log("=================== applyGenerator generateStructures(false)");
                wc.generateStructures(false);
            }
                
            case LavaOcean -> {
                wc.generator(new LavaOceanGenerator(Ostrov.instance));
                wc.type(org.bukkit.WorldType.FLAT);
                wc.generateStructures(false);
            }
                
            default -> wc.type(org.bukkit.WorldType.valueOf(generator.toString().toUpperCase()));
                
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




