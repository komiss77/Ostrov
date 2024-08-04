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

    private ItemType type;//private Material mat;
    private int amount = 1; //по умолчанию 1, или build() выдаёт AIR!
    private int maxStack;
    private @Nullable ItemMeta meta;
    private Color color;
    private List<Component> lore;
    private String skullOwnerUuid;
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
            type = mat.asItemType();//meta = null;при создании поле итак null
            //lore = new ArrayList<>(); создаём только, если надо
            maxStack = mat.getMaxStackSize();
        }
    }

    public ItemBuilder(final ItemType itemType) {
        if (itemType == null) {
            wrong = true;
            lore = List.of(Component.text("itemType==null"));
        } else {
            type = itemType;//meta = null;при создании поле итак null
            //lore = new ArrayList<>(); создаём только, если надо
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
        return true;
    }

    public <M extends ItemMeta> ItemBuilder customMeta(final Class<M> meta, final Consumer<M> applier) {
        if (checkMeta()) {
            if (meta.isInstance(this.meta)) {
                applier.accept(meta.cast(this.meta));
            }
        }
        return this;
    }

    public ItemBuilder asType(final ItemType tp) {
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

    public ItemBuilder data(final String key, final Serializable data) {
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

    /**
     * @param name null-сброс, или String/Component
     * @return
     */
    public ItemBuilder name(@Nullable final Object name) {
        if (checkMeta()) {
            if (name == null) {
                meta.displayName(null);
            } else if (name instanceof String) {
                meta.displayName(TCUtil.form((String) name));
            } else if (name instanceof Component) {
                meta.displayName((Component) name);
            }
        }
        return this;
    }


    public ItemBuilder persistentData(@Nullable final String key, @Nonnull final Object data) {
        if (checkMeta()) {
            final NamespacedKey nsk = key == null ? ItemUtil.key : new NamespacedKey(Ostrov.instance, key);
            if (data instanceof Integer) {
                meta.getPersistentDataContainer().set(nsk, PersistentDataType.INTEGER, (Integer) data);
            } else if (data instanceof String) {
                meta.getPersistentDataContainer().set(nsk, PersistentDataType.STRING, (String) data);
            }
        }
        return this;
    }

    /**
     * Добавить нечто к лор. null игнорируется
     * @param o = String, Component, Collection<String>, Collection<Component>, Component[]
     * @return ItemBuilder
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

    /**
     * Добавить несколько нечто к лор. null игнорируется
     *
     * @param lores = String..., Component...
     * @return ItemBuilder
     */
    public ItemBuilder lore(final Object... lores) {
        if (lores == null) return this;
        for (final Object o : lores) {
            lore(o);
        }
        return this;
    }

    /**
     * Поставить свой лор. null-удалить лор (вместо deLore)
     * @param o
     * @return
     */
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


    /**
     * Замена строки или компонента в лоре
     *
     * @param from String or Component
     * @param to   String or Component
     * @return ItemBuilder
     */
    public ItemBuilder repLore(Object from, Object to) {
        if (lore == null || lore.isEmpty()) return this;
        if (from instanceof String) {
            from = TCUtil.form((String) from);
        }
        if (to instanceof String) {
            to = TCUtil.form((String) to);
        }
        for (int i = 0; i < lore.size(); i++) {
            if (TCUtil.compare(lore.get(i), (Component) from)) {
                lore.set(i, (Component) to);
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
            if (meta instanceof ArmorMeta) {
                ((ArmorMeta) meta).setTrim(new ArmorTrim(mat, pat));
            }
        }
    }

    public ItemBuilder enchant(final Enchantment enchant, final int level) {
        if (enchants == null) enchants = new HashMap<>();
        if (level < 1) enchants.remove(enchant);
        else enchants.put(enchant, level);
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

   /* вроде очень редко нужно, думаю не страшно будет ставить всегда одним методом через float
    public ItemBuilder durability(final int dur) {
        if (checkMeta()) {
            if (meta instanceof final Damageable dr && dr.hasMaxDamage()) {
                final int mxd = dr.getMaxDamage();
                if (dur < mxd) dr.setDamage(Math.max(mxd - dur, 0));
                dr.resetDamage();
            }
        }
        return this;
    }*/

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

    public ItemBuilder glint(final @Nullable Boolean glint) {
        if (glint == null && (meta == null || !meta.hasEnchantmentGlintOverride())) return this;
        if (checkMeta()) {
            meta.setEnchantmentGlintOverride(glint);
        }
        return this;
    }

    /**
     * поставить владельца головы
     *
     * @param skullOwner OfflinePlayer, UUID или UUID.asString
     * @return
     */
    public ItemBuilder skullOf(Object skullOwner) {
        if (skullOwner instanceof OfflinePlayer op) {
            skullOwnerUuid = op.getUniqueId().toString();
        } else if (skullOwner instanceof UUID) {
            skullOwnerUuid = ((UUID) skullOwner).toString();
        } else if (skullOwner instanceof String) {
            skullOwnerUuid = (String) skullOwner;
        }
        return this;
    }

    /**
     * @param texture Texture или String вида <a href="https://minecraft-heads.com/custom-heads/">...</a>
     * @return ItemBuilder
     */
    public ItemBuilder headTexture(Object texture) {
        //if (texture.length()<70) return setCustomHeadUrl(texture); //фикс!!
        if (texture == null) return this;
        if (texture instanceof Texture) {
            texture = headTexture(((Texture) texture).value);
        }
        if (texture instanceof String) {
            this.skullTexture = (String) texture;
        }
        return this;
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
}


//public ItemBuilder skullOf(final String uuidAsString) {
//     skullOwnerUuid = uuidAsString;
//     return this;
// }

//public ItemBuilder headTexture(final Texture texture) {
//    return headTexture(texture.value);
//}

// public ItemBuilder attribute(final Attribute attribute, final double amount, final Operation op) {
//      attribute(attribute, amount, op, mat.getEquipmentSlot().getGroup());//setAttribute(attribute, amount, op, item.getType().getEquipmentSlot());
//     return this;
//  }


//public ItemBuilder enchant(final Enchantment enchantment) {
//     return enchant(enchantment, 1);
// }

//public ItemBuilder removeSlotAttribute() {
//    if (meta == null)
//        meta = Bukkit.getItemFactory().getItemMeta(mat);//item.getItemMeta();//meta.removeAttributeModifier(item.getType().getEquipmentSlot());
//     meta.removeAttributeModifier(mat.getEquipmentSlot());
//     return this;
//}


// public ItemBuilder setCustomHeadUrl(final String url) {
//     if (!url.startsWith("http://")) skullTexture = "http://textures.minecraft.net/texture/" + url;
//      else skullTexture = url;
//      return this;
//  }


//public ItemBuilder repLore(final String from, final String to) {
//    return repLore(TCUtil.form(from), TCUtil.form(to));
//}

// public ItemBuilder disEnchant() {
//     if (meta != null) meta.removeEnchantments();
//     if (enchants != null) enchants.clear();
//    return this;
// }


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


//иногда нужен простой быстрый метод
//public ItemBuilder setLore(final List<Component> lore) { //иногда нужен простой быстрый метод
//    this.lore = lore;
//     return this;
//}

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


    public ItemBuilder deLore() {
        if (lore == null) {
            lore = new ArrayList<>();
        } else {
            lore.clear();
        }
        return this;
    }
 */

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
