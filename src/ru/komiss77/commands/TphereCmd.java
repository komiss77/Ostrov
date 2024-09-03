package ru.komiss77.commands;

import java.util.List;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Cfg;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.player.PM;

public class TphereCmd implements OCommand {

  private static final String COMMAND = "tphere";
  private static final List<String> ALIASES = List.of();
  private static final String DESCRIPTION = "ТП к себе";
  private static final boolean CAN_CONSOLE = false;


  private static final String arg0 = "arg0", arg1 = "arg1", arg2 = "arg2", arg3 = "arg4", arg4 = "arg4";

  @Override
  public LiteralCommandNode<CommandSourceStack> command() {

    return Commands.literal(COMMAND)

        .executes(executor())//выполнение без аргументов

        //1 аргумент
        .then(Resolver.string(arg0).suggests((cntx, sb) -> {

                  //обычно 0 аргумент - имя игрока
                  PM.suggester(sb);

                  return sb.buildFuture();
                }).executes(executor())//выполнение c 1 аргументом

                //2 аргумента
                .then(Resolver.string(arg1).suggests((cntx, sb) -> {

                          return sb.buildFuture();
                        }).executes(executor())//выполнение c 2 аргументами

                        //3 аргумента
                        .then(Resolver.string(arg2).suggests((cntx, sb) -> {
                                  //sb.suggest("третий");
                                  return sb.buildFuture();
                                }).executes(executor())//выполнение c 3 аргументами

                                //4 аргумента
                                .then(Resolver.string(arg3).suggests((cntx, sb) -> {
                                          //sb.suggest("четвёртый");
                                          return sb.buildFuture();
                                        }).executes(executor())//выполнение c 4 аргументами

                                        //5 аргументов
                                        .then(Resolver.string(arg4).suggests((cntx, sb) -> {
                                              //sb.suggest("пятый");
                                              return sb.buildFuture();
                                            }).executes(executor())//выполнение c 5 аргументами

                                        )
                                )
                        )
                )
        )

        .build();
  }


  private static Command<CommandSourceStack> executor() {
    return cntx -> {
      final CommandSender sender = cntx.getSource().getSender();
      final Player p = (sender instanceof Player) ? (Player) sender : null;
      if (!CAN_CONSOLE && p == null) {
        sender.sendMessage("§eНе консольная команда!");
        return 0;
      }
      int idx = cntx.getInput().indexOf(" ");
      final String[] arg; //интересуют только аргументы, сама команда типа известна
      if (idx < 0) {
        arg = new String[0]; //"без параметров!");
      } else {
        arg = cntx.getInput().substring(idx + 1).split(" ");
      }

      if (Cfg.tphere_command) {
        if (p.hasPermission("ostrov.tphere")) {
          if (arg.length == 1) {
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "tp " + arg[0] + " " + p.getName());
          } else {
            p.sendMessage("§cФормат: tphere <ник>");
          }
        } else {
          p.sendMessage("§cУ Вас нет пава ostrov.tphere !");
        }
      } else {
        p.sendMessage("§ctphere отключёна на этом сервере!");
      }


      return Command.SINGLE_SUCCESS;
    };
  }


  @Override
  public List<String> aliases() {
    return ALIASES;
  }

  @Override
  public String description() {
    return DESCRIPTION;
  }

}






