package ru.komiss77.modules.warp;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.komiss77.utils.LocationUtil;


public class Warp {
    
    public final String warpName;
    public final String owner;
    public Material dispalyMat = Material.ENDER_PEARL;
    public String descr;
    public String locString;
    public boolean system = false;
    public boolean open = true;
    public boolean need_perm = false;
    public int use_cost = 0;
    public int use_counter = 0;
    public final int create_time;

    
    public Warp(final String warpName, final String owner, final int create_time) {
        this.warpName = warpName;
        this.owner = owner;
        this.create_time = create_time;
    }



    public boolean isOwner(final Player p) {
        return owner.equalsIgnoreCase(p.getName());
    }

    public boolean isPaid() {
        return use_cost>0;
    }


    public Location getLocation() {
        //if (loc==null) {
        //    loc = new XYZ ();//LocationUtil.stringToLoc(locString, false, true);
        //} 
        return LocationUtil.stringToLoc(locString, false, true);//loc.getCenterLoc();
    }

    public void setLocation(final Location loc) {
        locString = LocationUtil.toDirString(loc);
    }
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
