package ru.komiss77.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.modules.world.WXYZ;


public class RandomTpFindEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player p;
    private final WXYZ xyz;
    private boolean cancel = false;

    public RandomTpFindEvent(final Player p, final WXYZ xyz) {
        this.p = p;
        this.xyz = xyz;
    }

    public Player getPlayer() {
        return p;
    }

    public Location getFoundLocation() {
        return xyz.getCenterLoc();
    }


    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public final void setCancelled(final boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public final boolean isCancelled() {
        return cancel;
    }

}
