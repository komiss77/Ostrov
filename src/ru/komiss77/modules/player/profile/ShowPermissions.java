package ru.komiss77.modules.player.profile;


import java.util.ArrayList;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

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




public class ShowPermissions implements InventoryProvider {
    
    
    
    
    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ПРОФИЛЬ.glassMat).name("§8.").build());

    
    public ShowPermissions() {

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

        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();   
        
        for (String group : op.getGroups()) {
            menuEntry.add(ClickableItem.empty(new ItemBuilder(Material.EMERALD)
                .name("§7Группа §e"+group )
                .build()));            
        }
        
        
        for (String limitName : op.limits.keySet()) {
            menuEntry.add(ClickableItem.empty(new ItemBuilder(Material.PRISMARINE_CRYSTALS)
                .name("§7Лимит для §e"+limitName+" §7: §a"+op.limits.get(limitName) )
                .build()));            
        }
        
        
        
        
        TreeMap <String,Boolean>perm = new TreeMap<>();
        
        for (PermissionAttachmentInfo  attacement_info : p.getEffectivePermissions()) {
            perm.put(attacement_info.getPermission(), attacement_info.getValue());
        }
        
        
        if (perm.isEmpty()) {
            
            menuEntry.add( ClickableItem.empty(new ItemBuilder(Material.GLASS_BOTTLE)
                .name("§7нет записей с пермишенами!")
                .build()
            )); 
            
            return;
            
        }
            
        
        
        final Pagination pagination = content.pagination();
        
        
        for (String  s : perm.keySet()) {
            menuEntry.add(ClickableItem.empty(new ItemBuilder(perm.get(s) ? Material.LIME_DYE : Material.RED_DYE)
                .name("§7"+s )
                .build()));            
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
