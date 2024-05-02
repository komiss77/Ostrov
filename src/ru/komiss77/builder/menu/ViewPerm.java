package ru.komiss77.builder.menu;

import java.util.ArrayList;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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





public class ViewPerm implements InventoryProvider {
    
    
    
   // private static final ItemStack fill = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build();;
    private final Player target;
    

    
    public ViewPerm(final Player target) {
        this.target = target;
    }
    
    
    
    @Override
    public void init(final Player p, final InventoryContent contents) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
       // contents.fillRect(0,0,  4,8, ClickableItem.empty(fill));
        final Pagination pagination = contents.pagination();
        
        
        final Oplayer targetOp = PM.getOplayer(target);
        
        
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();
        

        
        for (String group : targetOp.getGroups()) {

            menuEntry.add(ClickableItem.empty(new ItemBuilder(Material.EMERALD)
                .name("§7Группа §e"+group )
                .build()));            
            
        }
        
        for (String limitName : targetOp.limits.keySet()) {
            menuEntry.add(ClickableItem.empty(new ItemBuilder(Material.PRISMARINE_CRYSTALS)
                .name("§7Лимит для §e"+limitName+" §7: §a"+PM.getOplayer(target).limits.get(limitName) )
                .build()));            
            
        }
        
        TreeMap <String,Boolean>perm = new TreeMap<String, Boolean>();
        
        for (PermissionAttachmentInfo  attacement_info : target.getEffectivePermissions()) {
            perm.put(attacement_info.getPermission(), attacement_info.getValue());
            //menuEntry.add(ClickableItem.empty(new ItemBuilder(attacement_info.getValue() ? Material.LIME_DYE : Material.RED_DYE)
            //    .name("§7"+attacement_info.getPermission() )
            //    .build()));            
            
        }
        
        for (String  s : perm.keySet()) {
            menuEntry.add(ClickableItem.empty(new ItemBuilder(perm.get(s) ? Material.LIME_DYE : Material.RED_DYE)
                .name("§7"+s )
                .build()));            
            
        }
        
     
            
            
        
        
        

        
        
            
            
        
        
        
        
        
        
        
        
        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(45);
        

        

        
        
        contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("закрыть").build(), e -> 
            p.closeInventory()
        ));
        

        
        if (!pagination.isLast()) {
            contents.set(5, 8, ClickableItem.of(ItemUtils.nextPage, e 
                    -> contents.getHost().open(p, pagination.next().getPage()) )
            );
        }

        if (!pagination.isFirst()) {
            contents.set(5, 0, ClickableItem.of(ItemUtils.previosPage, e 
                    -> contents.getHost().open(p, pagination.previous().getPage()) )
            );
        }
        
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));
        

        
        

    }
    
    
    
    
    
    public static class SelectPlayer implements InventoryProvider {



        private static final ItemStack fill = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).name("§8.").build();;



        public SelectPlayer() {
        }



        @Override
        public void init(final Player p, final InventoryContent contents) {
            p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
            contents.fillRect(0,0,  4,8, ClickableItem.empty(fill));
            final Pagination pagination = contents.pagination();

            final ArrayList<ClickableItem> menuEntry = new ArrayList<>();



            for (Player target : Bukkit.getOnlinePlayers()) {

                menuEntry.add(ClickableItem.of(new ItemBuilder( Material.PLAYER_HEAD )
                    .name("§7"+target.getName() )
                    .build(), e -> {
                        p.performCommand("operm "+target.getName());
                    }));            

            }


            pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
            pagination.setItemsPerPage(21);


            contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("закрыть").build(), e -> 
                p.closeInventory()
            ));



            if (!pagination.isLast()) {
                contents.set(5, 8, ClickableItem.of(ItemUtils.nextPage, e 
                        -> contents.getHost().open(p, pagination.next().getPage()) )
                );
            }

            if (!pagination.isFirst()) {
                contents.set(5, 0, ClickableItem.of(ItemUtils.previosPage, e 
                        -> contents.getHost().open(p, pagination.previous().getPage()) )
                );
            }

            pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1)).allowOverride(false));


        }


    }


}
