package ru.komiss77.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.warp.WarpMenu;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.SmartInventory;


public class Suffix implements CommandExecutor {


    
    @Override
    public boolean onCommand ( CommandSender se, Command comandd, String cmd, String[] arg) {
        
        if ( !(se instanceof Player) ) {
            se.sendMessage("§4команда только от игрока!"); 
            return false; 
        }
        Player p= (Player) se;
        final Oplayer op = PM.getOplayer(p);
        
        if ( !p.hasPermission("ostrov.prefix") ) {
            p.sendMessage("§6Нужно право ostrov.prefix!"); 
            return false; 
        }
                    

        if (arg.length>=1) {
            String suffix=arg[0];
            suffix=suffix.replaceAll("&k", "").replaceAll("&u", "").replaceAll("&", "§");
            if (suffix.length()>32) {
                p.sendMessage(Component.text(Ostrov.PREFIX+"§cСуффикс не может содержать более 32 символов!"));
                return true;
            }
            if (TCUtils.stripColor(suffix).length()>8) {
                p.sendMessage(Component.text(Ostrov.PREFIX+"§cдлинна суффикса не может превышать 8 символов (цветовые коды не учитываются)."));
                return true;
            }
            op.setData(Data.PREFIX, suffix);
            p.sendMessage(Component.text(Ostrov.PREFIX+"Ваш новый суффикс: " + suffix ));
        }
                
        return true;
    }
    
    
    
    
    
    
    public static void openMenu(final Player p) {
        SmartInventory.builder()
            .id("WarpMenu"+p.getName())
            .provider(new WarpMenu())
            .size(6, 9)
            .title("§fМеста")
            .build()
            .open(p);
    }
  
    
    
    
    
    
    
    
    
}
