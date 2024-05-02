package ru.komiss77.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;



public class FriendTeleportEvent extends Event{
    
    private static HandlerList handlers = new HandlerList();
    public Player source;
    public Player target;
    public String cause;
    private boolean canceled;

    public FriendTeleportEvent(Player source, Player target) {
        this.source = source;
        this.target = target;
        this.cause = "";
        this.canceled=false;
    }



    public boolean isCanceled() {
        return canceled;
    }   
   
    public void setCanceled(boolean canceled, String cause) {
        this.canceled=canceled;
        this.cause=cause;
    }   
   
   
   

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


}
