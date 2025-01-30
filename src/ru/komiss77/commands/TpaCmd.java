package ru.komiss77.commands;

import java.util.List;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Cfg;
import ru.komiss77.Timer;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.TCUtil;


public class TpaCmd implements OCommand {

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {

        final String target = "target";

        return Commands.literal("tpa").executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player p)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }
                    PM.getOplayer(p).menu.openTPAsection(p); //без параметров
                    return Command.SINGLE_SUCCESS;
                })

                //c вводом имени и запросом на ТП
                .then(Resolver.string(target).suggests((cntx, sb) -> {
                            final CommandSender cs = cntx.getSource().getSender();
                            Bukkit.getOnlinePlayers().stream()
                                    .filter(pl -> !pl.getName().equals(cs.getName()) && pl.getGameMode() != GameMode.SPECTATOR)
                                    .forEach(p -> sb.suggest(p.getName()));
                            return sb.buildFuture();
                        })
                        .executes(cntx -> {
                            final CommandSender cs = cntx.getSource().getSender();
                            if (!(cs instanceof final Player p)) {
                                cs.sendMessage("§eНе консольная команда!");
                                return 0;
                            }
                            final String targetName = Resolver.string(cntx, target);
                            if (p.getName().equals(targetName)) {
                                p.sendMessage("§cВы не можете ТП к самому себе");
                                return 0;
                            }

                            final boolean moder = p.hasPermission("ostrov.tpo");

//System.out.println("tpa1 moder?"+moder+" tpa_command="+Config.tpa_command);
                            if (Cfg.tpa_command_delay < 0 && !moder) {
                                p.sendMessage("§cКоманда отключена");
                                return 0;
                            }

                            //задержка даётся вызывающему
                            if (Timer.has(p, "tpa_command")) { //для модеров никогда не сработает - не добавляет в таймер
                                p.sendMessage("§cТелепортер перезаряжается. §7Осталось " + Timer.getLeft(p, "tpa_command") + " сек.!");
                                return 0;
                            }
                            final Oplayer op = PM.getOplayer(p);
                            if (op.isBlackListed(targetName)) {
                                p.sendMessage("§c" + targetName + " у вас в чёрном списке!");
                                return 0;
                            }

                            final Player targetPlayer = Bukkit.getPlayerExact(targetName);
                            final Oplayer targetOp = PM.getOplayer(targetName);

                            if (targetPlayer == null || targetOp == null) {
                                p.sendMessage("§cНет на этом сервере!");
                                return 0;
                            }

                            if (targetOp.isBlackListed(p.getName())) {
                                p.sendMessage("§cВы в чёрном списке у " + targetName + "!");
                                return 0;
                            }

                            if (Timer.has(targetPlayer, "tp_request_from_" + p.getName())) {
                                p.sendMessage("§cЗапрос уже отправлен!");
                                return 0;
                            }
                            Timer.add(targetPlayer, "tp_request_from_" + p.getName(), 15);
                            targetOp.tpRequestFrom = p.getUniqueId();

                            targetPlayer.sendMessage(TCUtil.form("§f<obf>11<!obf>§f Телепорт от §a" + p.getName() + "§f<obf>11<!obf> §2[>§aпринять§2<]")
                                    .hoverEvent(HoverEvent.showText(Component.text("§5Клик - принять")))
                                    .clickEvent(ClickEvent.runCommand("/tpaccept"))
                                    .append(Component.text(" §4[>§cв игнор§4<]")
                                            .hoverEvent(HoverEvent.showText(Component.text("§4Отправить " + p.getName() + " в игнор-лист.")))
                                            .clickEvent(ClickEvent.runCommand("/ignore add " + p.getName()))));

                            p.sendMessage("§6Запрос на телепорт " + targetPlayer.getName() + " отправлен, действетт 15сек.");


                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("tpo");
    }

    @Override
    public String description() {
        return "Телепорт к игрокам";
    }


}