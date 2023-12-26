package ru.komiss77.version.empty;

import java.util.Collection;
import java.util.function.Predicate;

import org.bukkit.entity.Player;

import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.version.INameTag;


public class NameTag implements INameTag {

	@Override
	public void updateTag(final Oplayer op, final Player to) {
	}

	@Override
	public void updateTag(final Oplayer op, final Collection<? extends Player> to) {
	}

	@Override
	public void updateTag(final String name, final String pfx, final String sfx, 
		final char clr, final Player to, final Predicate<Player> canSee) {
	}

	@Override
	public void updateTag(final String name, final String pfx, final String sfx, 
		final char clr, final Collection<? extends Player> to, final Predicate<Player> canSee) {
	}
 
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    



    
}
