package ru.komiss77.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.Enums.UniversalArenaState;



public class SignUpdateEvent extends Event{
    
    private static HandlerList handlers = new HandlerList();
    public final String server;
    public final String arena;
    public final String line1;
    public final String line2;
    public final String line3;
    public final String line4;
    public final String extraData;
    public final UniversalArenaState state;

    public SignUpdateEvent(String server, String arena, String line1, String line2, String line3, String line4, String extraData, UniversalArenaState state) {
        this.server = server;
        this.arena = arena;
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.line4 = line4;
        this.extraData = extraData;
        this.state = state;
    }

   
    
    

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


}
