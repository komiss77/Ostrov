package ru.komiss77.Commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.menu.WarpMenu;
import ru.komiss77.modules.DelayTeleport;
import ru.komiss77.utils.inventory.SmartInventory;


public class WarpCmd implements CommandExecutor, TabCompleter {


    public WarpCmd() {
    }

    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] args) {
        
        List <String> sugg = new ArrayList<>();
        switch (args.length) {
            
            case 1:
                //0- пустой (то,что уже введено)
                for (final String s : ApiOstrov.getWarpManager().getWarpNames()) {
                    if (s.startsWith(args[0])) sugg.add(s);
                }

                break;


        }
        
       return sugg;
    }
       
    
    
    @Override
    public boolean onCommand ( CommandSender se, Command comandd, String cmd, String[] a) {
        
        //выполнение от имент консоли - игнор закрытости, права и оплаты
        if ( se instanceof ConsoleCommandSender ) {
            if (a.length==2) {
                if (ApiOstrov.getWarpManager().exist(a[0])) {
                    final Player target = Bukkit.getPlayer(a[1]);
                    if (target!=null) {
                        target.sendMessage( "§6Перемещаемся на "+a[0]+"..." );
                        DelayTeleport.tp(target, ApiOstrov.getWarpManager().getWarp(a[0]).loc, 5, "§6Перемещение на "+a[0]+" прошло удачно.", true, true, DyeColor.YELLOW);
                        return true;
                    } else {
                        se.sendMessage( "§cНе найден игрок "+a[1] );
                        return true;
                    }
                } else {
                    se.sendMessage( "§cТакого места не существует!" );
                    return true;
                }
                
            }
            se.sendMessage( "§cwarp §eник название" );
            return true;
        }
        
        
        
        if ( !(se instanceof Player) ) {
            se.sendMessage("§4команда только от игрока!"); 
            return false; 
        }
        
        
        Player p= (Player) se;
                
        //if (Ostrov.getWarpManager().сonsoleOnlyUse) {
           // if (ApiOstrov.isLocalBuilder(se, true)) {
           //     Ostrov.getWarpManager().openMenu(p);//se.sendMessage("§4Варпы командой отключены для игроков на этом сервере!");
          //  } else {
          //      se.sendMessage("§4Варпы командой отключены для игроков на этом сервере!");
         ///       return true;
         //   }
        //}
        
   
           
           
           
           
           
           
        switch (a.length) {
            
            
            case 0:
                openMenu(p);
                break;
                
                
                
            case 1:
                ApiOstrov.getWarpManager().tryWarp(p, a[0]);
                break;
                
                
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
