package ru.komiss77.commands;

import java.util.Set;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Cfg;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.DelayTeleport;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.NumUtil;

public class TpposCMD implements OCommand {

  private static final String COMMAND = "tppos";
  private static final Set<String> ALIASES = Set.of();
  private static final String DESCRIPTION = "ТП по координатам";
  private static final boolean CAN_CONSOLE = false;


  private static final String arg0 = "arg0", arg1 = "arg1", arg2 = "arg2", arg3 = "arg4", arg4 = "arg4";

  @Override
  public LiteralCommandNode<CommandSourceStack> command() {

    return Commands.literal(COMMAND)

        .executes(executor())//выполнение без аргументов

        //1 аргумент
        .then(Resolver.string(arg0).suggests((cntx, sb) -> {
                  if (cntx.getSource().getSender() instanceof final Player p) {
                    sb.suggest(p.getLocation().getBlockX());
                  }
                  return sb.buildFuture();
                }).executes(executor())//выполнение c 1 аргументом

                //2 аргумента
                .then(Resolver.string(arg1).suggests((cntx, sb) -> {
                          if (cntx.getSource().getSender() instanceof final Player p) {
                            sb.suggest(p.getLocation().getBlockY());
                          }
                          return sb.buildFuture();
                        }).executes(executor())//выполнение c 2 аргументами

                        //3 аргумента
                        .then(Resolver.string(arg2).suggests((cntx, sb) -> {
                                  if (cntx.getSource().getSender() instanceof final Player p) {
                                    sb.suggest(p.getLocation().getBlockZ());
                                  }
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

      final Oplayer op = PM.getOplayer(p);
      if (Cfg.tppos_command || op.hasGroup("youtuber")) {
        if (p.hasPermission("ostrov.tppos") || op.hasGroup("youtuber")) {
          if (arg.length == 3) {
            if (NumUtil.isInt(arg[0]) && NumUtil.isInt(arg[1]) && NumUtil.isInt(arg[2])) {
              DelayTeleport.tp(p, new Location(p.getWorld(), Double.parseDouble(arg[0]), Double.parseDouble(arg[1]), Double.parseDouble(arg[2])), 3, "Вы вернулись на указанную локацию", true, true, DyeColor.BROWN);
              //Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "tp "+p.name()+" "+arg[0]+" "+arg[1]+" "+arg[2] );
            } else {
              p.sendMessage("§c" + Lang.t(p, "Координаты должны быть числами!"));
              return 0;
            }
          } else {
            p.sendMessage("§cФормат: tppos <x> <y> <z>");
          }
        } else {
          p.sendMessage("§cУ Вас нет пава ostrov.tppos !");
        }
      } else {
        p.sendMessage("§ctppos отключёна на этом сервере!");
      }

      return Command.SINGLE_SUCCESS;
    };
  }


  @Override
  public Set<String> aliases() {
    return ALIASES;
  }

  @Override
  public String description() {
    return DESCRIPTION;
  }

}






