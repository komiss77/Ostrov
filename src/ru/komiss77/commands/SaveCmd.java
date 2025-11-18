package ru.komiss77.commands;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import com.mojang.brigadier.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.tools.OCmdBuilder;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.world.*;
import ru.komiss77.utils.TCUtil;


public class SaveCmd {

    public SaveCmd() {
        final String schem = "id";
        new OCmdBuilder("svm", "/svm [schem]").then(Resolver.string(schem)).suggest(cntx -> {
                final File schems = new File(Schematic.DEF_PATH);
                return schems.exists() && schems.isDirectory() ? Arrays.stream(schems.listFiles(Schematic.FILTER))
                    .map(f -> f.getName().replaceFirst(Schematic.DEF_EXT, "")).collect(Collectors.toSet()) : Set.of();
            }, true).run(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player pl)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                if (!ApiOstrov.isLocalBuilder(pl, true)) {
                    pl.sendMessage("§cДоступно только билдерам!");
                    return 0;
                }

                if (!Cfg.savematics) {
                    pl.sendMessage("§6Сохраматики выключены!");
                    return 0;
                }

                final Schematic sch = WE.getSchematic(pl, Resolver.string(cntx, schem));
                if (sch == null) {
                    pl.sendMessage("схематик=null!");
                    return 0;
                }

                final BVec loc = BVec.of(pl);
                final Cuboid cb = sch.getCuboid().allign(loc);
                final int id = SaveMaticManager.save(cb, pl.getWorld(), () -> sch.paste(pl, loc, false));

                pl.sendMessage(TCUtil.form("§aСхематика §6" + sch.getName() +
                    " §aпоставлена, предыдущая терра сохранена как §e" + SaveMaticManager.PRFX + id));

                return Command.SINGLE_SUCCESS;
            })
            .description("Вставить тест сохраматику")
            .register(Ostrov.mgr);
    }
}