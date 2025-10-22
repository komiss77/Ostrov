package ru.komiss77.modules.player.profile.serverMenu;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.events.LocalMenuEvent;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.ProfileManager;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class Arcaim implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(ItemType.GREEN_STAINED_GLASS_PANE).name("§8.").build());


    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }


    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //content.fillRect(0,0,  5,8, ClickableItem.empty(fill));

        final Oplayer op = PM.getOplayer(p);
        final ProfileManager pm = op.menu;

        //линия - разделитель
        content.fillColumn(0, fill);
        content.fillColumn(8, fill);
        content.fillRow(4, fill);

        //выставить иконки внизу
        for (Section section : Section.values()) {
            content.set(5, section.column, Section.getMenuItem(section, op));
        }

        final LocalMenuEvent event = (new LocalMenuEvent(p, content));
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

/*
        content.set(0, 1, ClickableItem.of(new ItemBuilder(ItemType.GRASS_BLOCK)
            .name("§7Миры")
            .lore("")
            .lore("§7ЛКМ- перемещение в миры")
            .lore("")
            .lore("§6Вы находитесь в биоме:")
            .lore("§e" + p.getWorld().getBiome(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()))
            .lore("")
            .lore(Cfg.world_command ? "§7ЛКМ - открыть" : "§cОтключено на данном сервере")
            .lore("")
            .build(), e -> {
            pm.current = null;
            p.performCommand("world");
        }));

        content.set(0, 2, ClickableItem.of(new ItemBuilder(ItemType.ENDER_EYE)
            .name("§7Спавн")
            .lore("")
            .lore("§7Переход на спавн")
            .lore("")
            .build(), e -> {
            pm.current = null;
            p.closeInventory();
            p.performCommand("spawn");
        }));

        content.set(0, 3, ClickableItem.of(new ItemBuilder(ItemType.DAYLIGHT_DETECTOR)
            .name("§eТП к игрокам")
            .lore("")
            .lore("Телепорт к игрокам")
            .lore("")
            .build(), e -> {
            p.performCommand("tpa");//pm.openTPA(p);
        }));

        switch (p.getGameMode()) {
            case CREATIVE:
                content.set(0, 4, ClickableItem.of(new ItemBuilder(ItemType.LAVA_BUCKET)
                    .name("§4Выживание")
                    .lore("")
                    .lore("§f!! Чтобы открыть меню в !!")
                    .lore("§f!! режиме зрителя, нажми ЛКМ !!")
                    .lore("")
                    .build(), e -> {
                    pm.current = null;
                    p.closeInventory();
                    p.setGameMode(GameMode.SURVIVAL);
                }));
                break;
            case SURVIVAL:
                break;
            case ADVENTURE:
                break;
            case SPECTATOR:
                break;
        }


        content.set(2, 3, ClickableItem.of(new ItemBuilder(ItemType.MILK_BUCKET)
            .name("§lКреатив")
            .lore("")
            .lore("")
            .lore("")
            .build(), e -> {
            pm.current = null;
            p.closeInventory();
            p.setGameMode(GameMode.CREATIVE);
        }));


        content.set(2, 5, ClickableItem.of(new ItemBuilder(ItemType.WATER_BUCKET)
            .name("§eПриключения")
            .lore("")
            .lore("")
            .lore("")
            .build(), e -> {
            pm.current = null;
            p.closeInventory();
            p.setGameMode(GameMode.ADVENTURE);
        }));


        content.set(2, 7, ClickableItem.of(new ItemBuilder(ItemType.BUCKET)
            .name("§8Зритель")
            .lore("")
            .lore("§f!! Чтобы открыть меню в !!")
            .lore("§f!! режиме зрителя, нажми ЛКМ !!")
            .lore("")
            .build(), e -> {
            pm.current = null;
            p.closeInventory();
            p.setGameMode(GameMode.SPECTATOR);
        }));


        content.set(3, 2, ClickableItem.of(new ItemBuilder(ItemType.DROWNED_SPAWN_EGG)
            .name("§eМаскировка")
            .lore("§7Превратиться в кого-то")
            .lore("§7или что-то")
            .lore("")
            .build(), e -> {
            pm.current = null;
            p.closeInventory();
            p.performCommand("dgui");
        }));


        content.set(3, 4, ClickableItem.of(new ItemBuilder(ItemType.ENDER_CHEST)
            .name("§7Развлечения")
            .lore("")
            .lore("§7Очень весело, обхохочешься.")
            .lore("§7После нажатия ")
            .lore("§7откроется меню.")
            .lore("§7Чтобы выключить его, нажмите")
            .lore("§7сюда еще раз.")
            .lore("")
            .build(), e -> {
            //p.closeInventory();
            pm.current = null;
            p.performCommand("pc menu " + p.getName() + " main");
        }));


        content.set(3, 6, ClickableItem.of(new ItemBuilder(ItemType.REPEATER)
            .name("§bМеню личных настроек")
            .lore("")
            .lore("")
            .build(), e -> {
            pm.openLocalSettings(p, true);
        }));

*/

        
     /*
        content.set(3,6, ClickableItem.of(new ItemBuilder(ItemType.ARMOR_STAND)
            .name("§3Пугало")
            .addLore("")
            .addLore("§7Управление стойками для брони.")
            .addLore("")
            .build(), e-> {
                pm.current = null;
                //p.closeInventory();
                p.performCommand("astools");
            }));

        */
        

        
        
        
        
        
        
        

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
         
        
 
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
  
              
            
        
        





        

        /*
        
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(ItemType.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        
*/


    }


}
