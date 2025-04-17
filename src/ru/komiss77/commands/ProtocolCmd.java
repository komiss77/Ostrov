package ru.komiss77.commands;

import java.util.Set;
import com.mojang.brigadier.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.tools.OCmdBuilder;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.modules.protocols.Protocol10;
import ru.komiss77.modules.protocols.Protocol43;
import ru.komiss77.modules.protocols.Protocol77;

public class ProtocolCmd {

    //запрос банжи, если есть - разкодировать raw
    //если пустой - выкачать из снапшота БД

    public ProtocolCmd() { //новое
        final String val = "value";
        new OCmdBuilder("protocol").then(Resolver.integer(val)).suggest(cntx -> {
            if (!(cntx.getSource().getSender() instanceof final Player pl)
                || !Perm.isStaff(PM.getOplayer(pl), 2)) return Set.of();
            return Set.of("10", "43", "77");
        }, true).run(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof final Player pl)) {
                cs.sendMessage("§eНе консольная команда!");
                return 0;
            }

            final Oplayer op = PM.getOplayer(pl);
            if (!Perm.isStaff(op, 2)) {
                pl.sendMessage("§cДоступно только персоналу!");
                return 0;
            }

            return switch (Resolver.integer(cntx, val)) {
                case 77:
                    if (Protocol77.active) {
                        pl.sendMessage(Ostrov.PREFIX + "§cПротокол уже активен!");
                        yield 0;
                    }
                    new Protocol77(pl);
                    yield Command.SINGLE_SUCCESS;
                case 43:
                    if (Protocol10.active) {
                        pl.sendMessage(Ostrov.PREFIX + "§cПротокол уже активен!");
                        yield 0;
                    }
                    new Protocol43(pl);
                    yield Command.SINGLE_SUCCESS;
                case 10:
                    if (Protocol10.active) {
                        pl.sendMessage(Ostrov.PREFIX + "§cПротокол уже активен!");
                        yield 0;
                    }
                    new Protocol10(pl);
                    yield Command.SINGLE_SUCCESS;
                default: yield 0;
            };
        }).description("Протокол").register();
    }
}
