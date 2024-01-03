package ru.komiss77.modules.player.profile;


import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;




public class ShowJournal implements InventoryProvider {
    
    
    
    private final List<ClickableItem> buttons;
    private final int page;
    private final boolean hasNext;
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ПРОФИЛЬ.glassMat).name("§8.").build());

    
    public ShowJournal(final List<ClickableItem> buttons, final int page, final boolean hasNext) {
        this.buttons = buttons;
        this.page = page;
        this.hasNext = hasNext;
    }
    
    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }

    
    
    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        
        final Oplayer op = PM.getOplayer(p);
        
        //линия - разделитель
        content.fillRow(4, fill);
        
        //выставить иконки внизу
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }

        
        
        
        
        
        
        if (buttons.isEmpty()) {
            
            content.add(ClickableItem.empty(new ItemBuilder(Material.GLASS_BOTTLE)
                .name("§7нет записей!")
                .build()
            )); 
            
        } else {
            
            for (final ClickableItem head : buttons) {
                content.add(head);
            }
            
        }

        

        


        if (hasNext) {
            content.set(4, 8, ClickableItem.of(ItemUtils.nextPage, e 
                    -> op.menu.openJournal(p, page+1) )
            );
        }

        if (page>0) {
            content.set(4, 0, ClickableItem.of(ItemUtils.previosPage, e 
                    -> op.menu.openJournal(p, page-1) )
            );
        }
        
           

        
        

    }
    
    
    
    
    
    
    
    
    
    
}
