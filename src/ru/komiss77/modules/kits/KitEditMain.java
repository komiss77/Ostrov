package ru.komiss77.modules.kits;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;









public class KitEditMain implements InventoryProvider{

    private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).build();;
  
    
    
    @Override
    public void init(final Player player, final InventoryContent contents) {
        player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        contents.fillRow(4, ClickableItem.empty(fill));

        final Pagination pagination = contents.pagination();

        
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();
        
        
        
        
        
        
        
        ItemStack item;
        for (Kit kit : KitManager.kits.values()) {
            
            
            
            
            item = new ItemBuilder(kit.logoItem)
                    .addLore( kit.rarity.displayName )
                    .addLore( "" )
                    .addLore( kit.modifyed ? "§cНЕ СОХРАНЁН!" : ""  )
                    .addLore( kit.enabled ? "§2Активен" : "§4Заблокирован"  )
                    .addLore( kit.needPermission ? "§eтребуется право" : "§aдоступен всем" )
                    .addLore( "§7цена доступа: "+(kit.accesBuyPrice==0 ? "§8бесплатно" : "§e"+kit.accesBuyPrice+" §7лони") )
                    .addLore( "§7цена получения: "+(kit.getPrice==0 ? "§8бесплатно" : "§e"+kit.getPrice+" §7лони") )
                    .addLore( "§7продажа доступа: "+(kit.accesSellPrice==0 ? "§8никакой выгоды" : "§b"+kit.accesSellPrice+" §7лони") )
                    .addLore( kit.delaySec == 0 ? "§7интервал не установлен" : "§7интервал получения: §6"+ApiOstrov.secondToTime(kit.delaySec) )
                    .addLore( "" )
                    .addLore( "§fЛКМ §7- настройки набора" )
                    .addLore( "§fПКМ §7- изменить содержимое" )
                    .addLore( "§fшифт+ПКМ §7- клонировать набор" )
                    .addLore( "" )
                    .build();
            
            
            
            
            
            menuEntry.add(ClickableItem.of(item, e -> {
                //final Kit clickedKit = KitManager.kits.get(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()));
                //if (clickedKit==null) return;
                if (e.isLeftClick()) {
                    KitManager.openKitSettingsEditor(player,kit);
//System.out.println("KitEditMain isLeftClick kit="+kit.name+" ");        
                    //reopen(player, contents);
                }  else if (e.isShiftClick()) {
                    final Kit kitClone = kit.cloneWithNewName("");
                    kitClone.modifyed=true;
                    KitManager.kits.put(kitClone.name, kitClone);
//System.out.println("KitEditMain isShiftClick kit="+kit.name+" new kit="+kitClone.name);            
                    //SmartInventory.builder().id("KitSettingsEditor:"+player.getName()). provider(new KitSettingsEditor(kit)). size(6, 9). title("§4Настройки набора §6"+kitClone.name). build() .open(player);
                            
                    //player.performCommand("kit sellacces "+clickedKit.name);
                    //player.closeInventory();
                    reopen(player, contents);
                } else if (e.isRightClick()) {
                    KitManager.openKitKitComponentEditor(player, kit);
//System.out.println("KitEditMain isRightClick kit="+kit.name+" ");            
                    //reopen(player, contents);
                }

            }));  
            
        }
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(36);
        

        
        if (!pagination.isFirst()) {
            contents.set( 5, 0, ClickableItem.of( new ItemBuilder(Material.ARROW).name("назад").build(), p4 
                    -> contents.getHost().open(player, pagination.previous().getPage()) )
            );
        }
        
        if (!pagination.isLast()) {
            contents.set( 5, 8, ClickableItem.of( new ItemBuilder(Material.ARROW).name("далее").build(), p4 
                    -> contents.getHost().open(player, pagination.next().getPage()) )
            );
        }
        
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));
        
        
        
        
        

        
        
        
        
        

        

            contents.set(5, 2, ClickableItem.of( new ItemBuilder(Material.BOOKSHELF)
                .name("§aСоздать новый набор")
                .build(), e -> {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 5);
                    PlayerInput.get(InputType.ANVILL, player, kitName -> {
                        if(kitName.length()>16 || !ApiOstrov.checkString(kitName,true,true)) {
                            player.sendMessage("§cНедопустимое имя!");
                        } else if ( KitManager.kits.containsKey(kitName) ) {
                            player.sendMessage("§cТакой набор уже есть!");
                        } else {
                            Kit kit = new Kit(kitName);
                            kit.modifyed=true;
                            KitManager.kits.put(kitName, kit);
                            KitManager.openKitSettingsEditor(player, kit);
                        }
                    }, "название..");
                    
            }));
        
        
        
        
        
 
        
    
    
    
    }
    

        
    

    
    
    

    
    
    
    
    
    
    
    
}
