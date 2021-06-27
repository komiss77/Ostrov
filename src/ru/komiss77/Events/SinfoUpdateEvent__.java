package ru.komiss77.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


@Deprecated
public class SinfoUpdateEvent__ extends Event{
    
    private static HandlerList handlers = new HandlerList();
    public final String type;
    public final int online;


    public SinfoUpdateEvent__(final String type, final int online) {
        this.type = type;
        this.online = online;
    }

   
    
    

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


}
