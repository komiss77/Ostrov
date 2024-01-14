package ru.komiss77.modules.player.mission;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;


public class MissionWithdrawViewMenu implements InventoryProvider {
    
    private final List<ClickableItem> buttons;
    
    public MissionWithdrawViewMenu(final List<ClickableItem> buttons) {
        this.buttons = buttons;
    }
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.SCULK_VEIN).name("§8.").build());

    
    @Override
    public void init(final Player p, final InventoryContent content) {
        
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 5, 5);
        content.fillRect(0,0, 4,8, fill);
        

       
        
        if (buttons.isEmpty()) {
            content.set(13, ClickableItem.empty(new ItemBuilder(Material.GLASS_BOTTLE)
                .name("§7нет записей!")
                .build()
            )); 
            return;
        }
         
        final Oplayer op = PM.getOplayer(p);
        
        
        final Pagination pagination = content.pagination();

        pagination.setItems(buttons.toArray(ClickableItem[]::new));
        pagination.setItemsPerPage(21);    


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

        pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1)).allowOverride(false));


          

    }


    
    
    
    
    
    
    
    
    
}
