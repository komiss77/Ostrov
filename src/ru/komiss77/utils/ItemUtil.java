package ru.komiss77.utils;

import javax.annotation.Nullable;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import io.papermc.paper.datacomponent.item.Repairable;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.block.Biome;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import ru.komiss77.Ostrov;
import ru.komiss77.boot.OStrap;
import ru.komiss77.modules.items.DataParser;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.items.PDC;
import ru.komiss77.modules.menuItem.MenuItem;
import ru.komiss77.modules.menuItem.MenuItemsManager;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.notes.Slow;
import ru.komiss77.objects.CaseInsensitiveMap;
import ru.komiss77.objects.Duo;
import ru.komiss77.version.Nms;

import static org.bukkit.attribute.Attribute.*;

/*
	SPEED  СКОРОСТЬ
    SLOWNESS  МЕДЛЕННОСТЬ
    HASTE  УСКОРЕНИЕ
    MINING FATIGUE  УСТАЛОСТЬ ОТ ДОБЫЧИ
    STRENGTH  СИЛА
    INSTANT HEALTH  МГНОВЕННОЕ ЗДОРОВЬЕ
    INSTANT DAMAGE  МГНОВЕННЫЙ УРОН
    JUMP BOOST  УСИЛИЕ ПРЫЖКА
    NAUSEA  ТОШНОТА
    REGENERATION  РЕГЕНЕРАЦИЯ
    RESISTANCE  СОПРОТИВЛЕНИЕ
    FIRE RESISTANCE  СОПРОТИВЛЕНИЕ ОГНЮ
    WATER BREATHING  ВОДНОЕ ДЫХАНИЕ
    INVISIBILITY  НЕВИДИМОСТЬ
    BLINDNESS  СЛЕПОТА
    NIGHT VISION  НОЧНОЕ ЗРЕНИЕ
    HUNGER  ГОЛОД
    WEAKNESS  СЛАБОСТЬ
    POISON  ЯД
    WITHER  УВЯДЕНИЕ
    HEALTH BOOST  УВЕЛИЧЕНИЕ ЗДОРОВЬЯ
    ABSORPTION  ПОГЛОЩЕНИЕ
    SATURATION  НАСЫЩЕНИЕ
    GLOWING  СВЕТЛЕНИЕ
    LEVITATION  ЛЕВИТАЦИЯ
    LUCK  УДАЧА
    UNLUCK  НЕУДАЧА
    SLOW FALLING  МЕДЛЕННОЕ ПАДЕНИЕ
    CONDUIT POWER  СИЛА ПРОВОДНИКА
    DOLPHINS GRACE  ГРАЦИЯ ДЕЛЬФИНОВ
    BAD OMEN  ДУРНОЕ ЗНАМЕНИЕ
    HERO OF THE VILLAGE ГЕРОЙ ДЕРЕВНИ
    DARKNESS  ТЬМА
    TRIAL OMEN  ПРЕДЗНАМЕНИЕ ИСПЫТАНИЯ
    RAID OMEN  ПРЕДЗНАМЕНИЕ РЕЙДА
    WIND CHARGED  ЗАРЯЖЕННОСТЬ ВЕТРОМ
    WEAVING  ТКАЧЕСТВО
    OOZING  СОЧАЩИЙ
    INFESTED  ЗАРАЖЕННЫЙ
 */

/*
ЗАЩИТА;
ЗАЩИТА_ОТ_ОГНЯ;
ПАДЕНИЕ_ПЕРЬЯ;
ЗАЩИТА_ОТ_ВЗРЫВА;
ЗАЩИТА_ОТ_СНАРТИКА;
ДЫХАНИЕ;
АКВА_АФФИННОСТЬ;
ШИПЫ;
ГЛУБИНА_БЕГА;
МОРОЗ_ХОД;
СВЯЗЫВАЮЩЕЕ_ПРОКЛЯТИЕ;
ОСТРОСТЬ;
РАЗБИТЬ;
БОГ_ЧЛЕНИСТОНОГ;
ОТБИТЬ;
ОГНЕННЫЙ_АСПЕКТ;
ГРАБЛЕНИЕ;
РАЗМЫШЛЕНИЕ_КРАЯ;
ЭФФЕКТИВНОСТЬ;
ШЕЛКОВОЕ_КАСАНИЕ;
НЕРАЗРЫВ;
УДАРСТВО;
СИЛА;
УДАР;
ПЛАМЯ;
БЕСКОНЕЧНОСТЬ;
УДАЧА_МОРЯ;
ПРИМАШКА;
ВЕРНОСТЬ;
ПРОНЗАНИЕ;
БЕГ;
НАПРАВЛЕНИЕ;
МНОГОУДАРНЫЙ;
БЫСТРЫЙ_ЗАРЯД;
ПРОБИВАНИЕ;
ПЛОТНОСТЬ;
ПРОРЫВ;
ВЕТРОВОЙ_ВЗРЫВ;
ИСПРАВЛЕНИЕ;
ИСЧЕЗНОВЕНИЕ_ПРОКЛЯТИЯ;
СКОРОСТЬ_ДУШИ;
БЫСТРЫЙ_СЛЕД;
 */

public class ItemUtil {

    public static final NamespacedKey key;
    private static final CaseInsensitiveMap<PlayerProfile> playerProfilesCache;
    public static final ItemStack air, book, add, nextPage, previosPage;
    private static final Set<ItemType> POTION;
    private static final Pattern regex;
    private static final Registry<Attribute> ATTR_REG;
//    private static final Gson GSON;

    static {
        ATTR_REG = RegistryAccess.registryAccess().getRegistry(RegistryKey.ATTRIBUTE);
        key = new NamespacedKey(Ostrov.instance, "item");
        playerProfilesCache = new CaseInsensitiveMap<>();
        regex = Pattern.compile("(.{1,24}(?:\\s|$))|(.{0,24})", Pattern.DOTALL);
        air = ItemType.AIR.createItemStack();
        book = ItemType.WRITTEN_BOOK.createItemStack();
        add = new ItemBuilder(ItemType.PLAYER_HEAD)
            .name("§aдобавить")
            .headTexture(Texture.add)
            .build();
        nextPage = new ItemBuilder(ItemType.PLAYER_HEAD)
            .name("§fдалее")
            .headTexture(Texture.nextPage)
            .build();
        previosPage = new ItemBuilder(ItemType.PLAYER_HEAD)
            .name("§fназад")
            .headTexture(Texture.previosPage)
            .build();
        POTION = Set.of(ItemType.TIPPED_ARROW, ItemType.POTION,
            ItemType.LINGERING_POTION, ItemType.SPLASH_POTION);
    }

    public static Texture getNumberTexture(final int number) {
        return switch (number) {
            case 0 -> Texture._0_;
            case 1 -> Texture._1_;
            case 2 -> Texture._2_;
            case 3 -> Texture._3_;
            case 4 -> Texture._4_;
            case 5 -> Texture._5_;
            case 6 -> Texture._6_;
            case 7 -> Texture._7_;
            case 8 -> Texture._8_;
            case 9 -> Texture._9_;
            default -> Texture.none;
        };
    }

    public static int findItem(final Player p, final ItemStack item) {
        for (int i = 0; i < p.getInventory().getContents().length; i++) {
            if (compareItem(p.getInventory().getContents()[i], item, true)) {
                return i;
            }
        }
        return -1;
    }

    @Deprecated
    public static int getCusomModelData(final ItemStack is) {
        if (is != null && is.hasItemMeta() && is.getItemMeta().hasCustomModelData()) {
            return is.getItemMeta().getCustomModelData();
        }
        return 0;
    }

    @Deprecated
    public static ItemStack setCusomModelData(final ItemStack is, final int id) {
        if (is == null) return null;
        final ItemMeta im = is.hasItemMeta() ? is.getItemMeta() : Bukkit.getItemFactory().getItemMeta(is.getType());//is.getItemMeta();
        im.setCustomModelData(id);
        is.setItemMeta(im);
        return is;
    }

    public static ItemStack setName(final ItemStack is, final String name) {
        final ItemMeta im = is.getItemMeta();
        im.displayName(TCUtil.form(name));
        is.setItemMeta(im);
        return is;
    }


