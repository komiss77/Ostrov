package ru.komiss77.modules.player.mission;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class ProfileManageMenu implements InventoryProvider {
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ВОЗМОЖНОСТИ.glassMat).name("§8.").build());
    private final List<ClickableItem> buttonsCurrent;
    private final List<ClickableItem> buttonsDone;
    

    
    public ProfileManageMenu(final List<ClickableItem> buttonsCurrent, final List<ClickableItem> buttonsDone) {
        this.buttonsCurrent = buttonsCurrent;
        this.buttonsDone = buttonsDone;
    }
    
    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }


    
    
    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //content.fillRect(0,0,  5,8, ClickableItem.empty(fill));
        
        final Oplayer op = PM.getOplayer(p);
        //final ProfileManager pm = op.menu;

        //линия - разделитель
        content.fillRow(4, fill);
        
        //выставить иконки внизу
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }
        
        
        if (op.isGuest) {

            content.set(1,4, ClickableItem.empty(new ItemBuilder(Material.BARRIER)
                .name("§7Миссия невыполнима")
                .addLore("")
                .addLore("")
                .addLore( "§6Гости не могут" )
                .addLore("§6выполнять миссии!")
                .addLore("§6Вам нужно зарегаться!")
                .build()
            ));
            
        } else if ( (buttonsCurrent==null || buttonsCurrent.isEmpty()) && (buttonsDone==null || buttonsDone.isEmpty())) {
            
            content.set(1,4, ClickableItem.empty(new ItemBuilder(Material.GLASS_BOTTLE)
                .name("§7Миссия невыполнима")
                .addLore("")
                .addLore("")
                .addLore( "§6Нет активных миссий" )
                .addLore("")
                .build()
            ));

        } else {
            for (final ClickableItem icon : buttonsCurrent) {
                content.add(icon);
            }
            if (buttonsDone!=null) {
                for (final ClickableItem icon : buttonsDone) {
                    content.add(icon);
                }
            }
        }


        
               
        
        

        
 

    }


    
    
    
    
    
    
    
    
    
}
