package ru.komiss77.modules.games;

import org.bukkit.Location;
import org.bukkit.Material;
import ru.komiss77.ApiOstrov;





public class GameSign {
    
    protected final Location signLoc;
    protected final String server;
    protected final String arena;
    
    protected Location attachement_loc;
    protected Material attachement_mat;
    
    GameSign(final Location loc, final String server, final String arena) {
        this.signLoc = loc;
        this.server = server;
        this.arena = arena;
        
        attachement_loc=ApiOstrov.getSignAttachedBlock(signLoc.getBlock()).getLocation();
        attachement_mat=attachement_loc.getBlock().getType();
        
        if (!ApiOstrov.canChangeColor(attachement_mat)) {
            attachement_loc=null;
        }
    }
    
    
}
