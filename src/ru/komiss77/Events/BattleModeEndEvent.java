package ru.komiss77.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


@Deprecated
public class BattleModeEndEvent extends Event{
    
    private static HandlerList handlers = new HandlerList();
    private Player player;
@Deprecated
    public BattleModeEndEvent(Player player) {
        this.player = player;
    }

@Deprecated
    public Player Get_player() {
        return this.player;
    }   
   
   
   
   

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


}
