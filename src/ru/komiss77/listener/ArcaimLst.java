package ru.komiss77.listener;

import java.util.Iterator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.hook.WGhook;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.EntityUtil.EntityGroup;
import ru.komiss77.utils.ItemUtils;

//просто скинул сюда всё из двух мелких плагинов

public class ArcaimLst implements Listener {
   
    
    // ------------ No build outside -------------
    
             
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(final BlockPlaceEvent e) {
        if (ApiOstrov.isLocalBuilder(e.getPlayer(), false)) return;
        //if (nobuild.wg.getRegionManager(e.getPlayer().getWorld()).getApplicableRegions(e.getBlock().getLocation()).size()==0 ) {
        if (WGhook.getRegionsOnLocation(e.getBlock().getLocation()).size()==0 ) {
        	e.setBuild(false);
//            e.setCancelled(true);
            e.getPlayer().sendMessage("§cСтроить можно только в приватах!");
        }
    }
    
             
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(final PlayerBucketEmptyEvent e) {
        if (ApiOstrov.isLocalBuilder(e.getPlayer(), false)) return;
        //if (nobuild.wg.getRegionManager(e.getPlayer().getWorld()).getApplicableRegions(e.getBlock().getLocation()).size()==0 ) {
        if (WGhook.getRegionsOnLocation(e.getBlock().getLocation()).size()==0 ) {
        	e.setCancelled(true);
//            e.setCancelled(true);
            e.getPlayer().sendMessage("§cСтроить можно только в приватах!");
        }
    }
    
        
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(final BlockBreakEvent e) {
        if (ApiOstrov.isLocalBuilder(e.getPlayer(), false)) return;
//System.out.println("size="+WGutils.getRegionsOnLocation(e.getBlock().getLocation()).size()+" --"+WGutils.getRegionsOnLocation(e.getBlock().getLocation()) );
        if (WGhook.getRegionsOnLocation(e.getBlock().getLocation()).size()==0 ) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cСтроить можно только в приватах!");
        }
    }
   
    //--------------------------------
   
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockFromTo(final BlockFromToEvent e) {
//System.out.println("onBlockFromTo type="+e.getBlock().getType()+" isLiquid?"+e.getToBlock().isLiquid()+" to="+e.getToBlock().getType()+" isLiquid?"+e.getBlock().isLiquid());
        if (e.getBlock().getType()==Material.LAVA || e.getBlock().getType()==Material.WATER) {
            final ApplicableRegionSet fromRegionSet = WGhook.getRegionsOnLocation(e.getBlock().getLocation());
            final ApplicableRegionSet toRegionSet = WGhook.getRegionsOnLocation(e.getToBlock().getLocation());
            if (fromRegionSet.size()==1 && toRegionSet.size()==1) { //из привата в приват, обычная ситуация
                e.setCancelled(!fromRegionSet.getRegions().contains(toRegionSet.getRegions().iterator().next()));
            } else {
    			e.setCancelled(e.getFace() != BlockFace.DOWN && e.getToBlock().getRelative(BlockFace.DOWN).getType().isAir());
			}
        }
    }

 
   
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onlavaPlaceEntity(PlayerInteractAtEntityEvent e) {
        final ItemStack is = e.getPlayer().getInventory().getItem(e.getHand());//ItemInOffHand();
        switch (is.getType()) {
		case WATER_BUCKET:
			e.setCancelled(EntityUtil.group(e.getRightClicked().getType()) != EntityGroup.WATER_AMBIENT);
			break;
		case LAVA, LAVA_BUCKET, WATER, AXOLOTL_BUCKET, COD_BUCKET, PUFFERFISH_BUCKET, 
			SALMON_BUCKET, TADPOLE_BUCKET, TROPICAL_FISH_BUCKET:
			e.setCancelled(true);
			break;
		default:
			break;
		}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSpawn(final EntitySpawnEvent e) {
    	switch (e.getEntityType()) {
		case ENDER_DRAGON:
			e.getEntity().remove();
			break;
		default:
			e.setCancelled(WGhook.getRegionsOnLocation(e.getEntity().getLocation()).size() == 0);
			break;
		}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplode(final EntityExplodeEvent e) {
    	final Iterator<Block> bli = e.blockList().iterator();
    	while (bli.hasNext()) {
    		if (WGhook.getRegionsOnLocation(bli.next().getLocation()).size() == 0) {
    			bli.remove();
    		}
		}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreative(final InventoryCreativeEvent e) {
    	final ItemStack cr = e.getCursor();
    	if (!ItemUtils.isBlank(cr, true)) {
            switch (cr.getType()) {
    		case POTION, SPLASH_POTION, LINGERING_POTION, 
    			TIPPED_ARROW, ENCHANTED_BOOK:
    			break;
    		default:
        		e.setCursor(new ItemStack(cr.getType(), cr.getAmount()));
        		e.getWhoClicked().sendMessage(Ostrov.PREFIX + "§cДанные предмета были очищены!");
    			break;
    		}
    	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDrop(final EntityDropItemEvent e) {
    	if (e.getEntityType() == EntityType.PLAYER) {
    		final ItemStack it = e.getItemDrop().getItemStack();
        	if (!ItemUtils.isBlank(it, true)) {
                switch (it.getType()) {
        		case POTION, SPLASH_POTION, LINGERING_POTION, 
        			TIPPED_ARROW, ENCHANTED_BOOK:
        			break;
        		default:
            		e.getItemDrop().setItemStack(new ItemStack(it.getType(), it.getAmount()));
            		e.getEntity().sendMessage(Ostrov.PREFIX + "§cДанные предмета были очищены!");
        			break;
        		}
        	}
    	}
    }






    /*@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void Interact(PlayerInteractEvent e) { 
        if (ApiOstrov.isLocalBuilder(e.getPlayer(), false) ) return;
//System.out.println("size="+WGutils.getRegionsOnLocation(e.getClickedBlock().getLocation()).size()+" --"+WGutils.getRegionsOnLocation(e.getClickedBlock().getLocation()) );
        if (e.getAction()==Action.LEFT_CLICK_BLOCK || e.getAction()==Action.RIGHT_CLICK_BLOCK) {
            //if (nobuild.wg.getRegionManager(e.getPlayer().getWorld()).getApplicableRegions(e.getClickedBlock().getLocation()).size()==0 ) {
            if (WGhook.getRegionsOnLocation(e.getClickedBlock().getLocation()).size()==0 ) {
                e.setCancelled(true);
                e.getPlayer().sendMessage("§cСтроить можно только в приватах!");
            }
        }
    }*/


    /*@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onlavaPlace(final PlayerInteractEvent e) {
//System.out.println("action="+e.getAction()+"  item="+e.getItem()+" mat="+e.getMaterial());
        if (e.getAction()==Action.RIGHT_CLICK_BLOCK && e.getItem()!=null) {
            if ( (e.getItem().getType().name().contains("LAVA") && e.getPlayer().getWorld().getEnvironment()!=World.Environment.NETHER) || e.getItem().getType().name().contains("WATER") ) {
                final ApplicableRegionSet regionSet = WGhook.getRegionsOnLocation(e.getClickedBlock().getLocation());//rm.getApplicableRegions(BukkitAdapter.asBlockVector(player.getLocation()));
                if (regionSet.size()==0) {
                    e.setUseItemInHand(Event.Result.DENY);
                    return;
                }
                final LocalPlayer lp = WorldGuardPlugin.inst().wrapPlayer(e.getPlayer());
                for (final ProtectedRegion rg : regionSet) {
                    if (!rg.isOwner(lp) && !rg.isMember(lp)) {
                        e.setUseItemInHand(Event.Result.DENY);
                        return;
                    }
                }
            }
        }
    }*/
    
    
    /*@EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        final Location loc = e.getBlock().getLocation();
        final int y = loc.getBlockY();
        //final ApplicableRegionSet setUp = NoLavaWater.getRegionsOnLocation(loc);
        loc.setY(70);
        final ApplicableRegionSet setDown = WGhook.getRegionsOnLocation(loc);
        if (setDown.size()>=1) {
            final ProtectedRegion rg = setDown.getRegions().stream().findFirst().get();
            if (rg.getMaximumPoint().getBlockY()<=y) {
//System.out.println("- onBucketEmpty выше привата!");
                e.getPlayer().sendMessage("§cНельзя разливать над приватом!");
                e.setCancelled(true);
                //e.setItemStack(new ItemStack(Material.BUCKET));
                if (e.getPlayer().getInventory().getItemInMainHand().getType()==e.getBucket()) {
                    e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.BUCKET));
                } else {
                   e.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.BUCKET));
                }
            }
        }
        
        //if (!can(e.getBlockClicked().getLocation(),1)) {
        //    e.setCancelled(true);
//System.out.println("-- onBucketEmpty cancel!!");
        //}
    }*/
    


    //doFireTick moment
    /*@EventHandler(priority = EventPriority.NORMAL,ignoreCancelled = true)
    public void onBurn( BlockBurnEvent e) { //распространение огня
//System.out.println("BlockBurnEvent "+e.getIgnitingBlock());        
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void igniteFire(BlockIgniteEvent e) {
        if (e.getCause() == BlockIgniteEvent.IgniteCause.SPREAD ) {
            e.setCancelled(true);
        }
    }*/

    //зачем + неработает правильно
    /*@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleMoveEvent(VehicleMoveEvent e) {
        if (e.getFrom().getBlockX()==e.getTo().getBlockX() && e.getFrom().getBlockY()==e.getTo().getBlockY() && e.getFrom().getBlockZ()==e.getTo().getBlockZ()  ) return;
        //if ( Ostrov.getWorldGuard()==null ) return;
//System.out.println("-- VehicleMoveEvent");
        final RegionQuery query = WGhook.worldguard_platform.getRegionContainer().createQuery();
        final ApplicableRegionSet regionSetTo = query.getApplicableRegions(BukkitAdapter.adapt(e.getTo()));
        final ApplicableRegionSet regionSetFrom = query.getApplicableRegions(BukkitAdapter.adapt(e.getFrom()));
        if (regionSetFrom.size()==0 || regionSetTo.size()==0 ) {
            final Vector back = e.getTo().getDirection().multiply( - (3 * 0.1) );
            e.getVehicle().setVelocity(back);
        }
        for (final ProtectedRegion rg : regionSetFrom.getRegions()) {
            if (regionSetTo.getRegions().contains(rg)) return;
        }
        final Vector back = e.getTo().getDirection().multiply( - (3 * 0.1) );
        e.getVehicle().setVelocity(back);
//System.out.println("size="+WGutils.getRegionsOnLocation(e.getBlock().getLocation()).size()+" --"+WGutils.getRegionsOnLocation(e.getBlock().getLocation()) );
    }*/
    
}
