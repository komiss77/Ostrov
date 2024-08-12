package ru.komiss77.utils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemUtil.Texture;

public class ItemBuilder {

    private ItemType type;//private Material mat;
    private int amount = 1; //по умолчанию 1, или build() выдаёт AIR!
    private int maxStack;
    private @Nullable ItemMeta meta;
    private Color color;
    private List<Component> lore;
    private UUID skullOwnerUuid;
    private String skullTexture;
    private PotionType basePotionType;
    private List<PotionEffect> customPotionEffects = null;
    private Map<Enchantment, Integer> enchants = null;
    private boolean wrong;

    public ItemBuilder(final Material mat) {
        if (mat == null || mat.isAir()) { //защита от создание билдера с null или air
            wrong = true;
            lore = List.of(Component.text(mat == null ? "mat==null" : "mat==" + mat.name()));
        } else {
            type = mat.asItemType();
            maxStack = mat.getMaxStackSize();
        }
    }

    public ItemBuilder(final ItemType itemType) {
        if (itemType == null) {
            wrong = true;
            lore = List.of(Component.text("itemType==null"));
        } else {
            type = itemType;//meta = null;при создании поле итак null
            maxStack = itemType.getMaxStackSize();
        }
    }

    public ItemBuilder(final ItemStack from) {
        if (from == null || from.getType().isAir()) { //каждое from == null ? ниже - отдельное if. Собираем три if в одно.
            wrong = true;
            lore = List.of(Component.text(from == null ? "from==null" : from.getType().isAir() ? "from.isAir" : "from.other"));
        } else {
            type = from.getType().asItemType();
            amount = from.getAmount();
            maxStack = from.getType().asItemType().getMaxStackSize();
            if (from.hasItemMeta()) {
                meta = from.getItemMeta();//meta = from != null && from.hasItemMeta() ? from.getItemMeta() : null;
                if (meta.hasLore()) { //lore = meta != null && meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                    lore = new ArrayList<>(meta.lore());
                }
            }
        }
    }

    private boolean checkMeta() {
        if (wrong) return false;
        if (meta == null || !type.getItemMetaClass().isInstance(meta)) {
            meta = ((CraftItemType<?>) type).getItemMeta(meta);
        }
        return meta != null;
    }

    public <M extends ItemMeta> ItemBuilder meta(final @Nullable M mt) {
        if (mt == null) return this;
        final ItemStack it = type.createItemStack();
        it.setItemMeta(mt);
        meta = it.getItemMeta();
        return this;
    }

    public ItemBuilder customMeta(final Consumer<ItemMeta> applier) {
        if (checkMeta()) applier.accept(this.meta);
        return this;
    }

    public <M extends ItemMeta> ItemBuilder customMeta(final Class<M> meta, final Consumer<M> applier) {
        if (checkMeta()) {
            if (meta.isInstance(this.meta)) {
                applier.accept(meta.cast(this.meta));
            }
        }
        return this;
    }

    public ItemBuilder type(final ItemType tp) {
        if (tp == null) return this;
        type = tp;
        if (meta == null) return this;
        checkMeta();
        return this;
    }

    public ItemBuilder type(final Material mat) { //ну тип переделал билдер на материал @Deprecated //в будующем тип менять нельзя будет
        if (mat == null) return this;
        type = mat.asItemType();
        if (meta == null) return this;
        checkMeta();
        return this;
    }

    public ItemType type() {
        return type;
    }

    //если ключ нуль - использует готовый ключ острова
    public ItemBuilder data(@Nullable final String key, final Serializable data) {
        if (checkMeta()) {
            switch (data) {
                case final Byte d -> data(key, d, PersistentDataType.BYTE);
                case final Long d -> data(key, d, PersistentDataType.LONG);
                case final Integer d -> data(key, d, PersistentDataType.INTEGER);
                case final Float d -> data(key, d, PersistentDataType.FLOAT);
                case final Double d -> data(key, d, PersistentDataType.DOUBLE);
                case final byte[] d -> data(key, d, PersistentDataType.BYTE_ARRAY);
                case final int[] d -> data(key, d, PersistentDataType.INTEGER_ARRAY);
                case final String d -> data(key, d, PersistentDataType.STRING);
                default -> data(key, data.toString(), PersistentDataType.STRING);
            }
        }
        return this;
    }

