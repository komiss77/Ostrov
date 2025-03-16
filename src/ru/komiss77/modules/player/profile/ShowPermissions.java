package ru.komiss77.modules.player.profile;

import java.util.ArrayList;
import java.util.TreeMap;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.bukkit.permissions.PermissionAttachmentInfo;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.inventory.*;


public class ShowPermissions implements InventoryProvider {


    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ПРОФИЛЬ.glassMat).name("§8.").build());


    public ShowPermissions() {

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

        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

        for (String group : op.getGroups()) {
            menuEntry.add(ClickableItem.empty(new ItemBuilder(ItemType.EMERALD)
                .name("§7Группа §e" + group)
                .build()));
        }


        for (String limitName : op.limits.keySet()) {
            menuEntry.add(ClickableItem.empty(new ItemBuilder(ItemType.PRISMARINE_CRYSTALS)
                .name("§7Лимит для §e" + limitName + " §7: §a" + op.limits.get(limitName))
                .build()));
        }


        TreeMap<String, Boolean> perm = new TreeMap<>();

        for (PermissionAttachmentInfo attacement_info : p.getEffectivePermissions()) {
            perm.put(attacement_info.getPermission(), attacement_info.getValue());
        }


        if (perm.isEmpty()) {

            menuEntry.add(ClickableItem.empty(new ItemBuilder(ItemType.GLASS_BOTTLE)
                .name("§7нет записей с пермишенами!")
                .build()
            ));

            return;

        }


        final Pagination pagination = content.pagination();


        for (String s : perm.keySet()) {
            menuEntry.add(ClickableItem.empty(new ItemBuilder(perm.get(s) ? ItemType.LIME_DYE : ItemType.RED_DYE)
                .name("§7" + s)
                .build()));
        }


        pagination.setItems(menuEntry.toArray(new ClickableItem[0]));
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


}
