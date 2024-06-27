package ru.komiss77.hook;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import me.rerere.matrix.api.events.PlayerViolationEvent;
import ru.komiss77.Ostrov;
import ru.komiss77.Timer;
import ru.komiss77.enums.CheatType;
import ru.komiss77.enums.Operation;
import ru.komiss77.listener.SpigotChanellMsg;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.LocationUtil;
import ru.komiss77.version.Nms;


public class MatrixLst implements Listener{
    
    //public static final Map<String,Integer> viol = new HashMap<>();


//    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
//    public void onViolationCommand(final PlayerViolationCommandEvent e) {
//Ostrov.log("============ViolationCommand "+e.getPlayer().name()+" "+e.getHackType()+":"+e.getCommand());
//    }
    
    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCheat(final PlayerViolationEvent e) {
        
        
        /*switch (e.getHackType()) {
            
            case MOVE, JESUS -> {
            }
            default -> {
                return;
            }
        }*/
        
        final Player p = e.getPlayer();
        final Oplayer op = PM.getOplayer(p);
        if (op==null || op.getOnlineTime()<10 || Timer.has(p, "Matrix")) {
            return;
        }
        Timer.add(p, "Matrix", 5);
        
        final CheatType type = CheatType.valueOf(e.getHackType().name());
        
        if (type==CheatType.MOVE) {
            if (Nms.getFastMat(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY()-1, p.getLocation().getBlockZ()) != Material.AIR) {
Ostrov.log_warn("CheatType.MOVE, под ногами-1 не воздух!");
                return;
            }
            if (Nms.getFastMat(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY()-2, p.getLocation().getBlockZ()) != Material.AIR) {
Ostrov.log_warn("CheatType.MOVE, под ногами-2 не воздух!");
                return;
            }
        }
        
        if (op.cheats.containsKey(type)) {
            int count = op.cheats.get(type);
            count++;
            op.cheats.put(type, count);
//Ostrov.log("cheat "+p.name()+" "+type+":"+count);
            if (count%10==0) {
                //ApiOstrov.sendMessage(Operation.REPORT_SERVER, GM.this_server_name, 0, 0, 0, arg[0], LocationUtil.StringFromLoc(p.getLocation()), text);
                SpigotChanellMsg.sendMessage(p, Operation.REPORT_SERVER, Ostrov.MOT_D, 0, 0, 0, p.getName(), LocationUtil.toString(p.getLocation()), "Подтверждён чит "+type+","+count);
            }
        } else {
            op.cheats.put(type, 1);
//Ostrov.log("cheat "+p.name()+" "+type+":"+1);
        }
       /* if (!viol.containsKey(p.name())) {
            viol.put(p.name(), 1);
        } else {
            int count = viol.get(p.name())+1;
            viol.replace(p.name(), count);
            if (count==10) {
                Ostrov.log_warn("пока просто лог : 10 замечаний античита для "+p.name());
            }
        }*/
    }
    
    
   // @EventHandler (priority = EventPriority.MONITOR)
   // public void onCheat(final PlayerQuitEvent e) {
   //     viol.remove(e.getPlayer().name());
   // }
    
    
    
    

    
}
