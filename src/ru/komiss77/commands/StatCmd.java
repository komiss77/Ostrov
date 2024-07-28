package ru.komiss77.commands;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.args.Resolver;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.mission.MissionManager;

import java.util.List;





public class StatCmd implements Listener, OCommand {

  //запрос банжи, если есть - разкодировать raw
  //если пустой - выкачать из снапшота БД

  @Override
  public LiteralCommandNode<CommandSourceStack> command() {
    final String player = "player", stat = "stat", val = "value";
    return Commands.literal("stat")
      .then(Resolver.player(player).then(Resolver.string(stat).suggests((cntx, sb) -> {
        if (!ApiOstrov.isLocalBuilder(cntx.getSource().getSender())) {
          return sb.buildFuture();
        }

        for (final Stat st : Stat.values()) sb.suggest(st.name());
        for (final String name : MissionManager.customStatsDisplayNames.keySet()) sb.suggest(name);
        sb.suggest("локальная");
        return sb.buildFuture();
      })
        .then(Resolver.integer(val, 0).suggests((cntx, sb) -> {
          if (!ApiOstrov.isLocalBuilder(cntx.getSource().getSender())) {
            return sb.buildFuture();
          }

          for (int i = 1; i != 5; i++) {
            sb.suggest(i*5);
          }
          return sb.buildFuture();
        }).executes(cntx -> {
          final CommandSender cs = cntx.getSource().getExecutor();
          if (!(cs instanceof final Player pl)) {
            cs.sendMessage("§eНе консольная команда!");
            return 0;
          }

          final Player tgt = Resolver.player(cntx, player);
          if (tgt == null) {
            pl.sendMessage(Ostrov.PREFIX+"§cТакой игрок не онлайн");
            return 0;
          }

          final String sn = Resolver.string(cntx, stat);
          final Stat st = Stat.fromName(sn);
          final int value = Resolver.integer(cntx, val);
          if (st==null) {
            ApiOstrov.addCustomStat(tgt, sn, value);
            cs.sendMessage("§7Статистика §e"+sn+" §7(customStat) увеличена на §e"+value+" §7для §f"+tgt.getName());
          } else {
            ApiOstrov.addStat(tgt, st, value);
            cs.sendMessage("§7Статистика §b"+st+" §7увеличена на §e"+value+" §7для §f"+tgt.getName());
          }
          return Command.SINGLE_SUCCESS;
        }))))
      .build();
  }

  @Override
  public List<String> aliases() {
    return List.of("стат");
  }

  @Override
  public String description() {
    return "Выдает статы";
  }

}
    
    
 
