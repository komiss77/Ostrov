package ru.komiss77.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;





public class MissionEvent extends Event {

    private static HandlerList handlers = new HandlerList();
    private final Player p;
    public final String missionName;
    public final MissionAction action;
   
    public MissionEvent(final Player p, final String missionName, final MissionAction action) {
        this.p = p;
        this.missionName = missionName;
        this.action = action;
    }

    public Player getPlayer() {
        return p;
    }
    

    
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    
    public enum MissionAction {
        Accept, Complete, Deny;
    }
    
}
