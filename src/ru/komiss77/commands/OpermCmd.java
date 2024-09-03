package ru.komiss77.commands;

import java.util.List;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.builder.menu.ViewPerm;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.inventory.SmartInventory;

//!! в suggests не использовать получение аргумента через Resolver.string(cntx, arg0), или дальнейший код игнорится!!
//cntx.getArgument не работает, а cntx.getArguments вообще крашит серв!
//использовать костыль arg(sb, position );

public class OpermCmd implements OCommand {

  private static final String COMMAND = "operm";
  private static final List<String> ALIASES = List.of();
  private static final String DESCRIPTION = "";
  private static final boolean CAN_CONSOLE = false;

  @Override
  public List<String> aliases() {
    return ALIASES;
  }

  @Override
  public String description() {
    return DESCRIPTION;
  }

  private static final String arg0 = "arg0", arg1 = "arg1", arg2 = "arg2", arg3 = "arg4", arg4 = "arg4";

  @Override
  public LiteralCommandNode<CommandSourceStack> command() {

    return Commands.literal(COMMAND)

        .executes(executor())//выполнение без аргументов

        //1 аргумент
        .then(Resolver.string(arg0).suggests((cntx, sb) -> {
                  PM.suggester(sb);//Bukkit.getOnlinePlayers().forEach(p -> sb.suggest(p.getName()));
                  return sb.buildFuture();
                }).executes(executor())//выполнение c 1 аргументом

                //2 аргумента
                .then(Resolver.string(arg1).suggests((cntx, sb) -> {
                          final CommandSender cs = cntx.getSource().getSender();
                          if (cs instanceof final Player p) {
                            if (ApiOstrov.canBeBuilder(p)) {
                              final String playerName = Comm.arg(sb, 0);//Resolver.string(cntx, arg0); убивает дальнейший код
                              final Player target = Bukkit.getPlayerExact(playerName);
                              if (target != null) {
                                target.getEffectivePermissions().stream()
                                    .filter(pa -> pa.getPermission().startsWith(sb.getRemaining()))
                                    .forEach(pa -> sb.suggest(pa.getPermission()));
                              }
                            }
                          }
                          return sb.buildFuture();
                        }).executes(executor())//выполнение c 2 аргументами

                        //3 аргумента
                        .then(Resolver.string(arg2).suggests((cntx, sb) -> {
                                  //sb.suggest("лишнее");
                                  return sb.buildFuture();
                                }).executes(executor())//выполнение c 3 аргументами

                                //4 аргумента
                                .then(Resolver.string(arg3).suggests((cntx, sb) -> {
                                          //sb.suggest("лишнее");
                                          return sb.buildFuture();
                                        }).executes(executor())//выполнение c 4 аргументами

                                        //5 аргументов
                                        .then(Resolver.string(arg4).suggests((cntx, sb) -> {
                                              //sb.suggest("лишнее");
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

//for (String s : arg) Ostrov.log_warn(">"+s+"<");
      if (arg.length == 0 || (arg.length == 1 && arg[0].equalsIgnoreCase(sender.getName()))) { //админ - права других
        SmartInventory.builder()
            .id("Права " + sender.getName())
            .provider(new ViewPerm(p))
            .size(6, 9)
            .title("Ваши права")
            .build()
            .open(p);
        //sender.sendMessage("§c/operm <ник> [право]");
        return Command.SINGLE_SUCCESS;
      }

      if (!ApiOstrov.canBeBuilder(sender) && !arg[0].equals(sender.getName())) {
        arg[0] = sender.getName();
        sender.sendMessage("§c" + Lang.t(p, "Вы можете посмтотреть только свои права!"));
        return 0;
      }
      final Player target = (Bukkit.getPlayerExact(arg[0]));
      if (target == null) {
        sender.sendMessage("§c" + Lang.t(p, "Игрок не найден!"));
        return 0;
      }

      if (arg.length == 1) {
        SmartInventory.builder()
            .id("Права " + arg[0])
            .provider(new ViewPerm(target))
            .size(6, 9)
            .title("Права " + arg[0])
            .build()
            .open(p);

      } else if (arg.length == 2) {
        sender.sendMessage("§f" + arg[0] + " §7право " + arg[1] + " : " + (target.hasPermission(arg[1]) ? "§aДа" : "§4Нет"));
      }


      return Command.SINGLE_SUCCESS;
    };
  }



}



