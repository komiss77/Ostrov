package ru.komiss77.Listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import ru.komiss77.Events.OstrovChanelEvent;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.SM;
import ru.komiss77.Objects.Oplayer;
import ru.komiss77.Ostrov;
import ru.komiss77.ProfileMenu.PassportHandler;
import ru.komiss77.modules.Pandora;


public class OstrovChanelListener implements Listener {
    
    @EventHandler(ignoreCancelled = true,priority = EventPriority.HIGH)
    public void onChanelMsg (OstrovChanelEvent e) {
//System.out.println(" -- OstrovChanelEvent "+e.from+" "+e.action+" "+e.bungee_raw_data);
    
    final Player p = Bukkit.getPlayer(e.sender);
    final Oplayer op = PM.getOplayer(e.sender);
    
    
    if (p==null || op==null) {
        Ostrov.log_err("OstrovChanelListener : p==null || op==null sender="+e.sender+" action="+e.action);
        return;
    }
    
    switch (e.action) {
        //case BS_lobby:  //данные для табличек, отправляется всем серверам, чьё имя > 4
        case ARENA_INFO_TO_LISTENER:
            SM.ArenaInfoFromBungeeHandler(e.string1);
            break;

        case RAW_DATA_TO_OSTROV:
            op.bungeeDataInject(p, e.string1);
            break;

        //case SET_STAT_TO_OSTROV:
        //        final E_Stat st = E_Stat.byTag(e.int1);
        //        if (st!=null) op.updateStatFromBungee(p, st, e.int2);
                //PM.getOplayer(e.from).updateDataFromBungee(Bukkit.getPlayer(e.from), Data.byTag(ApiOstrov.getInteger(e.bungee_raw_data.split("<>")[0])), e.bungee_raw_data.split("<>")[1]);
        //    break;

        case SET_DATA_TO_OSTROV: //при обновлении на острове - только отправка в банжи, и ожидание обновы с банжи
            //final Data d = Data.byTag(e.int1);
            //if (d!=null) 
            op.updateDataFromBungee(p, e.int1, e.int2, e.string1);
            break;
            

        //case PANDORA_CHECK_RESULT:
        //    Pandora.bungee_result_pandora_check(p, e.string1);
        //    break;

        //case PANDORA_RUN_RESULT:
        //    Pandora.bungee_pandora_result(p, e.string1);
        //    break;

        case TELEPORT:
            op.teleportEvent(e.string1);
            break;

        case EXECUTE_SPIGOT_CMD:
            p.performCommand(e.string1);
            break;

        case BUNGEE_ONLINE:
            SM.bungee_online=e.int1;
            break;

        case OSTROV_PASSPORT:
            PassportHandler.showGlobal(p, e.string1);
            break;

        }
    }

                        
                        
}
