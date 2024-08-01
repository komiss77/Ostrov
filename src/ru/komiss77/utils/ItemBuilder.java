package ru.komiss77.utils;

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
import ru.komiss77.utils.ItemUtils.Texture;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

//im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
//Validate.isTrue(item.getType() == Material.PLAYER_HEAD, "skullOwner() only applicable for skulls!", new Object[0]);


public class ItemBuilder {

    private ItemType type;//private final ItemStack item; item не используем - в будущем тип менять нельзя будет
    private int amount;
    private int maxStack;
    private @Nullable ItemMeta meta;
    private Color color;
    private List<Component> lore;
    private UUID skullOwnerUuid;
    private String skullTexture;
    private PotionType basePotionType;
    private List<PotionEffect> customPotionEffects = null;
    private Map<Enchantment, Integer> enchants = null;

    public ItemBuilder(final Material material) {
        type = material.asItemType();
        meta = null;
        lore = new ArrayList<>();
        amount = 1;
        maxStack = material.getMaxStackSize();
    }

    public ItemBuilder(final ItemType tp) {
        type = tp;
        meta = null;
        lore = new ArrayList<>();
        amount = 1;
        maxStack = tp.getMaxStackSize();
    }

    public ItemBuilder(final ItemStack from) {
        //item = from==null ? new ItemStack(Material.AIR) : new ItemStack(from.getType(), from.getAmount());
        type = from == null ? ItemType.AIR : from.getType().asItemType();
        amount = from == null ? 1 : from.getAmount();
        maxStack = from == null ? 64 : from.getType().asItemType().getMaxStackSize();
        meta = from != null && from.hasItemMeta() ? from.getItemMeta() : null;
        lore = meta != null && meta.hasLore() ? meta.lore() : new ArrayList<>();
    }

    private ItemMeta checkMeta() {
        if (meta == null || !type.getItemMetaClass().isInstance(meta)) {
            meta = ((CraftItemType<?>) type).getItemMeta(meta);
        }
        return meta;
    }

    public <M extends ItemMeta> ItemBuilder customMeta(final Class<M> meta, final Consumer<M> applier) {
        checkMeta();
        if (meta.isInstance(this.meta)) {
            applier.accept(meta.cast(this.meta));
        }
        return this;
    }

    public ItemBuilder data(final String key, final Serializable data) {
        checkMeta();
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
        return this;
    }

    public <T extends Serializable, D> ItemBuilder data(final String key, final D data, final PersistentDataType<T, D> pdt) {
        checkMeta();
        final NamespacedKey nsk = key == null ? ItemUtils.key : new NamespacedKey(Ostrov.instance, key);
        meta.getPersistentDataContainer().set(nsk, pdt, data);
        return this;
    }

    public ItemBuilder asType(final ItemType tp) {
        type = tp;
        if (meta == null) return this;
        checkMeta();
        return this;
    }

    public ItemBuilder setType(final Material tp) {
        type = tp.asItemType();
        if (meta == null) return this;
        checkMeta();
        return this;
    }

    @Deprecated
    public Material getType() {
        return type.asMaterial();
    }

    public ItemType type() {
        return type;
    }

    public ItemBuilder amount(final int ammount) {
        this.amount = ammount;
        return this;
    }

    public ItemBuilder maxStack(final int maxStack) {
        this.maxStack = maxStack;
        return this;
    }

    public ItemBuilder name(@Nullable final String name) {
        checkMeta();
        if (name == null) meta.displayName(null);
        else meta.displayName(TCUtils.form(name));
        return this;
    }

    public ItemBuilder name(@Nullable final Component name) {
        checkMeta();
        meta.displayName(name);
        return this;
    }

    public ItemBuilder deLore() {
        lore.clear();
        return this;
    }

    @Deprecated
    public ItemBuilder clearLore() {
        return deLore();
    }

    @Deprecated
    public ItemBuilder addLore(final String s) {
        if (s == null) return this;
        if (lore == null) lore = new ArrayList<>();
        if (s.isEmpty()) lore.add(Component.text(""));
        else lore.add(TCUtils.form(s));
        return this;
    }

