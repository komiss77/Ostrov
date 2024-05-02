package ru.komiss77.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ru.komiss77.enums.Operation;





public class OstrovChanelEvent extends Event {

    private static HandlerList handlers = new HandlerList();
    public final Operation action;
    public final String senderInfo;
    public final int int1;
    public final int int2;
    public final int int3;
    public final String s1; //message2
    public final String s2; //message2
    public final String s3; //message2
    public final String s4; //message2
    public final String s5; //message2
    public final String s6; //message2
    
    
    
    public OstrovChanelEvent( final Operation action, final String senderInfo, final int int1, final int int2, final int int3, final String s1, final String s2, final String s3, final String s4, final String s5, final String s6) {
        this.senderInfo = senderInfo;
        this.action = action;
        this.int1 = int1;
        this.int2 = int2;
        this.int3 = int3;
        this.s1 = s1;
        this.s2 = s2;
        this.s3 = s3;
        this.s4 = s4;
        this.s5 = s5;
        this.s6 = s6;
    }
    
    
    
    
    
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
}
