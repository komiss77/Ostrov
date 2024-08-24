package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.Section;

import java.util.List;

@Deprecated
public class ProfileCmd implements OCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("profile")
            .executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player pl)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                final Oplayer op = PM.getOplayer(pl);
                if (op.menu == null) {
                    pl.sendMessage("§eПодождите, данные ещё не получены..");
                    return 0;
                }

                op.menu.open(pl, Section.ПРОФИЛЬ);
                pl.playSound(pl.getLocation(), Sound.BLOCK_COMPOSTER_EMPTY, 2, 2);
                return Command.SINGLE_SUCCESS;
            })
            .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("профиль");
    }

    @Override
    public String description() {
        return "Открывает Профиль";
    }
}
