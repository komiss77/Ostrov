package ru.komiss77.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ru.komiss77.Enums.Action;





public class OstrovChanelEvent extends Event {

    private static HandlerList handlers = new HandlerList();
    public final String sender;
    public final Action action;
    public final int int1;
    public final int int2;
    public final String string1; //message2
    public final String string2; //message2
    
    
    //@Deprecated
   // public OstrovChanelEvent(Action action, String from,  String bungee_raw_data) {
    //    this.action = action;
    //    this.from = from;
    //    this.bungee_raw_data = bungee_raw_data;
   // }
    
    public OstrovChanelEvent( final String sender, Action action, final int int1, final int int2, final String string1, final String string2) {
        this.sender = sender;
        this.action = action;
        this.int1 = int1;
        this.int2 = int2;
        this.string1 = string1;
        this.string2 = string2;
    }
    
    
    
    
    
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
}
