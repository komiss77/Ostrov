package ru.komiss77.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.listener.NbtLst;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;


@Deprecated
public class NbtfindCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if ((sender instanceof Player) && !Perm.isStaff(PM.getOplayer(sender.getName()), 4)) {
            sender.sendMessage("§cДоступно только модераторам!");
            return true;
        }


        if (args.length != 1) {
            sender.sendMessage("§cnbtfind [материал] (all-любой)");
            return true;
        }

        Material material = null;

        if (!args[0].equals("all")) material = Material.getMaterial(args[0].toUpperCase());

        if (material == null) {
            sender.sendMessage("§2Сканируем игроков на сервере ... (все предметы)");
        } else {
            sender.sendMessage("§2Сканируем игроков на сервере ... (материал: " + args[0].toUpperCase() + ")");
        }


        Bukkit.getOnlinePlayers().forEach(p -> {
            NbtLst.rebuildInventoryContent(p);
        });

        sender.sendMessage("§2Сканирование закончено!");

        return true;
    }


}
