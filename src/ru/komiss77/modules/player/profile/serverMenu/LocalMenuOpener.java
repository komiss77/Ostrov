package ru.komiss77.modules.player.profile.serverMenu;

import org.bukkit.entity.Player;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.inventory.SmartInventory;


public class LocalMenuOpener {

    public static void open(final Player p, final Oplayer op) {
        
        switch (GM.GAME) {
            
            case AR:
                op.menu.current = SmartInventory
                    .builder()
                    .id(op.nik+op.menu.section.name())
                    .provider(new Arcaim())
                    .size(6, 9)
                    .title("Меню сервера Аркаим")
                    .build()
                    .open(p);
                break;
                
            case DA:
                op.menu.current = SmartInventory
                    .builder()
                    .id(op.nik+op.menu.section.name())
                    .provider(new Daaria())
                    .size(6, 9)
                    .title("Меню сервера Даария")
                    .build()
                    .open(p);
                break;
                
            case SE:
                op.menu.current = SmartInventory
                    .builder()
                    .id(op.nik+op.menu.section.name())
                    .provider(new Sedna())
                    .size(6, 9)
                    .title("Меню сервера Седна")
                    .build()
                    .open(p);
                break;
                
            default:
                op.menu.openLastSection(p);
                break;

        }
        
        
        
        
    }
    
    
    
    
    
    
    
}
