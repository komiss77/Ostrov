package ru.komiss77.modules.player.profile;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.ApiOstrov;
import ru.komiss77.boot.OStrap;
import ru.komiss77.enums.Data;
import ru.komiss77.enums.ServerType;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.player.PM;
import ru.komiss77.modules.player.Perm;
import ru.komiss77.objects.Group;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class Donate implements InventoryProvider {

    private static final ClickableItem fill = ClickableItem.empty(new ItemBuilder(Section.ГРУППЫ.glassMat).name("§8.").build());


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


        for (final Group group : Perm.getGroups()) {
            if (group == null || group.isStaff()) continue;
            content.set(group.inv_slot, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(OStrap.get(Key.key(group.mat.toLowerCase()), ItemType.EMERALD))
                .name(group.chat_name)
                .hide(DataComponentTypes.ATTRIBUTE_MODIFIERS)
                .lore(group.lore)
                .build(), "15-180 дней", ammount -> {
                p.closeInventory();
              if (GM.GAME.type != ServerType.LOBBY) {
                p.sendMessage("§cДля покупки группы перейдите в Лобби!");
                return;
              }
                if (!NumUtil.isInt(ammount)) {
                    p.sendMessage("§cДолжно быть число!");
                    return;
                }
                final int days = Integer.parseInt(ammount);
                if (days < 15 || days > 180) {
                    p.sendMessage("§cот 15 до 180 дней!");
                    return;
                }
                final int price = group.getPrice(days);
                if (op.getDataInt(Data.RIL) < price) {
                    p.sendMessage("§cНедостаточно рил! (группа " + group.chat_name + " на " + days + "д. стоит " + price + " рил)");
                    return;
                }
                ApiOstrov.executeBungeeCmd(p, "group buy " + p.getName() + " " + group.chat_name + " " + days);
            }));


        }


        final ItemStack add = new ItemBuilder(ItemType.GOLD_INGOT)
            .name("§6Пополнить счёт")
            .lore("§7")
            .lore("§7Для оплаты привилегии")
            .lore("§7нужны §bРил")
            .lore("§7Рил можно заработать,")
            .lore("§7выполняя миссии, или")
            .lore("§7пополнить через магазин.")
            .lore("§7")
            .lore("§fКлик §6- Открыть офф. магазин")
            .build();

      content.set(7, ClickableItem.of(add
                , e -> {
                    p.performCommand("donate");
                    //p.closeInventory();
                    //ApiOstrov.executeBungeeCmd(p, "money add");
                }
            )
        );


    }


}
