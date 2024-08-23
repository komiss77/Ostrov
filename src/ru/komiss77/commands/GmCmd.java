package ru.komiss77.commands;

import java.util.List;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Cfg;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;


public class GmCmd implements OCommand {


    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String mode = "mode";
        return Commands.literal("gm").executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player p)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }
                    if (!Cfg.gm_command) {
                        p.sendMessage("§c" + Lang.t(p, "cGm отключёна на этом сервере!"));
                        return 0;
                    }
                    if (!p.hasPermission("ostrov.gm")) {
                        p.sendMessage("§cНет права ostrov.gm!");
                        return 0;
                    }
                    if (p.getGameMode() == GameMode.SURVIVAL) {
                        p.setGameMode(GameMode.CREATIVE);
                        p.sendMessage("§e" + Lang.t(p, "Установлен креатив!"));
                    } else if (p.getGameMode() == GameMode.CREATIVE) {
                        p.setGameMode(GameMode.SURVIVAL);
                        p.sendMessage("§e" + Lang.t(p, "Установлено выживание!"));
                    }
                    return Command.SINGLE_SUCCESS;
                })

                .then(Resolver.string(mode).suggests((cntx, sb) -> {
                            final CommandSender cs = cntx.getSource().getSender();
                            //if (!(cs instanceof ConsoleCommandSender)) return sb.buildFuture();
                            //Bukkit.getOnlinePlayers().forEach(p -> sb.suggest(p.getName()));
                            sb.suggest("0");
                            sb.suggest("1");
                            sb.suggest("2");
                            sb.suggest("3");
                            return sb.buildFuture();
                        })
                        .executes(cntx -> {
                            final CommandSender cs = cntx.getSource().getSender();
                            if (!(cs instanceof final Player p)) {
                                cs.sendMessage("§eНе консольная команда!");
                                return 0;
                            }
                            if (!Cfg.gm_command) {
                                p.sendMessage("§c" + Lang.t(p, "cGm отключёна на этом сервере!"));
                                return 0;
                            }
                            if (!p.hasPermission("ostrov.gm")) {
                                p.sendMessage("§cНет права ostrov.gm!");
                                return 0;
                            }
                            //final Oplayer op = PM.getOplayer(p);
                            final String arg0 = Resolver.string(cntx, mode);
                            switch (arg0) {
                                case "0" ->
                                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode survival " + p.getName());
                                case "1" ->
                                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode creative " + p.getName());
                                case "2" ->
                                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode adventure " + p.getName());
                                case "3" ->
                                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode spectator " + p.getName());
                                default -> p.sendMessage("§cФормат: gm <0..3>");
                            }
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
        return "Управлять игровым режимом";
    }

}
    
    
 
