package ru.komiss77.modules.enchants;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemUtils;

public class EnchantListener implements Listener {
	
    public static final HashMap<Integer, ItemStack> projWeapons = new HashMap<>();
    
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onDamage (final EntityDamageEvent e) {
    	if (e instanceof EntityDamageByEntityEvent) {
    		final EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) e;
    		if (ee.getDamager() instanceof LivingEntity && ee.getCause() == DamageCause.ENTITY_ATTACK) {
        		final EntityEquipment eq = ((LivingEntity) ee.getDamager()).getEquipment();
        		final ItemStack it = eq.getItemInMainHand();
    			if (!ItemUtils.isBlank(it, true)) {
    				for (final Enchantment en : it.getEnchantments().keySet()) {
    					if (en instanceof CustomEnchant && Ostrov.random.nextInt(((CustomEnchant) en).getChance()) == 0) 
    						((CustomEnchant) en).getOnHit().accept(ee);
    				}
    			}
    		}
    	}
    	
    	if (e.getEntity() instanceof LivingEntity) {
    		final EntityEquipment eq = ((LivingEntity) e.getEntity()).getEquipment();
    		final HashSet<CustomEnchant> active = new HashSet<>();
    		for (final ItemStack it : eq.getArmorContents()) {
    			if (!ItemUtils.isBlank(it, true)) {
    				for (final Enchantment en : it.getEnchantments().keySet()) {
    					if (en instanceof CustomEnchant && Ostrov.random.nextInt(((CustomEnchant) en).getChance()) == 0) 
    						active.add((CustomEnchant) en);
    				}
    			}
    		}
    		
    		for (final CustomEnchant ce : active) {
    			ce.getOnArm().accept(e);
    		}
    	}
    }
    
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onProj (final ProjectileHitEvent e) {
    	if (e.getHitEntity() != null && e.getEntity().getShooter() instanceof LivingEntity) {
    		final ItemStack it = projWeapons.get(((LivingEntity) e.getEntity().getShooter()).getEntityId());
    		if (!ItemUtils.isBlank(it, true)) {
    			for (final Enchantment en : it.getEnchantments().keySet()) {
    				if (en instanceof CustomEnchant && Ostrov.random.nextInt(((CustomEnchant) en).getChance()) == 0) 
    					((CustomEnchant) en).getOnPrj().accept(e);
    			}
    		}
    	}
    }
    
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onShoot (final EntityShootBowEvent e) {
		final ItemStack it = e.getBow();
		if (!ItemUtils.isBlank(it, true)) {
			projWeapons.put(e.getEntity().getEntityId(), it);
			Ostrov.async(() -> projWeapons.remove(e.getEntity().getEntityId()), 200);
			for (final Enchantment en : it.getEnchantments().keySet()) {
				if (en instanceof CustomEnchant && Ostrov.random.nextInt(((CustomEnchant) en).getChance()) == 0) 
					((CustomEnchant) en).getOnSht().accept(e);
			}
		}
    }
    
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onBreak (final BlockBreakEvent e) {
    	final Player p = e.getPlayer();
    	final ItemStack it = p.getInventory().getItemInMainHand();
		if (!ItemUtils.isBlank(it, true)) {
			for (final Enchantment en : it.getEnchantments().keySet()) {
				if (en instanceof CustomEnchant && Ostrov.random.nextInt(((CustomEnchant) en).getChance()) == 0) 
					((CustomEnchant) en).getOnBrk().accept(e);
			}
		}
    }
}
