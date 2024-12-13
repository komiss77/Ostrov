package ru.komiss77.utils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.util.Index;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Colorable;
import org.intellij.lang.annotations.Subst;
import ru.komiss77.Ostrov;
import ru.komiss77.notes.Slow;
import ru.komiss77.version.GameApi;


public class TCUtil {

    //    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9a-xа-я]");
    public static final TextComponent EMPTY;

    private static final Index<DyeColor, TextColor> dyeIx;
    private static final Index<Character, TextColor> chrIx;
    private static final Index<Color, TextColor> clrIx;
    private static final Set<Entry<Character, TextColor>> chrSet;
    private static final MiniMessage msg;
    private static final int tcSize;

    public static final char STYLE = '§';
    //    public static final char form = '᨟';
    //public static final char HEX = '#';
    //public static final char GRAD = '|';

    /**
     * 60% - Neutral color
     */
    public static String N = "§7";
    /**
     * 30% - Primary color
     */
    public static String P = "§7";
    /**
     * 10% - Action color
     */
    public static String A = "§7";

    static {
        EMPTY = Component.empty();
        final Map<TextColor, DyeColor> dyeLs = new HashMap<>();
        final Map<TextColor, Character> chrLs = new HashMap<>();
        final Map<TextColor, Color> clrLs = new HashMap<>();
        dyeLs.put(NamedTextColor.BLACK, DyeColor.BLACK);
        chrLs.put(NamedTextColor.BLACK, '0');//void
        clrLs.put(NamedTextColor.BLACK, Color.BLACK);
        dyeLs.put(NamedTextColor.DARK_BLUE, DyeColor.BLUE);
        chrLs.put(NamedTextColor.DARK_BLUE, '1');//adventure
        clrLs.put(NamedTextColor.DARK_BLUE, Color.NAVY);
        dyeLs.put(NamedTextColor.DARK_GREEN, DyeColor.GREEN);
        chrLs.put(NamedTextColor.DARK_GREEN, '2');//nature
        clrLs.put(NamedTextColor.DARK_GREEN, Color.GREEN);
        dyeLs.put(NamedTextColor.DARK_AQUA, DyeColor.CYAN);
        chrLs.put(NamedTextColor.DARK_AQUA, '3');//wisdom
        clrLs.put(NamedTextColor.DARK_AQUA, Color.TEAL);
        dyeLs.put(NamedTextColor.DARK_RED, DyeColor.BROWN);
        chrLs.put(NamedTextColor.DARK_RED, '4');//war
        clrLs.put(NamedTextColor.DARK_RED, Color.MAROON);
        dyeLs.put(NamedTextColor.DARK_PURPLE, DyeColor.MAGENTA);
        chrLs.put(NamedTextColor.DARK_PURPLE, '5');//royalty
        clrLs.put(NamedTextColor.DARK_PURPLE, Color.PURPLE);
        dyeLs.put(NamedTextColor.GOLD, DyeColor.ORANGE);
        chrLs.put(NamedTextColor.GOLD, '6');//wealth
        clrLs.put(NamedTextColor.GOLD, Color.ORANGE);
        dyeLs.put(NamedTextColor.GRAY, DyeColor.LIGHT_GRAY);
        chrLs.put(NamedTextColor.GRAY, '7');//plain
        clrLs.put(NamedTextColor.GRAY, Color.SILVER);
        dyeLs.put(NamedTextColor.DARK_GRAY, DyeColor.GRAY);
        chrLs.put(NamedTextColor.DARK_GRAY, '8');//shadow
        clrLs.put(NamedTextColor.DARK_GRAY, Color.GRAY);
        dyeLs.put(NamedTextColor.BLUE, DyeColor.PURPLE);
        chrLs.put(NamedTextColor.BLUE, '9');//trust
        clrLs.put(NamedTextColor.BLUE, Color.BLUE);
        dyeLs.put(NamedTextColor.GREEN, DyeColor.LIME);
        chrLs.put(NamedTextColor.GREEN, 'a');//balance
        clrLs.put(NamedTextColor.GREEN, Color.LIME);
        dyeLs.put(NamedTextColor.AQUA, DyeColor.LIGHT_BLUE);
        chrLs.put(NamedTextColor.AQUA, 'b');//spirit
        clrLs.put(NamedTextColor.AQUA, Color.AQUA);
        dyeLs.put(NamedTextColor.RED, DyeColor.RED);
        chrLs.put(NamedTextColor.RED, 'c');//health
        clrLs.put(NamedTextColor.RED, Color.RED);
        dyeLs.put(NamedTextColor.LIGHT_PURPLE, DyeColor.PINK);
        chrLs.put(NamedTextColor.LIGHT_PURPLE, 'd');//magic
        clrLs.put(NamedTextColor.LIGHT_PURPLE, Color.FUCHSIA);
        dyeLs.put(NamedTextColor.YELLOW, DyeColor.YELLOW);
        chrLs.put(NamedTextColor.YELLOW, 'e');//hope
        clrLs.put(NamedTextColor.YELLOW, Color.YELLOW);
        dyeLs.put(NamedTextColor.WHITE, DyeColor.WHITE);
        chrLs.put(NamedTextColor.WHITE, 'f');//confidence
        clrLs.put(NamedTextColor.WHITE, Color.WHITE);
        chrLs.put(CustomTextColor.OLIVE, 'о');//peace
        clrLs.put(CustomTextColor.OLIVE, Color.OLIVE);
        chrLs.put(CustomTextColor.AMBER, 'я');//strength
        clrLs.put(CustomTextColor.AMBER, Color.fromRGB(CustomTextColor.AMBER.value()));
        chrLs.put(CustomTextColor.APPLE, 'с');//growth
        clrLs.put(CustomTextColor.APPLE, Color.fromRGB(CustomTextColor.APPLE.value()));
        chrLs.put(CustomTextColor.BEIGE, 'б');//comfort
        clrLs.put(CustomTextColor.BEIGE, Color.fromRGB(CustomTextColor.BEIGE.value()));
        chrLs.put(CustomTextColor.CARDINAL, 'к');//passion
        clrLs.put(CustomTextColor.CARDINAL, Color.fromRGB(CustomTextColor.CARDINAL.value()));
        chrLs.put(CustomTextColor.INDIGO, 'ф');//energy
        clrLs.put(CustomTextColor.INDIGO, Color.fromRGB(CustomTextColor.INDIGO.value()));
        chrLs.put(CustomTextColor.PINK, 'р');//love
        clrLs.put(CustomTextColor.PINK, Color.fromRGB(CustomTextColor.PINK.value()));
        chrLs.put(CustomTextColor.SKY, 'н');//calm
        clrLs.put(CustomTextColor.SKY, Color.fromRGB(CustomTextColor.SKY.value()));
        chrLs.put(CustomTextColor.STALE, 'ч');//future
        clrLs.put(CustomTextColor.STALE, Color.fromRGB(CustomTextColor.STALE.value()));
        chrLs.put(CustomTextColor.MITHRIL, 'м');//durability
        clrLs.put(CustomTextColor.MITHRIL, Color.fromRGB(CustomTextColor.MITHRIL.value()));
        dyeIx = Index.create(tc -> dyeLs.get(tc), dyeLs.keySet().stream().toList());
        chrIx = Index.create(tc -> chrLs.get(tc), chrLs.keySet().stream().toList());
        chrSet = chrIx.keyToValue().entrySet();
        clrIx = Index.create(tc -> clrLs.get(tc), clrLs.keySet().stream().toList());
        tcSize = chrLs.size();

        TagResolver.Builder trb = TagResolver.builder();
        trb = trb.resolver(StandardTags.defaults());
        for (final Entry<String, CustomTextColor> en : CustomTextColor.VALUES.entrySet()) {
            @Subst("")
            final String key = en.getKey();
            trb = trb.resolver(TagResolver.resolver(key,
                Tag.styling(TextColor.color(en.getValue().value()))));
        }
        msg = MiniMessage.builder().tags(trb.build()).build();
        /*msg = MiniMessage.builder().tags(
            trb
                .resolver(StandardTags.defaults())
                .resolver(TagResolver.resolver("amber", Tag.styling(TextColor.color(0xCC8822))))//Янтарный
                .resolver(TagResolver.resolver("apple", Tag.styling(TextColor.color(0x88BB44))))//Салатовый
                .resolver(TagResolver.resolver("beige", Tag.styling(TextColor.color(0xDDCCAA))))//Бежевый
                .resolver(TagResolver.resolver("maroon", Tag.styling(TextColor.color(0xBB2244))))//Кардинный
                .resolver(TagResolver.resolver("indigo", Tag.styling(TextColor.color(0xAAAADD))))//Сиреневый
                .resolver(TagResolver.resolver("olive", Tag.styling(TextColor.color(0xBBDDAA))))//Оливковый
                .resolver(TagResolver.resolver("pink", Tag.styling(TextColor.color(0xDDAABB))))//Малиновый
                .resolver(TagResolver.resolver("sky", Tag.styling(TextColor.color(0xAADDDD))))//Небесный
                .resolver(TagResolver.resolver("stale", Tag.styling(TextColor.color(0x446666))))//Черствый
                .resolver(TagResolver.resolver("mithril", Tag.styling(TextColor.color(0xB0C0C0))))//Мифриловый
                .build()).build();*/
    }