    public enum Texture {
        nextPage("c2f910c47da042e4aa28af6cc81cf48ac6caf37dab35f88db993accb9dfe516"),
        previosPage("f2599bd986659b8ce2c4988525c94e19ddd39fad08a38284a197f1b70675acc"),
        add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "NWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19"),
        //https://minecraft-heads.com/custom-heads/alphabet?start=4720
        //черный стиль - https://minecraft-heads.com/custom-heads/alphabet?start=3600
        _0_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "M2YwOTAxOGY0NmYzNDllNTUzNDQ2OTQ2YTM4NjQ5ZmNmY2Y5ZmRmZDYyOTE2YWVjMzNlYmNhOTZiYjIxYjUifX19"),
        _1_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "Y2E1MTZmYmFlMTYwNThmMjUxYWVmOWE2OGQzMDc4NTQ5ZjQ4ZjZkNWI2ODNmMTljZjVhMTc0NTIxN2Q3MmNjIn19fQ=="),
        _2_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "NDY5OGFkZDM5Y2Y5ZTRlYTkyZDQyZmFkZWZkZWMzYmU4YTdkYWZhMTFmYjM1OWRlNzUyZTlmNTRhZWNlZGM5YSJ9fX0="),
        _3_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "ZmQ5ZTRjZDVlMWI5ZjNjOGQ2Y2E1YTFiZjQ1ZDg2ZWRkMWQ1MWU1MzVkYmY4NTVmZTlkMmY1ZDRjZmZjZDIifX19"),
        _4_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "ZjJhM2Q1Mzg5ODE0MWM1OGQ1YWNiY2ZjODc0NjlhODdkNDhjNWMxZmM4MmZiNGU3MmY3MDE1YTM2NDgwNTgifX19"),
        _5_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "ZDFmZTM2YzQxMDQyNDdjODdlYmZkMzU4YWU2Y2E3ODA5YjYxYWZmZDYyNDVmYTk4NDA2OTI3NWQxY2JhNzYzIn19fQ=="),
        _6_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "M2FiNGRhMjM1OGI3YjBlODk4MGQwM2JkYjY0Mzk5ZWZiNDQxODc2M2FhZjg5YWZiMDQzNDUzNTYzN2YwYTEifX19"),
        _7_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "Mjk3NzEyYmEzMjQ5NmM5ZTgyYjIwY2M3ZDE2ZTE2OGIwMzViNmY4OWYzZGYwMTQzMjRlNGQ3YzM2NWRiM2ZiIn19fQ=="),
        _8_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "YWJjMGZkYTlmYTFkOTg0N2EzYjE0NjQ1NGFkNjczN2FkMWJlNDhiZGFhOTQzMjQ0MjZlY2EwOTE4NTEyZCJ9fX0="),
        _9_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "ZDZhYmM2MWRjYWVmYmQ1MmQ5Njg5YzA2OTdjMjRjN2VjNGJjMWFmYjU2YjhiMzc1NWU2MTU0YjI0YTVkOGJhIn19fQ=="),
        dot("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "MzIzZTYxOWRjYjc1MTFjZGMyNTJhNWRjYTg1NjViMTlkOTUyYWM5ZjgyZDQ2N2U2NmM1MjI0MmY5Y2Q4OGZhIn19fQ=="),
        dotdot("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "MmZmY2MzMThjMjEyZGM3NDliNTk5NzU1ZTc2OTdkNDkyMzgyOTkzYzA3ZGUzZjhlNTRmZThmYzdkZGQxZSJ9fX0="),
        up("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "NGIyMjFjYjk2MDdjOGE5YmYwMmZlZjVkNzYxNGUzZWIxNjljYzIxOWJmNDI1MGZkNTcxNWQ1ZDJkNjA0NWY3In19fQ=="),
        down("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUv" +
            "ZDhhYWI2ZDlhMGJkYjA3YzEzNWM5Nzg2MmU0ZWRmMzYzMTk0Mzg1MWVmYzU0NTQ2M2Q2OGU3OTNhYjQ1YTNkMyJ9fX0="),
        none(""),
        ;

        public final String value;

        Texture(final String texture) {
            this.value = texture;
        }
    }

    public static ItemMeta setHeadTexture(final SkullMeta skullMeta, String skinData) {
        if (skinData.length() > 72) { //определяяем зашифрованную ссылку
            skinData = new String(Base64.getDecoder().decode(skinData));
            int idx = skinData.indexOf("SKIN");
            if (idx > 0) {
                skinData = skinData.substring(idx + 25);
                idx = skinData.indexOf("\"");
                if (idx > 0) {
                    skinData = skinData.substring(0, idx);
                }
            }
        }
        PlayerProfile profile = getProfile(skinData);
        skullMeta.setPlayerProfile(profile);
        return skullMeta;
    }

    public static PlayerProfile profileTexture(String skinData) {
        if (skinData.length() > 72) { //определяяем зашифрованную ссылку
            skinData = new String(Base64.getDecoder().decode(skinData));
            int idx = skinData.indexOf("SKIN");
            if (idx > 0) {
                skinData = skinData.substring(idx + 25);
                idx = skinData.indexOf("\"");
                if (idx > 0) {
                    skinData = skinData.substring(0, idx);
                }
            }
        }
        return getProfile(skinData);
    }

    public static PlayerProfile getProfile(String SHA_or_URL) {
        if (playerProfilesCache.containsKey(SHA_or_URL)) {
            return playerProfilesCache.get(SHA_or_URL);
        }
        final UUID uuid = UUID.randomUUID();
        final PlayerProfile profile = Bukkit.createProfile(uuid);
        final PlayerTextures textures = profile.getTextures();
        if (!SHA_or_URL.startsWith("http://")) {
            SHA_or_URL = "https://textures.minecraft.net/texture/" + SHA_or_URL;
        }
        try {
            final URL url = URI.create(SHA_or_URL).toURL();
            textures.setSkin(url);
            profile.setTextures(textures);
            playerProfilesCache.put(SHA_or_URL, profile);
        } catch (MalformedURLException | IllegalArgumentException ex) {
            Ostrov.log_warn("Invalid texture SHA_or_URL");
        }
        return profile;
    }


    /**
     * @param current текущее lore. null - создать новое
     * @param text    текст. (br в > скобках)- перенос строки. <br>пробел или |
     *                -возможный перенос
     * @param color   null или осносной цвет текста
     * @return
     */
    public static List<Component> lore(@Nullable List<Component> current, final String text, @Nullable String color) {
        if (current == null) current = new ArrayList<>();
        if (text == null) return current;
        final Matcher regexMatcher = regex.matcher(text);
        while (regexMatcher.find()) {
            current.add(TCUtil.form(color == null ? regexMatcher.group() : color + regexMatcher.group()));
        }
    /*final String[] блоки = text.replace('&', '§');
    //else блоки = {text};
    for (final String блок : блоки) {
      final List<String> нарезка = split(блок, 25);
      for (String строчка : нарезка) {
        current.add(clr + строчка);
      }
    }*/
//Ostrov.log("genLore current="+current);
        return current;
    }

    public static List<String> genLore(@Nullable List<String> current, final String text, @Nullable final String color) {
        if (current == null) current = new ArrayList<>();
        final String clr = color == null ? "§7" : color;

        final String[] blocks = text.replace('&', '§').split("<br>");
        //else блоки = {text};
        for (final String блок : blocks) {
            final List<String> split = split(блок, 25);
            for (String строчка : split) {
                current.add(clr + строчка);
            }
        }
//Ostrov.log("genLore current="+current);
        return current;
    }

    //не менять! именно List<Component> !
    public static List<Component> genLore(@Nullable List<Component> current, @Nullable final String text) {
        if (current == null) current = new ArrayList<>();
        if (text == null) return current;

        final String[] blocks = text.replace('&', '§').split("<br>");
        for (final String block : blocks) {
            if (block.length() <= 25) {
                current.add(TCUtil.form(block));
            } else {
                final List<String> split = split(block, 25);
                for (String line : split) {
                    current.add(TCUtil.form(line));
                }
            }
        }
        return current;
    }


    @Deprecated
    public static List<String> split(String block, int lineLenght) {
        List<String> split = new ArrayList<>();
        if (block.length() <= lineLenght) {
            split.add(block);
            return split;
        }

        boolean nextLine = false;
        //int index = 0;
        int currentLineLenght = lineLenght;

        StringBuilder sb = new StringBuilder();
        char[] blockArray = block.toCharArray();

        for (int position = 0; position < blockArray.length; position++) {
//System.out.println("111 index="+index+"  position="+position+" char="+блок_array[position] );

            if (blockArray[position] == '§') {
//System.out.println("skip § 111 position="+position );
                sb.append(blockArray[position]);
                //position++;
                currentLineLenght++;
                position++;
                sb.append(blockArray[position]);
                currentLineLenght++;
                //System.out.println("skip § 222 position="+position );
            } else {
//System.out.println("222 index="+index+"  position="+position );
                if (position != 0 && position % currentLineLenght == 0) {
//System.out.println("nextLine 111 position="+position+"  current_line_lenght="+current_line_lenght );
                    nextLine = true;
                }
                if (nextLine && (blockArray[position] == ' ' || blockArray[position] == '|' || blockArray[position] == ',' || blockArray[position] == '.')) {
                    nextLine = false;
                    split.add(sb.toString());
                    //index++;
                    sb = new StringBuilder();
                    currentLineLenght = lineLenght;
//System.out.println("nextLine 222 index="+index+" position="+position+"  current_line_lenght="+current_line_lenght );
                } else {
                    sb.append(blockArray[position]);
                }
            }
        }
        split.add(sb.toString()); //добавляем, что осталось

        return split;
    }

    public static boolean giveItemTo(final Player p, final ItemStack item, final int pos, final boolean force) {  //просто выдать в нужный слот
        final PlayerInventory inv = p.getInventory();
        final ItemStack there = inv.getItem(pos);
        if (isBlank(there, false)) {                                        //если требуемая позиция пустая,
            inv.setItem(pos, item);                                            //ставим предмет и возврат
            return true;
        }
        if (force) {
            inv.setItem(pos, item);                                            //ставим предмет и возврат
            final MenuItem mi = MenuItemsManager.fromItemStack(there);
            if (mi != null && mi.forced) return true;
            giveItemsTo(p, there);
            return true;
        }
        //уже есть в слоту
        if (compare(there, item, Stat.TYPE, Stat.NAME, Stat.AMOUNT)) return true;
        giveItemsTo(p, item);//кидаем предмет рядом
        return false;
    }

    public static boolean getItems(Player player, int count, Material mat) {
        final Map<Integer, ? extends ItemStack> ammo = player.getInventory().all(mat);

        int found = 0;
        for (ItemStack stack : ammo.values()) {
            found += stack.getAmount();
        }
        if (count > found) {
            return false;
        }

        for (final Entry<Integer, ? extends ItemStack> en : ammo.entrySet()) {
            ItemStack stack = en.getValue();
            int removed = Math.min(count, stack.getAmount());
            count -= removed;
            if (stack.getAmount() == removed) {
                player.getInventory().setItem(en.getKey(), null);
            } else {
                stack.setAmount(stack.getAmount() - removed);
            }
            if (count <= 0) {
                break;
            }
        }

        player.updateInventory();
        return true;
    }

    public static void substractItemInHand(final Player p, final EquipmentSlot hand) {
        if (hand == EquipmentSlot.HAND) {
            if (p.getInventory().getItemInMainHand().getAmount() == 1) {
                p.getInventory().setItemInMainHand(air);
            } else {
                p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
            }
        } else if (hand == EquipmentSlot.OFF_HAND) {
            if (p.getInventory().getItemInOffHand().getAmount() == 1) {
                p.getInventory().setItemInOffHand(air);
            } else {
                p.getInventory().getItemInOffHand().setAmount(p.getInventory().getItemInOffHand().getAmount() - 1);
            }
        }
    }

    public static boolean substractOneItem(final HumanEntity he, final Material mat) {
      //if (!he.getInventory().contains(mat)) {бессмысленно, там тоже делается обход циклом
      //    return false;
      //}
        ItemStack is;
        for (int i = 0; i < he.getInventory().getContents().length; i++) {
            is = he.getInventory().getContents()[i];
            if (is != null && is.getType() == mat) {
                if (is.getAmount() >= 2) {
                    is.setAmount(is.getAmount() - 1);//he.getInventory().getContents()[i].setAmount(he.getInventory().getContents()[i].getAmount() - 1);
                } else {
                    is = air;//he.getInventory().getContents()[i].setAmount(0);
                }
                he.getInventory().setItem(i, is);
                return true;
            }
        }
        return false;
    }

    public static boolean substractAllItems(final HumanEntity he, final Material mat) {
        if (!he.getInventory().contains(mat)) {
            return false;
        }
        boolean result = false;
        for (int i = 0; i < he.getInventory().getContents().length; i++) {
            if (he.getInventory().getContents()[i] != null && he.getInventory().getContents()[i].getType() == mat) {
                he.getInventory().getContents()[i].setAmount(0);
                result = true;
            }
        }
        return result;
    }

    public static boolean substractItem(final Player he, final Material mat, int ammount) {
        if (getItemCount(he, mat) < ammount) {
            return false;
        }
        final ItemStack[] cloneInv = new ItemStack[he.getInventory().getContents().length];// = playerInvClone.getContents();
        ItemStack toClone;
        for (int slot = 0; slot < he.getInventory().getContents().length; slot++) {
            toClone = he.getInventory().getContents()[slot];
            cloneInv[slot] = toClone == null ? null : toClone.clone();
        }
        for (int slot = 0; slot < cloneInv.length; slot++) {
            if (cloneInv[slot] != null && mat == cloneInv[slot].getType()) {
                if (cloneInv[slot].getAmount() == ammount) { //найдено и убрано - дальше не ищем
                    cloneInv[slot] = air.clone();
                    ammount = 0;
                    //itemFindResult.remove(mat);
                    break;
                }

                if (cloneInv[slot].getAmount() > ammount) { //найдено больше чем надо - дальше не ищем
                    cloneInv[slot].setAmount(cloneInv[slot].getAmount() - ammount);
                    ammount = 0;
                    //itemFindResult.remove(mat);
                    break;
                }

                if (cloneInv[slot].getAmount() < ammount) { //найдено меньше чем надо - убавили требуемое и ищем дальше
                    ammount -= cloneInv[slot].getAmount();
                    //itemFindResult.put(mat, ammount);
                    cloneInv[slot] = air.clone();
                }
            }
        }
        if (ammount == 0) {//if (itemFindResult.isEmpty()) {
            he.getInventory().setContents(cloneInv);
            he.updateInventory();
            return true;
        }
        return false;
    }

    public static int getItemCount(final HumanEntity he, final Material mat) {
        int result = 0;
        for (final ItemStack slot : he.getInventory().getContents()) {
            if (slot != null && slot.getType() == mat) {
                result += slot.getAmount();
            }
        }
        return result;
    }

    public static int repairAll(final Player p) {
        int repaired = 0; //Set <String> repaired = new HashSet<String>() {};
        for (final ItemStack item : p.getInventory()) {
            if (isBlank(item, false) || !hasDur(item)) continue;
            setDur(item, maxDur(item));
            repaired++;
        }

        p.updateInventory();
        return repaired;
    }

    public static boolean hasDur(final ItemStack it) {
        final Integer dmg = it.getData(DataComponentTypes.DAMAGE);
        return dmg != null && dmg != 0;
    }

    public static int maxDur(final ItemStack it) {
        final Integer max = it.getData(DataComponentTypes.MAX_DAMAGE);
        return max == null ? it.getType().asItemType().getMaxDurability() : max;
    }

    public static void setDur(final ItemStack it, final int d) {
        final int max = maxDur(it);
        if (d == max) {
            it.unsetData(DataComponentTypes.DAMAGE);
            return;
        }
        it.setData(DataComponentTypes.DAMAGE, Math.max(0, max - d));
    }

    @Deprecated
    public static boolean damage(final HumanEntity p, final ItemStack it, final int damage, final EntityEffect breackEffect, final boolean checkEnch) {
//    p.damageItemStack(it, damage);
        //p.sendMessage("1");
        if (!isBlank(it, false) && it.getItemMeta() instanceof final Damageable dm && !dm.isUnbreakable()) {
            if (it.containsEnchantment(Enchantment.UNBREAKING) && checkEnch
                && Ostrov.random.nextInt(it.getEnchantmentLevel(Enchantment.UNBREAKING) + 1) == 0) {
                return false;
            }
            //p.sendMessage("dmg-" + (dm.getDamage() - 1) + "->" + dm.getDamage());
            if (dm.getDamage() + damage < it.getType().getMaxDurability()) {
                if (dm.getDamage() + damage < 0) {
                    return false;
                }
                //Bukkit.getConsoleSender().sendMessage("" + (dm.getDamage() + amt));
                dm.setDamage(dm.getDamage() + damage);
                it.setItemMeta(dm);
            } else {
                //p.sendMessage("itmx-" + it.getType().getMaxDurability());
                p.playEffect(breackEffect);
                switch (breackEffect) {
                    case BREAK_EQUIPMENT_OFF_HAND -> p.getInventory().setItemInOffHand(air);
                    case BREAK_EQUIPMENT_HELMET -> p.getInventory().setHelmet(air);
                    case BREAK_EQUIPMENT_CHESTPLATE -> p.getInventory().setChestplate(air);
                    case BREAK_EQUIPMENT_LEGGINGS -> p.getInventory().setLeggings(air);
                    case BREAK_EQUIPMENT_BOOTS -> p.getInventory().setBoots(air);
                    default -> p.getInventory().setItemInMainHand(air);
                }
            }
            return true;

        }
        return false;
    }

    public static boolean is(final ItemStack item, final ItemType type) {
        return item != null && type.equals(item.getType().asItemType());
    }

    public static boolean isBlank(final ItemStack item, final boolean checkData) {
        if (item == null) return true;
        final ItemType tp = item.getType().asItemType();
        if (tp == ItemType.AIR) return true;
        return checkData && item.getDataTypes().size() > tp.getDefaultDataTypes().size();
    }

    public static boolean hasName(final ItemStack is) {
        return is != null && is.hasItemMeta() && is.getItemMeta().hasDisplayName();
    }

    public static String getName(final ItemStack is) {
        return hasName(is) ? TCUtil.deform(is.getItemMeta().displayName()) : "";
    }

    public static void giveItemsTo(final Player p, final ItemStack... its) {
        boolean left = false;
        for (final ItemStack it : p.getInventory().addItem(its).values()) {
            p.getWorld().dropItem(p.getLocation(), it);
            left = true;
        }
        if (left) {
            ScreenUtil.sendActionBarDirect(p, "§4В твоем инвентаре не было места, предмет выпал рядом!");
        }
    }
    private static final StringUtil.Split[] seps = StringUtil.Split.values();
    private static final DataParser parsers = createParser();
    private static DataParser createParser() {
        final DataParser dataParser = new DataParser();
        dataParser.put(DataParser.PDC_TYPE, new DataParser.Parser<>() {
            public String write(final PDC.Data val) {
                final StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (final Duo<NamespacedKey, Serializable> en : val) {
                    if (first) first = false;
                    else sb.append(seps[1].get());
                    sb.append(en.key().asMinimalString())
                        .append(seps[2].get()).append(en.val());
                }
                return sb.toString();
            }

            public PDC.Data parse(final String str) {
                final String[] parts = seps[1].split(str);
                final PDC.Data bld = new PDC.Data();
                for (final String p : parts) {
                    final String[] mod = seps[2].split(p);
                    if (!ClassUtil.check(mod, 2, false)) continue;
                    bld.add(NamespacedKey.fromString(mod[0]), mod[1]);
                }
                return bld;
            }
        });
        dataParser.put(DataComponentTypes.ITEM_MODEL, new DataParser.Parser<Key>() {
            public String write(final Key val) {
                return val.asMinimalString();
            }
            public Key parse(final String str) {
                return Key.key(str);
            }
        });
        dataParser.put(DataComponentTypes.ATTRIBUTE_MODIFIERS, new DataParser.Parser<ItemAttributeModifiers>() {
            public String write(final ItemAttributeModifiers val) {
                final StringBuilder sb = new StringBuilder();
                for (final ItemAttributeModifiers.Entry ie : val.modifiers()) {
                    final AttributeModifier mod = ie.modifier();
                    sb.append(seps[1].get()).append(String.join(seps[2].get(), ofRegKey(Ostrov.registries.ATTRIBS, ie.attribute()), ofKey(mod),
                        StringUtil.toSigFigs(mod.getAmount(), (byte) 4), mod.getOperation().name(), mod.getSlotGroup().toString()));
                }
                return sb.isEmpty() ? "" : sb.substring(seps[1].get().length());
            }
            public ItemAttributeModifiers parse(final String str) {
                final String[] parts = seps[1].split(str);
                final ItemAttributeModifiers.Builder bld = ItemAttributeModifiers.itemAttributes();
                if (!ClassUtil.check(parts, 1, true)) return bld.build();
                for (int i = 0; i != parts.length; i++) {
                    final String[] mod = seps[2].split(parts[i]);
                    if (!ClassUtil.check(mod, 5, false)) continue;
                    bld.addModifier(OStrap.get(Key.key(mod[0]), LUCK),
                        new AttributeModifier(NamespacedKey.fromString(mod[1]), NumUtil.doubleOf(mod[2], 0d),
                            Operation.valueOf(mod[3]), EquipmentSlotGroup.getByName(mod[4])));
                }
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.DAMAGE, new DataParser.Parser<Integer>() {
            public String write(final Integer val) {
                return val.toString();
            }
            public Integer parse(final String str) {
                return NumUtil.intOf(str, 0);
            }
        });
        dataParser.put(DataComponentTypes.ITEM_NAME, new DataParser.Parser<Component>() {
            public String write(final Component val) {
                return TCUtil.deform(val);
            }
            public Component parse(final String str) {
                return TCUtil.form(str);
            }
        });
        dataParser.put(DataComponentTypes.CUSTOM_NAME, new DataParser.Parser<Component>() {
            public String write(final Component val) {
                return TCUtil.deform(val);
            }
            public Component parse(final String str) {
                return TCUtil.form(str);
            }
        });
        dataParser.put(DataComponentTypes.LORE, new DataParser.Parser<ItemLore>() {
            public String write(final ItemLore val) {
                return String.join(seps[1].get(), val.lines().stream().map(TCUtil::deform).toArray(i -> new String[i]));
            }
            public ItemLore parse(final String str) {
                return ItemLore.lore(Arrays.stream(seps[1].split(str)).map(TCUtil::form).toList());
            }
        });
        dataParser.put(DataComponentTypes.DYED_COLOR, new DataParser.Parser<DyedItemColor>() {
            public String write(final DyedItemColor val) {
                return String.valueOf(val.color().asARGB());
            }
            public DyedItemColor parse(final String str) {
                final String[] parts = seps[1].split(str);
                final DyedItemColor.Builder bld = DyedItemColor.dyedItemColor();
                if (!ClassUtil.check(parts, 1, true)) return bld.build();
                return bld.color(Color.fromARGB(NumUtil.intOf(parts[parts.length - 1], 0))).build();
            }
        });
        dataParser.put(DataComponentTypes.CONSUMABLE, new DataParser.Parser<Consumable>() {
            public String write(final Consumable val) {
                return String.join(seps[1].get(), val.animation().name(),
                    StringUtil.toSigFigs(val.consumeSeconds(), (byte) 2),
                    String.valueOf(val.hasConsumeParticles()));
            }
            public Consumable parse(final String str) {
                final String[] parts = seps[1].split(str);
                final Consumable.Builder bld = Consumable.consumable();
                if (!ClassUtil.check(parts, 3, false)) return bld.build();
                return bld.animation(ItemUseAnimation.valueOf(parts[0]))
                    .consumeSeconds(NumUtil.floatOf(parts[1], 0f))
                    .hasConsumeParticles(Boolean.parseBoolean(parts[2])).build();
            }
        });
        dataParser.put(DataComponentTypes.DAMAGE_RESISTANT, new DataParser.Parser<DamageResistant>() {
            public String write(final DamageResistant val) {
                return ofKey(val.types());
            }
            public DamageResistant parse(final String str) {
                return DamageResistant.damageResistant(TagKey.create(RegistryKey.DAMAGE_TYPE, Key.key(str)));
            }
        });
        dataParser.put(DataComponentTypes.ENCHANTABLE, new DataParser.Parser<Enchantable>() {
            public String write(final Enchantable val) {
                return String.valueOf(val.value());
            }
            public Enchantable parse(final String str) {
                return Enchantable.enchantable(NumUtil.intOf(str, 0));
            }
        });
        dataParser.put(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, new DataParser.Parser<Boolean>() {
            public String write(final Boolean val) {
                return val.toString();
            }
            public Boolean parse(final String str) {
                return Boolean.parseBoolean(str);
            }
        });
        dataParser.put(DataComponentTypes.ENCHANTMENTS, new DataParser.Parser<ItemEnchantments>() {
            public String write(final ItemEnchantments val) {
                final StringBuilder sb = new StringBuilder();
                for (final Entry<Enchantment, Integer> ie : val.enchantments().entrySet()) {
                    sb.append(seps[1].get()).append(ofKey(ie.getKey()))
                        .append(seps[2].get()).append(ie.getValue().intValue());
                }
                return sb.isEmpty() ? "" : sb.substring(seps[1].get().length());
            }
            public ItemEnchantments parse(final String str) {
                final String[] parts = seps[1].split(str);
                final ItemEnchantments.Builder bld = ItemEnchantments.itemEnchantments();
                if (!ClassUtil.check(parts, 1, true)) return bld.build();
                for (int i = 0; i != parts.length; i++) {
                    final String[] mod = seps[2].split(parts[i]);
                    if (!ClassUtil.check(mod, 2, false)) continue;
                    bld.add(OStrap.get(Key.key(mod[0]), Enchantment.AQUA_AFFINITY), NumUtil.intOf(mod[1], 0));
                }
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.STORED_ENCHANTMENTS, new DataParser.Parser<ItemEnchantments>() {
            public String write(final ItemEnchantments val) {
                final StringBuilder sb = new StringBuilder();
                for (final Entry<Enchantment, Integer> ie : val.enchantments().entrySet()) {
                    sb.append(seps[1].get()).append(ofKey(ie.getKey()))
                        .append(seps[2].get()).append(ie.getValue().intValue());
                }
                return sb.isEmpty() ? "" : sb.substring(seps[1].get().length());
            }
            public ItemEnchantments parse(final String str) {
                final String[] parts = seps[1].split(str);
                final ItemEnchantments.Builder bld = ItemEnchantments.itemEnchantments();
                if (!ClassUtil.check(parts, 1, true)) return bld.build();
                for (int i = 0; i != parts.length; i++) {
                    final String[] mod = seps[2].split(parts[i]);
                    if (!ClassUtil.check(mod, 2, false)) continue;
                    bld.add(OStrap.get(Key.key(mod[0]), Enchantment.AQUA_AFFINITY), NumUtil.intOf(mod[1], 0));
                }
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.EQUIPPABLE, new DataParser.Parser<Equippable>() {
            public String write(final Equippable val) {
                final Key model = val.assetId();
                return String.join(seps[1].get(), val.slot().name(), model == null ? StringUtil.NA : model.asMinimalString(), String.valueOf(val.damageOnHurt()),
                    String.valueOf(val.dispensable()), String.valueOf(val.swappable()), val.equipSound().asMinimalString());
            }
            public Equippable parse(final String str) {
                final String[] parts = seps[1].split(str);
                final Equippable.Builder bld = Equippable.equippable(EquipmentSlot.valueOf(parts[0]));
                if (!ClassUtil.check(parts, 6, false)) return bld.build();
                if (!StringUtil.isNA(parts[1])) bld.assetId(Key.key(parts[1]));
                return bld.damageOnHurt(Boolean.parseBoolean(parts[2])).dispensable(Boolean.parseBoolean(parts[3]))
                    .swappable(Boolean.parseBoolean(parts[4])).equipSound(Key.key(parts[5])).build();
            }
        });
        dataParser.put(DataComponentTypes.FIREWORKS, new DataParser.Parser<Fireworks>() {
            public String write(final Fireworks val) {
                final StringBuilder sb = new StringBuilder();
                sb.append(val.flightDuration());
                for (final FireworkEffect fe : val.effects()) {
                    final List<Color> clrs = fe.getColors();
                    final Color clr = clrs.isEmpty() ? Color.WHITE : clrs.getFirst();
                    final List<Color> fds = fe.getFadeColors();
                    final Color fd = fds.isEmpty() ? clr : fds.getFirst();
                    sb.append(seps[1].get()).append(String.join(seps[2].get(), fe.getType().name(), String.valueOf(clr.asARGB()),
                        String.valueOf(fd.asARGB()), String.valueOf(fe.hasFlicker()), String.valueOf(fe.hasTrail())));
                }
                return sb.toString();
            }
            public Fireworks parse(final String str) {
                final String[] parts = seps[1].split(str);
                final Fireworks.Builder bld = Fireworks.fireworks();
                if (!ClassUtil.check(parts, 1, true)) return bld.build();
                bld.flightDuration(NumUtil.intOf(parts[0], 1));
                for (int i = 1; i != parts.length; i++) {
                    final String[] mod = seps[2].split(parts[i]);
                    if (!ClassUtil.check(mod, 5, false)) continue;
                    bld.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.valueOf(mod[0]))
                        .withColor(Color.fromARGB(NumUtil.intOf(mod[1], 0))).withFade(Color.fromARGB(NumUtil.intOf(mod[2], 0)))
                        .flicker(Boolean.parseBoolean(mod[3])).trail(Boolean.parseBoolean(mod[4])).build());
                }
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.FOOD, new DataParser.Parser<FoodProperties>() {
            public String write(final FoodProperties val) {
                return String.join(seps[1].get(), String.valueOf(val.nutrition()),
                    StringUtil.toSigFigs(val.saturation(), (byte) 2), String.valueOf(val.canAlwaysEat()));
            }
            public FoodProperties parse(final String str) {
                final String[] parts = seps[1].split(str);
                final FoodProperties.Builder bld = FoodProperties.food();
                if (!ClassUtil.check(parts, 3, false)) return bld.build();
                return bld.nutrition(NumUtil.intOf(parts[0], 0)).saturation(NumUtil.floatOf(parts[1], 0f))
                    .canAlwaysEat(Boolean.parseBoolean(parts[2])).build();
            }
        });
        dataParser.put(DataComponentTypes.USE_COOLDOWN, new DataParser.Parser<UseCooldown>() {
            public String write(final UseCooldown val) {
                final Key key = val.cooldownGroup();
                return val.seconds() + seps[1].get() + (key == null ? StringUtil.NA : key.asMinimalString());
            }
            public UseCooldown parse(final String str) {
                final String[] parts = seps[1].split(str);
                final UseCooldown.Builder bld = UseCooldown.useCooldown(NumUtil.floatOf(parts[0], 0f));
                if (!ClassUtil.check(parts, 2, false)) return bld.build();
                if (!StringUtil.isNA(parts[1])) bld.cooldownGroup(Key.key(parts[1]));
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.TRIM, new DataParser.Parser<ItemArmorTrim>() {
            public String write(final ItemArmorTrim val) {
                return ofKey(val.armorTrim().getMaterial()) + seps[1].get() + ofKey(val.armorTrim().getPattern());
            }
            public ItemArmorTrim parse(final String str) {
                final String[] parts = seps[1].split(str);
                if (!ClassUtil.check(parts, 2, true)) return null;
                return ItemArmorTrim.itemArmorTrim(new ArmorTrim(OStrap.get(Key.key(parts[parts.length - 2]), TrimMaterial.IRON),
                    OStrap.get(Key.key(parts[parts.length - 1]), TrimPattern.COAST))).build();
            }
        });
        dataParser.put(DataComponentTypes.MAX_DAMAGE, new DataParser.Parser<Integer>() {
            public String write(final Integer val) {
                return val.toString();
            }
            public Integer parse(final String str) {
                return NumUtil.intOf(str, 1);
            }
        });
        dataParser.put(DataComponentTypes.POTION_CONTENTS, new DataParser.Parser<PotionContents>() {
            public String write(final PotionContents val) {
                final StringBuilder sb = new StringBuilder();
                final Color clr = val.customColor();
                sb.append(ofKey(val.potion())).append(seps[1].get()).append(clr == null ? StringUtil.NA : clr.asARGB());
                for (final PotionEffect pe : val.customEffects()) {
                    sb.append(seps[1].get()).append(String.join(seps[2].get(), ofKey(pe.getType()), String.valueOf(pe.getDuration()),
                        String.valueOf(pe.getAmplifier()), String.valueOf(pe.hasParticles() && pe.hasIcon())));
                }
                return sb.toString();
            }
            public PotionContents parse(final String str) {
                final String[] parts = seps[1].split(str);
                final PotionContents.Builder bld = PotionContents.potionContents();
                if (!ClassUtil.check(parts, 2, true)) return bld.build();
                bld.potion(Registry.POTION.get(Key.key(parts[0])));
                if (!StringUtil.isNA(parts[1])) bld.customColor(Color.fromARGB(NumUtil.intOf(parts[1], 0)));
                for (int i = 2; i != parts.length; i++) {
                    final String[] mod = seps[2].split(parts[i]);
                    if (!ClassUtil.check(mod, 4, false)) continue;
                    final boolean vis = Boolean.parseBoolean(mod[3]);
                    bld.addCustomEffect(new PotionEffect(Registry.POTION_EFFECT_TYPE.get(Key.key(mod[0])),
                        NumUtil.intOf(mod[1], 0), NumUtil.intOf(mod[2], 0), !vis, vis, vis));
                }
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.MAX_STACK_SIZE, new DataParser.Parser<Integer>() {
            public String write(final Integer val) {
                return val.toString();
            }
            public Integer parse(final String str) {
                return NumUtil.intOf(str, 1);
            }
        });
        dataParser.put(DataComponentTypes.REPAIRABLE, new DataParser.Parser<Repairable>() {
            public String write(final Repairable val) {
                return String.join(seps[1].get(), val.types().values().stream()
                    .map(tk -> ofKey(tk)).toArray(i -> new String[i]));
            }
            public Repairable parse(final String str) {
                return Repairable.repairable(OStrap.regSetOf(Arrays.stream(seps[1].split(str))
                    .map(Key::key).toList(), RegistryKey.ITEM));
            }
        });
        dataParser.put(DataComponentTypes.RARITY, new DataParser.Parser<ItemRarity>() {
            public String write(final ItemRarity val) {
                return val.name();
            }
            public ItemRarity parse(final String str) {
                return ItemRarity.valueOf(str);
            }
        });
        dataParser.put(DataComponentTypes.TOOL, new DataParser.Parser<Tool>() {
            public String write(final Tool val) {
                final StringBuilder sb = new StringBuilder();
                sb.append(StringUtil.toSigFigs(val.defaultMiningSpeed(), (byte) 2))
                    .append(seps[1].get()).append(val.damagePerBlock());
                for (final Tool.Rule rl : val.rules()) {
                    if (rl.speed() == null) continue;
                    final List<String> rls = new ArrayList<>();
                    rls.add(StringUtil.toSigFigs(rl.speed(), (byte) 2));
                    rls.add(rl.correctForDrops().name());
                    rls.addAll(rl.blocks().values().stream().map(tk -> ofKey(tk)).toList());
                    sb.append(seps[1].get()).append(String.join(seps[2].get(), rls.toArray(new String[0])));
                }
                return sb.toString();
            }
            public Tool parse(final String str) {
                final String[] parts = seps[1].split(str);
                final Tool.Builder bld = Tool.tool();
                if (!ClassUtil.check(parts, 2, true)) return bld.build();
                bld.defaultMiningSpeed(NumUtil.floatOf(parts[0], 1)).damagePerBlock(NumUtil.intOf(parts[1], 0));
                for (int i = 2; i != parts.length; i++) {
                    final String[] mod = seps[2].split(parts[i]);
                    if (!ClassUtil.check(mod, 2, true)) continue;
                    final List<Key> bks = new ArrayList<>(mod.length - 2);
                    for (int j = 2; j != mod.length; j++) {
                        bks.add(Key.key(mod[j]));
                    }
                    bld.addRule(Tool.rule(OStrap.regSetOf(bks, RegistryKey.BLOCK),
                        NumUtil.floatOf(mod[0], 0f), TriState.valueOf(mod[1])));
                }
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.TOOLTIP_DISPLAY, new DataParser.Parser<TooltipDisplay>() {
            public String write(final TooltipDisplay val) {
                final StringBuilder sb = new StringBuilder(String.valueOf(val.hideTooltip()));
                for (final DataComponentType dtc : val.hiddenComponents()) {
                    sb.append(seps[1].get()).append(dtc.key().asMinimalString());
                }
                return sb.toString();
            }
            public TooltipDisplay parse(final String str) {
                final String[] parts = seps[1].split(str);
                final TooltipDisplay.Builder bld = TooltipDisplay.tooltipDisplay();
                if (!ClassUtil.check(parts, 1, true)) return bld.build();
                bld.hideTooltip(Boolean.parseBoolean(parts[0]));
                final Set<DataComponentType> dtcs = new HashSet<>();
                for (int i = 1; i != parts.length; i++) {
                    dtcs.add(OStrap.get(Key.key(parts[i]), DataComponentTypes.BREAK_SOUND));
                }
                return bld.hiddenComponents(dtcs).build();
            }
        });
        dataParser.put(DataComponentTypes.PROFILE, new DataParser.Parser<ResolvableProfile>() {
            public String write(final ResolvableProfile val) {
                final StringBuilder sb = new StringBuilder(val.name() == null ? StringUtil.NA : val.name());
                sb.append(seps[1].get()).append(val.uuid() == null ? StringUtil.NA : val.uuid().toString());
                for (final ProfileProperty pp : val.properties()) {
                    final String sig = pp.getSignature();
                    if (sig == null) sb.append(seps[1].get()).append(String.join(seps[2].get(), pp.getName(), pp.getValue()));
                    else sb.append(seps[1].get()).append(String.join(seps[2].get(), pp.getName(), pp.getValue(), sig));
                }
                return sb.toString();
            }
            public ResolvableProfile parse(final String str) {
                final String[] parts = seps[1].split(str);
                final ResolvableProfile.Builder bld = ResolvableProfile.resolvableProfile();
                if (!ClassUtil.check(parts, 2, true)) return bld.build();
                if (!StringUtil.isNA(parts[0])) bld.name(parts[0]);
                if (!StringUtil.isNA(parts[1])) bld.uuid(UUID.fromString(parts[1]));
                final List<ProfileProperty> dtcs = new ArrayList<>();
                for (int i = 2; i != parts.length; i++) {
                    final String[] pps = seps[2].split(parts[i]);
                    if (!ClassUtil.check(pps, 2, true)) return bld.build();
                    dtcs.add(new ProfileProperty(pps[0], pps[1],
                        pps.length == 3 ? pps[2] : null));
                }
                return bld.addProperties(dtcs).build();
            }
        });
        dataParser.put(DataComponentTypes.BLOCKS_ATTACKS, new DataParser.Parser<BlocksAttacks>() {
            public String write(final BlocksAttacks val) {
                return String.join(seps[1].get(), String.valueOf(val.blockDelaySeconds()),
                    String.valueOf(val.disableCooldownScale()),
                    val.bypassedBy() == null ? StringUtil.NA : val.bypassedBy().key().asMinimalString(),
                    val.blockSound() == null ? StringUtil.NA : val.blockSound().asMinimalString(),
                    val.disableSound() == null ? StringUtil.NA : val.disableSound().asMinimalString());
            }
            public BlocksAttacks parse(final String str) {
                final String[] parts = seps[1].split(str);
                final BlocksAttacks.Builder bld = BlocksAttacks.blocksAttacks();
                if (!ClassUtil.check(parts, 5, false)) return bld.build();
                bld.blockDelaySeconds(NumUtil.floatOf(parts[0], 0f));
                bld.disableCooldownScale(NumUtil.floatOf(parts[1], 1f));
                if (!StringUtil.isNA(parts[2])) bld.bypassedBy(TagKey
                    .create(RegistryKey.DAMAGE_TYPE, Key.key(parts[2])));
                if (!StringUtil.isNA(parts[3])) bld.blockSound(Key.key(parts[3]));
                if (!StringUtil.isNA(parts[4])) bld.disableSound(Key.key(parts[4]));
                return bld.build();
            }
        });
        dataParser.put(DataComponentTypes.WEAPON, new DataParser.Parser<Weapon>() {
            public String write(final Weapon val) {
                return val.itemDamagePerAttack()
                    + seps[1].get() + val.disableBlockingForSeconds();
            }
            public Weapon parse(final String str) {
                final String[] parts = seps[1].split(str, true);
                final Weapon.Builder bld = Weapon.weapon();
                if (!ClassUtil.check(parts, 2, false)) return bld.build();
                return bld.itemDamagePerAttack(NumUtil.intOf(parts[0], 1))
                    .disableBlockingForSeconds(NumUtil.floatOf(parts[1], 0f)).build();
            }
        });
        return dataParser;
    }

    public static final String OLD_PDC = "custom_data";

    @Slow(priority = 3)
    public static String write(final @Nullable ItemStack is) {
        if (is == null || ItemType.AIR.equals(is.getType().asItemType())) return "air";
        final StringBuilder res = new StringBuilder(ofKey(is.getType().asItemType()) + StringUtil.Split.MEDIUM.get() + is.getAmount());
        for (final DataComponentType dtc : is.getDataTypes()) {
            if (!is.isDataOverridden(dtc)) continue;
            switch (dtc) {
                case final DataComponentType.NonValued nvd -> {
                    if (nvd.key().value().equals(OLD_PDC)) continue;
                    res.append(StringUtil.Split.LARGE.get()).append(ofKey(nvd));
                }
                case final DataComponentType.Valued<?> vld -> append(is, res, vld);
                default -> {}
            }
        }
        final PersistentDataContainerView pdc = is.getPersistentDataContainer();
        if (pdc.isEmpty()) return res.toString();
        try {
            res.append(StringUtil.Split.LARGE.get()).append(PDC.ID)
            .append(StringUtil.Split.MEDIUM.get())
            .append(Base64Coder.encode(pdc.serializeToBytes()));
        } catch (IOException e) {}
        return res.toString();
    }

    @Slow(priority = 2)
    public static ItemStack parse(final @Nullable String str) {
        if (str == null || str.startsWith("air")) return ItemType.AIR.createItemStack();
        final String[] split = seps[0].split(str);
        final String[] idt = seps[1].split(split[0]);
        final ItemType tp;
        try {
            tp = OStrap.get(Key.key(idt[0]), ItemType.AIR);
        } catch (InvalidKeyException e) {
            Ostrov.log_err("Couldn't parse type for " + str);
            e.printStackTrace();
            return ItemType.AIR.createItemStack();
        }
        if (tp == ItemType.AIR) {
            Ostrov.log_err("Failed parsing item type for " + str);
            return tp.createItemStack();
        }
        final ItemStack it = tp.createItemStack(idt.length == 2 ? NumUtil.intOf(idt[1], 1) : 1);
        String data = null;
        try {
            for (int i = 1; i != split.length; i++) {
                data = split[i];
                final String[] dsp = seps[1].split(data, true);
                if (dsp.length == 1) {
                    if (Registry.DATA_COMPONENT_TYPE.get(Key.key(data))
                        instanceof final DataComponentType.NonValued nvd) {
                        it.setData(nvd);
                    }
                    continue;
                }
                if (PDC.ID.equals(dsp[0])) {
                    it.editPersistentDataContainer(pdc -> {
                        try {pdc.readFromBytes(Base64Coder.decode(dsp[1]));}
                        catch (IOException | IllegalArgumentException e) {
                            Ostrov.log_warn("Couldnt parse pdc of " + dsp[1] + ", trying old");
                            final DataParser.Parser<PDC.Data> prs = parsers.get(DataParser.PDC_TYPE);
                            if (prs != null) Nms.setCustomData(it, prs.parse(dsp[1]));
                        }
                    });
                    continue;
                }
                if (Registry.DATA_COMPONENT_TYPE.get(Key.key(dsp[0]))
                    instanceof final DataComponentType.Valued<?> vld) {
                    append(it, dsp[1], vld);
                }
            }
        } catch (NullPointerException | IllegalArgumentException | InvalidKeyException e) {
            Ostrov.log_err("Couldn't parse data " + data);
            e.printStackTrace();
            return it;
        }
        return it;
    }

    private static <D> void append(final ItemStack it, final StringBuilder sb, final DataComponentType.Valued<D> dtc) {
        final D val = it.getData(dtc); if (val == null) return;
        final DataParser.Parser<D> prs = parsers.get(dtc); if (prs == null) return;
        sb.append(seps[0].get()).append(ofKey(dtc)).append(seps[1].get()).append(prs.write(val));
    }

    private static <D> void append(final ItemStack it, final String data, final DataComponentType.Valued<D> dtc) {
        final DataParser.Parser<D> prs = parsers.get(dtc); if (prs == null) return;
        final D pd = prs.parse(data);
        if (pd == null) {
            Ostrov.log_warn("Couldnt parse '" + data + "' for " + dtc.key().asMinimalString());
            return;
        }
        it.setData(dtc, pd);
    }

    private static <K extends net.kyori.adventure.key.Keyed> @Nullable String ofKey(final @Nullable K k) {
        if (k == null) return null;
        return k.key().asMinimalString();
    }

    private static <K extends Keyed> @Nullable String ofRegKey(final Registry<K> reg, final @Nullable K k) {
        if (k == null) return null;
        final NamespacedKey key = reg.getKey(k);
        return key == null ? null : key.asMinimalString();
    }

    @Deprecated
    public static String toString(final ItemStack is) {
        return toString(is, ":");
    }

    @Deprecated
    public static String toString(final ItemStack is, final String splitter) {
        if (is == null || ItemType.AIR.equals(is.getType().asItemType())) return "air:1";
        final StringBuilder res = new StringBuilder(is.getType().asItemType().key().value() + ":" + is.getAmount());//apple<>1
        final String spl = " " + splitter + " ";

        if (is.hasItemMeta()) {

            final ItemMeta im = is.getItemMeta();
            if (im.hasDisplayName()) {
                res.append(spl).append("name:").append(TCUtil.deform(im.displayName()).replace('§', '&'));
            }

            if (im.hasLore()) {
                res.append(spl).append("lore:").append(im.lore().stream().map(TCUtil::deform).collect(Collectors.joining(":")));
                /*for (final Component lore :im.lore()) {
                    if (lore.isEmpty()) {
                        res=res+paramSplitter+"lore:&7";
                    } else {
                        res=res+paramSplitter+"lore:"+lore;
                    }
                }*/
            }

            if (im.hasCustomModelData()) {
                res.append(spl).append("model:").append(im.getCustomModelData());
            }

            if (!im.getItemFlags().isEmpty()) {
                res.append(spl).append("itemflag:").append(im.getItemFlags().stream().map(Enum::toString).collect(Collectors.joining(":")));
                /*for (ItemFlag itemFlag : im.getItemFlags()) {
                    res=res+paramSplitter+"itemflag:"+itemFlag.toString();
                }*/
            }

            if (im.isUnbreakable()) {
                res.append(spl).append("unbreakable");
            }

            if (im instanceof ArmorMeta am) {
                if (am.hasTrim()) {
                    final ArmorTrim trim = am.getTrim();
                    if (trim != null) {
                        final Key type = OStrap.keyOf(trim.getMaterial()),
                            patt = OStrap.keyOf(trim.getPattern());
                        if (type != null && patt != null) {
                            res.append(spl).append("trim:").append(type.value())
                                .append(":").append(patt.value());
                        }
                    }
                }

                if (im instanceof ColorableArmorMeta) {
                    final Color clr = ((ColorableArmorMeta) is.getItemMeta()).getColor();
                    res.append(spl).append("color:").append(clr.getRed()).append(":").append(clr.getGreen()).append(":").append(clr.getBlue());
                }
            } else if (is.getItemMeta() instanceof EnchantmentStorageMeta ebm) {
                //final EnchantmentStorageMeta ebm = enchantmentStorageMeta;
                if (ebm.hasStoredEnchants()) {
                    for (final Entry<Enchantment, Integer> en : ebm.getStoredEnchants().entrySet()) {
                        res.append(spl).append("bookenchant:").append(en.getKey().getKey().getKey()).append(":").append(en.getValue());
                    }
                }
            } else if (im instanceof SkullMeta skullMeta) {
                //final SkullMeta skullMeta = skullMeta;
                if (skullMeta.hasOwner()) {
                    res.append(spl).append("skull:").append(skullMeta.getOwningPlayer().getUniqueId().toString());
                }
            } else if (im instanceof PotionMeta pm) {
                //final PotionMeta pm = potionMeta;
                res.append(spl).append("basepot:").append(pm.getBasePotionType().toString().toLowerCase());

                if (pm.hasCustomEffects()) {
                    for (final PotionEffect cpe : pm.getCustomEffects()) {
                        res.append(spl).append("effect:").append(cpe.getType().key().value()).append(":")
                            .append(cpe.getDuration()).append(":").append(cpe.getAmplifier());
                    }
                }

                if (pm.hasColor()) {
                    res.append(spl).append("color:").append(pm.getColor().getBlue()).append(":")
                        .append(pm.getColor().getGreen()).append(":").append(pm.getColor().getRed());
                }
            } else if (im instanceof FireworkMeta fm) {
                //final PotionMeta pm = potionMeta;
                res.append(spl).append("firework:").append(fm.getPower());

                for (final FireworkEffect fe : fm.getEffects()) {
                    final List<Color> cls = fe.getColors();
                    final List<Color> fds = fe.getFadeColors();
                    res.append(spl).append("burst:").append(fe.getType().name()).append(":")
                        .append(cls.isEmpty() ? Color.WHITE.asRGB() : cls.getFirst().asRGB()).append(":")
                        .append(fds.isEmpty() ? Color.WHITE.asRGB() : fds.getFirst().asRGB()).append(":")
                        .append(fe.hasFlicker()).append(":").append(fe.hasTrail());
                }
            } else if (im instanceof FireworkEffectMeta fm) {
                //final PotionMeta pm = potionMeta;
                final FireworkEffect fe = fm.getEffect();
                if (fe != null) {
                    final List<Color> cls = fe.getColors();
                    final List<Color> fds = fe.getFadeColors();
                    res.append(spl).append("burst:").append(fe.getType().name()).append(":")
                        .append(cls.isEmpty() ? Color.WHITE.asRGB() : cls.getFirst().asRGB()).append(":")
                        .append(fds.isEmpty() ? Color.WHITE.asRGB() : fds.getFirst().asRGB()).append(":")
                        .append(fe.hasFlicker()).append(":").append(fe.hasTrail());
                }
            }

            if (im.hasAttributeModifiers()) {
                for (final Entry<Attribute, AttributeModifier> en : im.getAttributeModifiers().entries()) {
                    final AttributeModifier am = en.getValue();
                    res.append(spl).append("attribute:").append(en.getKey().toString())
                        .append(":").append(am.getAmount()).append(":")
                        .append(am.getOperation().ordinal()).append(":")
                        .append(am.getSlotGroup().toString());
                }
            }
        }

        if (!is.getEnchantments().isEmpty()) {
            for (final Entry<Enchantment, Integer> en : is.getEnchantments().entrySet()) {
                res.append(spl).append("enchant:").append(en.getKey().getKey().getKey()).append(":").append(en.getValue());
            }
        }

        return res.toString();
    }

    @Deprecated
    public static ItemStack parseItem(final String asString, final String splitter) {

        if (asString == null || asString.isBlank()) {
            Ostrov.log_warn("Декодер предмета : §7строка >§f" + asString + "§7< ошибочная!");
            return setName(new ItemStack(Material.BEDROCK), "§cСтрока для декодирования ошибочная!");
        }

        final String spl = splitter.equals(":") ? " : " : splitter;

        if (splitter.isBlank()) {
            Ostrov.log_warn("Декодер предмета : §7строка >§f" + asString + "§7<, Разделитель не может быть пробелом!");
            return setName(new ItemStack(Material.BEDROCK), "§cРазделитель для декодирования ошибочный!");
        }

        //if (!asString.contains(splitter)) { //простой декодер для 50% случаев где просто материал и кол-во вроде cactus:1
        //    return
        //}

        final List<String> paramAndArg = new ArrayList<>();

        for (String param : asString.split(spl)) {
            if (!param.isBlank()) {
                paramAndArg.add(param.trim());
            }
        }

        if (paramAndArg.isEmpty()) {
            Ostrov.log_warn("Декодер предмета : §7строка >§f" + asString + "§7<, Не найдено никаких параметров!");
            return setName(new ItemStack(Material.BEDROCK), "§cНе найдено никаких параметров!");
        }

//System.out.println("--- paramAndArg.size="+paramAndArg.size()+" 0="+paramAndArg.get(0));
        final @Nullable ItemType mat;
        int amount = 1;

        final String first = paramAndArg.getFirst().trim();
        int idx = first.indexOf(":");

        if (idx > 0) { //с колличеством
            mat = Ostrov.registries.ITEMS.get(Key.key(first.substring(0, idx)));//mat = Material.matchMaterial(first.substring(0, idx));
            try {
                amount = Integer.parseInt(first.substring(idx + 1));
                if (amount < 1) {
                    amount = 1;
                    Ostrov.log_warn("Декодер предмета :  колличество меньше 1 : §f" + first);
                } else if (amount > mat.getMaxStackSize()) {
                    amount = mat.getMaxStackSize();
                    Ostrov.log_warn("Декодер предмета : неправильное колличество §f" + first + " §7(max=" + mat.getMaxStackSize() + ")");
                }
            } catch (NumberFormatException | StringIndexOutOfBoundsException ex) {
                Ostrov.log_warn("Декодер предмета - неправильное колличество §f" + first);
            }
        } else {
            mat = Ostrov.registries.ITEMS.get(Key.key(first));//mat = Material.matchMaterial(first);
        }

        if (mat == null) {
            Ostrov.log_warn("Декодер предмета : §7строка >§f" + asString + "§7<, нет материала §f" + first);
            return setName(new ItemStack(Material.BEDROCK), "Декодер предмета : §7строка >§f" + asString + "§7<, нет материала §f" + first);
        }

        //final ItemBuilder builder;
       /* if (paramAndArg.getFirst().contains(":")) { //если с колличеством
            String[] s0 = paramAndArg.getFirst().trim().split(":");
            mat = Material.matchMaterial(s0[0].trim());
            if (mat != null) {
                builder.type(mat);
                if (NumUtils.isInt(s0[1].trim())) {
                builder = new ItemBuilder(mat);//builder.type(mat);
                if (ApiOstrov.isInteger(s0[1].trim())) {
                    builder.amount(Integer.parseInt(s0[1].trim()));
                } else {
                    Ostrov.log_warn("Декодер предмета : §7строка >§f" + asString + "§7<, неправильное колличество §f" + s0[1]);
                }
            } else {
                Ostrov.log_warn("Декодер предмета : §7строка >§f" + asString + "§7<, нет материала §f" + s0[0]);
                return  setName(new ItemStack(Material.BEDROCK),"Декодер предмета : §7строка >§f" + asString + "§7<, нет материала §f" + s0[0]);
            }
        } else {
            mat = Material.matchMaterial(paramAndArg.getFirst().trim());
            if (mat != null) {
                builder = new ItemBuilder(mat);//builder.type(mat);
            } else {
                Ostrov.log_warn("Декодер предмета : §7строка >§f" + asString + "§7<, нет материала §f" + paramAndArg.getFirst());
            }
        }*/
        if (paramAndArg.size() == 1) { //нет ничего кроме материала и колл-ва
            return mat.createItemStack(amount);//new ItemStack(mat., amount);//builder.build();
        }

//System.out.println("2 itemstack="+itemstack);
        Component name = null;
        List<Component> lore = null;
        ItemBuilder builder = null;

        // строка разбитая на характеристики -> name:nnn lore:sdsds:sdsd enchant:ARROW_DAMAGE:1 dye:RED<>end
        String raw, param, arg = null;
        String[] subArg;

        for (int i = 1; i < paramAndArg.size(); ++i) { //первый-материаал, пропускаем
            raw = paramAndArg.get(i);
            idx = raw.indexOf(":");

            //@Subst("") final String[] param = paramAndArg.get(i).trim().split(":");
            if (idx < 0) {// только один параметрр, без :  if (param.length == 1) {
                param = raw.trim().toLowerCase();
                switch (param) { //param[0].trim().toLowerCase()) {
                    case "end", "unbreakable" -> { //работают без аргументов
                    }
                    default -> {
                        Ostrov.log_warn("Декодер предмета : §7строка >§f" + asString + "§7<, пустой параметр §f" + paramAndArg.get(i));
                        continue;
                    }
                }
            } else {
                param = raw.substring(0, idx).trim().toLowerCase();
                arg = raw.substring(idx + 1);
            }

            try {
                //сюда придёт только то, где есть аргументы
                switch (param) { //(param[0].trim().toLowerCase()) {

                    case "name" ->
                        //if (param.length == 2) {
                        //builder.name(param[1].replace('&', '§'));
                        name = TCUtil.form(arg.replace('&', '§')); //param[1].replace('&', '§');
                    //} else {
                    //    Ostrov.log_warn("Декодер name : §7строка >§f" + asString + "§7<, неверные параметры §f" + param[1].toUpperCase());
                    //}

                    case "lore" -> {
                        if (lore == null) lore = new ArrayList<>();
                        subArg = arg.split(":");
                        //final List<Component> lrs = new ArrayList<>();
                        for (String s : subArg) {
                            //int j = 1; j < param.length; j++) {
                            lore.add(TCUtil.form(s.replace('&', '§')));//lrs.add(TCUtil.form(param[j].replace('&', '§')));
                        }
                        //builder.lore(lrs);
                        //builder.addLore(paramAndArg.get(i).trim().replaceFirst("lore:", "").replaceAll("&", "§"));
                    }

                    case "color" -> {
                        if (builder == null) builder = new ItemBuilder(mat);
                        subArg = arg.split(":");
                        //if (subArg.length == 3) {
                        try {
                            builder.color(Color.fromRGB(Integer.parseInt(subArg[0]), Integer.parseInt(subArg[1]), Integer.parseInt(subArg[2])));
                        } catch (NullPointerException | NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                            Ostrov.log_warn("Декодер " + param + " : §7строка >§f" + asString + "§7<, должны быть числа §f" + arg);
                        }
                        //} else {
                        //  Ostrov.log_warn("Декодер " + param + " : §7строка >§f" + asString + "§7<, неверные параметры §f" + arg);
                        //}
                    }

                    case "model" -> {//, "custommodeldata" -> {
                        //if (param.length == 2) {
                        if (builder == null) builder = new ItemBuilder(mat);
                        try {
                            //builder.modelData(Integer.parseInt(arg));
                            builder.model(Key.key(arg));
                        } catch (NullPointerException | NumberFormatException ex) {
                            Ostrov.log_warn("Декодер custommodeldata : §7строка >§f" + asString + "§7<, должны быть числа §f" + arg);
                        }
                        //if (ApiOstrov.isInteger(arg)) {//param[1])) {
                        //    int modelData = Integer.parseInt(param[1]);
                        //    if (modelData < 0) {
                        //        modelData = 0;
                        //    }
                        //    builder.modelData(modelData);
                        //} else {
                        //     Ostrov.log_warn("Декодер model : §7строка >§f" + asString + "§7<, должны быть числа §f" + param[1]);
                        //}
                        //} else {
                        //     Ostrov.log_warn("Декодер model : §7строка >§f" + asString + "§7<, неверные параметры §f" + param[1].toUpperCase());
                        //}
                    }

                    case "itemflag" -> {
                        if (builder == null) builder = new ItemBuilder(mat);
                        subArg = arg.split(":");
                        //if (param.length > 1) {
                        for (String s : subArg) {//for (int j = 1; j < param.length; j++) {
                            try {
                                final ItemFlag itemFlag = ItemFlag.valueOf(s);
                                builder.flags(true, itemFlag);
                            } catch (NullPointerException | IllegalArgumentException ex) {
                                Ostrov.log_warn("Декодер itemflag : §7строка >§f" + asString + "§7<, нет такого флага §f" + s);
                            }
                            //final ItemFlag itemFlag = ItemFlag.valueOf(param[j]);
                            //if (itemFlag == null) {
                            //     Ostrov.log_warn("Декодер itemflag : §7строка >§f" + asString + "§7<, нет такого флага §f" + param[j]);
                            //     continue;
                            //}
                            //builder.flags(itemFlag);
                        }
                        //} else {
                        //    Ostrov.log_warn("Декодер itemflag : §7строка >§f" + asString + "§7<, неверные параметры §f" + param[1].toUpperCase());
                        //}
                    }

                    case "unbreakable" -> {
                        if (builder == null) builder = new ItemBuilder(mat);
                        builder.unbreak(true);
                    }

                    case "attribute" -> {
                        if (builder == null) builder = new ItemBuilder(mat);
                        subArg = arg.split(":");
                        //if (subArg.length == 4) {//if (param.length == 5) {
                        try {
                            final Attribute at = ATTR_REG.get(Key.key(subArg[0]));//Attribute.valueOf(subArg[0]);
                            final double mod = Double.parseDouble(subArg[1]);
                            final int op = Integer.parseInt(subArg[2]);
                            final EquipmentSlotGroup esg = EquipmentSlotGroup.getByName(subArg[3]);
                            builder.attribute(at, mod, Operation.values()[op], esg == null ? EquipmentSlotGroup.ANY : esg);
                        } catch (NullPointerException | IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
                            Ostrov.log_warn("Декодер attribute : §7строка >§f" + asString + "§7<, неверные числа §f");
                        }
                        //builder.attribute(Attribute.valueOf(param[1]), mod, Operation.values()[op],
                        //param[4].equals("ANY") ? null : EquipmentSlot.valueOf(param[4]));
                        //}
                    }

                    case "skulltexture" -> {
                        if (builder == null) builder = new ItemBuilder(mat);
                        //if (param.length == 2) {
                        builder.headTexture(arg);
                        //} else {
                        //  Ostrov.log_warn("Декодер skulltexture : §7строка >§f" + asString + "§7<, неверные параметры §f" + param[1].toUpperCase());
                        //}
                    }
                    case "skull", "skullowneruuid" ->
                        Ostrov.log_warn("Декодер skullowneruuid : с uuid больше не работает, нужно переделать на skulltexture!" + asString);

                    //enchant:silk_touch:1
                    case "enchant", "bookenchant" -> {
                        if (builder == null) builder = new ItemBuilder(mat);
                        subArg = arg.split(":");
                        if (subArg.length == 2) {
                            final Enchantment enchant = OStrap.retrieve(RegistryKey.ENCHANTMENT, Key.key(subArg[0]));
                            if (enchant != null) {
                              //builder.enchant(enchant, NumUtil.intOf(subArg[1], 1));
                              builder.enchant(enchant, NumUtil.intOf(subArg[1], 1), mat == ItemType.ENCHANTED_BOOK); //фикс-кривые книги на починку скайблок
                            } else {
                                Ostrov.log_warn("Декодер enchant : §7строка >§f" + asString + "§7<, нет таких чар §f" + arg);
                            }
                        } else {
                            Ostrov.log_warn("Декодер enchant : §7строка >§f" + asString + "§7<, неверные параметры §f" + arg);
                        }
                    }
                    case "basepot", "basepotiondata" -> {
                        if (builder == null) builder = new ItemBuilder(mat);
                        subArg = arg.split(":");
                        if (subArg.length == 1 || subArg.length == 3) {
                            if (POTION.contains(mat)) {
                                PotionType potionType = Registry.POTION.get(NamespacedKey.minecraft(subArg[0]));
                                if (potionType == null) {
                                    potionType = PotionType.getByEffect(PotionEffectType.getByName(subArg[0]));
                                }
                                if (potionType != null) {
                                    builder.basePotion(potionType);
                                } else {
                                    Ostrov.log_warn("Декодер basepot : §7строка >§f" + asString + "§7<, нет PotionType §f" + arg);
                                }
                            } else {
                                Ostrov.log_warn("Декодер basepot : §7строка >§f" + asString + "§7<, неприменима к §f" + mat);
                            }
                        } else {
                            Ostrov.log_warn("Декодер basepot : §7строка >§f" + asString + "§7<, неверные параметры §f" + arg);
                        }
                    }

                    case "effect", "custompotioneffect" -> {
                        if (builder == null) builder = new ItemBuilder(mat);
                        subArg = arg.split(":");
                        if (subArg.length == 3) {
                            if (POTION.contains(mat)) {
                                PotionEffectType potionEffectType = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(subArg[0]));
                                if (potionEffectType == null) {
                                    potionEffectType = PotionEffectType.getByName(subArg[0]);
                                }
                                if (potionEffectType != null) {
                                    if (NumUtil.isInt(subArg[1]) && NumUtil.isInt(subArg[2])) {
                                        builder.potEffect(new PotionEffect(potionEffectType,
                                            Integer.parseInt(subArg[1]), Integer.parseInt(subArg[2])));
                                    } else {
                                        Ostrov.log_warn("Декодер effect : §7строка >§f" + asString + "§7<, должны быть числа §f" + arg);
                                    }
                                } else {
                                    Ostrov.log_warn("Декодер effect : §7строка >§f" + asString + "§7<, нет PotionType §f" + arg);
                                }
                            } else {
                                Ostrov.log_warn("Декодер effect : §7строка >§f" + asString + "§7<, неприменима к §f" + mat);
                            }
                        } else {
                            Ostrov.log_warn("Декодер effect : §7строка >§f" + asString + "§7<, неверные параметры §f" + arg);
                        }
                    }

                    case "trim" -> {
                        if (builder == null) builder = new ItemBuilder(mat);
                        subArg = arg.split(":");
                        if (subArg.length == 2) {
                            builder.trim(OStrap.retrieve(RegistryKey.TRIM_MATERIAL, NamespacedKey.minecraft(subArg[0])),
                                OStrap.retrieve(RegistryKey.TRIM_PATTERN, NamespacedKey.minecraft(subArg[1])));
                        } else {
                            Ostrov.log_warn("Декодер trim : §7строка >§f" + asString + "§7<, неверные параметры §f" + arg);
                        }
                    }

                    case "firework" -> {
                        if (builder == null) builder = new ItemBuilder(mat);
                        //if (param.length == 2) {
                        if (ItemType.FIREWORK_ROCKET.equals(mat)) {//if (builder.type().equals(ItemType.FIREWORK_ROCKET)) {
                            try {
                                builder.fireFlight(NumUtil.intOf(arg, 1));
                                //final String s = arg;
                                //builder.customMeta(FireworkMeta.class, fm -> fm.setPower(Integer.parseInt(s)));
                            } catch (NumberFormatException ex) {
                                Ostrov.log_warn("Декодер firework : §7строка >§f" + asString
                                    + "§7<, неприменима к §f" + mat);
                            }
                        } else {
                            Ostrov.log_warn("Декодер firework : §7строка >§f" + asString + "§7<, может быть только FIREWORK_ROCKET");
                        }
                        //} else {
                        //  Ostrov.log_warn("Декодер firework : §7строка >§f" + asString + "§7<, неверные параметры §f" + param[1]);
                        //}
                    }

                    case "burst" -> {
                        if (builder == null) builder = new ItemBuilder(mat);
                        final String[] s = arg.split(":");
                        if (s.length == 5) {
                            if (ItemType.FIREWORK_ROCKET.equals(mat) || ItemType.FIREWORK_STAR.equals(mat)) {
                                builder.fireEffect(FireworkEffect.builder()
                                    .with(FireworkEffect.Type.valueOf(s[0]))
                                    .withColor(Color.fromRGB(Integer.parseInt(s[1])))
                                    .withFade(Color.fromRGB(Integer.parseInt(s[2])))
                                    .flicker(Boolean.parseBoolean(s[3]))
                                    .trail(Boolean.parseBoolean(s[4])).build());
                            } else {
                                Ostrov.log_warn("Декодер burst : §7строка >§f" + asString
                                    + "§7<, неприменима к §f" + mat);
                            }
                            /*if (builder.type().equals(ItemType.FIREWORK_ROCKET)) {
                                builder.customMeta(FireworkMeta.class, fm -> fm.addEffect(FireworkEffect.builder()
                                    .with(FireworkEffect.Type.valueOf(s[0])).withColor(Color.fromRGB(Integer.parseInt(s[1])))
                                    .withFade(Color.fromRGB(Integer.parseInt(s[2]))).flicker(Boolean.parseBoolean(s[3]))
                                    .trail(Boolean.parseBoolean(s[4])).build()));
                            } else if (builder.type().equals(ItemType.FIREWORK_STAR)) {
                                builder.customMeta(FireworkEffectMeta.class, fm -> fm.setEffect(FireworkEffect.builder()
                                    .with(FireworkEffect.Type.valueOf(s[0])).withColor(Color.fromRGB(Integer.parseInt(s[1])))
                                    .withFade(Color.fromRGB(Integer.parseInt(s[2]))).flicker(Boolean.parseBoolean(s[3]))
                                    .trail(Boolean.parseBoolean(s[4])).build()));
                            } else {
                                Ostrov.log_warn("Декодер burst : §7строка >§f" + asString
                                    + "§7<, неприменима к §f" + builder.type().key().value());
                            }*/
                        } else {
                            Ostrov.log_warn("Декодер burst : §7строка >§f" + asString + "§7<, неверные параметры §f" + arg);
                        }
                    }

                    default ->
                        Ostrov.log_warn("Декодер ОБЩИЙ : §7строка >§f" + asString + "§7<, параметр не распознан §f" + arg);
                }

            } catch (IllegalArgumentException | SecurityException | NullPointerException ex) {
                Ostrov.log_err("parseItem : " + asString + " - " + ex.getMessage());
            }

        }
        if (builder == null) { //не было ничего, кроме имени и лор - наверное, 90%случаев
            final ItemStack is = mat.createItemStack(amount);//new ItemStack(mat, amount);
            final ItemMeta im = is.getItemMeta();
            if (name != null) {
                im.displayName(name);
            }
            if (lore != null) {
                im.lore(lore);
            }
            is.setItemMeta(im);
            return is;
        } else {
            builder.amount(amount);
            if (name != null) {
                builder.name(name);
            }
            if (lore != null) {
                builder.lore(lore);
            }
            return builder.build();
        }
    }

    public static boolean compare(@Nullable final ItemStack is1, @Nullable final ItemStack is2, final Stat... depth) {
        if (is1 == null || is2 == null) return is1 == is2;

        for (final Stat s : depth) {
            if (!testStat(is1, is2, s)) return false;
        }
        return true;
    }

    public enum Stat {TYPE, AMOUNT, NAME, LORE, DAMAGE, MODEL, PDC}

    private static boolean testStat(final ItemStack is1, final ItemStack is2, final Stat s) {
        return switch (s) {
            case AMOUNT -> is1.getAmount() == is2.getAmount();
            case TYPE -> is1.getType().asItemType().equals(is2.getType().asItemType());
            case DAMAGE -> Objects.equals(is1.getData(DataComponentTypes.DAMAGE), is2.getData(DataComponentTypes.DAMAGE))
                && Objects.equals(is1.getData(DataComponentTypes.MAX_DAMAGE), is2.getData(DataComponentTypes.MAX_DAMAGE));
            case NAME -> TCUtil.compare(is1.getData(DataComponentTypes.ITEM_NAME), is2.getData(DataComponentTypes.ITEM_NAME))
                && TCUtil.compare(is1.getData(DataComponentTypes.CUSTOM_NAME), is2.getData(DataComponentTypes.CUSTOM_NAME));
            case MODEL -> Objects.equals(is1.getData(DataComponentTypes.ITEM_MODEL), is2.getData(DataComponentTypes.ITEM_MODEL));
            case LORE -> {
                final ItemLore lore1 = is1.getData(DataComponentTypes.LORE);
                final ItemLore lore2 = is2.getData(DataComponentTypes.LORE);
                if (lore1 == null || lore2 == null)
                    if (!Objects.equals(lore1, lore2)) yield false;

                final List<Component> l1 = lore1.lines();
                final List<Component> l2 = lore2.lines();
                final int size = l1.size();
                if (size != l2.size()) yield false;
                for (int i = 0; i != size; i++) {
                    if (!TCUtil.compare(l1.get(i), l2.get(i)))
                        yield false;
                }
                yield true;
            }
            case PDC -> {
                final PersistentDataContainerView pdc1 = is1.getPersistentDataContainer();
                final PersistentDataContainerView pdc2 = is2.getPersistentDataContainer();
                final Set<NamespacedKey> keys = new HashSet<>();
                keys.addAll(pdc1.getKeys()); keys.addAll(pdc1.getKeys());
                for (final NamespacedKey k : keys) {
                    if (!Objects.equals(pdc1.get(k, PersistentDataType.STRING),
                        pdc2.get(k, PersistentDataType.STRING))) yield false;
                }
                yield true;
            }
        };
    }

    @Deprecated
    public static boolean compareItem(@Nullable final ItemStack is1, @Nullable final ItemStack is2, final boolean checkLore) {
        return checkLore ? compare(is1, is2, Stat.TYPE, Stat.NAME, Stat.LORE) : compare(is1, is2, Stat.TYPE, Stat.NAME);
    }

    public static void fillSign(final Sign sign, String suggest) {
        if (suggest == null || suggest.isEmpty()) {
            return;
        }
        final SignSide sd = sign.getSide(Side.FRONT);
        for (int ln = 0; !suggest.isEmpty() && ln < 4; ln++) {
            if (suggest.length() > 15) {
                sd.line(ln, TCUtil.form(suggest.substring(0, 15)));
                suggest = suggest.substring(15);
                continue;
            }

            sd.line(ln, TCUtil.form(suggest));
            break;
        }
        sign.update();
    }

    public static double getTrimMod(final ItemStack ti, final Attribute atr) {
        if (ti == null) return 0d;

        switch (ti.getType()) {
            case IRON_INGOT -> {//more defense, less mobility
                if (atr.equals(ARMOR)) return 0.2d;
                else if (atr.equals(ARMOR_TOUGHNESS)) return 0.1d;
                else if (atr.equals(MOVEMENT_SPEED)) return -0.1d;
                else if (atr.equals(WATER_MOVEMENT_EFFICIENCY)) return -0.2d;
            }
            case COPPER_INGOT -> {//more kick, less mining
                if (atr.equals(ATTACK_KNOCKBACK)) return 0.2d;
                else if (atr.equals(JUMP_STRENGTH)) return 0.1d;
                else if (atr.equals(BLOCK_BREAK_SPEED)) return -0.1d;
                else if (atr.equals(GRAVITY)) return -0.1d;
            }
            case GOLD_INGOT -> {//more health, less light
                if (atr.equals(MAX_HEALTH)) return 0.1d;
                else if (atr.equals(GRAVITY)) return 0.1d;
                else if (atr.equals(SNEAKING_SPEED)) return -0.1d;
                else if (atr.equals(BLOCK_BREAK_SPEED)) return 0.1d;
            }
            case AMETHYST_SHARD -> {//more attack, less defense
                if (atr.equals(ATTACK_DAMAGE)) return 0.1d;
                else if (atr.equals(ARMOR)) return -0.2d;
                else if (atr.equals(BLOCK_INTERACTION_RANGE)) return 0.1d;
                else if (atr.equals(ENTITY_INTERACTION_RANGE)) return 0.1d;
            }
            case DIAMOND -> {//buffs armor and damage
                if (atr.equals(ARMOR)) return 0.05d;
                else if (atr.equals(ARMOR_TOUGHNESS)) return 0.1d;
                else if (atr.equals(ATTACK_DAMAGE)) return 0.05d;
                else if (atr.equals(KNOCKBACK_RESISTANCE)) return -0.1d;
            }
            case EMERALD -> {//more mobility, less damage
                if (atr.equals(MOVEMENT_SPEED)) return 0.1d;
                else if (atr.equals(ARMOR_TOUGHNESS)) return 0.1d;
                else if (atr.equals(ATTACK_DAMAGE)) return -0.1d;
                else if (atr.equals(JUMP_STRENGTH)) return 0.1d;
            }
            case REDSTONE -> {//more bulk, less kick
                if (atr.equals(MAX_HEALTH)) return 0.1d;
                else if (atr.equals(SCALE)) return 0.1d;
                else if (atr.equals(WATER_MOVEMENT_EFFICIENCY)) return -0.1d;
                else if (atr.equals(JUMP_STRENGTH)) return -0.1d;
            }
            case LAPIS_LAZULI -> {//more mobility, less swing
                if (atr.equals(SNEAKING_SPEED)) return 0.1d;
                else if (atr.equals(WATER_MOVEMENT_EFFICIENCY)) return 0.2d;
                else if (atr.equals(ATTACK_SPEED)) return -0.1d;
                else if (atr.equals(GRAVITY)) return -0.1d;
            }
            case NETHERITE_INGOT -> {//more toughness, less hp
                if (atr.equals(ARMOR)) return 0.1d;
                else if (atr.equals(ARMOR_TOUGHNESS)) return 0.4d;
                else if (atr.equals(MAX_HEALTH)) return -0.1d;
                else if (atr.equals(WATER_MOVEMENT_EFFICIENCY)) return -0.1d;
            }
            case QUARTZ -> {//more damage, less haste
                if (atr.equals(ATTACK_DAMAGE)) return 0.2d;
                else if (atr.equals(ARMOR_TOUGHNESS)) return -0.1d;
                else if (atr.equals(ATTACK_SPEED)) return -0.1d;
                else if (atr.equals(SNEAKING_SPEED)) return -0.1d;
            }
        }
        return 0d;
    }


    public static ItemBuilder buildBiomeIcon(final Biome b) {
        final ItemType mat;
        if (b.toString().equalsIgnoreCase("NETHER") || b.toString().equalsIgnoreCase("NETHER_WASTES")) {
            mat = ItemType.NETHERRACK;
        } else {
            //TODO потом перепишу
            switch (b.key().value().toUpperCase()) {
                case "BADLANDS" -> mat = ItemType.RED_SAND;
                case "BAMBOO_JUNGLE" -> mat = ItemType.BAMBOO;
                case "BEACH" -> mat = ItemType.HORN_CORAL_FAN;
                case "BIRCH_FOREST" -> mat = ItemType.BIRCH_LOG;
                case "COLD_OCEAN" -> mat = ItemType.BLUE_CONCRETE_POWDER;
                case "DARK_FOREST" -> mat = ItemType.DARK_OAK_LOG;
                case "MUSHROOM_FIELDS" -> mat = ItemType.MYCELIUM;
                case "DEEP_COLD_OCEAN" -> mat = ItemType.BLUE_CONCRETE;
                case "DEEP_FROZEN_OCEAN" -> mat = ItemType.BLUE_ICE;
                case "DEEP_LUKEWARM_OCEAN" -> mat = ItemType.LIGHT_BLUE_CONCRETE;
                case "DEEP_OCEAN" -> mat = ItemType.BLUE_WOOL;
                case "DESERT" -> mat = ItemType.SAND;
                case "END_BARRENS" -> mat = ItemType.END_STONE;
                case "END_HIGHLANDS" -> mat = ItemType.END_STONE_BRICKS;
                case "END_MIDLANDS" -> mat = ItemType.END_STONE_BRICK_WALL;
                case "ERODED_BADLANDS" -> mat = ItemType.DEAD_BUSH;
                case "FLOWER_FOREST" -> mat = ItemType.ROSE_BUSH;
                case "WINDSWEPT_HILLS" -> mat = ItemType.GRANITE;
                case "FOREST" -> mat = ItemType.OAK_SAPLING;
                case "FROZEN_OCEAN" -> mat = ItemType.PACKED_ICE;
                case "FROZEN_RIVER" -> mat = ItemType.LIGHT_BLUE_DYE;
                case "ICE_SPIKES" -> mat = ItemType.ICE;
                case "JUNGLE" -> mat = ItemType.JUNGLE_LOG;
                case "LUKEWARM_OCEAN" -> mat = ItemType.LIGHT_BLUE_CONCRETE_POWDER;
                case "OCEAN" -> mat = ItemType.WATER_BUCKET;
                case "PLAINS" -> mat = ItemType.GRASS_BLOCK;
                case "MANGROVE_SWAMP" -> mat = ItemType.MANGROVE_ROOTS;
                case "RIVER" -> mat = ItemType.BLUE_DYE;
                case "SAVANNA" -> mat = ItemType.SPONGE;
                case "SAVANNA_PLATEAU" -> mat = ItemType.ACACIA_WOOD;
                case "SMALL_END_ISLANDS" -> mat = ItemType.END_STONE_BRICK_SLAB;
                case "SNOWY_BEACH" -> mat = ItemType.SNOW;
                case "SNOWY_TAIGA" -> mat = ItemType.WHITE_WOOL;
                case "SUNFLOWER_PLAINS" -> mat = ItemType.SUNFLOWER;
                case "SWAMP" -> mat = ItemType.LILY_PAD;
                case "TAIGA" -> mat = ItemType.SPRUCE_LOG;
                case "NETHER_WASTES" -> mat = ItemType.NETHERRACK;
                case "THE_END" -> mat = ItemType.END_PORTAL_FRAME;
                case "THE_VOID" -> mat = ItemType.RESPAWN_ANCHOR;
                case "WARM_OCEAN" -> mat = ItemType.CYAN_CONCRETE_POWDER;
                case "SNOWY_PLAINS" -> mat = ItemType.SNOW;
                case "SPARSE_JUNGLE" -> mat = ItemType.VINE;
                case "STONY_SHORE" -> mat = ItemType.GRAVEL;
                case "OLD_GROWTH_PINE_TAIGA" -> mat = ItemType.SPRUCE_WOOD;
                case "WINDSWEPT_FOREST" -> mat = ItemType.STRIPPED_OAK_LOG;
                case "WOODED_BADLANDS" -> mat = ItemType.DEAD_BUSH;
                case "WINDSWEPT_GRAVELLY_HILLS" -> mat = ItemType.ANDESITE;
                case "OLD_GROWTH_BIRCH_FOREST" -> mat = ItemType.BIRCH_WOOD;
                case "OLD_GROWTH_SPRUCE_TAIGA" -> mat = ItemType.STRIPPED_SPRUCE_LOG;
                case "WINDSWEPT_SAVANNA" -> mat = ItemType.STRIPPED_ACACIA_LOG;
                case "SOUL_SAND_VALLEY" -> mat = ItemType.SOUL_SAND;
                case "CRIMSON_FOREST" -> mat = ItemType.CRIMSON_NYLIUM;
                case "WARPED_FOREST" -> mat = ItemType.WARPED_NYLIUM;
                case "BASALT_DELTAS" -> mat = ItemType.BASALT;
                case "DRIPSTONE_CAVES" -> mat = ItemType.DRIPSTONE_BLOCK;
                case "LUSH_CAVES" -> mat = ItemType.BIG_DRIPLEAF;
                case "DEEP_DARK" -> mat = ItemType.SCULK_CATALYST;
                case "MEADOW" -> mat = ItemType.BEE_NEST;
                case "GROVE" -> mat = ItemType.DIRT_PATH;
                case "SNOWY_SLOPES" -> mat = ItemType.ECHO_SHARD;
                case "FROZEN_PEAKS" -> mat = ItemType.PACKED_ICE;
                case "JAGGED_PEAKS" -> mat = ItemType.DIORITE;
                case "STONY_PEAKS" -> mat = ItemType.STONE;
                case "CHERRY_GROVE" -> mat = ItemType.CHERRY_LOG;
                case "CUSTOM" -> mat = ItemType.CRIMSON_NYLIUM;
                default -> mat = ItemType.WARPED_NYLIUM;
            }
        }

        return new ItemBuilder(mat)
            .name(Lang.t(b, Lang.RU));
    }

    public static ItemBuilder buildEntityIcon(final EntityType type) {
        ItemType it = null;
        for (final ItemType it2 : Ostrov.registries.ITEMS) {
            if (it2.getKey().value().equalsIgnoreCase(type.name())) {
                it = it2;
                break;
            }
        }
        if (it == null) {
            switch (type) {
                case UNKNOWN -> { // UNKNOWN entities do not have translation keys !
                    return new ItemBuilder(ItemType.DRIED_KELP_BLOCK).name("Неизвестный тип");
                }
                case ARMOR_STAND -> it = ItemType.ARMOR_STAND;
                case ZOMBIE -> it = ItemType.ZOMBIE_HEAD;
                case CREEPER -> it = ItemType.CREEPER_HEAD;
                case PIGLIN -> it = ItemType.PIGLIN_HEAD;
                case ENDER_DRAGON -> it = ItemType.DRAGON_HEAD;
                //case ENDER_DRAGON -> it = ItemType.DRAGON_HEAD;
                //case  -> builder.setCustomHeadTexture("6d865aae2746a9b8e9a4fe629fb08d18d0a9251e5ccbe5fa7051f53eab9b94");
                default -> it = ItemType.NAME_TAG;
                //не помню почему, но с яйцами чо-то было не так и спецом их не ставил
                //default -> Optional.of(Ostrov.registries.ITEMS
                //        .get(Key.key(type.key().value() + "_spawn_egg")))
                //    .orElse(ItemType.NAME_TAG);
            }
        }

        return new ItemBuilder(it).name(Lang.t(type, Lang.RU));
    }

    @Deprecated
    public static boolean isMineCart(final Material type) {
        return switch (type) {
            case MINECART, CHEST_MINECART, FURNACE_MINECART,
                 TNT_MINECART, HOPPER_MINECART, COMMAND_BLOCK_MINECART -> true;
            default -> false;
        };
    }

    @Deprecated
    public static boolean isSpawnEgg(final Material type) {
        //switch (type) { пока лень забивать енумы
        //   case EGG, : return true;
        //  default: return false;
        //}
      return type.name().endsWith("_SPAWN_EGG"); // EntityUtil typeFromEgg : DRAGON_EGG не конвертируется в EntityType!
    }

  @Deprecated
  public static boolean isInteractable(final Material mat) {
//    final BlockType bt = Registry.BLOCK.get(mat.getKey());??????????????????????????
    return mat.asBlockType().isInteractable();
  }
//MySQL Player Data Bridge
//https://www.spigotmc.org/resources/mysql-inventory-bridge.7849/

    public static String serialize(final ItemStack[] items) {
        for (int i = 0; i != items.length; i++) {
            if (MenuItemsManager.isSpecItem(items[i]))
                items[i] = ItemUtil.air;
        }
        return Base64Coder.encodeLines(ItemStack.serializeItemsAsBytes(items));
    }


  public static @Nullable ItemStack[] deserialize(final String base64) {
        try {
          return ItemStack.deserializeItemsFromBytes(Base64Coder.decodeLines(base64));
        } catch (RuntimeException e) {
            Ostrov.log_warn("Failed deserializing items, trying older method");
          return itemsFromBase64(base64);
        }
    }

    @Deprecated
    public static @Nullable ItemStack[] itemsFromBase64(final String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            //почему не ItemStack.deserializeItemsFromBytes()?
            ItemStack[] items = new ItemStack[dataInput.readInt()];
            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                final ItemStack is = (ItemStack) dataInput.readObject();
                if (MenuItemsManager.isSpecItem(is)) {
                    items[i] = ItemUtil.air;
                } else {
                    items[i] = is;
                }
            }

            dataInput.close();
            return items;

        } catch (IllegalArgumentException | ClassNotFoundException | IOException e) {
            Ostrov.log_err("Could not deserialize items - " + data);
            e.printStackTrace();
            return null;
        }
    }


    public static String seripotlize(Collection<PotionEffect> collection) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream boos = new ObjectOutputStream(baos);
            boos.writeInt(collection.size());

            for (final PotionEffect pe : collection) {
                boos.writeObject(pe.serialize());
            }

            boos.close();
            return Base64Coder.encodeLines(baos.toByteArray());
        } catch (IOException e) {
            Ostrov.log_err("potionEffectsToBase64 - " + e.getMessage());
            return "error";
        }
    }

    @SuppressWarnings("unchecked")
    public static @Nullable Collection<PotionEffect> deseripotlize(String s) {
        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(Base64Coder.decodeLines(s));
            final ObjectInputStream bois = new ObjectInputStream(bais);
            final PotionEffect[] apotioneffect = new PotionEffect[bois.readInt()];

            final ArrayList<PotionEffect> arraylist = new ArrayList<>();
            for (int i = 0; i < apotioneffect.length; ++i) {
                arraylist.add(new PotionEffect((Map<String, Object>) bois.readObject()));
            }

            bois.close();
            return arraylist;
        } catch (ClassCastException | ClassNotFoundException | IOException e) {
            Ostrov.log_err("potionEffectsFromBase64 - " + e.getMessage());
            return null;
        }
    }

}


/*
  // Only returns the id at the end of the url.
  // Example:
  // <a href="https://textures.minecraft.net/texture/cb50beab76e56472637c304a54b330780e278decb017707bf7604e484e4d6c9f">
  // https://textures.minecraft.net/texture/cb50beab76e56472637c304a54b330780e278decb017707bf7604e484e4d6c9f
  // </a>
  // Would return: cb50beab76e56472637c304a54b330780e278decb017707bf7604e484e4d6c9f
  public static String getSkinTextureUrlStripped(String value) {//@NotNull SkinProperty property) {
    //return getSkinProfileData(value).getTextures().getSKIN().getStrippedUrl();
    return getSkinProfileData(value).textures.SKIN.getStrippedUrl();
  }

  // Returns the decoded profile data from the profile property.
  // This is useful for getting the skin data from the property and other information like cape.
  // The user stored in this property may not be the same as the player who has the skin.
  // APIs like MineSkin use multiple shared accounts to generate these properties.
  // Or it could be the property of another player that the player set their skin to.
  public static MojangProfileResponse getSkinProfileData(String value) {//SkinProperty property) {
    //String decodedString = new String(Base64.getDecoder().decode(property.getValue()), StandardCharsets.UTF_8);
    String decodedString = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    return GSON.fromJson(decodedString, MojangProfileResponse.class);
  }



class MojangProfileResponse {
    public long timestamp;
    public String profileId;
    public String profileName;
    public boolean signatureRequired;
    public MojangProfileTextures textures;
  }
  class MojangProfileTextures {
    public MojangProfileTexture SKIN;
    public MojangProfileTexture CAPE;
  }
  class MojangProfileTexture {
    public static final Pattern URL_STRIP_PATTERN = Pattern.compile("^https?://textures\\.minecraft\\.net/texture/");
    public String url;
    public MojangProfileTextureMeta metadata;
    public String getStrippedUrl() {
      return URL_STRIP_PATTERN.matcher(url).replaceAll("");
    }
  }
  class MojangProfileTextureMeta {
    public String model;
  }
*/







/*
   //используют прятки
    public enum EnchantDecode {
        PROTECTION_ENVIRONMENTAL (Enchantment.PROTECTION_ENVIRONMENTAL, "protection"),
        PROTECTION_FIRE (Enchantment.PROTECTION_FIRE, "fire_protection"),
        PROTECTION_FALL (Enchantment.PROTECTION_FALL, "feather_falling"),
        PROTECTION_EXPLOSIONS (Enchantment.PROTECTION_EXPLOSIONS, "blast_protection"),
        PROTECTION_PROJECTILE (Enchantment.PROTECTION_PROJECTILE, "projectile_protection"),
        OXYGEN (Enchantment.OXYGEN, "respiration"),
        WATER_WORKER (Enchantment.WATER_WORKER, "aqua_affinity"),
        THORNS (Enchantment.THORNS, "thorns"),
        DEPTH_STRIDER (Enchantment.DEPTH_STRIDER, "depth_strider"),
        FROST_WALKER (Enchantment.FROST_WALKER, "frost_walker"),
        BINDING_CURSE (Enchantment.BINDING_CURSE, "binding_curse"),
        DAMAGE_ALL (Enchantment.DAMAGE_ALL, "sharpness"),
        DAMAGE_UNDEAD (Enchantment.DAMAGE_UNDEAD, "smite"),
        DAMAGE_ARTHROPODS (Enchantment.DAMAGE_ARTHROPODS, "bane_of_arthropods"),
        KNOCKBACK (Enchantment.KNOCKBACK, "knockback"),
        FIRE_ASPECT (Enchantment.FIRE_ASPECT, "fire_aspect"),
        LOOT_BONUS_MOBS (Enchantment.LOOT_BONUS_MOBS, "looting"),
        SWEEPING_EDGE (Enchantment.SWEEPING_EDGE, "sweeping"),
        DIG_SPEED (Enchantment.DIG_SPEED, "efficiency"),
        SILK_TOUCH (Enchantment.SILK_TOUCH, "silk_touch"),
        DURABILITY (Enchantment.DURABILITY, "unbreaking"),
        LOOT_BONUS_BLOCKS (Enchantment.LOOT_BONUS_BLOCKS, "fortune"),
        ARROW_DAMAGE (Enchantment.ARROW_DAMAGE, "power"),
        ARROW_KNOCKBACK (Enchantment.ARROW_KNOCKBACK, "punch"),
        ARROW_FIRE (Enchantment.ARROW_FIRE, "flame"),
        ARROW_INFINITE (Enchantment.ARROW_INFINITE, "infinity"),
        LUCK (Enchantment.LUCK, "luck_of_the_sea"),
        LURE (Enchantment.LURE, "lure"),
        LOYALTY (Enchantment.LOYALTY, "loyalty"),
        IMPALING (Enchantment.IMPALING, "impaling"),
        RIPTIDE (Enchantment.RIPTIDE, "riptide"),
        CHANNELING (Enchantment.CHANNELING, "channeling"),
        MULTISHOT (Enchantment.MULTISHOT, "multishot"),
        QUICK_CHARGE (Enchantment.QUICK_CHARGE, "quick_charge"),
        PIERCING (Enchantment.PIERCING, "piercing"),
        MENDING (Enchantment.MENDING, "mending"),
        VANISHING_CURSE (Enchantment.VANISHING_CURSE, "vanishing_curse"),
        ;

        public final Enchantment enchantment;
        public final String key;

        private EnchantDecode (Enchantment enchantment, String key) {
            this.enchantment = enchantment;
            this.key = key;
        }


        public static Enchantment fromEnchantmentName (final String name) {
            if (name==null || name.isEmpty()) return null;
            for (EnchantDecode ed : EnchantDecode.values()) {
                if (ed.toString().equalsIgnoreCase(name)) {
                    return ed.enchantment;
                }
            }
            return null;
        }

   }
*/