package ru.komiss77.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;





public class StatChangeEvent extends Event implements Cancellable {

    private static HandlerList handlers = new HandlerList();
    private final Player p;
    private final Oplayer op;
    private final Stat stat;
    private final int oldValue;
    private int ammount;
    private boolean cancel = false;

    public StatChangeEvent(final Player p, final Oplayer op, final Stat stat, final int oldValue, final int ammount) {
        this.p = p;
        this.op = op;
        this.stat = stat;
        this.oldValue = oldValue;
        this.ammount = ammount;
    }

    public Player getPlayer() {
        return p;
    }
    
    public Oplayer getOplayer() {
        return op;
    }
    
    public Stat getStat() {
        return stat;
    }
    
    public int getOldValue() {
        return oldValue;
    }
    
    public int getAmmount() {
        return ammount;
    }
    
    public void setAmmount(final int ammount) {
        this.ammount = ammount;
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
