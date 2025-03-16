package ru.komiss77.modules.netty;

import java.util.Set;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.LocalDB;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.OCommand;
import ru.komiss77.commands.tools.OCmdBuilder;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Operation;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.TCUtil;


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
        .register();
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
    
    
 
