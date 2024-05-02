package ru.komiss77.hook;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import ru.komiss77.Ostrov;
import ru.komiss77.objects.ValueSortedMap;
import ru.komiss77.utils.BlockUtils;


// https://enginehub.org/
// https://worldguard.enginehub.org/en/latest/developer/regions/managers/

// не переименовывать! используют другие плагины

public class WGhook {
    
    public static WorldGuardPlatform worldguard_platform;
    
    
    public static void hook (final Plugin plugin) {
        worldguard_platform =  WorldGuard.getInstance().getPlatform();
        Ostrov.wg = true;
        Ostrov.log_ok("§bПодключен WorldGuard !");
    }


    public static int purgeDeadRegions (final Collection<String> validUsers, final Set<UUID> validUuids) {
        int result = 0;
                
        for (World w : Bukkit.getWorlds()) {
            
            final RegionManager rm = getRegionManager(w);
            
            //final Set <String> toDel = new HashSet<>();
              
            for (ProtectedRegion rg : rm.getRegions().values()) {
            //rm.getRegions().values().stream().forEach( (rg) -> {
                
                boolean valid = false;
                
                for (String name : rg.getOwners().getPlayers()) {
                    if (validUsers.contains(name)) {
                        valid = true;
                        break;
                    }
                }
                
                for (UUID uuid : rg.getOwners().getUniqueIds()) {
                    if (validUuids.contains(uuid)) {
                        valid = true;
                        break;
                    }
                }
                
                if (!valid) {
                    //toDel.add(rg.getId());
                    rm.removeRegion(rg.getId());
                    result++;
                }
                
                /*if ( rg.getOwners().contains(name) ) {
                    rg.getOwners().removePlayer(name);
                } else if (uuid!=null && rg.getOwners().contains(uuid)) {
                    rg.getOwners().removePlayer(uuid);
                } else if ( rg.getMembers().contains(name) ) {
                    rg.getMembers().removePlayer(name);
                } else if (uuid!=null && rg.getMembers().contains(uuid)) {
                    rg.getMembers().removePlayer(uuid);
                }*/
            }
            
           // for (final String id : toDel) {
           //     rm.removeRegion(id);
          //      result++;
         //   }
            
        }
        return result;
    }


    public static Map<ProtectedRegion,String> findPlayerRegions(final org.bukkit.entity.Player p, final World world, final boolean owner, final boolean member) {
        if (worldguard_platform==null) return null;

        Map <ProtectedRegion,String> regions = new ValueSortedMap<>();
        //LocalPlayer lp = Ostrov.getWorldGuard().wrapPlayer(p);
        
        if (world==null) {
            
            Bukkit.getWorlds().stream().forEach( (w) -> {
                final RegionManager rm = getRegionManager(w); 
                rm.getRegions().values().stream().forEach( (rg) -> {
                    if ( owner && rg.getOwners().contains(p.getUniqueId()) || rg.getOwners().contains(p.getName()) ) {
                        regions.put(rg, w.getName());
                    } else if ( member && rg.getMembers().contains(p.getUniqueId()) || rg.getMembers().contains(p.getName()) ) {
                        regions.put(rg, w.getName());
                    }
                });
            });
            
        } else {
            final RegionManager rm = getRegionManager(world); 
            rm.getRegions().values().stream().forEach( (rg) -> {
                if ( owner && rg.getOwners().contains(p.getUniqueId()) || rg.getOwners().contains(p.getName()) ) {
                    regions.put(rg, world.getName());
                } else if ( member && rg.getMembers().contains(p.getUniqueId()) || rg.getMembers().contains(p.getName()) ) {
                    regions.put(rg, world.getName());
                }
            });
        }
        return regions;
    }










    public static Location regionCenterByID ( final Player p, final String rg_id) {
        if (worldguard_platform==null) return null;
            RegionManager rm = getRegionManager(p.getWorld()); 
            ProtectedRegion rg = rm.getRegion(rg_id);
            return regionCenter(p, rg);
    }
    