    public <T extends Serializable, D> ItemBuilder data(final String key, final D data, final PersistentDataType<T, D> pdt) {
        if (checkMeta()) {
            final NamespacedKey nsk = key == null ? ItemUtil.key : new NamespacedKey(Ostrov.instance, key);
            meta.getPersistentDataContainer().set(nsk, pdt, data);
        }
        return this;
    }

    //public Material type() {
    //     return mat;//item.getType();
    // }

    public ItemBuilder amount(final int ammount) {
        this.amount = ammount;    //item.setAmount(amount);
        return this;
    }

    public ItemBuilder maxStack(final int maxStack) {
        this.maxStack = maxStack;
        return this;
    }

    public ItemBuilder name(@Nullable final String name) {
        if (checkMeta()) {
            meta.displayName(name == null ? null : TCUtil.form(name));
        }
        return this;
    }

    public ItemBuilder name(@Nullable final Component name) {
        if (checkMeta()) {
            meta.displayName(name);
        }
        return this;
    }

    public ItemBuilder lore(final String s) {
        if (s==null) return this;
        if (lore == null) lore = new ArrayList<>();
        if (s.isEmpty()) lore.add(Component.empty());
        else lore.add(TCUtil.form(s));
        return this;
    }

    public ItemBuilder lore(final Collection<String> sc) {
        if (sc==null || sc.isEmpty()) return this;
        if (lore == null) lore = new ArrayList<>();
        for (final String s : sc) lore.add(TCUtil.form(s));
        return this;
    }

    public ItemBuilder lore(final Component c) {
        if (c==null) return this;
        if (lore == null) lore = new ArrayList<>();
        else lore.add(c);
        return this;
    }

    public ItemBuilder lore(final List<Component> lc) {
        if (lc==null || lc.isEmpty()) return this;
        if (lore == null) lore = new ArrayList<>();
        lore.addAll(lc);
        return this;
    }

    public ItemBuilder lore(final Component... lores) {
        if (lores == null) return this;
        for (final Component c : lores) {
            lore(c);
        }
        return this;
    }

    public ItemBuilder lore(final String... lores) {
        if (lores == null) return this;
        for (final String c : lores) {
            lore(c);
        }
        return this;
    }

//    @Deprecated
    /*
     * @param o = String, Component, Collection<String>, Collection<Component>, Component[]
     * @return ItemBuilder
     */
    /*public ItemBuilder lore(Object o) {
        if (o == null) return this;
        if (lore == null) lore = new ArrayList<>();
        switch (o) {
            case String s:
                if (s.isEmpty()) lore.add(Component.empty());
                else lore.add(TCUtil.form(s));
                break;
            case Component c:
                lore.add(c);
                break;
            case Collection<?> c:
                for (Object x : c) {
                    lore(x);
                }
                break;
            case String[] ss:
                for (final String s : ss) {
                    lore.add(TCUtil.form(s));
                }
                break;
            case Component[] cc:
                Collections.addAll(lore, cc);
                break;
            default:
                break;
        }
        return this;
    }

    @Deprecated
    public ItemBuilder lore(final Object... lores) {
        if (lores == null) return this;
        for (final Object o : lores) {
            lore(o);
        }
        return this;
    }

    @Deprecated
    public ItemBuilder lore(final Object o) { //иногда нужен простой быстрый метод
        if (o == null) {
            lore = null;
            return this;
        }
        if (lore != null) {
            lore.clear();
        }
        return lore(o);
    }*/

    public ItemBuilder deLore() {
        lore = null;
        //if (lore == null) {
        //    lore = new ArrayList<>();
        //} else {
        //     lore.clear();
        // }
        return this;
    }

