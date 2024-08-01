package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.args.Resolver;
import ru.komiss77.modules.warp.WarpManager;
import ru.komiss77.modules.warp.WarpMenu;
import ru.komiss77.utils.inventory.SmartInventory;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public class WarpCmd implements OCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String warp = "warp", player = "player";
        return Commands.literal("warp").executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player pl)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }
                openMenu(pl);
                return Command.SINGLE_SUCCESS;
            })
            .then(Resolver.string(warp).suggests((cntx, sb) -> {
                    WarpManager.getWarpNames().forEach(s -> sb.suggest(s));
                    return sb.buildFuture();
                })
                .executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player pl)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }

                    WarpManager.tryWarp(pl, Resolver.string(cntx, warp));
                    return Command.SINGLE_SUCCESS;
                }).then(Resolver.player(player).executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final ConsoleCommandSender cn)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }

                    final Player tgt = Resolver.player(cntx, player);
                    if (tgt == null) {
                        cn.sendMessage(Ostrov.PREFIX + "§cИгрок не онлайн!");
                        return 0;
                    }

                    final String wp = Resolver.string(cntx, warp);
                    cn.sendMessage("§6Перемещаем " + tgt.getName() + " на " + wp + "...");
                    WarpManager.tryWarp(tgt, wp);
                    return Command.SINGLE_SUCCESS;
                })))
            .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("варп");
    }

    @Override
    public String description() {
        return "Варп комманда";
    }

    public static void openMenu(final Player p) {
        SmartInventory.builder()
            .id("WarpMenu" + p.getName())
            .provider(new WarpMenu())
            .size(6, 9)
            .title("§fМеста")
            .build()
            .open(p);
    }
}
