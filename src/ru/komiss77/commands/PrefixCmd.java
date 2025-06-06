package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.tools.OCmdBuilder;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.TCUtil;


public class PrefixCmd {

    public PrefixCmd() { //новое
        final String prefix = "prefix";
        new OCmdBuilder("prefix", "/prefix [префикс]")
            .then(Resolver.greedy(prefix)).run(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
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
                if (TCUtil.strip(pr).length() > 8) {
                    pl.sendMessage(TCUtil.form(Ostrov.PREFIX + "<red>Префикс не может превышать 8 символов (цвета не учитываются)."));
                    return 0;
                }

                op.setData(Data.PREFIX, pr);
                pl.sendMessage(TCUtil.form(Ostrov.PREFIX + "Твой новый префикс: " + pr));
                return Command.SINGLE_SUCCESS;
            })
            .description("Ставит префикс")
            .register(Ostrov.mgr);
    }
/*
    @Override //старое
    public LiteralCommandNode<CommandSourceStack> command() {
        final String prefix = "prefix";
        return Commands.literal("prefix")
                .then(Resolver.string(prefix)
                        .executes(cntx -> {
                            final CommandSender cs = cntx.getSource().getSender();
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
                            if (TCUtil.strip(pr).length() > 8) {
                                pl.sendMessage(TCUtil.form(Ostrov.PREFIX + "<red>Префикс не может превышать 8 символов (цвета не учитываются)."));
                                return 0;
                            }

                            op.setData(Data.PREFIX, pr);
                            pl.sendMessage(TCUtil.form(Ostrov.PREFIX + "Твой новый префикс: " + pr));
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }

    @Override
    public Set<String> aliases() {
        return Set.of("префикс");
    }

    @Override
    public String description() {
        return "Ставит префикс";
    }*/
}