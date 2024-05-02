package ru.komiss77.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Config;
import ru.komiss77.modules.DelayTeleport;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.utils.TeleportLoc;


public class HomeCmd implements CommandExecutor, TabCompleter {



    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String command, String[] args) {
        
        
        switch (args.length) {
            
            case 1:
                final Oplayer op = PM.getOplayer(cs.getName());
                //0- пустой (то,что уже введено)
                if (op!=null && op.homes.size()>1) {
                    return new ArrayList<>(op.homes.keySet());
                    //List <String> sugg = new ArrayList<>();
                    //for (final String s : op.homes.keySet()) {
                    //    if (s.startsWith(args[0])) sugg.add(s);
                    //}
                    //return sugg;
                }

                break;


        }
        
       return ImmutableList.of();
    }
       
    
    
    
    
    
    
    @Override
    public boolean onCommand ( CommandSender cs, Command c, String cmd, String[] arg) {
        
        
        if ( !(cs instanceof Player) ) {
            cs.sendMessage("§4команда только от игрока!"); 
            return false; 
        }
        final Player p= (Player) cs;
        if (!Config.home_command) {
            cs.sendMessage( "§c"+Lang.t(p, "Дома отключены на этом сервере!"));
            return false;
        }
        
        final Oplayer op = PM.getOplayer(p);
        //if (Ostrov.getWarpManager().сonsoleOnlyUse) {
           // if (ApiOstrov.isLocalBuilder(se, true)) {
           //     Ostrov.getWarpManager().openMenu(p);//se.sendMessage("§4Варпы командой отключены для игроков на этом сервере!");
          //  } else {
          //      se.sendMessage("§4Варпы командой отключены для игроков на этом сервере!");
         ///       return true;
         //   }
        //}
        
        
        String home = "home";
        
        if ( arg.length == 0) {
            if (op.homes.isEmpty()) {
                p.sendMessage( "§c"+Lang.t(p, "У Вас нет дома! Установите его командой")+" /sethome");
                return true;
            } else  if (op.homes.size()>1) {
            //p.sendMessage( "§bУ Вас несколько домов, выберите нужный: §6"+PM.OP_GetHomeList(p.getName()).toString().replaceAll("\\[|\\]", "") );
            	final TextComponent.Builder homes = Component.text().content("§a"+Lang.t(p, "В какой дом вернуться? "));
                for (final String homeName : op.homes.keySet()) {
                    homes.append(Component.text("§b- §e"+homeName+" ")
                		.hoverEvent(HoverEvent.showText(Component.text("§7"+Lang.t(p, "Клик - вернуться в точку дома")+" §6"+homeName)))
                		.clickEvent(ClickEvent.runCommand("/home " + homeName)));
                }
                p.sendMessage(homes.build());
                return true;
            }
        }
        
        
        if (arg.length == 1) {
            home = arg[0];
        }
        
        if ( op.homes.containsKey(home) ) {

            final Location homeLoc = ApiOstrov.locFromString(op.homes.get(home));
            if (homeLoc!=null) {

                if (!homeLoc.getChunk().isLoaded()) {
                   homeLoc.getChunk().load();
                }
                if (TeleportLoc.isSafeLocation(homeLoc) ) {
                    DelayTeleport.tp(p, homeLoc, 5, "§2"+Lang.t(p, "Дом милый дом!"), true, true, DyeColor.YELLOW);
                } else {
                    final Location saveLoc = TeleportLoc.findNearestSafeLocation(homeLoc,null);
                        if ( saveLoc != null) {
                            //p.teleport(loc2, PlayerTeleportEvent.TeleportCause.COMMAND);
                            DelayTeleport.tp(p, saveLoc, 5, "§2"+Lang.t(p, "Дом милый дом!"), true, true, DyeColor.YELLOW);
                            p.sendMessage( "§4"+Lang.t(p, "Дома что-то случилось, некуда вернуться! Дух Острова перенёс Вас в ближайшее безопасное место."));    
                            p.sendMessage( "§c"+Lang.t(p, "Установите точку дома заново."));    
                        } else {
                            p.sendMessage( "§c"+Lang.t(p, "Дома что-то случилось, некуда вернуться! Вернитесь пешком, проверьте и установите точку дома заново."));
                            p.sendMessage( "§c"+Lang.t(p, "Если Вы забыли где Ваш дом ")+home+" , "+Lang.t(p, "вот его координаты")+" x:"+(int)homeLoc.getBlockX()+", y:"+(int)homeLoc.getBlockY()+", z:"+(int)homeLoc.getBlockZ() );
                        }
                }

            } else {
                p.sendMessage( "§c"+Lang.t(p, "Что-то пошло не так при получении координат."));
            }

        } else {

            p.sendMessage( "§c"+Lang.t(p, "Нет такого дома! Ваши дома:")+" §6"+ApiOstrov.listToString(op.homes.keySet(), ",") );

        }   
           
           
           
           
           
     /*      
        switch (a.length) {
            
            
            case 0:
                openMenu(p);
                break;
                
                
                
            case 1:
                //ApiOstrov.getWarpManager().tryWarp(p, a[0]);
                break;
                
                
        }
            */
             

                
        return true;
    }
    
    
    
    
    
    
    
    
    
    /*private static void openMenu(final Player p) {
        SmartInventory.builder()
            .id("WarpMenu"+p.getName())
            .provider(new WarpMenu())
            .size(6, 9)
            .title("§fМеста")
            .build()
            .open(p);
    }*/
  
    
    
    
    
    
    
    
    
}
