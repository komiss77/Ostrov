package ru.komiss77.listener;
/*
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import ru.komiss77.version.VM;


public class JumpPlateLst__ implements Listener{
    
    
    @EventHandler(ignoreCancelled = true,priority = EventPriority.MONITOR)    
    public void onPlayerMove (PlayerMoveEvent e) { 
    //if (!PlayerListener.enable_jump_plate) return;
    if (e.getFrom().getBlockX()==e.getTo().getBlockX() && e.getFrom().getBlockY()==e.getTo().getBlockY() && e.getFrom().getBlockZ()==e.getTo().getBlockZ()  ) return;
    
   final Location l = e.getPlayer().getLocation();
    //if ( e.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.REDSTONE_BLOCK ||
            //e.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.LAPIS_BLOCK  ) {
    if ( VM.getNmsServer().getFastMat(l.getWorld(), l.getBlockX(), l.getBlockY()-1, l.getBlockZ()) == Material.REDSTONE_BLOCK ||
            VM.getNmsServer().getFastMat(l.getWorld(), l.getBlockX(), l.getBlockY()-1, l.getBlockZ()) == Material.LAPIS_BLOCK  ) {

        switch ( VM.getNmsServer().getFastMat(l.getWorld(), l.getBlockX(), l.getBlockY(), l.getBlockZ())) {
            case OAK_PRESSURE_PLATE -> {
                e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().setY(1.1D));
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP , 3F, 2.0F);
            }

            case STONE_PRESSURE_PLATE -> {
                e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(1.2D).setY(1.2D));
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP , 3F, 2.0F);
            }

            case LIGHT_WEIGHTED_PRESSURE_PLATE -> {
                e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(1.4D).setY(1.4D));
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP , 3F, 2.0F);
            }

            case HEAVY_WEIGHTED_PRESSURE_PLATE -> {
                e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(1.8D).setY(1.8D));
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP , 3F, 2.0F);
            }
            
            default -> {}

        }
    }

   }
    
    
    

    
}
*/