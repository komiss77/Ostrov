package ru.komiss77.utils;

import java.util.*;
import java.util.Map.Entry;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import java.net.URI;
import com.google.gson.Gson;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.EntityEffect;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
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
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.Bukkit;
import org.bukkit.profile.PlayerTextures;
import ru.komiss77.ApiOstrov;
import ru.komiss77.OStrap;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemClass;
import ru.komiss77.modules.translate.Lang;
import ru.komiss77.objects.CaseInsensitiveMap;


public class ItemUtils {

    public static final NamespacedKey key;
    private static final CaseInsensitiveMap<com.destroystokyo.paper.profile.PlayerProfile> playerProfilesCache;
    public static final ItemStack air, book, add, nextPage, previosPage;
    private static final Pattern regex;
    private static final Gson GSON;

    static {
        key = new NamespacedKey(Ostrov.instance, "ostrov");
        playerProfilesCache = new CaseInsensitiveMap<>();
        regex = Pattern.compile("(.{1,24}(?:\\s|$))|(.{0,24})", Pattern.DOTALL);
        GSON = new Gson();
        air = new ItemStack(Material.AIR);
        book = new ItemStack(Material.WRITTEN_BOOK);
        add = new ItemBuilder(Material.PLAYER_HEAD)
                .name("§aдобавить")
                .setCustomHeadTexture(Texture.add)
                .build();
        nextPage = new ItemBuilder(Material.PLAYER_HEAD)
                .name("§fдалее")
                .setCustomHeadTexture(Texture.nextPage)
                .build();
        previosPage = new ItemBuilder(Material.PLAYER_HEAD)
                .name("§fназад")
                .setCustomHeadTexture(Texture.previosPage)
                .build();
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

    public static int getCusomModelData(final ItemStack is) {
        if (is != null && is.hasItemMeta() && is.getItemMeta().hasCustomModelData()) {
            return is.getItemMeta().getCustomModelData();
        }
        return 0;
    }

    public static ItemStack setCusomModelData(final ItemStack is, final int id) {
        final ItemMeta im = is.getItemMeta();
        im.setCustomModelData(id);
        is.setItemMeta(im);
        return is;
    }

    public static ItemStack setName(final ItemStack is, final String name) {
        final ItemMeta im = is.getItemMeta();
        im.displayName(TCUtils.format(name));
        is.setItemMeta(im);
        return is;
    }


    public enum Texture {
        nextPage("c2f910c47da042e4aa28af6cc81cf48ac6caf37dab35f88db993accb9dfe516"),
        previosPage("f2599bd986659b8ce2c4988525c94e19ddd39fad08a38284a197f1b70675acc"),
        add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19"),
        //https://minecraft-heads.com/custom-heads/alphabet?start=4720
        //черный стиль - https://minecraft-heads.com/custom-heads/alphabet?start=3600
        _0_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2YwOTAxOGY0NmYzNDllNTUzNDQ2OTQ2YTM4NjQ5ZmNmY2Y5ZmRmZDYyOTE2YWVjMzNlYmNhOTZiYjIxYjUifX19"),
        _1_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2E1MTZmYmFlMTYwNThmMjUxYWVmOWE2OGQzMDc4NTQ5ZjQ4ZjZkNWI2ODNmMTljZjVhMTc0NTIxN2Q3MmNjIn19fQ=="),
        _2_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDY5OGFkZDM5Y2Y5ZTRlYTkyZDQyZmFkZWZkZWMzYmU4YTdkYWZhMTFmYjM1OWRlNzUyZTlmNTRhZWNlZGM5YSJ9fX0="),
        _3_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmQ5ZTRjZDVlMWI5ZjNjOGQ2Y2E1YTFiZjQ1ZDg2ZWRkMWQ1MWU1MzVkYmY4NTVmZTlkMmY1ZDRjZmZjZDIifX19"),
        _4_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjJhM2Q1Mzg5ODE0MWM1OGQ1YWNiY2ZjODc0NjlhODdkNDhjNWMxZmM4MmZiNGU3MmY3MDE1YTM2NDgwNTgifX19"),
        _5_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDFmZTM2YzQxMDQyNDdjODdlYmZkMzU4YWU2Y2E3ODA5YjYxYWZmZDYyNDVmYTk4NDA2OTI3NWQxY2JhNzYzIn19fQ=="),
        _6_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2FiNGRhMjM1OGI3YjBlODk4MGQwM2JkYjY0Mzk5ZWZiNDQxODc2M2FhZjg5YWZiMDQzNDUzNTYzN2YwYTEifX19"),
        _7_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjk3NzEyYmEzMjQ5NmM5ZTgyYjIwY2M3ZDE2ZTE2OGIwMzViNmY4OWYzZGYwMTQzMjRlNGQ3YzM2NWRiM2ZiIn19fQ=="),
        _8_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWJjMGZkYTlmYTFkOTg0N2EzYjE0NjQ1NGFkNjczN2FkMWJlNDhiZGFhOTQzMjQ0MjZlY2EwOTE4NTEyZCJ9fX0="),
        _9_("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDZhYmM2MWRjYWVmYmQ1MmQ5Njg5YzA2OTdjMjRjN2VjNGJjMWFmYjU2YjhiMzc1NWU2MTU0YjI0YTVkOGJhIn19fQ=="),
        dot("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzIzZTYxOWRjYjc1MTFjZGMyNTJhNWRjYTg1NjViMTlkOTUyYWM5ZjgyZDQ2N2U2NmM1MjI0MmY5Y2Q4OGZhIn19fQ=="),
        dotdot("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmZmY2MzMThjMjEyZGM3NDliNTk5NzU1ZTc2OTdkNDkyMzgyOTkzYzA3ZGUzZjhlNTRmZThmYzdkZGQxZSJ9fX0="),
        up("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGIyMjFjYjk2MDdjOGE5YmYwMmZlZjVkNzYxNGUzZWIxNjljYzIxOWJmNDI1MGZkNTcxNWQ1ZDJkNjA0NWY3In19fQ=="),
        down("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDhhYWI2ZDlhMGJkYjA3YzEzNWM5Nzg2MmU0ZWRmMzYzMTk0Mzg1MWVmYzU0NTQ2M2Q2OGU3OTNhYjQ1YTNkMyJ9fX0="),
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
//Ostrov.log("skinData="+skinData);
            //skullTexture = decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length());
            //value = getSkinTextureUrlStripped(value);
        }
        com.destroystokyo.paper.profile.PlayerProfile profile = getProfile(skinData);
        skullMeta.setPlayerProfile(profile);
        return skullMeta;
    }

