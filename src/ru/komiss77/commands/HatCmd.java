package ru.komiss77.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.Config;
import ru.komiss77.modules.warp.WarpMenu;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.inventory.SmartInventory;


public class HatCmd implements CommandExecutor {


    
    
    @Override
    public boolean onCommand ( CommandSender se, Command comandd, String cmd, String[] a) {
        
        
        
        if ( !(se instanceof Player) ) {
            se.sendMessage("§4команда только от игрока!"); 
            return false; 
        }
        if (!Config.getConfig().getBoolean("modules.command.hat")) {
            se.sendMessage("§6На этом сервере команда недоступна!"); 
            return false; 
        }
        
        Player p= (Player) se;
                
        if ( !p.hasPermission("ostrov.hat") ) {
            p.sendMessage("§6Нужно право ostrov.hat!"); 
            return false; 
        }
           
        if ( p.getInventory().getHelmet()!=null) {
            p.sendMessage("§6Сначала нужно снять шлем!"); 
            return false; 
        }
        
        final ItemStack is = p.getInventory().getItemInMainHand();
           
       if ( is.getType().isAir() ||  !is.getType().isBlock()) {
            p.sendMessage("§6Возьмите одеваемый блок в руку!"); 
            return false; 
        }
           
       if ( !is.getType().isBlock()) {
            p.sendMessage("§6Одеть можно только блок!"); 
            return false; 
        }
           
        if ( is.getAmount()>1 ) {
            p.sendMessage("§6Одеть можно только отдельный блок (колл-во =1)!"); 
            return false; 
        }
           
           
        p.getInventory().setHelmet(is);
        p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        
        //if (Ostrov.langUtils) {
            p.sendMessage(Component.text("§aВы одели ").append(Lang.t(is.getType(), p )).append(Component.text(" на голову!"))); 
        //} else {
        //    p.sendMessage("§aВы одели "+p.getInventory().getHelmet().getType().name()+" на голову!"); 
        //}
            
             

                
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
