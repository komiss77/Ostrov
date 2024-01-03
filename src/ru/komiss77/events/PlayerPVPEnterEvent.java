package ru.komiss77.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerEvent;

public class PlayerPVPEnterEvent extends PlayerEvent {
    
    private static HandlerList handlers = new HandlerList();
	private final LivingEntity other;
	private final DamageCause cause;
	private final boolean attack;
	private boolean allowed;
    
    public PlayerPVPEnterEvent(final Player player, final LivingEntity other, final DamageCause cause, final boolean attack) {
    	super(player, false);
        this.other = other;
        this.cause = cause;
        this.attack = attack;
        allowed = true;
    }

    public LivingEntity getOther() {
        return other;
    }

    public DamageCause getCause() {
        return cause;
    }

    public boolean isPlAttack() {
        return attack;
    }
    
    public void setCancelled(final boolean cancel) {
    	this.allowed = !cancel;
    }
    
    @Override
    public boolean callEvent() {
    	Bukkit.getPluginManager().callEvent(this);
    	return allowed;
    }

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
