package ru.komiss77.modules.player.profile;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.OstrovDB;
import ru.komiss77.enums.Data;
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





public class FriendMail implements InventoryProvider {
    
    
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ДРУЗЬЯ.glassMat).name("§8.").build());
    
    private final List<ItemStack> mails;

    FriendMail(final List<ItemStack> mails) {
        this.mails = mails;
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
        
        //линия - разделитель
        content.fillRow(4, fill);
        
        //выставить иконки внизу
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }

        
        
        
        
        final Pagination pagination = content.pagination();;
        ArrayList<ClickableItem> menuEntry = new ArrayList<>();
        
        

            for (final ItemStack msgIcon : mails) {
                menuEntry.add(ClickableItem.empty(msgIcon));
            }

            menuEntry.add(ClickableItem.of(new ItemBuilder(Material.REDSTONE)
                    .name("§cОчистить почту")
                    .build(), e -> {
                        op.setData(Data.FRIENDS_MSG_OFFLINE, 0);
                        mails.clear();
                        OstrovDB.executePstAsync(p, "DELETE FROM `fr_messages` WHERE `reciever`='"+op.nik+"';");
                        reopen(p, content);
                    }
                ));
        
        
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

        
              /* if (op.friends.isEmpty()) {

                    content.set( SlotPos.of(1, 4), ClickableItem.of(new ItemBuilder(Material.GLASS_BOTTLE)
                                .setName("§7Ой!")
                                .lore("У Вас пока нет друзей!")
                                .lore("")
                                .lore("Чтобы добавить друга/подругу,")
                                .lore("§7встаньте рядом и")
                                .lore("§7клик на бутылочку.")
                                .lore("")
                                .build(), e-> {

                                }
                        ) 
                    );

                } else {*/

        



        

        
        /*
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        */


        

    }


    
    
    
    
    
    
    
    
    
}
