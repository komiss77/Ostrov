package ru.komiss77.commands;

import java.util.Set;
import com.mojang.brigadier.Command;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.tools.OCmdBuilder;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.enums.Module;
import ru.komiss77.modules.entities.PvPManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.SmartInventory;

public final class PvpCmd {

    //запрос банжи, если есть - разкодировать raw
    //если пустой - выкачать из снапшота БД

    public PvpCmd() { //новое
        final String act = "action";
        new OCmdBuilder("pvp", "/pvp <on | off>").run(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof final Player pl)) {
                cs.sendMessage("§eНе консольная команда!");
                return 0;
            }

            if (!PvPManager.getFlag(PvPManager.PvpFlag.allow_pvp_command)) {
                pl.sendMessage("§cУправление режимом ПВП отключено!");
                return 0;
            }

            final Component msg;
            if (PM.getOplayer(pl).pvp_allow) {
                msg = TCUtil.form(Lang.t(pl, "<gray>Сейчас ПВП <dark_red>Разрешен<gray>  <gold>[<gray>Клик - <green>ВЫКЛЮЧИТЬ<gold>]"))
                    .hoverEvent(HoverEvent.showText(Component.text("Клик - выключить")))
                    .clickEvent(ClickEvent.runCommand("/pvp off"));//Component.text("Сейчас ПВП ", NamedTextColor.GRAY)
            } else {
                msg = TCUtil.form(Lang.t(pl, "<gray>Сейчас ПВП <green>Запрещён<gray> <gold>[<gray>Клик - <dark_red>ВКЛЮЧИТЬ<gold>]"))
                    .hoverEvent(HoverEvent.showText(Component.text("Клик - включить")))
                    .clickEvent(ClickEvent.runCommand("/pvp on"));//Component.text("Сейчас ПВП ", NamedTextColor.GRAY)
            }
            pl.sendMessage(msg);//p.sendMessage("§2ПВП выключен!");
            return Command.SINGLE_SUCCESS;
        }).then(Resolver.integer(act)).suggest(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof final Player pl)) return Set.of();
            if (!ApiOstrov.isStaff(pl)) return Set.of("on", "off");
            return Set.of("on", "off", "setup", "reload");
        }, true).run(cntx -> {
            final CommandSender cs = cntx.getSource().getSender();
            if (!(cs instanceof final Player pl)) {
                cs.sendMessage("§eНе консольная команда!");
                return 0;
            }

            final Oplayer op = PM.getOplayer(pl);
            switch (Resolver.string(cntx, act)) {
                case "on":
                    if (!PvPManager.getFlag(PvPManager.PvpFlag.allow_pvp_command)) {
                        pl.sendMessage(Lang.t(pl, "§cУправление режимом ПВП отключено!"));
                        return 0;
                    }
                    PvPManager.pvpOn(op);
                    pl.sendMessage(Lang.t(pl, "§4ПВП включен!"));
                    return Command.SINGLE_SUCCESS;
                case "of":
                    if (!PvPManager.getFlag(PvPManager.PvpFlag.allow_pvp_command)) {
                        pl.sendMessage(Lang.t(pl, "§cУправление режимом ПВП отключено!"));
                        return 0;
                    }
                    if (PvPManager.isForced(pl, op, true)) return 0;
                    PvPManager.pvpOff(op);
                    pl.sendMessage(Lang.t(pl, "§2ПВП выключен!"));
                    return Command.SINGLE_SUCCESS;
                case "reload":
                    if (ApiOstrov.isLocalBuilder(cs, true)) {
                        Ostrov.getModule(Module.pvp).reload();
                        pl.sendMessage("§aНастройки ПВП режима загружены из файла pvp.yml");
                    }
                    return Command.SINGLE_SUCCESS;
                case "setup":
                    if (ApiOstrov.isLocalBuilder(cs, true)) {
                        SmartInventory.builder()
                            .id("PVPsetup" + pl.getName())
                            .provider(new PvPManager.PvpSetupMenu())
                            .size(6, 9)
                            .title("§fНастройки ПВП режима")
                            .build()
                            .open(pl);
                    }
                    return Command.SINGLE_SUCCESS;
                default:
                    return 0;
            }
        }).description("Вкл/Выкл ПВП Режим").register();
    }
}