    @Deprecated
    public ItemBuilder addLore(final Collection<String> sc) {
        if (sc == null || sc.isEmpty()) return this;
        if (lore == null) lore = new ArrayList<>();
        for (final String s : sc) lore.add(TCUtils.form(s));
        return this;
    }

    @Deprecated
    public ItemBuilder addLore(final Component c) {
        if (c == null) return this;
        if (lore == null) lore = new ArrayList<>();
        else lore.add(c);
        return this;
    }

    @Deprecated
    public ItemBuilder addLore(final List<Component> lc) {
        if (lc == null || lc.isEmpty()) return this;
        if (lore == null) lore = new ArrayList<>();
        lore.addAll(lc);
        return this;
    }

    @Deprecated
    public ItemBuilder addLore(final Component... lores) {
        if (lores == null) return this;
        for (final Component c : lores) {
            lore(c);
        }
        return this;
    }

    @Deprecated
    public ItemBuilder addLore(final String... lores) {
        if (lores == null) return this;
        for (final String c : lores) {
            lore(c);
        }
        return this;
    }

    @Deprecated
    public ItemBuilder addLore(Object o) {
        if (o == null) return this;
        if (lore == null) lore = new ArrayList<>();
        switch (o) {
            case String s -> {
                if (s.isEmpty()) lore.add(Component.empty());
                else lore.add(TCUtils.form(s));
            }
            case Component c -> lore.add(c);
            case Collection<?> c -> {
                for (Object x : c) {
                    addLore(x);
                }
            }
            case String[] ss -> {
                for (final String s : ss) {
                    lore.add(TCUtils.form(s));
                }
            }
            case Component[] cc -> Collections.addAll(lore, cc);
            default -> {
            }
        }
        //if (s.isEmpty()) lore.add(Component.text(""));
        //else lore.add(TCUtils.form(s));
        return this;
    }

    @Deprecated
    public ItemBuilder addLore(final Object... lores) {
        if (lores == null) return this;
        for (final Object o : lores) {
            addLore(o);
        }
        return this;
    }

    //иногда нужен простой быстрый метод - .deLore().lore(...)
    @Deprecated
    public ItemBuilder setLore(final List<Component> lore) {
        this.lore = lore;
        return this;
    }

    @Deprecated
    public ItemBuilder setLore(final Object o) {
        if (o == null) {
            lore = null;
            return this;
        }
        if (lore != null) {
            lore.clear();
        }
        return addLore(o);
    }

    @Deprecated
    public ItemBuilder setLore(final Object... lores) {
        if (lores == null) {
            lore = null;
            return this;
        }
        if (lores.length == 0) return this;
        if (lore != null) {
            lore.clear();
        }
        return addLore(lores);
    }

    public ItemBuilder lore(final String s) {
        if (s.isEmpty()) lore.add(Component.empty());
        else lore.add(TCUtils.form(s));
        return this;
    }

    public ItemBuilder lore(final Iterable<String> sc) {
        for (final String s : sc) lore.add(TCUtils.form(s));
        return this;
    }

    public ItemBuilder lore(final Component c) {
        if (lore == null) lore = new ArrayList<>();
        else lore.add(c);
        return this;
    }

    public ItemBuilder lore(final Collection<Component> lc) {
        if (lore == null) lore = new ArrayList<>();
        lore.addAll(lc);
        return this;
    }

    public ItemBuilder lore(final Component... lores) {
        for (final Component c : lores) lore(c);
        return this;
    }

    public ItemBuilder lore(final String... lores) {
        for (final String c : lores) lore(c);
        return this;
    }

    public ItemBuilder repLore(final String from, final String to) {
        if (lore == null || lore.isEmpty()) return this;
        for (int i = 0; i < lore.size(); i++) {
            if (TCUtils.deform(lore.get(i)).equals(from)) {
                lore.set(i, TCUtils.form(to));
            }
        }
        return this;
    }

