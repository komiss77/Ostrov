package ru.komiss77.commands;

import java.util.Set;
import com.mojang.brigadier.Command;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.commands.tools.OCmdBuilder;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.translate.Lang;


public class GmCmd {

    public GmCmd() { //новое
        final String mode = "mode";

        new OCmdBuilder("gm", "/gm [режим]")
            .run(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player p)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }
                if (!Cfg.gm_command && (!ApiOstrov.isStaff(cs) && !ApiOstrov.canBeBuilder(cs))) {
                    p.sendMessage("§c" + Lang.t(p, "Gm отключёна на этом сервере!"));
                    return 0;
                }
                switch (p.getGameMode()) {
                    case CREATIVE:
                        p.setGameMode(GameMode.SURVIVAL);
                        p.sendMessage("§e" + Lang.t(p, "Установлено выживание!"));
                        break;
                    case SURVIVAL:
                        p.setGameMode(GameMode.CREATIVE);
                        p.sendMessage("§e" + Lang.t(p, "Установлен креатив!"));
                        break;
                    case ADVENTURE:
                        p.setGameMode(GameMode.CREATIVE);
                        p.sendMessage("§e" + Lang.t(p, "Установлен креатив!"));
                        break;
                    case SPECTATOR:
                        p.setGameMode(GameMode.SURVIVAL);
                        p.sendMessage("§e" + Lang.t(p, "Установлено выживание!"));
                        break;
                }
                return Command.SINGLE_SUCCESS;
            })


            .then(Resolver.string(mode))
            .suggest(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (ApiOstrov.isStaff(cs) || ApiOstrov.canBeBuilder(cs)) {
                    return Set.of("0", "1", "2", "3", "sv", "cr", "ad", "sp");
                }
                return Set.of();
            }, true)

            .run(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player p)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }
                if (!Cfg.gm_command && (!ApiOstrov.isStaff(cs) && !ApiOstrov.canBeBuilder(cs))) {
                    p.sendMessage("§c" + Lang.t(p, "Gm отключёна на этом сервере!"));
                    return 0;
                }
                switch (Resolver.string(cntx, mode)) {
                    case "0", "sv" ->
                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode survival " + p.getName());
                    case "1", "cr" ->
                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode creative " + p.getName());
                    case "2", "ad" ->
                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode adventure " + p.getName());
                    case "3", "sp" ->
                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode spectator " + p.getName());
                }
                return Command.SINGLE_SUCCESS;
            })
            .description("Меняет режим игры")
            .aliases("")
            .register();
    }

}
    
    
 
