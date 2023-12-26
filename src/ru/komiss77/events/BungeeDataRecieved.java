package ru.komiss77.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.enums.Data;
import ru.komiss77.modules.player.Oplayer;



public class BungeeDataRecieved extends Event{
    
    private static HandlerList handlers = new HandlerList();
    private Player p;
    private Oplayer op;

    public BungeeDataRecieved(final Player p, final Oplayer op) {
        this.p = p;
        this.op=op;
    }

  
    public Player getPlayer() {
        return this.p;
    }   

    public Oplayer getOplayer() {
        return this.op;
    }   

    public int getBalance() {
        return op.getDataInt(Data.LONI);
    }   
   
   

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


}
