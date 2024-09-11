package ru.komiss77.commands;

import java.util.List;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.player.PM;


//!! в suggests не использовать получение аргумента через Resolver.string(cntx, arg0), или дальнейший код игнорится!!
//cntx.getArgument не работает, а cntx.getArguments вообще крашит серв!
//использовать костыль arg(sb, position );

public class EXAMPLE implements OCommand {

  private static final String COMMAND = "example";
  private static final List<String> ALIASES = List.of("aliase");
  private static final String DESCRIPTION = "команда";
  private static final boolean CAN_CONSOLE = false;
  private static final String arg0 = "arg0", arg1 = "arg1", arg2 = "arg2", arg3 = "arg4", arg4 = "arg4";

  @Override
  public LiteralCommandNode<CommandSourceStack> command() {

    return Commands.literal(COMMAND)
        .executes(executor())//выполнение без аргументов
        //1 аргумент
        .then(Resolver.string(arg0)
            .suggests((cntx, sb) -> {

              //обычно 0 аргумент - имя игрока
              PM.suggester(sb.getRemaining()).forEach(s -> sb.suggest(s));

              return sb.buildFuture();
            })
            .executes(executor())//выполнение c 1 аргументом

            //2 аргумент
            .then(Resolver.string(arg1)
                .suggests((cntx, sb) -> {
                  final CommandSender cs = cntx.getSource().getSender();
                  final String playerName = Comm.arg(sb, 0); //обычно 0 аргумент - имя игрока
                  final Player target = Bukkit.getPlayerExact(playerName);
                  if (target != null) {

                  }
                  return sb.buildFuture();
                })
                .executes(executor())//выполнение c 2 аргументами

                //3 аргумент
                .then(Resolver.string(arg2)
                    .suggests((cntx, sb) -> {
                      //sb.suggest("третий");
                      return sb.buildFuture();
                    })
                    .executes(executor())//выполнение c 3 аргументами

                    //4 аргумент
                    .then(Resolver.string(arg3)
                        .suggests((cntx, sb) -> {
                          //sb.suggest("четвёртый");
                          return sb.buildFuture();
                        })
                        .executes(executor())//выполнение c 4 аргументами

                        //5 аргумент
                        .then(Resolver.string(arg4)
                            .suggests((cntx, sb) -> {
                              //sb.suggest("пятый");
                              return sb.buildFuture();
                            })
                            .executes(executor())//выполнение c 5 аргументами

                        )
                    )
                )
            )
        )

        .build();
  }


  private static Command<CommandSourceStack> executor() {
    return cntx -> {
      final CommandSender cs = cntx.getSource().getSender();
      final Player p = (cs instanceof Player) ? (Player) cs : null;
      if (!CAN_CONSOLE && p == null) {
        cs.sendMessage("§eНе консольная команда!");
        return 0;
      }
      int idx = cntx.getInput().indexOf(" ");
      final String[] arg; //интересуют только аргументы, сама команда типа известна
      if (idx < 0) {
        arg = new String[0]; //"без параметров!");
      } else {
        arg = cntx.getInput().substring(idx + 1).split(" ");
      }
      //тут юзаем по старинке со всеми аргументами   /команда arg[0] ... arg[4]


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






