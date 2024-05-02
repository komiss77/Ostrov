package ru.komiss77.events;

import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;


public class LocalDataLoadEvent extends Event implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    private final Player p;
    private final Oplayer op;
    private boolean cancel;
    private final boolean isFirstJoin;
    private Location logoutLoc;

    public LocalDataLoadEvent(final Player p, final Oplayer op, final Location logoutLoc) {
        this.p = p;
        this.op = op;
        this.isFirstJoin = op.firstJoin;
        this.logoutLoc = logoutLoc;
    }


    public Player getPlayer() {
        return p;
    }   
   
    public Oplayer getOplayer() {
        return op;
    }   
   
    public Location getLogoutLocation() {
        return logoutLoc;
    }   
   
    public void setLogoutLocation(final Location logoutLoc) {
        this.logoutLoc = logoutLoc;
    }   
   
    public Map <String,String> getData() {
        return op.mysqlData;
    }

    public boolean hasSqlError() {
        return op.mysqlError;
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
