package ru.komiss77.utils;

import javax.annotation.Nonnull;
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

//im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
//Validate.isTrue(item.getType() == Material.PLAYER_HEAD, "skullOwner() only applicable for skulls!", new Object[0]);


public class ItemBuilder {

    private ItemType type;//private Material mat;//private final ItemStack item; item не используем - в будущем тип менять нельзя будет
    private int amount;
    private int maxStack;
    private @Nullable ItemMeta meta;
    private Color color;
    private List<Component> lore;
    private String skullOwnerUuid;
    private String skullTexture;
    private PotionType basePotionType;
    private List<PotionEffect> customPotionEffects = null;
    private Map<Enchantment, Integer> enchants = null;

    //use StackBuilder
    public ItemBuilder(final Material material) {
        type = material.asItemType();//mat = material;
        meta = null;
        lore = new ArrayList<>();
        maxStack = material.getMaxStackSize();
    }

    public ItemBuilder(final ItemType tp) {
        type = tp;
        meta = null;
        lore = new ArrayList<>();
        amount = 1;
        maxStack = tp.getMaxStackSize();
    }

    //use StackBuilder
    public ItemBuilder(final ItemStack from) {
        //item = from==null ? new ItemStack(Material.AIR) : new ItemStack(from.getType(), from.getAmount());
        //mat = from == null ? Material.AIR : from.getType();
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
        final NamespacedKey nsk = key == null ? ItemUtil.key : new NamespacedKey(Ostrov.instance, key);
        meta.getPersistentDataContainer().set(nsk, pdt, data);
        return this;
    }

    public ItemBuilder asType(final ItemType tp) {
        type = tp;
        if (meta == null) return this;
        checkMeta();
        return this;
    }

    //ну тип переделал билдер на материал @Deprecated //в будующем тип менять нельзя будет
    public ItemBuilder type(final Material mat) {
        if (mat == null) return this;
        type = mat.asItemType();
        if (meta == null) return this;
        checkMeta();
        //meta = Bukkit.getItemFactory().asMetaFor(meta, mat);
        return this;
    }

