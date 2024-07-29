package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.args.Resolver;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.protocols.Protocol77;

import java.util.List;

public class ProtocolCmd implements OCommand {

    //запрос банжи, если есть - разкодировать raw
    //если пустой - выкачать из снапшота БД

    public static final String grp = "xpanitely";

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String val = "value";
        return Commands.literal("protocol")
                .then(Resolver.integer(val).suggests((cntx, sb) -> {
                            if (!(cntx.getSource().getExecutor() instanceof final Player pl)
                                    || !PM.getOplayer(pl).hasGroup(grp)) {
                                return sb.buildFuture();
                            }
                            sb.suggest(77);
                            return sb.buildFuture();
                        })
                        .executes(cntx -> {
                            final CommandSender cs = cntx.getSource().getExecutor();
                            if (!(cs instanceof final Player pl)) {
                                cs.sendMessage("§eНе консольная команда!");
                                return 0;
                            }

                            final Oplayer op = PM.getOplayer(pl);

                            if (!op.hasGroup(grp)) {
                                pl.sendMessage("§cДоступно только персоналу!");
                                return 0;
                            }
                            return switch (Resolver.integer(cntx, val)) {
                                case 77 -> {
                                    if (Protocol77.active) {
                                        pl.sendMessage(Ostrov.PREFIX + "§cПротокол уже активен!");
                                        yield 0;
                                    }
                                    new Protocol77(pl);
                                    yield Command.SINGLE_SUCCESS;
                                }
                                default -> 0;
                            };
                        }))
                .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("протокол");
    }

    @Override
    public String description() {
        return "Вызывает протокол";
    }

}
