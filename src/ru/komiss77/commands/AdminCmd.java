package ru.komiss77.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.komiss77.builder.menu.AdminInv;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.inventory.SmartInventory;

public class AdminCmd implements CommandExecutor {

	@Override
	public boolean onCommand(final CommandSender cs, final Command cmd, final String label, final String[] args) {
        
        if ( !(cs instanceof Player) ) {
            cs.sendMessage("§eНе консольная команда!");
            return true;
        }
        
        final Oplayer op = PM.getOplayer(cs.getName());
        if (op.hasGroup("xpanitely") || op.hasGroup("owner")) {
        	SmartInventory.builder().id("Admin " + cs.getName()).provider(new AdminInv()).size(3, 9).title("§dМеню Абьюзера").build().open((Player) cs);
            return true;
        }
        
        cs.sendMessage("§cУ вас нету разрешения на это!");
		return false;
	}
}
