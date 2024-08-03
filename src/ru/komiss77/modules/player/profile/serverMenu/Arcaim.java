package ru.komiss77.modules.player.profile.serverMenu;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import ru.komiss77.Cfg;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.profile.ProfileManager;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class Arcaim implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).name("§8.").build());


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
            content.set(section.slot, Section.getMenuItem(section, op));
        }


        content.set(0, 1, ClickableItem.of(new ItemBuilder(Material.GRASS_BLOCK)
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


        content.set(0, 2, ClickableItem.of(new ItemBuilder(Material.BOOKSHELF)
                .name("§eВарпы")
                .lore("")
                .lore("§7Варпы сервера,игроков")
                .lore("§7и администрации.")
                .lore("")
                .build(), e -> {
            pm.current = null;
            p.performCommand("warp");
        }));


        content.set(0, 4, ClickableItem.of(new ItemBuilder(Material.ENDER_EYE)
                .name("§7Спавн")
                .lore("")
                .lore("§7Переход на спавн")
                .lore("")
                .build(), e -> {
            pm.current = null;
            p.closeInventory();
            p.performCommand("spawn");
        }));


        content.set(0, 6, ClickableItem.of(new ItemBuilder(Material.DAYLIGHT_DETECTOR)
                .name("§eТП к игрокам")
                .lore("")
                .lore("Телепорт к игрокам")
                .lore("")
                .build(), e -> {
            p.performCommand("tpa");//pm.openTPA(p);
        }));


        content.set(0, 7, ClickableItem.of(new ItemBuilder(Material.COMPASS)
                .name("§eRandom ТП")
                .lore("")
                .lore("§7ТП куда подальше")
                .lore("§7Телепорт стоит несколько лони,")
                .lore("§7зато будет найдено безопасное")
                .lore("§7место, где нет чужих регионов.")
                .build(), e -> {
            //p.closeInventory();
            pm.current = null;
            p.performCommand("tpr");
        }));


        content.set(1, 3, ClickableItem.of(new ItemBuilder(Material.OAK_FENCE)
                .name("§eРегионы")
                .enchant(Enchantment.KNOCKBACK, 1)
                .lore("§fУправление регионами.")
                .lore("§7Создание, удаление,")
                .lore("§7Установка точек ТП (домов),")
                .lore("§7Настройка флагов.")
                .lore("")
                .build(), e -> {
            //p.closeInventory();
            pm.current = null;
            p.performCommand("land");
        }));


        content.set(1, 5, ClickableItem.of(new ItemBuilder(Material.YELLOW_BED)
                .name("§eВернуться в свой регион")
                .lore("§7Дом любимый дом.")
                .lore("§7Создание, удаление,")
                .lore("§7Откроется меню выбора")
                .lore("§7региона, в который вернуться.")
                .lore("")
                .build(), e -> {
            //p.closeInventory();
            pm.current = null;
            p.performCommand("land home");
        }));


        content.set(2, 1, ClickableItem.of(new ItemBuilder(Material.LAVA_BUCKET)
                .name("§4Выживание")
                .lore("")
                .lore("")
                .lore("")
                .build(), e -> {
            pm.current = null;
            p.closeInventory();
            p.setGameMode(GameMode.SURVIVAL);
        }));


        content.set(2, 3, ClickableItem.of(new ItemBuilder(Material.MILK_BUCKET)
                .name("§lКреатив")
                .lore("")
                .lore("")
                .lore("")
                .build(), e -> {
            pm.current = null;
            p.closeInventory();
            p.setGameMode(GameMode.CREATIVE);
        }));


        content.set(2, 5, ClickableItem.of(new ItemBuilder(Material.WATER_BUCKET)
                .name("§eПриключения")
                .lore("")
                .lore("")
                .lore("")
                .build(), e -> {
            pm.current = null;
            p.closeInventory();
            p.setGameMode(GameMode.ADVENTURE);
        }));


        content.set(2, 7, ClickableItem.of(new ItemBuilder(Material.LAVA_BUCKET)
                .name("§8Зритель")
                .lore("")
                .lore("§f!! Чтобы открыть меню !!")
                .lore("§f!! в режиме зрителя !!")
                .lore("§f!! левый клик мышкой !!")
                .lore("")
                .build(), e -> {
            pm.current = null;
            p.closeInventory();
            p.setGameMode(GameMode.SPECTATOR);
        }));


        content.set(3, 2, ClickableItem.of(new ItemBuilder(Material.DROWNED_SPAWN_EGG)
                .name("§eМаскировка")
                .lore("§7Превратиться в кого-то")
                .lore("§7или что-то")
                .lore("")
                .build(), e -> {
            pm.current = null;
            p.closeInventory();
            p.performCommand("dgui");
        }));


        content.set(3, 4, ClickableItem.of(new ItemBuilder(Material.ENDER_CHEST)
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


        content.set(3, 6, ClickableItem.of(new ItemBuilder(Material.REPEATER)
                .name("§bМеню личных настроек")
                .lore("")
                .lore("")
                .build(), e -> {
            pm.openLocalSettings(p, true);
        }));



        
     /*
        content.set(3,6, ClickableItem.of(new ItemBuilder(Material.ARMOR_STAND)
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
        
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        
*/


    }


}
