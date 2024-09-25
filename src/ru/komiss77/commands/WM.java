package ru.komiss77.commands;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.common.io.Files;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.builder.menu.WorldSetupMenu;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.*;

//не переименовывать! Острова используют методы copyFile
public class WM implements OCommand {

  private static final String COMMAND = "wm";
  private static final List<String> ALIASES = List.of("worldmanager");
  private static final String DESCRIPTION = "Управление мирами";
  private static final String arg0 = "operation", arg1 = "world", arg2 = "environment", arg3 = "generator", arg4 = "arg4";
  private static final List<String> subCmd = Arrays.asList("list", "tp", "create",
      "load", "import", "save", "unload", "setspawn", "delete", "backup", "restore");

  @Override
  public LiteralCommandNode<CommandSourceStack> command() {

    return Commands.literal(COMMAND)
        .executes(executor())//выполнение без аргументов

        //1 аргумент подкоманда
        .then(Resolver.string(arg0)
            .suggests((cntx, sb) -> {
              final CommandSender cs = cntx.getSource().getSender();
              if (ApiOstrov.isLocalBuilder(cs)) {
                subCmd.stream()
                    .filter(c -> c.startsWith(sb.getRemaining()))
                    .forEach(c -> sb.suggest(c));
              }
              return sb.buildFuture();
            })
            .executes(executor())//выполнение c 1 аргументом

            //2 аргумент название мира
            .then(Resolver.string(arg1)
                .suggests((cntx, sb) -> {
                  final CommandSender cs = cntx.getSource().getSender();
                  if (ApiOstrov.isLocalBuilder(cs)) {
                    final String sub = Comm.arg(sb, 0); //смотрим подкоманду
                    switch (sub) {
                      case "tp", "unload", "delete", "save", "fill", "trim", "backup" -> {
                        Bukkit.getWorlds().stream()
                            .filter(w -> w.getName().startsWith(sb.getRemaining()))
                            .forEach(w -> sb.suggest(w.getName()));
                      }
                      case "load", "import" -> {
                        FileFilter worldFolderFilter = (File file) -> {
                          if (file.isDirectory() && file.listFiles().length >= 2) {
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
                          if (Bukkit.getWorld(serverWorldFolder.getName()) == null) {
                            sb.suggest(serverWorldFolder.getName());
                          }
                        }
                      }
                    }
                  }
                  return sb.buildFuture();
                })
                .executes(executor())//выполнение c 2 аргументами

                //3 аргумент - Environment
                .then(Resolver.string(arg2)
                    .suggests((cntx, sb) -> {
                      final CommandSender cs = cntx.getSource().getSender();
                      if (ApiOstrov.isLocalBuilder(cs)) {
                        final String a1 = Comm.arg(sb, 0); //смотрим подкоманду
                        switch (a1) {
                          case "create", "load", "import":
                            for (final World.Environment en : World.Environment.values()) {
                              sb.suggest(en.name());
                            }
                        }
                      }
                      return sb.buildFuture();
                    })
                    .executes(executor())//выполнение c 3 аргументами

                    //4 аргумент Generator
                    .then(Resolver.string(arg3)
                        .suggests((cntx, sb) -> {
                          final CommandSender cs = cntx.getSource().getSender();
                          if (ApiOstrov.isLocalBuilder(cs)) {
                            final String a1 = Comm.arg(sb, 0); //смотрим подкоманду
                            switch (a1) {
                              case "create", "load", "import":
                                for (final WorldManager.Generator gn : WorldManager.Generator.values()) {
                                  sb.suggest(gn.name());
                                }
                            }
                          }
                          return sb.buildFuture();
                        })
                        .executes(executor())//выполнение c 4 аргументами

                        //5 аргумент
                        .then(Resolver.string(arg4)
                            .suggests((cntx, sb) -> {
                              //sb.suggest("пятый");
                              return sb.buildFuture();
                            })
                            .executes(executor())//выполнение c 5 аргументами

                        )
                    )
                )
            )
        )

        .build();
  }


  private static Command<CommandSourceStack> executor() {
    return cntx -> {
      final CommandSender cs = cntx.getSource().getSender();
      int idx = cntx.getInput().indexOf(" ");
      final String[] arg; //интересуют только аргументы, сама команда типа известна
      if (idx < 0) {
        arg = new String[0]; //"без параметров!");
      } else {
        arg = cntx.getInput().substring(idx + 1).split(" ");
      }
      //тут юзаем по старинке со всеми аргументами   /команда arg[0] ... arg[4]


      if (cs == null || !ApiOstrov.isLocalBuilder(cs, true)) {
        return 0;
      }
      if (arg.length == 0) {
        help(cs);
        return 0;
      }

      final String sub_command = arg[0].toLowerCase();
      final Player p = cs instanceof Player ? (Player) cs : null;

      if (sub_command.equals("create")) {
        if (arg.length != 4) {
          cs.sendMessage("§ccreate <название> <провайдер> <генератор>");
          return 0;
        }

        boolean valid = false;
        for (World.Environment env : World.Environment.values()) {
          if (env.toString().equalsIgnoreCase(arg[2])) {
            valid = true;
            break;
          }
        }
        if (!valid) {
          cs.sendMessage("§cПровайдеры: §e" + WorldManager.possibleEnvironment());
          return 0;
        }

        valid = false;
        for (WorldManager.Generator gen : WorldManager.Generator.values()) {
          if (gen.toString().equalsIgnoreCase(arg[3])) {
            valid = true;
            break;
          }
        }
        if (!valid) {
          cs.sendMessage("§cГенераторы: §e" + WorldManager.possibleGenerator());
          return 0;
        }

        World.Environment env = World.Environment.valueOf(arg[2].toUpperCase());

        final World nw = WorldManager.create(cs, arg[1], env, WorldManager.Generator.fromString(arg[3]), true);
        if (nw == null) {
          cs.sendMessage(Ostrov.PREFIX + "Мир " + arg[1] + " не был создан... ");
        } //else wnames.put(arg[1], nw.getName());
        return 0;


      } else if (sub_command.equals("delete")) {
        if (arg.length != 2) {
          sendCommandUsage(cs, "WorldManager Delete", "<Name>", new String[0]);
          return 0;
        }
        if (p != null) {
          ConfirmationGUI.open(p, "Удалить мир и его файлы?", (b) -> {
            if (b) {
              WorldManager.delete(cs, arg[1]);
            } else {
              p.closeInventory();
              if (Bukkit.getWorld(arg[1]) != null) {
                p.sendMessage(TCUtil.form("§2> §a§lКлик - ВЫГРУЗИТЬ мир без удаления файлов §2<")
                    .clickEvent(ClickEvent.runCommand("wm unload " + arg[1]))
                );
              }
            }
          });
        } else {
          WorldManager.delete(cs, arg[1]);
        }
        return 0;


      } else if (sub_command.equals("save")) {
        if (arg.length != 2) {
          sendCommandUsage(cs, "WorldManager save", "<Name>", new String[0]);
          return 0;
        }
        //final String nm = wnames.get(arg[1]);
        final World world = Bukkit.getWorld(arg[1]);//Bukkit.getWorld(nm == null ? arg[1] : nm);
        if (world == null) {
          cs.sendMessage("§cМир " + arg[1] + " не найден!");
          return 0;
        }
        world.save();
        return 0;


      } else if (sub_command.equals("setwordspawn")) {
        if (p == null) {
          cs.sendMessage(Ostrov.PREFIX + " §cэто не консольная команда!");
        } else {
          p.getWorld().setSpawnLocation(p.getLocation());
          cs.sendMessage(Ostrov.PREFIX + " §aточка спавна мира установлена под ногами!");
        }
        return 0;


      } else if (sub_command.equalsIgnoreCase("import") || sub_command.equalsIgnoreCase("load")) {

        String envString = "NORMAL";
        String genString = "empty";

        if (arg.length < 2) {//if (arg.length <1 || arg.length > 4) {
          cs.sendMessage("§c" + sub_command.toLowerCase() + " §c<название> §e<провайдер> [генератор]");
          return 0;
        }


        //подстановка провайдера или генератора по умолчанию
        String notify = "";

        if (arg.length >= 3) {
          envString = arg[2].toUpperCase();
        } else {
          notify = "§6Провайдер по умолчанию: §eNORMAL";
        }

        if (arg.length >= 4) {
          genString = arg[3];
        } else {
          notify = notify.isEmpty() ? "§6Генератор по умолчанию: §eempty" : notify + "§7, §6Генератор по умолчанию: §eempty";
        }

        if (!notify.isEmpty()) {
          cs.sendMessage(notify);
        }

        //проверка введёного провайдера
        boolean valid = false;
        for (World.Environment env : World.Environment.values()) {
          if (env.toString().equals(envString)) {
            valid = true;
            break;
          }
        }
        if (!valid) {
          cs.sendMessage("§cПровайдеры: §e" + WorldManager.possibleEnvironment());
          return 0;
        }

        //проверка введёного генератора
        valid = false;
        for (WorldManager.Generator gen : WorldManager.Generator.values()) {
          if (gen.toString().equalsIgnoreCase(genString)) {
            valid = true;
            break;
          }
        }
        if (!valid) {
          cs.sendMessage("§cГенераторы: §e" + WorldManager.possibleGenerator());
          return 0;
        }

        final World.Environment env = World.Environment.valueOf(envString);
        final WorldManager.Generator gen = WorldManager.Generator.fromString(genString);
//Ostrov.log_warn("WM genString="+genString+" gen="+gen);
        final World nw = WorldManager.load(cs, arg[1], env, gen);
        if (nw == null) {
          cs.sendMessage(Ostrov.PREFIX + "Мир " + arg[1] + " не был загружен... ");
        }
        //else wnames.put(arg[1], nw.getName());
        return 0;


      } else if (sub_command.equals("unload")) {
        if (arg.length != 2) {
          sendCommandUsage(cs, "WorldManager unload", "<Name>", new String[0]);
          return 0;
        }
        //final String nm = wnames.get(arg[1]);
        final World world = Bukkit.getWorld(arg[1]);//Bukkit.getWorld(nm == null ? arg[1] : nm);
        if (world != null) {
          if (!world.getPlayers().isEmpty()) {
            cs.sendMessage(Ostrov.PREFIX + "Все игроки должны покинуть мир перед удалением!");
            world.getPlayers().stream().forEach((p1) -> {
              cs.sendMessage(Ostrov.PREFIX + "- " + p1.getName());
            });
            return 0;
          }
          //wnames.remove(arg[1]);
          Bukkit.unloadWorld(world, true);
          cs.sendMessage(Ostrov.PREFIX + " мир " + arg[1] + " выгружен!");
        } else {
          cs.sendMessage(Ostrov.PREFIX + "Загруженный мир с таким названием не найден!");
        }
        return 0;

      } else if (sub_command.equals("backup") || sub_command.equals("restore")) {
        if (arg.length != 2) {
          sendCommandUsage(cs, "WorldManager " + sub_command.substring(0, 1).toUpperCase() + sub_command.substring(1, sub_command.length()), "<Name>", new String[0]);
          return 0;
        }
        //final String nm = wnames.get(arg[1]);
        final World world = Bukkit.getWorld(arg[1]);//Bukkit.getWorld(nm == null ? arg[1] : nm);
        if (world == null) {
          cs.sendMessage(Ostrov.PREFIX + "Загруженный мир с таким названием не найден!");
          return 0;
        }
        if (sub_command.equals("backup")) {
          new BukkitRunnable() {
            @Override
            public void run() {
              cs.sendMessage(Ostrov.PREFIX + "Создаём резервную копию мира " + world.getName() + "..");
              final long currentTimeMillis = System.currentTimeMillis();
              copyFile(world.getWorldFolder(), new File(Ostrov.instance.getDataFolder() + "/backup-", world.getName()));
              cs.sendMessage(Ostrov.PREFIX + "§aРезервная копия создана §7за §5" + (System.currentTimeMillis() - currentTimeMillis) + "ms!");
            }
          }.runTaskAsynchronously(Ostrov.instance);
          return 0;
        }
        if (!sub_command.equals("restore")) {
          return 0;
        }
        if (!world.getPlayers().isEmpty()) {
          cs.sendMessage(Ostrov.PREFIX + "В мире не должно быть игроков!");
          world.getPlayers().stream().forEach((p1) -> {
            cs.sendMessage(Ostrov.PREFIX + "- " + p1.getName());
          });
          return 0;
        }
        final File wfile = new File(Ostrov.instance + "/backup-", world.getName());
        if (!wfile.exists()) {
          cs.sendMessage(Ostrov.PREFIX + "Копии этого мира не найдено!");
          return 0;
        }
        new BukkitRunnable() {
          @Override
          public void run() {
            cs.sendMessage(Ostrov.PREFIX + "Восстановление мира " + world.getName() + " из резервной копии... ");
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
                cs.sendMessage(Ostrov.PREFIX + "Мир " + wfile.getName() + " не был восстановлен... ");
              }
              //else wnames.put("backup-" + world.getName(), nw.getName());
              cs.sendMessage(Ostrov.PREFIX + "Мир §b" + wfile.getName() + "§aвосстановлен из копии за §5" + (System.currentTimeMillis() - currentTimeMillis) + "ms!");
            });
          }
        }.runTaskAsynchronously(Ostrov.instance);
        return 0;

      } else if (sub_command.equals("tp")) {

        if (p == null) {
          cs.sendMessage("§cНе консольная команда!");
        }
        if (arg.length != 2) {
          sendCommandUsage(cs, "wm Tp", "<Name>", new String[0]);
          return 0;
        }
        //final String nm = wnames.get(arg[1]);
        final World world = Bukkit.getWorld(arg[1]);//Bukkit.getWorld(nm == null ? arg[1] : nm);
        if (world == null) {
          cs.sendMessage(Ostrov.PREFIX + "Мир с таким названием не найден!");
          return 0;
        }
        if (p != null) {
          p.teleport(world.getSpawnLocation());
          p.sendMessage(Ostrov.PREFIX + "Вы перемещены в мир §2" + arg[1]);
        }
        return 0;


      } else {

        if (sub_command.equals("list")) {
          cs.sendMessage("");
          cs.sendMessage(Ostrov.PREFIX + "Загружено миров: §5" + Bukkit.getWorlds().size());
          for (final World w : Bukkit.getWorlds()) {
            final ChunkGenerator cg = w.getGenerator();
            final String wgn = cg == null ? null : cg.getClass().getName();
            cs.sendMessage(TCUtil.form(
                    "§b- §e" + w.getName() +
                        " §7 (" + w.getEnvironment().name() +
                        ", " + (wgn == null ? "null" :
                        (wgn.contains(".") ? wgn.substring(wgn.lastIndexOf(".") + 1) : wgn)) +
                        ", " + w.getDifficulty().name() + ") §8>ТП< ")
                .hoverEvent(HoverEvent.showText(TCUtil.form("§7Чанков загружено: §6" + w.getLoadedChunks().length +
                    "§7, Игроки: §6" + w.getPlayers().size() +
                    "§7, ПВП: " + (w.getPVP() ? "§4Да" : "§2Нет") +
                    "§7, Энтити: §6" + w.getEntities().size())))
                .clickEvent(ClickEvent.runCommand("/wm tp " + w.getName())));
          }
          cs.sendMessage("");
          return 0;
        }

        help(cs);
        return 0;
      }

    };

  }


  private static void help(final CommandSender sender) {
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
    commandSender.sendMessage(Ostrov.PREFIX + " пример: /§2" + s + "§7 " + s2);
    for (int length = array.length, i = 0; i < length; ++i) {
      commandSender.sendMessage(Ostrov.PREFIX + "§b- §7" + array[i]);
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

      if (source.getName().contains("level.dat_old") ||
          source.getName().contains("session.lock") ||
          source.getName().contains("uid.dat")
      ) return; //пропускаем ненужные

      try {
        Files.copy(source, destination);
      } catch (IOException ex) {
        ex.printStackTrace();
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
        } else {
          file2.delete();
        }
      }
    }
    file.delete();
  }


  public static void openWorldMenu1(final Player p) {
    SmartInventory.builder()
        .id("Worlds" + p.getName())
        .provider(new WorldSetupMenu())
        .size(3, 9)
        .title("§2Миры сервера")
        .build().open(p);
  }


  @Override
  public List<String> aliases() {
    return ALIASES;
  }

  @Override
  public String description() {
    return DESCRIPTION;
  }

}


