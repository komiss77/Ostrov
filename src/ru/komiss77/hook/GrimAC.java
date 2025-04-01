package ru.komiss77.hook;

import ac.grim.grimac.api.GrimAbstractAPI;
import ac.grim.grimac.api.events.FlagEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;


public class GrimAC implements Listener {

    public final GrimAbstractAPI api;

    public GrimAC() {
        final RegisteredServiceProvider<GrimAbstractAPI> prv =
            Bukkit.getServicesManager().getRegistration(GrimAbstractAPI.class);
        api = prv == null ? null : prv.getProvider();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCheat(final FlagEvent e) {
        final Oplayer op = PM.getOplayer(e.getUser().getUniqueId());
        if (op == null) return;
        //TODO grim API наказания
    }

    /*@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCheat(final PlayerViolationEvent e) {
        
        
        *//*switch (e.getHackType()) {
            
            case MOVE, JESUS -> {
            }
            default -> {
                return;
            }
        }*//*

        final Player p = e.getPlayer();
        final Oplayer op = PM.getOplayer(p);
        if (op == null || op.getOnlineTime() < 10 || Timer.has(p, "Matrix")) {
            return;
        }
        Timer.add(p, "Matrix", 5);

        final CheatType type = CheatType.valueOf(e.getHackType().name());

        if (type == CheatType.MOVE) {
            if (Nms.getFastMat(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY() - 1, p.getLocation().getBlockZ()) != Material.AIR) {
                Ostrov.log_warn("CheatType.MOVE, под ногами-1 не воздух!");
                return;
            }
            if (Nms.getFastMat(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY() - 2, p.getLocation().getBlockZ()) != Material.AIR) {
                Ostrov.log_warn("CheatType.MOVE, под ногами-2 не воздух!");
                return;
            }
        }

        if (op.cheats.containsKey(type)) {
            int count = op.cheats.get(type);
            count++;
            op.cheats.put(type, count);
//Ostrov.log("cheat "+p.name()+" "+type+":"+count);
            if (count % 10 == 0) {
                //ApiOstrov.sendMessage(Operation.REPORT_SERVER, GM.this_server_name, 0, 0, 0, arg[0], LocationUtil.StringFromLoc(p.getLocation()), text);
                SpigotChanellMsg.sendMessage(p, Operation.REPORT_SERVER, Ostrov.MOT_D, 0, 0, 0, p.getName(), LocUtil.toString(p.getLocation()), "Подтверждён чит " + type + "," + count);
            }
        } else {
            op.cheats.put(type, 1);
//Ostrov.log("cheat "+p.name()+" "+type+":"+1);
        }
       *//* if (!viol.containsKey(p.name())) {
            viol.put(p.name(), 1);
        } else {
            int count = viol.get(p.name())+1;
            viol.replace(p.name(), count);
            if (count==10) {
                Ostrov.log_warn("пока просто лог : 10 замечаний античита для "+p.name());
            }
        }*//*
    }*/


    // @EventHandler (priority = EventPriority.MONITOR)
    // public void onCheat(final PlayerQuitEvent e) {
    //     viol.remove(e.getPlayer().name());
    // }


}