    public ItemType type() {
        return type;
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

    /**
     * @param name null-сброс, или String/Component
     * @return
     */
    public ItemBuilder name(@Nullable final Object name) {
        checkMeta();//meta = item.getItemMeta();
        if (name == null) {
            meta.displayName(null);
        } else if (name instanceof String) {
            meta.displayName(TCUtil.form((String) name));
        } else if (name instanceof Component) {
            meta.displayName((Component) name);
        }
        return this;
    }


    public ItemBuilder persistentData(@Nullable final String key, @Nonnull final Object data) {
        checkMeta();//meta = item.getItemMeta();
        final NamespacedKey nsk = key == null ? ItemUtil.key : new NamespacedKey(Ostrov.instance, key);
        if (data instanceof Integer) {
            meta.getPersistentDataContainer().set(nsk, PersistentDataType.INTEGER, (Integer) data);
        } else if (data instanceof String) {
            meta.getPersistentDataContainer().set(nsk, PersistentDataType.STRING, (String) data);
        }
        return this;
    }
    /*
    public ItemBuilder persistentData(final String key, final String data) {
        if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(mat);//meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Ostrov.instance, key), PersistentDataType.STRING, data);
        return this;
    }

    public ItemBuilder persistentData(final String key, final int data) {
        if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(mat);//meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Ostrov.instance, key), PersistentDataType.INTEGER, data);
        return this;
    }

    public ItemBuilder persistentData(final String data) {
        if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(mat);//meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemUtils.key, PersistentDataType.STRING, data);
        return this;
    }

    public ItemBuilder persistentData(final int data) {
        if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(mat);//meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemUtils.key, PersistentDataType.INTEGER, data);
        return this;
    }*/

/*
    public ItemBuilder name(@Nullable final Component name) {
        if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(mat);//meta = item.getItemMeta();
        meta.displayName(name);
        return this;
    }

    public ItemBuilder addLore(final String s) {
        if (s == null) return this;
        if (lore == null) lore = new ArrayList<>();
        if (s.isEmpty()) lore.add(Component.text(""));
        else lore.add(TCUtils.format(s));
        return this;
    }

    public ItemBuilder addLore(final Collection<String> sc) {
        if (sc == null || sc.isEmpty()) return this;
        if (lore == null) lore = new ArrayList<>();
        for (final String s : sc) lore.add(TCUtils.format(s));
        return this;
    }

    public ItemBuilder addLore(final Component c) {
        if (c == null) return this;
        if (lore == null) lore = new ArrayList<>();
        else lore.add(c);
        return this;
    }

    public ItemBuilder addLore(final List<Component> lc) {
        if (lc == null || lc.isEmpty()) return this;
        if (lore == null) lore = new ArrayList<>();
        lore.addAll(lc);
        return this;
    }

    public ItemBuilder addLore(final Component... lores) {
        if (lores == null) return this;
        for (final Component c : lores) {
            addLore(c);
        }
        return this;
    }

    public ItemBuilder addLore(final String... lores) {
        if (lores == null) return this;
        for (final String c : lores) {
            addLore(c);
        }
        return this;
    }*/

    //@Deprecated

    /**
     * @param o = String, Component, Collection<String>, Collection<Component>, Component[]
     * @return
     */
    public ItemBuilder lore(Object o) {
        if (o == null) return this;
        if (lore == null) lore = new ArrayList<>();
        if (o instanceof String s) {
            if (s.isEmpty()) lore.add(Component.empty());
            else lore.add(TCUtil.form(s));
        } else if (o instanceof Component c) {
            lore.add(c);
        } else if (o instanceof Collection<?> c) {
            for (Object x : c) {
                lore(x);
            }
        } else if (o instanceof String[] ss) {
            for (final String s : ss) {
                lore.add(TCUtil.form(s));
            }
        } else if (o instanceof Component[] cc) {
            Collections.addAll(lore, cc);
        }
        //if (s.isEmpty()) lore.add(Component.text(""));
        //else lore.add(TCUtils.format(s));
        return this;
    }

    //@Deprecated
    public ItemBuilder lore(final Object... lores) {
        if (lores == null) return this;
        for (final Object o : lores) {
            lore(o);
        }
        return this;
    }

    //иногда нужен простой быстрый метод
    //public ItemBuilder setLore(final List<Component> lore) { //иногда нужен простой быстрый метод
    //    this.lore = lore;
    //     return this;
    //}

    @Deprecated
    public ItemBuilder setLore(final Object o) { //иногда нужен простой быстрый метод
        if (o == null) {
            lore = null;
            return this;
        }
        if (lore != null) {
            lore.clear();
        }
        return lore(o);
    }

    /* @Deprecated
     public ItemBuilder setLore(final Object... lores) {
         if (lores == null) {
             lore = null;
             return this;
         }
         if (lores.length == 0) return this;
         if (lore != null) {
             lore.clear();
         }
         return lore(lores);
     }

 */
    public ItemBuilder deLore() {
        if (lore == null) {
            lore = new ArrayList<>();
        } else {
            lore.clear();
        }
        return this;
    }

    public ItemBuilder repLore(final String from, final String to) {
        return repLore(TCUtil.form(from), TCUtil.form(to));
    }

    public ItemBuilder repLore(final Component from, final Component to) {
        //final List<Component> lores = meta.lore();
        if (lore == null || lore.isEmpty()) return this;
        for (int i = 0; i < lore.size(); i++) {
            if (TCUtil.compare(lore.get(i), from)) {
                lore.set(i, to);
            }
        }
        //meta.lore(lores);
        return this;
    }


    public ItemBuilder flags(final ItemFlag... flags) {
        checkMeta();// mat.item.getItemMeta();
        meta.addItemFlags(flags);
        return this;
    }


    public void trim(final TrimMaterial mat, final TrimPattern pat) {
        checkMeta();
        if (meta instanceof ArmorMeta) {
            ((ArmorMeta) meta).setTrim(new ArmorTrim(mat, pat));
        }
    }

    public ItemBuilder enchant(final Enchantment enchantment) {
        return enchant(enchantment, 1);
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
    /*@Deprecated
    public ItemBuilder unsafeEnchantment(final Enchantment enchantment, final int level) {
        return enchant(enchantment, level);
    }

    @Deprecated
    public ItemBuilder clearEnchantment() {
        return clearEnchants();
    }


    public ItemBuilder clearEnchants() {
        if (meta != null) meta.removeEnchantments();
        enchants.clear();
        return this;
    }*/


    public ItemBuilder unbreak(final boolean unbreakable) {
        checkMeta();//meta = item.getItemMeta();
        meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder flags(final ItemFlag flag) {
        checkMeta();//meta = item.getItemMeta();
        meta.addItemFlags(flag);
        return this;
    }

   // public ItemBuilder attribute(final Attribute attribute, final double amount, final Operation op) {
    //      attribute(attribute, amount, op, mat.getEquipmentSlot().getGroup());//setAttribute(attribute, amount, op, item.getType().getEquipmentSlot());
    //     return this;
    //  }

    public ItemBuilder attribute(final Attribute attribute, final double amount, final Operation op, @Nullable final EquipmentSlotGroup slotGroup) {
        checkMeta();//item.getItemMeta();
        meta.addAttributeModifier(attribute, new AttributeModifier(attribute.getKey(), amount, op, slotGroup));
        return this;
    }

    public ItemBuilder removeAttribute(final Attribute attribute) {
        checkMeta();//item.getItemMeta();
        meta.removeAttributeModifier(attribute);
        return this;
    }

    //public ItemBuilder removeSlotAttribute() {
    //    if (meta == null)
    //        meta = Bukkit.getItemFactory().getItemMeta(mat);//item.getItemMeta();//meta.removeAttributeModifier(item.getType().getEquipmentSlot());
    //     meta.removeAttributeModifier(mat.getEquipmentSlot());
    //     return this;
    //}

    public ItemBuilder modelData(final int data) {
        checkMeta();//item.getItemMeta();
        meta.setCustomModelData(data);
        return this;
    }

    public ItemBuilder durability(final int dur) {
        checkMeta();
        if (meta instanceof final Damageable dr && dr.hasMaxDamage()) {
            final int mxd = dr.getMaxDamage();
            if (dur < mxd) dr.setDamage(Math.max(mxd - dur, 0));
            dr.resetDamage();
        }
        return this;
        //final int mdr = mat.getMaxDurability(); //final int mdr = item.getType().getMaxDurability();
        //checkMeta();//item.getItemMeta();
        //if (meta instanceof Damageable) ((Damageable) meta).setDamage(dur < mdr ? mdr - dur : 0);
        //return this;
    }

    public ItemBuilder durability(final float dur) {
        checkMeta();
        if (meta instanceof final Damageable dr && dr.hasMaxDamage()) {
            if (dur == 1f) dr.resetDamage();
            else dr.setDamage((int) (dr.getMaxDamage() * (1f - Math.clamp(dur, 0f, 1f))));
        }
        return this;
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

    public ItemBuilder glint(final @Nullable Boolean glint) {
        if (glint == null && (meta == null
                || !meta.hasEnchantmentGlintOverride())) return this;
        checkMeta();
        meta.setEnchantmentGlintOverride(glint);
        return this;
    }

    public ItemBuilder skullOf(final OfflinePlayer player) {
        skullOwnerUuid = player.getUniqueId().toString();
        return this;
    }

    public ItemBuilder skullOf(final String uuidAsString) {
        skullOwnerUuid = uuidAsString;
        return this;
    }

    /**
     * @param texture <a href="https://minecraft-heads.com/custom-heads/">...</a>
     * @return
     */
    public ItemBuilder headTexture(final String texture) {
        //if (texture.length()<70) return setCustomHeadUrl(texture); //фикс!!
        this.skullTexture = texture;
        return this;
    }

    public ItemBuilder headTexture(final Texture texture) {
        return headTexture(texture.value);
    }

    // public ItemBuilder setCustomHeadUrl(final String url) {
    //     if (!url.startsWith("http://")) skullTexture = "http://textures.minecraft.net/texture/" + url;
    //      else skullTexture = url;
    //      return this;
    //  }


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
