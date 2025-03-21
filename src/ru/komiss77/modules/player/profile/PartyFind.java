package ru.komiss77.modules.player.profile;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Settings;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
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
            content.set(section.slot, Section.getMenuItem(section, op));
        }


        final Pagination pagination = content.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

        Oplayer findOp;
        boolean found = false;

        for (final Player find : Bukkit.getOnlinePlayers()) {

            if (find.getName().equals(p.getName())) continue;
            if (LocUtil.getDistance(p.getLocation(), find.getLocation()) > 30) continue;

            findOp = PM.getOplayer(find);
            if (findOp == null) continue;
            found = true;

            if (op.party_members.containsKey(find.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.EMERALD)
                    .name(find.getName())
                    .lore("")
                    .lore("§2Уже в вашей команде")
                    .lore("")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (!findOp.party_leader.isEmpty()) {

                final ItemStack friend_item = new ItemBuilder(ItemType.SKELETON_SKULL)
                    .name(find.getName())
                    .lore("")
                    .lore("§2Уже в команде " + findOp.party_leader)
                    .lore("")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (findOp.hasSettings(Settings.InviteOthersDeny) && !ApiOstrov.isFriend(op.nik, find.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.SKELETON_SKULL)
                    .name(find.getName())
                    .lore("")
                    .lore("§cПриглашения в команду")
                    .lore("§cот посторонних")
                    .lore("§cотключены в настройках.")
                    .lore("")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (findOp.hasSettings(Settings.InviteFriendsDeny) && ApiOstrov.isFriend(op.nik, find.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.SKELETON_SKULL)
                    .name(find.getName())
                    .lore("")
                    .lore("§cПриглашения в команду")
                    .lore("§cот друзей")
                    .lore("§cотключены в настройках.")
                    .lore("")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (op.isBlackListed(p.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.WITHER_SKELETON_SKULL)
                    .name(find.getName())
                    .lore("")
                    .lore("§cВ игноре")
                    .lore("")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (findOp.isBlackListed(p.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.WITHER_SKELETON_SKULL)
                    .name(find.getName())
                    .lore("")
                    .lore("§cВы занесены в игнор")
                    .lore("")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (findOp.partyInvite.contains(p.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.CREEPER_HEAD)
                    .name(find.getName())
                    .lore("")
                    .lore("§6Приглашение уже")
                    .lore("§6отправлено.")
                    .lore("")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else {

                final ItemStack friend_item = new ItemBuilder(ItemType.PLAYER_HEAD)
                    .name(find.getName())
                    .lore("")
                    .lore("§aПригласить в команду")
                    .lore("")
                    .build();

                menuEntry.add(ClickableItem.of(friend_item
                        , e -> {
                            if (find.isOnline()) {
                                Friends.suggestParty(p, op, find);
                                reopen(p, content);
                            }
                        }
                    )
                );

            }


        }

        if (!found) {

            final ItemStack notFound = new ItemBuilder(ItemType.GLASS_BOTTLE)
                .name("§7Никого не смогли найти..")
                .lore("")
                .lore("§7Поиск ведется в радиусе")
                .lore("§75 блоков.")
                .lore("")
                .lore("§7ЛКМ - обновить")
                .lore("")
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
