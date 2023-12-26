package ru.komiss77.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.komiss77.builder.menu.ModerInv;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.inventory.SmartInventory;

public class ModerCmd implements CommandExecutor {

	@Override
	public boolean onCommand(final CommandSender cs, final Command cmd, final String label, final String[] args) {

            if ( ! (cs instanceof Player) ) {
                cs.sendMessage("§eне консольная команда!");
                return true;
            }
            
            final Oplayer op = PM.getOplayer(cs.getName());
            if (op.hasGroup("moder") 
                || op.hasGroup("moder_spy") 
                || op.hasGroup("cerber") 
                || op.hasGroup("supermoder")
                || op.hasGroup("xpanitely")
                || op.hasGroup("owner") )
            {
            	SmartInventory.builder().id("Moder " + cs.getName()).provider(new ModerInv()).size(3, 9).title("§aМеню Модера").build().open((Player) cs);
                return true;
            }
                cs.sendMessage("§cУ вас нету разрешения на это!");
            //if (send instanceof Player && label.equalsIgnoreCase("moder")) {
	//		return true;
	//	}
	//	send.sendMessage("§cУ вас нету разрешения на это!");
		return false;
	}
}
