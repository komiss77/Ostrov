package ru.komiss77.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ru.komiss77.Objects.Oplayer;




public class BungeeStatRecieved extends Event {

    private static HandlerList handlers = new HandlerList();
    private Oplayer oplayer;
    
    public BungeeStatRecieved(Oplayer oplayer) {
        this.oplayer=oplayer;
    }

    public Oplayer getOplayer() {
        return oplayer;
    }
    
    public Player getPlayer() {
        return oplayer.getPlayer();
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
}
