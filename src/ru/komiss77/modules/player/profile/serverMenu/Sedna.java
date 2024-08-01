package ru.komiss77.modules.player.profile.serverMenu;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.komiss77.Config;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.profile.ProfileManager;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class Sedna implements InventoryProvider {


    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ВОЗМОЖНОСТИ.glassMat).name("§8.").build());


    public Sedna() {
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
        final ProfileManager pm = op.menu;

        //линия - разделитель
        content.fillRow(4, fill);

        //выставить иконки внизу
        for (Section section : Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }


        //вкл/выкл глобальный чат
        //регионы
        //в точках дома - показывать приваты, если есть
        //работы, рынок,аукцион?

        content.set(1, 1, ClickableItem.of(new ItemBuilder(Material.FIRE_CHARGE)
            .name("§7Спавн")
            .lore("")
            .lore("")
            .lore(Config.spawn_command ? "§7ЛКМ - переместиться" : "§cКоманда отключена")
            .lore("")
            .build(), e -> {
            p.closeInventory();
            p.performCommand("spawn");
        }));


        content.set(1, 2, ClickableItem.of(new ItemBuilder(Material.GRASS_BLOCK)
            .name("§7Миры")
            .lore("")
            .lore("§7ЛКМ- перемещение в миры")
            .lore("")
            .lore("§6Вы находитесь в биоме:")
            .lore("§e" + p.getWorld().getBiome(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()))
            .lore("")
            .lore(Config.world_command ? "§7ЛКМ - открыть" : "§cОтключено на данном сервере")
            .lore("")
            .build(), e -> {
            pm.current = null;
            p.performCommand("world");
        }));


        content.set(1, 3, ClickableItem.of(new ItemBuilder(Material.ENDER_PEARL)
            .name("§7Места")
            .lore("")
            .lore("")
            .lore("§7ЛКМ - открыть")
            .lore("")
            .build(), e -> {
            pm.current = null;
            p.performCommand("warp");
        }));


        if (Config.home_command) {
            content.set(1, 4, ClickableItem.of(new ItemBuilder(Material.LIME_BED)
                .name("§7Точки дома")
                .lore("")
                .lore("")
                .lore(Config.home_command ? "§7ЛКМ - открыть" : "§cКоманда отключена")
                .lore("")
                .build(), e -> {
                pm.openHomes(p);//p.performCommand("home");
            }));
        } else {
            content.set(1, 4, ClickableItem.empty(new ItemBuilder(Material.RED_BED)
                .name("§7Точки дома")
                .lore("")
                .lore("")
                .lore("§cКоманда отключена")
                .lore("")
                .build()
            ));
        }


        final boolean moderTPO = p.hasPermission("ostrov.tpo");
        content.set(1, 5, ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
            .name(moderTPO ? "§7Перемещение к игрокам" : "§7Запрос на телепорт")
            .lore("")
            .lore("")
            .lore(moderTPO ? "*право модератора" : Config.tpa_command_delay >= 0 ? "§7ЛКМ - отправить" : "§cКоманда отключена")
            .lore("")
            .build(), e -> {
            p.performCommand("tpa");
        }));


        content.set(1, 6, ClickableItem.of(new ItemBuilder(Material.COMPASS)
            .name("§7Случайный телепорт")
            .lore("")
            .lore("")
            .lore(Config.tpr_command_delay >= 0 ? "§7ЛКМ - совершить" : "§cКоманда отключена")
            .lore("")
            .build(), e -> {
            p.closeInventory();
            p.performCommand("tpr");
        }));


        // "§7Информация и управление приватами" ,"§6Левый клик - §bВ этом мире + управление", "§6Правый клик - §6Во всех мирах (только просмотр)"
        if (Ostrov.wg) {
            content.set(1, 7, ClickableItem.empty(new ItemBuilder(Material.DARK_OAK_FENCE_GATE)
                .name("§7Приваты")
                .lore("")
                .lore("")
                .lore("§cНе используется")
                .lore("§cна данном сервере.")
                .build()
            ));
        } else {
            content.set(1, 7, ClickableItem.of(new ItemBuilder(Material.OAK_FENCE_GATE)
                .name("§7Приваты")
                .lore("")
                .lore(Bukkit.getPluginManager().getPlugin("RegionGUI") == null ? "" : "§7ЛКМ - помошник привата")
                .lore("§7ПКМ - найти все приваты,")
                .lore("§7где вы владелец или житель")
                .build(), e -> {
                if (e.isLeftClick()) {
                    pm.current = null;
                    p.performCommand("land");
                } else if (e.isRightClick()) {
                    pm.findRegions(p);
                }

            }));
        }


        content.set(2, 1, ClickableItem.of(new ItemBuilder(Material.GOLDEN_SWORD)
            .name("§6Наборы")
            .lore("")
            .lore(Config.getConfig().getBoolean("modules.command.kit") ? "§7ЛКМ - получение" : "§cОтключено на данном сервере")
            .lore("")
            .build(), e -> {
            if (e.isLeftClick()) {
                pm.current = null;
                p.performCommand("kit");
            }
        }));

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
         
        
 
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
  
              
            
        
        





        

        /*
        
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        
*/


    }


}
