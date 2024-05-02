package ru.komiss77.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Colorable;
import ru.komiss77.ApiOstrov;
import ru.komiss77.notes.Slow;

import java.util.*;
import java.util.Map.Entry;

public class TCUtils {

//    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9a-xа-я]");
    public static final TextComponent EMPTY;

    private static final BiMap<DyeColor, TextColor> dyeIx;
    private static final BiMap<Character, TextColor> chrIx;
    private static final BiMap<Color, TextColor> clrIx;

    public static final char STYLE = '§';
//    public static final char form = '᨟';
    public static final char HEX = '#';
    public static final char GRAD = '|';

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
        dyeIx = HashBiMap.create();
        chrIx = HashBiMap.create();
        clrIx = HashBiMap.create();
        dyeIx.put(DyeColor.BLACK, NamedTextColor.BLACK);
        chrIx.put('0', NamedTextColor.BLACK);//void
        clrIx.put(Color.BLACK, NamedTextColor.BLACK);
        dyeIx.put(DyeColor.BLUE, NamedTextColor.DARK_BLUE);
        chrIx.put('1', NamedTextColor.DARK_BLUE);//adventure
        clrIx.put(Color.NAVY, NamedTextColor.DARK_BLUE);
        dyeIx.put(DyeColor.GREEN, NamedTextColor.DARK_GREEN);
        chrIx.put('2', NamedTextColor.DARK_GREEN);//nature
        clrIx.put(Color.GREEN, NamedTextColor.DARK_GREEN);
        dyeIx.put(DyeColor.CYAN, NamedTextColor.DARK_AQUA);
        chrIx.put('3', NamedTextColor.DARK_AQUA);//wisdom
        clrIx.put(Color.TEAL, NamedTextColor.DARK_AQUA);
        dyeIx.put(DyeColor.BROWN, NamedTextColor.DARK_RED);
        chrIx.put('4', NamedTextColor.DARK_RED);//war
        clrIx.put(Color.MAROON, NamedTextColor.DARK_RED);
        dyeIx.put(DyeColor.MAGENTA, NamedTextColor.DARK_PURPLE);
        chrIx.put('5', NamedTextColor.DARK_PURPLE);//royalty
        clrIx.put(Color.PURPLE, NamedTextColor.DARK_PURPLE);
        dyeIx.put(DyeColor.ORANGE, NamedTextColor.GOLD);
        chrIx.put('6', NamedTextColor.GOLD);//wealth
        clrIx.put(Color.ORANGE, NamedTextColor.GOLD);
        dyeIx.put(DyeColor.LIGHT_GRAY, NamedTextColor.GRAY);
        chrIx.put('7', NamedTextColor.GRAY);//plain
        clrIx.put(Color.SILVER, NamedTextColor.GRAY);
        dyeIx.put(DyeColor.GRAY, NamedTextColor.DARK_GRAY);
        chrIx.put('8', NamedTextColor.DARK_GRAY);//shadow
        clrIx.put(Color.GRAY, NamedTextColor.DARK_GRAY);
        dyeIx.put(DyeColor.PURPLE, NamedTextColor.BLUE);
        chrIx.put('9', NamedTextColor.BLUE);//trust
        clrIx.put(Color.BLUE, NamedTextColor.BLUE);
        dyeIx.put(DyeColor.LIME, NamedTextColor.GREEN);
        chrIx.put('a', NamedTextColor.GREEN);//balance
        clrIx.put(Color.LIME, NamedTextColor.GREEN);
        dyeIx.put(DyeColor.LIGHT_BLUE, NamedTextColor.AQUA);
        chrIx.put('b', NamedTextColor.AQUA);//spirit
        clrIx.put(Color.AQUA, NamedTextColor.AQUA);
        dyeIx.put(DyeColor.RED, NamedTextColor.RED);
        chrIx.put('c', NamedTextColor.RED);//health
        clrIx.put(Color.RED, NamedTextColor.RED);
        dyeIx.put(DyeColor.PINK, NamedTextColor.LIGHT_PURPLE);
        chrIx.put('d', NamedTextColor.LIGHT_PURPLE);//magic
        clrIx.put(Color.FUCHSIA, NamedTextColor.LIGHT_PURPLE);
        dyeIx.put(DyeColor.YELLOW, NamedTextColor.YELLOW);
        chrIx.put('e', NamedTextColor.YELLOW);//hope
        clrIx.put(Color.YELLOW, NamedTextColor.YELLOW);
        dyeIx.put(DyeColor.WHITE, NamedTextColor.WHITE);
        chrIx.put('f', NamedTextColor.WHITE);//confidence
        clrIx.put(Color.WHITE, NamedTextColor.WHITE);

