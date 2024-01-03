package ru.komiss77.modules.kits;

import java.util.Iterator;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;









public class KitComponentEditor implements InventoryProvider{

    private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).build();
    private static final ItemStack emptySlot = new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE).name("§8>пустой слот<").build();
    private final Kit kit;
    //KitManager kitManager;//KitManager kitManager;//KitManager kitManager;//KitManager kitManager;//KitManager kitManager;//KitManager kitManager;//KitManager kitManager;//KitManager kitManager;
    
    KitComponentEditor(final Kit kit) {
        this.kit = kit;
    }
        
    
    
    @Override
    public void init(final Player player, final InventoryContent contents) {
        player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

        
        contents.fillRow(0, ClickableItem.empty(fill));
        contents.fillRow(4, ClickableItem.empty(fill));
        
        
       
        
            

        Iterator <ItemStack> it = kit.items.iterator();
        
        for (int i=0; i<27; i++) {
            
            if (it.hasNext()) {
                
                contents.add( ClickableItem.of( it.next(), e -> {
                    
                            if (e.isLeftClick()) {
                               /* if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                                    kit.items.remove(e.getCurrentItem());
                                    kit.items.add(e.getCursor().clone());
                                    kit.modifyed = true;
                                    e.getView().getBottomInventory().addItem(new ItemStack[] { e.getCursor() }); //вернуть в нижний инв.
                                    //e.getView().getBottomInventory().setItem(e.getSlot(), e.getCursor());
                                    e.getView().setCursor(new ItemStack(Material.AIR));
                                    reopen(player, contents);
                                } else {*/
                                if (e.getCurrentItem().getAmount()<e.getCurrentItem().getMaxStackSize()) {
                                    kit.modifyed = true;
                                    kit.items.remove(e.getCurrentItem());
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount()+1);
                                    kit.items.add(e.getCurrentItem());
                                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                                    reopen(player, contents);
                                } else {
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                                }
                                //}
                            } else if (e.isShiftClick()) {
                                
//System.out.println("isShiftClick clicked"+e.getCurrentItem()+" size = "+kit.items.size()+" contains? "+kit.items.contains(e.getCurrentItem()));        
                                //e.setCancelled(true);
                                kit.modifyed = true;
                                kit.items.remove(e.getCurrentItem());
                                e.setCurrentItem(emptySlot);
                                //if (e.getCurrentItem().getAmount()==1) {
                               //     kit.items.remove(e.getCurrentItem());
                              //  } else {
                              //      for (ItemStack is : kit.items) {
                              //          if (is==e.getCurrentItem()) {
                               //             is.setAmount(1);
                               //             break;
                               //         }
                               //     }
                               // }
//System.out.println("isShiftClick modifyed"+" size = "+kit.items.size()+" contains? "+kit.items.contains(e.getCurrentItem()));        
                                //e.getClickedInventory().setItem(e.getSlot(), emptySlot);
                                //e.getView().setCursor(new ItemStack(Material.AIR));
                                reopen(player, contents);
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1);
                                
                            } else if (e.isRightClick()) {
//System.out.println("isShiftClick clicked"+e.getCurrentItem()+" size = "+kit.items.size()+" contains? "+kit.items.contains(e.getCurrentItem()));        
                                //e.setCancelled(true);
                                if (e.getCurrentItem().getAmount()>1) {
                                    kit.modifyed = true;
                                    kit.items.remove(e.getCurrentItem());
                                    e.getCurrentItem().setAmount(e.getCurrentItem().getAmount()-1);
                                    kit.items.add(e.getCurrentItem());
                                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                                    reopen(player, contents);
                                } else {
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                                }
                                //kit.items.remove(e.getCurrentItem());
                                //kit.modifyed = true;
//System.out.println("isRightClick size = "+kit.items.size()+" contains? "+kit.items.contains(e.getCurrentItem()));        
                                //e.getClickedInventory().setItem(e.getSlot(), emptySlot);
                                //e.getView().setCursor(new ItemStack(Material.AIR));
                               // player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1);
                            }

                        }
                    )
                );  
            
            } else {
                
                contents.add( ClickableItem.of( emptySlot, e -> {
                            if (e.isLeftClick()) { 
                                if (e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                                    kit.items.add(e.getCursor().clone());
                                    kit.modifyed = true;
                                    //e.getView().getBottomInventory().addItem(new ItemStack[] { e.getCursor() });
                                    //e.getView().getBottomInventory().setItem(e.getSlot(), e.getCursor());
                                    e.getView().setCursor(new ItemStack(Material.AIR));
                                    reopen(player, contents);
                                } else {
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                                }
                            } else if (e.isShiftClick()) {
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                            }
                           // return;
                        }
                    )
                );  
                
            }
        }
        


        
        
        
        
       
        
        contents.set(0, 4, ClickableItem.empty(new ItemBuilder(kit.logoItem)
                .addLore("")
                .addLore("§bПереместите предметы из своего инвентаря")
                .addLore("§bна свободный слот (бутылочка)")
                .addLore("")
                .addLore("§7ЛКМ - §aколличество+1")
                .addLore("§7ПКМ - §cколличество-1")
                .addLore("§7shift+ПКМ - §cУдалить предмет.")
                .addLore("")
                .addLore( kit.modifyed ? "§eНе забудьте сохранить изменения!" : "")
                .build()));
        
        
        
        contents.set( 5, 2, ClickableItem.of( new ItemBuilder(Material.GRINDSTONE).name("§eредактировать настройки").build(), e ->
                KitManager.openKitSettingsEditor(player, kit)
                // SmartInventory.builder().id("KitSettingsEditor:"+player.getName()). provider(new KitSettingsEditor(kit)). size(6, 9). title("§4Настройки набора §6"+kit.name). build() .open(player)
        ) );
        
        
        
        if (kit.modifyed) {
            
            contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR)
                    .name("гл.меню")
                    .addLore("§cВНИМАНИЕ!")
                    .addLore("§cБез сохранения на диск")
                    .addLore("§cданные будут утеряны")
                    .addLore("§cпосле перезагрузки сервера.")
                    .build(), e ->
                            KitManager.openKitEditMain(player)
                    //-> SmartInventory.builder().id("KitEditMain:"+player.getName()). provider(new KitEditMain(Ostrov.kitManager)). size(6, 9). title("§4Администрирование наборов"). build() .open(player)
            ) );
            
                contents.set( 5, 6, ClickableItem.of( new ItemBuilder(Material.NETHER_STAR).name("сохранить на диск").build(), e -> {
                    KitManager.saveKit((Player) e.getWhoClicked(), kit);
                    reopen(player, contents);
                }
            ) );
                
        } else {
            
            contents.set( 5, 4, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR)
                    .name("гл.меню")
                    .build(), e ->
                            KitManager.openKitEditMain(player)
                    //-> SmartInventory.builder().id("KitEditMain:"+player.getName()). provider(new KitEditMain(Ostrov.kitManager)). size(6, 9). title("§4Администрирование наборов"). build() .open(player)
            ) );
            
        }
        
        

 
        
        

        
        
        
        
        
    
    
    
    }
    

        

    
    


    
    
    
    
    
    
    
    
}
