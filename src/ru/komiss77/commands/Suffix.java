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


public class Suffix implements CommandExecutor {


    
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
            final String suffix=arg[0].replaceAll("&k", "").replaceAll("&u", "").replaceAll("&", "§");
            /*if (suffix.length()>32) {
                p.sendMessage(Component.text(Ostrov.PREFIX+"§cСуффикс не может содержать более 32 символов!"));
                return true;
            }*/
            if (TCUtils.stripColor(suffix).length()>8) {
                p.sendMessage(TCUtils.format(Ostrov.PREFIX+"§cСуффикс не может превышать 8 символов (цвета не учитываются)."));
                return true;
            }
            op.setData(Data.SUFFIX, suffix);
            p.sendMessage(TCUtils.format(Ostrov.PREFIX+"Твой новый суффикс: " + suffix));
        }
                
        return true;
    }
    
    
    
    
    
    
    
}
