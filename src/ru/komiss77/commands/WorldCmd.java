package ru.komiss77.commands;


import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.modules.world.WorldSetupMenu;
import ru.komiss77.utils.inventory.SmartInventory;




public class WorldCmd implements Listener, CommandExecutor {
    

    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( ! (cs instanceof Player) ) {
            cs.sendMessage("§cНе консольная команда!");
            return true;
        }
        final Player p = (Player) cs;
        
        if (ApiOstrov.isLocalBuilder(cs, false)) {
            SmartInventory.builder()
                .id("Worlds"+p.getName())
                .provider(new WorldSetupMenu())
                .size(6, 9)
                .title("§2Миры сервера")
                .build().open(p);
            return true;
        }
        
        if ( Config.world_command ) {
            if ( p.hasPermission("ostrov.world")) {
                SmartInventory.builder()
                .id("Worlds"+p.getName())
                .provider(new WorldSetupMenu())
                .size(3, 9)
                .title("§2Миры сервера")
                .build().open(p);
            } else {
                p.sendMessage("§cУ Вас нет пава ostrov.world !");
            }
        } else {
            p.sendMessage( "§cСмена мира командой world отключён на этом сервере!");
        }
        //if (!ApiOstrov.isLocalBuilder(cs, true)) return false;


        return true;
    }
    



    
    
    

    
    
    
    
    
    
    
    
    









    
    
    


}
    
    
 