    public ItemBuilder repLore(final String from, final String to) {
        return repLore(TCUtil.form(from), TCUtil.form(to));
    }

    public ItemBuilder repLore(final Component from, final Component to) {
        if (lore == null || lore.isEmpty()) return this;
        for (int i = 0; i < lore.size(); i++) {
            if (TCUtil.compare(lore.get(i), from)) {
                lore.set(i, to);
            }
        }
        return this;
    }


    public ItemBuilder flags(final ItemFlag... flags) {
        if (checkMeta()) {
            meta.addItemFlags(flags);
        }
        return this;
    }

    public void trim(final TrimMaterial mat, final TrimPattern pat) {
        if (checkMeta()) {
            customMeta(ArmorMeta.class, am ->
                am.setTrim(new ArmorTrim(mat, pat)));
        }
    }

    public ItemBuilder enchant(final Enchantment enchant) {
        return enchant(enchant, 1);
    }

    public ItemBuilder enchant(final Enchantment enchant, final int level) {
        if (enchants == null) enchants = new HashMap<>();
        if (level < 1) enchants.remove(enchant);
        else enchants.put(enchant, level);
        return this;
    }

    public ItemBuilder disEnchant() {
        if (meta != null) meta.removeEnchantments();
        if (enchants != null) enchants.clear();
        return this;
    }

    public ItemBuilder unbreak(final boolean unbreakable) {
        if (checkMeta()) {
            meta.setUnbreakable(unbreakable);
        }
        return this;
    }

    public ItemBuilder flags(final ItemFlag flag) {
        if (checkMeta()) {
            meta.addItemFlags(flag);
        }
        return this;
    }

    @Deprecated
    public ItemBuilder attribute(final Attribute attribute, final double amount, final Operation op) {
         attribute(attribute, amount, op, type.asMaterial().getEquipmentSlot().getGroup());
         return this;
      }

    public ItemBuilder attribute(final Attribute attribute, final double amount, final Operation op, @Nullable final EquipmentSlotGroup slotGroup) {
        if (checkMeta()) {
            meta.addAttributeModifier(attribute, new AttributeModifier(attribute.getKey(), amount, op, slotGroup));
        }
        return this;
    }

    public ItemBuilder removeAttribute(final Attribute attribute) {
        if (checkMeta()) {
            meta.removeAttributeModifier(attribute);
        }
        return this;
    }

    public ItemBuilder modelData(final int data) {
        if (checkMeta()) {
            meta.setCustomModelData(data);
        }
        return this;
    }

    //вроде очень редко нужно, думаю не страшно будет ставить всегда одним методом через float
    //хз мне нужно довольно часто
    public ItemBuilder durability(final int dur) {
        if (checkMeta()) {
            if (meta instanceof final Damageable dr && dr.hasMaxDamage()) {
                final int mxd = dr.getMaxDamage();
                if (dur < mxd) dr.setDamage(Math.max(mxd - dur, 0));
                dr.resetDamage();
            }
        }
        return this;
    }

    public ItemBuilder durability(final float dur) {
        if (checkMeta()) {
            if (meta instanceof final Damageable dr && dr.hasMaxDamage()) {
                if (dur == 1f) {
                    dr.resetDamage();
                } else {
                    dr.setDamage((int) (dr.getMaxDamage() * (1f - Math.clamp(dur, 0f, 1f))));
                }
            }
        }
        return this;
    }

    public ItemBuilder maxDamage(final int dur) {
        if (checkMeta()) {
            if (meta instanceof final Damageable dr) {
                final float rel = (float) dr.getDamage() / (float) dr.getMaxDamage();
                dr.setMaxDamage(dur);
                dr.setDamage((int) (dur * rel));
            }
        }
        return this;
    }

    //взял глинт закомментил (((  наверное, случайно. сорян
    public ItemBuilder glint(final @Nullable Boolean glint) {
        if (glint == null && (meta == null || !meta.hasEnchantmentGlintOverride())) return this;
        if (checkMeta()) {
            meta.setEnchantmentGlintOverride(glint);
        }
        return this;
    }