    public static ItemStack changeColor(ItemStack source, byte new_color) {
        DyeColor dc;
        switch (new_color) {
            case 0 -> dc = DyeColor.BLACK;
            case 1 -> dc = DyeColor.BLUE;
            case 2 -> dc = DyeColor.GREEN;
            case 3 -> dc = DyeColor.ORANGE;
            case 4 -> dc = DyeColor.RED;
            case 5 -> dc = DyeColor.PURPLE;
            case 6 -> dc = DyeColor.BROWN;
            case 7 -> dc = DyeColor.LIGHT_GRAY;
            case 8 -> dc = DyeColor.GRAY;
            case 9 -> dc = DyeColor.LIGHT_BLUE;
            case 10 -> dc = DyeColor.LIME;
            case 11 -> dc = DyeColor.CYAN;
            case 12 -> dc = DyeColor.PINK;
            case 13 -> dc = DyeColor.MAGENTA;
            case 14 -> dc = DyeColor.YELLOW;
            default -> dc = DyeColor.WHITE;
        }
        return changeColor(source, dc);
    }

    public static ItemStack changeColor(final ItemStack source, final DyeColor color) {
        if (source == null || color == null) return source;
        final ItemStack it;
        if (source.getType().isBlock()) {
            String matName = source.getType().name();
            String stripMatName = stripMaterialName(matName);
            if (matName.length() == stripMatName.length()) {
                return source;//(base_mat_name.isEmpty()) {
            }                //return source;
            //}
            final Material newMat = Material.matchMaterial(color.name() + "_" + stripMatName);
            it = newMat == null ? source : source.withType(newMat);
        } else {
            final ItemMeta im = source.getItemMeta();
            if (im instanceof final Colorable c) {
                c.setColor(color);
                source.setItemMeta(im);
            }
            it = source;
        }
        return it;
    }

