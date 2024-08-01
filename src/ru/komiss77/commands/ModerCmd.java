package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.builder.menu.ModerInv;
import ru.komiss77.utils.inventory.SmartInventory;

import java.util.List;

public class ModerCmd implements OCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("moder").executes(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof final Player pl)) {
                cs.sendMessage("§eНе консольная команда!");
                return 0;
            }

            if (!ApiOstrov.isStaff(pl)) {
                pl.sendMessage("§cДоступно только персоналу!");
                return 0;
            }

            SmartInventory.builder()
                .id("Moder " + cs.getName())
                .provider(new ModerInv())
                .size(3, 9)
                .title("§aМеню Модера")
                .build().open((Player) cs);
            return Command.SINGLE_SUCCESS;
        }).build();
    }

    @Override
    public List<String> aliases() {
        return List.of("модер");
    }

    @Override
    public String description() {
        return "Меню модератора";
    }
}
