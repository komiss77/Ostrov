package ru.komiss77.modules.kits;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;









public class KitPrewiev implements InventoryProvider{

    private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).build();
    private final Kit kit;
    //KitManager kitManager;//KitManager kitManager;//KitManager kitManager;//KitManager kitManager;//KitManager kitManager;//KitManager kitManager;//KitManager kitManager;//KitManager kitManager;
    
    KitPrewiev(final Kit kit) {
        this.kit = kit;
    }
        
    
    
    @Override
    public void init(final Player player, final InventoryContent contents) {
        player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //contents.fillRect(SlotPos.of(0), SlotPos.of(53), ClickableItem.empty(fill));
        //contents.fillRow(3, ClickableItem.empty(fill));
        //contents.fillRow(4, ClickableItem.empty(fill));
        contents.fillBorders(ClickableItem.empty(fill));
        
        
        
        
        //final Pagination pagination = contents.pagination();
        
        
        contents.set(0, 4, ClickableItem.empty( kit.logoItem ) );
        
       
        //final ArrayList<ClickableItem> menuEntry = new ArrayList<>();
        
            

            
        for (ItemStack item : kit.items) {
            contents.add( ClickableItem.empty(item) );  
            //menuEntry.add( ClickableItem.empty(item) );  
            
        }
        
        //pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        //pagination.setItemsPerPage(18);
        

        
        contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("гл.меню").build(), e ->
                KitManager.openGuiMain(player)
                //-> SmartInventory.builder().id("KitGuiMain:"+player.getName()). provider(new KitGuiMain(Ostrov.kitManager)). size(6, 9). title("§2Наборы"). build() .open(player)
        
        ) );
        
        //contents.set( 4, 6, ClickableItem.of( new ItemBuilder(Material.MAP).name("далее").build(), p4 
        //        -> contents.getHost().open(player, pagination.next().getPage()) )
        //);
        
        //contents.set( 4, 2, ClickableItem.of( new ItemBuilder(Material.MAP).name("назад").build(), p4 
        //        -> contents.getHost().open(player, pagination.previous().getPage()) )
        //);
        
       // pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1)).allowOverride(false));
        
        
 
        
        

        
        
        
        
        
 
        
    
    
    
    }
    

        

    
    
    

    
    
    
    
    
    
    
    
}
