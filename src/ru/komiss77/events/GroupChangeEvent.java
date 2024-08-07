package ru.komiss77.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Set;


public class GroupChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player p;
    private final Set<String> new_groups;

    public GroupChangeEvent(final Player p, final Set<String> new_groups) {
        this.p = p;
        this.new_groups = new_groups;
    }

    public Player getPlayer() {
        return p;
    }

    public Set<String> getNewGroups() {
        return new_groups;
    }


    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


}
