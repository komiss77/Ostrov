package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.args.Resolver;
import ru.komiss77.enums.Operation;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;

import java.util.List;


public class SeenCmd implements OCommand {

    //запрос банжи, если есть - разкодировать raw
    //если пустой - выкачать из снапшота БД

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String player = "player";
        return Commands.literal("seen")
            .then(Resolver.player(player)
                .executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player pl)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }

                    final Oplayer op = PM.getOplayer(pl);

                    if (!op.isStaff) {
                        pl.sendMessage("§cДоступно только персоналу!");
                        return 0;
                    }

                    final Player tgt = Resolver.player(cntx, player);
                    if (tgt == null) {
                        pl.sendMessage(Ostrov.PREFIX + "§cИгрок не онлайн!");
                        return 0;
                    }

                    SpigotChanellMsg.sendMessage(pl, Operation.REQUEST_PLAYER_DATA, pl.getName(), tgt.getName());
                    return Command.SINGLE_SUCCESS;
                }))
            .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("инфо");
    }

    @Override
    public String description() {
        return "Показывает информацию игрока";
    }


    public static void onResult(final Player sender, final int status, final String raw) {
        if (status == 1) {
            sender.sendMessage("получен массив для : " + raw);
        } else {
            sender.sendMessage(" оффлайн, выкачать из снапшота БД - недоделано");
        }
    }


}
