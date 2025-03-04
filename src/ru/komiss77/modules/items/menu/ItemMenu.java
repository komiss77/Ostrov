package ru.komiss77.modules.items.menu;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import io.papermc.paper.datacomponent.DataComponentType;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.*;
import ru.komiss77.utils.inventory.InputButton.InputType;


public class ItemMenu implements InventoryProvider {

    private static final ItemStack[] invIts;
    private final ItemStack it;

    static {
        invIts = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            switch (i) {
                /*case 22:
                    invIts[i] = new ItemBuilder(ItemType.HONEYCOMB).name("§6Выдать Предмет").build();
                    break;
                case 16:
                    invIts[i] = new ItemBuilder(ItemType.BOOK).name("§dЗачарования").build();
                    break;
                case 14:
                    invIts[i] = new ItemBuilder(ItemType.MOJANG_BANNER_PATTERN).name("§eОписание Предмета").build();
                    break;
                case 13:
                    invIts[i] = new ItemBuilder(ItemType.POINTED_DRIPSTONE).name("§6§l\\/").build();
                    break;
                case 12:
                    invIts[i] = new ItemBuilder(ItemType.NAME_TAG).name("§aИмя Предмета").build();
                    break;
                case 10:
                    invIts[i] = new ItemBuilder(ItemType.QUARTZ).name("§bЗначение Модели").build();
                    break;*/
                case 3, 5:
                    invIts[i] = new ItemBuilder(ItemType.HANGING_ROOTS).build();
                    break;
                default:
                    invIts[i] = new ItemBuilder((i & 1) == 1 ? ItemType.BROWN_STAINED_GLASS_PANE
                        : ItemType.ORANGE_STAINED_GLASS_PANE).name("§0.").build();
                    break;
            }
        }
    }

    public ItemMenu(final ItemStack it) {
        this.it = it.clone();
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
        final Inventory inv = its.getInventory();
        final ItemMeta im = it.getItemMeta();
        inv.setContents(invIts);
        inv.setItem(4, it);
        its.set(22, ClickableItem.of(new ItemBuilder(ItemType.HONEYCOMB).name("§aВыдать (ЛКМ) §6/ §eЗаменить (ПКМ) §6Предмет").build(), e -> {
            switch (e.getClick()) {
                case LEFT:
                case SHIFT_LEFT:
                    p.getInventory().addItem(it);
                    p.sendMessage(Ostrov.PREFIX + "Предмет удачно создан!");
                    break;
                case RIGHT:
                case SHIFT_RIGHT:
                    p.getInventory().setItemInOffHand(it);
                    p.sendMessage(Ostrov.PREFIX + "Предмет удачно заменен!");
                    break;
                default:
                    break;
            }
            p.closeInventory();
        }));

        its.set(12, new InputButton(InputType.ANVILL, new ItemBuilder(ItemType.NAME_TAG)
            .name("§7Имя:<white> " + (im.hasDisplayName() ? im.displayName() : "§8(Не Указано)"))
            .lore(" ", "§aКлик §7- Изменить имя", "§c'-' §7уберет имя предмета").build(), im.hasDisplayName()
            ? TCUtil.deform(im.displayName()).replace('§', '&') : "&7Предмет", msg -> {
            p.sendMessage("m1-" + msg);
            im.displayName(msg.equals("-") ? null : TCUtil.form(msg.replace('&', '§')));
            p.sendMessage(TCUtil.form(msg.replace('&', '§')));
            p.sendMessage(im.displayName());
            it.setItemMeta(im);
            reopen(p, its);
        }));

        ItemBuilder prep = new ItemBuilder(ItemType.MOJANG_BANNER_PATTERN);
        if (im.hasLore()) {
            prep.name("§7Описание:").lore(" ", "§eЛКМ §7- Добавить линию", "§eПКМ §7- Убрать посл. линию");
            for (final Component lr : im.lore()) prep.lore("- " + TCUtil.deform(lr).replace('§', '&'));
        } else {
            prep = new ItemBuilder(ItemType.MOJANG_BANNER_PATTERN).name("§7Описание: §8(Не Указано)")
                .lore(" ", "§eЛКМ §7- Добавить линию");
        }

        its.set(14, ClickableItem.from(prep.build(), e -> {
            final List<Component> lrs = im.lore();
            if (e.getClick().isLeftClick()) {
                PlayerInput.get(InputType.ANVILL, p, text -> {
                    if (lrs == null) {
                        im.lore(Arrays.asList(TCUtil.form(text)));
                    } else {
                        lrs.add(TCUtil.form(text));
                        im.lore(lrs);
                    }
                    it.setItemMeta(im);
                    reopen(p, its);
                }, "");
            } else if (lrs != null && !lrs.isEmpty()) {
                lrs.removeLast();
                im.lore(lrs);
                it.setItemMeta(im);
                reopen(p, its);
            }
        }));

        prep = new ItemBuilder(ItemType.SEA_LANTERN).name("§bСвечение");
        if (im.hasEnchantmentGlintOverride()) {
            final boolean glint = im.getEnchantmentGlintOverride();
            prep.glint(glint);
            if (glint) {
                prep.lore(" ", "§aОтдельное свечение,", "§dЛКМ §7- Поменять", "§5ПКМ §7- Сбросить");
            } else {
                prep.lore(" ", "§cСвечение выключено,", "§dЛКМ §7- Поменять", "§5ПКМ §7- Сбросить");
            }
        } else {
            prep.lore(" ", "§7Без правил свечения,", "§5ЛКМ §7- Добавить", "§8ПКМ §7- Сбросить");
        }
        its.set(16, ClickableItem.of(prep.build(), e -> {
            switch (e.getClick()) {
                case LEFT:
                case SHIFT_LEFT:
                    if (im.hasEnchantmentGlintOverride()) {
                        im.setEnchantmentGlintOverride(im
                            .getEnchantmentGlintOverride());
                    } else im.setEnchantmentGlintOverride(true);
                    it.setItemMeta(im);
                    reopen(p, its);
                    break;
                case RIGHT:
                case SHIFT_RIGHT:
                    if (im.hasEnchantmentGlintOverride()) {
                        im.setEnchantmentGlintOverride(null);
                    }
                    it.setItemMeta(im);
                    reopen(p, its);
                    break;
                default:
                    break;
            }
        }));

        prep = new ItemBuilder(ItemType.ENCHANTED_BOOK).name("§dЗачарования");
        if (im.hasEnchants()) {
            prep.glint(true);
            prep.lore(" ", "§aЕсть зачарования,", "§dЛКМ §7- Выдать зачар", "§cПКМ §7- Снять все зачары");
            for (final Map.Entry<Enchantment, Integer> en : im.getEnchants().entrySet()) {
                prep.enchant(en.getKey(), en.getValue());
            }
        } else {
            prep.lore(" ", "§7Зачарований нет,", "§dЛКМ §7- Выдать зачар", "§8ПКМ §7- Снять все зачары");
        }
        its.set(16, ClickableItem.of(prep.build(), e -> {
            switch (e.getClick()) {
                case LEFT:
                case SHIFT_LEFT:
                    //TODO enchant menu
                    it.setItemMeta(im);
                    reopen(p, its);
                    break;
                case RIGHT:
                case SHIFT_RIGHT:
                    im.removeEnchantments();
                    it.setItemMeta(im);
                    reopen(p, its);
                    break;
                default:
                    break;
            }
        }));

        its.set(20, ClickableItem.from(new ItemBuilder(ItemType.ENDER_PEARL).name("§фСкрытые Флаги")
            .lore("§7Клик - редактировать §ффлаги").build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
                SmartInventory.builder().id(p.getName() + " Flags").title("     §фНастройки Флагов")
                    .provider(new FlagMenu(it)).size(1, 9).build().open(p);
            }
        }));

        its.set(24, ClickableItem.from(new ItemBuilder(ItemType.ENDER_EYE).name("§кАттрибуты")
            .lore("§7Клик - редактировать §ксвойства").build(), e -> {
            if (e.getEvent() instanceof InventoryClickEvent) {
                SmartInventory.builder().id(p.getName() + " Flags").title("    §кНастройки Аттрибутов")
                    .provider(new AttrMenu(it)).size(3, 9).build().open(p);
            }
        }));

        final ItemType tp = it.getType().asItemType();
        its.set(0, ClickableItem.empty(new ItemBuilder(ItemType.QUARTZ).name("<mithril>Стандарт дата:")
            .lore(tp.getDefaultDataTypes().stream().map(dt -> {
                if (dt instanceof final DataComponentType.Valued<?> vdt)
                    return "§7- " + dt.key().asMinimalString() + " > " + tp.getDefaultData(vdt);
                return "§7- " + dt.key().asMinimalString();
            }).toList()).build()));
    }
}
