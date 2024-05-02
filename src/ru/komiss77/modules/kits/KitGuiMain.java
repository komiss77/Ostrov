package ru.komiss77.modules.kits;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;





public class KitGuiMain implements InventoryProvider{

    private static final ItemStack up = new ItemBuilder(Material.HORN_CORAL_FAN).build();
    private static final ItemStack side = new ItemBuilder(Material.VINE).build();
    private static final ItemStack down = new ItemBuilder(Material.TUBE_CORAL).build();;

        
    
    
    @Override
    public void init(final Player player, final InventoryContent contents) {
        player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //contents.fillRect(SlotPos.of(0), SlotPos.of(53), ClickableItem.empty(fill));
        //contents.fillRow(3, ClickableItem.empty(fill));
        //contents.fillRow(4, ClickableItem.empty(fill));
        contents.fillColumn(0, ClickableItem.empty(side));
        contents.fillColumn(8, ClickableItem.empty(side));
        contents.fillRow(0, ClickableItem.empty(up));
        contents.fillRow(5, ClickableItem.empty(down));
        final Pagination pagination = contents.pagination();
        
        
       // contents.set(0, 4, ClickableItem.empty(new ItemBuilder(Material.STONECUTTER)
             //   .name("§7Наборов на сервере: §f"+KitManager.kits.size())
              //  //.lore("§7Состояние: §e"+arena.state.toString())
              //  .build()));
        
        final Oplayer op = PM.getOplayer(player);
        
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();
        
        
        
        
        ItemStack item;
        String giveInfo1="";
        String giveInfo2="";
        
        for (Kit kit : KitManager.kits.values()) {
            
            if (!kit.enabled) continue; //добавляем только включенные
            
            if (kit.needPermission && !player.hasPermission("ostrov.kit."+kit.name) && !player.hasPermission("ostrov.kit.*")) {
                
                giveInfo1 = "§cтребуется право §5ostrov.kit."+kit.name;
                giveInfo2 = "§cдля доступа к набору!";
                
            } else if ( kit.accesBuyPrice==0 ) {
                
                //if (PM.Kit_has_acces(player.getName(), kit.name)) {
                    
                    final int secondLeft = KitManager.getSecondLetf(player, kit);
                    if (secondLeft>0) {
                        giveInfo1 = "§cПолучить можно через "+ApiOstrov.secondToTime(secondLeft);
                    } else {
                        giveInfo1 = "§fЛКМ §e- Получить набор.";
                    }
                    giveInfo2 = "§7цена получения: §5"+ (kit.getPrice>0 ? kit.getPrice+" §7лони" : "бесплатно");
                    
               // } else {
                //    giveInfo1 = "§fЛКМ §e- Получить право доступа.";
                //    giveInfo2 = "§7(бесплатно)";
               // }

                
            } else if ( kit.accesBuyPrice>=0 )  {
                
                
                if (op.hasKitAcces(kit.name)) {
                    
                    final int secondLeft = KitManager.getSecondLetf(player, kit);
                    if (secondLeft>0) {
                        giveInfo1 = "§cПолучить можно через "+ApiOstrov.secondToTime(secondLeft);
                    } else {
                        giveInfo1 = "§fЛКМ §e- Получить набор.";
                    }
                    giveInfo2 = "§7цена получения: §5"+ (kit.getPrice>0 ? kit.getPrice+" §7лони" : "бесплатно");
                    
                } else {
                    
                    giveInfo1 = "§fЛКМ §e- Покупка права доступа.";
                    giveInfo2 = "§7цена покупки: §5"+kit.accesBuyPrice+" §7лони";
                    
                }
                
            }
            
            
            
            
            
            item = new ItemBuilder(kit.logoItem)
                    .addLore( "" )
                    .addLore( kit.rarity.displayName )
                    .addLore( "" )
                    .addLore( kit.enabled ? "§aАктивен§7, "+(kit.needPermission ? "§eтребуется право" : "§aдоступен всем") : "§сЗаблокирован" )
                    .addLore( "§7цена доступа: "+(kit.accesBuyPrice==0 ? "§8бесплатно" : "§e"+kit.accesBuyPrice+" §7лони") )
                    .addLore( "§7цена получения: "+(kit.getPrice==0 ? "§8бесплатно" : "§e"+kit.getPrice+" §7лони") )
                    .addLore( "§7продажа доступа: "+(kit.accesSellPrice==0 ? "§8никакой выгоды" : "§b"+kit.accesSellPrice+" §7лони") )
                    .addLore( kit.delaySec == 0 ? "§8интервал получения не установлен" : "§7интервал получения: §6"+ApiOstrov.secondToTime(kit.delaySec) )
                    .addLore( "" )
                    .addLore( "§fПКМ §7- §eпосмотреть состав" )
                    .addLore( giveInfo1 )
                    .addLore( giveInfo2 )
                    .addLore( (kit.accesBuyPrice>0 && op.hasKitAcces(kit.name)) ? "§9Shift+ПКМ §7- продать доступ за §e"+kit.accesSellPrice+" §7лони" : "" )
                    .build();
            
            
            
            
            
            menuEntry.add(ClickableItem.of(item, e -> {
                final Kit clickedKit = KitManager.kits.get(TCUtils.stripColor(e.getCurrentItem().getItemMeta().displayName()));
//System.out.println("-- ClickableItem clickedKit="+clickedKit+" name="+ ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()) );
               
                if (clickedKit==null) return;
                if (e.isLeftClick()) { //проверка на выключен везде!!
//System.out.println("-- ClickableItem clickedKit="+clickedKit+" name="+ ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()) );
                    if (kit.accesBuyPrice>0 && !op.hasKitAcces(clickedKit.name)) {
                        player.closeInventory();
                        player.performCommand("kit buyacces "+clickedKit.name);
                    } else {
                        player.closeInventory();
                        player.performCommand("kit give "+clickedKit.name);
                    }
                    //reopen(player, contents);
                }else if (e.isShiftClick()) {
                    if (op.hasKitAcces(clickedKit.name)) {
                        player.closeInventory();
                        player.performCommand("kit sellacces "+clickedKit.name);
                    }
                    //reopen(player, contents);
                }
                 else if (e.isRightClick()) {
                    KitManager.openKitPrewiev(player, kit);
                    //reopen(player, contents);
                } 
            }));  
            
        }
        
        pagination.setItems(menuEntry.toArray(new ClickableItem[menuEntry.size()]));
        pagination.setItemsPerPage(28);
        

        
        //прятать если нет
        if (!pagination.isFirst()) {
            contents.set( 2, 0, ClickableItem.of( new ItemBuilder(Material.PINK_STAINED_GLASS_PANE).name("назад").build(), p4 
                    -> contents.getHost().open(player, pagination.previous().getPage()) )
            );
            contents.set( 3, 0, ClickableItem.of( new ItemBuilder(Material.PINK_STAINED_GLASS_PANE).name("назад").build(), p4 
                    -> contents.getHost().open(player, pagination.previous().getPage()) )
            );
        }
        
        if (!pagination.isLast()) {
            contents.set( 2, 8, ClickableItem.of( new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("далее").build(), p4 
                    -> contents.getHost().open(player, pagination.next().getPage()) )
            );
            contents.set( 3, 8, ClickableItem.of( new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("далее").build(), p4 
                    -> contents.getHost().open(player, pagination.next().getPage()) )
            );
        }
        
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1)).allowOverride(false));
        
        
        
        

 
        
    
    
    
    }
    

        

    
    

    
    
    
    
    
    
    
    
}
