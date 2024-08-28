package ru.komiss77.modules.player.profile.serverMenu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.ProfileManager;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class Daaria implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.SCULK_VEIN).name("§8.").build());
    private static final ClickableItem main = ClickableItem.empty(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("Экономика").build());
    private static final ClickableItem regions = ClickableItem.empty(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("Приваты").build());
    private static final ClickableItem homes = ClickableItem.empty(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("Дома").build());
    private static final ClickableItem settings = ClickableItem.empty(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("Настройки").build());


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
    /*    content.set(0,0, main);
        content.set(0,8, main);
        content.set(1,0, regions);
        content.set(1,8, regions);
        content.set(2,0, homes);
        content.set(2,8, homes);
        content.set(3,0, settings);
        content.set(3,8, settings);*/
        content.fillRect(0, 0, 4, 8, fill);

        //выставить иконки внизу
        for (Section section : Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }


        content.set(1, 2, ClickableItem.of(new ItemBuilder(Material.BOOKSHELF)
            .name("§eВарпы")
            .lore("")
            .lore("§7Варпы сервера,игроков")
            .lore("§7и администрации.")
            .lore("")
            .build(), e -> {
            //p.closeInventory();
            pm.current = null;
            p.performCommand("warp");
        }));

        content.set(1, 4, ClickableItem.of(new ItemBuilder(Material.ENDER_EYE)
            .name("§7Спавн")
            .lore("")
            .lore("§7Переход на спавн")
            .lore("")
            .build(), e -> {
            pm.current = null;
            p.closeInventory();
            p.performCommand("spawn");
        }));


        content.set(1, 6, ClickableItem.of(new ItemBuilder(Material.REPEATER)
            .name("§bМеню личных настроек")
            .lore("")
            .lore("")
            .build(), e -> {
            pm.openLocalSettings(p, true);
        }));


     /*   content.set(0, 4, ClickableItem.of(new ItemBuilder(Material.COBWEB)
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
        
*/

        if (Ostrov.wg) {
            content.set(2, 3, ClickableItem.of(new ItemBuilder(Material.OAK_FENCE)
                .name("§eРегионы")
                .lore("§fУправление регионами.")
                .enchant(Enchantment.KNOCKBACK, 1)
                .flags(ItemFlag.HIDE_ENCHANTS)
                .build(), e -> {
                pm.openRegions(p);
            }));
        } else {
            content.set(2, 1, ClickableItem.empty(new ItemBuilder(Material.OAK_FENCE)
                .name("§cРегионы")
                .lore("§cНет WG!")
                .enchant(Enchantment.KNOCKBACK, 1)
                .flags(ItemFlag.HIDE_ENCHANTS)
                .build()));
        }


       /*  content.set(2, 3, ClickableItem.of(new ItemBuilder(Material.YELLOW_BED)
            .name("§eВернуться в свой регион")
                //.lore("§7Дом любимый дом.")
                //.lore("§7Создание, удаление,")
            .lore("§7Откроется меню выбора")
            .lore("§7региона, в который вернуться.")
            .lore("")
            .build(), e -> {
            //p.closeInventory();
            pm.current = null;
            p.performCommand("land home");
        }));


       content.set(2, 5, ClickableItem.of(new ItemBuilder(Material.RED_BED)
            .name("§eВернуться домой")
            .lore("")
            .lore("§7Дом любимый дом")
            .lore("")
            .build(), e -> {
            p.closeInventory();
            pm.current = null;
            p.performCommand("home");
        }));*/


        content.set(2, 5, ClickableItem.of(new ItemBuilder(Material.WHITE_BED)
            .name("§eДома")
                //.lore("")
                //.lore("§eУправление точками дома")
                //.lore("")
            .build(), e -> {
            pm.openHomes(p);
        }));


        content.set(3, 2, ClickableItem.of(new ItemBuilder(Material.ENDER_CHEST)
            .name("§aНаборы")
            .lore("")
            .lore("§7Меню наборов.")
            .lore("")
            .build(), e -> {
            //p.closeInventory();
            pm.current = null;
            p.performCommand("kit");
        }));


        content.set(3, 4, ClickableItem.of(new ItemBuilder(Material.DIAMOND_PICKAXE)
            .name("§6Работы")
            .lore("")
            .lore("§7ЛКМ - меню работ")
            .lore("§7Чтобы уволиться с работы,")
            .lore("§7наберите команду")
            .lore("§b/jobs leave §7название_работы")
            .lore("§7")
            .build(), e -> {
            if (e.isLeftClick()) {
                pm.current = null;
                p.performCommand("jobs join");
            } else {

            }

        }));


        content.set(3, 6, ClickableItem.of(new ItemBuilder(Material.DIAMOND)
            .name("§eРынок")
            .lore("")
            //.addLore("§cПлагин для рынка")
            //.addLore("§cпеределывают.")
            .lore("")
            .build(), e -> {
            //p.closeInventory();
            //PM.soundDeny(p);
            p.performCommand("market");
            //pm.current = null;
            //p.performCommand("ah");
        }));

     /*   content.set(2,6, ClickableItem.of(new ItemBuilder(Material.DIAMOND_AXE)
            .name("§7/ah sell цена")
            .addLore("")
            .addLore("§7Чтобы продать товар")
            .addLore("§7возьмите его в руку")
            .build(), e-> {
                //pm.current = null;
                //p.closeInventory();
                //p.performCommand("ah sell");
                PM.soundDeny(p);
            }));*/


        // if (Ostrov.deluxeChat) {
    /*        final boolean local = op.isLocalChat();//op.hasFlag(StatFlag.LocalChat); //final boolean local = Ostrov.deluxechatPlugin.isLocal(p.getUniqueId().toString());
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
        */
        

   /*     content.set(3, 7, ClickableItem.of(new ItemBuilder(Material.FIRE_CORAL)
            .name("§cВыход")
            .addLore("")
            .addLore("")
            .build(), e-> {
                pm.current = null;
                p.closeInventory();
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
