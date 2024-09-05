package ru.komiss77.commands;

import java.util.ArrayList;
import java.util.List;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.*;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.enums.Module;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Perm;


public class OreloadCmd implements OCommand {

    //запрос банжи, если есть - разкодировать raw
    //если пустой - выкачать из снапшота БД

    private static final List<String> subCommands;

    static {
        subCommands = new ArrayList<>();
        subCommands.add("all");
        subCommands.add("gamemanager");
        subCommands.add("signs");
        subCommands.add("group");
        subCommands.add("connection_ostrov");
        subCommands.add("connection_local");
        //Arrays.asList("all", "group", "connection_ostrov");
        for (final Module m : Module.values()) {
            subCommands.add(m.name());
        }
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String mod = "module";
        return Commands.literal("oreload")
                .then(Resolver.string(mod).suggests((cntx, sb) -> {
                    if (!ApiOstrov.isLocalBuilder(cntx.getSource().getExecutor())) {
                        return sb.buildFuture();
                    }
                    subCommands.forEach(sc -> sb.suggest(sc));
                    return sb.buildFuture();
                }).executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getExecutor();
                    if (!ApiOstrov.isLocalBuilder(cs, false)) {
                        cs.sendMessage("§cДоступно только персоналу!");
                        return 0;
                    }
                    final String md = Resolver.string(cntx, mod);
                    final Player p = (cs instanceof Player) ? (Player) cs : null;
                    return switch (md) {
                        case "all" -> {
                            Cfg.ReLoadAllConfig();
                            Ostrov.modules.entrySet().forEach(es -> {
                                es.getValue().reload();
                                if (p != null) {
                                    p.sendMessage("§freload modeule : §a" + es.getKey());
                                }
                            });
                            yield Command.SINGLE_SUCCESS;
                        }
                        case "connection_ostrov" -> {
                            RemoteDB.init(false, true);
                            if (p != null) p.sendMessage("§freload modeule : §a" + md);
                            yield Command.SINGLE_SUCCESS;
                        }
                        case "connection_local" -> {
                            Ostrov.async(() -> {
                                LocalDB.init();
                                if (p != null) p.sendMessage("§freload modeule : §a" + md);
                            }, 0);
                            yield Command.SINGLE_SUCCESS;//!!!! релоад локал - делать асинх
                        }
                        case "group" -> {
                            Ostrov.async(() -> {
                                //RemoteDB.getBungeeServerInfo(); //1!!!
                                Perm.loadGroups(true); //2!!! сначала прогрузить allBungeeServersName, или не определяет пермы по серверам
                                if (p != null) p.sendMessage("§freload modeule : §a" + md);
                            }, 0);
                            yield Command.SINGLE_SUCCESS;
                        }
                        //Perm.loadGroups(true);
                        case "gamemanager" -> {
                            Ostrov.async(() -> {
                                GM.load(GM.State.RELOAD);
                                if (p != null) p.sendMessage("§freload modeule : §a" + md);
                            }, 0);
                            yield Command.SINGLE_SUCCESS;//GM.reload = true;
                        }
                        case "signs" -> {
                            GM.onWorldsLoadDone();
                            if (p != null) p.sendMessage("§freload modeule : §a" + md);
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
    
    
 
