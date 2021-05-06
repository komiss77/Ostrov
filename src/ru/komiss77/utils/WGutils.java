package ru.komiss77.utils;


import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import ru.komiss77.Ostrov;





//  https://worldguard.enginehub.org/en/latest/developer/regions/managers/

// не переименовывать! используют другие плагины


public class WGutils {

    public static List <ProtectedRegion> Get_all_player_region ( Player p ) {
            if (Ostrov.worldguard_platform==null) return null;

            List <ProtectedRegion> regions = new ArrayList<>();

            //LocalPlayer lp = Ostrov.getWorldGuard().wrapPlayer(p);

            Bukkit.getWorlds().stream().forEach((w) -> {
                RegionManager rm = getRegionManager(w); 
                rm.getRegions().values().stream().forEach((rg) -> {
                    if ( rg.getOwners().contains(p.getUniqueId()) || rg.getOwners().contains(p.getName()) ) regions.add(rg);
                });
            });
               return regions;

    }




    public static List<ProtectedRegion> Get_world_player_owned_region(Player p) {
        if (Ostrov.getWorldGuard()==null) return null;

        List <ProtectedRegion> regions = new ArrayList<>();
        //LocalPlayer lp = Ostrov.getWorldGuard().wrapPlayer(p);

            RegionManager rm = getRegionManager(p.getWorld()); 
            rm.getRegions().values().stream().forEach((rg) -> {
                if ( rg.getOwners().contains(p.getUniqueId()) || rg.getOwners().contains(p.getName()) ) regions.add(rg);
            });

                return regions;
        }




    public static List<ProtectedRegion> Get_world_player_member_region(Player p) {
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
    





    public static Location Get_region_center ( final Player p, final ProtectedRegion rg) {
        if (Ostrov.getWorldGuard()==null) return p.getLocation();

        final int x = Math.abs ((rg.getMaximumPoint().getBlockX()+ rg.getMinimumPoint().getBlockX())/2);
        final int z = Math.abs((rg.getMaximumPoint().getBlockZ()+ rg.getMinimumPoint().getBlockZ())/2);
        int y = (rg.getMaximumPoint().getBlockY()+ rg.getMinimumPoint().getBlockY())/2;
        final int yTop = BlockUtils.getHighestBlock(p.getWorld(), x, z).getY();
        if (y < yTop) y = yTop;
        if (y < p.getLocation().getBlockY()) y =  p.getLocation().getBlockY();
        return new Location ( p.getWorld(), x+0.5, y, z+0.5 );
    }

    public static Location Get_region_center_by_id ( final Player p, final String rg_id) {
        if (Ostrov.getWorldGuard()==null) return null;
            RegionManager rm = getRegionManager(p.getWorld()); 
            ProtectedRegion rg = rm.getRegion(rg_id);
            return Get_region_center(p, rg);
    }
    

    
    public static RegionManager getRegionManager(final World world) {
	final RegionContainer container = Ostrov.getWorldGuard().getRegionContainer();
        return container.get(BukkitAdapter.adapt(world));
    }

    public static boolean canBuild (final Player p, final Location loc) {
        final RegionQuery query = Ostrov.getWorldGuard().getRegionContainer().createQuery();
        final LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(p);
        //if (Ostrov.getWorldGuard().getSessionManager().hasBypass(lp, BukkitAdapter.adapt(p.getWorld()) )) return true;//проверка player has bypass permissions
        return query.testState(BukkitAdapter.adapt(loc), lp, Flags.BUILD);
    }



    public static ApplicableRegionSet getRegionsOnLocation (final Location loc) {
        //if (Ostrov.getWorldGuard()==null) return new ApplicableRegionSett
        //final RegionContainer container = Ostrov.getWorldGuard().getRegionContainer();
        //final RegionQuery query = container.createQuery();
        final RegionQuery query = Ostrov.getWorldGuard().getRegionContainer().createQuery();
        return query.getApplicableRegions(BukkitAdapter.adapt(loc));
    }




    
}