    @Deprecated
    public static Material changeColor(final Material source, final DyeColor color) {
        if (source == null) {
            return Material.BEDROCK; //заглушки от NullPoint  в плагинах
        }
        if (color == null) {
            return source; //заглушки от NullPoint  в плагинах
        }
        final String stripName = stripMaterialName(source.name());
        final Material newMat = Material.matchMaterial(color.name() + "_" + stripName);
        return newMat == null ? source : newMat;
    }

    public static ItemType changeColor(final ItemType source, final DyeColor color) {
        if (source == null) return ItemType.BEDROCK; //заглушки от NullPoint  в плагинах
        if (color == null) return source; //заглушки от NullPoint  в плагинах
        final String stripName = stripMaterialName(source.key().value());
        final ItemType newMat = Ostrov.registries.ITEMS.get(Key
            .key(color.name().toLowerCase() + "_" + stripName));
        return newMat == null ? source : newMat;
    }

    public static boolean canChangeColor(final Material mat) {
        if (mat == null) {
            return false; //заглушки от NullPoint  в плагинах
        }
        return stripMaterialName(mat.name()).length() != mat.name().length();
    }

    public static String stripMaterialName(final String materialName) {
        if (materialName == null) {
            return Material.BEDROCK.name(); //заглушки от NullPoint  в плагинах
        }
        final String clr = materialName.split("_")[0].toLowerCase();
        return switch (clr) {
            case "red", "magenta", "orange", "purple", "yellow", "black", "brown", "white", "green",
                 "blue", "cyan", "gray", "lime", "pink" -> materialName.substring(clr.length() + 1);
            case "light" -> materialName.substring(11);  // "light_blue", "light_gray"
            default -> materialName;
        };
    }

    public static String nameOf(final TextColor color, final String end, final boolean clrz) {
        return nameOf(toChar(color), end, clrz);
    }

    public static String nameOf(final char color, final String end, final boolean clrz) {
        final String cnm = switch (color) {
            case '0' -> "Черн";
            case '1' -> "Темно-Лазурн";
            case '2' -> "Зелен";
            case '3' -> "Бирюзов";
            case '4' -> "Бардов";
            case '5' -> "Пурпурн";
            case '6' -> "Золот";
            case '7' -> "Сер";
            case '8' -> "Темно-Сер";
            case '9' -> "Лазурн";
            case 'a' -> "Лаймов";
            case 'b' -> "Голуб";
            case 'c' -> "Красн";
            case 'd' -> "Розов";
            case 'e' -> "Желт";
            case 'я' -> "Янтарн";
            case 'с' -> "Салатов";
            case 'б' -> "Бежев";
            case 'к' -> "Кардинн";
            case 'ф' -> "Сиренев";
            case 'о' -> "Оливков";
            case 'р' -> "Малинов";
            case 'н' -> "Небесн";
            case 'ч' -> "Черств";
            case 'м' -> "Мифрилов";
            default -> "Бел";
        };
        return (clrz ? "§" + color + cnm : cnm) + end;
    }

    public static DyeColor randomDyeColor() {
        return switch (NumUtils.randInt(0, 16)) {
            case 0 -> DyeColor.BLACK;
            case 1 -> DyeColor.BLUE;
            case 2 -> DyeColor.BROWN;
            case 3 -> DyeColor.CYAN;
            case 4 -> DyeColor.GRAY;
            case 5 -> DyeColor.GREEN;
            case 6 -> DyeColor.LIGHT_BLUE;
            case 7 -> DyeColor.LIGHT_GRAY;
            case 8 -> DyeColor.LIME;
            case 9 -> DyeColor.MAGENTA;
            case 10 -> DyeColor.ORANGE;
            case 11 -> DyeColor.PINK;
            case 12 -> DyeColor.PURPLE;
            case 13 -> DyeColor.RED;
            case 14 -> DyeColor.YELLOW;
            default -> DyeColor.WHITE;
        };
    }

    public static Color randomCol() {
        return switch (NumUtils.randInt(0, 16)) {
            case 0 -> Color.AQUA;
            case 1 -> Color.BLACK;
            case 2 -> Color.BLUE;
            case 3 -> Color.FUCHSIA;
            case 4 -> Color.GRAY;
            case 5 -> Color.GREEN;
            case 6 -> Color.LIME;
            case 7 -> Color.MAROON;
            case 8 -> Color.NAVY;
            case 9 -> Color.OLIVE;
            case 10 -> Color.ORANGE;
            case 11 -> Color.PURPLE;
            case 12 -> Color.RED;
            case 13 -> Color.SILVER;
            case 14 -> Color.TEAL;
            case 15 -> Color.YELLOW;
            default -> Color.WHITE;
        };
    }

    public static String randomColor() {
        return randomColor(false);
    }

    public static String randomColor(final boolean extra) {
        return getColor(NumUtils.randInt(0, extra ? tcSize : 16));
    }

    public static String getColor(final int col) {
        return switch (col) {
            case 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 -> "§" + col;
            case 10 -> "§a";
            case 11 -> "§b";
            case 12 -> "§c";
            case 13 -> "§d";
            case 14 -> "§e";
            case 16 -> "§я";
            case 17 -> "§н";
            case 18 -> "§б";
            case 19 -> "§р";
            case 20 -> "§о";
            case 21 -> "§ф";
            case 22 -> "§с";
            case 23 -> "§к";
            case 24 -> "§ч";
            case 25 -> "§м";
            default -> "§f";
        };
    }

