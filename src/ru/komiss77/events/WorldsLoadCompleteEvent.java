package ru.komiss77.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;



/**
 * 
 * сработает через 10 секунд
 * после загрузки последнего 
 * мира при старте сервера
 * (только оди раз!)
 */

public class WorldsLoadCompleteEvent extends Event {

    private static HandlerList handlers = new HandlerList();
    
    public WorldsLoadCompleteEvent() {
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
}
