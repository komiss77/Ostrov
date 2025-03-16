package ru.komiss77.modules.player.mission;

import java.util.List;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.profile.Section;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class ProfileManageMenu implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ВОЗМОЖНОСТИ.glassMat).name("§8.").build());
    private final List<ClickableItem> buttonsCurrent;
    private final List<ClickableItem> buttonsDone;


    public ProfileManageMenu(final List<ClickableItem> buttonsCurrent, final List<ClickableItem> buttonsDone) {
        this.buttonsCurrent = buttonsCurrent;
        this.buttonsDone = buttonsDone;
    }

    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }


    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);

        final Oplayer op = PM.getOplayer(p);

        //линия - разделитель
        content.fillRow(1, fill);

        //выставить иконки внизу
        for (Section section : Section.values()) {
            content.set(section.slot, Section.getMenuItem(section, op));
        }


        if (op.isGuest) {

            content.set(0, 4, ClickableItem.empty(new ItemBuilder(ItemType.BARRIER)
                .name("§7Миссия невыполнима")
                .lore("")
                .lore("")
                .lore("§6Гости не могут")
                .lore("§6выполнять миссии!")
                .lore("§6Вам нужно зарегаться!")
                .build()
            ));

        } else if ((buttonsCurrent == null || buttonsCurrent.isEmpty()) && (buttonsDone == null || buttonsDone.isEmpty())) {

            content.set(0, 4, ClickableItem.empty(new ItemBuilder(ItemType.GLASS_BOTTLE)
                .name("§7Миссия невыполнима")
                .lore("")
                .lore("")
                .lore("§6Нет активных миссий")
                .lore("")
                .build()
            ));

        } else {
            for (final ClickableItem icon : buttonsCurrent) {
                content.add(icon);
            }
            if (buttonsDone != null) {
                for (final ClickableItem icon : buttonsDone) {
                    content.add(icon);
                }
            }
        }


    }


}