    public ItemBuilder repLore(final Component from, final Component to) {
        if (lore == null || lore.isEmpty()) return this;
        for (int i = 0; i < lore.size(); i++) {
            if (TCUtils.compare(lore.get(i), from)) {
                lore.set(i, to);
            }
        }
        return this;
    }

    @Deprecated
    public ItemBuilder replaceLore(final String from, final String to) {
        return repLore(TCUtils.form(from), TCUtils.form(to));
    }

    @Deprecated
    public ItemBuilder replaceLore(final Component from, final Component to) {
        if (lore == null || lore.isEmpty()) return this;
        for (int i = 0; i < lore.size(); i++) {
            if (TCUtils.compare(lore.get(i), from)) {
                lore.set(i, to);
            }
        }
        return this;
    }

    public ItemBuilder flags(final ItemFlag... flags) {
        checkMeta();
        meta.addItemFlags(flags);
        return this;
    }

    @Deprecated
    public ItemBuilder addFlags(final ItemFlag... flags) {
        return flags(flags);
    }

    public ItemBuilder trim(final TrimMaterial mat, final TrimPattern pat) {
        checkMeta();
        if (meta instanceof ArmorMeta) {
            ((ArmorMeta) meta).setTrim(new ArmorTrim(mat, pat));
        }
        return this;
    }

