package ru.komiss77.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Config;
import ru.komiss77.Ostrov;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.warp.WarpMenu;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.SmartInventory;


public class Prefix implements CommandExecutor {


    
    
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
            String prefix=arg[0];
            prefix=prefix.replaceAll("&k", "").replaceAll("&u", "").replaceAll("&", "§");
            if (prefix.length()>32) {
                p.sendMessage(Component.text(Ostrov.PREFIX+"§cПрефикс не может содержать более 32 символов!"));
                return true;
            }
            if (TCUtils.stripColor(prefix).length()>8) {
                p.sendMessage(Component.text(Ostrov.PREFIX+"§cдлинна префикса не может превышать 8 символов (цветовые коды не учитываются)."));
                return true;
            }
            op.setData(Data.PREFIX, prefix);
            p.sendMessage(Component.text(Ostrov.PREFIX+"Ваш новый префикс: " + prefix ));
        }
                
        return true;
    }
    
    
    
    

    
    
    
    
    
    
    
    
}
