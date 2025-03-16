package ru.komiss77.modules.player.mission;

import java.util.List;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.*;


public class MissionWithdrawViewMenu implements InventoryProvider {

    private final List<ClickableItem> buttons;

    public MissionWithdrawViewMenu(final List<ClickableItem> buttons) {
        this.buttons = buttons;
    }

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(ItemType.SCULK_VEIN).name("§8.").build());

    @Override
    public void init(final Player p, final InventoryContent content) {
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 5, 5);
        content.fillRect(0, 0, 2, 8, fill);


        if (buttons.isEmpty()) {
            content.set(13, ClickableItem.empty(new ItemBuilder(ItemType.GLASS_BOTTLE)
                .name("§7нет записей!")
                .build()
            ));
            return;
        }

        final Pagination pagination = content.pagination();
        pagination.setItems(buttons.toArray(new ClickableItem[0]));
        pagination.setItemsPerPage(21);


        if (!pagination.isLast()) {
            content.set(2, 8, ClickableItem.of(ItemUtil.nextPage,
                e -> content.getHost().open(p, pagination.next().getPage())));
        }

        if (!pagination.isFirst()) {
            content.set(2, 0, ClickableItem.of(ItemUtil.previosPage,
                e -> content.getHost().open(p, pagination.previous().getPage())));
        }

        pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1)).allowOverride(false));
    }
}
