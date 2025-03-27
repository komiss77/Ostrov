package ru.komiss77.modules.items.menu;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.ChiseledBookshelf;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.*;

public class EnchMenu implements InventoryProvider {

    private static final Enchantment[] ENCHS = OStrap
        .getAll(RegistryKey.ENCHANTMENT).toArray(new Enchantment[0]);

    private final ItemStack it;

    public EnchMenu(final ItemStack it) {
        this.it = it;
    }

    private int bookBuff(final BlockData bd) {
        if (BlockType.BOOKSHELF.equals(bd.getMaterial().asBlockType())) return 4;
        if (bd instanceof final ChiseledBookshelf cb) {
            return cb.getOccupiedSlots().size();
        }
        return 0;
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
        p.playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.8f);

        for (int i = 0; i < 54; i++) {
            switch (i / 9) {case 0, 5: break; default: continue;}
            its.set(i, ClickableItem.empty((i & 1) == 0 ?
                new ItemBuilder(ItemType.MAGENTA_STAINED_GLASS_PANE).name("§0.").build()
                : new ItemBuilder(ItemType.YELLOW_STAINED_GLASS_PANE).name("§0.").build()));
        }

        its.set(4, ClickableItem.from(new ItemBuilder(it).lore(" ")
            .lore("§фКлик §7 - подтвердить").build(), e -> ItemMenu.open(p, it)));

        final Pagination pagination = its.pagination();
        final ClickableItem[] ciar = new ClickableItem[ENCHS.length];
        final boolean ste = ItemUtil.is(it, ItemType.ENCHANTED_BOOK);
        final ItemEnchantments ies = it.getData(ste ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS);
        final Map<Enchantment, Integer> em = ies == null ? null : new HashMap<>(ies.enchantments());
        for (int i = 0; i != ENCHS.length; i++) {
            final Enchantment en = ENCHS[i];
            final Integer lvl = em == null ? null : em.get(en);
            ciar[i] = ClickableItem.from(new ItemBuilder(display(en)).name(TCUtil.form("<aqua>").append(Lang.t(en, p)))
                .lore("<dark_gray>" + en.key().asString(), "<green>ЛКМ: +1 ур.", lvl == null ? "" : "<red>ПКМ: -1 ур.")
                .glint(lvl != null).amount(lvl == null ? 1 : lvl).build(), e -> {
                switch (e.getClick()) {
                    case LEFT, SHIFT_LEFT:
                        if (em == null) {
                            it.setData(ste ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS,
                                ItemEnchantments.itemEnchantments(Map.of(en, 1)));
                            reopen(p, its);
                            break;
                        }
                        em.put(en, lvl == null ? 1 : lvl + 1);
                        it.setData(ste ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS,
                            ItemEnchantments.itemEnchantments(em));
                        reopen(p, its);
                        break;
                    case RIGHT, SHIFT_RIGHT:
                        if (lvl == null) return;
                        if (lvl < 2) em.remove(en);
                        else em.put(en, lvl - 1);
                        if (em.isEmpty()) {
                            it.resetData(ste ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS);
                            reopen(p, its);
                            break;
                        }
                        it.setData(ste ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS,
                            ItemEnchantments.itemEnchantments(em));
                        reopen(p, its);
                        break;
                }
            });
        }

        pagination.setItems(ciar);
        pagination.setItemsPerPage(36);

        if (!pagination.isLast()) {
            its.set(5, 8, ClickableItem.of(ItemUtil.nextPage,
                e -> its.getHost().open(p, pagination.next().getPage())));
        }

        if (!pagination.isFirst()) {
            its.set(5, 0, ClickableItem.of(ItemUtil.previosPage,
                e -> its.getHost().open(p, pagination.previous().getPage())));
        }

        pagination.addToIterator(its.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 0)).allowOverride(false));
    }

    private static final ItemType[] DITS = {ItemType.DIAMOND_PICKAXE, ItemType.NETHERITE_SWORD, ItemType.IRON_CHESTPLATE,
        ItemType.BOW, ItemType.CROSSBOW, ItemType.TURTLE_HELMET, ItemType.CHAINMAIL_LEGGINGS, ItemType.LEATHER_BOOTS,
        ItemType.MACE, ItemType.TRIDENT, ItemType.FISHING_ROD, ItemType.ELYTRA, ItemType.SHIELD, ItemType.BRUSH, ItemType.SHEARS};
    private ItemType display(final Enchantment en) {
        final Set<ItemType> allowed = new HashSet<>(en.getSupportedItems().resolve(Registry.ITEM));
        final RegistryKeySet<ItemType> prm = en.getPrimaryItems();
        if (prm != null) allowed.addAll(prm.resolve(Registry.ITEM));
        for (final ItemType it : DITS) if (allowed.contains(it)) return it;
        return ItemType.ENCHANTED_BOOK;
    }
}
