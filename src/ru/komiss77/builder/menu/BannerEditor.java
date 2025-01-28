package ru.komiss77.builder.menu;

import java.util.*;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BannerPatternLayers;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.BannerMeta;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.komiss77.utils.inventory.InventoryContent;
import ru.komiss77.utils.inventory.InventoryProvider;
import ru.komiss77.utils.inventory.SmartInventory;


public class BannerEditor implements InventoryProvider {

    private static final ClickableItem empty = ClickableItem.empty(new ItemBuilder(ItemType.LIGHT_GRAY_STAINED_GLASS_PANE).name("§8пустой слой").build());
    private static final ClickableItem c = ClickableItem.empty(new ItemBuilder(ItemType.GLOW_LICHEN).name("§8Линия-разделитель").build());
    private static final ClickableItem select = ClickableItem.empty(new ItemBuilder(ItemType.POINTED_DRIPSTONE).name("§8Ниже-варианты на выбор").build());
    private static final ClickableItem resut = ClickableItem.empty(new ItemBuilder(ItemType.IRON_NUGGET).name("§8Справа-результат").build());

    private final Block b;
    private ItemType mat;
    private List<Pattern> patterns;
    private int editIdx; //индекс редактируемого слоя
    private EditMode mode = EditMode.NO;
    private boolean bordered = true;
    private static final String symbols = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!?.";

    //если хоть раз меню откроется, статичные мапы прогрузятся и меню у всех будет работать быстрее
    private static final HashMap<Character, Pattern[]> alphabetBordered;
    private static final HashMap<Character, Pattern[]> alphabetNotBordered;
    private static final HashMap<PatternType, ItemStack> patternExample;

    private static final List<PatternType> PARRENTS = OStrap.retrieveAll(RegistryKey.BANNER_PATTERN);

