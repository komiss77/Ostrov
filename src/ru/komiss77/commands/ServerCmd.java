package ru.komiss77.commands;

import java.util.List;
import java.util.regex.Pattern;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
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


public class ServerCmd implements OCommand {

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
        final String server = "server", map = "map";
        return Commands.literal("server").executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player pl)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                final Oplayer op = PM.getOplayer(pl);
                op.menu.open(pl, Section.РЕЖИМЫ);
        	/*final TextComponent.Builder servers = Component.text().content("§bКлик на сервер: §e");
          for (final String serverName : displayNames) {
            servers.append(Component.text(serverName+"§7, §e")
              .hoverEvent(HoverEvent.showText(Component.text("§7Клик - перейти")))
              .clickEvent(ClickEvent.runCommand("/server "+serverName)));
          }
          p.sendMessage(servers.build());*/
                return Command.SINGLE_SUCCESS;
            })
            .then(Resolver.string(server).suggests((cntx, sb) -> {
                    displayNames.forEach(s -> sb.suggest(s));
                    return sb.buildFuture();
                })
                .executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player pl)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }

                    final Oplayer op = PM.getOplayer(pl);
                    String serv = Resolver.string(cntx, server);
                    final Game game = Game.fromServerName(serv);

                    if (game == null) {
                        pl.sendMessage("§cНету режима " + serv);
                        return 0;
                    }

                    //чекаем уровень и репу для перехода на серв вообще
                    if (op.getStat(Stat.LEVEL) < game.level || op.reputationCalc < game.reputation) {
                        pl.sendMessage("§cДля перехода надо иметь уровень > " + game.level + " и репутацию > " + game.reputation);
                        return 0;
                    }

                    if (game == Game.JL && op.getDataInt(Data.BAN_TO) > 0) {
                        pl.sendMessage("§cВы сможете покинуть чистилище через " + ApiOstrov.secondToTime(op.getDataInt(Data.BAN_TO) - Timer.getTime()));
                        return 0;
                    }

                    return switch (game) {
                        case SE -> trySend(pl, "sedna_wastes");
//            serverName = op.getTextData("sedna"); //не нужно, процессится уже на седне
//            if (!GM.allBungeeServersName.contains(game)) {
//            }
                        case LOBBY -> {
                            if (!Pattern.matches("lobby[0-9]+", serv)) {
                                serv = "lobby0";
                            }
                            yield trySend(pl, serv);
                        }
                        default -> trySend(pl, game.defaultServer);
                    };
          /*if (game.type == ServerType.ARENAS) { //переход типа /server wz или /server поле_брани, без арены
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
          }*/
                }).then(Resolver.string(map).suggests((cntx, sb) -> {
                    final Game game = Game.fromServerName(Resolver.string(cntx, server));
                    final GameInfo gi = GM.getGameInfo(game);
                    gi.arenas().forEach(a -> sb.suggest(a.arenaName));
                    return sb.buildFuture();
                }).executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player pl)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }

                    final Oplayer op = PM.getOplayer(pl);
                    String serv = Resolver.string(cntx, server);
                    final Game game = Game.fromServerName(serv);

                    if (game == null) {
                        pl.sendMessage("§cНету режима " + serv);
                        return 0;
                    }

                    //чекаем уровень и репу для перехода на серв вообще
                    if (op.getStat(Stat.LEVEL) < game.level || op.reputationCalc < game.reputation) {
                        pl.sendMessage("§cДля перехода надо иметь уровень > " + game.level + " и репутацию > " + game.reputation);
                        return 0;
                    }

                    if (game == Game.JL && op.getDataInt(Data.BAN_TO) > 0) {
                        pl.sendMessage("§cВы сможете покинуть чистилище через " + ApiOstrov.secondToTime(op.getDataInt(Data.BAN_TO) - Timer.getTime()));
                        return 0;
                    }

                    return switch (game) {
                        case SE -> trySend(pl, "sedna_wastes");
//            serverName = op.getTextData("sedna"); //не нужно, процессится уже на седне
//            if (!GM.allBungeeServersName.contains(game)) {
//            }
                        case LOBBY -> {
                            if (!Pattern.matches("lobby[0-9]+", serv)) {
                                serv = "lobby0";
                            }
                            yield trySend(pl, serv);
                        }
                        default -> {
                            if (game.type == ServerType.ARENAS) {
                                //определяем арену, если указана как аргумент
                                //одиночки сюда уже не дойдут
                                final String arena = Resolver.string(cntx, map);
                                ArenaInfo ai;
                                if (game == Game.GLOBAL) {
                                    ai = GM.lookup("", arena);//пытаться найти арену по названию
                                } else { //игра была определена (это могло быть типа /server поле_брани арена)
                                    final GameInfo gi = GM.getGameInfo(game);
                                    ai = gi.getArena(game.defaultServer, arena);
                                    if (ai == null) {
                                        ai = GM.lookup("", arena);//пытаться найти арену по названию
                                    }
                                }
                                yield trySend(pl, game.defaultServer, ai == null ? "" : ai.arenaName);
                            }
                            yield trySend(pl, game.defaultServer);
                        }
                    };
                })))
            .build();
    }

    private int trySend(final Player pl, final String serv) {
        return trySend(pl, serv, null);
    }

    private int trySend(final Player pl, final String serv, final String arena) {
        if (Ostrov.MOT_D.equalsIgnoreCase(serv)) {
            pl.sendMessage("§6Вы и так уже на этом сервере!");
            return 0;
        }
        ApiOstrov.sendToServer(pl, serv, arena);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public List<String> aliases() {
        return List.of("сервер", "serv", "серв");
    }

    @Override
    public String description() {
        return "Сервер комманда";
    }

}
    
    
 
