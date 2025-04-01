package ru.komiss77.modules.items.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.profile.Skins;
import ru.komiss77.utils.*;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InputButton.InputType;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;


public class ItemMenu implements InventoryProvider {

    private static final StringUtil.Split CLR_SPLIT = StringUtil.Split.SMALL;
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

    public static void open(final Player pl, final ItemStack it) {
        SmartInventory.builder().id("Item " + pl.getName()).provider(new ItemMenu(it))
            .size(3, 9).title("      §6Создание Предмета").build().open(pl);
    }

    @Override
    public void init(final Player p, final InventoryContent its) {
        p.playSound(p.getLocation(), Sound.BLOCK_SMITHING_TABLE_USE, 1f, 0.8f);

        final Inventory inv = its.getInventory();
        inv.setContents(invIts);
        inv.setItem(4, it);
        its.set(22, ClickableItem.of(new ItemBuilder(ItemType.HONEYCOMB).name("§aВыдать (ЛКМ) §6/ §eЗаменить (ПКМ) §6Предмет").build(), e -> {
            if (e.isRightClick()) {
                p.getInventory().setItemInOffHand(it);
                p.sendMessage(Ostrov.PREFIX + "Предмет удачно заменен!");
                p.closeInventory();
                return;
            }
            p.getInventory().addItem(it);
            p.sendMessage(Ostrov.PREFIX + "Предмет удачно создан!");
            p.closeInventory();
        }));

        final Component dnm = it.getData(DataComponentTypes.CUSTOM_NAME);
        its.set(12, ClickableItem.of(new ItemBuilder(ItemType.NAME_TAG)
            .name(TCUtil.form("§7Имя:<reset> ").append(dnm == null ? TCUtil.form("§8(Не Указано)") : dnm))
            .lore(" ", "§aЛКМ §7- Изменить имя", "§cПКМ §7- Сбросить имя").build(), e -> {
            if (e.isRightClick()) {
                it.resetData(DataComponentTypes.CUSTOM_NAME);
                reopen(p, its);
                return;
            }
            PlayerInput.get(InputType.ANVILL, p, msg -> {
                it.setData(DataComponentTypes.CUSTOM_NAME, TCUtil.form(msg));
                reopen(p, its);
            }, dnm == null ? "<gray>Предмет" : TCUtil.deform(dnm));
        }));

        ItemBuilder prep = new ItemBuilder(ItemType.MOJANG_BANNER_PATTERN);
        final ItemLore ilr = it.getData(DataComponentTypes.LORE);
        if (ilr == null || ilr.lines().isEmpty()) {
            prep = new ItemBuilder(ItemType.MOJANG_BANNER_PATTERN).name("§7Описание: §8(Не Указано)")
                .lore(" ", "§eЛКМ §7- Добавить линию");
        } else {
            prep.name("§7Описание:").lore(" ", "§eЛКМ §7- Добавить линию", "§eПКМ §7- Убрать посл. линию");
            for (final Component lr : ilr.lines()) prep.lore("- " + TCUtil.deform(lr));
        }

        its.set(14, ClickableItem.from(prep.build(), e -> {
            if (e.getClick().isLeftClick()) {
                PlayerInput.get(InputType.ANVILL, p, text -> {
                    if (ilr == null) {
                        it.setData(DataComponentTypes.LORE,
                            ItemLore.lore(Arrays.asList(TCUtil.form(text))));
                    } else {
                        it.setData(DataComponentTypes.LORE, ItemLore.lore()
                            .lines(ilr.lines()).addLine(TCUtil.form(text)));
                    }
                    reopen(p, its);
                }, "");
                return;
            }
            final List<Component> lns = ilr == null
                ? List.of() : new ArrayList<>(ilr.lines());
            if (!lns.isEmpty()) {
                lns.removeLast();
                it.setData(DataComponentTypes.LORE, ItemLore.lore(lns));
                reopen(p, its);
            }
        }));

        prep = new ItemBuilder(ItemType.BOOK).name("§dЗачарования");
        final boolean ste = ItemUtil.is(it, ItemType.ENCHANTED_BOOK);
        final ItemEnchantments ies = it.getData(ste
            ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS);
        if (ies == null || ies.enchantments().isEmpty()) {
            prep.lore(" ", "§7Зачарований нет,", "§dЛКМ §7- Выдать");
        } else {
            prep.glint(true);
            prep.lore(" ", "§aЕсть зачарования,", "§dЛКМ §7- Выдать зачар", "§cПКМ §7- Снять все зачары");
            for (final Map.Entry<Enchantment, Integer> en : ies.enchantments().entrySet()) {
                prep.enchant(en.getKey(), en.getValue(), ste);
            }
        }
        final Boolean gl = it.getData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        if (gl == null) {
            prep.lore(" ", "§8Свечение не указано,", "§5Шифт + ЛКМ §7- Добавить");
        } else {
            prep.glint(gl);
            if (gl) {
                prep.lore(" ", "§7Свечение §aвключено,",
                    "§dШифт + ЛКМ §7- Выключить", "§5Шифт + ПКМ §7- Сбросить");
            } else {
                prep.lore(" ", "§8Свечение §cвыключено,",
                    "§dШифт + ЛКМ §7- Включить", "§5Шифт + ПКМ §7- Сбросить");
            }
        }
        its.set(16, ClickableItem.of(prep.build(), e -> {
            switch (e.getClick()) {
                case LEFT:
                    SmartInventory.builder().id(p.getName() + " Flags").title("     §лЗачарования")
                        .provider(new EnchMenu(it)).size(6, 9).build().open(p);
                    break;
                case RIGHT:
                    it.resetData(ste ?
                        DataComponentTypes.STORED_ENCHANTMENTS
                        : DataComponentTypes.ENCHANTMENTS);
                    reopen(p, its);
                    break;
                case SHIFT_LEFT:
                    it.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE,
                        gl == null || !gl);
                    reopen(p, its);
                    break;
                case SHIFT_RIGHT:
                    it.resetData(DataComponentTypes
                        .ENCHANTMENT_GLINT_OVERRIDE);
                    reopen(p, its);
                    break;
                default:
                    break;
            }
        }));

