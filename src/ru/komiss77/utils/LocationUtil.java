package ru.komiss77.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import ru.komiss77.Managers.WorldManager;
import ru.komiss77.Ostrov;






public class LocationUtil {
    
 
    private static final  BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    private static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

    public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
        if (useSubCardinalDirections) {
            return radial[Math.round(yaw / 45f) & 0x7].getOppositeFace();
        }
        return axis[Math.round(yaw / 90f) & 0x3].getOppositeFace();
    }

    public static Location LocFromString (final String loc_string) {
        return LocFromString(loc_string, true);
    }
    public static Location LocFromString (final String loc_string, final boolean autoLoadWorld) {
//System.out.println("-----LocFromString "+loc_string);
        if (loc_string==null || loc_string.isEmpty()) return null;
        
        final String separator;
        if (loc_string.contains("<>")) separator = "<>"; 
        else if (loc_string.contains(":"))  separator = ":"; 
        else return null;
        
        final String[] split = loc_string.split(separator);
        if ( split.length<4 || split.length>6 ) {
            Ostrov.log_err("Декодер локации : длинна массива должна быть от 4 до 6 : "+loc_string);
            return null;
        }
        
        World world = Bukkit.getWorld(split[0]);
        if (world == null) {
            if (!autoLoadWorld) {
                Ostrov.log_err("Декодер локации : мир не найден! (autoLoadWorld=false) "+loc_string);
                return null;
            }
            WorldManager.load(Bukkit.getConsoleSender(), split[0], World.Environment.NORMAL, WorldManager.Generator.Empty);
            world = Bukkit.getWorld(split[0]);
        }
        if (world == null) {
            WorldManager.create(Bukkit.getConsoleSender(), split[0], World.Environment.NORMAL, WorldManager.Generator.Empty, true);
            world = Bukkit.getWorld(split[0]);
        }
        if (world == null) {
            Ostrov.log_err("Декодер локации : Не удалось найти, загрузить или создать мир "+split[0]+" : "+loc_string);
            return null;
        }
        if (!Ostrov.isInteger(split[1]) || !Ostrov.isInteger(split[2]) || !Ostrov.isInteger(split[3]) ) {
            Ostrov.log_err("Декодер локации : X, Y или Z - не числа : "+loc_string);
            return null;
        }
        
        final Location loc = new Location ( world, Double.valueOf(split[1])+0.5, Integer.valueOf(split[2]), Double.valueOf(split[3])+0.5 ) ;
 
//System.out.println("LocFromString >"+loc_string+"< length="+split.length);

        if (split.length >= 5) {
            if (Ostrov.isInteger(split[4]) ) {
                loc.setYaw(Integer.valueOf(split[4]));
            } else {
                Ostrov.log_warn("Декодер локации : yaw - не числo : >"+loc_string+"<");
            }
            if (split.length == 6) {
                if (Ostrov.isInteger(split[5]) ) {
                    loc.setPitch(Integer.valueOf(split[5]));
                } else {
                    Ostrov.log_warn("Декодер локации : pitch - не числo : >"+loc_string+"<");
                }
            }
        }
        
        
        return loc;
    }    



    public static String StringFromLoc (final Location loc) {
        if (loc==null) return ":::";
        return loc.getWorld().getName()+":"+loc.getBlockX()+ ":" + loc.getBlockY()+ ":" + loc.getBlockZ();
    }      

    public static String StringFromLocWithYawPitch (final Location loc) {
        if (loc==null) return ":::::";
        return loc.getWorld().getName()+":"+loc.getBlockX()+ ":" + loc.getBlockY()+ ":" + loc.getBlockZ()+ ":" + (int)loc.getYaw()+ ":" + (int)loc.getPitch();
    }      

    
    public static int getDistance (final Location loc1, final Location loc2) {
        if (loc1==null || loc2==null || !loc1.getWorld().getName().equals(loc2.getWorld().getName()) ) return Integer.MAX_VALUE;
        //return ( (int) (Math.pow( loc1.getBlockX()-loc2.getBlockX(), 2) +
        //                    Math.pow( loc1.getBlockY()- loc2.getBlockY(), 2) +
        //                        Math.pow( loc1.getBlockZ()- loc2.getBlockZ(), 2)) );
        return  square(loc1.getBlockX()-loc2.getBlockX()) + square(loc1.getBlockY()-loc2.getBlockY()) + square(loc1.getBlockZ()-loc2.getBlockZ()) ;
    }
    
    private static int square(final int num) {
        return num * num;
    }
    
    public static Location getNearestPlayer(final Player p) {
        Location loc=p.getLocation();
        int minDistance=Integer.MAX_VALUE;
        for (Player pl:p.getWorld().getPlayers()) {
//System.out.println("getNearestPlayer "+p.getName()+":"+pl.getName()+" distance = "+getDistance(p.getLocation(), pl.getLocation())+" minDistance="+minDistance);
            if (!p.getName().equals(pl.getName()) && getDistance(p.getLocation(), pl.getLocation()) < minDistance) {
                loc = pl.getLocation();
                minDistance = LocationUtil.getDistance(p.getLocation(), pl.getLocation());
            }
        }
        return loc;
    }


    public static Biome biomeFromString(final String biomename) {
        for (Biome b : Biome.values()) {
            if (b.toString().equalsIgnoreCase(biomename)) return b;
        }
        return Biome.PLAINS;
    }
  
    
    
    
    
}
