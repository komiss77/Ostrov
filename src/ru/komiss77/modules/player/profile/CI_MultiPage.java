package ru.komiss77.modules.player.profile;

import java.util.List;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.*;


public class CI_MultiPage implements InventoryProvider {


    private final List<ClickableItem> buttons;
    private final ItemType glassMat;


    public CI_MultiPage(final List<ClickableItem> buttons, final ItemType glassMat) {
        this.buttons = buttons;
        this.glassMat = glassMat;
    }


    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

        final Oplayer op = PM.getOplayer(p);
        //final ProfileManager pm = op.menu;

        //линия - разделитель
        content.fillRow(1, ClickableItem.empty(new ItemBuilder(glassMat).name("§8.").build()));

        //выставить иконки внизу
        for (Section section : Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }

        if (buttons.isEmpty()) {
            content.set(4, ClickableItem.empty(new ItemBuilder(ItemType.GLASS_BOTTLE)
                .name("§7нет записей!")
                .build()
            ));
            return;
        }


        final Pagination pagination = content.pagination();

        pagination.setItems(buttons.toArray(new ClickableItem[0]));
        pagination.setItemsPerPage(9);


        if (!pagination.isLast()) {
            content.set(1, 8, ClickableItem.of(ItemUtil.nextPage, e
                    -> {
                    content.getHost().open(p, pagination.next().getPage());
                }
            ));
        }

        if (!pagination.isFirst()) {
            content.set(1, 0, ClickableItem.of(ItemUtil.previosPage, e
                    -> {
                    content.getHost().open(p, pagination.previous().getPage());
                })
            );
        }

        pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0)).allowOverride(false));


    }

    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }
}