    @Deprecated
    public void setTrim(final TrimMaterial mat, final TrimPattern pat) {
        trim(mat, pat);
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

    @Deprecated
    public ItemBuilder addEnchant(final Enchantment enchantment) {
        return enchant(enchantment, 1);
    }

    @Deprecated
    public ItemBuilder addEnchant(final Enchantment enchantment, final int level) {
        return enchant(enchantment, level);
    }

    @Deprecated
    public ItemBuilder unsafeEnchantment(final Enchantment enchantment, final int level) {
        return enchant(enchantment, level);
    }

    @Deprecated
    public ItemBuilder clearEnchantment() {
        return disEnchant();
    }

    @Deprecated
    public ItemBuilder clearEnchants() {
        return disEnchant();
    }

    public ItemBuilder unbreak(final boolean unbreakable) {
        checkMeta();
        meta.setUnbreakable(unbreakable);
        return this;
    }

    @Deprecated
    public ItemBuilder setUnbreakable(final boolean unbreakable) {
        return unbreak(unbreakable);
    }

    @Deprecated
    public ItemBuilder setItemFlag(final ItemFlag flag) {
        checkMeta();
        meta.addItemFlags(flag);
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder attribute(final Attribute attribute, final double amount, final AttributeModifier.Operation op) {
        return attribute(attribute, amount, op, type.asMaterial().getEquipmentSlot().getGroup());
    }

    public ItemBuilder attribute(final Attribute attribute, final double amount,
                                 final AttributeModifier.Operation op, @Nullable final EquipmentSlotGroup slot) {
        checkMeta();
        meta.addAttributeModifier(attribute, new AttributeModifier(attribute.getKey(), amount, op, slot));
        return this;
    }

    @Deprecated
    public ItemBuilder setAttribute(final Attribute attribute, final double amount, final Operation op) {
        return attribute(attribute, amount, op);
    }

    @Deprecated
    public ItemBuilder setAttribute(final Attribute attribute, final double amount, final Operation op, @Nullable final EquipmentSlotGroup slotGroup) {
        return attribute(attribute, amount, op, slotGroup);
    }

    public ItemBuilder removeAttribute(final Attribute attribute) {
        checkMeta();
        meta.removeAttributeModifier(attribute);
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder removeSlotAttribute() {
        checkMeta();
        meta.removeAttributeModifier(type.asMaterial().getEquipmentSlot());
        return this;
    }

    public ItemBuilder modelData(final int data) {
        checkMeta();
        meta.setCustomModelData(data);
        return this;
    }

    @Deprecated
    public ItemBuilder setModelData(final int data) {
        return modelData(data);
    }

    public ItemBuilder durability(final int dur) {
        checkMeta();
        if (meta instanceof final Damageable dr && dr.hasMaxDamage()) {
            final int mxd = dr.getMaxDamage();
            if (dur < mxd) dr.setDamage(Math.max(mxd - dur, 0));
            dr.resetDamage();
        }
        return this;
    }

    public ItemBuilder durability(final float dur) {
        checkMeta();
        if (meta instanceof final Damageable dr && dr.hasMaxDamage()) {
            if (dur == 1f) dr.resetDamage();
            else dr.setDamage((int) (dr.getMaxDamage() * (1f - Math.clamp(dur, 0f, 1f))));
        }
        return this;
    }

    @Deprecated
    public ItemBuilder setDurability(final int dur) {
        return durability(dur);
    }

    public ItemBuilder maxDamage(final int dur) {
        checkMeta();
        if (meta instanceof final Damageable dr) {
            final float rel = (float) dr.getDamage() / (float) dr.getMaxDamage();
            dr.setMaxDamage(dur);
            dr.setDamage((int) (dur * rel));
        }
        return this;
    }

    @Deprecated
    public <M extends ItemMeta> ItemBuilder applyCustomMeta(final Class<M> metaType, final Consumer<M> metaApplier) {
        return customMeta(metaType, metaApplier);
    }

    public ItemBuilder glint(final @Nullable Boolean glint) {
        if (glint == null && (meta == null
            || !meta.hasEnchantmentGlintOverride())) return this;
        checkMeta();
        meta.setEnchantmentGlintOverride(glint);
        return this;
    }


    public ItemBuilder skullOf(final OfflinePlayer pl) {
        skullOwnerUuid = pl.getUniqueId();
        return this;
    }

    public ItemBuilder skullOf(final UUID id) {
        skullOwnerUuid = id;
        return this;
    }

    @Deprecated
    public ItemBuilder setSkullOwner(final OfflinePlayer player) {
        return skullOf(player);
    }

    @Deprecated
    public ItemBuilder setSkullOwnerUuid(final String uuidAsString) {
        return skullOf(UUID.fromString(uuidAsString));
    }

    /**
     * @param texture <a href="https://minecraft-heads.com/custom-heads/">...</a>
     * @return
     */
    public ItemBuilder headTexture(final String texture) {
        this.skullTexture = texture;
        return this;
    }

    public ItemBuilder headTexture(final ItemUtils.Texture texture) {
        return headTexture(texture.value);
    }

    @Deprecated
    public ItemBuilder setCustomHeadTexture(final String texture) {
        return headTexture(texture);
    }

    @Deprecated
    public ItemBuilder setCustomHeadTexture(final Texture texture) {
        return headTexture(texture.value);
    }

    public ItemBuilder color(final Color color) {
        this.color = color;
        return this;
    }

    @Deprecated
    public ItemBuilder setColor(final Color color) {
        return color(color);
    }

    public ItemBuilder basePotion(final PotionType type) {
        this.basePotionType = type;
        return this;
    }

    public ItemBuilder customPotion(final PotionEffect customPotionEffect) {
        if (customPotionEffects == null) customPotionEffects = new ArrayList<>();
        customPotionEffects.add(customPotionEffect);
        return this;
    }

    public ItemBuilder clearPotions() {
        if (customPotionEffects != null) customPotionEffects = null;
        if (basePotionType != null) basePotionType = null;
        return this;
    }

    @Deprecated
    public ItemBuilder setBasePotionType(final PotionType type) {
        return basePotion(type);
    }

    @Deprecated
    public ItemBuilder addCustomPotionEffect(final PotionEffect customPotionEffect) {
        return customPotion(customPotionEffect);
    }

    public ItemStack build() {
        if (amount < 1) {
            return ItemUtils.air.clone();
        } else if (amount > maxStack) {
            amount = maxStack;
        }
        final ItemStack item = type.createItemStack(amount);
        if (meta == null) meta = item.getItemMeta();
        if (meta == null) return item;

        if (maxStack != type.getMaxStackSize()) {
            meta.setMaxStackSize(maxStack);
        }
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }

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
                    ItemUtils.setHeadTexture(sm, skullTexture);
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
}
