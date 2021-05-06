package ru.komiss77.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;



public class BungeeDataRecieved extends Event{
    
    private static HandlerList handlers = new HandlerList();
    private Player p;
    private int bal;

    public BungeeDataRecieved(Player p, int balance) {
        this.p = p;
        this.bal=balance;
    }

@Deprecated
    public Player Get_player() {
        return this.p;
    }   

@Deprecated
    public int Get_balance() {
        return this.bal;
    }   
    public Player getPlayer() {
        return this.p;
    }   

    public int getBalance() {
        return this.bal;
    }   
   
   

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


}
