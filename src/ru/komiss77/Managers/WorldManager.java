package ru.komiss77.Managers;

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
import ru.komiss77.Commands.WorldManagerCommand;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.TransLiter;


public class WorldManager {
    
    
    
    





// !!!!!!!!!!!!!!!!!!  Не перемещать! ссылаются плагины!!









    
    
    @Deprecated
    public static boolean load (CommandSender sender, String world_name) {
        return load(sender, world_name, Environment.NORMAL, Generator.Empty) !=null;
    }
    
    
    public static World load (CommandSender sender, String world_name, Environment environment,  Generator generator) {
        
        if (sender==null) sender = Bukkit.getConsoleSender();
        
        if (world_name==null || world_name.isEmpty()) {
            sender.sendMessage(Ostrov.prefix+"§cНедопустимое название мира : "+world_name);
            return null;
        }
        
        final String translitName = TransLiter.cyr2lat(world_name);
        if (!world_name.equals(translitName)) {
            sender.sendMessage(Ostrov.prefix+"§e*Название перекодировано в "+translitName);
            world_name=translitName;
        }
        
        if (Bukkit.getWorld(world_name) != null) {
            //sender.sendMessage(Ostrov.prefix+"WorldManager : Этот мир уже загружен!");
            final TextComponent msg=new TextComponent(Ostrov.prefix+"§eЭтот мир уже загружен! §7тп в мир - /ostrov wm tp " + world_name + " <клик");
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wm tp " + world_name));
            sender.spigot().sendMessage(msg);
            return null;
        }
        
        
        final File worldFoldersDirectory = new File(Bukkit.getWorldContainer().getPath()+"/"+world_name);
        
        if (!worldFoldersDirectory.exists() || !worldFoldersDirectory.isDirectory()) {
            sender.sendMessage(Ostrov.prefix+"§cПапка мира с таким путём не найдена!");
            return null;
        }
            
        final File configFile = new File(worldFoldersDirectory, "ostrov.cfg");
        if (configFile.exists() && !configFile.isDirectory()) {
            StringBuilder sb = new StringBuilder (Ostrov.prefix);
            sb.append("§aнайдена конфигурация для мира! ");
            //sender.sendMessage(Ostrov.prefix+"§aнайдена конфигурация для мира!");
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
            sender.sendMessage(Ostrov.prefix+"§cв директории "+worldFoldersDirectory.getName()+" нет level.dat");
            return null;
        }

        if (!valid_regions) {
            sender.sendMessage(Ostrov.prefix+"§cв директории "+worldFoldersDirectory.getName()+" нет папки "+regionFolderName+", или она пустая.");
            return null;
        }


        sender.sendMessage(Ostrov.prefix+"§fЗагрузка мира "+world_name+" §7(провайдер: "+environment.toString()+", генератор: "+generator.toString()+")");
        final long currentTimeMillis5 = System.currentTimeMillis();

        final WorldCreator wc = new WorldCreator(world_name)
        .environment(environment)
        .seed(Ostrov.random.nextLong())
        ;
        
        applyGenerator(wc, generator);
        
        final World world = wc.createWorld();

        final TextComponent msg=new TextComponent(Ostrov.prefix+"Мир загружен за §5"+(System.currentTimeMillis() - currentTimeMillis5) + "ms" +"§7, тп в мир - /ostrov wm tp " + world_name + " <клик");
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wm tp " + world_name));
        sender.spigot().sendMessage(msg);

        return world;


    }
    
    



















    
    
    @Deprecated
    public static boolean create(final CommandSender sender, final String world_name, final String world_type) {
        WorldType type = WorldType.fromString(world_type);
        if (!world_type.equalsIgnoreCase(type.toString())) {
            sender.sendMessage("§cТип мира может быть normal, nether, the_end, empty");
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
            sender.sendMessage(Ostrov.prefix+"Название мира >"+world_name+"<недопустимое!");
            return null;
        }
        if (Bukkit.getWorld(world_name) != null) {
            sender.sendMessage(Ostrov.prefix+"Этот мир уже создан и загружен!");
            return Bukkit.getWorld(world_name);
        }
        final String translitName = TransLiter.cyr2lat(world_name);
        if (!world_name.equals(translitName)) {
            sender.sendMessage(Ostrov.prefix+"§e*Название перекодировано в "+translitName);
            world_name=translitName;
        }
        if (!checkWorldName(world_name)) {
            sender.sendMessage(Ostrov.prefix+"Допустимые символы [ a-z0-9/._- ]");
            //return false;
        }
        if (Bukkit.getWorld(world_name) != null) {
            sender.sendMessage(Ostrov.prefix+"Такой мир уже есть!");
            return Bukkit.getWorld(world_name);
        }
        
        String[] list2;
        for (int length = (list2 = Bukkit.getWorldContainer().list()).length, i = 0; i < length; ++i) {
            if (list2[i].equalsIgnoreCase(world_name)) {
                sender.sendMessage(Ostrov.prefix+"§cМир существует, но не загружен! Загрузить: §e/ostrov wm import <Name>");
                return null;
            }
        }
        
        sender.sendMessage(Ostrov.prefix+"Создаём мир!");
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
            Ostrov.log_err("не удалось сохранить настройки мира: "+ex.getMessage());
        }
        
        
        if (suggestTp) {
            TextComponent msg=new TextComponent(Ostrov.prefix+"Мир создан за §5"+(System.currentTimeMillis() - currentTimeMillis3) + "ms" +"§7, тп в мир - /ostrov wm tp " + world_name + " <клик");
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
                sender.sendMessage(Ostrov.prefix+"§e*Название перекодировано в "+translitName);
                world_name=translitName;
            }
            
            if (world != null) {
                
                if (!world.getPlayers().isEmpty()) {
                    sender.sendMessage(Ostrov.prefix+"Все игроки должны покинуть мир перед удалением!");
                    for (Player p : world.getPlayers()) {
                        sender.sendMessage(Ostrov.prefix+"- " + p.getName());
                    }
                    return false;
                }
                Bukkit.unloadWorld(world, false); //тут не надо сохранять - на удаление!
                
                final long currentTimeMillis4 = System.currentTimeMillis();
                WorldManagerCommand.deleteFile(world.getWorldFolder());
                sender.sendMessage(Ostrov.prefix+"мир выгружен, его файлы удалёны за §5"+(System.currentTimeMillis() - currentTimeMillis4) + "ms!");
                return true;

                
            } else {
                
                sender.sendMessage(Ostrov.prefix+"указанный мир не загружен, ищем файлы мира...");
                
                final File worldFolder = new File(Bukkit.getWorldContainer().getPath()+"/"+world_name);
                
                if (worldFolder.exists() && worldFolder.isDirectory()) {
                    final long currentTimeMillis4 = System.currentTimeMillis();
                    WorldManagerCommand.deleteFile(worldFolder);
                    sender.sendMessage(Ostrov.prefix+"файлы мира удалёны за §5"+(System.currentTimeMillis() - currentTimeMillis4) + "ms!");
                    return true;
                } else {
                    sender.sendMessage(Ostrov.prefix+"папки мира с таким путём не найдена!");
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




