package ru.komiss77.commands;

import org.bukkit.Material;
import com.mojang.brigadier.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Cfg;
import ru.komiss77.commands.tools.OCmdBuilder;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.TCUtil;


public class HatCmd {

    public HatCmd() {
        new OCmdBuilder("hat").run(cntx -> {
                    final CommandSender cs = cntx.getSource().getSender();
                    if (!(cs instanceof final Player pl)) {
                        cs.sendMessage("§eНе консольная команда!");
                        return 0;
                    }
                    if (!Cfg.getConfig().getBoolean("modules.command.hat")) {
                        cs.sendMessage("§6На этом сервере команда недоступна!");
                        return 0;
                    }

                    if (!pl.hasPermission("ostrov.hat")) {
                        pl.sendMessage("§6Нужно право ostrov.hat!");
                        return 0;
                    }

                    if (pl.getInventory().getHelmet() != null) {
                        pl.sendMessage("§6Сначала нужно снять шлем!");
                        return 0;
                    }

                    final ItemStack is = pl.getInventory().getItemInMainHand();

                    if (is.getType().isAir() || !is.getType().isBlock()) {
                        pl.sendMessage("§6Возьмите одеваемый блок в руку!");
                        return 0;
                    }

                    if (!is.getType().isBlock()) {
                        pl.sendMessage("§6Одеть можно только блок!");
                        return 0;
                    }

                    if (is.getAmount() > 1) {
                        pl.sendMessage("§6Одеть можно только отдельный блок (колл-во =1)!");
                        return 0;
                    }


                    pl.getInventory().setHelmet(is);
                    pl.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

                    pl.sendMessage(TCUtil.form("§aВы одели ").append(Lang.t(is.getType(), pl)).append(TCUtil.form(" на голову!")));

                    return Command.SINGLE_SUCCESS;
                })
                .aliases("шляпа")
                .description("Надеть шляпу")
                .register();
    }
}