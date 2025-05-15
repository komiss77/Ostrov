package ru.komiss77.modules.netty;

import com.mojang.brigadier.Command;
import org.bukkit.command.CommandSender;
import ru.komiss77.ApiOstrov;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.tools.OCmdBuilder;
import ru.komiss77.commands.tools.Resolver;


public class RemoteCmd {

  public RemoteCmd() { //новое
    final String remote = "remote";
    new OCmdBuilder("remote", "/remote [команда и параметры]")
        .then(Resolver.greedy(remote)).run(cntx -> {
          final CommandSender cs = cntx.getSource().getSender();
          if (!ApiOstrov.canBeBuilder(cs)) {
            cs.sendMessage("§cБилдер!");
            return 0;
          }

          final String remoteCmd = Resolver.string(cntx, remote);
          OsQuery.send(QueryCode.REMOTE_CMD, cs.getName() + LocalDB.WORD_SPLIT + remoteCmd);

          return Command.SINGLE_SUCCESS;
        })
        .description("Выполнение команды на bridge")
        .register(Ostrov.mgr);
  }



  /*
    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String cmd = "cmd", remote = "remote";

        return Commands.literal("remote").executes( cntx -> {
          final CommandSender cs = cntx.getSource().getSender();
          //if (!(cs instanceof final Player p)) {
          //    cs.sendMessage("§eНе консольная команда!");
          //    return 0;
          //}
          if (!ApiOstrov.canBeBuilder(cs)) {
            cs.sendMessage("§cБилдер!");
            return 0;
          }
          cs.sendMessage("§6Добавь текст удалённой команды");
          return Command.SINGLE_SUCCESS;
          })

            .then(Commands.argument(cmd, StringArgumentType.greedyString())//.then(Resolver.string(reason)
                .suggests( (cntx, sb) -> {
                  sb.suggest("diag");
                  sb.suggest("diag add daaria");
                  sb.suggest("diag del daaria");
                  return sb.buildFuture();
                })
                .executes( cntx -> {
                  final CommandSender cs = cntx.getSource().getSender();
                  //if (!(cs instanceof final Player p)) {
                  //  cs.sendMessage("§eНе консольная команда!");
                  //  return 0;
                  //}
                  if (!ApiOstrov.canBeBuilder(cs)) {
                    cs.sendMessage("§cБилдер!");
                    return 0;
                  }
                  final String remoteCmd = Resolver.string(cntx, remote);
                  OsQuery.send(QueryCode.REMOTE_CMD, cs.getName()+ LocalDB.WORD_SPLIT+remoteCmd);
                  return Command.SINGLE_SUCCESS;
                })
            )
            .build();
    }

    @Override
    public Set<String> aliases() {
        return Set.of();
    }

    @Override
    public String description() {
        return "Управлять режимом полёт";
    }*/

}
    
    
 
