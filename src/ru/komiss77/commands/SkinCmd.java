package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.hook.SkinRestorerHook;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;

import java.util.List;


public class SkinCmd implements OCommand {

    //запрос банжи, если есть - разкодировать raw
    //если пустой - выкачать из снапшота БД

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("skin")
            .executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player p)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                final Oplayer op = PM.getOplayer(p);

                if (op.isGuest) {
                    p.sendMessage("§6Гости не могут менять скин!");
                    return 0;
                }

                SkinRestorerHook.openGui(p, 0);
                return Command.SINGLE_SUCCESS;
            }).build();
    }

    @Override
    public List<String> aliases() {
        return List.of("скин");
    }

    @Override
    public String description() {
        return "Смена скина";
    }
}
