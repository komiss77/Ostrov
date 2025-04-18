package ru.komiss77.modules.player.profile;

import java.util.ArrayList;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Settings;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.inventory.*;


public class PartyFind implements InventoryProvider {


    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ДРУЗЬЯ.glassMat).name("§8.").build());


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
        content.fillRow(1, fill);

        //выставить иконки внизу
        for (Section section : Section.values()) {
          content.set(2, section.column, Section.getMenuItem(section, op));
        }


        final Pagination pagination = content.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

        boolean found = false;
        final int id = p.getEntityId();
        for (final Player find : LocUtil.getChEnts(BVec.of(p),
            20, Player.class, pl -> pl.getEntityId() != id)) {
            final Oplayer findOp = PM.getOplayer(find);
            if (findOp == null) continue;
            found = true;

            if (op.party_members.containsKey(find.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.EMERALD)
                    .name(find.getName())
                    .lore("")
                    .lore("§2Уже с тобой в команде!")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (!findOp.party_leader.isEmpty()) {

                final ItemStack friend_item = new ItemBuilder(ItemType.SKELETON_SKULL)
                    .name(find.getName())
                    .lore("")
                    .lore("§2Уже в команде " + findOp.party_leader + "!")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (findOp.hasSettings(Settings.InviteOthersDeny) && !ApiOstrov.isFriend(op.nik, find.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.SKELETON_SKULL)
                    .name(find.getName())
                    .lore("")
                    .lore("§cПриглашения в команду от")
                    .lore("§cпосторонних отключены!")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (findOp.hasSettings(Settings.InviteFriendsDeny) && ApiOstrov.isFriend(op.nik, find.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.SKELETON_SKULL)
                    .name(find.getName())
                    .lore("")
                    .lore("§cПриглашения в команду от")
                    .lore("§cдрузей отключены!")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (op.isBlackListed(p.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.SKELETON_SKULL)
                    .name(find.getName())
                    .lore("")
                    .lore("§cУ тебя в игноре!")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (findOp.isBlackListed(p.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.WITHER_SKELETON_SKULL)
                    .name(find.getName())
                    .lore("")
                    .lore("§4Ты в игноре!")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (findOp.friendInvite.contains(p.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.CREEPER_HEAD)
                    .name(find.getName())
                    .lore("")
                    .lore("§6Приглашение отправлено!")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else {

                final ItemStack friend_item = new ItemBuilder(ItemType.PLAYER_HEAD)
                    .name(find.getName())
                    .skullOf(find)
                    .lore("")
                    .lore("§aПригласить в команду")
                    .build();

                menuEntry.add(ClickableItem.of(friend_item
                        , e -> {
                            if (find.isOnline()) {
                                Friends.suggestParty(p, find);
                                reopen(p, content);
                            }
                        }
                    )
                );

            }


        }

        if (!found) {

            final ItemStack notFound = new ItemBuilder(ItemType.GLASS_BOTTLE)
                .name("§7Нет никого рядом..")
                .lore("§8Радиус: 20 блоков")
                .lore("")
                .lore("§7ЛКМ - обновить")
                .build();

            content.set(4, ClickableItem.of(notFound, e -> {
                reopen(p, content);
            }));

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
