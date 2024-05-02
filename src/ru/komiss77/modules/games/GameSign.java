package ru.komiss77.modules.games;

import org.bukkit.Location;
import org.bukkit.Material;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Game;
import ru.komiss77.utils.TCUtils;





public class GameSign {
    
    public final Location signLoc;
    public final Game game;
    public final String server;
    public final String arena;
    
    public Location attachement_loc;
    public Material attachement_mat;
    
    public GameSign(final Location loc, final Game game, final String server, final String arena) {
        this.signLoc = loc;
        this.server = server;
        this.arena = arena;
        this.game = game;

        attachement_loc=ApiOstrov.getSignAttachedBlock(signLoc.getBlock()).getLocation();
        attachement_mat=attachement_loc.getBlock().getType();
        
        if (!TCUtils.canChangeColor(attachement_mat)) {
            attachement_loc=null;
        }
    }
    
    
}
