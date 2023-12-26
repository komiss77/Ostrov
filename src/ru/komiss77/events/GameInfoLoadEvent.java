package ru.komiss77.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;



public class GameInfoLoadEvent extends Event{
    
    private static HandlerList handlers = new HandlerList();
    

    public GameInfoLoadEvent() {
    }

   
    
    

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }




}
