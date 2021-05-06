package ru.komiss77.utils;

import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;







//используется в  RegionGui !!! НЕ ПЕРЕМЕЩАТЬ!! 

public class PlayerChatInput implements Listener {
    

    @Deprecated
    public static void get(final Player player, final Consumer<String> result) {
       PlayerInput.get(player, result);
    }
    
}
