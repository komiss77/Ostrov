package ru.komiss77.version;

import java.util.Collection;
import java.util.function.Predicate;

import org.bukkit.entity.Player;

import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.notes.ThreadSafe;

//https://www.spigotmc.org/resources/nametagedit.3836/

@Deprecated
public interface INameTag {
@Deprecated
	@ThreadSafe
	void updateTag(final Oplayer op, final Player to);
@Deprecated
	@ThreadSafe
	void updateTag(final Oplayer op,  final Collection<? extends Player> to);
@Deprecated
	@ThreadSafe
	void updateTag(final String name, final String pfx, final String sfx, 
		final char clr, final Player to, final Predicate<Player> canSee);
@Deprecated
	@ThreadSafe
	void updateTag(final String name, final String pfx, final String sfx, 
		final char clr, final Collection<? extends Player> to, final Predicate<Player> canSee);
    
    /*
    void sendCreateTeamPacket (final Player player, final String name, final int param, final List<String> members); 
    
    void sendAddPlayerToTeampacket (final String name, final int param, final List<String> members); 

    void sendRemovePlayerFromTeampacket (final Player player);
    
    void sendModifyTeamPacket (final Player player);
    
    void sendRemoveTeamPacket (final Player player);
    */
    

    
    
}
