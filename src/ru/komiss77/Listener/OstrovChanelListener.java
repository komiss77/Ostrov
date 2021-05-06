package ru.komiss77.Listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import ru.komiss77.Enums.Data;
import ru.komiss77.Events.OstrovChanelEvent;
import ru.komiss77.Managers.PM;
import ru.komiss77.Managers.SM;
import ru.komiss77.Ostrov;
import ru.komiss77.ProfileMenu.PassportHandler;
import ru.komiss77.modules.Pandora;


public class OstrovChanelListener implements Listener {
    
    @EventHandler(ignoreCancelled = true,priority = EventPriority.HIGH)
    public void onChanelMsg (OstrovChanelEvent e) {
//System.out.println(" -- OstrovChanelEvent "+e.from+" "+e.action+" "+e.bungee_raw_data);
    switch (e.action) {
        //case BS_lobby:  //данные для табличек, отправляется всем серверам, чьё имя > 4
        case ARENA_INFO_TO_LISTENER:
            SM.ArenaInfoFromBungeeHandler(e.bungee_raw_data);
            break;

        case OSTROV_PLAYER_RAW_DATA:
            if(PM.exist(e.from)) PM.getOplayer(e.from).bungeeDataInject(e.bungee_raw_data);
            break;

        case OSTROV_STAT_DATA:
            if(PM.exist(e.from)) PM.getOplayer(e.from).bungeeStatInject(e.bungee_raw_data);
            break;

        case OSTROV_UPDATE_DATA: //при обновлении на острове - только отправка в банжи, и ожидание обновы с банжи
            if(PM.exist(e.from)) PM.getOplayer(e.from).updateDataFromBungee(Data.byTag(Integer.valueOf(e.bungee_raw_data.split("<>")[0])), e.bungee_raw_data.split("<>")[1]);
            break;

        case OSTROV_PANDORA_CHECK:
            if(PM.exist(e.from)) Pandora.bungee_result_pandora_check(Bukkit.getPlayer(e.from),e.bungee_raw_data);
            break;

        case OSTROV_PANDORA_RESULT:
            if(PM.exist(e.from)) Pandora.bungee_pandora_result(e.from,e.bungee_raw_data);
            break;

        case OSTROV_TELEPORT:
            if(PM.exist(e.from)) PM.getOplayer(e.from).teleportEvent(e.bungee_raw_data);
            break;

        case OSTROV_RUN_SPIGOT_CMD:
            if(PM.exist(e.from)) Bukkit.getPlayer(e.from).performCommand(e.bungee_raw_data);
            break;

        case BUNGEE_ONLINE:
            if (Ostrov.isInteger(e.bungee_raw_data)) SM.bungee_online=Integer.valueOf(e.bungee_raw_data);
            break;

        case OSTROV_PASSPORT:
            if(PM.exist(e.from)) PassportHandler.showGlobal(Bukkit.getPlayer(e.from),e.bungee_raw_data);
            break;

        }
    }

                        
                        
}
