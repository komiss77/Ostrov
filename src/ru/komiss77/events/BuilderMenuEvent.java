package ru.komiss77.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.komiss77.builder.SetupMode;



public class BuilderMenuEvent extends Event{
    
    private static HandlerList handlers = new HandlerList();
    private final Player player;
    private final SetupMode setup;


    public BuilderMenuEvent(final Player player, final SetupMode setup) {
        this.player = player;
        this.setup = setup;
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
    
    public SetupMode getSetupMode() {
        return setup;
    }
    


   
   

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }






    

    
}
