package ru.komiss77.modules.player.profile.serverMenu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.ProfileManager;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;





public class Daaria implements InventoryProvider {
    
    
    
   private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("§8.").build());
   private static final ClickableItem main = ClickableItem.empty(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("Экономика").build());
   private static final ClickableItem regions = ClickableItem.empty(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("Приваты").build());
   private static final ClickableItem homes = ClickableItem.empty(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("Дома").build());
   private static final ClickableItem settings = ClickableItem.empty(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("Настройки").build());
    

    
    public Daaria() {
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
        content.set(0,0, main);
        content.set(0,8, main);
        content.set(1,0, regions);
        content.set(1,8, regions);
        content.set(2,0, homes);
        content.set(2,8, homes);
        content.set(3,0, settings);
        content.set(3,8, settings);
        content.fillRow(4, fill);
        
        //выставить иконки внизу
        for (Section section:Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }
        
        
        
        
 

        content.set(0,1, ClickableItem.of(new ItemBuilder(Material.FIRE_CHARGE)
            .name("§7Спавн")
            .addLore("")
            .addLore("§7Переход на спавн")
            .addLore("")
            .build(), e-> {
                pm.current = null;
                p.closeInventory();
                p.performCommand("spawn");
            }));



        content.set(0, 2, ClickableItem.of(new ItemBuilder(Material.BOOKSHELF)
            .name("§eВарпы")
            .addLore("")
            .addLore("§7Варпы сервера,игроков")
            .addLore("§7и администрации.")
            .addLore("")
            .build(), e-> {
                //p.closeInventory();
                pm.current = null;
                p.performCommand("warp");
            }));
        

        content.set(0, 4, ClickableItem.of(new ItemBuilder(Material.COBWEB)
            .name("§fВыбор сервера")
            .addLore("")
            .addLore("§fМеню выбора сервера")
            .addLore("")
            .build(), e-> {
                pm.open(p, Section.РЕЖИМЫ);
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
                pm.current = null;
                p.performCommand("tpa");
            }));
        










        
        
        
        
        
        
        
        
        
        
        
        
        






        
        content.set(1,1, ClickableItem.of(new ItemBuilder(Material.OAK_FENCE)
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

        
        content.set(1,3, ClickableItem.of(new ItemBuilder(Material.YELLOW_BED)
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


        
        content.set(1, 5, ClickableItem.of(new ItemBuilder(Material.RED_BED)
            .name("§eВернуться домой")
            .addLore("")
            .addLore("§7Дом любимый дом")
            .addLore("")
            .build(), e-> {
                p.closeInventory();
                pm.current = null;
                p.performCommand("home");
            }));
        
        
        content.set(1, 7, ClickableItem.of(new ItemBuilder(Material.WHITE_BED)
            .name("§eУправление точками дома")
            .addLore("")
            .addLore("§eУправление точками дома")
            .addLore("")
            .build(), e-> {
                pm.openHomes(p);
            }));
        
        
        
        
        
        
        










        
        
        
        
        content.set(2,2, ClickableItem.of(new ItemBuilder(Material.DIAMOND_PICKAXE)
            .name("§6Работы")
            .addLore("")
            .addLore("§7ЛКМ - меню работ")
            .addLore("§7Чтобы уволиться с работы,")
            .addLore("§7наберите команду")
            .addLore("§b/jobs leave §7название_работы")
            .addLore("§7")
            .build(), e-> {
                if (e.isLeftClick()) {
                    pm.current = null;
                    p.performCommand("jobs join");
                } else {
                    
                }
                
            }));



        content.set(2,4, ClickableItem.of(new ItemBuilder(Material.DIAMOND)
            .name("§eРынок")
            .addLore("")
            //.addLore("§cПлагин для рынка")
            //.addLore("§cпеределывают.")
            .addLore("")
            .build(), e-> {
                //p.closeInventory();
                //PM.soundDeny(p);
                p.performCommand("market");
                //pm.current = null;
                //p.performCommand("ah");
            }));

        content.set(2,6, ClickableItem.of(new ItemBuilder(Material.DIAMOND_AXE)
            .name("§7/ah sell цена")
            .addLore("")
            .addLore("§7Чтобы продать товар")
            .addLore("§7возьмите его в руку")
            .build(), e-> {
                //pm.current = null;
                //p.closeInventory();
                //p.performCommand("ah sell");
                PM.soundDeny(p);
            }));


        
        
        

        
        
        
        
        
        
        
        
        
        
        
        
        content.set(3, 1, ClickableItem.of(new ItemBuilder(Material.HONEYCOMB)
            .name("§aНаборы")
            .addLore("")
            .addLore("§7Меню наборов.")
            .addLore("")
            .build(), e-> {
                //p.closeInventory();
                pm.current = null;
                p.performCommand("kit");
            }));
        
        
        
        content.set(3, 3, ClickableItem.of(new ItemBuilder(Material.STONECUTTER)
            .name("§bМеню личных настроек")
            .addLore("")
            .addLore("")
            .build(), e-> {
                pm.openLocalSettings(p, true);
            }));
        
       // if (Ostrov.deluxeChat) {
            final boolean local = op.isLocalChat();//op.hasFlag(StatFlag.LocalChat); //final boolean local = Ostrov.deluxechatPlugin.isLocal(p.getUniqueId().toString());
            content.set(3,5, ClickableItem.of(new ItemBuilder( local ? Material.SCUTE : Material.GUNPOWDER)
                .name("§7Режим чата")
                .addLore(local ? "§7Сейчас: §bлокальный" : "§7Сейчас: §eглобальный")
                .addLore( local ? "§7ЛКМ - сделать глобальным" : "§7ЛКМ - сделать локальным" )
                .addLore( "§7" )
                .addLore("В режиме &bглобальный")
                .addLore("вы получаете сообщения со всех серверов,")
                .addLore("и на всех серверах видят ваши сообщения.")
                .addLore("В режиме &bлокальный")
                .addLore("вы получаете сообщения только")
                .addLore("от игроков с этого сервера,")
                .addLore("Ваши сообщения так же будут")
                .addLore("видны только на этом сервере.")
                .addLore( "§7" )
                .addLore("§eКомандой /msg ник сообщение")
                .addLore("§eможно начать личный диалог.")
                .build(), e-> {
                    op.setLocalChat(!local);
                    //if (local) {
                        //DchatHook.setGlobal(p);
                    //    op.setFlag(StatFlag.LocalChat, false);
                  //  } else {
                        //DchatHook.setLocal(p);
                  //      op.setFlag(StatFlag.LocalChat, true);
                  //  }
                    reopen(p, content);
                }
            ));
       // }
        
        

        content.set(3, 7, ClickableItem.of(new ItemBuilder(Material.FIRE_CORAL)
            .name("§cВыход")
            .addLore("")
            .addLore("")
            .build(), e-> {
                pm.current = null;
                p.closeInventory();
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
