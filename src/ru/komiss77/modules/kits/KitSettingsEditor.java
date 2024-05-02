package ru.komiss77.modules.kits;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import ru.komiss77.ApiOstrov;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SlotPos;









public class KitSettingsEditor implements InventoryProvider{

    private static final ItemStack fill = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).build();
    private final Kit kit;
    //KitManager kitManager;//KitManager kitManager;//KitManager kitManager;//KitManager kitManager;//KitManager kitManager;//KitManager kitManager;//KitManager kitManager;//KitManager kitManager;
    
    KitSettingsEditor(final Kit kit) {
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
       
        
        
        
        
        
        
        
        
        contents.set(SlotPos.of(1, 2), ClickableItem.of(new ItemBuilder(kit.logoItem.getType())
                .name("§7Установить иконку")
                .addLore("§7Ткните сюда предметом из инвентаря")
                .addLore("§7для смены иконки")
                .build(), e -> {
                    if (e.isLeftClick() && e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                        //e.setCancelled(true);
                        kit.logoItem.setType(e.getCursor().getType());
                        kit.modifyed = true;
                        e.getView().getBottomInventory().addItem(new ItemStack[] { e.getCursor() });
                        e.getView().setCursor(new ItemStack(Material.AIR));
                        reopen(player, contents);
                    }
            //return;
        }));
        
        
        contents.set(SlotPos.of(1, 4), new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.NAME_TAG)
                .name("§7Изменить название")
                .addLore("§7Сейчас: §6" + kit.name)
                .build(), kit.name, newName -> {
                    
                    if(newName.length()>16 || !ApiOstrov.checkString(newName,true,true) ) {
                            player.sendMessage("§cНедопустимое имя!");
                        } else if ( KitManager.kits.containsKey(newName) ) {
                            player.sendMessage("§cТакой набор уже есть!");
                        } else {
                            final Kit newKit = kit.cloneWithNewName(newName);
                            newKit.name = newName;
                            KitManager.saveKit(player, newKit);
                            KitManager.deleteKit(player, kit.name);
                            KitManager.openKitSettingsEditor(player, newKit);
                            //SmartInventory.builder().id("KitSettingsEditor:"+player.getName()). provider(new KitSettingsEditor(newKit)). size(6, 9). title("§4Настройки набора §6"+newKit.name). build() .open(player);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                        }
                   // return;
                }));
        
        //Описание
        final List<Component> lrl = kit.logoItem.getItemMeta().lore();
        contents.set(SlotPos.of(1, 6), ClickableItem.of(new ItemBuilder(Material.BOOK)
                .name("§7Описание")
                .addLore("§7Текущее:")
                .addLore(lrl)
                .addLore("")
                .addLore("§fЛКМ §aдобавить строку")
                .addLore("§fПКМ §cудалить последнюю строку.")
                .build(), e -> {

                    if ( e.isRightClick()) {
                        if (lrl.size()>1) {
                            kit.modifyed = true;
                            final List<Component>addLore = new ArrayList<>(lrl);
                            addLore.remove(addLore.size()-1);
                            kit.logoItem = new ItemBuilder(kit.logoItem).setLore(addLore).build();
                            reopen(player, contents);
                        }

                    } else if ( e.isLeftClick()) {
                    	PlayerInput.get(InputType.ANVILL, player, value -> {
                            kit.modifyed = true;
                            kit.logoItem = new ItemBuilder(kit.logoItem).setLore(value).build();
                            reopen(player, contents);
                        }, "строка..");

                    }
                }
        ));
       

        
        

        
        
        

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        contents.set(SlotPos.of(2, 4), ClickableItem.of(new ItemBuilder(Material.REPEATER)
                .name("§7Уровень набора")
                .addLore("§7 Сейчас: "+kit.rarity.displayName)
                .addLore("§fЛКМ §aменять")
                .build(), e -> {

                    if ( e.isLeftClick()) {
                        kit.modifyed = true;
                        kit.rarity = KitManager.Rarity.rotate(kit.rarity);
                        reopen(player, contents);

                    }
                }
        ));
       
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        if (kit.enabled) {
            contents.set(SlotPos.of(3, 3), ClickableItem.of(new ItemBuilder(Material.GREEN_CONCRETE)
                    .name("§2Активен")
                    .addLore("§7ЛКМ - выключить")
                    .build(), e -> {
                        if ( e.isLeftClick()) {
                            kit.enabled = false;
                            kit.modifyed = true;
                            reopen(player, contents);
                        }
                    }
            ));
        } else {
            contents.set(SlotPos.of(3, 3), ClickableItem.of(new ItemBuilder(Material.RED_CONCRETE)
                    .name("§4Выключен")
                    .addLore("§7ЛКМ - включить")
                    .build(), e -> {
                        if ( e.isLeftClick()) {
                            kit.enabled = true;
                            kit.modifyed = true;
                            reopen(player, contents);
                        }
                    }
            ));
        }
        
        
        
       if (kit.needPermission) {
            contents.set(SlotPos.of(3, 5), ClickableItem.of(new ItemBuilder(Material.YELLOW_WOOL)
                    .name("§eТребуется право")
                    .addLore("§7ЛКМ - выключить")
                    .build(), e -> {
                        if ( e.isLeftClick()) {
                            kit.needPermission = false;
                            kit.modifyed = true;
                            reopen(player, contents);
                        }
                    }
            ));
        } else {
            contents.set(SlotPos.of(3, 5), ClickableItem.of(new ItemBuilder(Material.GREEN_WOOL)
                    .name("§aДоступен всем")
                    .addLore("§7ЛКМ - включить")
                    .build(), e -> {
                        if ( e.isLeftClick()) {
                            kit.needPermission = true;
                            kit.modifyed = true;
                            reopen(player, contents);
                        }
                    }
            ));
        }
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        contents.set(SlotPos.of(4, 1), new InputButton(InputType.ANVILL, new ItemBuilder(Material.GOLD_INGOT)
                .name("§7Цена права доступа")
                .addLore("§7Сейчас: §6" + kit.accesBuyPrice)
                .build(), ""+kit.accesBuyPrice, newValue -> {
                    
                    if( !ApiOstrov.isInteger(newValue) ) {
                        player.sendMessage("§cДолжно быть число!");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                        return;
                    }
                    final int price = Integer.valueOf(newValue);
                    if ( price<0 || price>100000 ) {
                        player.sendMessage("§cОт 0 до 100000!");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                    } else {
                        kit.modifyed = true;
                        kit.accesBuyPrice = price;
                        reopen(player, contents);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2);
                    }
                   // return;
                }));        
        
        
        
        contents.set(SlotPos.of(4, 3), new InputButton(InputType.ANVILL, new ItemBuilder(Material.GOLD_INGOT)
                .name("§7Цена ПРОДАЖИ права доступа")
                .addLore("§7Сейчас: §6" + kit.accesSellPrice)
                .build(), ""+kit.accesSellPrice, newValue -> {
                    
                    if( !ApiOstrov.isInteger(newValue) ) {
                        player.sendMessage("§cДолжно быть число!");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                        return;
                    }
                    final int price = Integer.valueOf(newValue);
                    if ( price<0 || price>100000 ) {
                        player.sendMessage("§cОт 0 до 100000!");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                    } else if ( price>kit.accesBuyPrice ) {
                        player.sendMessage("§cНе может быть больше цены покупки! (фарм)");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                    } else {
                        kit.modifyed = true;
                        kit.accesSellPrice = price;
                        reopen(player, contents);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2);
                    }
                   // return;
                }));        
        
        
        contents.set(SlotPos.of(4, 5), new InputButton(InputType.ANVILL, new ItemBuilder(Material.GOLD_INGOT)
                .name("§7Цена за каждое получение")
                .addLore("§7Сейчас: §6" + kit.getPrice)
                .build(), ""+kit.getPrice, newValue -> {
                    
                    if( !ApiOstrov.isInteger(newValue) ) {
                        player.sendMessage("§cДолжно быть число!");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                        return;
                    }
                    final int price = Integer.valueOf(newValue);
                    if ( price<0 || price>100000 ) {
                        player.sendMessage("§cОт 0 до 100000!");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                    } else {
                        kit.modifyed = true;
                        kit.getPrice = price;
                        reopen(player, contents);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2);
                    }
                   // return;
                }));        
        
        
        
        contents.set(SlotPos.of(4, 7), new InputButton(InputButton.InputType.ANVILL,new ItemBuilder(Material.BLACK_BED)
                .name("§7интервал получения в минутах")
                .addLore("§7Сейчас: §6" + kit.delaySec/60)
                .addLore("§7(§6" +ApiOstrov.secondToTime(kit.delaySec)+"§7)")
                .build(), ""+kit.delaySec/60, newValue -> {
                    
                    if( !ApiOstrov.isInteger(newValue) ) {
                        player.sendMessage("§cДолжно быть число!");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                        return;
                    }
                    final int delay = Integer.valueOf(newValue);
                    if ( delay<0 || delay>100000 ) {
                        player.sendMessage("§cОт 0 до 100000!");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.5f, 1);
                    } else {
                        kit.modifyed = true;
                        kit.delaySec = delay*60;
                        reopen(player, contents);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2);
                    }
                   // return;
                }));        
        
        
        

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        contents.set( 5, 2, ClickableItem.of( new ItemBuilder(Material.CHEST).name("§eредактировать содержимое").build(), e ->
                KitManager.openKitKitComponentEditor(player, kit)
                //SmartInventory.builder().id("KitComponentEditor:"+player.getName()). provider(new KitComponentEditor(kit)). size(6, 9). title("§4Компоненты набора §6"+kit.name). build() .open(player)
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
