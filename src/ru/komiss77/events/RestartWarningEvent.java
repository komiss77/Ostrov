package ru.komiss77.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;





public class RestartWarningEvent extends Event {

    private static HandlerList handlers = new HandlerList();
    private final int secondLeft;
    
    public RestartWarningEvent(final int secondLeft) {
        this.secondLeft = secondLeft;
    }

    public int getSecondLeft() {
        return secondLeft;
    }
    
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
}
