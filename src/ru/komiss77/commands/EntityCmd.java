package ru.komiss77.commands;


import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.komiss77.ApiOstrov;
import ru.komiss77.builder.menu.EntityByWorld;
import ru.komiss77.builder.menu.EntityByWorlds;
import ru.komiss77.utils.inventory.SmartInventory;




public class EntityCmd implements Listener, CommandExecutor {
    

    
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String string, String[] arg) {
        
        if ( ! (cs instanceof Player) ) {
            cs.sendMessage("§cНе консольная команда!");
            return true;
        }
        final Player p = (Player) cs;
        

        if ( !p.hasPermission("ostrov.entity") && !ApiOstrov.isLocalBuilder(p, true)) {
            p.sendMessage("§cУ Вас нет пава ostrov.entity !");
        }


        if (arg.length==0) {
            SmartInventory
                .builder()
                .id("EntityMain"+p.getName())
                .provider(new EntityByWorld(p.getWorld(), -1))
                .size(3, 9)
                .title("§2Сущности "+p.getWorld()
                .getName())
                .build()
                .open(p);
            return true;
        }



        if ( arg.length==1) {

            if ( ApiOstrov.isInteger(arg[0])) {

                int r = Integer.parseInt(arg[0]);
                SmartInventory
                    .builder()
                    .id("EntityMain"+p.getName())
                    .provider(new EntityByWorld(p.getWorld(), r))
                    .size(3, 9)
                    .title("§2Сущности "+p.getWorld()
                    .getName()+" §1r="+r)
                    .build()
                    .open(p);
                return true;

            } else if (arg[0].equalsIgnoreCase("--server")) {

                if (!ApiOstrov.isLocalBuilder(p, false)) return false;
                final StringBuilder sb = new StringBuilder();
                sb.append("entity");
                for (final World w : Bukkit.getWorlds()) {
//System.out.println("entity --server world="+w);
                    sb.append(" ");
                    sb.append(w.getName());
                }
                p.performCommand(sb.toString());
                return true;
            }

        }


        final Set<World> worlds = new HashSet<>();
        for (String arg1 : arg) {
            final World world = Bukkit.getWorld(arg1);
            //System.out.println("++ i="+i+" arg="+arg[i]);
            if (world!=null) {
                worlds.add(world);
            }
            SmartInventory
                .builder()
                .id("EntityWorlds"+p.getName())
                .provider(new EntityByWorlds(worlds))
                .size(6, 9)
                .title("§2Сущности миров")
                .build()
                .open(p);
        }

        return true;
    }
    



    
    
    

    
    
    
    
    
    
    
    



}
    
    
 
