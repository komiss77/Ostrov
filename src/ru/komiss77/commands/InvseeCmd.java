package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.args.Resolver;

import java.util.List;

public class InvseeCmd implements OCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String player = "target", type = "type";
        return Commands.literal("invsee")
            .then(Resolver.player(player)
                .executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getExecutor();
                    if (!(cs instanceof final Player pl)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }

                    final Player tgt = Resolver.player(cntx, player);
                    if (tgt == null) {
                        pl.sendMessage(Ostrov.PREFIX + "§cИгрок не онлайн!");
                        return 0;
                    }

                    pl.openInventory(tgt.getInventory());
                    tgt.sendMessage(Ostrov.PREFIX + pl.getName() + " §aпросматривает твой инвентарь!");
                    return Command.SINGLE_SUCCESS;
                }).then(Resolver.string(type)
                    .executes(cntx -> {
                        final CommandSender cs = cntx.getSource().getExecutor();
                        if (!(cs instanceof final Player pl)) {
                            cs.sendMessage("§eНе консольная команда!");
                            return 0;
                        }

                        final Player tgt = Resolver.player(cntx, player);
                        if (tgt == null) {
                            pl.sendMessage(Ostrov.PREFIX + "§cИгрок не онлайн!");
                            return 0;
                        }

                        final Inventory inv;
                        switch (Resolver.string(cntx, type)) {
                            case "main":
                                inv = tgt.getInventory();
                                break;
                            case "ender":
                                inv = tgt.getEnderChest();
                                break;
                            case "extra":
                            default:
                                pl.sendMessage(Ostrov.PREFIX + "§cНеправильный синтакс комманды!");
                                return 0;
                        }

                        pl.openInventory(inv);
                        tgt.sendMessage(Ostrov.PREFIX + pl.getName() + " §aпросматривает твой инвентарь!");
                        return Command.SINGLE_SUCCESS;
                    })))
            .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("инвентарь");
    }

    @Override
    public String description() {
        return "Просмотр инвентаря";
    }
}
