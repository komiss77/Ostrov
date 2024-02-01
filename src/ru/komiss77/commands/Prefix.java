package ru.komiss77.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.TCUtils;


public class Prefix implements CommandExecutor {


    
    
    @Override
    public boolean onCommand ( CommandSender se, Command comandd, String cmd, String[] arg) {
        
        if ( !(se instanceof final Player p) ) {
            se.sendMessage("§4команда только от игрока!"); 
            return false; 
        }
        final Oplayer op = PM.getOplayer(p);
        
        if ( !p.hasPermission("ostrov.prefix") ) {
            p.sendMessage("§6Нужно право ostrov.prefix!"); 
            return false; 
        }
                    

        if (arg.length>=1) {
            final String prefix=arg[0].replaceAll("&k", "").replaceAll("&u", "").replaceAll("&", "§");
            /*if (prefix.length()>32) {
                p.sendMessage(Component.text(Ostrov.PREFIX+"§cПрефикс не может содержать более 32 символов!"));
                return true;
            }*/
            if (TCUtils.stripColor(prefix).length()>8) {
                p.sendMessage(TCUtils.format(Ostrov.PREFIX+"§cПрефикс не может превышать 8 символов (цвета не учитываются)."));
                return true;
            }
            op.setData(Data.PREFIX, prefix);
            p.sendMessage(TCUtils.format(Ostrov.PREFIX+"Твой новый префикс: " + prefix));
        }
                
        return true;
    }
    
    
    
    

    
    
    
    
    
    
    
    
}