    /*
     * поставить владельца головы
     *
     * @param skullOwner OfflinePlayer, UUID или UUID.asString
     * @return
     */
    /*public ItemBuilder skullOf(Object skullOwner) {
        if (skullOwner instanceof OfflinePlayer op) {
            skullOwnerUuid = op.getUniqueId().toString();
        } else if (skullOwner instanceof UUID) {
            skullOwnerUuid = skullOwner.toString();
        } else if (skullOwner instanceof String) {
            skullOwnerUuid = (String) skullOwner;
        }
        return this;
    }*/

    public ItemBuilder skullOf(final OfflinePlayer pl) {
        skullOwnerUuid = pl.getUniqueId();
        return this;
    }

    public ItemBuilder skullOf(final UUID id) {
        skullOwnerUuid = id;
        return this;
    }

    public ItemBuilder skullOf(final String name) { //хз что вернет)
        skullOwnerUuid = Bukkit.getOfflinePlayer(name).getUniqueId();
        return this;
    }

    /*
     * @param texture Texture или String вида <a href="https://minecraft-heads.com/custom-heads/">...</a>
     * @return ItemBuilder
     */
    /*public ItemBuilder headTexture(Object texture) {
        //if (texture.length()<70) return setCustomHeadUrl(texture); //фикс!!
        if (texture == null) return this;
        if (texture instanceof Texture) {
            texture = headTexture(((Texture) texture).value);
        }
        if (texture instanceof String) {
            this.skullTexture = (String) texture;
        }
        return this;
    }*/

    public ItemBuilder headTexture(final String texture) {
        this.skullTexture = texture;
        return this;
    }

    public ItemBuilder headTexture(final Texture texture) {
        return headTexture(texture.value);
    }

    public ItemBuilder color(final Color color) {
        this.color = color;
        return this;
    }

    public ItemBuilder basePotion(final PotionType type) {
        this.basePotionType = type;
        return this;
    }

    public ItemBuilder customPotion(final PotionEffect customPotionEffect) {
        if (customPotionEffect != null && (customPotionEffects == null)) {
            customPotionEffects = new ArrayList<>();
        }
        customPotionEffects.add(customPotionEffect);
        return this;
    }

