package ru.komiss77.commands;

import java.util.ArrayList;
import java.util.List;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import ru.komiss77.*;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.enums.Module;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Perm;


public class OreloadCmd implements OCommand {

    //запрос банжи, если есть - разкодировать raw
    //если пустой - выкачать из снапшота БД

  private static final List<String> subCmd;

    static {
      subCmd = new ArrayList<>();
      subCmd.add("all");
      subCmd.add("gamemanager");
      subCmd.add("signs");
      subCmd.add("group");
      subCmd.add("connection_ostrov");
      subCmd.add("connection_local");
        //Arrays.asList("all", "group", "connection_ostrov");
        for (final Module m : Module.values()) {
          subCmd.add(m.name());
        }
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String mod = "module";
        return Commands.literal("oreload")
                .then(Resolver.string(mod).suggests((cntx, sb) -> {
                  if (!ApiOstrov.isLocalBuilder(cntx.getSource().getSender())) {
                        return sb.buildFuture();
                    }
                  subCmd.stream()
                      .filter(c -> c.startsWith(sb.getRemaining()))
                      .forEach(c -> sb.suggest(c));
                    return sb.buildFuture();
                }).executes(cntx -> {
                  final CommandSender cs = cntx.getSource().getSender();
                    if (!ApiOstrov.isLocalBuilder(cs, false)) {
                        cs.sendMessage("§cДоступно только персоналу!");
                        return 0;
                    }
                    final String md = Resolver.string(cntx, mod);
                  //final Player p = (cs instanceof Player) ? (Player) cs : null;
                    return switch (md) {
                        case "all" -> {
                            Cfg.ReLoadAllConfig();
                            Ostrov.modules.forEach((key, value) -> {
                                value.reload();
                                cs.sendMessage("§freload modeule : §a" + key);
                            });
                            yield Command.SINGLE_SUCCESS;
                        }
                        case "connection_ostrov" -> {
                            RemoteDB.init(false, true);
                          cs.sendMessage("§freload modeule : §a" + md);
                            yield Command.SINGLE_SUCCESS;
                        }
                        case "connection_local" -> {
                            Ostrov.async(() -> {
                                LocalDB.init();
                              cs.sendMessage("§freload modeule : §a" + md);
                            }, 0);
                            yield Command.SINGLE_SUCCESS;//!!!! релоад локал - делать асинх
                        }
                        case "group" -> {
                            Ostrov.async(() -> {
                                //RemoteDB.getBungeeServerInfo(); //1!!!
                                Perm.loadGroups(true); //2!!! сначала прогрузить allBungeeServersName, или не определяет пермы по серверам
                              cs.sendMessage("§freload modeule : §a" + md);
                            }, 0);
                            yield Command.SINGLE_SUCCESS;
                        }
                        //Perm.loadGroups(true);
                        case "gamemanager" -> {
                            Ostrov.async(() -> {
                                GM.load(GM.State.RELOAD);
                              cs.sendMessage("§freload modeule : §a" + md);
                            }, 0);
                            yield Command.SINGLE_SUCCESS;//GM.reload = true;
                        }
                        case "signs" -> {
                            GM.onWorldsLoadDone();
                          cs.sendMessage("§freload modeule : §a" + md);
                            yield Command.SINGLE_SUCCESS;
                        }
                        default -> {
                            Module module = null;
                            for (final Module m : Module.values()) {
                                if (m.name().equalsIgnoreCase(md)) {
                                    module = m;
                                    break;
                                }
                            }
                            if (module == null) {
                                cs.sendMessage("§cТакой модуль не найден!");
                                yield 0;
                            }
                            Ostrov.getModule(module).reload();
                            cs.sendMessage("§aМодуль §f" + md + " §aперезагружен!");
                            yield Command.SINGLE_SUCCESS;
                        }
                    };
                }))
                .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("орелоад");
    }

    @Override
    public String description() {
        return "Перезагрузка модулей";
    }
}
    
    
 
