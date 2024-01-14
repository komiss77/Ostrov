package ru.komiss77.modules.player.mission;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;


public class MissionMainMenu implements InventoryProvider {

    private static final ClickableItem fill, guestDeny, empty, select, my, withdraw, journal;

    static {
        fill = ClickableItem.empty(new ItemBuilder(Material.SCULK_VEIN).name("§8.").build());
        guestDeny = ClickableItem.empty(new ItemBuilder(Material.BARRIER)
                .name("§7Миссия невыполнима")
                .addLore("")
                .addLore("")
                .addLore( "§6Гости не могут" )
                .addLore("§6выполнять миссии!")
                .addLore("§6Вам нужно зарегаться!")
                .build()
            );
        empty = ClickableItem.empty(new ItemBuilder(Material.GLASS_BOTTLE)
                .name("§7Миссия невыполнима")
                .addLore("")
                .addLore("")
                .addLore( "§6Нет активных миссий" )
                .addLore("")
                .build()
            );
        select = ClickableItem.of(new ItemBuilder(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE)
            .name("§b§lМиссионария")
            .addLore("")
            .addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ITEM_SPECIFICS)
            .addLore("§fОткрыть меню")
            .addLore("§fвыбора §bМиссий")
            .build(), e-> {
                ((Player)e.getWhoClicked()).performCommand("mission select gui");
            }
        );
        my = ClickableItem.of(new ItemBuilder(Material.ECHO_SHARD)
            .name("§a§lЗаметки пиллигрима")
            .addLore("")
            //.addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ITEM_SPECIFICS)
            .addLore("§fВыбор, отказ и ")
            .addLore("§fпрогресс выполнения.")
            .build(), e-> {
                MissionManager.openMissionsMenu(PM.getOplayer(e.getWhoClicked()), false);
            }
        );
        
        withdraw = ClickableItem.of(new ItemBuilder(Material.RAW_GOLD)
            .name("§6§lКассация")
            .addLore("")
            .addLore("§fЛКМ §7- §bЗаказать вывод")
            .addLore("§fденег, заработанных")
            .addLore("§fза выполнение")
            .addLore("§fмиссий.")
            .addLore("")
            .addLore("§fПКМ §7- §eПросмотр статуса")
            .addLore("§fзаявок на вывод.")
            .addLore("")
            .build(), e-> {
                if (e.getClick() == ClickType.LEFT) {
                    SmartInventory
                        .builder()
                        .provider(new MissionWithdrawCreateMenu())
                        .size(5, 9)
                        .title("§6§lВывод средств")
                        .build()
                        .open((Player)e.getWhoClicked());
                } else if (e.getClick() == ClickType.RIGHT) {
                    PM.getOplayer(e.getWhoClicked()).menu.openWithdrawalRequest((Player) e.getWhoClicked(), false);
                }

            }
        );
        
        journal = ClickableItem.of(new ItemBuilder(Material.WRITTEN_BOOK)
            .name("§а§дЖурнал §5§l\"Миссия сегодня\"")
            .addLore("")
            .addLore("§аРеестр Миссий,")
            .addLore("§ав том числе")
            .addLore("§апрошедших и предстоящих.")
            .addLore("")
            .build(), e-> {
                ((Player)e.getWhoClicked()).performCommand("mission journal");
            }
        );
    }
    
    
    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.ENTITY_SHULKER_OPEN, 1, 1);
        content.fillRect(0,0,  4,8, fill);
        
        final Oplayer op = PM.getOplayer(p);
        
        
        if (op.isGuest) {
            content.set(2,4, guestDeny);
            return;
            
        }
        
        if (MissionManager.missions.isEmpty()) {
            content.set(2,4, empty);
            return;
        }

        
        
        content.set(2,2, select);     
        
        content.set(2,4, my);     
        
        content.set(2,6, withdraw);        

        content.set(4,4, journal);        
        
        
        
        
        
        
        
        
        
  

    }


    
    
    
    
    
    
    
    
    
}