        final boolean unb = it.hasData(DataComponentTypes.UNBREAKABLE);
        its.set(8, ClickableItem.from(new ItemBuilder(unb ? ItemType.CRYING_OBSIDIAN : ItemType.OBSIDIAN)
            .name("§чНеразрушимость").lore(unb ? "§7Клик - §5Выключить" : "§7Клик - §dВключить").build(), e -> {
            if (unb) {
                it.resetData(DataComponentTypes.UNBREAKABLE);
                reopen(p, its);
                return;
            }
            it.setData(DataComponentTypes.UNBREAKABLE);
            reopen(p, its);
        }));

        final Key mdl = it.getData(DataComponentTypes.ITEM_MODEL);
        its.set(10, ClickableItem.from(new ItemBuilder(ItemType.NETHERITE_SCRAP)
            .name("<sky>Модель - " + (mdl == null ? "<dark_gray>Не указана" : mdl.asMinimalString()))
            .lore("§7ЛКМ - §9Поставить", "§7ПКМ - §1Сбросить").build(), e -> {
            switch (e.getClick()) {
                case LEFT, SHIFT_LEFT:
                    PlayerInput.get(InputType.ANVILL, p, text -> {
                        it.setData(DataComponentTypes.ITEM_MODEL, Key.key(text.toLowerCase()));
                        reopen(p, its);
                    }, mdl == null ? "" : mdl.asMinimalString());
                    break;
                case RIGHT, SHIFT_RIGHT:
                    it.resetData(DataComponentTypes.ITEM_MODEL);
                    reopen(p, its);
                    break;
            }
        }));

