package ru.komiss77.commands;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import ru.komiss77.Config;
import ru.komiss77.Ostrov;




public class Example implements Listener, CommandExecutor, TabCompleter {
    
    public static List<String> subCommands = Arrays.asList( "help");
    
    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] args) {
        
        List <String> sugg = new ArrayList<>();
//System.out.println("l="+strings.length+" 0="+strings[0]);




        switch (args.length) {
            
            case 1:
                //0- пустой (то,что уже введено)
                for (String s : subCommands) {
                    if (s.startsWith(args[0])) sugg.add(s);
                }
                
                // if (ApiOstrov.isLocalBuilder(cs, false)){
                //     for (String s : adminCommands) {
                //         if (s.startsWith(strings[0])) sugg.add(s);
                //     }
                //  }
                break;
                
            case 2:
                //if (strings[0].equalsIgnoreCase("create") ) {
                //  sugg.addAll(plugin.kits.keySet());
                //}
                break;
                
            case 3:
                //if (strings[0].equalsIgnoreCase("create") ) {
                //}
                break;

        }
        
       return sugg;
    }
       
    
    

    public Example() {
        init();
    }
    
    private void help(final Player p) {
        p.sendMessage(Component.text("§3/"+this.getClass().getSimpleName()+" <ник> - §7  §8<<клик")
        	.hoverEvent(HoverEvent.showText(Component.text("§aКлик - набрать")))
        	.clickEvent(ClickEvent.suggestCommand("/ ")));
    }

    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( ! (cs instanceof Player) ) {
            if (arg.length==1 && arg[0].equals("reload")) {
                reload();
            } else {
                cs.sendMessage("§e/"+this.getClass().getSimpleName()+" reload §7- перезагрузить настройки команды");
            }
            return true;
        }
        
        final Player p=(Player) cs;
        
        //if (!allow_command) {
        //    p.sendMessage( "");
        //    return true;
        //}
            
            switch (arg.length) { 
                
                case 0:
                    
                    break;
                    
                case 1:
                    
                    break;
            }

        help(p);
        return true;
    }
    



    
    
    
    

    public void init() {
        try {
     
            //if (!allow_command) {
             //   Ostrov.log_ok ("§e"+this.getClass().getSimpleName()+" выключен.");
            //    return;
            //}
            
            //Bukkit.getPluginManager().registerEvents(this, Ostrov.getInstance());
            
            Ostrov.log_ok ("§2"+this.getClass().getSimpleName()+" активен!");
            
        } catch (Exception ex) { 
            Ostrov.log_err("§4Не удалось загрузить настройки "+this.getClass().getSimpleName()+" : "+ex.getMessage());
        }
    }

    public void reload () {
        //HandlerList.unregisterAll(this);
        Config.loadConfigs();
        init();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
/*    
@EventHandler(priority = EventPriority.MONITOR) 
    public void onDataRecieved (BungeeDataRecieved e) {
           
        
    }
    
    
@EventHandler(priority = EventPriority.MONITOR) 
    public void onQuit (PlayerQuitEvent e) {
           
        
    }
*/

    
    
    
    
    
    
    
    
    


}
    
    
 
