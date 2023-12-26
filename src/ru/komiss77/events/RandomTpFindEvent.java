package ru.komiss77.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;





public class RandomTpFindEvent extends Event implements Cancellable {

    private static HandlerList handlers = new HandlerList();
    private final Player p;
    private Location loc;
    private boolean cancel = false;

    public RandomTpFindEvent(final Player p, final Location loc) {
        this.p = p;
        this.loc = loc;
    }

    public Player getPlayer() {
        return p;
    }
    
    public Location getFoundLocation() {
        return loc;
    }

    
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public final void setCancelled(final boolean cancel){
            this.cancel = cancel;
    }

    @Override
    public final boolean isCancelled(){
            return cancel;
    }

}