    public static Location regionCenter ( final Player p, final ProtectedRegion rg) {
        if (worldguard_platform==null) return p.getLocation();
        final int x = Math.abs ((rg.getMaximumPoint().getBlockX()+ rg.getMinimumPoint().getBlockX())/2);
        final int z = Math.abs((rg.getMaximumPoint().getBlockZ()+ rg.getMinimumPoint().getBlockZ())/2);
        int y = (rg.getMaximumPoint().getBlockY()+ rg.getMinimumPoint().getBlockY())/2;
        final int yTop = BlockUtils.getHighestBlock(p.getWorld(), x, z).getY();
        if (y < yTop) y = yTop;
        if (y < p.getLocation().getBlockY()) y =  p.getLocation().getBlockY();
        return new Location ( p.getWorld(), x+0.5, y, z+0.5 );
    }

    public static String getRegionLocationInfo ( final ProtectedRegion rg) {
        final int x = Math.abs ((rg.getMaximumPoint().getBlockX()+ rg.getMinimumPoint().getBlockX())/2);
        final int z = Math.abs((rg.getMaximumPoint().getBlockZ()+ rg.getMinimumPoint().getBlockZ())/2);
        int y = (rg.getMaximumPoint().getBlockY()+ rg.getMinimumPoint().getBlockY())/2;
        return x+","+y+","+z;
    }

    
    public static RegionManager getRegionManager(final World world) {
	final RegionContainer container = worldguard_platform.getRegionContainer();
        return container.get(BukkitAdapter.adapt(world));
    }

    public static boolean canBuild (final Player p, final Location loc) {
        final RegionQuery query = worldguard_platform.getRegionContainer().createQuery();
        final LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);
        return query.testState(BukkitAdapter.adapt(loc), lp, Flags.BUILD);
    }

    public static ApplicableRegionSet getRegionsOnLocation (final Location loc) {
        final RegionQuery query = worldguard_platform.getRegionContainer().createQuery();
        return query.getApplicableRegions(BukkitAdapter.adapt(loc));
    }




    
}


/*

    public static List<String> Get_world_player_owned_region_text(Player p) {
        if (Ostrov.getWorldGuard()==null) return null;

        List <String> regions = new ArrayList<>();
        //LocalPlayer lp = Ostrov.getWorldGuard().wrapPlayer(p);

            RegionManager rm = getRegionManager(p.getWorld());  
            rm.getRegions().values().stream().forEach((rg) -> {
                if ( rg.getOwners().contains(p.getUniqueId()) || rg.getOwners().contains(p.getName()) ) regions.add(rg.getId());
            });
                                
            return regions;
    }

    public static Object Get_world_player_member_region_text(Player p) {
        if (Ostrov.getWorldGuard()==null) return null;

        List <String> regions = new ArrayList<>();
        //LocalPlayer lp = Ostrov.getWorldGuard().wrapPlayer(p);

            RegionManager rm = getRegionManager(p.getWorld()); 
            rm.getRegions().values().stream().forEach((rg) -> {
                if ( rg.getMembers().contains(p.getUniqueId()) || rg.getMembers().contains(p.getName()) ) regions.add(rg.getId());
            });
                                
            return regions;
    }
    
*/

/*
    public static List <ProtectedRegion> findPlayerOwnedRegions ( Player p ) {
            if (Ostrov.worldguard_platform==null) return null;

            List <ProtectedRegion> regions = new ArrayList<>();

            //LocalPlayer lp = Ostrov.getWorldGuard().wrapPlayer(p);

            Bukkit.getWorlds().stream().forEach( (w) -> {
                RegionManager rm = getRegionManager(w); 
                rm.getRegions().values().stream().forEach((rg) -> {
                    if ( rg.getOwners().contains(p.getUniqueId()) || rg.getOwners().contains(p.getName()) ) regions.add(rg);
                });
            });
               return regions;

    }

    public static List<ProtectedRegion> findPlayerMemberRegions(Player p) {
        if (Ostrov.getWorldGuard()==null) return null;

        List <ProtectedRegion> regions = new ArrayList<>();

       // LocalPlayer lp = Ostrov.getWorldGuard().wrapPlayer(p);

        Bukkit.getWorlds().stream().forEach((w) -> {
            RegionManager rm = getRegionManager(w); 
            rm.getRegions().values().stream().forEach((rg) -> {
                if ( rg.getMembers().contains(p.getUniqueId()) || rg.getMembers().contains(p.getName()) ) regions.add(rg);
            });
        });
           return regions;
    }
*/
