package ru.komiss77.modules.player.profile;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.enums.Settings;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.inventory.*;


public class FriendFind implements InventoryProvider {


    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ДРУЗЬЯ.glassMat).name("§8.").build());


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

        final Pagination pagination = content.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

        boolean found = false;
        for (final Player find : Bukkit.getOnlinePlayers()) {

            if (find.getName().equals(p.getName())) continue;
            if (LocUtil.getDistance(p.getLocation(), find.getLocation()) > 30) continue;

            final Oplayer findOp = PM.getOplayer(find);
            if (findOp == null) continue;
            found = true;

            if (op.friends.contains(find.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.EMERALD)
                    .name(find.getName())
                    .lore("")
                    .lore("§2Уже друзья")
                    .lore("")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else if (findOp.hasSettings(Settings.InviteDeny)) {

                final ItemStack friend_item = new ItemBuilder(ItemType.SKELETON_SKULL)
                    .name(find.getName())
                    .lore("")
                    .lore("§cПредложения дружить")
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

            } else if (findOp.friendInvite.contains(p.getName())) {

                final ItemStack friend_item = new ItemBuilder(ItemType.CREEPER_HEAD)
                    .name(find.getName())
                    .lore("")
                    .lore("§6Предложение дружить.")
                    .lore("§6отправлено.")
                    .lore("")
                    .build();

                menuEntry.add(ClickableItem.empty(friend_item));

            } else {

                final ItemStack friend_item = new ItemBuilder(ItemType.PLAYER_HEAD)
                    .name(find.getName())
                    .lore("")
                    .lore("§aПредложить дружбу")
                    .lore("")
                    .build();

                menuEntry.add(ClickableItem.of(friend_item
                        , e -> {
                            if (find.isOnline()) {
                                Friends.suggestFriend(p, op, find);
                                //mode = FriendMode.Просмотр;
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

        
              /* if (op.friends.isEmpty()) {

                    content.set( SlotPos.of(1, 4), ClickableItem.of(new ItemBuilder(ItemType.GLASS_BOTTLE)
                                .setName("§7Ой!")
                                .addLore("У Вас пока нет друзей!")
                                .addLore("")
                                .addLore("Чтобы добавить друга/подругу,")
                                .addLore("§7встаньте рядом и")
                                .addLore("§7клик на бутылочку.")
                                .addLore("")
                                .build(), e-> {

                                }
                        ) 
                    );

                } else {*/

        



        

        
        /*
        content.set( 5, 8, ClickableItem.of( new ItemBuilder(ItemType.OAK_DOOR).name("Закрыть").build(), e -> 
        {
            p.closeInventory();
        }
        ));
        */


    }


}
