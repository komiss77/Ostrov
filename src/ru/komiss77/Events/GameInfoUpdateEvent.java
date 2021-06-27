package ru.komiss77.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.Enums.Game;
import ru.komiss77.Enums.GameState;



public class GameInfoUpdateEvent extends Event{
    
    private static HandlerList handlers = new HandlerList();
    public final Game game; //arcaim daaria bw01 bb01 sg02
    public final String server; //arcaim daaria bw01 bb01 sg02
    public final String arena;
    public final GameState state;
    public final int players;
    public final String line0;
    public final String line1;
    public final String line2;
    public final String line3;
    public final String extra;

    public GameInfoUpdateEvent(final Game game, final String server, final String arena, final GameState state, final int players, final String line0, final String line1, final String line2, final String line3, final String extra) {
        this.game = game;
        this.server = server;
        this.arena = arena;
        this.state = state;
        this.players = players;
        this.line0 = line0;
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.extra = extra;
    }

   
    
    

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


}
