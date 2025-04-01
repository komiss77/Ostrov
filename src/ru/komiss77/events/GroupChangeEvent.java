package ru.komiss77.events;

import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.objects.Group;


public class GroupChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player p;
    private final Set<Group> new_groups;

    public GroupChangeEvent(final Player p, final Set<Group> new_groups) {
        this.p = p;
        this.new_groups = new_groups;
    }

    public Player getPlayer() {
        return p;
    }

    public Set<String> getNewGroups() {
        return new_groups.stream().map(g -> g.name).collect(Collectors.toSet());
    }

    public Set<Group> getGroups() {
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
