package ru.komiss77.commands;

import java.util.List;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.player.PM;


public class InvseeCmd implements OCommand {

  private static final String COMMAND = "invsee";
  private static final List<String> ALIASES = List.of("");
  private static final String DESCRIPTION = "Просмотр инвентаря";
  private static final boolean CAN_CONSOLE = false;
  private static final String arg0 = "name", arg1 = "type", arg2 = "arg2", arg3 = "arg4", arg4 = "arg4";

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

            //2 аргумента
            .then(Resolver.string(arg1)
                .suggests((cntx, sb) -> {
                  sb.suggest("main");
                  sb.suggest("ender");
                  sb.suggest("extra");
                  return sb.buildFuture();
                })
                .executes(executor())//выполнение c 2 аргументами

                //3 аргумента
                .then(Resolver.string(arg2)
                    .suggests((cntx, sb) -> {
                      //sb.suggest("третий");
                      return sb.buildFuture();
                    })
                    .executes(executor())//выполнение c 3 аргументами

                    //4 аргумента
                    .then(Resolver.string(arg3)
                        .suggests((cntx, sb) -> {
                          //sb.suggest("четвёртый");
                          return sb.buildFuture();
                        })
                        .executes(executor())//выполнение c 4 аргументами

                        //5 аргументов
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
      //тут юзаем по старинке со всеми аргументами   /команда arg[0] ... arg[4]
      if (arg.length == 0) {
        p.sendMessage(Ostrov.PREFIX + "§cУкажи игрока!");
        return 0;
      }
      final Player tgt = Bukkit.getPlayerExact(arg[0]);
      if (tgt == null) {
        p.sendMessage(Ostrov.PREFIX + "§6" + arg[0] + " §cнет на сервере!");
        return 0;
      }
      String type = "main";
      if (arg.length >= 2) {
        type = arg[1];
      }
      switch (type) {
        case "main":
          p.openInventory(tgt.getInventory());
          tgt.sendMessage(Ostrov.PREFIX + p.getName() + " §aпросматривает твой инвентарь!");
          break;
        case "ender":
          p.openInventory(tgt.getEnderChest());
          tgt.sendMessage(Ostrov.PREFIX + p.getName() + " §aпросматривает твой эндер-сундук!");
          break;
        case "extra":
        default:
          return 0;
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

/*

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String player = "target", type = "type";
        return Commands.literal("invsee")
            .then(Resolver.player(player)
                .executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player pl)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }

                    final Player tgt = Resolver.player(cntx, player);
                    if (tgt == null) {
                        pl.sendMessage(Ostrov.PREFIX + "§cИгрок не онлайн!");
                        return 0;
                    }

                    pl.openInventory(tgt.getInventory());
                    tgt.sendMessage(Ostrov.PREFIX + pl.getName() + " §aпросматривает твой инвентарь!");
                    return Command.SINGLE_SUCCESS;
                }).then(Resolver.string(type)
                    .executes(cntx -> {
                        final CommandSender cs = cntx.getSource().getSender();
                        if (!(cs instanceof final Player pl)) {
                            cs.sendMessage("§eНе консольная команда!");
                            return 0;
                        }

                        final Player tgt = Resolver.player(cntx, player);
                        if (tgt == null) {
                            pl.sendMessage(Ostrov.PREFIX + "§cИгрок не онлайн!");
                            return 0;
                        }

                        final Inventory inv;
                        switch (Resolver.string(cntx, type)) {
                            case "main":
                                inv = tgt.getInventory();
                                break;
                            case "ender":
                                inv = tgt.getEnderChest();
                                break;
                            case "extra":
                            default:
                                pl.sendMessage(Ostrov.PREFIX + "§cНеправильный синтакс комманды!");
                                return 0;
                        }

                        pl.openInventory(inv);
                        tgt.sendMessage(Ostrov.PREFIX + pl.getName() + " §aпросматривает твой инвентарь!");
                        return Command.SINGLE_SUCCESS;
                    })))
            .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("инвентарь");
    }

    @Override
    public String description() {
        return "Просмотр инвентаря";
    }*/
}