    public static com.destroystokyo.paper.profile.PlayerProfile getProfile(String SHA_or_URL) {
        if (playerProfilesCache.containsKey(SHA_or_URL)) {
            return playerProfilesCache.get(SHA_or_URL);
        }
        final UUID uuid = UUID.randomUUID();
        final com.destroystokyo.paper.profile.PlayerProfile profile = Bukkit.createProfile(uuid);
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
            current.add(TCUtils.format(color == null ? regexMatcher.group() : color + regexMatcher.group()));
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
                current.add(TCUtils.format(block));
            } else {
                final List<String> split = split(block, 25);
                for (String line : split) {
                    current.add(TCUtils.format(line));
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
        } else if (force) {
            inv.setItem(pos, item);                                            //ставим предмет и возврат
            giveItemsTo(p, there);
            return true;
        } else if (compareItem(there, item, false)) {//уже есть в слоту
            return true;
        } else {
            giveItemsTo(p, item);//кидаем предмет рядом
            return false;
        }
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
        if (!he.getInventory().contains(mat)) {
            return false;
        }
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
                    cloneInv[slot].setType(Material.AIR);
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
                    cloneInv[slot].setType(Material.AIR);
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

        ItemMeta im;
        for (final ItemStack item : p.getInventory().getContents()) {
            if (item != null && !item.getType().isBlock() && item.hasItemMeta() && (item.getItemMeta() instanceof Damageable)) {
                im = item.getItemMeta();
                Damageable d = (Damageable) im;
                if (d.hasDamage()) {
                    d.setDamage(0);
                    item.setItemMeta(im);
                    repaired++;
                }
            }
        }

        for (final ItemStack item : p.getInventory().getArmorContents()) {
            if (item != null && !item.getType().isBlock() && item.hasItemMeta() && (item.getItemMeta() instanceof Damageable)) {
                im = item.getItemMeta();
                Damageable d = (Damageable) im;
                if (d.hasDamage()) {
                    d.setDamage(0);
                    item.setItemMeta(im);
                    repaired++;
                }
            }
        }

        p.updateInventory();

        return repaired;
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

    public static boolean isBlank(final ItemStack item, final boolean checkMeta) {
        return item == null || item.getType().isAir() || (checkMeta && !item.hasItemMeta());
    }

    public static boolean hasName(final ItemStack is) {
        return is != null && is.hasItemMeta() && is.getItemMeta().hasDisplayName();
    }

    public static String getName(final ItemStack is) {
        return hasName(is) ? TCUtils.toString(is.getItemMeta().displayName()) : "";
    }

    public static void giveItemsTo(final Player p, final ItemStack... its) {
        boolean left = false;
        for (final ItemStack it : p.getInventory().addItem(its).values()) {
            p.getWorld().dropItem(p.getLocation(), it);
            left = true;
        }
        if (left) {
            ApiOstrov.sendActionBarDirect(p, "§4В твоем инвентаре не было места, предмет выпал рядом!");
        }
    }


    public static String toString(final ItemStack is) {
        return toString(is, ":");
    }

    public static String toString(final ItemStack is, final String splitter) {
        if (is == null || is.getType() == Material.AIR) {
            return "air:1";
        }
        final StringBuilder res = new StringBuilder(is.getType().toString().toLowerCase() + ":" + is.getAmount());//apple<>1
        final String spl = " " + splitter + " ";

        if (is.hasItemMeta()) {

            final ItemMeta im = is.getItemMeta();
            if (im.hasDisplayName()) {
                res.append(spl).append("name:").append(TCUtils.toString(im.displayName()).replace('§', '&'));
            }

            if (im.hasLore()) {
                res.append(spl).append("lore:").append(im.lore().stream().map(TCUtils::toString).collect(Collectors.joining(":")));
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
                //final ArmorMeta am = armorMeta;
                if (am.hasTrim()) {
                    res.append(spl).append("trim:").append(am.getTrim().getMaterial().key().value()).append(":").append(am.getTrim().getPattern().key().value());
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
                        res.append(spl).append("effect:").append(cpe.getType().key().value()).append(":").append(cpe.getDuration()).append(":").append(cpe.getAmplifier());
                    }
                }

                if (pm.hasColor()) {
                    res.append(spl).append("color:").append(pm.getColor().getBlue()).append(":").append(pm.getColor().getGreen()).append(":").append(pm.getColor().getRed());
                }
            } else if (im instanceof FireworkMeta fm) {
                //final PotionMeta pm = potionMeta;
                res.append(spl).append("firework:").append(fm.getPower());

                for (final FireworkEffect fe : fm.getEffects()) {
                    final List<Color> cls = fe.getColors();
                    final List<Color> fds = fe.getFadeColors();
                    res.append(spl).append("burst:").append(fe.getType().name()).append(":")
                            .append(cls.isEmpty() ? Color.WHITE.asRGB() : cls.get(0).asRGB()).append(":")
                            .append(fds.isEmpty() ? Color.WHITE.asRGB() : fds.get(0).asRGB()).append(":")
                            .append(fe.hasFlicker()).append(":").append(fe.hasTrail());
                }
            } else if (im instanceof FireworkEffectMeta fm) {
                //final PotionMeta pm = potionMeta;
                final FireworkEffect fe = fm.getEffect();
                if (fe != null) {
                    final List<Color> cls = fe.getColors();
                    final List<Color> fds = fe.getFadeColors();
                    res.append(spl).append("burst:").append(fe.getType().name()).append(":")
                            .append(cls.isEmpty() ? Color.WHITE.asRGB() : cls.get(0).asRGB()).append(":")
                            .append(fds.isEmpty() ? Color.WHITE.asRGB() : fds.get(0).asRGB()).append(":")
                            .append(fe.hasFlicker()).append(":").append(fe.hasTrail());
                }
            }

            if (im.hasAttributeModifiers()) {
                for (final Entry<Attribute, AttributeModifier> en : im.getAttributeModifiers().entries()) {
                    final AttributeModifier am = en.getValue();
                    res.append(spl).append("attribute:").append(en.getKey().toString())
                            .append(":").append(am.getAmount()).append(":")
                            .append(am.getOperation().ordinal()).append(":")
                            .append(am.getSlot() == null ? "ANY" : am.getSlot().toString());
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


    public static ItemStack parseItem(final String item, final String splitter) {

        //grass:1<>name:nnn<>lore:sdsds:sdsd<>enchant:ARROW_DAMAGE:1<>dye:RED<>end
        final String spl = splitter.equals(":") ? " : " : splitter;

        final ItemBuilder builder = new ItemBuilder(Material.BEDROCK);

        if (item == null || item.isEmpty()) {
            Ostrov.log_warn("Декодер предмета : §7строка >§f" + item + "§7< ошибочная!");
            return builder.name("§cСтрока для декодирования ошибочная!").build();
        }

        if (splitter.isBlank()) {
            Ostrov.log_warn("Декодер предмета : §7строка >§f" + item + "§7<, Разделитель не может быть пробелом!");
            return builder.name("§cРазделитель для декодирования ошибочный!").build();
        }

        final List<String> splittedParametrs = new ArrayList<>();

        for (String param : item.split(spl)) {
            if (!param.trim().isEmpty()) {
                splittedParametrs.add(param.trim());
            }
        }

        if (splittedParametrs.isEmpty()) {
            Ostrov.log_warn("Декодер предмета : §7строка >§f" + item + "§7<, Не найдено никаких параметров!");
            return builder.name("§cНе найдено никаких параметров!").build();
        }

//System.out.println("--- splittedParametrs.size="+splittedParametrs.size()+" 0="+splittedParametrs.get(0));
        final Material mat;
        if (splittedParametrs.get(0).contains(":")) { //если с колличеством
            String[] s0 = splittedParametrs.get(0).trim().split(":");
            mat = Material.matchMaterial(s0[0].trim());
            if (mat != null) {
                builder.type(mat);
                if (Ostrov.isInteger(s0[1].trim())) {
                    builder.amount(Integer.parseInt(s0[1].trim()));
                } else {
                    Ostrov.log_warn("Декодер предмета : §7строка >§f" + item + "§7<, неправильное колличество §f" + s0[1]);
                }
            } else {
                Ostrov.log_warn("Декодер предмета : §7строка >§f" + item + "§7<, нет материала §f" + s0[0]);
            }
        } else {
            mat = Material.matchMaterial(splittedParametrs.get(0).trim());
            if (mat != null) {
                builder.type(mat);
            } else {
                Ostrov.log_warn("Декодер предмета : §7строка >§f" + item + "§7<, нет материала §f" + splittedParametrs.get(0));
            }
        }

        if (splittedParametrs.size() == 1) {
            return builder.build();
        }

//System.out.println("2 itemstack="+itemstack);
        for (int i = 1; i < splittedParametrs.size(); ++i) {

            final String[] param = splittedParametrs.get(i).trim().split(":");
            if (param.length == 1) {
                switch (param[0].trim().toLowerCase()) {
                    case "end", "unbreakable":
                        break;
                    default:
                        Ostrov.log_warn("Декодер предмета : §7строка >§f" + item + "§7<, пустой параметр §f" + splittedParametrs.get(i));
                        continue;
                }
            }

            //System.out.println("--"+temp[j]);
            try {

                switch (param[0].trim().toLowerCase()) {

                    case "name":
                        if (param.length == 2) {
                            builder.name(param[1].replace('&', '§'));
                        } else {
                            Ostrov.log_warn("Декодер name : §7строка >§f" + item + "§7<, неверные параметры §f" + param[1].toUpperCase());
                        }
                        break;

                    case "lore":
                        if (param.length > 1) {
                            final List<Component> lrs = new ArrayList<>();
                            for (int j = 1; j < param.length; j++) {
                                lrs.add(TCUtils.format(param[j].replace('&', '§')));
                            }
                            builder.addLore(lrs);
                            //builder.addLore(splittedParametrs.get(i).trim().replaceFirst("lore:", "").replaceAll("&", "§"));
                        } else {
                            Ostrov.log_warn("Декодер lore : §7строка >§f" + item + "§7<, неверные параметры §f" + param[1].toUpperCase());
                        }
                        break;

                    case "color":
                        if (param.length == 4) {
                            if (ApiOstrov.isInteger(param[1]) && ApiOstrov.isInteger(param[2]) && ApiOstrov.isInteger(param[3])) {
                                builder.setColor(Color.fromRGB(Integer.parseInt(param[1]), Integer.parseInt(param[2]), Integer.parseInt(param[3])));
                            } else {
                                Ostrov.log_warn("Декодер color : §7строка >§f" + item + "§7<, должны быть числа §f" + param[1] + " " + param[2] + " " + param[3]);
                            }
                        } else {
                            Ostrov.log_warn("Декодер color : §7строка >§f" + item + "§7<, неверные параметры §f" + param[1].toUpperCase());
                        }
                        break;

                    case "model", "custommodeldata":
                        if (param.length == 2) {
                            if (ApiOstrov.isInteger(param[1])) {
                                int modelData = Integer.parseInt(param[1]);
                                if (modelData < 0) {
                                    modelData = 0;
                                }
                                builder.setModelData(modelData);
                            } else {
                                Ostrov.log_warn("Декодер model : §7строка >§f" + item + "§7<, должны быть числа §f" + param[1]);
                            }
                        } else {
                            Ostrov.log_warn("Декодер model : §7строка >§f" + item + "§7<, неверные параметры §f" + param[1].toUpperCase());
                        }
                        break;

                    case "itemflag":
                        if (param.length > 1) {
                            for (int j = 1; j < param.length; j++) {
                                final ItemFlag itemFlag = ItemFlag.valueOf(param[j]);
                                if (itemFlag == null) {
                                    Ostrov.log_warn("Декодер itemflag : §7строка >§f" + item + "§7<, нет такого флага §f" + param[j]);
                                    continue;
                                }
                                builder.addFlags(itemFlag);
                            }
                        } else {
                            Ostrov.log_warn("Декодер itemflag : §7строка >§f" + item + "§7<, неверные параметры §f" + param[1].toUpperCase());
                        }
                        break;

                    case "unbreakable":
                        builder.setUnbreakable(true);
                        break;

                    case "attribute":
                        if (param.length == 5) {
                            final double mod;
                            final int op;
                            try {
                                mod = Double.parseDouble(param[2]);
                                op = Integer.parseInt(param[3]);
                            } catch (NumberFormatException e) {
                                Ostrov.log_warn("Декодер attribute : §7строка >§f" + item + "§7<, неверные числа §f");
                                break;
                            }
                            final EquipmentSlotGroup esg = EquipmentSlotGroup.getByName(param[4]);
                            builder.setAttribute(Attribute.valueOf(param[1]), mod, Operation.values()[op],
                                    esg == null ? EquipmentSlotGroup.ANY : esg);
                            //builder.setAttribute(Attribute.valueOf(param[1]), mod, Operation.values()[op],
                            //param[4].equals("ANY") ? null : EquipmentSlot.valueOf(param[4]));
                        }
                        break;

                    case "skulltexture":
                        if (param.length == 2) {
                            builder.setCustomHeadTexture(param[1]);
                        } else {
                            Ostrov.log_warn("Декодер skulltexture : §7строка >§f" + item + "§7<, неверные параметры §f" + param[1].toUpperCase());
                        }
                        break;
                    case "skull", "skullowneruuid": //в итоге высерает java.lang.NullPointerException: Profile name must not be null
                        if (param.length == 2) {
                            //builder.setSkullOwnerUuid(param[1]);
                            Ostrov.log_warn("Декодер skullowneruuid : с uuid больше не работает, нужно переделать на skulltexture!");
                        } else {
                            Ostrov.log_warn("Декодер skullowneruuid : §7строка >§f" + item + "§7<, неверные параметры §f" + param[1].toUpperCase());
                        }
                        break;

                    //enchant:silk_touch:1
                    case "enchant", "bookenchant":
                        if (param.length == 3) {
                            final Enchantment enchant = OStrap.retrieve(RegistryKey.ENCHANTMENT, Key.key(param[1]));
                            if (enchant != null) {
                                builder.addEnchant(enchant, ApiOstrov.getInteger(param[2], 1));
                            } else {
                                Ostrov.log_warn("Декодер enchant : §7строка >§f" + item + "§7<, нет таких чар §f" + param[1]);
                            }
                        } else {
                            Ostrov.log_warn("Декодер enchant : §7строка >§f" + item + "§7<, неверные параметры §f" + param[1].toUpperCase());
                        }
                        break;

                    case "basepot", "basepotiondata":
                        if (param.length == 4 || param.length == 2) {
                            switch (builder.type()) {
                                case TIPPED_ARROW, POTION, LINGERING_POTION, SPLASH_POTION:
                                    PotionType potionType = Registry.POTION.get(NamespacedKey.minecraft(param[1].toLowerCase()));
                                    if (potionType == null) {
                                        @SuppressWarnings("deprecation") final PotionType npt = PotionType.getByEffect(PotionEffectType.getByName(param[1]));
                                        potionType = npt;
                                    }
                  /*try { //по ключу найдёт не все, например для SPEED key=minecraft:swiftness. Сначала ищем по енум обычные, потом по ключу кастомные
                  } catch (IllegalArgumentException ex) {
                    NamespacedKey key = NamespacedKey.minecraft(param[1].toLowerCase());
                    potionType = Registry.POTION.get(key);
                  }*/

                                    if (potionType != null) {
                                        builder.setBasePotionType(potionType);
                                    } else {
                                        Ostrov.log_warn("Декодер basepot : §7строка >§f" + item + "§7<, нет PotionType §f" + param[1].toLowerCase());
                                    }
                                    break;
                                default:
                                    Ostrov.log_warn("Декодер basepot : §7строка >§f" + item + "§7<, неприменима к §f" + builder.type());
                                    break;
                            }
                        } else {
                            Ostrov.log_warn("Декодер basepot : §7строка >§f" + item + "§7<, неверные параметры §f" + param[1].toLowerCase());
                        }
                        break;

                    case "effect", "custompotioneffect":
                        if (param.length == 4) {
                            switch (builder.type()) {
                                case TIPPED_ARROW, POTION, LINGERING_POTION, SPLASH_POTION:
                                    PotionEffectType potionEffectType = Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(param[1].toLowerCase()));
                                    if (potionEffectType == null) {
                                        @SuppressWarnings("deprecation") final PotionEffectType npe = PotionEffectType.getByName(param[1]);
                                        potionEffectType = npe;
                                    }
                                    if (potionEffectType != null) {
                                        if (ApiOstrov.isInteger(param[2]) && ApiOstrov.isInteger(param[3])) {
                                            builder.addCustomPotionEffect(new PotionEffect(potionEffectType, Integer.parseInt(param[2].toLowerCase()), Integer.parseInt(param[3].toLowerCase())));
                                        } else {
                                            Ostrov.log_warn("Декодер effect : §7строка >§f" + item + "§7<, должны быть числа §f" + param[2] + " " + param[3]);
                                        }
                                    } else {
                                        Ostrov.log_warn("Декодер effect : §7строка >§f" + item + "§7<, нет PotionType §f" + param[1]);
                                    }
                                    break;
                                default:
                                    Ostrov.log_warn("Декодер effect : §7строка >§f" + item + "§7<, неприменима к §f" + builder.type());
                                    break;
                            }
                        } else {
                            Ostrov.log_warn("Декодер effect : §7строка >§f" + item + "§7<, неверные параметры §f" + param[1]);
                        }
                        break;
                    case "trim":
                        if (param.length == 3) {
                            builder.setTrim(Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(param[1])),
                                    Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(param[2])));
                        } else {
                            Ostrov.log_warn("Декодер trim : §7строка >§f" + item + "§7<, неверные параметры §f" + param[1]);
                        }
                        break;
                    case "firework":
                        if (param.length == 2) {
                            if (builder.type() == Material.FIREWORK_ROCKET) {
                                builder.applyCustomMeta(FireworkMeta.class, fm -> fm.setPower(Integer.parseInt(param[1])));
                            } else {
                                Ostrov.log_warn("Декодер firework : §7строка >§f" + item + "§7<, неприменима к §f" + builder.type());
                            }
                        } else {
                            Ostrov.log_warn("Декодер firework : §7строка >§f" + item + "§7<, неверные параметры §f" + param[1]);
                        }
                        break;
                    case "burst":
                        if (param.length == 6) {
                            switch (builder.type()) {
                                case FIREWORK_ROCKET:
                                    builder.applyCustomMeta(FireworkMeta.class, fm -> fm.addEffect(FireworkEffect.builder()
                                            .with(FireworkEffect.Type.valueOf(param[1])).withColor(Color.fromRGB(Integer.parseInt(param[2])))
                                            .withFade(Color.fromRGB(Integer.parseInt(param[3]))).flicker(Boolean.parseBoolean(param[4]))
                                            .trail(Boolean.parseBoolean(param[5])).build()));
                                    break;
                                case FIREWORK_STAR:
                                    builder.applyCustomMeta(FireworkEffectMeta.class, fm -> fm.setEffect(FireworkEffect.builder()
                                            .with(FireworkEffect.Type.valueOf(param[1])).withColor(Color.fromRGB(Integer.parseInt(param[2])))
                                            .withFade(Color.fromRGB(Integer.parseInt(param[3]))).flicker(Boolean.parseBoolean(param[4]))
                                            .trail(Boolean.parseBoolean(param[5])).build()));
                                    break;
                                default:
                                    Ostrov.log_warn("Декодер burst : §7строка >§f" + item + "§7<, неприменима к §f" + builder.type());
                                    break;
                            }
                        } else {
                            Ostrov.log_warn("Декодер burst : §7строка >§f" + item + "§7<, неверные параметры §f" + param[1]);
                        }
                        break;
                    default:
                        Ostrov.log_warn("Декодер ОБЩИЙ : §7строка >§f" + item + "§7<, параметр не распознан §f" + param[0]);
                        break;
                }
            } catch (IllegalArgumentException | SecurityException | NullPointerException ex) {
                Ostrov.log_err("parseItem : " + item + " - " + ex.getMessage());
            }

        }

        return builder.build();
    }

    public static boolean compareItem(@Nullable final ItemStack is1, @Nullable final ItemStack is2, final boolean checkLore) {
        if (is1 == null || is2 == null) {
            return is1 == is2;
        }

        if (is1.getType() == is2.getType()) {  //тип совпадает

            if (is1.hasItemMeta() && is2.hasItemMeta()) { //если у обоих есть мета

                final ItemMeta im1 = is1.getItemMeta();
                final ItemMeta im2 = is2.getItemMeta();
                if (im1.hasDisplayName() && im2.hasDisplayName()) { //если у обоих есть название

                    if (TCUtils.compare(im1.displayName(), im2.displayName())) { //если название совпадает

                        if (!checkLore) {
                            return true;
                        }

                        if (im1.hasLore() && im2.hasLore()) { //если у обоих есть лоре

                            final List<Component> lore1 = im1.lore();
                            final List<Component> lore2 = im2.lore();

                            if (lore1.isEmpty() && lore2.isEmpty()) {
                                return true; //если одна лоре пустая, другая тоже должна быть пустая
                            }
                            if (lore1.size() != lore2.size()) {
                                return false;  //если размеры лоре не одинаковые - нет
                            }
                            for (int i = 0; i < lore1.size(); i++) {
                                if (!TCUtils.compare(lore1.get(i), lore2.get(i))) {  //перебираем строки
                                    return false;  //хоть одна строка разная - предметы разные
                                }
                            }
                            return true;

                        } else {
                            return !im1.hasLore() && !im2.hasLore(); //если хотя бы у одного неты лоре, то и у другого не должно быть
                        }
                    } else {
                        return false; //если название не совпадает - разные
                    }
                } else {
                    return !im1.hasDisplayName() && !im2.hasDisplayName(); //если хотя бы у одного неты названия, то и у другого не должно быть
                }
            } else {
                return !is1.hasItemMeta() && !is2.hasItemMeta(); //если хотя бы у одного неты меты, то и у другого не должно быть
            }
        } else {
            return false; //если тип не совпадает - разные
        }
        //return is1 != null && is2 != null && is1.getType().equals(is2.getType()) && is1.getItemMeta().hasDisplayName() && is1.getItemMeta().hasDisplayName() && is1.getItemMeta().getDisplayName().equals(is2.getItemMeta().getDisplayName());
    }

    public static void fillSign(final Sign sign, String suggest) {
        if (suggest == null || suggest.isEmpty()) {
            return;
        }
        final SignSide sd = sign.getSide(Side.FRONT);
        for (int ln = 0; !suggest.isEmpty() && ln < 4; ln++) {
            if (suggest.length() > 15) {
                sd.line(ln, TCUtils.format(suggest.substring(0, 15)));
                suggest = suggest.substring(15);
                continue;
            }

            sd.line(ln, TCUtils.format(suggest));
            break;
        }
        /*int line = 0;
            while( suggest.length() > 15 && line<4) {
                sign.setLine(line, suggest.substring(0, 15));
//System.out.println("line="+line+" -> "+suggest.substring(0, 14));
                suggest = suggest.substring(15);
                line++;
            }
            if (line<4 && !suggest.isEmpty()) { //добавляем остаток
                sign.setLine(line, suggest);
            }*/
        //}
        sign.update();
    }


    public static double getTrimMod(final ItemStack ti, final Attribute atr) {
        if (ti == null) {
            return 0d;
        }
        switch (ti.getType()) {
            case IRON_INGOT -> {
                return switch (atr) {
                    case GENERIC_ARMOR -> 0.2d;
                    case GENERIC_ARMOR_TOUGHNESS -> 0.1d;
                    case GENERIC_MAX_HEALTH -> 0d;
                    case GENERIC_ATTACK_DAMAGE -> 0d;
                    case GENERIC_ATTACK_KNOCKBACK -> 0d;
                    case GENERIC_ATTACK_SPEED -> -0.1d;
                    case GENERIC_KNOCKBACK_RESISTANCE -> 0.1d;
                    case GENERIC_MOVEMENT_SPEED -> -0.1d;
                    default -> 0d;
                };
            }
            case COPPER_INGOT -> {
                return switch (atr) {
                    case GENERIC_ARMOR -> -0.1d;
                    case GENERIC_ARMOR_TOUGHNESS -> 0.2d;
                    case GENERIC_MAX_HEALTH -> 2d;
                    case GENERIC_ATTACK_DAMAGE -> 0d;
                    case GENERIC_ATTACK_KNOCKBACK -> 0.1d;
                    case GENERIC_ATTACK_SPEED -> -0.1d;
                    case GENERIC_KNOCKBACK_RESISTANCE -> 0.2d;
                    case GENERIC_MOVEMENT_SPEED -> 0d;
                    default -> 0d;
                };
            }
            case GOLD_INGOT -> {
                return switch (atr) {
                    case GENERIC_ARMOR -> -0.1d;
                    case GENERIC_ARMOR_TOUGHNESS -> -0.2d;
                    case GENERIC_MAX_HEALTH -> 4d;
                    case GENERIC_ATTACK_DAMAGE -> 0d;
                    case GENERIC_ATTACK_KNOCKBACK -> -0.2d;
                    case GENERIC_ATTACK_SPEED -> 0.1d;
                    case GENERIC_KNOCKBACK_RESISTANCE -> 0d;
                    case GENERIC_MOVEMENT_SPEED -> 0d;
                    default -> 0d;
                };
            }
            case AMETHYST_SHARD -> {
                return switch (atr) {
                    case GENERIC_ARMOR -> -0.2d;
                    case GENERIC_ARMOR_TOUGHNESS -> 0d;
                    case GENERIC_MAX_HEALTH -> 0d;
                    case GENERIC_ATTACK_DAMAGE -> 0.1d;
                    case GENERIC_ATTACK_KNOCKBACK -> 0.2d;
                    case GENERIC_ATTACK_SPEED -> 0d;
                    case GENERIC_KNOCKBACK_RESISTANCE -> -0.4d;
                    case GENERIC_MOVEMENT_SPEED -> 0d;
                    default -> 0d;
                };
            }
            case DIAMOND -> {
                return switch (atr) {
                    case GENERIC_ARMOR -> 0.2d;
                    case GENERIC_ARMOR_TOUGHNESS -> 0.2d;
                    case GENERIC_MAX_HEALTH -> -1d;
                    case GENERIC_ATTACK_DAMAGE -> 0.1d;
                    case GENERIC_ATTACK_KNOCKBACK -> 0d;
                    case GENERIC_ATTACK_SPEED -> 0d;
                    case GENERIC_KNOCKBACK_RESISTANCE -> -0.1d;
                    case GENERIC_MOVEMENT_SPEED -> 0d;
                    default -> 0d;
                };
            }
            case EMERALD -> {
                return switch (atr) {
                    case GENERIC_ARMOR -> 0d;
                    case GENERIC_ARMOR_TOUGHNESS -> 0.4d;
                    case GENERIC_MAX_HEALTH -> 0d;
                    case GENERIC_ATTACK_DAMAGE -> 0d;
                    case GENERIC_ATTACK_KNOCKBACK -> -0.1d;
                    case GENERIC_ATTACK_SPEED -> 0d;
                    case GENERIC_KNOCKBACK_RESISTANCE -> -0.2d;
                    case GENERIC_MOVEMENT_SPEED -> 0d;
                    default -> 0d;
                };
            }
            case REDSTONE -> {
                return switch (atr) {
                    case GENERIC_ARMOR -> -0.4d;
                    case GENERIC_ARMOR_TOUGHNESS -> -0.6d;
                    case GENERIC_MAX_HEALTH -> 1d;
                    case GENERIC_ATTACK_DAMAGE -> -0.1d;
                    case GENERIC_ATTACK_KNOCKBACK -> 0d;
                    case GENERIC_ATTACK_SPEED -> 0.2d;
                    case GENERIC_KNOCKBACK_RESISTANCE -> 0d;
                    case GENERIC_MOVEMENT_SPEED -> 0.2d;
                    default -> 0d;
                };
            }
            case LAPIS_LAZULI -> {
                return switch (atr) {
                    case GENERIC_ARMOR -> -0.4d;
                    case GENERIC_ARMOR_TOUGHNESS -> 0.2d;
                    case GENERIC_MAX_HEALTH -> 0d;
                    case GENERIC_ATTACK_DAMAGE -> -0.1d;
                    case GENERIC_ATTACK_KNOCKBACK -> 0.2d;
                    case GENERIC_ATTACK_SPEED -> 0d;
                    case GENERIC_KNOCKBACK_RESISTANCE -> 0d;
                    case GENERIC_MOVEMENT_SPEED -> 0.2d;
                    default -> 0d;
                };
            }
            case NETHERITE_INGOT -> {
                return switch (atr) {
                    case GENERIC_ARMOR -> 0.6d;
                    case GENERIC_ARMOR_TOUGHNESS -> 0.4d;
                    case GENERIC_MAX_HEALTH -> 0d;
                    case GENERIC_ATTACK_DAMAGE -> 0d;
                    case GENERIC_ATTACK_KNOCKBACK -> 0d;
                    case GENERIC_ATTACK_SPEED -> -0.2d;
                    case GENERIC_KNOCKBACK_RESISTANCE -> 0.2d;
                    case GENERIC_MOVEMENT_SPEED -> -0.2d;
                    default -> 0d;
                };
            }
            case QUARTZ -> {
                return switch (atr) {
                    case GENERIC_ARMOR -> 0d;
                    case GENERIC_ARMOR_TOUGHNESS -> -0.4d;
                    case GENERIC_MAX_HEALTH -> -2d;
                    case GENERIC_ATTACK_DAMAGE -> 0.2d;
                    case GENERIC_ATTACK_KNOCKBACK -> 0.1d;
                    case GENERIC_ATTACK_SPEED -> 0d;
                    case GENERIC_KNOCKBACK_RESISTANCE -> -0.2d;
                    case GENERIC_MOVEMENT_SPEED -> 0.1d;
                    default -> 0d;
                };
            }
            default -> {
                return 0d;
            }
        }
    }


    public static ItemBuilder buildBiomeIcon(final Biome b) {
        final ItemBuilder builder = new ItemBuilder(Material.TROPICAL_FISH_BUCKET);
//System.out.println("getBiomeIcon "+b.toString());       
        if (b.toString().equalsIgnoreCase("NETHER") || b.toString().equalsIgnoreCase("NETHER_WASTES")) {
            builder.type(Material.NETHERRACK);
        } else {

            switch (b) {
                case BADLANDS -> builder.type(Material.RED_SAND);
                case BAMBOO_JUNGLE -> builder.type(Material.BAMBOO);
                case BEACH -> builder.type(Material.HORN_CORAL_FAN);
                case BIRCH_FOREST -> builder.type(Material.BIRCH_LOG);
                case COLD_OCEAN -> builder.type(Material.BLUE_CONCRETE_POWDER);
                case DARK_FOREST -> builder.type(Material.DARK_OAK_LOG);
                case MUSHROOM_FIELDS -> builder.type(Material.MYCELIUM);
                case DEEP_COLD_OCEAN -> builder.type(Material.BLUE_CONCRETE);
                case DEEP_FROZEN_OCEAN -> builder.type(Material.BLUE_ICE);
                case DEEP_LUKEWARM_OCEAN -> builder.type(Material.LIGHT_BLUE_CONCRETE);
                case DEEP_OCEAN -> builder.type(Material.BLUE_WOOL);
                case DESERT -> builder.type(Material.SAND);
                case END_BARRENS -> builder.type(Material.END_STONE);
                case END_HIGHLANDS -> builder.type(Material.END_STONE_BRICKS);
                case END_MIDLANDS -> builder.type(Material.END_STONE_BRICKS);
                case ERODED_BADLANDS -> builder.type(Material.DEAD_BUSH);
                case FLOWER_FOREST -> builder.type(Material.ROSE_BUSH);
                case WINDSWEPT_HILLS -> builder.type(Material.GRANITE);
                case FOREST -> builder.type(Material.DARK_OAK_LOG);
                case FROZEN_OCEAN -> builder.type(Material.PACKED_ICE);
                case FROZEN_RIVER -> builder.type(Material.LIGHT_BLUE_DYE);
                case ICE_SPIKES -> builder.type(Material.ICE);
                case JUNGLE -> builder.type(Material.JUNGLE_LOG);
                case LUKEWARM_OCEAN -> builder.type(Material.LIGHT_BLUE_CONCRETE_POWDER);
                case OCEAN -> builder.type(Material.WATER_BUCKET);
                case PLAINS -> builder.type(Material.GRASS_BLOCK);
                case MANGROVE_SWAMP -> builder.type(Material.MANGROVE_ROOTS);
                case RIVER -> builder.type(Material.BLUE_DYE);
                case SAVANNA -> builder.type(Material.ACACIA_LOG);
                case SAVANNA_PLATEAU -> builder.type(Material.ACACIA_WOOD);
                case SMALL_END_ISLANDS -> builder.type(Material.END_STONE);
                case SNOWY_BEACH -> builder.type(Material.SNOW);
                case SNOWY_TAIGA -> builder.type(Material.WHITE_WOOL);
                case SUNFLOWER_PLAINS -> builder.type(Material.SUNFLOWER);
                case SWAMP -> builder.type(Material.LILY_PAD);
                case TAIGA -> builder.type(Material.SPRUCE_LOG);
                case NETHER_WASTES -> builder.type(Material.NETHERRACK);
                case THE_END -> builder.type(Material.END_STONE);
                case THE_VOID -> builder.type(Material.BEDROCK);
                case WARM_OCEAN -> builder.type(Material.CYAN_CONCRETE_POWDER);
                case SNOWY_PLAINS -> builder.type(Material.SNOW);
                case SPARSE_JUNGLE -> builder.type(Material.VINE);
                case STONY_SHORE -> builder.type(Material.GRAVEL);
                case OLD_GROWTH_PINE_TAIGA -> builder.type(Material.SPRUCE_WOOD);
                case WINDSWEPT_FOREST -> builder.type(Material.STRIPPED_OAK_LOG);
                case WOODED_BADLANDS -> builder.type(Material.DEAD_BUSH);
                case WINDSWEPT_GRAVELLY_HILLS -> builder.type(Material.ANDESITE);
                case OLD_GROWTH_BIRCH_FOREST -> builder.type(Material.BIRCH_WOOD);
                case OLD_GROWTH_SPRUCE_TAIGA -> builder.type(Material.STRIPPED_SPRUCE_LOG);
                case WINDSWEPT_SAVANNA -> builder.type(Material.STRIPPED_ACACIA_LOG);
                case SOUL_SAND_VALLEY -> builder.type(Material.SOUL_SAND);
                case CRIMSON_FOREST -> builder.type(Material.CRIMSON_NYLIUM);
                case WARPED_FOREST -> builder.type(Material.WARPED_NYLIUM);
                case BASALT_DELTAS -> builder.type(Material.BASALT);
                case DRIPSTONE_CAVES -> builder.type(Material.DRIPSTONE_BLOCK);
                case LUSH_CAVES -> builder.type(Material.BIG_DRIPLEAF);
                case DEEP_DARK -> builder.type(Material.SCULK_CATALYST);
                case MEADOW -> builder.type(Material.BEE_NEST);
                case GROVE -> builder.type(Material.DIRT_PATH);
                case SNOWY_SLOPES -> builder.type(Material.POWDER_SNOW);
                case FROZEN_PEAKS -> builder.type(Material.PACKED_ICE);
                case JAGGED_PEAKS -> builder.type(Material.DIORITE);
                case STONY_PEAKS -> builder.type(Material.STONE);
                case CHERRY_GROVE -> builder.type(Material.CHERRY_LOG);
                case CUSTOM -> builder.type(Material.BEDROCK);
            }
        }

        builder.name(Lang.t(b, Lang.RU));

        return builder;
    }

    public static ItemBuilder buildEntityIcon(final EntityType type) {
        final ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD);

        switch (type) {
            case ARMOR_STAND -> builder.type(Material.ARMOR_STAND);
            case ZOMBIE -> builder.type(Material.ZOMBIE_HEAD);
            case CREEPER -> builder.type(Material.CREEPER_HEAD);
            case PIGLIN -> builder.type(Material.PIGLIN_HEAD);
            case ENDER_DRAGON -> builder.type(Material.DRAGON_HEAD);
            //case  -> builder.setCustomHeadTexture("6d865aae2746a9b8e9a4fe629fb08d18d0a9251e5ccbe5fa7051f53eab9b94");
            default -> builder.type(Material.NAME_TAG);
        }

        builder.name(Lang.t(type, Lang.RU));
        return builder;
    }

    public static boolean isItemA(final ItemStack is, final ItemClass cls) {
        return cls.has(is == null ? Material.AIR : is.getType());
    }

    public static boolean isMineCart(final Material type) {
        switch (type) {
            case MINECART, CHEST_MINECART, FURNACE_MINECART, TNT_MINECART, HOPPER_MINECART, COMMAND_BLOCK_MINECART:
                return true;
            default:
                return false;
        }
    }

    public static boolean isSpawnEgg(final Material type) {
        //switch (type) { пока лень забивать енумы
        //   case EGG, : return true;
        //  default: return false;
        //}
        return type.name().endsWith("_EGG");
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
