package ru.komiss77.commands;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.scheduler.BukkitRunnable;
import com.google.common.io.Files;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.modules.world.WorldManager.Generator;
import ru.komiss77.builder.menu.WorldSetupMenu;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.ConfirmationGUI;
import ru.komiss77.utils.inventory.SmartInventory;




//public class WorldManagerCommand implements CommandExecutor{
public class WorldManagerCmd implements CommandExecutor, TabCompleter {

    public static final List<String> commands = Arrays.asList( "list", "tp", "create", "load", "import", "save", "unload", "setwordspawn", "delete", "backup", "restore");
    //private static final HashMap<String, String> wnames = new HashMap<>();

     
    
 // !!!!!!!!!!!!!!!!!!  Не перемещать! ссылаются плагины!!
   
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] strings) {
        List <String> sugg = new ArrayList<>();
//System.out.println("l="+strings.length+" 0="+strings[0]);




        switch (strings.length) {
            
            case 1 -> {
                //0- пустой (то,что уже введено)
                for (String s : commands) {
                    if (s.startsWith(strings[0])) sugg.add(s);
                }
                // if (ApiOstrov.isLocalBuilder(cs, false)){
                //     for (String s : adminCommands) {
                //         if (s.startsWith(strings[0])) sugg.add(s);
                //     }
                //  }
            }
                
            case 2 -> {
                //1-то,что вводится (обновляется после каждой буквы
//System.out.println("l="+strings.length+" 0="+strings[0]+" 1="+strings[1]);
                if (strings[0].equalsIgnoreCase("tp") ||
                        strings[0].equalsIgnoreCase("unload") ||
                        strings[0].equalsIgnoreCase("delete") ||
                        strings[0].equalsIgnoreCase("save") ||
                        strings[0].equalsIgnoreCase("fill") ||
                        strings[0].equalsIgnoreCase("trim") ||
                        strings[0].equalsIgnoreCase("backup")) {
                    for (World w : Bukkit.getWorlds()) {
                        sugg.add(w.getName());
                    }
                } else if (strings[0].equalsIgnoreCase("load") || strings[0].equalsIgnoreCase("import")) { //для импорт - скан папок с level.dat но не загруженных
                    
                    FileFilter worldFolderFilter  = (File file) -> {
                        if (file.isDirectory() && file.listFiles().length>=2) {
                            final File[] files = file.listFiles();
                            for (final File f : files) {
                                if (f.getName().equals("level.dat")) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    };
                    
                    for (File serverWorldFolder : Bukkit.getWorldContainer().listFiles(worldFolderFilter)) {
                        if (Bukkit.getWorld(serverWorldFolder.getName())==null) {
                            sugg.add(serverWorldFolder.getName());
                        }
                    }
                    
                }// if (strings[0].equalsIgnoreCase("ChestManager")) {
                //  sugg.addAll(plugin.kits.keySet());
                // }
            }
                
            case 3 -> {
                if (strings[0].equalsIgnoreCase("create") || strings[0].equalsIgnoreCase("load") || strings[0].equalsIgnoreCase("import")) {
                    //for (WorldType type : WorldType.values()) {
                    //    sugg.add(type.toString());
                    //}
                    for (World.Environment env : World.Environment.values()) {
                        sugg.add(env.toString());
                    }
                }
            }
                
            case 4 -> {
                if (strings[0].equalsIgnoreCase("create") || strings[0].equalsIgnoreCase("load") || strings[0].equalsIgnoreCase("import")) {
                    //for (WorldType type : WorldType.values()) {
                    //    sugg.add(type.toString());
                    //}
                    for (Generator gen : Generator.values()) {
                        sugg.add(gen.toString());
                    }
                }
            }
                
        }
        
       return sugg;
    }
       
      
       
    
    
    
    
    
    
    
    
    
    
    
    
    
       
       
       
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arg) {
        
        
        if (sender==null || !ApiOstrov.isLocalBuilder(sender, true)) {
            return true;
        }
        if (arg.length==0) {
            help(sender);
            return true;
        }
        
        final String sub_command = arg[0].toLowerCase();
        final Player p = sender instanceof Player ? (Player) sender : null;
        
        
        
        
        
        
        
        
        
        
        
        
        
        
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

        	final World nw = WorldManager.create(sender, arg[1], env, Generator.fromString(arg[3]), true);
        	if (nw == null) {
                    sender.sendMessage(Ostrov.PREFIX+"Мир "+arg[1]+" не был создан... ");
                } //else wnames.put(arg[1], nw.getName());
            return true;
            
            
            
        } else if (sub_command.equals("delete")) {
            if (arg.length != 2) {
                sendCommandUsage(sender, "WorldManager Delete", "<Name>", new String[0]);
                return true;
            }
            if (p!=null) {
                ConfirmationGUI.open(p, "Удалить мир и его файлы?", (b) -> {
                    if (b) {
                        WorldManager.delete(sender, arg[1]);
                    } else {
                        p.closeInventory();
                        if (Bukkit.getWorld(arg[1]) != null)  {
                            p.sendMessage(TCUtils.format("§2> §a§lКлик - ВЫГРУЗИТЬ мир без удаления файлов §2<")
                                .clickEvent(ClickEvent.runCommand("wm unload "+arg[1]))
                            );
                        }
                    }
                });
            } else {
                WorldManager.delete(sender, arg[1]);
            }
            return true;
            
            
            
            
            
            
            
            
            
            
        } else if (sub_command.equals("save")) {
            if (arg.length != 2) {
                sendCommandUsage(sender, "WorldManager save", "<Name>", new String[0]);
                return true;
            }
            //final String nm = wnames.get(arg[1]);
            final World world = Bukkit.getWorld(arg[1]);//Bukkit.getWorld(nm == null ? arg[1] : nm);
            if (world==null) {
                sender.sendMessage("§cМир "+arg[1]+" не найден!");
                return true;
            }
            world.save();
            return true;
            
            
            
            
            
            
            
            
            
            
        } else if (sub_command.equals("setwordspawn")) {
            if (p==null) {
                sender.sendMessage(Ostrov.PREFIX+" §cэто не консольная команда!");
            } else {
                p.getWorld().setSpawnLocation(p.getLocation());
                sender.sendMessage(Ostrov.PREFIX+" §aточка спавна мира установлена под ногами!");
            }
            return true;
            
            
            
            
            
            
            
        } else if (sub_command.equalsIgnoreCase("import") || sub_command.equalsIgnoreCase("load")) {
            
            String envString = "NORMAL"; 
            String genString = "empty";
            
            if (arg.length <2) {//if (arg.length <1 || arg.length > 4) {
                sender.sendMessage("§c"+sub_command.toLowerCase()+" §c<название> §e<провайдер> [генератор]");
                return true;
            }
            
            
            //подстановка провайдера или генератора по умолчанию
            String notify = "";
            
            if (arg.length>=3) {
                envString = arg[2].toUpperCase();
            } else {
                notify = "§6Провайдер по умолчанию: §eNORMAL";
            }
            
            if (arg.length>=4) {
                genString = arg[3];
            } else {
                notify = notify.isEmpty() ? "§6Генератор по умолчанию: §eempty" : notify+"§7, §6Генератор по умолчанию: §eempty";
            }
            
            if (!notify.isEmpty()) {
                sender.sendMessage(notify);
            }
            
            //проверка введёного провайдера
            boolean valid = false;
            for (World.Environment env : World.Environment.values()) {
                if (env.toString().equals(envString)) {
                    valid=true;
                    break;
                }
            }
            if (!valid) {
                sender.sendMessage("§cПровайдеры: §e"+WorldManager.possibleEnvironment());
                return true;
            }
            
            //проверка введёного генератора
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

            final World.Environment env = World.Environment.valueOf(envString);
            final Generator gen = Generator.fromString(genString);
            
        	final World nw = WorldManager.load(sender, arg[1], env, gen);
        	if (nw == null) {
                    sender.sendMessage(Ostrov.PREFIX+"Мир "+arg[1]+" не был загружен... ");
                }
        	//else wnames.put(arg[1], nw.getName());
            return true;
            
            
            
            
            
            
            
            
            
            
            
            
        } else if (sub_command.equals("unload")) {
            if (arg.length != 2) {
                sendCommandUsage(sender, "WorldManager unload", "<Name>", new String[0]);
                return true;
            }
            //final String nm = wnames.get(arg[1]);
            final World world =  Bukkit.getWorld(arg[1]);//Bukkit.getWorld(nm == null ? arg[1] : nm);
            if (world != null) {
                if (!world.getPlayers().isEmpty()) {
                    sender.sendMessage(Ostrov.PREFIX+"Все игроки должны покинуть мир перед удалением!");
                    world.getPlayers().stream().forEach((p1) -> {
                        sender.sendMessage(Ostrov.PREFIX+"- " + p1.getName());
                    });
                    return false;
                }
                //wnames.remove(arg[1]);
                Bukkit.unloadWorld(world, true);
                sender.sendMessage(Ostrov.PREFIX+" мир "+arg[1]+" выгружен!");
            } else {
                sender.sendMessage(Ostrov.PREFIX+"Загруженный мир с таким названием не найден!");
            }
            return true;
            
        } else if (sub_command.equals("backup") || sub_command.equals("restore")) {
            if (arg.length != 2) {
                sendCommandUsage(sender, "WorldManager " + sub_command.substring(0, 1).toUpperCase() + sub_command.substring(1, sub_command.length()), "<Name>", new String[0]);
                return true;
            }
            //final String nm = wnames.get(arg[1]);
            final World world =  Bukkit.getWorld(arg[1]);//Bukkit.getWorld(nm == null ? arg[1] : nm);
            if (world == null) {
                sender.sendMessage(Ostrov.PREFIX+"Загруженный мир с таким названием не найден!");
                return true;
            }
            if (sub_command.equals("backup")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        sender.sendMessage(Ostrov.PREFIX+"Создаём резервную копию мира "+world.getName()+"..");
                        final long currentTimeMillis = System.currentTimeMillis();
                        copyFile(world.getWorldFolder(), new File(Ostrov.instance.getDataFolder() + "/backup-", world.getName()));
                        sender.sendMessage(Ostrov.PREFIX+"§aРезервная копия создана §7за §5"+(System.currentTimeMillis() - currentTimeMillis) + "ms!");
                    }
                }.runTaskAsynchronously(Ostrov.instance);
                return true;
            }
            if (!sub_command.equals("restore")) {
                return true;
            }
            if (!world.getPlayers().isEmpty()) {
                sender.sendMessage(Ostrov.PREFIX+"В мире не должно быть игроков!");
                    world.getPlayers().stream().forEach((p1) -> {
                        sender.sendMessage(Ostrov.PREFIX+"- " + p1.getName());
                    });
                return true;
            }
            final File wfile = new File(Ostrov.instance + "/backup-", world.getName());
            if (!wfile.exists()) {
                sender.sendMessage(Ostrov.PREFIX+"Копии этого мира не найдено!");
                return true;
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    sender.sendMessage(Ostrov.PREFIX+"Восстановление мира "+world.getName()+" из резервной копии... ");
                    final long currentTimeMillis = System.currentTimeMillis();
                    final File worldFolder = world.getWorldFolder();
                    final World.Environment environment = world.getEnvironment();
                    Bukkit.unloadWorld(world, false); //тут не надо сохранять - на подмену!
                    //wnames.remove(arg[1]);
                    deleteFile(worldFolder);
                    copyFile(wfile, worldFolder);
                    Ostrov.sync(() -> {
                    	final World nw = Bukkit.createWorld(new WorldCreator(wfile.getName()).environment(environment));
                    	if (nw == null) {
                            sender.sendMessage(Ostrov.PREFIX+"Мир "+wfile.getName()+" не был восстановлен... ");
                        }
                    	//else wnames.put("backup-" + world.getName(), nw.getName());
                        sender.sendMessage(Ostrov.PREFIX+"Мир §b"+wfile.getName()+"§aвосстановлен из копии за §5"+(System.currentTimeMillis() - currentTimeMillis) + "ms!");
                    });
                }
            }.runTaskAsynchronously(Ostrov.instance);
            return true;
            
        } else if (sub_command.equals("tp")) {
            
            if (p==null) {
                sender.sendMessage("§cНе консольная команда!");
            }
            if (arg.length != 2) {
                sendCommandUsage(sender, "wm Tp", "<Name>", new String[0]);
                return true;
            }
            //final String nm = wnames.get(arg[1]);
            final World world =  Bukkit.getWorld(arg[1]);//Bukkit.getWorld(nm == null ? arg[1] : nm);
            if (world == null) {
                sender.sendMessage(Ostrov.PREFIX+"Мир с таким названием не найден!");
                return true;
            }
            if (p!=null) {
                p.teleport(world.getSpawnLocation());
                p.sendMessage(Ostrov.PREFIX+"Вы перемещены в мир §2"+arg[1]);
            }
            return true;
            
            
        } else {
            
            if (sub_command.equals("list")) {
                sender.sendMessage("");
                sender.sendMessage(Ostrov.PREFIX+"Загружено миров: §5"+Bukkit.getWorlds().size());
                for (final World w : Bukkit.getWorlds()) {
                	final ChunkGenerator cg = w.getGenerator();
                    final String wgn = cg == null ? null : cg.getClass().getName();
                    sender.sendMessage(TCUtils.format( 
	                    "§b- §e"+w.getName()+
	                    " §7 ("+w.getEnvironment().name()+
	                    ", "+(wgn == null ? "null" : 
	                    (wgn.contains(".") ? wgn.substring(wgn.lastIndexOf(".")+1) : wgn))+
	                    ", "+w.getDifficulty().name()+") §8>ТП< ")
	                	.hoverEvent(HoverEvent.showText(TCUtils.format("§7Чанков загружено: §6"+w.getLoadedChunks().length+
	                		"§7, Игроки: §6"+w.getPlayers().size()+
	                		"§7, ПВП: "+(w.getPVP()?"§4Да":"§2Нет")+
	                		"§7, Энтити: §6"+w.getEntities().size() )))
	                	.clickEvent(ClickEvent.runCommand("/wm tp " + w.getName())));
                }
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
        commandSender.sendMessage(Ostrov.PREFIX + " пример: /§2"+s+"§7 "+s2);
        for (int length = array.length, i = 0; i < length; ++i) {
            commandSender.sendMessage(Ostrov.PREFIX+"§b- §7"+array[i]);
        }
    }
    

    public static void copyFile(final File source, final File destination) {
        //if (!new ArrayList(Arrays.asList("session.dat")).contains(source.getName())) {
        if (source.isDirectory()) {
            if (!destination.exists()) {
                destination.mkdirs();
            }

            for (final String fileName : source.list()) {
                
                if (fileName.equalsIgnoreCase("playerdata") || 
                        fileName.equalsIgnoreCase("poi") 
                    ) continue; //пропускаем всякий хлам
                
                copyFile(new File(source, fileName), new File(destination, fileName));
                
            }
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
    

    
    
    public static void openWorldMenu1(final Player p) {
        SmartInventory.builder()
                .id("Worlds"+p.getName())
                .provider(new WorldSetupMenu())
                .size(3, 9)
                .title("§2Миры сервера")
                .build().open(p);
    }
    
}
