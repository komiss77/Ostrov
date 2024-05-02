package ru.komiss77.modules.player.profile;


import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
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





public class IgnoreList implements InventoryProvider {
    
    
    
   private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ПРОФИЛЬ.glassMat).name("§8.").build());
    

    public IgnoreList() {
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
        
        

        
        
        
        
        final Pagination pagination = content.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();        
        
        
        
        
        
        
        
        
        for (final String name : op.getBlackListed()) {

                final ItemStack friend_item = new ItemBuilder(Material.PLAYER_HEAD)
                        .name(name)
                        .addLore("")
                        .addLore("§7Лкм - удалить из списка")
                        .addLore("")
                        .build();

                menuEntry.add(ClickableItem.of(friend_item, e-> {
                        op.removeBlackList(name);
                        ApiOstrov.executeBungeeCmd(p, "ignore del "+name);
                        reopen(p, content);
                    }
                ));


            }
        
        
        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
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


    
    
    
    

    
    
}
