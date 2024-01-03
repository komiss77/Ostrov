package ru.komiss77.modules.player.profile.serverMenu;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import ru.komiss77.Config;
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
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }
        
        
        
        
        
        //вкл/выкл глобальный чат
        //регионы
        //в точках дома - показывать приваты, если есть
        //работы, рынок,аукцион?
        
        content.set(0,1, ClickableItem.of(new ItemBuilder(Material.OAK_FENCE)
            .name("§eРегионы")
            .unsafeEnchantment(Enchantment.KNOCKBACK, 1)
            .addLore("§fУправление регионами.")
            .addLore("§7Создание, удаление,")
            .addLore("§7Установка точек ТП (домов),")
            .addLore("§7Настройка флагов.")
            .addLore("")
            .build(), e-> {
                //p.closeInventory();
                pm.current = null;
                p.performCommand("land");
            }));

        
        content.set(0,2, ClickableItem.of(new ItemBuilder(Material.YELLOW_BED)
            .name("§eВернуться в свой регион")
            .addLore("§7Дом любимый дом.")
            .addLore("§7Создание, удаление,")
            .addLore("§7Откроется меню выбора")
            .addLore("§7региона, в который вернуться.")
            .addLore("")
            .build(), e-> {
                //p.closeInventory();
                pm.current = null;
                p.performCommand("land home");
            }));


        content.set(0, 4, ClickableItem.of(new ItemBuilder(Material.GOLDEN_HORSE_ARMOR)
            .name("§eМеста")
            .addLore("")
            .addLore("")
            .addLore("")
            .build(), e-> {
                //p.closeInventory();
                pm.current = null;
                p.performCommand("warp");
            }));
        
        
        content.set(0, 6, ClickableItem.of(new ItemBuilder(Material.DAYLIGHT_DETECTOR)
            .name("§eТП к игрокам")
            .addLore("")
            .addLore("Телепорт к игрокам")
            .addLore("")
            .build(), e-> {
                p.performCommand("tpa");//pm.openTPA(p);
            }));
        
        
        content.set(0, 7, ClickableItem.of(new ItemBuilder(Material.COMPASS)
            .name("§eRandom ТП")
            .addLore("")
            .addLore("§7ТП куда подальше")
            .addLore("§7Телепорт стоит несколько лони,")
            .addLore("§7зато будет найдено безопасное")
            .addLore("§7место, где нет чужих регионов.")
            .build(), e-> {
                //p.closeInventory();
                pm.current = null;
                p.performCommand("tpa");
            }));
        
        
        
        
        
        
        










        
        
        
        
        content.set(1,3, ClickableItem.of(new ItemBuilder(Material.DROWNED_SPAWN_EGG)
            .name("§eМаскировка")
            .addLore("§7Превратиться в кого-то")
            .addLore("§7или что-то")
            .addLore("")
            .build(), e-> {
                pm.current = null;
                p.closeInventory();
                p.performCommand("dgui");
            }));


        
        content.set(1,5, ClickableItem.of(new ItemBuilder(Material.GRASS_BLOCK)
            .name("§7Миры")
            .addLore("")
            .addLore("§7ЛКМ- перемещение в миры")
            .addLore("")
            .addLore("§6Вы находитесь в биоме:")
            .addLore("§e"+p.getWorld().getBiome(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()))
            .addLore("")
            .addLore(Config.world_command ? "§7ЛКМ - открыть" : "§cОтключено на данном сервере")
            .addLore("")
            .build(), e-> {
                pm.current = null;
                p.performCommand("world");
            }));

        
        
        
        
        
        
        
        
        
        
        
        
        content.set(2,1, ClickableItem.of(new ItemBuilder(Material.LAVA_BUCKET)
            .name("§4Выживание")
            .addLore("")
            .addLore("")
            .addLore("")
            .build(), e-> {
                pm.current = null;
                p.closeInventory();
                p.setGameMode(GameMode.SURVIVAL);
            }));


        content.set(2,3, ClickableItem.of(new ItemBuilder(Material.MILK_BUCKET)
            .name("§lКреатив")
            .addLore("")
            .addLore("")
            .addLore("")
            .build(), e-> {
                pm.current = null;
                p.closeInventory();
                p.setGameMode(GameMode.CREATIVE);
            }));


        content.set(2,5, ClickableItem.of(new ItemBuilder(Material.WATER_BUCKET)
            .name("§eПриключения")
            .addLore("")
            .addLore("")
            .addLore("")
            .build(), e-> {
                pm.current = null;
                p.closeInventory();
                p.setGameMode(GameMode.ADVENTURE);
            }));


        content.set(2,7, ClickableItem.of(new ItemBuilder(Material.LAVA_BUCKET)
            .name("§8Зритель")
            .addLore("")
            .addLore("§f!! Чтобы открыть меню !!")
            .addLore("§f!! в режиме зрителя !!")
            .addLore("§f!! наберите команду &b&l/menu &f&l!!")
            .addLore("")
            .build(), e-> {
                pm.current = null;
                p.closeInventory();
                p.setGameMode(GameMode.SPECTATOR);
            }));


        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        content.set(3,2, ClickableItem.of(new ItemBuilder(Material.FIRE_CHARGE)
            .name("§7Спавн")
            .addLore("")
            .addLore("")
            .addLore("")
            .build(), e-> {
                pm.current = null;
                p.closeInventory();
                p.performCommand("spawn");
            }));


        
        content.set(3,4, ClickableItem.of(new ItemBuilder(Material.ENDER_CHEST)
            .name("§7Развлечения")
            .addLore("")
            .addLore("§7Очень весело, обхохочешься.")
            .addLore("§7После нажатия ")
            .addLore("§7откроется меню.")
            .addLore("§7Чтобы выключить его, нажмите")
            .addLore("§7сюда еще раз.")
            .addLore("")
            .build(), e-> {
                //p.closeInventory();
                pm.current = null;
                p.performCommand("pc menu "+p.getName()+" main");
            }));


        
        
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

        
        

        
        
        
        
        
        
        

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
         
        
 
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
  
              
            
        
        





        

        /*
        
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(Material.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        
*/

        

    }


    
    
    
    
    
    
    
    
    
}
