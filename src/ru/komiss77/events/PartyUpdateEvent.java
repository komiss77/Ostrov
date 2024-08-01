package ru.komiss77.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Set;


public class PartyUpdateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Set<String> party_members;
    private final String party_leader;

    public PartyUpdateEvent(final Player player, final String party_leader, final Set<String> party_members) {
        this.player = player;
        this.party_leader = party_leader;
        this.party_members = party_members;
    }


    public Player getPlayer() {
        return player;
    }

    public String getLeader() {
        return party_leader;
    }

    public Set<String> getMembers() {
        return party_members;
    }


    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


}
