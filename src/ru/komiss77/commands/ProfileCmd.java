package ru.komiss77.commands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.Section;


public class ProfileCmd implements CommandExecutor {

    
    
    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] a) {
        if ( ! (cs instanceof final Player p) ) {
            cs.sendMessage("§cНе консольная команда!");
            return true;
        }
        final Oplayer op = PM.getOplayer(p);
//System.out.println("Profile.onCommand()");
        //p.openInventory(PM.getOplayer(p.getName()).profile);
        if (op.menu==null) {
            p.sendMessage("§eПодождите, данные ещё не получены..");
            return true;
        } else {
            op.menu.open(p, Section.ПРОФИЛЬ);
        }
        p.playSound(p.getLocation(), Sound.BLOCK_COMPOSTER_EMPTY, 2, 2);
        return true;
    }
    
    
    
    
    
    
    
    
}