        final DyedItemColor dic = it.getData(DataComponentTypes.DYED_COLOR);
        its.set(26, ClickableItem.from(new ItemBuilder(ItemType.CAULDRON)
            .name("<pink>Цвет - " + (dic == null ? "<dark_gray>Не указан" : "<#" + Integer.toHexString(dic.color().asRGB()) + ">Такой"))
            .lore("§7ЛКМ - §6Изменить", "§7ПКМ - §4Сбросить").build(), e -> {
            switch (e.getClick()) {
                case LEFT, SHIFT_LEFT:
                    final String spl = CLR_SPLIT.get();
                    final Color clr = dic == null ? Color.fromRGB(0) : dic.color();
                    PlayerInput.get(InputType.ANVILL, p, text -> {
                        final String[] csp = CLR_SPLIT.split(text);
                        if (csp.length != 3) {
                            p.sendMessage("§cНужно иметь 3 числа, разделеных " + spl);
                            reopen(p, its);
                            return;
                        }
                        it.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(Color
                            .fromRGB(Math.clamp(NumUtil.intOf(csp[0], 0), 0, 255),
                                Math.clamp(NumUtil.intOf(csp[1], 0), 0, 255),
                                Math.clamp(NumUtil.intOf(csp[2], 0), 0, 255))));
                        reopen(p, its);
                    }, clr.getRed() + spl + clr.getGreen() + spl + clr.getBlue());
                    break;
                case RIGHT, SHIFT_RIGHT:
                    it.resetData(DataComponentTypes.DYED_COLOR);
                    reopen(p, its);
                    break;
            }
        }));

        its.set(20, ClickableItem.from(new ItemBuilder(ItemType.ENDER_PEARL).name("§фСкрытые Флаги")
            .lore("§7Клик - редактировать §ффлаги").build(), e -> {
            SmartInventory.builder().id(p.getName() + " Flags").title("     §фНастройки Флагов")
                .provider(new FlagMenu(it)).size(1, 9).build().open(p);
        }));

        its.set(24, ClickableItem.from(new ItemBuilder(ItemType.ENDER_EYE).name("§кАттрибуты")
            .lore("§7Клик - редактировать §ксвойства").build(), e -> {
            SmartInventory.builder().id(p.getName() + " Flags").title("    §кНастройки Аттрибутов")
                .provider(new AttrMenu(it)).size(3, 9).build().open(p);
        }));

        final ItemType tp = it.getType().asItemType();
        its.set(0, ClickableItem.of(new ItemBuilder(ItemType.QUARTZ).name("<mithril>Стандарт дата:")
            .lore(tp.getDefaultDataTypes().stream().map(dt -> {
                if (dt instanceof final DataComponentType.Valued<?> vdt)
                    return "§7- " + dt.key().asMinimalString() + " > " + tp.getDefaultData(vdt);
                return "§7- " + dt.key().asMinimalString();
            }).toList()).build(), e -> {
            tp.getDefaultDataTypes().forEach(dt ->
                p.sendMessage(dt instanceof final DataComponentType.Valued<?> vdt
                    ? tp.getDefaultData(vdt).toString() : dt.key().asMinimalString()));
        }));

        if (tp == ItemType.PLAYER_HEAD) {
            final ResolvableProfile rp = it.getData(DataComponentTypes.PROFILE);
            its.set(6, ClickableItem.of(new ItemBuilder(ItemType.TURTLE_HELMET)
                .name("<gray>Профиль: " + (rp == null || rp.name() == null ? "§8(Не Указан)" : "<olive>" + rp.name()))
                .lore(" ", "§aЛКМ §7- Изменить", "§cПКМ §7- Сбросить").build(), e -> {
                if (e.isRightClick()) {
                    it.resetData(DataComponentTypes.PROFILE);
                    reopen(p, its);
                    return;
                }
                PlayerInput.get(InputType.ANVILL, p, msg -> {
                    Skins.future(msg, pp -> {
                        it.setData(DataComponentTypes.PROFILE,
                            ResolvableProfile.resolvableProfile(pp));
                        reopen(p, its);
                    });
                    reopen(p, its);
                }, dnm == null ? rp == null ? "Name" : rp.name() : TCUtil.deform(dnm));
            }));
        }
    }
}