        chrIx.put('я', CustomTextColor.AMBER);//strength
        clrIx.put(Color.fromRGB(CustomTextColor.AMBER.value()), CustomTextColor.AMBER);
        chrIx.put('с', CustomTextColor.APPLE);//growth
        clrIx.put(Color.fromRGB(CustomTextColor.APPLE.value()), CustomTextColor.APPLE);
        chrIx.put('б', CustomTextColor.BEIGE);//comfort
        clrIx.put(Color.fromRGB(CustomTextColor.BEIGE.value()), CustomTextColor.BEIGE);
        chrIx.put('к', CustomTextColor.CARDINAL);//passion
        clrIx.put(Color.fromRGB(CustomTextColor.CARDINAL.value()), CustomTextColor.CARDINAL);
        chrIx.put('ф', CustomTextColor.INDIGO);//energy
        clrIx.put(Color.fromRGB(CustomTextColor.INDIGO.value()), CustomTextColor.INDIGO);
        chrIx.put('о', CustomTextColor.OLIVE);//peace
        clrIx.put(Color.OLIVE, CustomTextColor.OLIVE);
        chrIx.put('р', CustomTextColor.ORCHID);//love
        clrIx.put(Color.fromRGB(CustomTextColor.ORCHID.value()), CustomTextColor.ORCHID);
        chrIx.put('н', CustomTextColor.SKYBLUE);//calm
        clrIx.put(Color.fromRGB(CustomTextColor.SKYBLUE.value()), CustomTextColor.SKYBLUE);
        chrIx.put('ч', CustomTextColor.STALE);//future
        clrIx.put(Color.fromRGB(CustomTextColor.STALE.value()), CustomTextColor.STALE);
        chrIx.put('м', CustomTextColor.MITHRIL);//durability
        clrIx.put(Color.fromRGB(CustomTextColor.MITHRIL.value()), CustomTextColor.MITHRIL);
    }

    public static ItemStack changeColor(ItemStack source, byte new_color) {
        DyeColor dc;
        switch (new_color) {
            case 0 ->
                dc = DyeColor.BLACK;
            case 1 ->
                dc = DyeColor.BLUE;
            case 2 ->
                dc = DyeColor.GREEN;
            case 3 ->
                dc = DyeColor.ORANGE;
            case 4 ->
                dc = DyeColor.RED;
            case 5 ->
                dc = DyeColor.PURPLE;
            case 6 ->
                dc = DyeColor.BROWN;
            case 7 ->
                dc = DyeColor.LIGHT_GRAY;
            case 8 ->
                dc = DyeColor.GRAY;
            case 9 ->
                dc = DyeColor.LIGHT_BLUE;
            case 10 ->
                dc = DyeColor.LIME;
            case 11 ->
                dc = DyeColor.CYAN;
            case 12 ->
                dc = DyeColor.PINK;
            case 13 ->
                dc = DyeColor.MAGENTA;
            case 14 ->
                dc = DyeColor.YELLOW;
            default ->
                dc = DyeColor.WHITE;
        }
        return changeColor(source, dc);
    }

    public static ItemStack changeColor(final ItemStack source, final DyeColor color) {
        if (source == null || color == null) {
            return source;
        }
        if (source.getType().isBlock()) {
            String matName = source.getType().name();
            String stripMatName = stripMaterialName(matName);
            if (matName.length() == stripMatName.length()) {
                return source;//(base_mat_name.isEmpty()) {
            }                //return source;
            //}
            final Material newMat = Material.matchMaterial(color.name() + "_" + stripMatName);
            if (newMat != null) {
                source.setType(newMat);//Material.matchMaterial(new_color.toString() + "_" + base_mat_name));
            }
        } else {
            final ItemMeta im = source.getItemMeta();
            if (im instanceof final Colorable c) {
                c.setColor(color);
                source.setItemMeta(im);
            }
        }
        return source;
    }

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

    public static boolean canChangeColor(final Material mat) {
        if (mat == null) {
            return false; //заглушки от NullPoint  в плагинах
        }
        return stripMaterialName(mat.name()).length() != mat.name().length();
    }

    public static String stripMaterialName(String materialName) {
        if (materialName == null) {
            return Material.BEDROCK.name(); //заглушки от NullPoint  в плагинах
        }
        return switch (materialName.split("_")[0]) {
            case "RED" ->
                materialName.substring(4);
            case "BLUE", "CYAN", "GRAY", "LIME", "PINK" ->
                materialName.substring(5);
            case "BLACK", "BROWN", "WHITE", "GREEN" ->
                materialName.substring(6);
            case "ORANGE", "PURPLE", "YELLOW" ->
                materialName.substring(7);
            case "MAGENTA" ->
                materialName.substring(8);
            case "LIGHT" ->
                materialName.substring(11);  // "LIGHT_BLUE", "LIGHT_GRAY"
            default ->
                materialName;
        };
    }

    public static String nameOf(final TextColor color, final String end, final boolean clrz) {
        return nameOf(toChar(color), end, clrz);
    }

    public static String nameOf(final char color, final String end, final boolean clrz) {
        final String cnm = switch (color) {
            case '0' ->
                "Черн";
            case '1' ->
                "Темно-Лазурн";
            case '2' ->
                "Зелен";
            case '3' ->
                "Бирюзов";
            case '4' ->
                "Бардов";
            case '5' ->
                "Пурпурн";
            case '6' ->
                "Золот";
            case '7' ->
                "Сер";
            case '8' ->
                "Темно-Сер";
            case '9' ->
                "Лазурн";
            case 'a' ->
                "Лаймов";
            case 'b' ->
                "Голуб";
            case 'c' ->
                "Красн";
            case 'd' ->
                "Розов";
            case 'e' ->
                "Желт";
            case 'я' ->
                "Янтарн";
            case 'с' ->
                "Салатов";
            case 'б' ->
                "Бежев";
            case 'к' ->
                "Кардинн";
            case 'ф' ->
                "Сиренев";
            case 'о' ->
                "Оливков";
            case 'р' ->
                "Малинов";
            case 'н' ->
                "Небесн";
            case 'ч' ->
                "Черств";
            case 'м' ->
                "Мифрилов";
            default ->
                "Бел";
        };
        return (clrz ? "§" + color + cnm : cnm) + end;
    }

    public static DyeColor randomDyeColor() {
        return switch (ApiOstrov.randInt(0, 16)) {
            case 0 ->
                DyeColor.BLACK;
            case 1 ->
                DyeColor.BLUE;
            case 2 ->
                DyeColor.BROWN;
            case 3 ->
                DyeColor.CYAN;
            case 4 ->
                DyeColor.GRAY;
            case 5 ->
                DyeColor.GREEN;
            case 6 ->
                DyeColor.LIGHT_BLUE;
            case 7 ->
                DyeColor.LIGHT_GRAY;
            case 8 ->
                DyeColor.LIME;
            case 9 ->
                DyeColor.MAGENTA;
            case 10 ->
                DyeColor.ORANGE;
            case 11 ->
                DyeColor.PINK;
            case 12 ->
                DyeColor.PURPLE;
            case 13 ->
                DyeColor.RED;
            case 14 ->
                DyeColor.YELLOW;
            default ->
                DyeColor.WHITE;
        };
    }

    public static Color randomCol() {
        return switch (ApiOstrov.randInt(0, 16)) {
            case 0 ->
                Color.AQUA;
            case 1 ->
                Color.BLACK;
            case 2 ->
                Color.BLUE;
            case 3 ->
                Color.FUCHSIA;
            case 4 ->
                Color.GRAY;
            case 5 ->
                Color.GREEN;
            case 6 ->
                Color.LIME;
            case 7 ->
                Color.MAROON;
            case 8 ->
                Color.NAVY;
            case 9 ->
                Color.OLIVE;
            case 10 ->
                Color.ORANGE;
            case 11 ->
                Color.PURPLE;
            case 12 ->
                Color.RED;
            case 13 ->
                Color.SILVER;
            case 14 ->
                Color.TEAL;
            case 15 ->
                Color.YELLOW;
            default ->
                Color.WHITE;
        };
    }

    public static String randomColor() {
        return randomColor(false);
    }

    public static String randomColor(final boolean extra) {
        return getColor(ApiOstrov.randInt(0, extra ? chrIx.size() : 16));
    }

    public static String getColor(final int col) {
        return switch (col) {
            case 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 ->
                "§" + col;
            case 10 ->
                "§a";
            case 11 ->
                "§b";
            case 12 ->
                "§c";
            case 13 ->
                "§d";
            case 14 ->
                "§e";
            case 16 ->
                "§я";
            case 17 ->
                "§н";
            case 18 ->
                "§б";
            case 19 ->
                "§р";
            case 20 ->
                "§о";
            case 21 ->
                "§ф";
            case 22 ->
                "§с";
            case 23 ->
                "§к";
            case 24 ->
                "§ч";
            case 25 ->
                "§м";
            default ->
                "§f";
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
            case WHITE ->
                "§f";    //+++бел
            case ORANGE ->
                "§6";
            case PURPLE ->
                "§5";
            case LIGHT_BLUE ->
                "§b";
            case YELLOW ->
                "§e";
            case LIME ->
                "§a";
            case PINK ->
                "§d";
            case GRAY ->
                "§8";
            case LIGHT_GRAY ->
                "§7";
            case CYAN ->
                "§3";
            case MAGENTA ->
                "§9";
            case BLUE ->
                "§1";
            case BROWN ->
                "§4";
            case GREEN ->
                "§2";
            case RED ->
                "§c";
            case BLACK ->
                "§0";
        };
    }

    public static String stripColor(final String str) {
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

    public static String stripColor(final Component cmp) {
        final StringBuilder sb = new StringBuilder();
        if (cmp instanceof TextComponent) {
            sb.append(stripColor(((TextComponent) cmp).content()));
        }
        for (final Component ch : cmp.children()) {
            sb.append(stripColor(ch));
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
        return setColorChar(ch, toString(str));
    }

    //"§[^\s]"
    @Slow(priority = 1)
    public static TextComponent format(final String msg) {
        if (msg == null || msg.isEmpty()) {
            return EMPTY;//Component.text("");
        }
        final ArrayList<TextComponent> comps = new ArrayList<>();
        final char[] chMsg = msg.toCharArray();

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
    }

    public static boolean has(final Component parent, final Component has) {
        return parent.contains(has);
    }

    @Slow(priority = 1)
    public static String toString(final Component cmp) {
        lstClr = null;
        gradient = null;
        final StringBuilder sb = new StringBuilder();
        return toString(cmp, sb, EnumSet.noneOf(TextDecoration.class), true);
    }

    private static TextColor lstClr;
    private static Gradient gradient;

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
                            if (gradient == null) {//no gradient
                                gradient = new Gradient(color, sb.length(),
                                        lstClr != null && lstClr.value() == color.value());
                            }
                        } else {//stop gradient - >2 chars
                            if (gradient == null) {//no gradient
//                            Bukkit.broadcast(Component.text(cnt + ", " + lstClr));
                                if (lstClr == null || lstClr.value() != color.value()) {
                                    sb.append("§").append(clr);
                                    decor.clear();
                                }
                            } else {//gradient
                                if (lstClr == null || gradient.init.value() == lstClr.value()) {
                                    sb.insert(gradient.start, "§" + toString(gradient.init));
                                } else {
                                    sb.insert(gradient.start, gradient.ext ? "§" + GRAD + toString(lstClr)
                                        : "§" + toString(gradient.init) + GRAD + toString(lstClr));
                                }
                                gradient = null;
                                sb.append("§").append(clr);
                                decor.clear();
                            }
                        }
                    } else if (lstClr != null) {
                        sb.append("§r");
                        decor.clear();

                        if (gradient != null) {//stop gradient - no color
                            if (lstClr == null || gradient.init.value() == lstClr.value()) {
                                sb.insert(gradient.start, "§" + toString(gradient.init));
                            } else {
                                sb.insert(gradient.start, gradient.ext ? "§" + GRAD + toString(lstClr)
                                    : "§" + toString(gradient.init) + GRAD + toString(lstClr));
                            }
                            gradient = null;
                        }
                    }

                    for (final Entry<TextDecoration, State> en : stl.decorations().entrySet()) {
                        if (en.getValue() == State.TRUE && decor.add(en.getKey())) {
                            final char dc = switch (en.getKey()) {
                                case BOLD ->
                                    'l';
                                case OBFUSCATED ->
                                    'k';
                                case STRIKETHROUGH ->
                                    'm';
                                case UNDERLINED ->
                                    'n';
                                case ITALIC ->
                                    'o';
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

        if (gradient != null && parent) {//stop gradient - end
            if (lstClr == null || gradient.init.value() == lstClr.value()) {
                sb.insert(gradient.start, "§" + toString(gradient.init));
            } else {
                sb.insert(gradient.start, gradient.ext ? "§" + GRAD + toString(lstClr)
                    : "§" + toString(gradient.init) + GRAD + toString(lstClr));
            }
            gradient = null;
        }
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

    private record Gradient(TextColor init, int start, boolean ext) {

    }

    public static boolean compare(final Component of, final Component to) {
        return toString(of).equals(toString(to));
    }

    //надо для скайблока
    public static TextColor getTextColor(final int col) {
        return getTextColor(getColor(col).charAt(1));
    }

    public static TextColor getTextColor(final String s) {
        return chrIx.getOrDefault(s.isEmpty() ? 'f' : (s.length() == 1 ? s.charAt(0) : s.charAt(1)), NamedTextColor.WHITE);
    }

    public static TextColor getTextColor(final DyeColor clr) {
        return dyeIx.getOrDefault(clr, NamedTextColor.WHITE);
    }

    public static TextColor getTextColor(final Color clr) {
        return clrIx.getOrDefault(clr, NamedTextColor.WHITE);
    }

    public static TextColor getTextColor(final char c) {
        return chrIx.getOrDefault(c, NamedTextColor.WHITE);
    }

    public static Character toChar(final TextColor clr) {
        return chrIx.inverse().getOrDefault(clr, 'f');
    }

    public static DyeColor getDyeColor(final TextColor clr) {
        return dyeIx.inverse().getOrDefault(clr, DyeColor.WHITE);
    }

    public static Color getBukkitColor(final TextColor clr) {
        return clrIx.inverse().getOrDefault(clr, Color.WHITE);
    }

    public static Color getBukkitColor(final String s) {
        return switch (s.toUpperCase()) {
            case "AQUA" ->
                Color.AQUA;
            case "BLUE" ->
                Color.BLUE;
            case "FUCHSIA" ->
                Color.FUCHSIA;
            case "GRAY" ->
                Color.GRAY;
            case "GREEN" ->
                Color.GREEN;
            case "LIME" ->
                Color.LIME;
            case "MAROON" ->
                Color.MAROON;
            case "NAVY" ->
                Color.NAVY;
            case "OLIVE" ->
                Color.OLIVE;
            case "ORANGE" ->
                Color.ORANGE;
            case "PURPLE" ->
                Color.PURPLE;
            case "RED" ->
                Color.RED;
            case "SILVER" ->
                Color.SILVER;
            case "TEAL" ->
                Color.TEAL;
            case "WHITE" ->
                Color.WHITE;
            case "YELLOW" ->
                Color.YELLOW;
            default ->
                Color.BLACK;
        };
    }

    public static int toByte(final TextColor color) {
        return toByte(toChar(color));
    }

    public static int toByte(final char color) {
        return switch (color) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' ->
                color - 48;
            case 'a' ->
                10;
            case 'b' ->
                11;
            case 'c' ->
                12;
            case 'd' ->
                13;
            case 'e' ->
                14;
            case 'я' ->
                16;
            case 'с' ->
                17;
            case 'б' ->
                18;
            case 'к' ->
                19;
            case 'ф' ->
                20;
            case 'о' ->
                21;
            case 'р' ->
                22;
            case 'н' ->
                23;
            case 'ч' ->
                24;
            case 'м' ->
                25;
            default ->
                15;
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
        return chrIx.getOrDefault(c, NamedTextColor.WHITE);
    }

    @Deprecated
    public static int toByte(final NamedTextColor color) {
        return toByte((TextColor) color);
    }

    @Deprecated
    public static String toChat(final TextColor color) {
        return "§" + toChar(color).toString();
    }

    @Deprecated
    public static String toChat(final NamedTextColor color) {
        return "§" + toChar(color).toString();
    }

    @Deprecated
    public static DyeColor getDyeColor(final NamedTextColor color) {
        return getDyeColor((TextColor) color);
    }

    //@Deprecated //вроде только в кланах иногда юзается в разных плагинах
    public static NamedTextColor chatColorFromString(final String s) {
        final TextColor tc = getTextColor(s);
        return tc instanceof NamedTextColor
                //? (NamedTextColor) tc : NamedTextColor.WHITE;
                ? (NamedTextColor) tc : NamedTextColor.nearestTo(tc);
    }

}
