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
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.mission.MissionManager;
import ru.komiss77.utils.NumUtils;

public class StatCmd implements OCommand {

  private static final String COMMAND = "stat";
  private static final List<String> ALIASES = List.of();
  private static final String DESCRIPTION = "Добавить статистику";
  private static final boolean CAN_CONSOLE = true;
  private static final String arg0 = "player", arg1 = "stat", arg2 = "value", arg3 = "arg4", arg4 = "arg4";

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


                  for (final Stat st : Stat.values()) {
                    if (st.name().toLowerCase().startsWith(sb.getRemaining())) sb.suggest(st.name());
                  }
                  for (final String name : MissionManager.customStatsDisplayNames.keySet()) {
                    if (name.startsWith(sb.getRemaining())) sb.suggest(name);
                  }
                  sb.suggest("локальная");
                  return sb.buildFuture();
                })
                .executes(executor())//выполнение c 2 аргументами

                //3 аргумент
                .then(Resolver.string(arg2)
                    .suggests((cntx, sb) -> {
                      for (int i = 1; i <= 10; i++) {
                        sb.suggest(i);
                      }
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

      int idx = cntx.getInput().indexOf(" ");
      final String[] arg; //интересуют только аргументы, сама команда типа известна
      if (idx < 0) {
        arg = new String[0]; //"без параметров!");
      } else {
        arg = cntx.getInput().substring(idx + 1).split(" ");
      }

      if (cs instanceof Player && !cs.isOp()) {
        cs.sendMessage("");
        cs.sendMessage("§cКоманда исполняется от имени консоли/плагинов/оператора!");
        cs.sendMessage("");
        return 0;
      }


      if (arg.length != 3) {
        help(cs);
        return 0;
      }

      final String name = arg[0];
      //final Oplayer op = PM.getOplayer(arg[0]);
      if (!PM.exist(name)) {
        cs.sendMessage("§eИгрока " + name + " нет на локальном сервере.");
        return 0;
      }

      final int value = NumUtils.intOf(arg[2], 0);
      if (value < 1 || value > 100) {
        cs.sendMessage("§eЗначение допустимо от 1 до 100!");
        return 0;
      }

      final Stat st = Stat.fromName(arg[1]);
      if (st == null) {
        ApiOstrov.addCustomStat(Bukkit.getPlayer(name), arg[1], value);
        cs.sendMessage("§7Статистика §e" + arg[1] + " §7(customStat) увеличена на §e" + value + " §7для §f" + name);
        //cs.sendMessage("§eНет статистики "+arg[1]+"!");
        //return false;
      } else {
        ApiOstrov.addStat(Bukkit.getPlayer(name), st, value);
        cs.sendMessage("§7Статистика §b" + st + " §7увеличена на §e" + value + " §7для §f" + name);
      }

      //cs.sendMessage("§7Статистика §b"+st+" §7увеличена на §e"+value+" §7для §f"+name);
      //final String stat_raw = arg[1];

//System.out.print("StatAdd name="+name+" stats="+stat_raw);

//System.out.println(Action.OSTROV_REWARD+", "+for_name+", "+type.toString()+", "+param+", "+(forever?true:ammount)+", "+sender);


      //ApiOstrov.sendMessage(sender, Action.OSTROV_REWARD, for_name+":"+type.toString()+":"+param+":"+(forever?"forever":ammount));


      return Command.SINGLE_SUCCESS;
    };
  }

  private static void help(final CommandSender cs) {
    cs.sendMessage("");
    //cs.sendMessage("§3/"+this.getClass().getSimpleName()+" statadd bw_wi");
    cs.sendMessage("§cКоманда исполняется от имени консоли/плагинов/оператора!");
    cs.sendMessage("§fПримеры:");
    cs.sendMessage("§a/statadd komiss77 bw_game 5");
    //cs.sendMessage("§a/statadd komiss77 sg_game,sg_kill:5");
    cs.sendMessage("");
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

/*

public class StatCmd implements Listener, OCommand {

    //запрос банжи, если есть - разкодировать raw
    //если пустой - выкачать из снапшота БД

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String player = "player", stat = "stat", val = "value";

        return Commands.literal("stat")

            .then(Resolver.player(player).then(Resolver.string(stat).suggests( (cntx, sb) -> {
                    if (!ApiOstrov.isLocalBuilder(cntx.getSource().getSender())) {
                        return sb.buildFuture();
                    }

                    for (final Stat st : Stat.values()) {
                        sb.suggest(st.name());
                    }
                    for (final String name : MissionManager.customStatsDisplayNames.keySet()) {
                        sb.suggest(name);
                    }
                    sb.suggest("локальная");
                    return sb.buildFuture();
                })

                .then(Resolver.integer(val, 0).suggests( (cntx, sb) -> {
                    if (!ApiOstrov.isLocalBuilder(cntx.getSource().getSender())) {
                        return sb.buildFuture();
                    }
                    for (int i = 1; i != 5; i++) {
                        sb.suggest(i * 5);
                    }
                    return sb.buildFuture();
                }).executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player pl)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }

                    final Player tgt = Resolver.player(cntx, player);
                    if (tgt == null) {
                        pl.sendMessage(Ostrov.PREFIX + "§cТакой игрок не онлайн");
                        return 0;
                    }

                    final String sn = Resolver.string(cntx, stat);
                    final Stat st = Stat.fromName(sn);
                    final int value = Resolver.integer(cntx, val);
                    if (st == null) {
                        ApiOstrov.addCustomStat(tgt, sn, value);
                        cs.sendMessage("§7Статистика §e" + sn + " §7(customStat) увеличена на §e" + value + " §7для §f" + tgt.getName());
                    } else {
                        ApiOstrov.addStat(tgt, st, value);
                        cs.sendMessage("§7Статистика §b" + st + " §7увеличена на §e" + value + " §7для §f" + tgt.getName());
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
    
    */
 
