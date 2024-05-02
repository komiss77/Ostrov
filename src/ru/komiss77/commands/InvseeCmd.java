package ru.komiss77.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.Ostrov;

import java.util.Arrays;
import java.util.List;

public class InvseeCmd implements CommandExecutor, TabCompleter {

  @Override
  public List<String> onTabComplete(final @NotNull CommandSender se, final @NotNull Command cmd, final @NotNull String label, final String[] args) {
    if (se instanceof final Player p) {
      if (p.hasPermission("ostrov.invact")) {
        if (args.length == 2) {
          return Arrays.asList("main", "ender", "extra");
        }
      }
    }
    return null;
  }

  @Override
  public boolean onCommand (final @NotNull CommandSender se, final @NotNull Command cmd, final @NotNull String label, final String[] args) {
    if (se instanceof final Player pl) {
      if (se.hasPermission("ostrov.invact")) {
        final Player opl;
        switch (args.length) {
          case 2:
            opl = Bukkit.getPlayerExact(args[0]);
            if (opl == null) {
              se.sendMessage(Ostrov.PREFIX + "§cИгрок " + args[0] + " не онлайн!");
              return false;
            }

            final Inventory inv;
            switch (args[1]) {
              case "main":
                inv = opl.getInventory();
                break;
              case "ender":
                inv = opl.getEnderChest();
                break;
              case "extra":
              default:
                se.sendMessage(Ostrov.PREFIX + "§cНеправильный синтакс комманды!");
                return false;
            }

            pl.openInventory(inv);
            opl.sendMessage(Ostrov.PREFIX + pl.getName() + " §aпросматривает твой инвентарь!");
            break;
          case 1:
            opl = Bukkit.getPlayerExact(args[0]);
            if (opl == null) {
              se.sendMessage(Ostrov.PREFIX + "§cИгрок " + args[0] + " не онлайн!");
              return false;
            }

            pl.openInventory(opl.getInventory());
            opl.sendMessage(Ostrov.PREFIX + pl.getName() + " §aпросматривает твой инвентарь!");
            break;
          default:
            break;
        }
      } else {
        se.sendMessage("§cУ Вас нет права ostrov.invact");
      }
    }
    return true;
  }

}
