package ru.komiss77.modules.warp;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.commands.WarpCmd;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;


public class WarpSetupMenu implements InventoryProvider {


    private final Warp w;
    //private static final ItemStack fill = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("§8.").build();;


    public WarpSetupMenu(final Warp w) {
        this.w = w;
    }


    @Override
    public void init(final Player p, final InventoryContent contents) {
        //p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 5, 5);
        //contents.fillRect(0,0, 2,3, ClickableItem.empty(fill1));
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, .5f, 1);

        if (w == null) {
            return;
        }


        contents.set(0, ClickableItem.of(new ItemBuilder(w.dispalyMat)
            .name("§fЛоготип места")
            .lore("§7")
            .lore("§7Положите сюда предмет,")
            .lore("§7и он станет иконкой.")
            .lore("§7")
            .build(), e -> {
            if (e.isLeftClick() && e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                w.dispalyMat = e.getCursor().getType();
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                e.getView().getBottomInventory().addItem(new ItemStack[]{e.getCursor()});
                e.getView().setCursor(new ItemStack(Material.AIR));
                WarpManager.saveWarp(p, w);
                reopen(p, contents);
            }


        }));


        contents.set(1, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.ACACIA_SIGN)
            .name("§7Описание")
            .lore("§7Сейчас:")
            .lore("§7" + w.descr)
            .lore("§7")
            .lore("§7ЛКМ - редактировать")
            .lore("§7")
            .build(), w.descr, msg -> {
            final String strip = TCUtil.strip(msg);

            if (strip.length() > 24) {
                p.sendMessage("§cЛимит 24 символа!");
                PM.soundDeny(p);
                return;
            }

            w.descr = strip;
            WarpManager.saveWarp(p, w);

            reopen(p, contents);
        }));


        contents.set(2, new InputButton(InputButton.InputType.ANVILL, new ItemBuilder(Material.GOLD_NUGGET)
            .name("§6Плата за посещение")
            .lore(w.use_cost == 0 ? "§7Сейчас: §aбесплатно" : "§7Сейчас: §6" + w.use_cost + " лони")
            .lore("§7Владелец будет")
            .lore("§7получать указанную сумму,")
            .lore("§7когда кто-то переместится.")
            .lore("")
            .lore("§7ЛКМ - изменить")
            .lore("")
            .build(), "" + w.use_cost, msg -> {
            if (!NumUtil.isInt(msg)) {
                p.sendMessage("§cДолжно быть число!");
                PM.soundDeny(p);
                return;
            }
            final int amount = Integer.valueOf(msg);
            if (amount < 0 || amount > 10000) {
                p.sendMessage("§cот 0 до 10000");
                PM.soundDeny(p);
                return;
            }

            WarpManager.setCost(w, amount);
            reopen(p, contents);
        }));


        contents.set(3, ClickableItem.of(new ItemBuilder(Material.OAK_FENCE_GATE)
            .name("§6Требовать право")
            .lore("")
            .lore(w.need_perm ? "§7Сейчас: §4требуется" : "§7Сейчас: §2не требуется")
            .lore("")
            .lore("§7Для посещения места")
            .lore("§7будет требоваться право")
            .lore("§7warp.use." + w.warpName)
            .lore("")
            .lore("§7ЛКМ - изменить")
            .lore("")
            .build(), e -> {

            if (e.isLeftClick()) {
                w.need_perm = !w.need_perm;
                WarpManager.saveWarp(p, w);
                reopen(p, contents);
            }


        }));


        contents.set(4, ClickableItem.of(new ItemBuilder(Material.OAK_DOOR).name("назад").build(), e ->
            WarpCmd.openMenu(p)
        ));


    }


}
