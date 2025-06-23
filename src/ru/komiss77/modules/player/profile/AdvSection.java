package ru.komiss77.modules.player.profile;

import java.util.ArrayList;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.*;

public class AdvSection implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ДОСТИЖЕНИЯ.glassMat).name("§8.").build());


    @Override
    public void onClose(final Player p, final InventoryContent content) {
        PM.getOplayer(p).menu.current = null;
    }


    @Override
    public void init(final Player p, final InventoryContent content) {
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //content.fillRect(0,0,  5,8, ClickableItem.empty(fill));

        final Oplayer op = PM.getOplayer(p);
        //final ProfileManager pm = op.menu;


        //линия - разделитель
      content.fillRow(4, fill);

        //выставить иконки внизу
        for (Section section : Section.values()) {
          content.set(5, section.column, Section.getMenuItem(section, op));
        }


        final Pagination pagination = content.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

        for (final Stat st : Stat.values()) {
            if (st.achiv == null) continue;

            final int level = StatManager.getLevel(st, op.getStat(st));
            final ItemType mat = switch (level) {
                case 5 -> ItemType.DIAMOND_HELMET;
                case 4 -> ItemType.GOLDEN_HELMET;
                case 3 -> ItemType.IRON_HELMET;
                case 2 -> ItemType.CHAINMAIL_HELMET;
                case 1 -> ItemType.TURTLE_HELMET;
                default -> ItemType.LEATHER_HELMET;
            };

            final ItemStack adv_item = new ItemBuilder(mat)
                .name(Lang.t(p, st.game.displayName) + " : " + Lang.t(p, st.desc))
                .hide(DataComponentTypes.ATTRIBUTE_MODIFIERS)
                .lore("")
                .lore(level >= 5 ? "§6✪ §e" + StatManager.topAdv(st) + " §6✪" : "")
                .lore(Lang.t(p, "§fНакоплено : §6") + op.getStat(st))
                .lore(level == 0 ? Lang.t(p, "§5Пока нечем гордиться") : level >= 5 ? Lang.t(p, "§8Предел достижения") : Lang.t(p, "§fУровень достижения : §b") + level)
                .lore(level >= 5 ? "" : Lang.t(p, "До след. уровня: §f") + StatManager.getLeftToNextLevel(st, op.getStat(st)))
                .lore("")
                .lore(Lang.t(p, "§7Опыта за каждый уровень: §e") + st.exp_per_point)
                .build();


            menuEntry.add(ClickableItem.empty(adv_item));
        }

        pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
      pagination.setItemsPerPage(36);

        if (!pagination.isLast()) {
          content.set(4, 8, ClickableItem.of(ItemUtil.nextPage, e
                    -> {
                    content.getHost().open(p, pagination.next().getPage());
                }
            ));
        }

        if (!pagination.isFirst()) {
          content.set(4, 0, ClickableItem.of(ItemUtil.previosPage, e
                    -> {
                    content.getHost().open(p, pagination.previous().getPage());
                })
            );
        }

      pagination.addToIterator(content.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0).allowOverride(false));
    }
}
