package ru.komiss77.commands;

import com.google.common.io.Files;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.modules.world.WorldManager.Generator;
import ru.komiss77.Ostrov;
import ru.komiss77.menu.Worlds;
import ru.komiss77.utils.inventory.SmartInventory;




//public class WorldManagerCommand implements CommandExecutor{
public class WorldManagerCmd implements CommandExecutor, TabCompleter{

    public static List<String> commands = Arrays.asList( "list", "tp", "create", "load", "import", "save", "unload", "setwordspawn", "delete", "backup", "restore");


     
    
 // !!!!!!!!!!!!!!!!!!  Не перемещать! ссылаются плагины!!
   
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] strings) {
        List <String> sugg = new ArrayList<>();
//System.out.println("l="+strings.length+" 0="+strings[0]);




        switch (strings.length) {
            
            case 1:
                //0- пустой (то,что уже введено)
                for (String s : commands) {
                    if (s.startsWith(strings[0])) sugg.add(s);
                }
                
                // if (ApiOstrov.isLocalBuilder(cs, false)){
                //     for (String s : adminCommands) {
                //         if (s.startsWith(strings[0])) sugg.add(s);
                //     }
                //  }
                break;
                
            case 2:
                //1-то,что вводится (обновляется после каждой буквы
//System.out.println("l="+strings.length+" 0="+strings[0]+" 1="+strings[1]);
                if (strings[0].equalsIgnoreCase("tp") ||
                        strings[0].equalsIgnoreCase("unload") ||
                        strings[0].equalsIgnoreCase("delete") ||
                        strings[0].equalsIgnoreCase("save") ||
                        strings[0].equalsIgnoreCase("fill") ||
                        strings[0].equalsIgnoreCase("trim") ||
                        strings[0].equalsIgnoreCase("backup") 
                        ) {
                    for (World w : Bukkit.getWorlds()) {
                        sugg.add(w.getName());
                    }
                } else if (strings[0].equalsIgnoreCase("load") || strings[0].equalsIgnoreCase("import")) { //для импорт - скан папок с level.dat но не загруженных
                    
                    FileFilter worldFolderFilter  = new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            
                            if (file.isDirectory() && file.listFiles().length>=2) {
                                final File[] files = file.listFiles();
                                for (final File f : files) {
                                    if (f.getName().equals("level.dat")) {
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }
                    };
                    
                    for (File serverWorldFolder : Bukkit.getWorldContainer().listFiles(worldFolderFilter)) {
                        if (Bukkit.getWorld(serverWorldFolder.getName())==null) {
                            sugg.add(serverWorldFolder.getName());
                        }
                    }
                    
                }// if (strings[0].equalsIgnoreCase("ChestManager")) {
                //  sugg.addAll(plugin.kits.keySet());
                // } 
                break;
                
            case 3:
                if (strings[0].equalsIgnoreCase("create") || strings[0].equalsIgnoreCase("load") || strings[0].equalsIgnoreCase("import")) {
                    //for (WorldType type : WorldType.values()) {
                    //    sugg.add(type.toString());
                    //}
                    for (World.Environment env : World.Environment.values()) {
                        sugg.add(env.toString());
                    }
                }
                break;
                
            case 4:
                if (strings[0].equalsIgnoreCase("create") || strings[0].equalsIgnoreCase("load") || strings[0].equalsIgnoreCase("import")) {
                    //for (WorldType type : WorldType.values()) {
                    //    sugg.add(type.toString());
                    //}
                    for (Generator gen : Generator.values()) {
                        sugg.add(gen.toString());
                    }
                }
                break;
                
        }
        
       return sugg;
    }
       
      
       
    
    
    
    
    
    
    
    
    
    
    
    
    
       
       
       
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arg) {
        
        
        if (!ApiOstrov.isLocalBuilder(sender, true)) {
            return true;
        }
        if (arg.length==0) {
            help(sender);
            return true;
        }
        
        final String sub_command = arg[0].toLowerCase();
        Player p = null;
        if (sender instanceof Player) p = (Player) sender;
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        if (sub_command.equals("create")) {
            if (arg.length != 4) {
                sender.sendMessage("§ccreate <название> <провайдер> <генератор>");
                return true;
            }
            
            boolean valid = false;
            for (World.Environment env : World.Environment.values()) {
                if (env.toString().equalsIgnoreCase(arg[2])) {
                    valid=true;
                    break;
                }
            }
            if (!valid) {
                sender.sendMessage("§cПровайдеры: §e"+WorldManager.possibleEnvironment());
                return true;
            }
            
            valid = false;
            for (Generator gen : Generator.values()) {
                if (gen.toString().equalsIgnoreCase(arg[3])) {
                    valid=true;
                    break;
                }
            }
            if (!valid) {
                sender.sendMessage("§cГенераторы: §e"+WorldManager.possibleGenerator());
                return true;
            }

            World.Environment env = World.Environment.valueOf(arg[2].toUpperCase());
            
            WorldManager.create(sender, arg[1], env, Generator.fromString(arg[3]), true);
            return true;
            
            
            
        } else if (sub_command.equals("delete")) {
            if (arg.length != 2) {
                sendCommandUsage(sender, "WorldManager Delete", "<Name>", new String[0]);
                return true;
            }
            WorldManager.delete(sender, arg[1]);
            return true;
            
            
            
            
            
            
            
            
            
            
        } else if (sub_command.equals("save")) {
            if (arg.length != 2) {
                sendCommandUsage(sender, "WorldManager save", "<Name>", new String[0]);
                return true;
            }
            final World world = Bukkit.getWorld(arg[1]);
            if (world==null) {
                sender.sendMessage("§cМир "+arg[1]+" не найден!");
                return true;
            }
            Bukkit.getWorld(arg[1]).save();
            return true;
            
            
            
            
            
            
            
            
            
            
        } else if (sub_command.equals("setwordspawn")) {
            if (p==null) {
                sender.sendMessage(Ostrov.prefix+" §cэто не консольная команда!");
            } else {
                p.getWorld().setSpawnLocation(p.getLocation());
                sender.sendMessage(Ostrov.prefix+" §aточка спавна мира установлена под ногами!");
            }
            return true;
            
            
            
            
            
            
            
            
        /*    
            
        } else if (sub_command.equals("fill")) {
            World world = p.getWorld();
            if (arg.length == 2) {
                world = Bukkit.getWorld(arg[1]);
            }
            if (world == null) {
                sender.sendMessage(Ostrov.prefix+"Загруженный мир с таким названием не найден!");
                return true;
            }
            //if (p==null) {
            //    sender.sendMessage(Ostrov.prefix+" §cэто не консольная команда!");
            //} else {
                //World world = p.getWorld();
               // if (arg.length == 2) {
               //     world = Bukkit.getWorld(arg[1]);
              //  }
              //  if (world == null) {
              //      sender.sendMessage(Ostrov.prefix+"Загруженный мир с таким названием не найден!");
              //      return true;
              //  }
                
                //WorldManagerCommand.openWorldMenu(p);

                
            //}
            return true;
            
            
            
            
            
            
            
            
            
            
        } else if (sub_command.equals("trim")) {
            World world = p.getWorld();
            if (arg.length == 2) {
                world = Bukkit.getWorld(arg[1]);
            }
            if (world == null) {
                sender.sendMessage(Ostrov.prefix+"Загруженный мир с таким названием не найден!");
                return true;
            }
            
            return true;
            
            
            
            */
            
            
            
            
            
            
        } else if (sub_command.equalsIgnoreCase("import") || sub_command.equalsIgnoreCase("load")) {
            
            String envString = "NORMAL"; 
            String genString = "empty";
            
            if (arg.length <3 || arg.length > 4) {
                sender.sendMessage("§c"+sub_command.toLowerCase()+" §c<название> §e<провайдер> [генератор]");
                return true;
            }
            
            if (arg.length>=3) envString = arg[2];
            if (arg.length>=4) genString = arg[3];
            
            boolean valid = false;
            for (World.Environment env : World.Environment.values()) {
                if (env.toString().equalsIgnoreCase(envString)) {
                    valid=true;
                    break;
                }
            }
            if (!valid) {
                sender.sendMessage("§cПровайдеры: §e"+WorldManager.possibleEnvironment());
                return true;
            }
            
            valid = false;
            for (Generator gen : Generator.values()) {
                if (gen.toString().equalsIgnoreCase(genString)) {
                    valid=true;
                    break;
                }
            }
            if (!valid) {
                sender.sendMessage("§cГенераторы: §e"+WorldManager.possibleGenerator());
                return true;
            }

            World.Environment env = World.Environment.valueOf(envString.toUpperCase());
            
            WorldManager.load(sender, arg[1], env, Generator.fromString(genString));
            //WorldManager.load(sender, arg[1]);
            return true;
            
            
            
            
            
            
            
            
            
            
            
            
        } else if (sub_command.equals("unload")) {
            if (arg.length != 2) {
                sendCommandUsage(sender, "WorldManager unload", "<Name>", new String[0]);
                return true;
            }
            final World world = Bukkit.getWorld(arg[1]);
            if (world != null) {
                if (!world.getPlayers().isEmpty()) {
                    sender.sendMessage(Ostrov.prefix+"Все игроки должны покинуть мир перед удалением!");
                    world.getPlayers().stream().forEach((p1) -> {
                        sender.sendMessage(Ostrov.prefix+"- " + p1.getName());
                    });
                    return false;
                }
                Bukkit.unloadWorld(world, true);
                sender.sendMessage(Ostrov.prefix+" мир "+arg[1]+" выгружен!");
            } else {
                sender.sendMessage(Ostrov.prefix+"Загруженный мир с таким названием не найден!");
            }
            return true;
            
            /*
                public static void pack(final World world, final String zipFilePath) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Path p = Files.createFile(Paths.get(zipFilePath));
                    try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
                        Path pp = Paths.get(world.getWorldFolder().getAbsolutePath());
                        Files.walk(pp)
                                .filter(path -> !Files.isDirectory(path))
                                .forEach(path -> {
                                    ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                                    try {
                                        zs.putNextEntry(zipEntry);
                                        Files.copy(path, zs);
                                        zs.closeEntry();
                                    } catch (IOException e) {
                                        SW.log_err("не удалось сохранить копию мира1 "+world.getName()+": "+e.getMessage());
                                    }
                                });
                    }
                } catch (IOException ex) {
                    SW.log_err("не удалось сохранить копию мира2 "+world.getName()+": "+ex.getMessage());
                }
            }
        }.runTaskAsynchronously(SW.plugin);
    }
            */
            
        } else if (sub_command.equals("backup") || sub_command.equals("restore")) {
            if (arg.length != 2) {
                sendCommandUsage(sender, "WorldManager " + sub_command.substring(0, 1).toUpperCase() + sub_command.substring(1, sub_command.length()), "<Name>", new String[0]);
                return true;
            }
            final World world2 = Bukkit.getWorld(arg[1]);
            if (world2 == null) {
                sender.sendMessage(Ostrov.prefix+"Загруженный мир с таким названием не найден!");
                return true;
            }
            if (sub_command.equals("backup")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        sender.sendMessage(Ostrov.prefix+"Создаём резервную копию мира "+world2.getName()+"..");
                        final long currentTimeMillis = System.currentTimeMillis();
                        copyFile(world2.getWorldFolder(), new File(Ostrov.instance.getDataFolder() + "/backup", world2.getName()));
                        sender.sendMessage(Ostrov.prefix+"§aРезервная копия создана §7за §5"+(System.currentTimeMillis() - currentTimeMillis) + "ms!");
                    }
                }.runTaskAsynchronously(Ostrov.instance);
                return true;
            }
            if (!sub_command.equals("restore")) {
                return true;
            }
            if (!world2.getPlayers().isEmpty()) {
                sender.sendMessage(Ostrov.prefix+"В мире не должно быть игроков!");
                    world2.getPlayers().stream().forEach((p1) -> {
                        sender.sendMessage(Ostrov.prefix+"- " + p1.getName());
                    });
                return true;
            }
            final File file5 = new File(Ostrov.instance + "/backup", world2.getName());
            if (!file5.exists()) {
                sender.sendMessage(Ostrov.prefix+"Копии этого мира не найдено!");
                return true;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    sender.sendMessage(Ostrov.prefix+"Восстановление мира "+world2.getName()+" из резервной копии... ");
                    final long currentTimeMillis = System.currentTimeMillis();
                    final File worldFolder = world2.getWorldFolder();
                    final World.Environment environment = world2.getEnvironment();
                    Bukkit.unloadWorld(world2, false); //тут не надо сохранять - на подмену!
                    deleteFile(worldFolder);
                    copyFile(file5, worldFolder);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.createWorld(new WorldCreator(file5.getName()).environment(environment));
                            sender.sendMessage(Ostrov.prefix+"Мир §b"+file5.getName()+"§aвосстановлен из копии за §5"+(System.currentTimeMillis() - currentTimeMillis) + "ms!");
                            //sender.sendMessage(Ostrov.prefix+"If the world didn't restore correctly, try using the restore command again!");
                        }
                    }.runTask(Ostrov.instance);
                }
            }.runTaskAsynchronously(Ostrov.instance);
            return true;
            
            
            
        } else if (sub_command.equals("tp")) {
            //if (!sender.hasPermission("ostrov.worldmanager")) {
            //    return true;
            //}
            if (arg.length != 2) {
                sendCommandUsage(sender, "wm Tp", "<Name>", new String[0]);
                return true;
            }
            final Player player17 = (Player)sender;
            final String s9 = arg[1];
            if (Bukkit.getWorld(s9) == null) {
                player17.sendMessage(Ostrov.prefix+"Мир с таким названием не найден!");
                return true;
            }
            player17.teleport(Bukkit.getWorld(s9).getSpawnLocation());
            player17.sendMessage(Ostrov.prefix+"Вы перемещены в мир §2"+s9);
            return true;
            
            
        } else {
            
            if (sub_command.equals("list")) {
                sender.sendMessage("");
                sender.sendMessage(Ostrov.prefix+"Загружено миров: §5"+Bukkit.getWorlds().size());
                Bukkit.getWorlds().stream().forEach((world3) -> {
                    TextComponent msg=new TextComponent( 
                            "§b- §e"+
                            world3.getName()+
                            " §7 ("+world3.getEnvironment().name()+
                            ", "+
                            ( world3.getGenerator()==null ? "null" :    (world3.getGenerator().getClass().getName().contains(".") ? world3.getGenerator().getClass().getName().substring(world3.getGenerator().getClass().getName().lastIndexOf(".")+1) : world3.getGenerator().getClass().getName() )   )+
                            ", "+
                            world3.getDifficulty().name()+
                            ") §8>ТП< "
                    );
                    msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "§7Чанков загружено: §6"+world3.getLoadedChunks().length +"§7, Игроки: §6"+world3.getPlayers().size()+"§7, ПВП: "+(world3.getPVP()?"§4Да":"§2Нет")+"§7, Энтити: §6"+world3.getEntities().size() ).create() ) );
                    msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wm tp " + world3.getName()));
                    sender.spigot().sendMessage(msg);
                    //sender.sendMessage("§b- §5"+world3.getName()+"§7 -> Окружение: §e"+world3.getEnvironment().name()+"§7, Сложность: §e"+ world3.getDifficulty().name() + "§7, ПВП: §e"+world3.getPVP()+"§7, Игроки: §e"+world3.getPlayers().size());
                });
                sender.sendMessage("");
                return true;
            }
            
            help(sender);
            return true;
        } 



        
        
        
        
     //   return true;
    }

    private static void help (final CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage("/wm list §7-  список миров");
        sender.sendMessage("/wm create <World> <type> §7-  создать (normal, nether, the_end, empty)");
        sender.sendMessage("/wm delete <World> §7-  удалить мир");
        sender.sendMessage("/wm import <World> §7-  импортировать мир");
        sender.sendMessage("/wm backup <World> §7-  создать резервную копию мира");
        sender.sendMessage("/wm restore <World> §7-  восстановить мир из резервной копии");
        sender.sendMessage("/wm tp <World> §7-  переместиться с мир");
        sender.sendMessage("");
    }
    
    private static void sendCommandUsage(final CommandSender commandSender, final String s, final String s2, final String... array) {
        commandSender.sendMessage(Ostrov.prefix + " пример: /§2"+s+"§7 "+s2);
        for (int length = array.length, i = 0; i < length; ++i) {
            commandSender.sendMessage(Ostrov.prefix+"§b- §7"+array[i]);
        }
    }
    

    public static void copyFile(final File source, final File destination) {
        //if (!new ArrayList(Arrays.asList("session.dat")).contains(source.getName())) {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }

            for (final String fileName : source.list()) {
                
                if ( fileName.equalsIgnoreCase("data") || 
                        fileName.equalsIgnoreCase("datapacks") || 
                        fileName.equalsIgnoreCase("playerdata") || 
                        fileName.equalsIgnoreCase("poi") 
                    ) continue; //пропускаем всякий хлам
                
                copyFile(new File(source, fileName), new File(destination, fileName));
                
            }
            //final String[] sourceFileNames = source.list();
            //for (int i = sourceFileNames.length, i = 0; i < length; ++i) {
                //final String s = list[i];
                //copyFile(new File(source, s), new File(destination, s));
            //}
        } else {
            
            if ( source.getName().contains("level.dat_old") ||
                    source.getName().contains("session.lock")  ||
                    source.getName().contains("uid.dat") 
                ) return; //пропускаем ненужные
                
            try {
                Files.copy(source, destination);
            } catch (IOException ex) {
               // ex.printStackTrace();
            }
        }
        //}
    }

    
    
    
    public static void deleteFile(final File file) {
        if (file.exists()) {
            File[] listFiles;
            for (int length = (listFiles = file.listFiles()).length, i = 0; i < length; ++i) {
                final File file2 = listFiles[i];
                if (file2.isDirectory()) {
                    deleteFile(file2);
                }
                else {
                    file2.delete();
                }
            }
        }
        file.delete();
    }
    

    
    
    public static void openWorldMenu(final Player p) {
        SmartInventory.builder()
                .id("Worlds"+p.getName())
                .provider(new Worlds())
                .size(3, 9)
                .title("§2Миры сервера")
                .build().open(p);
    }
    
}
