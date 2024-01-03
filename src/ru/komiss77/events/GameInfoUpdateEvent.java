package ru.komiss77.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.ServerType;
import ru.komiss77.modules.games.ArenaInfo;



public class GameInfoUpdateEvent extends Event{
    
    private static HandlerList handlers = new HandlerList();
    
    public final ArenaInfo ai;

    public GameInfoUpdateEvent(final ArenaInfo ai) {
        this.ai = ai;
    }

   
    
    

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Game getGame() {
        return ai.gameInfo.game;
    }

    public int getOnline() {
        return getGame().type==ServerType.ARENAS ? ai.players : ai.gameInfo.getOnline();
    }

    public GameState getState() {
        return ai.state;
    }


}
