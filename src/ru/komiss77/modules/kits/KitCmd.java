
package ru.komiss77.modules.kits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;





public class KitCmd implements CommandExecutor, TabCompleter {

    
    public static List<String> subCommands = Arrays.asList( "gui", "buyacces", "give", "sellacces", "admin");

    
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] strings) {
        List <String> sugg = new ArrayList<>();
//System.out.println("l="+strings.length+" 0="+strings[0]);
       
            if (strings.length==1) {  //0- пустой (то,что уже введено)
                for (String s : subCommands) {
                    if (s.startsWith(strings[0])) sugg.add(s);
                }

            } else if (strings.length==2) {  //1-то,что вводится (обновляется после каждой буквы
//System.out.println("l="+strings.length+" 0="+strings[0]+" 1="+strings[1]);
                if (strings[0].equalsIgnoreCase("buyacces") ||
                        strings[0].equalsIgnoreCase("give") ||
                        strings[0].equalsIgnoreCase("sellacces") 
                        ) {
                    for (String kitName : KitManager.getKitsNames()) {
                        if (kitName.toLowerCase().startsWith(strings[1].toLowerCase())) sugg.add(kitName);
                    }
                } 
                
            } /*else if (strings.length==3) {
                if (strings[0].equalsIgnoreCase("create")) {
                    for (WorldType type : WorldType.values()) {
                        sugg.add(type.toString());
                    }
                } 
            }*/
       return sugg;
    }

    
    
    


    
    @Override
    public boolean onCommand(CommandSender se, Command comandd, String cmd, String[] arg) {
        
        if (!Config.getConfig().getBoolean("modules.command.kit")) {
            se.sendMessage("§4Наборы отключены на этом сервере!");
            return true; 
        }
        
        Player p = null;
        if ( se instanceof Player) p = (Player) se;
        
        

        switch (arg.length) {
            
            
            case 0:
                if (p==null) {
                    se.sendMessage("§4Это не консольная команда!");
                    return false;
                }
                KitManager.openGuiMain(p);
                return false;
                
                
            case 1:
                if ( arg[0].equalsIgnoreCase("gui")) {
                    KitManager.openGuiMain(p);
                    return true;
                }
                if ( arg[0].equalsIgnoreCase("admin")) {
                    if (ApiOstrov.isLocalBuilder(se, true)) {
                        KitManager.openKitEditMain(p);
                    }
                    return true;
                }
                if ( arg[0].equalsIgnoreCase("buyacces") || arg[0].equalsIgnoreCase("give") || arg[0].equalsIgnoreCase("sellacces") ) {
                    p.sendMessage("§cУкажите название набора!");
                    return true;
                }
                se.sendMessage("§c/kit <gui/buyacces/give/sellacces> <название>");
                return false;
                
                
            case 2:
                if ( arg[0].equalsIgnoreCase("buyacces")) {
                    KitManager.buyKitAcces(p, arg[1].toLowerCase() );
                    return true;
                }
                if ( arg[0].equalsIgnoreCase("give")) {
                    KitManager.tryGiveKit(p, arg[1].toLowerCase() );
                    return true;
                }
                if ( arg[0].equalsIgnoreCase("sellacces")) {
                    KitManager.trySellAcces(p, arg[1].toLowerCase() );
                    return true;
                }
                se.sendMessage("§c/kit <gui/buyacces/give/sellacces> <название>");
                break;
                
                
            default:
                se.sendMessage("§c/kit <gui/buyacces/give/sellacces> <название>");
                break;
        }
        
         return true;
         
    }

    
    

    
    
    
    
    


}
   
    
    
    
    
    
    
    
    
