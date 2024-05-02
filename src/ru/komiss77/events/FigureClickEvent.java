package ru.komiss77.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.objects.Figure;
import ru.komiss77.objects.FigureAnswer;



public class FigureClickEvent extends Event implements Cancellable {
    
    private static HandlerList handlers = new HandlerList();
    private final Player player;
    private final Figure figure;
    private final boolean leftClick;
    private FigureAnswer answer;
    private boolean canceled;


    public FigureClickEvent(final Player player, final Figure figure, final boolean leftClick) {
        this.player = player;
        this.figure = figure;
        this.leftClick = leftClick;
    }
 
    
    
    
    
    
   /* public void setSpeach(final Player p, final String[] lines, int second) {
        figure.setSpeach(p, lines, second);
    }      
    public void setSpeach(final Player p, final String line, int second) {
        figure.setSpeach(p, line, second);
    }    
    
    
    public void setLookAtPlayer(final Player p, final int second) {
        figure.setLookAtPlayer(p, second);
    }*/





    public Player getPlayer() {
        return player;
    }
    
    public Figure getFigure() {
        return figure;
    }

    public boolean isLeftClick() {
        return leftClick;
    }  
    
    public boolean isRightClick() {
        return !leftClick;
    }   
    

   
   

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public void setAnswer(final FigureAnswer answer) {
        this.answer = answer;
        if (answer!=null) {
            answer.player = player;
            answer.figure = figure;
        }
    }

    public FigureAnswer getAnswer() {
        return answer;
    }

    @Override
    public boolean isCancelled() {
       return canceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        canceled = cancel;
    }




    

    
}
