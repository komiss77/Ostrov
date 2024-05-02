package ru.komiss77.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class BsignLocalArenaClick extends Event {

    private static HandlerList handlers = new HandlerList();
    public final Player player;
    public final String arenaName;

    public BsignLocalArenaClick(final Player player, final String arenaName) {
        this.player = player;
        this.arenaName = arenaName;
    }

    
    
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }    
    
    
    
}

