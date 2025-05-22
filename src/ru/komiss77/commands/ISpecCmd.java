package ru.komiss77.commands;

import java.util.stream.Collectors;
import com.mojang.brigadier.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.tools.OCmdBuilder;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.items.SpecialItem;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;

public class ISpecCmd {

    public ISpecCmd() { //новое
        final String group = "id";
        new OCmdBuilder("ispec", "/ispec [id]")
            .then(Resolver.string(group)).suggest(cntx -> SpecialItem.VALUES.values().stream()
                .map(g -> g.key().value()).collect(Collectors.toSet()), true).run(cntx -> {
                final CommandSender cs = cntx.getSource().getSender();
                if (!(cs instanceof final Player pl)) {
                    cs.sendMessage("§eНе консольная команда!");
                    return 0;
                }

                if (!ApiOstrov.isLocalBuilder(pl, true)) {
                    pl.sendMessage("§cДоступно только билдерам!");
                    return 0;
                }

                final String id = Resolver.string(cntx, group);
                final SpecialItem si = SpecialItem.VALUES.get(id);
                if (si == null) {
                    pl.sendMessage(TCUtil.form(Ostrov.PREFIX + "<red>Такой реликвии не существует."));
                    return 0;
                }

                ItemUtil.giveItemsTo(pl, si.item());
                si.obtain(pl, si.item());
                pl.sendMessage(TCUtil.form(Ostrov.PREFIX + "Выданы предметы группы: " + id));
                return Command.SINGLE_SUCCESS;
            }).description("Выдает Реликвии!").register(Ostrov.mgr);
    }
}
