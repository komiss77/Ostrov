package ru.komiss77.commands;

import java.util.List;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
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
import ru.komiss77.modules.world.LocFinder;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.StringUtil;
import ru.komiss77.utils.TCUtil;

@Deprecated
public class HomeCmd implements OCommand {

    //запрос банжи, если есть - разкодировать raw
    //если пустой - выкачать из снапшота БД

    @Override
    public LiteralCommandNode<CommandSourceStack> command() {
        final String name = "name";
        return Commands.literal("home")
            .executes(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player p)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                if (!Cfg.home_command) {
                    cs.sendMessage("§c" + Lang.t(p, "Дома отключены на этом сервере!"));
                    return 0;
                }

                final Oplayer op = PM.getOplayer(p);
                if (op.homes.isEmpty()) {
                    p.sendMessage("§c" + Lang.t(p, "У Вас нет дома! Установите его командой") + " /sethome");
                    return 0;
                } else if (op.homes.size() > 1) {
                    //p.sendMessage( "§bУ Вас несколько домов, выберите нужный: §6"+PM.OP_GetHomeList(p.name()).toString().replaceAll("\\[|\\]", "") );
                    final TextComponent.Builder homes = Component.text().content("§a" + Lang.t(p, "В какой дом вернуться? "));
                    for (final String homeName : op.homes.keySet()) {
                        homes.append(TCUtil.form("§b- §e" + homeName + " ")
                            .hoverEvent(HoverEvent.showText(TCUtil.form("§7" + Lang.t(p, "Клик - вернуться в точку дома") + " §6" + homeName)))
                            .clickEvent(ClickEvent.runCommand("/home " + homeName)));
                    }
                    p.sendMessage(homes.build());
                    return 0;
                }
                return Command.SINGLE_SUCCESS;
            })
            .then(Resolver.player(name)
                .suggests((cntx, sb) -> {
                    if (!(cntx.getSource().getExecutor()
                        instanceof final Player pl)) {
                        return sb.buildFuture();
                    }
                    PM.getOplayer(pl).homes.keySet();
                    return sb.buildFuture();
                }).executes(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player p)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }

                        if (!Cfg.home_command) {
                        cs.sendMessage("§c" + Lang.t(p, "Дома отключены на этом сервере!"));
                        return 0;
                    }

                    final Oplayer op = PM.getOplayer(p);
                    final String home = Resolver.string(cntx, name);

                    if (op.homes.containsKey(home)) {

                        final Location homeLoc = LocUtil.stringToLoc(op.homes.get(home), false, true);
                        if (homeLoc != null) {

                            if (!homeLoc.getChunk().isLoaded()) {
                                homeLoc.getChunk().load();
                            }

                            final WXYZ save = new LocFinder(new WXYZ(homeLoc)).find(LocFinder.DYrect.BOTH, 3, 1);
                            if (save != null) {
                                DelayTeleport.tp(p, save.getCenterLoc(), 5, "§2" + Lang.t(p, "Дом милый дом!"), true, true, DyeColor.YELLOW);
                                p.sendMessage("§4" + Lang.t(p, "Дома что-то случилось, некуда вернуться! Дух Острова перенёс Вас в ближайшее безопасное место."));
                                p.sendMessage("§c" + Lang.t(p, "Установите точку дома заново."));
                            } else {
                                p.sendMessage("§c" + Lang.t(p, "Дома что-то случилось, некуда вернуться! Вернитесь пешком, проверьте и установите точку дома заново."));
                                p.sendMessage("§c" + Lang.t(p, "Если Вы забыли где Ваш дом ") + home + " , " + Lang.t(p, "вот его координаты") + " x:" + (int) homeLoc.getBlockX() + ", y:" + (int) homeLoc.getBlockY() + ", z:" + (int) homeLoc.getBlockZ());
                            }

                        } else {
                            p.sendMessage("§c" + Lang.t(p, "Что-то пошло не так при получении координат."));
                        }

                    } else {

                        p.sendMessage("§c" + Lang.t(p, "Нет такого дома! Ваши дома:") + " §6" + StringUtil.listToString(op.homes.keySet(), ","));

                    }
                    return Command.SINGLE_SUCCESS;
                }))
            .build();
    }

    @Override
    public List<String> aliases() {
        return List.of("дом");
    }

    @Override
    public String description() {
        return "Телеппорт на дом";
    }
}
