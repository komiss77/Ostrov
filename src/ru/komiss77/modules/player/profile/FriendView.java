package ru.komiss77.modules.player.profile;

import java.util.ArrayList;
import java.util.HashMap;
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
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.inventory.*;
import ru.komiss77.utils.inventory.InputButton.InputType;

public class FriendView implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ДРУЗЬЯ.glassMat).name("§8.").build());
    private final String rawData;

    FriendView(final String rawData) {
        this.rawData = rawData;
    }

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

        final HashMap<String, String> server = new HashMap<>(); //друг, сервер:настройки
        final HashMap<String, Integer> settings = new HashMap<>(); //друг, сервер:настройки

        String name;
        int index;
        for (String info : rawData.split(",")) {
            index = info.indexOf(":");
            if (index < 1) continue;
            name = info.substring(0, index);
            info = info.substring(index + 1);
            index = info.indexOf(":");
            if (index < 1) continue;
            server.put(name, info.substring(0, index));
            settings.put(name, NumUtil.intOf(info
                .substring(0, index), Integer.MIN_VALUE));
        }


        final Pagination pagination = content.pagination();
        final ArrayList<ClickableItem> menuEntry = new ArrayList<>();

        for (final String inviteName : op.friendInvite) {

            if (op.isBlackListed(inviteName)) continue;

            final ItemStack friend_item = new ItemBuilder(ItemType.PLAYER_HEAD)
                .name(inviteName)
                .headTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDE5NGEyMjM0NWQ5Y2RkZTc1MTY4Mjk5YWQ2MTg3M2JjMTA1ZTNhZTczY2Q2YzlhYzAyYTI4NTI5MWFkMGYxYiJ9fX0=")
                .lore("§6Предлагает дружить")
                .lore("§7ЛКМ - §aпринять")
                .lore("§7ПКМ - §cотклонить")
                .build();

            menuEntry.add(ClickableItem.of(friend_item, e -> {
                    if (e.isLeftClick()) {
                        Friends.add(p, op, inviteName);
                    } else if (e.isRightClick()) {
                        p.sendMessage("§6Ник " + inviteName + " занесён в игнор");
                        ApiOstrov.executeBungeeCmd(p, "ignore add " + inviteName);
                        op.friendInvite.remove(inviteName);
                        //p.performCommand("ignore add "+inviteName);//op.blackList.add(inviteName);
                        reopen(p, content);
                    }
                }
            ));
        }

        for (final String friendName : op.friends) {
            if (server.containsKey(friendName)) {
                final int friendSettings = settings.get(friendName);
                final ItemStack friend_item = new ItemBuilder(ItemType.PLAYER_HEAD)
                    .name(friendName + (op.isBlackListed(friendName) ? "§7, §cв игноре" : ""))
                    .lore("§7Сервер: §a" + server.get(friendName))
                    .lore(Settings.hasSettings(friendSettings, Settings.MsgDeny) ? "§8сообщения отключены" : "§7ЛКМ - §6написать сообщение")
                    .lore(Settings.hasSettings(friendSettings, Settings.TeleportDeny) ? "§8запрос на ТП отключён" : "§7ПКМ - §bзапрос на телепорт")
                    .lore(op.isBlackListed(friendName) ? "§7Шфт+ЛКМ - §6разблокировать" : "")
                    .lore("")
                    .lore("§7Клав Q - §cудалить")
                    .build();

                menuEntry.add(ClickableItem.of(friend_item, e -> {
                    switch (e.getClick()) {
                        case LEFT:
                            p.closeInventory();
                            PlayerInput.get(InputType.CHAT, p, msg -> {
                                ApiOstrov.executeBungeeCmd(p, "friend mail " + friendName + " " + msg);
                                //p.performCommand("friend mail "+friendName+" "+msg);
                            }, "");
                            return;
                        case SHIFT_LEFT:
                            ApiOstrov.executeBungeeCmd(p, "ignore del " + friendName);
                            op.removeBlackList(friendName);
                            return;
                        case RIGHT:
                            p.closeInventory();
                            ApiOstrov.executeBungeeCmd(p, "friend jump " + friendName);
                            //p.performCommand("friend jump "+friendName);
                            return;
                        case DROP:
                            Friends.delete(p, op, friendName);
                            reopen(p, content);
                            return;
                        default:
                            break;
                    }
                    PM.soundDeny(p);
                }));
            } else {
                final ItemStack friend_item = new ItemBuilder(ItemType.PLAYER_HEAD)
                    .name(friendName + (op.isBlackListed(friendName) ? "§7, §cв игноре" : ""))
                    .lore("§8оффлайн")
                    .lore("§7ЛКМ - §6написать письмо")
                    .lore(op.isBlackListed(friendName) ? "§7Шфт+ЛКМ - §6разблокировать" : "")
                    .lore("")
                    .lore("§7Клав Q - §cудалить")
                    .build();

                menuEntry.add(ClickableItem.of(friend_item, e -> {
                    switch (e.getClick()) {
                        case DROP:
                            Friends.delete(p, op, friendName);
                            reopen(p, content);
                            break;

                        case LEFT:
                            p.closeInventory();
                            PlayerInput.get(InputType.CHAT, p, msg -> {
                                ApiOstrov.executeBungeeCmd(p, "friend mail " + friendName + " " + msg);
                                //p.performCommand("friend mail "+friendName+" "+msg);
                            }, "");
                            break;

                        case SHIFT_LEFT:
                            ApiOstrov.executeBungeeCmd(p, "ignore del " + friendName);
                            op.removeBlackList(friendName);
                            return;

                        default:
                            PM.soundDeny(p);
                            break;
                    }
                }));
            }
        }


        menuEntry.add(ClickableItem.of(new ItemBuilder(ItemType.ENDER_EYE)
            .name("§aдобавить")
            .lore("§7")
            .lore("§7Чтобы отправить предложение")
            .lore("§7дружить, встаньте рядом")
            .lore("§7и нажмите на эту иконку.")
            .lore("§7")
            .build(), e -> Friends.openFriendsFind(op)));


        //  }


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
