package ru.komiss77.commands;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;

import java.util.List;


public class BossBarCmd implements OCommand {

    //запрос банжи, если есть - разкодировать raw
    //если пустой - выкачать из снапшота БД

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String player = "player";
        return Commands.literal("bossbar").executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof ConsoleCommandSender)) return 0;

                cs.sendMessage("§6Операторы:");
                Bukkit.getOperators().forEach(op -> cs.sendMessage("§6" + op.getName()));
                return Command.SINGLE_SUCCESS;
            })
            .then(Resolver.string(player).suggests((cntx, sb) -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof ConsoleCommandSender)) return sb.buildFuture();
                Bukkit.getOnlinePlayers().forEach(p -> sb.suggest(p.getName()));
                return sb.buildFuture();
            }).executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof ConsoleCommandSender)) return 0;

                final OfflinePlayer tgt = Bukkit
                    .getOfflinePlayer(Resolver.string(cntx, player));
                if (tgt.isOp()) {
                    tgt.setOp(false);
                    cs.sendMessage("§c" + tgt.getName() + " теперь не оператор!");
                    Ostrov.log_warn("§e" + tgt.getName() + " теперь не оператор!");
                    return Command.SINGLE_SUCCESS;
                }

                final Player tpl = tgt.getPlayer();
                if (tpl != null) {
                    final Oplayer top = PM.getOplayer(tpl);
                    if (!top.isStaff) {
                        cs.sendMessage("§c" + tgt.getName() + " даже не персонал!");
                        Ostrov.log_warn("§c" + cs.getName() + " пытался дать ОП " + tgt.getName() + "!");
                        return 0;
                    }
                    tpl.sendMessage(Ostrov.PREFIX + "§6Ты теперь оператор!");
                }
                tgt.setOp(true);
                cs.sendMessage("§c" + tgt.getName() + " назначен оператором!");
                Ostrov.log_warn("§e" + tgt.getName() + " назначен оператором!");
                return Command.SINGLE_SUCCESS;
            }))
            .build();
    }

    @Override
    public List<String> aliases() {
        return List.of();
    }

    @Override
    public String description() {
        return "";
    }

}
    
    
 
