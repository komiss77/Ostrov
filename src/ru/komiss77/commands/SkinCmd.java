package ru.komiss77.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.hook.SkinRestorerHook;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;


public class SkinCmd implements CommandExecutor {


    
    @Override
    public boolean onCommand ( CommandSender se, Command comandd, String cmd, String[] arg) {
        
        if ( !(se instanceof final Player p) ) {
            se.sendMessage("§4команда только от игрока!"); 
            return false;
        }
        final Oplayer op = PM.getOplayer(p);
        
        if ( op.isGuest ) {
            p.sendMessage("§6Гости не могут менять скин!");
            return false; 
        }

        SkinRestorerHook.openGui (p, 0);
                
        return true;
    }
    
    
    
    
    
    
    
}
