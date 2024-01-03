package ru.komiss77.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;





public class PandoraUseEvent extends Event {

    private static HandlerList handlers = new HandlerList();
    private final Player p;
    private final boolean luck;
   
    public PandoraUseEvent(final Player p, final boolean luck) {
        this.p = p;
        this.luck = luck;
    }

    public Player getPlayer() {
        return p;
    }
    
    public boolean luck() {
        return luck;
    }
    
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
}
