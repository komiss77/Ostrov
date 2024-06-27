package ru.komiss77.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
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
import ru.komiss77.OStrap;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class StackBuilder {

  private ItemStack item;
  private @Nullable ItemMeta meta;
  private Color color;
  private List<Component> lore;
  private UUID skullOwnerUuid;
  private String skullTexture;
  private PotionType basePotionType;
  private List<PotionEffect> customPotionEffects=null;
  private Map<Enchantment, Integer> enchants=null;

  private StackBuilder(final ItemType it) {
    item = it.createItemStack();
    meta = null;
    lore = new ArrayList<>();
  }

  private StackBuilder(final ItemStack from) {
    item = from==null ? ItemType.AIR.createItemStack() : new ItemStack(from.getType(), from.getAmount());
    meta = from != null && from.hasItemMeta() ? from.getItemMeta() : null;
    lore = meta != null && meta.hasLore() ? meta.lore() : new ArrayList<>();
  }

  public static StackBuilder of(final ItemType it) {
    return new StackBuilder(it);
  }

  public static StackBuilder of(final ItemStack from) {
    return new StackBuilder(from);
  }

  public StackBuilder as(final ItemType it) {
    item = it.createItemStack(item.getAmount());
    if (meta == null) return this;
    meta = Bukkit.getItemFactory().asMetaFor(meta, item);
    return this;
  }

  public StackBuilder persistentData(final String key, final String data) {
    if (meta == null) meta = item.getItemMeta();
    meta.getPersistentDataContainer().set(OStrap.key(key), PersistentDataType.STRING, data);
    return this;
  }
  public StackBuilder persistentData(final String key, final int data) {
    if (meta == null) meta = item.getItemMeta();
    meta.getPersistentDataContainer().set(OStrap.key(key), PersistentDataType.INTEGER, data);
    return this;
  }

  public ItemType getType() {
    return item.getType().asItemType();
  }

  public StackBuilder setAmount(final int amount) {
    item.setAmount(amount);
    return this;
  }

  public StackBuilder name(@Nullable final String name) {
    if (meta == null) meta = item.getItemMeta();
    if (name == null) meta.displayName(null);
    else meta.displayName(TCUtils.form(name));
    return this;
  }

  public StackBuilder name(@Nullable final Component name) {
    if (meta == null) meta = item.getItemMeta();
    meta.displayName(name);
    return this;
  }

  public StackBuilder deLore() {
    lore.clear();
    return this;
  }

  public StackBuilder lore(final String s) {
    if (s.isEmpty()) lore.add(Component.empty());
    else lore.add(TCUtils.form(s));
    return this;
  }

  public StackBuilder lore(final Iterable<String> sc) {
    for (final String s : sc) lore.add(TCUtils.form(s));
    return this;
  }

  public StackBuilder lore(final Component c) {
    if (lore == null) lore = new ArrayList<>();
    else lore.add(c);
    return this;
  }

  public StackBuilder lore(final Collection<Component> lc) {
    if (lore == null) lore = new ArrayList<>();
    lore.addAll(lc);
    return this;
  }

  public StackBuilder lore(final Component... lores) {
    for (final Component c : lores) lore(c);
    return this;
  }

  public StackBuilder lore(final String... lores) {
    for (final String c : lores) lore(c);
    return this;
  }

  public StackBuilder repLore(final String from, final String to) {
    if (lore==null || lore.isEmpty()) return this;
    for (int i=0; i<lore.size(); i++) {
      if (TCUtils.deform(lore.get(i)).equals(from)) {
        lore.set(i, TCUtils.form(to));
      }
    }
    return this;
  }

  public StackBuilder repLore(final Component from, final Component to) {
    if (lore==null || lore.isEmpty()) return this;
    for (int i=0; i<lore.size(); i++) {
      if (TCUtils.compare(lore.get(i), from)) {
        lore.set(i, to);
      }
    }
    return this;
  }


  public StackBuilder flags(final ItemFlag... flags) {
    if (meta == null) meta = item.getItemMeta();
    meta.addItemFlags(flags);
    return this;
  }


  public StackBuilder trim(final TrimMaterial mat, final TrimPattern pat) {
    if (meta == null) meta = item.getItemMeta();
    if (meta instanceof ArmorMeta) {
      ((ArmorMeta) meta).setTrim(new ArmorTrim(mat, pat));
    }
    return this;
  }

  public StackBuilder enchant(final Enchantment enchant) {
    return enchant(enchant, 1);
  }

  public StackBuilder enchant(final Enchantment enchant, final int level) {
    if (enchants==null) enchants = new HashMap<>();
    if (level < 1) enchants.remove(enchant);
    else enchants.put(enchant, level);
    return this;
  }

  public StackBuilder disEnchant() {
    if (meta != null) meta.removeEnchantments();
    if (enchants!=null) enchants.clear();
    return this;
  }



  public StackBuilder unbreak(final boolean unbreakable) {
    if (meta == null) meta = item.getItemMeta();
    meta.setUnbreakable(unbreakable);
    return this;
  }

  public StackBuilder attribute(final Attribute attribute, final double amount, final AttributeModifier.Operation op) {
    attribute(attribute, amount, op, item.getType().getEquipmentSlot().getGroup());
    return this;
  }

  public StackBuilder attribute(final Attribute attribute, final double amount, final AttributeModifier.Operation op, @Nullable final EquipmentSlotGroup slot) {
    if (meta == null) meta = item.getItemMeta();
    meta.addAttributeModifier(attribute, new AttributeModifier(attribute.getKey(), amount, op, slot));
    return this;
  }

  public StackBuilder removeAttribute(final Attribute attribute) {
    if (meta == null) meta = item.getItemMeta();
    meta.removeAttributeModifier(attribute);
    return this;
  }

  public StackBuilder removeSlotAttribute() {
    if (meta == null) meta = item.getItemMeta();
    meta.removeAttributeModifier(item.getType().getEquipmentSlot());
    return this;
  }

  public StackBuilder modelData(final int data) {
    if (meta == null) meta = item.getItemMeta();
    meta.setCustomModelData(data);
    return this;
  }

  public StackBuilder durability(final int dur) {
    if (meta == null) meta = item.getItemMeta();
    if (meta instanceof final Damageable dr) {
      final int mxd = dr.getMaxDamage();
      dr.setDamage(Math.clamp(dur, 0, mxd));
    }
    return this;
  }

  public StackBuilder durability(final float dur) {
    if (meta == null) meta = item.getItemMeta();
    if (meta instanceof final Damageable dr) {
      dr.setDamage((int) (dr.getMaxDamage() * (1f - Math.clamp(dur, 0f, 1f))));
    }
    return this;
  }

  public StackBuilder maxDamage(final int dur) {
    if (meta == null) meta = item.getItemMeta();
    if (meta instanceof final Damageable dr) {
      final float rel = (float) dr.getDamage() / (float) dr.getMaxDamage();
      dr.setMaxDamage(dur);
      dr.setDamage((int) (dur * rel));
    }
    return this;
  }

  public <M extends ItemMeta> StackBuilder applyCustomMeta(final Class<M> metaType, final Consumer<M> metaApplier) {
    if (meta == null) meta = item.getItemMeta();
    if (metaType.isInstance(meta)) {
      metaApplier.accept(metaType.cast(meta));
    }
    return this;
  }

  public StackBuilder glint(final @Nullable Boolean glint) {
    if (glint == null && (meta == null
      || !meta.hasEnchantmentGlintOverride())) return this;
    if (meta == null) meta = item.getItemMeta();
    meta.setEnchantmentGlintOverride(glint);
    return this;
  }











  public StackBuilder skullOf(final OfflinePlayer pl) {
    skullOwnerUuid = pl.getUniqueId();
    return this;
  }

  public StackBuilder skullOf(final UUID id) {
    skullOwnerUuid = id;
    return this;
  }

  /**
   * @param texture <a href="https://minecraft-heads.com/custom-heads/">...</a>
   * @return
   */
  public StackBuilder headTexture(final String texture) {
    //if (texture.length()<70) return setCustomHeadUrl(texture); //фикс!!
    this.skullTexture = texture;
    return this;
  }

  public StackBuilder headTexture(final ItemUtils.Texture texture) {
    return headTexture(texture.value);
  }

  public StackBuilder color(final Color color) {
    this.color=color;
    return this;
  }

  public StackBuilder basePotion(final PotionType type) {
    this.basePotionType = type;
    return this;
  }

  public StackBuilder customPotion(final PotionEffect customPotionEffect) {
    if (customPotionEffects==null) customPotionEffects = new ArrayList<>();
    customPotionEffects.add(customPotionEffect);
    return this;
  }

  public StackBuilder clearPotions() {
    if (customPotionEffects!=null) customPotionEffects = null;
    if (basePotionType!=null) basePotionType = null;
    return this;
  }
















  private final Set<ItemType> POTIONS = Set.of(ItemType.POTION,
    ItemType.TIPPED_ARROW, ItemType.LINGERING_POTION, ItemType.SPLASH_POTION);
  private final Set<ItemType> COLORABLE = Set.of(ItemType.LEATHER_BOOTS, ItemType.LEATHER_CHESTPLATE,
    ItemType.LEATHER_HELMET, ItemType.LEATHER_LEGGINGS, ItemType.LEATHER_HORSE_ARMOR, ItemType.WOLF_ARMOR);

  public ItemStack build() {
    final ItemMeta im = meta == null ? item.getItemMeta() : meta;
    if (!lore.isEmpty()) {
      im.lore(lore);
    }

    switch (im) {
      case final PotionMeta pm:
        if (basePotionType!=null || customPotionEffects!=null) {
          if (basePotionType!=null) pm.setBasePotionType(basePotionType);
          if (customPotionEffects!=null && !customPotionEffects.isEmpty()) {
            for (final PotionEffect ef : customPotionEffects) {
              pm.addCustomEffect(ef,true);
            }
          }
          if (color!=null) {
            pm.setColor(color);
          }
        }
        break;

      case final SkullMeta sm:
        if (skullOwnerUuid!=null) {
          final OfflinePlayer ofp = Bukkit.getOfflinePlayer(skullOwnerUuid);
          sm.setOwningPlayer(ofp);
        }

        if (skullTexture!=null && !skullTexture.isEmpty()) {
          ItemUtils.setHeadTexture(sm, skullTexture);
        }
        break;

      case final LeatherArmorMeta lam:
        if (color!=null) {
          lam.setColor(color);
        }
        break;

      case final EnchantmentStorageMeta esm://для книг чары в storage
        if (enchants!=null && !enchants.isEmpty()) {
          for (final Map.Entry<Enchantment, Integer> en : enchants.entrySet()) {
            esm.addStoredEnchant(en.getKey(), en.getValue(), false);
          }
        }
        enchants = null;
        break;
      default: break; //для обычных предметов кидаем чары
    }

    if (enchants!=null && !enchants.isEmpty()) {
      for (final Map.Entry<Enchantment, Integer> en : enchants.entrySet()) {
        im.addEnchant(en.getKey(), en.getValue(), true);
      }
    }

    item.setItemMeta(im);
    return item;
  }
}
