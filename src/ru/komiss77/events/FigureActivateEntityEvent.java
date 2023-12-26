package ru.komiss77.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.objects.Figure;



public class FigureActivateEntityEvent extends Event{
    
    private static HandlerList handlers = new HandlerList();
    private final Figure figure;


    public FigureActivateEntityEvent(final Figure figure) {
        this.figure = figure;
    }

    
    public Figure getFigure() {
        return figure;
    }
   

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }





    

    
}
