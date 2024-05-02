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
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;




public class GroupsAndPermsDB implements InventoryProvider {
    
    
    
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ПРОФИЛЬ.glassMat).name("§8.").build());
    private final List<ClickableItem> buttons;
    
    public GroupsAndPermsDB(final List<ClickableItem> buttons) {
        this.buttons = buttons;
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
            
            content.set(2, 4, ClickableItem.empty(new ItemBuilder(Material.GLASS_BOTTLE)
                .name("§7нет записей!")
                .build()
            )); 
            
            return;
            
        }
            
        
        
        final Pagination pagination = content.pagination();
        

        

            

        

        


        pagination.setItems(buttons.toArray(new ClickableItem[buttons.size()]));
        pagination.setItemsPerPage(36);    


        if (!pagination.isLast()) {
            content.set(4, 8, ClickableItem.of(ItemUtils.nextPage, e 
                    -> {
                content.getHost().open(p, pagination.next().getPage()) ;
            }
            ));
        }

        if (!pagination.isFirst()) {
            content.set(4, 0, ClickableItem.of(ItemUtils.previosPage, e 
                    -> {
                content.getHost().open(p, pagination.previous().getPage()) ;
               })
            );
        }

        pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));
           

        
        

    }
    
    
    
    
    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }

    
    
    
    
    
}