    static {
        alphabetBordered = new HashMap<>();
        for (char c : symbols.toCharArray()) {
            final List<Pattern> list = alphabet(DyeColor.WHITE, DyeColor.BLACK, c, true);
            alphabetBordered.put(c, list.toArray(new Pattern[0]));
        }
        alphabetNotBordered = new HashMap<>();
        for (char c : symbols.toCharArray()) {
            final List<Pattern> list = alphabet(DyeColor.WHITE, DyeColor.BLACK, c, false);
            alphabetNotBordered.put(c, list.toArray(new Pattern[0]));
        }
        patternExample = new HashMap<>();
        for (PatternType patternType : PARRENTS) {
            final Pattern pattern = new Pattern(DyeColor.BLACK, patternType);
            patternExample.put(patternType, new ItemBuilder(ItemType.WHITE_BANNER).lore("§7ЛКМ - §aвыбрать")
                .set(DataComponentTypes.BANNER_PATTERNS, BannerPatternLayers.bannerPatternLayers(List.of(pattern))).build());
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
            mat = b.getType().asItemType();
        }

        if (mode != EditMode.NO) {
            if (mode == EditMode.BASE) {
                content.set(1, 0, select);
            } else {
                content.set(1, editIdx + 1, select);
            }

        }

        content.set(0, 0, ClickableItem.of(new ItemBuilder(mat)
            .name("Основа")
            .lore("§fЛКМ §7- §6изменить цвет")
            .lore("")
            .lore("§fПКМ §7- §9символ в рамке")
            .lore("§fШифт+ПКМ §7- §9символ без рамки")
            .build(), e -> {
            if (e.getClick() == ClickType.LEFT) {
                mode = EditMode.BASE;
                reopen(p, content);
            } else if (e.getClick() == ClickType.RIGHT) {
                mode = EditMode.CHAR;
                bordered = true;
                reopen(p, content);
            } else if (e.getClick() == ClickType.SHIFT_RIGHT) {
                mode = EditMode.CHAR;
                bordered = false;
                reopen(p, content);
            }
        }));

        content.set(0, 7, resut);

        int slot = 1;
        for (final Pattern pt : patterns) {
            final int idx = slot - 1;
            content.set(0, slot, ClickableItem.of(
                new ItemBuilder(ItemType.WHITE_BANNER).name("Слой " + slot)
                    .lore(List.of(Component.text("§fЛКМ §7- §6настроить Маску"),
                        Component.text("§fПКМ §7- §6настроить Цвет"),
                        Component.text(idx == 0 ? "§3Верхний слой" : "§fШифт+ПКМ §7- §3переместить выше"),
                        Component.text("§cКлав.Q - удалить"))).set(DataComponentTypes.BANNER_PATTERNS,
                    BannerPatternLayers.bannerPatternLayers(List.of(pt))).build(), e -> {
                    switch (e.getClick()) {
                        case LEFT -> {
                            editIdx = idx;
                            mode = EditMode.MASK;
                            reopen(p, content);
                        }
                        case RIGHT -> {
                            editIdx = idx;
                            mode = EditMode.COLOR;
                            reopen(p, content);
                        }
                        case SHIFT_RIGHT -> {
                            if (idx != 0) {
                                final Pattern ptBefore = patterns.set(idx - 1, pt);
                                patterns.set(idx, ptBefore);
                                mode = EditMode.NO;
                                reopen(p, content);
                            }
                        }
                        case DROP -> {
                            patterns.remove(idx);
                            mode = EditMode.NO;
                            reopen(p, content);
                        }
                    }

                }
            ));
            slot++;
        }

        if (patterns.size() < 6) {
            content.set(0, slot, ClickableItem.of(ItemUtil.add, e -> {
                final Pattern pattern = new Pattern(DyeColor.WHITE, PatternType.BASE);
                patterns.add(pattern);
                editIdx = patterns.indexOf(pattern);
                mode = EditMode.MASK; //ниже сразу развернёт выбор
                reopen(p, content);
            }));
            slot++;
            while (slot <= 6) {
                content.set(0, slot, empty);
                slot++;
            }
        }


        content.set(0, 7, resut);

        final ItemStack is = new ItemBuilder(mat).name("Вот что получается").lore("§fЛКМ §7- §bприменить к баннеру")
            .set(DataComponentTypes.BANNER_PATTERNS, BannerPatternLayers.bannerPatternLayers(patterns)).build();

        content.set(0, 8, ClickableItem.of(is, e -> {
            p.closeInventory();
            if (!Tag.BANNERS.isTagged(b.getType())) {
                p.sendMessage("§cБлок уже не баннер!");
                return;
            }
            if (!Objects.equals(b.getType().asItemType(), mat)) {
                b.setBlockData(mat.getBlockType().createBlockData());
            }
            final Banner ban = (Banner) b.getState();
            ban.setPatterns(patterns);
            ban.update();
        }));


        switch (mode) {

            case BASE:

                for (DyeColor dc : DyeColor.values()) {
                    final ItemType m = TCUtil.changeColor(mat, dc);
                    content.add(ClickableItem.of(m.createItemStack(), e -> {
                        mat = m;
                        reopen(p, content);
                    }));
                }
                break;

            case CHAR:

                for (char c : symbols.toCharArray()) {
                    content.add(ClickableItem.of(new ItemBuilder(inverted(bordered, c) ? ItemType.BLACK_BANNER : ItemType.WHITE_BANNER)
                        .name("§f" + c).lore("§fЛКМ §7- §1наложить слои").set(DataComponentTypes.BANNER_PATTERNS, BannerPatternLayers
                            .bannerPatternLayers(Arrays.asList(bordered ? alphabetBordered.get(c) : alphabetNotBordered.get(c)))).build(), e -> {
                        patterns = alphabet(DyeColor.WHITE, DyeColor.BLACK, c, bordered);
                        mode = EditMode.NO;
                        reopen(p, content);
                    }));
                }

                break;

            case MASK:
                for (PatternType patternType : PARRENTS) {
                    content.add(ClickableItem.of(patternExample.get(patternType), e -> {
                        patterns.set(editIdx, ((BannerMeta) patternExample.get(patternType).getItemMeta()).getPatterns().getFirst());
                        reopen(p, content);
                    }));
                }
                break;

            case COLOR:
                final PatternType patternType = patterns.get(editIdx).getPattern();
                for (DyeColor dc : DyeColor.values()) {
                    final Pattern pattern = new Pattern(dc, patternType);
                    content.add(ClickableItem.of(new ItemBuilder(ItemType.WHITE_BANNER).lore("ЛКМ - выбрать")
                        .set(DataComponentTypes.BANNER_PATTERNS, BannerPatternLayers.bannerPatternLayers(List.of(pattern))).build(), e -> {
                        patterns.set(editIdx, pattern);
                        reopen(p, content);
                    }));
                }
                break;

        }

    }

    public static boolean inverted(final boolean bordered, final char c) {
        if (bordered) {
            switch (c) {
                case 'A', 'B', '8', 'P', '6', '9' -> {
                    return true;
                }
            }
        } else {
            switch (c) {
                case 'D', 'H', 'P', 'R', '4', '5' -> {
                    return true;
                }
            }
        }

        return false;
    }