    public static char[] getColors(final boolean extra) {
        return extra ? new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'я', 'н', 'б', 'р', 'о', 'ф', 'с', 'к', 'ч', 'м'}
            : new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    }

    public static String dyeDisplayName(final DyeColor dyecolor) {
        return nameOf(getTextColor(dyecolor), "ый", true);
    }

    public static String toChat(final DyeColor dyecolor) {
        return switch (dyecolor) {
            case WHITE -> "§f";    //+++бел
            case ORANGE -> "§6";
            case PURPLE -> "§5";
            case LIGHT_BLUE -> "§b";
            case YELLOW -> "§e";
            case LIME -> "§a";
            case PINK -> "§d";
            case GRAY -> "§8";
            case LIGHT_GRAY -> "§7";
            case CYAN -> "§3";
            case MAGENTA -> "§9";
            case BLUE -> "§1";
            case BROWN -> "§4";
            case GREEN -> "§2";
            case RED -> "§c";
            case BLACK -> "§0";
        };
    }

    /*@Deprecated
    public static String strip(final String str) {
        if (str.isEmpty()) {
            return "";
        }

        final char[] chMsg = str.toCharArray();
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i != chMsg.length; i++) {
            if (chMsg[i] == STYLE) {//начало стиля

                if (i + 1 == chMsg.length) {
                    continue;
                }
                i++;

                final char ch = chMsg[i];
                final int cend;
                switch (ch) {//форматы стиля
                    case HEX://хекс
                        cend = i + 7;//#000000
                        if (cend > chMsg.length) {
                            continue;
                        }
                        while (true) {//6 -> 1 хекс код
                            if (i + 1 == cend) {
                                break;
                            } else {
                                i++;
                            }
                            final int dg = Character.digit(chMsg[i], 16);
                            if (dg == -1) {//символ не хекс
                                i = cend - 7;
                                break;
                            }
                        }

                        if (i + 1 == chMsg.length) {
                            continue;
                        }
                        if (chMsg[i + 1] == GRAD) {
                            i++;
                            if (i + 1 == chMsg.length) {
                                continue;
                            }
                            i++;
                            final char to = chMsg[i];
                            if (to == HEX) {
                                final int gend = i + 7;//#000000
                                if (gend > chMsg.length) {
                                    continue;
                                }
                                while (true) {//6 -> 1 хекс код
                                    if (i + 1 == gend) {
                                        break;
                                    } else {
                                        i++;
                                    }
                                    final int dg = Character.digit(chMsg[i], 16);
                                    if (dg == -1) {//символ не хекс
                                        i = gend - 8;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    case GRAD://градиент
                        if (i + 1 == chMsg.length) {
                            continue;
                        }
                        i++;

                        final char from = chMsg[i];
                        if (from == HEX) {//хекс
                            cend = i + 7;//#000000
                            if (cend > chMsg.length) {
                                continue;
                            }
                            while (true) {//6 -> 1 хекс код
                                if (i + 1 == cend) {
                                    break;
                                } else {
                                    i++;
                                }
                                final int dg = Character.digit(chMsg[i], 16);
                                if (dg == -1) {//символ не хекс
                                    i = cend - 7;
                                    break;
                                }
                            }
                        }
                        break;
                    default:
                        if (chrIx.get(ch) == null || i + 1 == chMsg.length) {
                            continue;
                        }
                        if (chMsg[i + 1] == GRAD) {
                            i++;
                            if (i + 1 == chMsg.length) {
                                continue;
                            }
                            i++;
                            final char to = chMsg[i];
                            if (to == HEX) {
                                final int gend = i + 7;//#000000
                                if (gend > chMsg.length) {
                                    continue;
                                }
                                while (true) {//6 -> 1 хекс код
                                    if (i + 1 == gend) {
                                        break;
                                    } else {
                                        i++;
                                    }
                                    final int dg = Character.digit(chMsg[i], 16);
                                    if (dg == -1) {//символ не хекс
                                        i = gend - 8;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                }
                continue;
            }
            sb.append(chMsg[i]);
        }
        return sb.toString();
    }

    @Deprecated
    public static String strip(final Component cmp) {
        final StringBuilder sb = new StringBuilder();
        if (cmp instanceof TextComponent) {
            sb.append(strip(((TextComponent) cmp).content()));
        }
        for (final Component ch : cmp.children()) {
            sb.append(strip(ch));
        }
        return sb.toString();
    }*/

    public static String strip(final String str) {
        return MiniMessage.miniMessage().stripTags(deLegacify(str));
    }

    public static String strip(final Component cmp) {
        final StringBuilder sb = new StringBuilder();
        if (cmp instanceof TextComponent) {
            sb.append(strip(((TextComponent) cmp).content()));
        }
        for (final Component ch : cmp.children()) {
            sb.append(strip(ch));
        }
        return sb.toString();
    }

    public static String translateAlternateColorCodes(char c, String string) {
        if (c == '§') {
            return string.replace('§', '&');
        } else {
            return string.replace('&', '§');
        }
    }

    public static String setColorChar(final char ch, final String str) {
        return str.replace(ch, '§');
    }

    public static String setColorChar(final char ch, final Component str) {
        return setColorChar(ch, deform(str));
    }

    @Slow(priority = 1)
    private static String deLegacify(final String str) {
        String fin = str;
        for (final Entry<Character, TextColor> en : chrSet) {
            final TextColor tc = en.getValue();
            final String rpl;
            switch (tc) {
                case final NamedTextColor nc:
                    rpl = nc.toString();
                    break;
                case final CustomTextColor cc:
                    rpl = cc.toString();
                    break;
                default:
                    continue;
            }

            fin = fin.replace(STYLE + en.getKey().toString(), "<" + rpl + ">");
        }
        fin = fin.replace(STYLE + "k", "<obf>");
        fin = fin.replace(STYLE + "l", "<b>");
        fin = fin.replace(STYLE + "m", "<st>");
        fin = fin.replace(STYLE + "n", "<u>");
        fin = fin.replace(STYLE + "o", "<i>");
        fin = fin.replace(STYLE + "r", "<r>");
        for (final Entry<String, CustomTextColor> en : CustomTextColor.VALUES.entrySet()) {
            fin = fin.replace(":" + en.getKey(), ":#" + Integer.toHexString(en.getValue().value()));
        }
        return fin;
    }

    @Slow(priority = 2)
    public static Component form(final String str) {
        if (str == null || str.isEmpty()) return EMPTY;
        return msg.deserialize(deLegacify(str)).decorationIfAbsent(
            TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static String deform(final Component cmp) {
        if (cmp == null) return "";
        return msg.serialize(cmp);
    }

    //"§[^\s]"
    /*@Slow(priority = 1)
    @Deprecated
    public static TextComponent format(final String str) {
        if (str == null || str.isEmpty()) {
            return EMPTY;//Component.text("");
        }
        final ArrayList<TextComponent> comps = new ArrayList<>();
        final char[] chMsg = str.toCharArray();

        StringBuilder sb = new StringBuilder();
        TextColor color = null, gradTo = null;
        final LinkedHashMap<TextDecoration, Integer> dec = new LinkedHashMap<>();

        for (int i = 0; i != chMsg.length; i++) {
            if (chMsg[i] == STYLE) {//начало стиля

                if (i + 1 == chMsg.length) {
                    continue;
                }
                i++;

                final char ch = chMsg[i];
                final int cend;
                int val;
                final TextColor tclr;
                switch (ch) {//форматы стиля
                    case 'k':
                        dec.putIfAbsent(TextDecoration.OBFUSCATED, sb.length());
                        break;
                    case 'l':
                        dec.putIfAbsent(TextDecoration.BOLD, sb.length());
                        break;
                    case 'm':
                        dec.putIfAbsent(TextDecoration.STRIKETHROUGH, sb.length());
                        break;
                    case 'n':
                        dec.putIfAbsent(TextDecoration.UNDERLINED, sb.length());
                        break;
                    case 'o':
                        dec.putIfAbsent(TextDecoration.ITALIC, sb.length());
                        break;
                    case 'r'://reset
                        buildCmp(sb, color, gradTo, dec, comps);//предыдущий стиль
                        sb = new StringBuilder();
                        color = null;
                        gradTo = null;
                        dec.clear();
                        break;
                    case HEX://хекс
                        cend = i + 7;//#000000
                        if (cend > chMsg.length) {
                            continue;
                        }
                        val = 0;//10чная версия хекса
                        while (true) {//1 -> 6 хекс код
                            if (i + 1 == cend) {
                                break;
                            } else {
                                i++;
                            }
                            final int dg = Character.digit(chMsg[i], 16);
                            if (dg == -1) {//символ не хекс
                                val = -1;
                                i = cend - 7;
                                break;
                            }
                            val += dg << (4 * (cend - i - 1));//хекс код ставит в позицию
                        }

                        if (val != -1) {
                            buildCmp(sb, color, gradTo, dec, comps);//предыдущий стиль
                            sb = new StringBuilder();
                            if (gradTo != null) {
                                color = gradTo;
                            }
                            if (color == null || val != color.value()) {
                                color = TextColor.color(val);
                            }
                            gradTo = null;
                            dec.clear();

                            if (i + 1 == chMsg.length) {
                                continue;
                            }
                            if (chMsg[i + 1] == GRAD) {//градиент
                                i++;
                                if (i + 1 == chMsg.length) {
                                    continue;
                                }
                                i++;
                                final char to = chMsg[i];
                                if (to == HEX) {
                                    final int gend = i + 7;//#000000
                                    if (gend > chMsg.length) {
                                        continue;
                                    }
                                    int eval = 0;//10чная версия хекса
                                    while (true) {//1 -> 6 хекс код
                                        if (i + 1 == gend) {
                                            break;
                                        } else {
                                            i++;
                                        }
                                        final int dg = Character.digit(chMsg[i], 16);
                                        if (dg == -1) {//символ не хекс
                                            eval = -1;
                                            i = gend - 7;
                                            break;
                                        }
                                        eval += dg << (4 * (gend - i - 1));//хекс код ставит в позицию
                                    }

                                    if (eval != -1) {
                                        gradTo = TextColor.color(eval);
                                    }
                                } else {
                                    final TextColor toc = chrIx.get(to);
                                    if (toc != null) {
                                        gradTo = toc;
                                    }
                                }
                            }
                        }
                        break;
                    case GRAD://простой градиент
                        if (i + 1 == chMsg.length) {
                            continue;
                        }
                        i++;

                        final char from = chMsg[i];
                        if (from == HEX) {//хекс
                            cend = i + 7;//#000000
                            if (cend > chMsg.length) {
                                continue;
                            }
                            val = 0;//10чная версия хекса
                            while (true) {//1 -> 6 хекс код
                                if (i + 1 == cend) {
                                    break;
                                } else {
                                    i++;
                                }
                                final int dg = Character.digit(chMsg[i], 16);
                                if (dg == -1) {//символ не хекс
                                    val = -1;
                                    i = cend - 7;
                                    break;
                                }
                                val += dg << (4 * (cend - i - 1));//хекс код ставит в позицию
                            }

                            if (val == -1) {
                                continue;
                            }
                            buildCmp(sb, color, gradTo, dec, comps);//предыдущий стиль
                            sb = new StringBuilder();
                            if (gradTo != null) {
                                color = gradTo;
                            }
                            gradTo = TextColor.color(val);
                        } else {
                            tclr = chrIx.get(from);
                            if (tclr == null) {
                                continue;
                            }
                            buildCmp(sb, color, gradTo, dec, comps);//предыдущий стиль
                            sb = new StringBuilder();
                            if (gradTo != null) {
                                color = gradTo;
                            }
                            gradTo = tclr;

                        }

                        if (color == null) {
                            color = gradTo;
                            gradTo = null;
                        } else if (gradTo.value() == color.value()) {
                            gradTo = null;
                        }

                        dec.clear();
                        break;
                    default://цвет
                        tclr = chrIx.get(ch);
                        if (tclr != null) {
                            buildCmp(sb, color, gradTo, dec, comps);//предыдущий стиль
                            sb = new StringBuilder();
                            if (gradTo != null) {
                                color = gradTo;
                            }
                            if (color == null || tclr.value() != color.value()) {
                                color = tclr;
                            }
                            gradTo = null;
                            dec.clear();

                            if (i + 1 == chMsg.length) {
                                continue;
                            }
                            if (chMsg[i + 1] == GRAD) {//градиент
                                i++;
                                if (i + 1 == chMsg.length) {
                                    continue;
                                }
                                i++;
                                final char to = chMsg[i];
                                if (to == HEX) {
                                    final int gend = i + 7;//#000000
                                    if (gend > chMsg.length) {
                                        continue;
                                    }
                                    int eval = 0;//10чная версия хекса
                                    while (true) {//1 -> 6 хекс код
                                        if (i + 1 == gend) {
                                            break;
                                        } else {
                                            i++;
                                        }
                                        final int dg = Character.digit(chMsg[i], 16);
                                        if (dg == -1) {//символ не хекс
                                            eval = -1;
                                            i = gend - 7;
                                            break;
                                        }
                                        eval += dg << (4 * (gend - i - 1));//хекс код ставит в позицию
                                    }

                                    if (eval != -1) {
                                        gradTo = TextColor.color(eval);
                                    }
                                } else {
                                    final TextColor toc = chrIx.get(to);
                                    if (toc != null) {
                                        gradTo = toc;
                                    }
                                }
                            }
                        }
                        break;
                }
                continue;
            }
            sb.append(chMsg[i]);
        }

        buildCmp(sb, color, gradTo, dec, comps);//последний стиль
        return Component.text().append(comps).build();
    }

    private static void buildCmp(final StringBuilder sb, final TextColor color, final TextColor gradTo,
                                 final LinkedHashMap<TextDecoration, Integer> dec, final ArrayList<TextComponent> comps) {
        if (!sb.isEmpty()) {
            final Style.Builder stb = Style.style().decoration(TextDecoration.ITALIC, false);
            if (gradTo == null || sb.length() == 1) {
                final EnumSet<TextDecoration> decs = EnumSet.noneOf(TextDecoration.class);
                int last = 0;
                if (!dec.isEmpty()) {
                    for (final Entry<TextDecoration, Integer> en : dec.entrySet()) {
                        final int end = en.getValue();
                        if (last != end) {
                            comps.add(Component.text(sb.substring(last, end),
                                stb.decorations(decs, true).color(color).build()));
                            last = end;
                        }
                        decs.add(en.getKey());
                    }
                }

                if (last != sb.length()) {
                    comps.add(Component.text(sb.substring(last, sb.length()),
                        stb.decorations(decs, true).color(color).build()));
                }
            } else if (color != null) {
                final EnumSet<TextDecoration> decs = EnumSet.noneOf(TextDecoration.class);
                int ln = sb.length() - 1, ir = color.red(), ig = color.green(), ib = color.blue(),
                    dr = (gradTo.red() - ir) / ln, dg = (gradTo.green() - ig) / ln, db = (gradTo.blue() - ib) / ln;
                final char[] car = sb.toString().toCharArray();
                if (dec.isEmpty()) {
                    for (int ci = 0; ci != ln; ci++) {
                        comps.add(Component.text(car[ci], stb.color(TextColor.color(ir, ig, ib)).build()));
                        ir += dr;
                        ig += dg;
                        ib += db;
                    }
                } else {
                    final Iterator<Entry<TextDecoration, Integer>> it = dec.entrySet().iterator();
                    Entry<TextDecoration, Integer> nextDec = it.next();
                    int nxt = nextDec.getValue();
                    for (int ci = 0; ci != ln; ci++) {
                        while (nxt == ci) {
                            decs.add(nextDec.getKey());
                            if (!it.hasNext()) {
                                break;
                            }
                            nextDec = it.next();
                            nxt = nextDec.getValue();
                        }
                        comps.add(Component.text(car[ci], stb.decorations(decs, true)
                            .color(TextColor.color(ir, ig, ib)).build()));
                        ir += dr;
                        ig += dg;
                        ib += db;
                    }

                    while (true) {
                        decs.add(nextDec.getKey());
                        if (!it.hasNext()) {
                            break;
                        }
                        nextDec = it.next();
                    }
                }
                comps.add(Component.text(car[ln], stb.decorations(decs, true).color(gradTo).build()));
            }
        }
    }*/

    public static boolean has(final Component parent, final Component has) {
        return parent.contains(has);
    }

    //@Slow(priority = 1)
    @Deprecated

    //нужен чтобы получить текст с цветами по старинке с §. Кое-где цвета в виде <цвет> не прокатывают и неудобно

    //мы вроде договорились что переходим на <> цвета, ибо с ними понятно, особенно с кастомными цветами и градиентами
    //через них работает MiniMessage через который все это строится
    //и еще, вот методы на Component -> String надо в идеале использовать ТОЛЬКО для сравнения друх
    //компонентов, как например имена предметов. саму String форму мы в игре вообще видеть не должны

    public static String toString(final Component cmp) {
        lstClr = null;
        //gradient = null;
        final StringBuilder sb = new StringBuilder();
        return toString(cmp, sb, EnumSet.noneOf(TextDecoration.class), true);
    }

    private static TextColor lstClr;
    //private static Gradient gradient;

    private static String toString(final Component comp, final StringBuilder sb, final EnumSet<TextDecoration> decor, final boolean parent) {
        if (comp == null) {
            return "";
        }

        final TextColor color = comp.color();
//Bukkit.broadcast(Component.text("tc-" + tc.value()));
        if (comp instanceof TextComponent) {
            final String cnt = ((TextComponent) comp).content();
            if (!cnt.isEmpty()) {
                if (comp.hasStyling()) {
                    final Style stl = comp.style();
                    for (final TextDecoration td : decor) {
                        if (!stl.hasDecoration(td)) {
                            decor.clear();
                            break;
                        }
                    }

                    if (color != null) {
                        final String clr = toString(color);
                        if (cnt.length() == 1) {//>1 char
                            //if (gradient == null) {//no gradient
                            //    gradient = new Gradient(color, sb.length(),
                            //        lstClr != null && lstClr.value() == color.value());
                            // }
                        } else {//stop gradient - >2 chars
                            //if (gradient == null) {//no gradient
//                            Bukkit.broadcast(Component.text(cnt + ", " + lstClr));
                                if (lstClr == null || lstClr.value() != color.value()) {
                                    sb.append("§").append(clr);
                                    decor.clear();
                                }
                            //} else {//gradient
                            //   if (lstClr == null || gradient.init.value() == lstClr.value()) {
                            //        sb.insert(gradient.start, "§" + toString(gradient.init));
                            //   } else {
                            //        sb.insert(gradient.start, gradient.ext ? "§" + GRAD + toString(lstClr)
                            //           : "§" + toString(gradient.init) + GRAD + toString(lstClr));
                            //   }
                            //   gradient = null;
                            //   sb.append("§").append(clr);
                            //    decor.clear();
                            //}
                        }
                    } else if (lstClr != null) {
                        sb.append("§r");
                        decor.clear();

                        //if (gradient != null) {//stop gradient - no color
                        //    if (lstClr == null || gradient.init.value() == lstClr.value()) {
                        //         sb.insert(gradient.start, "§" + toString(gradient.init));
                        //     } else {
                        //        sb.insert(gradient.start, gradient.ext ? "§" + GRAD + toString(lstClr)
                        //            : "§" + toString(gradient.init) + GRAD + toString(lstClr));
                        //    }
                        //    gradient = null;
                        //}
                    }

                    for (final Entry<TextDecoration, TextDecoration.State> en : stl.decorations().entrySet()) {
                        if (en.getValue() == TextDecoration.State.TRUE && decor.add(en.getKey())) {
                            final char dc = switch (en.getKey()) {
                                case BOLD -> 'l';
                                case OBFUSCATED -> 'k';
                                case STRIKETHROUGH -> 'm';
                                case UNDERLINED -> 'n';
                                case ITALIC -> 'o';
                            };
                            sb.append("§").append(dc);
                        }
                    }
                }
                sb.append(cnt);
            }
            lstClr = color;
        }

        final List<Component> cls = comp.children();
        if (!cls.isEmpty()) {
            for (final Component cm : cls) {
                toString(cm, sb, decor, false);
            }
        }

        //if (gradient != null && parent) {//stop gradient - end
        //    if (lstClr == null || gradient.init.value() == lstClr.value()) {
        //        sb.insert(gradient.start, "§" + toString(gradient.init));
        //    } else {
        //        sb.insert(gradient.start, gradient.ext ? "§" + GRAD + toString(lstClr)
        //            : "§" + toString(gradient.init) + GRAD + toString(lstClr));
        //    }
        //    gradient = null;
        //}
        return sb.toString();
    }

    public static String toString(final TextColor color) {
        if (color instanceof NamedTextColor) {
            return String.valueOf(toChar(color));
        }
        final CustomTextColor ctc = CustomTextColor.intClr.get(color.value());
        if (ctc != null) {
            return String.valueOf(toChar(ctc));
        }
        return color.asHexString().toUpperCase();
    }

    // private record Gradient(TextColor init, int start, boolean ext) {}

    public static boolean compare(final Component of, final Component to) {
        ////defom делает какое-то месиво вроде <!italic><!underlined><!strikethrough><!bold><!obfuscated><red>
        ////имя не сравнивает нормально,посыпались предметы меню на минииграх
        //пока такой фикс
        //return strip(deform(of)).equals(strip(deform(to)));
        //return deform(of).equals(deform(to));
        return GameApi.fromComponent(of).equals(GameApi.fromComponent(to));
    }

    //надо для скайблока
    public static TextColor getTextColor(final int col) {
        return getTextColor(getColor(col).charAt(1));
    }

    public static TextColor getTextColor(final String s) {
        return chrIx.valueOr(s.isEmpty() ? 'f' : (s.length() == 1 ? s.charAt(0) : s.charAt(1)), NamedTextColor.WHITE);
    }

    public static TextColor getTextColor(final DyeColor clr) {
        return dyeIx.valueOr(clr, NamedTextColor.WHITE);
    }

    public static TextColor getTextColor(final Color clr) {
        return clrIx.valueOr(clr, NamedTextColor.WHITE);
    }

    public static TextColor getTextColor(final char c) {
        return chrIx.valueOr(c, NamedTextColor.WHITE);
    }

    public static Character toChar(final TextColor clr) {
        return chrIx.keyOr(clr, 'f');
    }

    public static DyeColor getDyeColor(final TextColor clr) {
        return dyeIx.keyOr(clr, DyeColor.WHITE);
    }

    public static Color getBukkitColor(final TextColor clr) {
        return clrIx.keyOr(clr, Color.WHITE);
    }


    @Slow(priority = 1)
    public static String toLegacy(@Nullable String val) {
        if (val == null || val.isEmpty()) return val;
        for (final CustomTextColor ctc : CustomTextColor.values()) {
            val = val.replace(String.valueOf(STYLE) + chrIx.keyOr(ctc, 'f'),
                "<" + ctc.toString() + ">").replace(ctc.toString(), ctc.like());
        }
        for (final NamedTextColor ntc : NamedTextColor.NAMES.values()) {
            val = val.replace("<" + ntc.toString() + ">",
                String.valueOf(STYLE) + chrIx.keyOr(ntc, 'f'));
        }
        val = val.replace("<obf>", STYLE + "k");
        val = val.replace("<b>", STYLE + "l");
        val = val.replace("<st>", STYLE + "m");
        val = val.replace("<u>", STYLE + "n");
        val = val.replace("<i>", STYLE + "o");
        val = val.replace("<r>", STYLE + "r");
        return val;
    }






    public static Color getBukkitColor(final String s) {
        return switch (s.toUpperCase()) {
            case "AQUA" -> Color.AQUA;
            case "BLUE" -> Color.BLUE;
            case "FUCHSIA" -> Color.FUCHSIA;
            case "GRAY" -> Color.GRAY;
            case "GREEN" -> Color.GREEN;
            case "LIME" -> Color.LIME;
            case "MAROON" -> Color.MAROON;
            case "NAVY" -> Color.NAVY;
            case "OLIVE" -> Color.OLIVE;
            case "ORANGE" -> Color.ORANGE;
            case "PURPLE" -> Color.PURPLE;
            case "RED" -> Color.RED;
            case "SILVER" -> Color.SILVER;
            case "TEAL" -> Color.TEAL;
            case "WHITE" -> Color.WHITE;
            case "YELLOW" -> Color.YELLOW;
            default -> Color.BLACK;
        };
    }

    public static int toByte(final TextColor color) {
        return toByte(toChar(color));
    }

    public static int toByte(final char color) {
        return switch (color) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> color - 48;
            case 'a' -> 10;
            case 'b' -> 11;
            case 'c' -> 12;
            case 'd' -> 13;
            case 'e' -> 14;
            case 'я' -> 16;
            case 'с' -> 17;
            case 'б' -> 18;
            case 'к' -> 19;
            case 'ф' -> 20;
            case 'о' -> 21;
            case 'р' -> 22;
            case 'н' -> 23;
            case 'ч' -> 24;
            case 'м' -> 25;
            default -> 15;
        };
    }

    @Deprecated
    public static Character getColorChar(final TextColor clr) {
        return toChar(clr);
    }

    @Deprecated
    public static TextColor getColorDye(final DyeColor clr) {
        return getTextColor(clr);
    }

    @Deprecated
    public static TextColor getCharColor(final char c) {
        return chrIx.valueOr(c, NamedTextColor.WHITE);
    }

    @Deprecated
    public static int toByte(final NamedTextColor color) {
        return toByte((TextColor) color);
    }

  //@Deprecated нужны на островах. Работает и вроде не мешает
    public static String toChat(final TextColor color) {
        return "§" + toChar(color).toString();
    }

  //@Deprecated нужны на островах. Работает и вроде не мешает
    public static String toChat(final NamedTextColor color) {
        return "§" + toChar(color).toString();
    }

    @Deprecated
    public static DyeColor getDyeColor(final NamedTextColor color) {
        return getDyeColor((TextColor) color);
    }

    @Deprecated //вроде только в кланах иногда юзается в разных плагинах
    // - Возвращать будет не всегда NamedTextColor, ибо CustomTextColor тоже может быть, так что надо смотреть getTextColor()
    public static NamedTextColor chatColorFromString(final String s) {
        final TextColor tc = getTextColor(s);
        return tc instanceof NamedTextColor
            //? (NamedTextColor) tc : NamedTextColor.WHITE;
            ? (NamedTextColor) tc : NamedTextColor.nearestTo(tc);
    }

}