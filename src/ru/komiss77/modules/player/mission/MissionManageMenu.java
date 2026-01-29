package ru.komiss77.modules.player.mission;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.Pagination;
import ru.komiss77.utils.inventory.SlotIterator;
import ru.komiss77.utils.inventory.SlotPos;


public class MissionManageMenu implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Material.GLOW_LICHEN).name("§8.").build());
    private final List<ClickableItem> buttonsCurrent;
    private final List<ClickableItem> buttonsDone;


    public MissionManageMenu(final List<ClickableItem> buttonsCurrent, final List<ClickableItem> buttonsDone) {
        this.buttonsCurrent = buttonsCurrent;
        this.buttonsDone = buttonsDone;
    }


    @Override
    public void init(final Player p, final InventoryContent content) {
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 5, 5);
        content.fillRect(0, 0, 4, 8, fill);
      final Oplayer op = PM.getOplayer(p);

      if (op.isGuest) {

        content.set(0, 4, ClickableItem.empty(new ru.komiss77.modules.items.ItemBuilder(ItemType.BARRIER)
            .name("§7Миссия невыполнима")
            .lore("")
            .lore("")
            .lore("§6Гости не могут")
            .lore("§6выполнять миссии!")
            .lore("§6Вам нужно зарегаться!")
            .build()
        ));

      } else if ((buttonsCurrent == null || buttonsCurrent.isEmpty()) && (buttonsDone == null || buttonsDone.isEmpty())) {

            content.set(1, 4, ClickableItem.empty(new ItemBuilder(Material.GLASS_BOTTLE)
                .name("§7Миссия невыполнима")
                .lore("")
                .lore("")
                .lore("§6Нет активных миссий")
                .lore("")
                .build()
            ));

        } else {

            final List<ClickableItem> buttons = new ArrayList<>(buttonsCurrent);
            if (buttonsDone != null) {
                buttons.addAll(buttonsDone);
            }

            final Pagination pagination = content.pagination();

            pagination.setItems(buttons.toArray(ClickableItem[]::new));
            pagination.setItemsPerPage(21);

            if (!pagination.isLast()) {
                content.set(4, 8, ClickableItem.of(ItemUtil.nextPage, e -> {
                    content.getHost().open(p, pagination.next().getPage());
                }));
            }

            if (!pagination.isFirst()) {
                content.set(4, 0, ClickableItem.of(ItemUtil.previosPage, e -> {
                    content.getHost().open(p, pagination.previous().getPage());
                }));
            }
        content.set(4, 4, ClickableItem.of(new ru.komiss77.modules.items.ItemBuilder(ItemType.OAK_DOOR)
            //.headTexture(ItemUtil.Texture.previosPage)
            .name("§7назад")
            .build(), e -> {
          MissionMainMenu.open(p);
        }));

            pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));

        }
    }
}
