package ru.komiss77.modules.player.profile;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class PartyView implements InventoryProvider {


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
          content.set(2, section.column, Section.getMenuItem(section, op));
        }

        if (op.party_members.isEmpty()) {

            content.add(ClickableItem.of(new ItemBuilder(ItemType.ENDER_EYE)
                .name("§eСоздать Команду")
                .lore("§7")
                .build(), e -> {
                ApiOstrov.executeBungeeCmd(p, "party create");
                op.party_leader = op.nik;
                op.party_members.put(op.nik, Ostrov.MOT_D);
                reopen(p, content);
            }));
          return;
        }


      if (op.nik.equals(op.party_leader)) {  //лидер

            for (final String name : op.party_members.keySet()) {

                final boolean isLeader = op.party_leader.equals(name);
                final ItemStack head = new ItemBuilder(ItemType.PLAYER_HEAD)
                    .name(name)
                    .lore(isLeader ? "§6Лидер" : "§9Участник")
                    .lore("§7Сервер: §a" + op.party_members.get(name))
                    .lore("")
                    .lore("§7" + TCUtil.bind(TCUtil.Input.DROP) + (isLeader
                        ? " - §cПокинуть команду" :  " - §cВыгнать"))
                    .lore(isLeader ? "" : "§7ПКМ - §6Передать лидерство")
                    .lore("")
                    .build();

                Skins.future(name, pp -> {
                    head.setData(DataComponentTypes.PROFILE,
                        ResolvableProfile.resolvableProfile(pp));
                });

                content.add(ClickableItem.of(head, e -> {
                        if (e.getClick() == ClickType.DROP) {
                            if (isLeader) {
                                ApiOstrov.executeBungeeCmd(p, "party leave");
                                op.party_members.clear();
                                op.party_leader = "";
                            } else {
                                ApiOstrov.executeBungeeCmd(p, "party kick " + name);
                                op.party_members.remove(name);
                            }
                            reopen(p, content);
                        } else if (e.getClick() == ClickType.RIGHT) {
                            ApiOstrov.executeBungeeCmd(p, "party leader " + name);
                            op.party_leader = name;
                            reopen(p, content);
                        }
                    }
                ));

            }

            if (op.party_members.size() < 8) {
              content.add(ClickableItem.of(new ItemBuilder(ItemType.PLAYER_HEAD)
                  .name("§aПригласить")
                  .headTexture(ItemUtil.Texture.add)
                  .lore("")
                  .lore("§6Нужно быть рядом с игроком!")
                    .build(), e -> Friends.openPartyFind(op)));
            }

        } else { //участник

            for (final String name : op.party_members.keySet()) {

                final boolean isLeader = op.party_leader.equals(name);
                final boolean same = op.nik.equals(name);
                final ItemStack head = new ItemBuilder(ItemType.PLAYER_HEAD)
                    .name(name)
                    .lore(isLeader ? "§6Лидер" : "§9Участник")
                    .lore("§7Сервер: §a" + op.party_members.get(name))
                    .lore("")
                    .lore(same ? "§7" + TCUtil.bind(TCUtil.Input.DROP)
                        + " - §cПокинуть команду" : "")
                    .build();

                Skins.future(name, pp -> {
                    head.setData(DataComponentTypes.PROFILE,
                        ResolvableProfile.resolvableProfile(pp));
                });

                if (!same) content.add(ClickableItem.empty(head));
                else content.add(ClickableItem.of(head, e -> {
                    if (e.getClick() == ClickType.DROP) {
                        ApiOstrov.executeBungeeCmd(p, "party leave");
                        op.party_members.clear();
                        op.party_leader = "";
                        reopen(p, content);
                    }
                }));
            }

        }

      content.add(ClickableItem.of(new ItemBuilder(ItemType.OAK_HANGING_SIGN)
              .name("§6Сообщение команде")
              .build(), e -> {
            p.closeInventory();
            PlayerInput.get(InputButton.InputType.CHAT, p, msg -> ApiOstrov.executeBungeeCmd(p, "party msg " + msg), "");
          }
      ));



    }
}
