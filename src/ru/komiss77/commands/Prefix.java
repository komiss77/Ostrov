package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.args.Resolver;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.TCUtils;

import java.util.List;


public class Prefix implements OCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String prefix = "prefix";
        return Commands.literal("prefix")
                .then(Resolver.string(prefix)
                        .executes(cntx -> {
                            final CommandSender cs = cntx.getSource().getExecutor();
                            if (!(cs instanceof final Player pl)) {
                                cs.sendMessage("§eНе консольная команда!");
                                return 0;
                            }

                            final Oplayer op = PM.getOplayer(pl);

                            if (!pl.hasPermission("ostrov.prefix")) {
                                pl.sendMessage("§6Нужно право ostrov.prefix!");
                                return 0;
                            }

                            final String pr = Resolver.string(cntx, prefix).replace("&k", "").replace("&", "§");
                            if (TCUtils.strip(pr).length() > 8) {
                                pl.sendMessage(TCUtils.form(Ostrov.PREFIX + "<red>Префикс не может превышать 8 символов (цвета не учитываются)."));
                                return 0;
                            }

                            op.setData(Data.PREFIX, pr);
                            pl.sendMessage(TCUtils.form(Ostrov.PREFIX + "Твой новый префикс: " + pr));
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("префикс");
    }

    @Override
    public String description() {
        return "Ставит новый префикс";
    }
}
