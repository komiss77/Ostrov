package ru.komiss77.commands;

import java.util.Set;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Cfg;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;


public class FlyCmd implements OCommand {


    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String mode = "mode";
        return Commands.literal("fly").executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player p)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }
                    if (!Cfg.fly_command) {
                        p.sendMessage("§c" + Lang.t(p, "Полёт отключён на этом сервере!"));
                        return 0;
                    }
                    if (!p.hasPermission("ostrov.fly")) {
                        p.sendMessage("§cНет права ostrov.fly!");
                        return 0;
                    }
                    final Oplayer op = PM.getOplayer(p);
                    if (op.allow_fly && p.getAllowFlight()) {
                        op.allow_fly = false;
                        p.setFlying(false);
                        p.setAllowFlight(false);
                        p.setFallDistance(0);
                        p.sendMessage("§e" + Lang.t(p, "Режим полёта выключен!"));
                    } else if (op.pvp_time == 0) {
                        op.allow_fly = true;
                        p.setAllowFlight(true);
                        p.sendMessage("§a" + Lang.t(p, "Режим полёта включен!"));
                    }
                    return Command.SINGLE_SUCCESS;
                })

                .then(Resolver.string(mode).suggests((cntx, sb) -> {
                            final CommandSender cs = cntx.getSource().getSender();
                            //if (!(cs instanceof ConsoleCommandSender)) return sb.buildFuture();
                            //Bukkit.getOnlinePlayers().forEach(p -> sb.suggest(p.getName()));
                            sb.suggest("on");
                            sb.suggest("off");
                            return sb.buildFuture();
                        })
                        .executes(cntx -> {
                            final CommandSender cs = cntx.getSource().getSender();
                            if (!(cs instanceof final Player p)) {
                                cs.sendMessage("§eНе консольная команда!");
                                return 0;
                            }
                            if (!Cfg.fly_command) {
                                p.sendMessage("§c" + Lang.t(p, "Полёт отключён на этом сервере!"));
                                return 0;
                            }
                            if (!p.hasPermission("ostrov.fly")) {
                                p.sendMessage("§cНет права ostrov.fly!");
                                return 0;
                            }
                            //final Oplayer op = PM.getOplayer(p);
                            final String arg0 = Resolver.string(cntx, mode);
                            switch (arg0) {
                                case "on" -> {
                                    p.setAllowFlight(true);
                                    // p.setFlying(true);
                                    p.sendMessage("§6" + Lang.t(p, "Режим полёта включен!"));
                                    return 0;
                                }
                                case "off" -> {
                                    p.setFlying(false);
                                    p.setAllowFlight(false);
                                    p.sendMessage("§6" + Lang.t(p, "Режим полёта выключен!"));
                                    return 0;
                                }
                            }
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }

    @Override
    public Set<String> aliases() {
        return Set.of();
    }

    @Override
    public String description() {
        return "Управлять режимом полёт";
    }

}
    
    
 
