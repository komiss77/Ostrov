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


public class SuffixCmd {

  public SuffixCmd() { //новое
    final String suffix = "suffix";
    new OCmdBuilder("suffix", "/suffix [суффикс]")
        .then(Resolver.greedy(suffix)).run(cntx -> {
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

          final String sf = Resolver.string(cntx, suffix).replace("&k", "").replace("&", "§");
          if (TCUtil.strip(sf).length() > 8) {
            pl.sendMessage(TCUtil.form(Ostrov.PREFIX + "<red>Суффикс не может превышать 8 символов (цвета не учитываются)."));
            return 0;
          }

          op.setData(Data.SUFFIX, sf);
          pl.sendMessage(TCUtil.form(Ostrov.PREFIX + "Твой новый суффикс: " + sf));
          return Command.SINGLE_SUCCESS;
        })
        .description("Ставит суффикс")
        //.aliases("")
        .register();
  }
/*
    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String suffix = "suffix";
        return Commands.literal("suffix")
            .then(Resolver.string(suffix)
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

                    final String sf = Resolver.string(cntx, suffix).replace("&k", "").replace("&", "§");
                    if (TCUtil.strip(sf).length() > 8) {
                        pl.sendMessage(TCUtil.form(Ostrov.PREFIX + "<red>Суффикс не может превышать 8 символов (цвета не учитываются)."));
                        return 0;
                    }

                    op.setData(Data.SUFFIX, sf);
                    pl.sendMessage(TCUtil.form(Ostrov.PREFIX + "Твой новый суффикс: " + sf));
                    return Command.SINGLE_SUCCESS;
                }))
            .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("суффикс");
    }

    @Override
    public String description() {
        return "Ставит новый суффикс";
    }*/
}
