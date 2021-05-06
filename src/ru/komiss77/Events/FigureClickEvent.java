package ru.komiss77.Events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;



public class FigureClickEvent extends Event{
    
    private static HandlerList handlers = new HandlerList();
    private final Player player;
    private final String figureName;
    private final String tag;
    private final boolean leftClick;
    private final Location figureLocation;

    public FigureClickEvent(final Player player, final String figureName, final Location figureLocation, final String tag, final boolean leftClick) {
        this.player = player;
        this.figureName = figureName;
        this.figureLocation = figureLocation;
        this.tag = tag;
        this.leftClick = leftClick;
    }


    public Player getPlayer() {
        return this.player;
    }   
    public String getFigureName() {
        return this.figureName;
    }   
    
    public Location getFigureLocation() {
        return this.figureLocation;
    }   
    public String getTag() {
        return this.tag;
    }   
    public boolean isLeftClick() {
        return this.leftClick;
    }   
    
   
   

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    

    
}
