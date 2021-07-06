package ru.komiss77.version;

import java.util.Collection;
import java.util.List;
import org.bukkit.entity.Player;

//https://www.spigotmc.org/resources/nametagedit.3836/

public interface INameTag {

    void sendNameTag (final Player player, final String name, final int param, final List<String> members); 
    
    void sendNameTag (final Player player, final String name, final String prefix, String suffix, final int param, final Collection<?> players);
    
    void sendNameTag (final String name, final int param, final List<String> members); 
    
    void sendNameTag (final String name, final String prefix, String suffix, final int param, final Collection<?> players);
    
    

    
    
}