    public ItemStack build() {
        if (wrong) {
            return new ItemBuilder(Material.BEDROCK)
                    .name("§cКривой предмет!")
                    .lore(lore)
                    .build();
        }
        if (amount < 1) {
            return ItemUtil.air.clone();
        } else if (amount > maxStack) {
            amount = maxStack;
        }
        final ItemStack item = type.createItemStack(amount);
        if (meta == null) meta = item.getItemMeta();
        if (meta == null) return item;

        if (maxStack != type.getMaxStackSize()) {
            meta.setMaxStackSize(maxStack);
        }
        //if (lore != null && !lore.isEmpty()) {
        meta.lore(lore); //вроде не надо проверять, ставим что есть
        //}

        switch (meta) {
            case final PotionMeta pm:
                if (basePotionType != null || customPotionEffects != null) {
                    if (basePotionType != null) pm.setBasePotionType(basePotionType);
                    if (customPotionEffects != null && !customPotionEffects.isEmpty()) {
                        for (final PotionEffect ef : customPotionEffects) {
                            pm.addCustomEffect(ef, true);
                        }
                    }
                    if (color != null) {
                        pm.setColor(color);
                    }
                }
                break;

            case final SkullMeta sm:
                if (skullOwnerUuid != null) {
                    final OfflinePlayer ofp = Bukkit.getOfflinePlayer(skullOwnerUuid);
                    sm.setOwningPlayer(ofp);
                }

                if (skullTexture != null && !skullTexture.isEmpty()) {
                    ItemUtil.setHeadTexture(sm, skullTexture);
                }
                break;

            case final LeatherArmorMeta lam:
                if (color != null) {
                    lam.setColor(color);
                }
                break;

            case final EnchantmentStorageMeta esm://для книг чары в storage
                if (enchants != null && !enchants.isEmpty()) {
                    for (final Map.Entry<Enchantment, Integer> en : enchants.entrySet()) {
                        esm.addStoredEnchant(en.getKey(), en.getValue(), false);
                    }
                }
                enchants = null;
                break;
            default:
                break; //для обычных предметов кидаем чары
        }

        if (enchants != null && !enchants.isEmpty()) {
            for (final Map.Entry<Enchantment, Integer> en : enchants.entrySet()) {
                meta.addEnchant(en.getKey(), en.getValue(), true);
            }
        }

        item.setItemMeta(meta);
        return item;
    }
/*
    public ItemStack build() {
        if (amount < 1) {
            amount = 1;
        } else if (amount > maxStack) {
            amount = maxStack;
        }
        final ItemStack item = type.createItemStack(amount);//new ItemStack(mat, amount);
        if (!lore.isEmpty()) {
            if (meta == null) meta = item.getItemMeta();
            meta.lore(lore);
        }

        if (maxStack != type.getMaxStackSize()) {
            meta.setMaxStackSize(maxStack);
        }

        switch (mat) {

            case POTION, TIPPED_ARROW, LINGERING_POTION, SPLASH_POTION:
                if (basePotionType != null || customPotionEffects != null) {
                    if (meta == null) meta = item.getItemMeta();
                    final PotionMeta potionMeta = (PotionMeta) meta;
                    if (basePotionType != null) potionMeta.setBasePotionType(basePotionType);
                    if (customPotionEffects != null && !customPotionEffects.isEmpty()) {
                        for (final PotionEffect ef : customPotionEffects) {
                            potionMeta.addCustomEffect(ef, true);
                        }
                    }
                    if (color != null) {
                        potionMeta.setColor(color);
                    }
                }
                break;

            case PLAYER_HEAD:
                if (meta == null) meta = item.getItemMeta();
                final SkullMeta skullMeta = (SkullMeta) meta;

                if (skullOwnerUuid != null && !skullOwnerUuid.isEmpty()) {
                    final UUID uuid = UUID.fromString(skullOwnerUuid);
                    final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                    skullMeta.setOwningPlayer(offlinePlayer);
                }

                if (skullTexture != null && !skullTexture.isEmpty()) {
                    ItemUtil.setHeadTexture(skullMeta, skullTexture);
                }
                break;

            case LEATHER_BOOTS, LEATHER_CHESTPLATE, LEATHER_HELMET,
                    LEATHER_LEGGINGS, LEATHER_HORSE_ARMOR:
                if (color != null) {
                    if (meta == null) meta = item.getItemMeta();
                    final LeatherArmorMeta leatherMeta = (LeatherArmorMeta) meta;
                    leatherMeta.setColor(color);
                }
                break;

            case ENCHANTED_BOOK://для книг чары в storage
                if (enchants != null && !enchants.isEmpty()) {
                    if (meta == null) meta = item.getItemMeta();
                    final EnchantmentStorageMeta enchantedBookMeta = (EnchantmentStorageMeta) meta;
                    for (final Map.Entry<Enchantment, Integer> en : enchants.entrySet()) {//ignoreLevelRestriction
                        enchantedBookMeta.addStoredEnchant(en.getKey(), en.getValue(), false);
                    }
                }
                enchants = null;
                break;

            default:
                break; //для обычных предметов просто кидаем чары - а для дригих не кидаем????? не заслужили тип????
        }

        if (enchants != null && !enchants.isEmpty()) {
            if (meta == null) meta = item.getItemMeta();
            for (final Map.Entry<Enchantment, Integer> en : enchants.entrySet()) {
                meta.addEnchant(en.getKey(), en.getValue(), true);
            }
        }

        if (meta != null) item.setItemMeta(meta);
        return item;
    }*/


}
