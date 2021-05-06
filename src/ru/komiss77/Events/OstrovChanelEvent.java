package ru.komiss77.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ru.komiss77.Enums.Action;





public class OstrovChanelEvent extends Event {

    private static HandlerList handlers = new HandlerList();
    public final String from;
    public final Action action;
    public final String bungee_raw_data;
    
    
    
    public OstrovChanelEvent(String from, Action action, String bungee_raw_data) {
        this.from = from;
        this.action = action;
        this.bungee_raw_data = bungee_raw_data;
    }
    
    
    
    
    
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
}
