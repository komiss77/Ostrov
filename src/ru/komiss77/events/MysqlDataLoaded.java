package ru.komiss77.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;



public class MysqlDataLoaded extends Event implements Cancellable {
    
    private static HandlerList handlers = new HandlerList();
    private final Player p;
    private boolean cancel;

    public MysqlDataLoaded(final Player p) {
        this.p = p;
    }

@Deprecated
    public Player Get_player() {
        return this.p;
    }   
   
    public Player getPlayer() {
        return p;
    }   
   
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel=cancel;
    }
   
   

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }



}
