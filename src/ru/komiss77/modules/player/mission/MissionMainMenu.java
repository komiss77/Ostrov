package ru.komiss77.modules.player.mission;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;


public class MissionMainMenu implements InventoryProvider {

    private static final ClickableItem fill, guestDeny, empty, select, my, withdraw, journal;

  public static void open(Player p) {
    SmartInventory
        .builder()
        .provider(new MissionMainMenu())
        .size(5, 9)
        .title("§a§lМиссии")
        .build()
        .open(p);
  }


    static {
        fill = ClickableItem.empty(new ItemBuilder(ItemType.SCULK_VEIN).name("§8.").build());
        guestDeny = ClickableItem.empty(new ItemBuilder(ItemType.BARRIER)
            .name("§7Миссия невыполнима")
            .lore("")
            .lore("")
            .lore("§6Гости не могут")
            .lore("§6выполнять миссии!")
            .lore("§6Вам нужно зарегаться!")
            .build()
        );
        empty = ClickableItem.empty(new ItemBuilder(ItemType.GLASS_BOTTLE)
            .name("§7Миссия невыполнима")
            .lore("")
            .lore("")
            .lore("§6Нет активных миссий")
            .lore("")
            .build()
        );
      select = ClickableItem.of(new ItemBuilder(ItemType.SCRAPE_POTTERY_SHERD)
                .name("§b§lМиссионария")
                .lore("")
          //.hide(DataComponentTypes.TRIM, DataComponentTypes.ATTRIBUTE_MODIFIERS,
          //    DataComponentTypes.PROVIDES_TRIM_MATERIAL)
                .lore("§fОткрыть меню")
                .lore("§fвыбора §bМиссий")
                .build(), e -> {
                ((Player) e.getWhoClicked()).performCommand("mission select gui");
            }
        );
        my = ClickableItem.of(new ItemBuilder(ItemType.ECHO_SHARD)
                .name("§a§lЗаметки пиллигрима")
                .lore("")
                //.addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ITEM_SPECIFICS)
                .lore("§fВыбор, отказ и ")
                .lore("§fпрогресс выполнения.")
                .build(), e -> {
                MissionManager.openMissionsMenu(PM.getOplayer(e.getWhoClicked()), false);
            }
        );

        withdraw = ClickableItem.of(new ItemBuilder(ItemType.RAW_GOLD)
                .name("§6§lКассация")
                .lore("")
                .lore("§fЛКМ §7- §bЗаказать вывод")
                .lore("§fденег, заработанных")
                .lore("§fза выполнение")
                .lore("§fмиссий.")
                .lore("")
                .lore("§fПКМ §7- §eПросмотр статуса")
                .lore("§fзаявок на вывод.")
                .lore("")
                .build(), e -> {
                if (e.getClick() == ClickType.LEFT) {
                    SmartInventory
                        .builder()
                        .provider(new MissionWithdrawCreateMenu())
                        .size(5, 9)
                        .title("§6§lВывод средств")
                        .build()
                        .open((Player) e.getWhoClicked());
                } else if (e.getClick() == ClickType.RIGHT) {
                    PM.getOplayer(e.getWhoClicked()).menu.openWithdrawalRequest((Player) e.getWhoClicked(), false);
                }

            }
        );

        journal = ClickableItem.of(new ItemBuilder(ItemType.WRITTEN_BOOK)
                .name("§а§дЖурнал §5§l\"Миссия сегодня\"")
                .lore("")
                .lore("§аРеестр Миссий,")
                .lore("§ав том числе")
                .lore("§апрошедших и предстоящих.")
                .lore("")
                .build(), e -> {
                ((Player) e.getWhoClicked()).performCommand("mission journal");
            }
        );
    }


  @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.ENTITY_SHULKER_OPEN, 1, 1);
        content.fillRect(0, 0, 4, 8, fill);

        final Oplayer op = PM.getOplayer(p);


        if (op.isGuest) {
            content.set(2, 4, guestDeny);
            return;

        }

        if (MissionManager.missions.isEmpty()) {
            content.set(2, 4, empty);
            return;
        }


        content.set(2, 2, select);

        content.set(2, 4, my);

        content.set(2, 6, withdraw);

        content.set(4, 4, journal);


    }


}
