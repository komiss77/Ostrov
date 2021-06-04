package ru.komiss77.Commands;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.HandlerList;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Cfg;
import ru.komiss77.Ostrov;
import ru.komiss77.Listener.PlayerListener;
import ru.komiss77.Managers.PM;
import ru.komiss77.utils.ItemUtils;
import builder.SetupMode;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.utils.ItemBuilder;




public class Builder implements CommandExecutor, TabCompleter {
    
    public static List<String> subCommands = Arrays.asList( "end");
    public static ItemStack openBuildMenu = new ItemBuilder(Material.MAP).name("§aМеню настройки SkyBlock").build();;
    public static final ItemStack fill = new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).build();;
    
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
       
    
    

    public Builder() {
        init();
    }
    
   /* private void help(final Player p) {
        p.spigot().sendMessage(
            new ComponentBuilder("§3/"+this.getClass().getSimpleName()+" <ник> - §7  §8<<клик")
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aКлик-набрать").create()))
            .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ "))
            .create()
        );
    }*/

    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( ! (cs instanceof Player) ) {
            cs.sendMessage("§eне консольная команда!");
            return true;
        }
        
        final Player p=(Player) cs;
        
        //if (!allow_command) {
        //    p.sendMessage( "");
        //    return true;
        //}
        if (!ApiOstrov.canBeBuilder(cs)) {
            p.sendMessage( "§сНужно право §e"+Bukkit.getServer().getMotd()+".builder"+" §cили группа §esupermoder");
            return true;
        }
        
        
        
        
        switch (arg.length) {

            case 0:
                Ostrov.sync( ()-> {
                    if (p.getGameMode()==GameMode.SURVIVAL || p.getGameMode()==GameMode.ADVENTURE) {
                        p.performCommand("gm 1");
                        p.setAllowFlight(true);
                        p.setFlying(true);
                    }
                    p.getInventory().setItem(0, openBuildMenu.clone());
                    p.updateInventory();
                    if (PM.getOplayer(p).setup==null) {
                        final SetupMode sm = new SetupMode(p);
                        PM.getOplayer(p).setup = sm;
                        Bukkit.getPluginManager().registerEvents(sm, Ostrov.GetInstance());
                    }
                    PM.getOplayer(p).setup.openMainSetupMenu(p);
                }, 10);
                break;

            case 1:
                if (arg[0].equalsIgnoreCase("end")) {
                    end(p.getName());
                }
                break;
        }

       // help(p);
        return true;
    }
    



    
    
    
    

    public void init() {
        try {
     
            //if (!allow_command) {
             //   Ostrov.log_ok ("§e"+this.getClass().getSimpleName()+" выключен.");
            //    return;
            //}
            
            //Bukkit.getPluginManager().registerEvents(this, Ostrov.GetInstance());
            
            Ostrov.log_ok ("§2"+this.getClass().getSimpleName()+" активен!");
            
        } catch (Exception ex) { 
            Ostrov.log_err("§4Не удалось загрузить настройки "+this.getClass().getSimpleName()+" : "+ex.getMessage());
        }
    }

    public void reload () {
        //HandlerList.unregisterAll(this);
        Cfg.LoadConfigs();
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

    public static void end(final String name) {
        final Player p = Bukkit.getPlayer(name);
        if (p!=null && p.isOnline()) {
            p.setGameMode(GameMode.SURVIVAL);
            p.closeInventory();
            ItemUtils.substractAllItems(p, openBuildMenu.getType());
        }
        PlayerListener.signCache.remove(name);
        if (PM.getOplayer(name).setup!=null) {
            HandlerList.unregisterAll(PM.getOplayer(name).setup);
            PM.getOplayer(name).setup = null;
        }
    }

    
    
    
    
    
    
    
    
    


}
    
    
 