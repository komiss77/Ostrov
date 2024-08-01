package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import ru.komiss77.*;
import ru.komiss77.commands.args.Resolver;
import ru.komiss77.enums.Module;
import ru.komiss77.modules.games.GM;

import java.util.ArrayList;
import java.util.List;


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
                final CommandSender cs = cntx.getSource().getSender();
                if (!ApiOstrov.isLocalBuilder(cs, true)) {
                    cs.sendMessage("§cДоступно только персоналу!");
                    return 0;
                }
                final String md = Resolver.string(cntx, mod);
                return switch (md) {
                    case "all" -> {
                        Config.ReLoadAllConfig();
                        Ostrov.getModules().forEach(m -> m.reload());
                        yield Command.SINGLE_SUCCESS;
                    }
                    case "connection_ostrov" -> {
                        OstrovDB.init(false, true);
                        yield Command.SINGLE_SUCCESS;
                    }
                    case "connection_local" -> {
                        Ostrov.async(() -> LocalDB.init(), 0);
                        yield Command.SINGLE_SUCCESS;//!!!! релоад локал - делать асинх
                    }
                    case "group" -> {
                        Ostrov.async(() -> {
                            //OstrovDB.getBungeeServerInfo(); //1!!!
                            Perm.loadGroups(true); //2!!! сначала прогрузить allBungeeServersName, или не определяет пермы по серверам
                        }, 0);
                        yield Command.SINGLE_SUCCESS;
                    }
                    //Perm.loadGroups(true);
                    case "gamemanager" -> {
                        Ostrov.async(() -> GM.load(GM.State.RELOAD), 0);
                        yield Command.SINGLE_SUCCESS;//GM.reload = true;
                    }
                    case "signs" -> {
                        GM.onWorldsLoadDone();
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
                        ApiOstrov.getModule(module).reload();
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
    
    
 
