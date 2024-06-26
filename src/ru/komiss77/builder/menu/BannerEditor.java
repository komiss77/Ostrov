package ru.komiss77.builder.menu;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;

public class BannerEditor implements InventoryProvider {

    private static final ClickableItem empty = ClickableItem.empty(new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE).name("§8пустой слой").build());
    private static final ClickableItem c = ClickableItem.empty(new ItemBuilder(Material.GLOW_LICHEN).name("§8Линия-разделитель").build());
    private static final ClickableItem select = ClickableItem.empty(new ItemBuilder(Material.POINTED_DRIPSTONE).name("§8Ниже-варианты на выбор").build());
    private static final ClickableItem resut = ClickableItem.empty(new ItemBuilder(Material.IRON_NUGGET).name("§8Справа-результат").build());

    private final Block b;
    private Material mat;
    private List<Pattern> patterns;
    private int editIdx; //индекс редактируемого слоя
    private EditMode mode = EditMode.Нет;
    private static final String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!?.";

    //если хоть раз меню откроется, статичные мапы прогрузятся и меню у всех будет работать быстрее
    private static final HashMap<Character, Pattern[]> alphabet;
    private static final EnumMap<PatternType, ItemStack> patternExample;

    static {
        alphabet = new HashMap<>();
        for (char c : symbols.toCharArray()) {
            final List<Pattern> list = alphabet(DyeColor.WHITE, DyeColor.BLACK, c);
            alphabet.put(c, list.toArray(new Pattern[list.size()]));
        }
        patternExample = new EnumMap<>(PatternType.class);
        for (PatternType patternType : PatternType.values()) {
            final Pattern pattern = new Pattern(DyeColor.BLACK, patternType);
            patternExample.put(patternType, genBanner(null,
                    List.of(Component.text("ЛКМ - выбрать")),
                    pattern));
        }
    }

    public BannerEditor(Block b) {
        this.b = b;
    }

    public static void edit(final Player p, final Block b) {
        SmartInventory.builder()
                .id("BannerEditor" + p.getName())
                .provider(new BannerEditor(b))
                .size(6, 9)
                .title("§6Настройка банера")
                .build()
                .open(p);
    }

    @Override
    public void init(final Player p, final InventoryContent content) {
        //its.fillRect(0, 0, 4, 8, c);
        content.fillRect(9, 17, c);
        p.playSound(p.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 0.3f, 2);

        if (patterns == null) {
            if (!Tag.BANNERS.isTagged(b.getType())) {
                p.sendMessage("§cБлок уже не баннер!");
                return;
            }
            final Banner ban = (Banner) b.getState();
            patterns = ban.getPatterns();
            mat = b.getType();
        }

        if (mode != EditMode.Нет) {
            if (mode == EditMode.Основа) {
                content.set(1, 0, select);
            } else {
                content.set(1, editIdx + 1, select);
            }

        }

        content.set(0, 0, ClickableItem.of(new ItemBuilder(mat)
                .name("Основа")
                .addLore("ЛКМ - изменить цвет")
                .addLore("ПКМ - наложить символ")
                .build(), e -> {
            if (e.getClick() == ClickType.LEFT) {
                mode = EditMode.Основа;
                reopen(p, content);
            } else if (e.getClick() == ClickType.RIGHT) {
                mode = EditMode.Символ;
                reopen(p, content);
            }
        }));

        content.set(0, 7, resut);

        int slot = 1;
        for (final Pattern pt : patterns) {
            final int idx = slot - 1;
            content.set(0, slot, ClickableItem.of(
                    genBanner("Слой " + slot,
                            List.of(
                                    Component.text("ЛКМ - настроить Маску"),
                                    Component.text("ПКМ - настроить Цвет"),
                                    Component.text(idx == 0 ? "Верхний слой" : "Шифт+ПКМ - переместить выше"),
                                    Component.text("Клав.Q - удалить")), pt), e -> {
                        switch (e.getClick()) {
                            case LEFT -> {
                                editIdx = idx;
                                mode = EditMode.Маска;
                                reopen(p, content);
                            }
                            case RIGHT -> {
                                editIdx = idx;
                                mode = EditMode.Цвет;
                                reopen(p, content);
                            }
                            case SHIFT_RIGHT -> {
                                if (idx != 0) {
                                    final Pattern ptBefore = patterns.set(idx - 1, pt);
                                    patterns.set(idx, ptBefore);
                                    mode = EditMode.Нет;
                                    reopen(p, content);
                                }
                            }
                            case DROP -> {
                                patterns.remove(idx);
                                mode = EditMode.Нет;
                                reopen(p, content);
                            }
                        }

                    }
            ));
            slot++;
        }

        if (patterns.size() < 6) {
            content.set(0, slot, ClickableItem.of(ItemUtils.add, e -> {
                final Pattern pattern = new Pattern(DyeColor.WHITE, PatternType.BASE);
                patterns.add(pattern);
                editIdx = patterns.indexOf(pattern);
                mode = EditMode.Маска; //ниже сразу развернёт выбор
                reopen(p, content);
            }));
            slot++;
            while (slot <= 6) {
                content.set(0, slot, empty);
                slot++;
            }
        }


        content.set(0, 7, resut);

        final ItemStack is = new ItemStack(mat);
        ItemMeta im = is.getItemMeta();
        im.displayName(Component.text("Вот что получается"));
        im.lore(List.of(Component.text("ЛКМ - применить к баннеру")));
        ((BannerMeta) im).setPatterns(patterns);
        is.setItemMeta(im);

        content.set(0, 8, ClickableItem.of(is, e -> {
            p.closeInventory();
            if (!Tag.BANNERS.isTagged(b.getType())) {
                p.sendMessage("§cБлок уже не баннер!");
                return;
            }
            if (b.getType() != mat) {
                b.setType(mat);
            }
            final Banner ban = (Banner) b.getState();
            ban.setPatterns(patterns);
            ban.update();
        }));


        switch (mode) {

            case Основа:

                for (DyeColor dc : DyeColor.values()) {
                    final Material m = TCUtils.changeColor(mat, dc);
                    content.add(ClickableItem.of(new ItemStack(m), e -> {
                        mat = m;
                        reopen(p, content);
                    }));
                }
                break;

            case Символ:

                for (char c : symbols.toCharArray()) {
                    content.add(ClickableItem.of(genBanner("§f" + String.valueOf(c),
                            List.of(Component.text("ЛКМ - наложить слои")),
                            alphabet.get(c)), e -> {
                        patterns = alphabet(DyeColor.WHITE, DyeColor.BLACK, c);
                        mode = EditMode.Нет;
                        reopen(p, content);
                    }));
                }

                break;

            case Маска:
                for (PatternType patternType : PatternType.values()) {
                    content.add(ClickableItem.of(patternExample.get(patternType), e -> {
                        patterns.set(editIdx, ((BannerMeta) patternExample.get(patternType).getItemMeta()).getPatterns().get(0));
                        reopen(p, content);
                    }));
                }
                break;

            case Цвет:
                final PatternType patternType = patterns.get(editIdx).getPattern();
                for (DyeColor dc : DyeColor.values()) {
                    final Pattern pattern = new Pattern(dc, patternType);
                    content.add(ClickableItem.of(genBanner(null,
                            List.of(Component.text("ЛКМ - выбрать")),
                            pattern), e -> {
                        patterns.set(editIdx, pattern);
                        reopen(p, content);
                    }));
                }
                break;

        }

    }


    private static ItemStack genBanner(final String name, List<Component> lore, Pattern... patterns) {
        final ItemStack is = new ItemStack(Material.WHITE_BANNER);
        ItemMeta im = is.getItemMeta();
        if (name != null) {
            im.displayName(Component.text(name));
        }
        if (lore != null) {
            im.lore(lore);
        }
        if (patterns != null) {
            for (Pattern pt : patterns) {
                ((BannerMeta) im).addPattern(pt);
            }
        }
        //if (enchant) {
        //im.addEnchant(Enchantment.LUCK, 1, true);
        //im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        // }
        is.setItemMeta(im);
        return is;
    }


    public static List<Pattern> alphabet(final DyeColor baseColor, final DyeColor dyeColor, final char alphabet) {
        //ItemStack banner = new ItemStack(DyeColorUtil.toBannerMaterial(baseColor));
        //BannerMeta bannerMeta = (BannerMeta) banner.getItemMeta();

        final List<Pattern> list = new ArrayList<>();
        boolean invertBanner = false;
        boolean bordered = false;

        if (!bordered) {
            switch (alphabet) {
                case 'A':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    break;
                case 'B':
                case '8':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    break;
                case 'C':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    break;
                case 'D':
                    invertBanner = true;
                    list.add(new Pattern(baseColor, PatternType.RHOMBUS_MIDDLE));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    break;
                case 'E':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    break;
                case 'F':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    break;
                case 'G':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    break;
                case 'H':
                    invertBanner = true;
                    list.add(new Pattern(baseColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    break;
                case 'I':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_CENTER));
                    break;
                case 'J':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    break;
                case 'K':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    break;
                case 'L':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    break;
                case 'M':
                    list.add(new Pattern(dyeColor, PatternType.TRIANGLE_TOP));
                    list.add(new Pattern(baseColor, PatternType.TRIANGLES_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    break;
                case 'N':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.TRIANGLE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT));
                    break;
                case 'O':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    break;
                case 'P':
                    invertBanner = true;
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    break;
                case 'Q':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    break;
                case 'R':
                    invertBanner = true;
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_MIRROR));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_VERTICAL));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    break;
                case 'S':
                    list.add(new Pattern(dyeColor, PatternType.TRIANGLE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.TRIANGLE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.SQUARE_TOP_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.SQUARE_BOTTOM_LEFT));
                    list.add(new Pattern(baseColor, PatternType.RHOMBUS_MIDDLE));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT));
                    break;
                case 'T':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_CENTER));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    break;
                case 'U':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    break;
                case 'V':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.TRIANGLES_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT));
                    break;
                case 'W':
                    list.add(new Pattern(dyeColor, PatternType.TRIANGLE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.TRIANGLES_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    break;
                case 'X':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT));
                    break;
                case 'Y':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_MIRROR));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT));
                    break;
                case 'Z':
                case '2':
                    list.add(new Pattern(dyeColor, PatternType.TRIANGLE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.TRIANGLE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.SQUARE_TOP_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.SQUARE_BOTTOM_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.RHOMBUS_MIDDLE));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT));
                    break;
                case '1':
                    list.add(new Pattern(dyeColor, PatternType.SQUARE_TOP_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_CENTER));
                    break;
                case '3':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    break;
                case '4':
                    invertBanner = true;
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    break;
                case '5':
                    invertBanner = true;
                    list.add(new Pattern(baseColor, PatternType.HALF_VERTICAL_MIRROR));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_MIRROR));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.DIAGONAL_RIGHT_MIRROR));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    break;
                case '6':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    break;
                case '7':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.DIAGONAL_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT));
                    break;
                case '9':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_MIRROR));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    break;
                case '0':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT));
                    break;
                case '?':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_MIRROR));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(dyeColor, PatternType.SQUARE_BOTTOM_LEFT));
                    break;
                case '!':
                    list.add(new Pattern(dyeColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(dyeColor, PatternType.SQUARE_BOTTOM_LEFT));
                    list.add(new Pattern(baseColor, PatternType.HALF_VERTICAL_MIRROR));
                    break;
                case '.':
                    list.add(new Pattern(dyeColor, PatternType.SQUARE_BOTTOM_LEFT));
                    break;
            }

        } else {
            //有框
            switch (alphabet) {
                case 'A':
                    invertBanner = true;
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'B':
                case '8':
                    invertBanner = true;
                    list.add(new Pattern(baseColor, PatternType.STRIPE_CENTER));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'C':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'D':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.CURLY_BORDER));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'E':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'F':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'G':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'H':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'I':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_CENTER));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'J':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'K':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.HALF_VERTICAL_MIRROR));
                    list.add(new Pattern(dyeColor, PatternType.CROSS));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'L':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'M':
                    list.add(new Pattern(dyeColor, PatternType.TRIANGLE_TOP));
                    list.add(new Pattern(baseColor, PatternType.TRIANGLES_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'N':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.DIAGONAL_RIGHT_MIRROR));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'O':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'P':
                    invertBanner = true;
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'Q':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    list.add(new Pattern(dyeColor, PatternType.SQUARE_BOTTOM_RIGHT));
                    break;
                case 'R':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_MIRROR));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'S':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.RHOMBUS_MIDDLE));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    list.add(new Pattern(baseColor, PatternType.CURLY_BORDER));
                    break;
                case 'T':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_CENTER));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'U':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'V':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.TRIANGLES_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'W':
                    list.add(new Pattern(dyeColor, PatternType.TRIANGLE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.TRIANGLES_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'X':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_CENTER));
                    list.add(new Pattern(dyeColor, PatternType.CROSS));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'Y':
                    list.add(new Pattern(dyeColor, PatternType.CROSS));
                    list.add(new Pattern(baseColor, PatternType.HALF_VERTICAL_MIRROR));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'Z':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '1':
                    list.add(new Pattern(dyeColor, PatternType.SQUARE_TOP_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_CENTER));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '2':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.RHOMBUS_MIDDLE));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '3':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '4':
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '5':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(baseColor, PatternType.CURLY_BORDER));
                    list.add(new Pattern(dyeColor, PatternType.SQUARE_BOTTOM_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '6':
                    invertBanner = true;
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_MIRROR));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '7':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.DIAGONAL_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(dyeColor, PatternType.SQUARE_BOTTOM_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '9':
                    invertBanner = true;
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '0':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '?':
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_MIRROR));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(dyeColor, PatternType.SQUARE_BOTTOM_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '!':
                    list.add(new Pattern(dyeColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(dyeColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(dyeColor, PatternType.SQUARE_BOTTOM_LEFT));
                    list.add(new Pattern(baseColor, PatternType.HALF_VERTICAL_MIRROR));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '.':
                    list.add(new Pattern(dyeColor, PatternType.SQUARE_BOTTOM_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
            }
        }
        if (invertBanner) {
//            bannerMeta.setBaseColor(dyeColor);
            //banner = new ItemStack(DyeColorUtil.toBannerMaterial(dyeColor));
        }
        return list;
    }


}

enum EditMode {Нет, Основа, Символ, Маска, Цвет};