package ru.komiss77.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.OstrovDB;
import ru.komiss77.Perm;
import ru.komiss77.commands.args.Resolver;
import ru.komiss77.enums.Operation;
import ru.komiss77.enums.RewardType;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.objects.Group;

import java.util.Arrays;
import java.util.List;


public class RewardCmd implements OCommand {


    private static final String player = "player", item = "item", op = "operation", val = "value", reason = "reason";

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("reward")
            .then(Resolver.string(player).suggests((cntx, sb) -> {
                    if (!ApiOstrov.isLocalBuilder(cntx.getSource().getExecutor())) {
                        return sb.buildFuture();
                    }
                    Bukkit.getOnlinePlayers().forEach(p -> sb.suggest(p.getName()));
                    return sb.buildFuture();
                })
                .then(Resolver.string(item).suggests((cntx, sb) -> {
                        if (!ApiOstrov.isLocalBuilder(cntx.getSource().getExecutor())) {
                            return sb.buildFuture();
                        }
                        Arrays.stream(RewardType.values()).forEach(r -> sb.suggest(r.name().toLowerCase()));
                        return sb.buildFuture();
                    })
                    .then(Resolver.string(op).suggests((cntx, sb) -> {
                        if (!ApiOstrov.isLocalBuilder(cntx.getSource().getExecutor())) {
                            return sb.buildFuture();
                        }
                        switch (Resolver.string(cntx, item)) {
                            case "permission":
                            case "perm":
                                sb.suggest("ostrov.perm");
                                sb.suggest(Ostrov.MOT_D + ".builder");
                                break;
                            case "group":
                                for (final Group g : Perm.getGroups()) {
                                    if (!g.isStaff()) sb.suggest(g.name);
                                }
                                break;
                            default:
                                sb.suggest("add");
                                sb.suggest("get");
                                break;
                        }
                        return sb.buildFuture();
                    }).then(Resolver.string(val).suggests((cntx, sb) -> {
                                if (!ApiOstrov.isLocalBuilder(cntx.getSource().getExecutor())) {
                                    return sb.buildFuture();
                                }
                                switch (Resolver.string(cntx, item)) {
                                    case "permission":
                                    case "perm":
                                    case "group":
                                        sb.suggest("1h");
                                        sb.suggest("10h");
                                        sb.suggest("1d");
                                        sb.suggest("7d");
                                        sb.suggest("1m");
                                        break;
                                    default:
                                        sb.suggest("10");
                                        sb.suggest("100");
                                        sb.suggest("1000");
                                        break;
                                }
                                return sb.buildFuture();
                            })
                            .executes(tryReward())
                            .then(Resolver.string(reason).suggests((cntx, sb) -> {
                                    if (!ApiOstrov.isLocalBuilder(cntx.getSource().getExecutor())) {
                                        return sb.buildFuture();
                                    }
                                    sb.suggest("Ostrov");
                                    return sb.buildFuture();
                                })
                                .executes(tryReward()))
                    ))))
            .build();
    }

    private static Command<CommandSourceStack> tryReward() {
        return cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!ApiOstrov.isLocalBuilder(cs)) {
                cs.sendMessage("§cНедостаточно прав!");
                return 0;
            }

            final String tgt = Resolver.string(cntx, player);
            final RewardType type = RewardType.fromString(Resolver.string(cntx, item));
            if (type == null) {
                cs.sendMessage(Ostrov.PREFIX + "§cТакой награды не существует");
                help(cs);
                return 0;
            }

            String oper = Resolver.string(cntx, op);
            String value = Resolver.string(cntx, val);
            int amt;
            switch (type) {
                case PERM:
                case GROUP:
                    if (oper.length() > 64) {
                        oper = oper.substring(0, 63);
                        final int dt = oper.lastIndexOf('.');
                        if (dt > 0) oper = oper.substring(0, dt);
                        cs.sendMessage("§eПревышена длинна, обрезано до " + oper);
                    }

                    if (value.length() > 1) {
                        final int v = Integer.valueOf(value.substring(0, value.length() - 1), 0);
                        amt = switch (value.charAt(value.length() - 1)) {
                            case 'h' -> v * 60 * 60;
                            case 'd' -> v * 24 * 60 * 60;
                            case 'm' -> v * 30 * 24 * 60 * 60;
                            default -> 0;
                        };
                    } else {
                        amt = Integer.valueOf(value, 0);
                    }

                    if (amt == 0) {
                        cs.sendMessage("§cПустая награда! Для прав и групп укажи время: §e1h §c- 1 час, §e2d §c- 2 дня.");
                        Ostrov.log_warn("reward error: " + type.name() + ", " + oper + ", " + value);
                        return 0;
                    }

                    if (type == RewardType.GROUP && Ostrov.MOT_D.equals("pay")) {
                        amt = amt / 60 / 60 / 24; //привести к дням
                        if (amt < 1) {
                            cs.sendMessage("§cГруппа дни > 0!");
                            return 0;
                        }
                        OstrovDB.executePstAsync(cs, "INSERT INTO `payments` (`name`, `gr`, `days`) VALUES ('" + tgt + "', '" + oper + "', '" + amt + "')");
                        cs.sendMessage("§aГруппа " + oper + " для " + tgt + " на " + amt + "дн. : отправлена запись в БД");
                        return Command.SINGLE_SUCCESS;
                    }
                    break;
                case LONI:
                case EXP:
                case REP:
                case KARMA:
                case RIL:
                    amt = Integer.valueOf(value, 0);
                    if (amt < 1) {
                        cs.sendMessage("§eКолличество - целое положительное число!");
                        Ostrov.log_warn("reward error: " + type.name() + ", " + oper + ", " + value);
                        return 0;
                    }

                    if (oper.equals("get")) {
                        amt = -amt;
                    } else if (!oper.equals("add")) {
                        cs.sendMessage("§eДля награды " + type.name() + " допустимы только 'add' или 'get'");
                        return 0;
                    }

                    if (type == RewardType.EXP && amt < 1) {
                        cs.sendMessage("§eОпыт нужно прибавлять!");
                        return 0;
                    }

                    if (type == RewardType.RIL && Ostrov.MOT_D.equals("pay")) {
                        OstrovDB.executePstAsync(cs, "INSERT INTO `payments` (`name`, `rub`) VALUES ('" + tgt + "', '" + amt + "')");
                        cs.sendMessage("§a" + amt + " рил для " + tgt + " : отправлена запись в БД");
                        return Command.SINGLE_SUCCESS;
                    }
                    break;
                default:
                    amt = 0;
                    break;
            }

            //выполняем на банжи, чтобы кросссерверно!
            if (cs instanceof Player) {
                SpigotChanellMsg.sendMessage(((Player) cs), Operation.REWARD, cs.getName(), type.tag, amt, tgt, oper);
            } else {
                SpigotChanellMsg.sendMessage(Bukkit.getOnlinePlayers().stream().findAny().get(),
                    Operation.REWARD, "консоль", type.tag, amt, tgt, oper);
            }
            return Command.SINGLE_SUCCESS;
        };
    }

    @Override
    public List<String> aliases() {
        return List.of("ревард");
    }

    @Override
    public String description() {
        return "Выдача плюшек";
    }

    private static void help(final CommandSender cs) {
        cs.sendMessage("");
        cs.sendMessage("§3/reward <ник> <тип_награды> <параметр> <колл-во> <причина>");
        cs.sendMessage("§cКоманда исполняется от имени консоли/плагинов/оператора!");
        cs.sendMessage("§fПримеры:");
        cs.sendMessage("§a/reward komiss77 loni add 1000");
        cs.sendMessage("§a/reward komiss77 loni get rnd:0:100");
        cs.sendMessage("§a/reward komiss77 perm serwer.world.perm.aaa 1h");
        cs.sendMessage("§a/reward komiss77 perm perm.aaa forever");
        cs.sendMessage("§a/reward komiss77 group vip 10d");
        cs.sendMessage("§a/reward komiss77 group vip forever");
        cs.sendMessage("§a/reward komiss77 exp add 100");
        cs.sendMessage("§a/reward komiss77 rep get 5");
        cs.sendMessage("§a");
    }


    //reward <ник>  <тип_награды> <параметр>           <колл-во>  <источник>
    //reward komiss77 loni          add                   1000     ostrov
    //reward komiss77 loni          get                   500   ostrov
    //reward komiss77 perm          serwer.world.perm.aaa     1      ostrov
    //reward komiss77 perm          perm.aaa              1h      ostrov
    //reward komiss77 group          vip                   10       ostrov
    //reward komiss77 group          vip                  5d       ostrov
    //reward komiss77 exp            add                   1000     ostrov
}
    
    
 
