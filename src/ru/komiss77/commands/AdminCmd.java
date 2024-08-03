package ru.komiss77.commands;

import java.util.List;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.builder.menu.AdminInv;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.inventory.SmartInventory;


public class AdminCmd implements OCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("admin")
            .executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player pl)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                final Oplayer op = PM.getOplayer(pl);
                if (op.hasGroup("xpanitely") || op.hasGroup("owner")) {
                    SmartInventory.builder().id("Admin " + cs.getName())
                        .provider(new AdminInv()).size(3, 9)
                        .title("§dМеню Абьюзера").build().open(pl);
                    return Command.SINGLE_SUCCESS;
                }

                cs.sendMessage("§cУ вас нету разрешения на это!");
                return 0;
            })
            .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("админ");
    }

    @Override
    public String description() {
        return "Открывает меню Абьюзера";
    }
}
