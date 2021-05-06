package ru.komiss77.Commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.komiss77.Managers.PM;








public class Profile implements CommandExecutor {

    
    
    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] a) {
        if ( ! (cs instanceof Player) ) {
            cs.sendMessage("§cНе консольная команда!");
            return true;
        }
        final Player p=(Player) cs;
//System.out.println("Profile.onCommand()");
        p.openInventory(PM.getOplayer(p.getName()).profile);
        p.playSound(p.getLocation(), Sound.BLOCK_COMPOSTER_EMPTY, 2, 2);
        return true;
    }
    
    
    
    
    
    
    
    
}
