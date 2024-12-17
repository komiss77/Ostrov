package ru.komiss77.commands;

import java.util.stream.Collectors;
import com.mojang.brigadier.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.commands.tools.OCmdBuilder;
import ru.komiss77.commands.tools.Resolver;
import ru.komiss77.modules.items.ItemGroup;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;

public class IGroupCmd {

    public IGroupCmd() { //новое
        final String group = "group", type = "type";
        new OCmdBuilder("igroup", "/igroup [группа] <предмет>")
            .then(Resolver.string(group)).suggest(cntx -> ItemGroup.values().stream()
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
                final ItemGroup ig = ItemGroup.get(id);
                if (ig == null) {
                    pl.sendMessage(TCUtil.form(Ostrov.PREFIX + "<red>Такая группа не создана."));
                    return 0;
                }

                ItemUtil.giveItemsTo(pl, ig.items().toArray(new ItemStack[0]));
                pl.sendMessage(TCUtil.form(Ostrov.PREFIX + "Выданы предметы группы: " + id));
                return Command.SINGLE_SUCCESS;
            }).then(Resolver.string(type)).suggest(cntx -> {
                final String id = Resolver.string(cntx, group);

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

                final String id = Resolver.string(cntx, group);
                final ItemGroup ig = ItemGroup.get(id);
                if (ig == null) {
                    pl.sendMessage(TCUtil.form(Ostrov.PREFIX + "<red>Такая группа не создана."));
                    return 0;
                }

                ItemUtil.giveItemsTo(pl, ig.items().toArray(new ItemStack[0]));
                pl.sendMessage(TCUtil.form(Ostrov.PREFIX + "Выданы предметы группы: " + id));
                return Command.SINGLE_SUCCESS;
            })
            .description("Выдает предметы из группы")
            .register();
    }
}