    public static List<Pattern> alphabet(final DyeColor baseColor, final DyeColor patternColor, final char alphabet, final boolean bordered) {
        final List<Pattern> list = new ArrayList<>();

        if (bordered) {
            switch (alphabet) {
                case 'A':
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'B':
                case '8':
                    list.add(new Pattern(baseColor, PatternType.STRIPE_CENTER));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'C':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'D':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.CURLY_BORDER));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'E':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'F':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'G':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'H':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'I':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_CENTER));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'J':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'K':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.HALF_VERTICAL_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.CROSS));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'L':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'M':
                    list.add(new Pattern(patternColor, PatternType.TRIANGLE_TOP));
                    list.add(new Pattern(baseColor, PatternType.TRIANGLES_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'N':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.DIAGONAL_UP_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'O':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'P':
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'Q':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    list.add(new Pattern(patternColor, PatternType.SQUARE_BOTTOM_RIGHT));
                    break;
                case 'R':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'S':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.RHOMBUS));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    list.add(new Pattern(baseColor, PatternType.CURLY_BORDER));
                    break;
                case 'T':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_CENTER));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'U':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'V':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.TRIANGLES_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'W':
                    list.add(new Pattern(patternColor, PatternType.TRIANGLE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.TRIANGLES_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'X':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_CENTER));
                    list.add(new Pattern(patternColor, PatternType.CROSS));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'Y':
                    list.add(new Pattern(patternColor, PatternType.CROSS));
                    list.add(new Pattern(baseColor, PatternType.HALF_VERTICAL_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case 'Z':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '1':
                    list.add(new Pattern(patternColor, PatternType.SQUARE_TOP_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_CENTER));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '2':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.RHOMBUS));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '3':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '4':
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '5':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(baseColor, PatternType.CURLY_BORDER));
                    list.add(new Pattern(patternColor, PatternType.SQUARE_BOTTOM_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '6':
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '7':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.DIAGONAL_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(patternColor, PatternType.SQUARE_BOTTOM_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '9':
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '0':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '?':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(patternColor, PatternType.SQUARE_BOTTOM_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '!':
                    list.add(new Pattern(patternColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(patternColor, PatternType.SQUARE_BOTTOM_LEFT));
                    list.add(new Pattern(baseColor, PatternType.HALF_VERTICAL_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
                case '.':
                    list.add(new Pattern(patternColor, PatternType.SQUARE_BOTTOM_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    break;
            }
        } else {
            switch (alphabet) {
                case 'A':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    break;
                case 'B':
                case '8':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    break;
                case 'C':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    break;
                case 'D':
                    list.add(new Pattern(baseColor, PatternType.RHOMBUS));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    break;
                case 'E':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    break;
                case 'F':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    break;
                case 'G':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    break;
                case 'H':
                    list.add(new Pattern(baseColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    break;
                case 'I':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_CENTER));
                    break;
                case 'J':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    break;
                case 'K':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    break;
                case 'L':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    break;
                case 'M':
                    list.add(new Pattern(patternColor, PatternType.TRIANGLE_TOP));
                    list.add(new Pattern(baseColor, PatternType.TRIANGLES_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    break;
                case 'N':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.TRIANGLE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNRIGHT));
                    break;
                case 'O':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    break;
                case 'P':
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    break;
                case 'Q':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    break;
                case 'R':
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_VERTICAL));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    break;
                case 'S':
                    list.add(new Pattern(patternColor, PatternType.TRIANGLE_TOP));
                    list.add(new Pattern(patternColor, PatternType.TRIANGLE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.SQUARE_TOP_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.SQUARE_BOTTOM_LEFT));
                    list.add(new Pattern(baseColor, PatternType.RHOMBUS));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNRIGHT));
                    break;
                case 'T':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_CENTER));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    break;
                case 'U':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    break;
                case 'V':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.TRIANGLES_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNLEFT));
                    break;
                case 'W':
                    list.add(new Pattern(patternColor, PatternType.TRIANGLE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.TRIANGLES_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    break;
                case 'X':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNLEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNRIGHT));
                    break;
                case 'Y':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNLEFT));
                    break;
                case 'Z':
                case '2':
                    list.add(new Pattern(patternColor, PatternType.TRIANGLE_TOP));
                    list.add(new Pattern(patternColor, PatternType.TRIANGLE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.SQUARE_TOP_LEFT));
                    list.add(new Pattern(patternColor, PatternType.SQUARE_BOTTOM_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.RHOMBUS));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNLEFT));
                    break;
                case '1':
                    list.add(new Pattern(patternColor, PatternType.SQUARE_TOP_LEFT));
                    list.add(new Pattern(baseColor, PatternType.BORDER));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_CENTER));
                    break;
                case '3':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    break;
                case '4':
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    break;
                case '5':
                    list.add(new Pattern(baseColor, PatternType.HALF_VERTICAL_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(baseColor, PatternType.DIAGONAL_UP_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNRIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    break;
                case '6':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    break;
                case '7':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(baseColor, PatternType.DIAGONAL_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNLEFT));
                    break;
                case '9':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    break;
                case '0':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_LEFT));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_DOWNLEFT));
                    break;
                case '?':
                    list.add(new Pattern(patternColor, PatternType.STRIPE_RIGHT));
                    list.add(new Pattern(baseColor, PatternType.HALF_HORIZONTAL_BOTTOM));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_TOP));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(patternColor, PatternType.SQUARE_BOTTOM_LEFT));
                    break;
                case '!':
                    list.add(new Pattern(patternColor, PatternType.HALF_HORIZONTAL));
                    list.add(new Pattern(patternColor, PatternType.STRIPE_MIDDLE));
                    list.add(new Pattern(patternColor, PatternType.SQUARE_BOTTOM_LEFT));
                    list.add(new Pattern(baseColor, PatternType.HALF_VERTICAL_RIGHT));
                    break;
                case '.':
                    list.add(new Pattern(patternColor, PatternType.SQUARE_BOTTOM_LEFT));
                    break;
            }


        }
        return list;
    }


}

enum EditMode {NO, BASE, CHAR, MASK, COLOR};