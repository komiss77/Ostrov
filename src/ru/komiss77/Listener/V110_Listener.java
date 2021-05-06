
package ru.komiss77.Listener;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import ru.komiss77.Ostrov;


public class V110_Listener implements Listener {





 /*   
@EventHandler
	public void onServerListPing(ServerListPingEvent e) {
            if (!ServerListener.bauth) return;
		int tps =(int) MinecraftServer.getServer().recentTps[0];
		String max = String.valueOf ( (int) (Runtime.getRuntime().maxMemory() / 1024 / 1024 ) );
		String used = String.valueOf ( (int) (Runtime.getRuntime().totalMemory() / 1024 /1024 ) );

            e.setMotd(max+"<:>"+used+"<:>"+String.valueOf(tps) );
        }
*/    
    

@EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent e)
    {
        if( PlayerListener.disable_break_place && e.getRightClicked().getType() ==EntityType.ARMOR_STAND && !e.getPlayer().isOp() ) e.setCancelled(true);
    }


   

@EventHandler(ignoreCancelled = true,priority=EventPriority.LOWEST)
    public void PlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent e){
        if ( PlayerListener.disable_break_place && !e.getPlayer().isOp()) e.setCancelled(true);
    }    
    

    
    
@EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onEntityDamageEvent ( EntityDamageEvent  e ) {
        if (e.getEntity().getType()==EntityType.PLAYER) return;
        if ( e.getCause()==EntityDamageEvent.DamageCause.VOID) {
            e.getEntity().remove();
            Ostrov.log_warn("Удалена бесконечно падающая в бездну сущность "+ e.getEntity());
        }
 
    }
    
    
    
    
    
}