class WorldSelectMenu implements InventoryProvider {

  private static final ItemStack fill = new ItemBuilder(ItemType.BLACK_STAINED_GLASS_PANE).name("§8.").build();

  @Override
  public void init(final Player p, final InventoryContent contents) {
    p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
    //contents.fillRect(0,0,  2,8, ClickableItem.empty(fill));

    final Oplayer op = PM.getOplayer(p);

    final Pagination pagination = contents.pagination();
    final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

    contents.fillRect(0, 0, 2, 8, ClickableItem.empty(fill));

    for (final World world : Bukkit.getWorlds()) {

      menuEntry.add(ClickableItem.of(new ItemBuilder(getWorldMat(world))
          .name(world.getName())
          .lore(op.world_positions.containsKey(world.getName()) ? "§7ЛКМ - ТП на точку выхода" : "")
          .lore("§7ПКМ - ТП на точку спавна мира")
          .lore("")
          .build(), e -> {
        if (e.isLeftClick() && op.world_positions.containsKey(world.getName())) {
          final Location exit = LocUtil.stringToLoc(op.world_positions.get(world.getName()), false, false);
          ApiOstrov.teleportSave(p, exit, true);//p.teleport( world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        } else {
          ApiOstrov.teleportSave(p, world.getSpawnLocation(), true);//p.teleport( world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
      }));
    }

    pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
    pagination.setItemsPerPage(9);

    contents.set(2, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e ->
        p.closeInventory()
    ));


    if (!pagination.isLast()) {
      contents.set(2, 8, ClickableItem.of(ItemUtil.nextPage, e
          -> contents.getHost().open(p, pagination.next().getPage()))
      );
    }

    if (!pagination.isFirst()) {
      contents.set(2, 0, ClickableItem.of(ItemUtil.previosPage, e
          -> contents.getHost().open(p, pagination.previous().getPage()))
      );
    }

    pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));

  }


  private ItemType getWorldMat(final World w) {
    return switch (w.getEnvironment()) {
      case NORMAL -> ItemType.GRASS_BLOCK;
      case NETHER -> ItemType.NETHERRACK;
      case THE_END -> ItemType.END_STONE;
      default -> ItemType.WHITE_GLAZED_TERRACOTTA;
    };
  }


}

