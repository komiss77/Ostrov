package ru.komiss77.modules.items.menu;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;

public class FlagMenu implements InventoryProvider {

    private final ItemStack it;

    public FlagMenu(final ItemStack it) {
        this.it = it;
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
        p.playSound(p.getLocation(), Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1f, 0.8f);
        final ItemMeta im = it.getItemMeta();

        its.set(4, ClickableItem.from(new ItemBuilder(it).lore(" ").lore("§фКлик §7 - подтвердить").build(), e -> ItemMenu.open(p, it)));

        for (final ItemFlag hf : ItemFlag.values()) {
            final ItemStack fli = im.hasItemFlag(hf) ? new ItemBuilder(ItemType.LIGHT_BLUE_DYE).name("§7Флаг: §b" + hf.name() + " §7[§чКлик§7]").build()
                : new ItemBuilder(ItemType.GRAY_DYE).name("§7Флаг: §ч" + hf.name() + " §7[§bКлик§7]").build();
            its.set(hf.ordinal() > 3 ? hf.ordinal() + 1 : hf.ordinal(), ClickableItem.from(fli, e -> {
                if (e.getEvent() instanceof InventoryClickEvent) {
                    if (im.hasItemFlag(hf)) im.removeItemFlags(hf);
                    else im.addItemFlags(hf);
                    it.setItemMeta(im);
                    reopen(p, its);
                }
            }));
        }
    }
}
