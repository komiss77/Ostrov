package ru.komiss77.commands;

import java.util.List;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.ServerType;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.games.ArenaInfo;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.games.GameInfo;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.objects.CaseInsensitiveSet;
import ru.komiss77.utils.TimeUtil;


public class Server implements OCommand {

  private static final String COMMAND = "server";
  private static final List<String> ALIASES = List.of();
  private static final String DESCRIPTION = "Выбор игры";
  private static final boolean CAN_CONSOLE = false;
  private static final String arg0 = "game", arg1 = "arena", arg2 = "arg2", arg3 = "arg4", arg4 = "arg4";
  private static final CaseInsensitiveSet displayNames; //для команды /server

  static {
    displayNames = new CaseInsensitiveSet();
    for (final Game game : Game.values()) {
      if (game == Game.GLOBAL) continue;
      displayNames.add(game.suggestName);
    }
  }

  @Override
  public LiteralCommandNode<CommandSourceStack> command() {

    return Commands.literal(COMMAND)
        .executes(executor())//выполнение без аргументов
        //1 аргумент
        .then(Resolver.string(arg0)
                .suggests((cntx, sb) -> {

                  displayNames.stream()
                      .filter(c -> c.startsWith(sb.getRemaining()))
                      .forEach(c -> sb.suggest(c));

                  return sb.buildFuture();
                })
                .executes(executor())//выполнение c 1 аргументом

                //2 аргумента
                .then(Commands.argument(arg1, StringArgumentType.greedyString())//.then(Resolver.string(arg1)
                        .suggests((cntx, sb) -> {

                          final String serverName = Comm.arg(sb, 0);
                          final Game game = Game.fromServerName(serverName);
//Ostrov.log("game="+game);
                          if (game.type == ServerType.ARENAS) {
                            final GameInfo gi = GM.getGameInfo(game);
//Ostrov.log("GameInfo="+gi);
                            if (gi != null) {
                              gi.getArenaNames(game.defaultServer).stream()
                                  .filter(c -> c.startsWith(sb.getRemaining()))
                                  .forEach(c -> sb.suggest(c));
                            }
                          } else if (game.type == ServerType.LOBBY) {
                            final GameInfo gi = GM.getGameInfo(game);
                            if (gi != null) {
                              gi.getArenaNames(game.defaultServer).stream()
                                  .filter(c -> c.startsWith(sb.getRemaining()))
                                  .forEach(c -> sb.suggest(c));
                            }
                          }
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

      final Oplayer op = PM.getOplayer(p);

      p.getOpenInventory().close();

      if (arg.length == 0) {
        //op.menu.open(p, Section.РЕЖИМЫ);
        final TextComponent.Builder servers = Component.text().content("§bКлик на сервер: §e");
        for (final Game game : Game.values()) {
          if (game == Game.GLOBAL || game == Game.JL) continue;
          servers.append(Component.text(game.displayName + "§7, ")
              .hoverEvent(HoverEvent.showText(Component.text("§7Клик - перейти")))
              .clickEvent(ClickEvent.runCommand("/server " + game.name())));
        }
        //for (final String serverName : displayNames) {
        //  servers.append(Component.text(serverName + "§7, §e")
        //      .hoverEvent(HoverEvent.showText(Component.text("§7Клик - перейти")))
        //      .clickEvent(ClickEvent.runCommand("/server " + serverName)));
        //}
        p.sendMessage(servers.build());
        return 0;
      }


      boolean hasLevel;
      boolean hasReputation;

      if (arg.length >= 1) {

        String serverName = arg[0];

        if (serverName.equalsIgnoreCase("gui")) {
          op.menu.open(p, Section.РЕЖИМЫ);
          return 0;
        }

        if (serverName.equalsIgnoreCase("jail")) {
          if (ApiOstrov.isLocalBuilder(p)) {
            ApiOstrov.sendToServer(p, serverName, "");
          } else if (ApiOstrov.canBeBuilder(p)) {
            p.sendMessage("§5Перейти в чистилище можно только в режиме билдера!");
          } else {
            p.sendMessage("§5Перейти в чистилище могут только билдеры!");
          }
          return 0;
        }

        if (op.getDataInt(Data.BAN_TO) > 0) {
          p.sendMessage("§cВы сможете покинуть чистилище через " + TimeUtil.secondToTime(op.getDataInt(Data.BAN_TO) - Timer.getTime()));
          return 0;
        }

        final Game game = Game.fromServerName(serverName);
//Ostrov.log("onCommand arg.length="+arg.length+" serverName="+serverName+" game="+game);

        if (arg.length == 1
            || game.type == ServerType.ONE_GAME //для больших аргументы не имеют значения
            || game.type == ServerType.LOBBY //для лобби аргументы не имеют значения
            || (arg.length == 2 && arg[1].isEmpty())) {  //иногда в лобби отправляет как /server lobby0 [space] !

          if (game.type == ServerType.ONE_GAME) {

            //чекаем уровень и репу для перехода на серв вообще
            hasLevel = op.getStat(Stat.LEVEL) >= game.level;
            hasReputation = op.reputationCalc >= game.reputation;
            if (!hasLevel || !hasReputation) {
              p.sendMessage("§cДля перехода на данный сервер требуется уровень > " + game.level + " и репутация > " + game.reputation);
              return 0;
            }
            if (game == Game.SE) {
              serverName = op.getTextData("sedna");//подставить сервер выхода с седны!?
              if (serverName.isEmpty() || !GM.allBungeeServersName.contains(serverName)) {
                serverName = "sedna_wastes";
              }
//p.sendMessage("to Sedna:"+serverName);
            } else {
              serverName = game.defaultServer; //могло быть набрано /server Даария
            }

          } else if (game.type == ServerType.LOBBY) {

            if (arg[0].length() == 6 && arg[0].startsWith("lobby")) {
              serverName = arg[0]; //для лобби восстановить конкретный номер при прямом вводе
            } else {
              serverName = "lobby0";
            }

          } else if (game.type == ServerType.ARENAS) { //переход типа /server wz или /server поле_брани, без арены
            final GameInfo gi = GM.getGameInfo(game);
            if (gi == null) {
              p.sendMessage("§5Нет данных для игры " + game.name() + " - пробуем подключиться к §e" + game.defaultServer);
              serverName = game.defaultServer;
              //return true;
            } else if (gi.count() == 0) {
              p.sendMessage("§5Для игры §6" + game.name() + " §5не найдено арен - пробуем подключиться к §e" + game.defaultServer);
              serverName = game.defaultServer;
              //return true;
            } else {
              serverName = gi.arenas().stream().findAny().get().server;//arenas.get(0).server;
            }
          }

          if (Ostrov.MOT_D.equalsIgnoreCase(serverName)) {
            p.sendMessage("§6Вы и так уже на этом сервере!");
            return 0;
          } else {
            ApiOstrov.sendToServer(p, serverName, "");
            return 0;
          }

        }

        //if (displayNames.contains(serverName)) {

        // }
        //далее сработает только если serverName указан напрямую, типа bw01

        //определяем арену, если указана как аргумент
        //одиночки сюда уже не дойдут
        final String arenaMane = arg[1];
//Ostrov.log_warn("CMD arenaMane="+arenaMane);
        ArenaInfo ai = null;
        if (game == Game.GLOBAL) {
          ai = GM.lookup("", arenaMane);//пытаться найти арену по названию
        } else { //игра была определена (это могло быть типа /server поле_брани арена)
          final GameInfo gi = GM.getGameInfo(game);
          ai = gi.getArena(game.defaultServer, arenaMane);
          if (ai == null) {
            ai = GM.lookup("", arenaMane);//пытаться найти арену по названию
          }
        }
//Ostrov.log("arenaMane=>"+arenaMane+" ai="+ai);


        if (ai == null) { //арены не определить - просто на серв

          ApiOstrov.sendToServer(p, serverName, "");
          return 0;

        } else {

          hasLevel = op.getStat(Stat.LEVEL) >= ai.level;
          hasReputation = op.reputationCalc >= ai.reputation;
          if (hasLevel && hasReputation) {
            ApiOstrov.sendToServer(p, ai.server, ai.arenaName);
          } else {
            p.sendMessage("§cДля перехода на данный сервер требуется уровень > " + ai.level + " и репутация > " + ai.reputation);
          }

        }


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