/*

public class WorldManagerCmd implements OCommand {

    public static final List<String> commands = Arrays.asList("list", "tp", "create",
            "load", "import", "save", "unload", "setspawn", "delete", "backup", "restore");

    private static final String op = "operation", world = "world",
            env = "environment", gen = "generator";

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("wm").executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player pl) || !ApiOstrov.isLocalBuilder(cs)) {
                        cs.sendMessage("§cНевыполнимая комманда!");
                        return 0;
                    }

                    SmartInventory.builder()
                            .id("Worlds" + pl.getName())
                            .provider(new WorldSetupMenu())
                            .size(6, 9)
                            .title("§2Миры сервера")
                            .build().open(pl);
                    return Command.SINGLE_SUCCESS;
                })

                .then(Resolver.string(op).suggests( (cntx, sb) -> {
                    if (!ApiOstrov.isLocalBuilder(cntx.getSource().getSender())) {
                        return sb.buildFuture();
                    }
                    commands.forEach(c -> sb.suggest(c));
                    return sb.buildFuture();
                })
                    .then(Resolver.string(world).suggests((cntx, sb) -> {
                        if (!ApiOstrov.isLocalBuilder(cntx.getSource().getSender())) {
                            return sb.buildFuture();
                        }

                        Bukkit.getWorlds().forEach(w -> sb.suggest(w.getName()));
                        return sb.buildFuture();
                    }).executes(tryWMCmd()).then(Resolver.string(env).suggests((cntx, sb) -> {
                        if (!ApiOstrov.isLocalBuilder(cntx.getSource().getSender())) {
                            return sb.buildFuture();
                        }

                        switch (Resolver.string(cntx, op)) {
                            case "create", "load", "import":
                                for (final World.Environment en : World.Environment.values()) {
                                    sb.suggest(en.name());
                                }
                        }
                        return sb.buildFuture();
                    })
                        .then(Resolver.string(gen).suggests((cntx, sb) -> {
                            if (!ApiOstrov.isLocalBuilder(cntx.getSource().getSender())) {
                                return sb.buildFuture();
                            }

                            switch (Resolver.string(cntx, op)) {
                                case "create", "load", "import":
                                    for (final Generator gn : Generator.values()) {
                                        sb.suggest(gn.name());
                                    }
                            }
                            return sb.buildFuture();
                        }).executes(tryWMCmd())))))
                .build();
    }

    private static Command<CommandSourceStack> tryWMCmd() {
        return cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!ApiOstrov.isLocalBuilder(cs)) {
                cs.sendMessage("§cНедостаточно прав!");
                return 0;
            }

            final World w;
            final String wnm = Resolver.string(cntx, world);
            return switch (Resolver.string(cntx, op)) {
                case "list" -> {
                    cs.sendMessage("");
                    cs.sendMessage(Ostrov.PREFIX + "Загружено миров: §5" + Bukkit.getWorlds().size());
                    for (final World ow : Bukkit.getWorlds()) {
                        final ChunkGenerator cg = ow.getGenerator();
                        final String wgn = cg == null ? null : cg.getClass().getName();
                        cs.sendMessage(TCUtil.form(
                                        "§b- §e" + ow.getName() +
                                                " §7 (" + ow.getEnvironment().name() +
                                                ", " + (wgn == null ? "null" :
                                                (wgn.contains(".") ? wgn.substring(wgn.lastIndexOf(".") + 1) : wgn)) +
                                                ", " + ow.getDifficulty().name() + ") §8>ТП< ")
                                .hoverEvent(HoverEvent.showText(TCUtil.form("§7Чанков загружено: §6" + ow.getLoadedChunks().length +
                                        "§7, Игроки: §6" + ow.getPlayers().size() +
                                        "§7, ПВП: " + (ow.getPVP() ? "§4Да" : "§2Нет") +
                                        "§7, Энтити: §6" + ow.getEntities().size())))
                                .clickEvent(ClickEvent.runCommand("/wm tp " + ow.getName())));
                    }
                    cs.sendMessage("");
                    yield Command.SINGLE_SUCCESS;
                }
                case "tp" -> {
                    w = Bukkit.getWorld(wnm);//Bukkit.getWorld(nm == null ? arg[1] : nm);
                    if (w == null) {
                        cs.sendMessage(Ostrov.PREFIX + "§cМир " + wnm + " не найден!");
                        yield 0;
                    }

                    if (cs instanceof final Player pl) {
                        pl.teleport(w.getSpawnLocation());
                        pl.sendMessage(Ostrov.PREFIX + "Ты теперь в мире §2" + wnm);
                        yield Command.SINGLE_SUCCESS;
                    }
                    cs.sendMessage("§eНе консольная команда!");
                    yield 0;
                }
                case "create" -> onEnvGen(cntx, (en, gn) -> {
                    final World nw = WorldManager.create(cs, wnm, en, gn, cs instanceof Player);
                    if (nw == null) {
                        cs.sendMessage(Ostrov.PREFIX + "Мир " + wnm + " не был создан... ");
                    }
                    return Command.SINGLE_SUCCESS;
                });
                case "load", "import" -> onEnvGen(cntx, (en, gn) -> {
                    final World nw = WorldManager.load(cs, wnm, en, gn);
                    if (nw == null) {
                        cs.sendMessage(Ostrov.PREFIX + "Мир " + wnm + " не был загружен... ");
                    }
                    return Command.SINGLE_SUCCESS;
                });
                case "save" -> {
                    w = Bukkit.getWorld(wnm);//Bukkit.getWorld(nm == null ? arg[1] : nm);
                    if (w == null) {
                        yield 0;
                    }
                    w.save();
                    yield Command.SINGLE_SUCCESS;
                }
                case "unload" -> {
                    w = Bukkit.getWorld(wnm);//Bukkit.getWorld(nm == null ? arg[1] : nm);
                    if (w == null) {
                        cs.sendMessage(Ostrov.PREFIX + "§cМир " + wnm + " не найден!");
                        yield 0;
                    }
                    if (!w.getPlayers().isEmpty()) {
                        cs.sendMessage(Ostrov.PREFIX + "Все игроки должны покинуть мир перед удалением!");
                        w.getPlayers().forEach(op -> {
                            cs.sendMessage(Ostrov.PREFIX + "- " + op.getName());
                        });
                        yield 0;
                    }
                    Bukkit.unloadWorld(world, true);
                    cs.sendMessage(Ostrov.PREFIX + " мир " + wnm + " выгружен!");
                    yield Command.SINGLE_SUCCESS;
                }
                case "setspawn" -> {
                    if (cs instanceof final Player pl) {
                        pl.getWorld().setSpawnLocation(pl.getLocation());
                        cs.sendMessage(Ostrov.PREFIX + " §aточка спавна мира установлена под ногами!");
                        yield Command.SINGLE_SUCCESS;
                    }
                    cs.sendMessage("§eНе консольная команда!");
                    yield 0;
                }
                case "delete" -> {
                    if (cs instanceof final Player pl) {
                        PlayerInput.get(InputButton.InputType.ANVILL, pl, s -> {
                            pl.closeInventory();
                            if (s.equalsIgnoreCase("xpanitely")) {
                                WorldManager.delete(cs, wnm);
                            } else {
                                if (Bukkit.getWorld(wnm) != null) {
                                    pl.sendMessage(TCUtil.form("§2> §a§lКлик - ВЫГРУЗИТЬ мир без удаления файлов §2<")
                                            .clickEvent(ClickEvent.runCommand("wm unload " + wnm))
                                    );
                                }
                            }
                        }, "");
                        yield Command.SINGLE_SUCCESS;
                    }
                    WorldManager.delete(cs, wnm);
                    yield Command.SINGLE_SUCCESS;
                }
                case "backup" -> {
                    w = Bukkit.getWorld(wnm);//Bukkit.getWorld(nm == null ? arg[1] : nm);
                    if (w == null) {
                        cs.sendMessage(Ostrov.PREFIX + "§cМир " + wnm + " не найден!");
                        yield 0;
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            cs.sendMessage(Ostrov.PREFIX + "Создаём резервную копию мира " + wnm + "..");
                            final long currentTimeMillis = System.currentTimeMillis();
                            copyFile(w.getWorldFolder(), new File(Ostrov.instance.getDataFolder() + "/backup-", wnm));
                            cs.sendMessage(Ostrov.PREFIX + "§aРезервная копия создана §7за §5" + (System.currentTimeMillis() - currentTimeMillis) + "ms!");
                        }
                    }.runTaskAsynchronously(Ostrov.instance);
                    yield Command.SINGLE_SUCCESS;
                }
                case "restore" -> {
                    w = Bukkit.getWorld(wnm);//Bukkit.getWorld(nm == null ? arg[1] : nm);
                    if (w == null) {
                        cs.sendMessage(Ostrov.PREFIX + "§cМир " + wnm + " не найден!");
                        yield 0;
                    }

                    if (!w.getPlayers().isEmpty()) {
                        cs.sendMessage(Ostrov.PREFIX + "В мире не должно быть игроков!");
                        w.getPlayers().forEach((p1) -> {
                            cs.sendMessage(Ostrov.PREFIX + "- " + p1.getName());
                        });
                        yield 0;
                    }

                    final File wfile = new File(Ostrov.instance + "/backup-", wnm);
                    if (!wfile.exists()) {
                        cs.sendMessage(Ostrov.PREFIX + "Копии этого мира не найдено!");
                        yield 0;
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            cs.sendMessage(Ostrov.PREFIX + "Восстановление мира " + wnm + " из резервной копии... ");
                            final long currentTimeMillis = System.currentTimeMillis();
                            final File worldFolder = w.getWorldFolder();
                            final World.Environment environment = w.getEnvironment();
                            Bukkit.unloadWorld(world, false); //тут не надо сохранять - на подмену!
                            deleteFile(worldFolder);
                            copyFile(wfile, worldFolder);
                            Ostrov.sync(() -> {
                                final World nw = Bukkit.createWorld(new WorldCreator(wfile.getName()).environment(environment));
                                if (nw == null) {
                                    cs.sendMessage(Ostrov.PREFIX + "Мир " + wfile.getName() + " не был восстановлен... ");
                                    return;
                                }
                                cs.sendMessage(Ostrov.PREFIX + "Мир §b" + wfile.getName() + "§aвосстановлен из копии за §5" + (System.currentTimeMillis() - currentTimeMillis) + "ms!");
                            });
                        }
                    }.runTaskAsynchronously(Ostrov.instance);
                    yield Command.SINGLE_SUCCESS;
                }
                default -> {
                    help(cs);
                    yield Command.SINGLE_SUCCESS;
                }
            };
        };
    }

    private static int onEnvGen(final CommandContext<CommandSourceStack> cntx, final EnvGen eg) throws CommandSyntaxException {
        World.Environment en = null;
        try {
            final String ennm = Resolver.string(cntx, env);
            for (final World.Environment env : World.Environment.values()) {
                if (env.name().equalsIgnoreCase(ennm)) {
                    en = env;
                    break;
                }
            }
            if (en == null) {
                cntx.getSource().getSender().sendMessage("§cПровайдеры: §e" + WorldManager.possibleEnvironment());
                return 0;
            }
        } catch (CommandSyntaxException e) {
            en = World.Environment.NORMAL;
        }
        Generator gn = null;
        try {
            final String gnnm = Resolver.string(cntx, gen);
            for (final Generator gen : Generator.values()) {
                if (gen.toString().equalsIgnoreCase(gnnm)) {
                    gn = gen;
                    break;
                }
            }
            if (gn == null) {
                cntx.getSource().getSender().sendMessage("§cГенераторы: §e" + WorldManager.possibleGenerator());
                return 0;
            }
        } catch (CommandSyntaxException e) {
            en = World.Environment.NORMAL;
        }

        return eg.run(en, gn);
    }

    private interface EnvGen {
        int run(final World.Environment en, final Generator gn) throws CommandSyntaxException;
    }

    @Override
    public List<String> aliases() {
        return List.of("мм");
    }

    @Override
    public String description() {
        return "Настройки миров";
    }

    private static void help(final CommandSender sender) {
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

    public static void copyFile(final File source, final File destination) {
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

            if (source.getName().contains("level.dat_old") ||
                    source.getName().contains("session.lock") ||
                    source.getName().contains("uid.dat")
            ) return; //пропускаем ненужные

            try {
                Files.copy(source, destination);
            } catch (IOException ex) {
            }
        }
    }

    public static void deleteFile(final File file) {
        if (file.exists()) {
            File[] listFiles;
            for (int length = (listFiles = file.listFiles()).length, i = 0; i < length; ++i) {
                final File file2 = listFiles[i];
                if (file2.isDirectory()) {
                    deleteFile(file2);
                } else {
                    file2.delete();
                }
            }
        }
        file.delete();
    }
}

class WorldSelectMenu implements InventoryProvider {

    private static final ItemStack fill = new ItemBuilder(ItemType.BLACK_STAINED_GLASS_PANE).name("§8.").build();

    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //contents.fillRect(0,0,  2,8, ClickableItem.empty(fill));

        final Oplayer op = PM.getOplayer(p);

        final Pagination pagination = contents.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

        contents.fillRect(0, 0, 2, 8, ClickableItem.empty(fill));

        for (final World world : Bukkit.getWorlds()) {

            menuEntry.add(ClickableItem.of(new ItemBuilder(getWorldMat(world))
                    .name(world.getName())
                    .lore(op.world_positions.containsKey(world.getName()) ? "§7ЛКМ - ТП на точку выхода" : "")
                    .lore("§7ПКМ - ТП на точку спавна мира")
                    .lore("")
                    .build(), e -> {
                if (e.isLeftClick() && op.world_positions.containsKey(world.getName())) {
                    final Location exit = LocUtil.stringToLoc(op.world_positions.get(world.getName()), false, false);
                    ApiOstrov.teleportSave(p, exit, true);//p.teleport( world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                } else {
                    ApiOstrov.teleportSave(p, world.getSpawnLocation(), true);//p.teleport( world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }));
        }

        pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
        pagination.setItemsPerPage(9);

        contents.set(2, 4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e ->
                p.closeInventory()
        ));


        if (!pagination.isLast()) {
            contents.set(2, 8, ClickableItem.of(ItemUtil.nextPage, e
                    -> contents.getHost().open(p, pagination.next().getPage()))
            );
        }

        if (!pagination.isFirst()) {
            contents.set(2, 0, ClickableItem.of(ItemUtil.previosPage, e
                    -> contents.getHost().open(p, pagination.previous().getPage()))
            );
        }

        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));

    }

    private ItemType getWorldMat(final World w) {
        return switch (w.getEnvironment()) {
            case NORMAL -> ItemType.GRASS_BLOCK;
            case NETHER -> ItemType.NETHERRACK;
            case THE_END -> ItemType.END_STONE;
            default -> ItemType.WHITE_GLAZED_TERRACOTTA;
        };
    }
}
 */