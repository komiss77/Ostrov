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





public class Default implements InventoryProvider {
    
    
    
   private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ВОЗМОЖНОСТИ.glassMat).name("§8.").build());
    

    
    public Default() {
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
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }
        
        
        
        
        
        //вкл/выкл глобальный чат
        //регионы
        //в точках дома - показывать приваты, если есть
        //работы, рынок,аукцион?
        
        content.set(1,1, ClickableItem.of(new ItemBuilder(Material.FIRE_CHARGE)
            .name("§7Спавн")
            .addLore("")
            .addLore("")
            .addLore( Config.spawn_command ? "§7ЛКМ - переместиться" : "§cКоманда отключена" )
            .addLore("")
            .build(), e-> {
                p.closeInventory();
                p.performCommand("spawn");
            }));


        
        
        
        content.set(1,2, ClickableItem.of(new ItemBuilder(Material.GRASS_BLOCK)
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

        
        
        content.set(1,3, ClickableItem.of(new ItemBuilder(Material.ENDER_PEARL)
            .name("§7Места")
            .addLore("")
            .addLore("")
            .addLore( "§7ЛКМ - открыть" )
            .addLore("")
            .build(), e-> {
                pm.current = null;
                p.performCommand("warp");
            }));

        
        
        
        
        if ( Config.home_command) {
            content.set(1,4, ClickableItem.of(new ItemBuilder(Material.LIME_BED)
                .name("§7Точки дома")
                .addLore("")
                .addLore("")
                .addLore( Config.home_command ? "§7ЛКМ - открыть" : "§cКоманда отключена" )
                .addLore("")
                .build(), e-> {
                    pm.openHomes(p);//p.performCommand("home");
                }));
        } else {
            content.set(1,4, ClickableItem.empty(new ItemBuilder(Material.RED_BED)
                .name("§7Точки дома")
                .addLore("")
                .addLore("")
                .addLore("§cКоманда отключена" )
                .addLore("")
                .build()
            ));
        }
        /*
        if ( Config.home_command && itemname.equals("§bМои дома")) {
                            Res(p,"ok");
                            Set <String> homes = PM.OP_GetHomeList(p.getName());
                           if (homes.isEmpty()) {
                               e.getInventory().setItem( 18, ItemUtils.no_homes);
                               return;
                           }
                            int pos = 18;
                            
                            Location h_loc;
                            for (String h: homes ) {
                                h_loc = PM.OP_GetHomeLocation(p, h);
                                ItemStack bed = new ItemBuilder(Material.RED_BED).name(h).build();
                                //ItemUtils.Set_name(bed, h);
                                if (h_loc != null) ItemUtils.Set_lore(bed, "§6Координаты: §7"+h_loc.getWorld().getName()+",", "§7  "+h_loc.getBlockX()+" x "+h_loc.getBlockY()+" x "+h_loc.getBlockZ(), "§aЛевый клик - §2ТП В ЭТОТ ДОМ", "§6Правый клик - §4УДАЛИТЬ" );
                                else ItemUtils.Set_lore(bed, "§6Координаты:", "§cНеисправность,", "§cНужно установить заново.", "§6Правый клик - §4УДАЛИТЬ");
                                bed.addUnsafeEnchantment(Enchantment.LUCK, pos);
                                e.getInventory().setItem(pos, bed);
                                e.getInventory().getItem(pos).setAmount(pos-17);
                                pos+=1;
                                    if ( pos > 35) {
                                        e.getInventory().setItem( 35, ItemUtils.too_many );
                                        return;
                                    }
                                }
                        }
        */
        
        
        

        final boolean moderTPO = p.hasPermission("ostrov.tpo");
        content.set(1,5, ClickableItem.of(new ItemBuilder(Material.PLAYER_HEAD)
            .name(moderTPO ? "§7Перемещение к игрокам" : "§7Запрос на телепорт")
            .addLore("")
            .addLore("")
            .addLore( moderTPO ? "*право модератора"  : Config.tpa_command>=0 ? "§7ЛКМ - отправить" :  "§cКоманда отключена"  )
            .addLore("")
            .build(), e-> {
                p.performCommand("tpa");
            }));


        
        content.set(1,6, ClickableItem.of(new ItemBuilder(Material.COMPASS)
            .name("§7Случайный телепорт")
            .addLore("")
            .addLore("")
            .addLore( Config.tpr_command>=0 ? "§7ЛКМ - совершить" : "§cКоманда отключена" )
            .addLore("")
            .build(), e-> {
                p.closeInventory();
                p.performCommand("tpr");
            }));


        
        // "§7Информация и управление приватами" ,"§6Левый клик - §bВ этом мире + управление", "§6Правый клик - §6Во всех мирах (только просмотр)"
        if (Ostrov.wg) {
            content.set(1,7, ClickableItem.of(new ItemBuilder(Material.OAK_FENCE_GATE)
                .name("§7Приваты")
                .addLore("")
                .addLore(  Bukkit.getPluginManager().getPlugin("RegionGUI") == null ? "" : "§7ЛКМ - помошник привата" )
                .addLore("§7ПКМ - найти все приваты,")
                .addLore("§7где вы владелец или житель")
                .build(), e-> {
                    if (e.isLeftClick()) {
                        pm.current = null;
                        p.performCommand("land");
                    } else if (e.isRightClick()) {
                        pm.findRegions(p);
                    }

                }));
        } else {
            content.set(1,7, ClickableItem.empty(new ItemBuilder(Material.DARK_OAK_FENCE_GATE)
                .name("§7Приваты")
                .addLore("")
                .addLore("")
                .addLore("§cНе используется" )
                .addLore("§cна данном сервере.")
                .build()
            ));
        }
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        content.set(2,1, ClickableItem.of(new ItemBuilder(Material.GOLDEN_SWORD)
            .name("§6Наборы")
            .addLore("")
            .addLore(Config.getConfig().getBoolean("modules.command.kit") ? "§7ЛКМ - получение" : "§cОтключено на данном сервере" )
            .addLore("")
            .build(), e-> {
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
