package ru.komiss77.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
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
import java.util.*;
import java.util.function.Consumer;

        //im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        //Validate.isTrue(item.getType() == Material.PLAYER_HEAD, "skullOwner() only applicable for skulls!", new Object[0]);

@Deprecated
//use StackBuilder
public class ItemBuilder {
    
    
    private final ItemStack item;
    private @Nullable ItemMeta meta;
    private Color color;
    private List<Component> lore;
    private String skullOwnerUuid;
    private String skullTexture;
    private PotionType basePotionType;
    private List<PotionEffect> customPotionEffects=null;
    private Map<Enchantment, Integer> enchants=null;

   
    public ItemBuilder(final Material material) {
       item = new ItemStack(material);
       meta = null;
       lore = new ArrayList<>();
    }
   

    public ItemBuilder(final ItemStack from) {
        item = from==null ? new ItemStack(Material.AIR) : new ItemStack(from.getType(), from.getAmount());
        meta = from != null && from.hasItemMeta() ? from.getItemMeta() : null;
        lore = meta != null && meta.hasLore() ? meta.lore() : new ArrayList<>();
    }
   
    
    public ItemBuilder persistentData(final String key, final String data) {
      if (meta == null) meta = item.getItemMeta();
      meta.getPersistentDataContainer().set(new NamespacedKey(Ostrov.instance, key), PersistentDataType.STRING, data);
      return this;
    }   
    public ItemBuilder persistentData(final String key, final int data) {
      if (meta == null) meta = item.getItemMeta();
      meta.getPersistentDataContainer().set(new NamespacedKey(Ostrov.instance, key), PersistentDataType.INTEGER, data);
      return this;
    }
    public ItemBuilder persistentData(final String data) {
      if (meta == null) meta = item.getItemMeta();
      meta.getPersistentDataContainer().set(ItemUtils.key, PersistentDataType.STRING, data);
      return this;
    }   
    public ItemBuilder persistentData(final int data) {
      if (meta == null) meta = item.getItemMeta();
      meta.getPersistentDataContainer().set(ItemUtils.key, PersistentDataType.INTEGER, data);
      return this;
    }

    @Deprecated //в будующем тип менять нельзя будет
    public ItemBuilder setType(final Material material) {
        if (material==null) return this;
        item.setType(material);
        if (meta == null) return this;
        meta = Bukkit.getItemFactory().asMetaFor(meta, material);
        return this;
    }

    public Material getType() {
        return item.getType();
    }
    
    public ItemBuilder setAmount(final int amount) {
    	item.setAmount(amount);
    	return this;
    }
    
    public ItemBuilder name(@Nullable final String name) {
      if (meta == null) meta = item.getItemMeta();
      if (name == null) meta.displayName(null);
      else meta.displayName(TCUtils.format(name));
    	return this;
    }
    
    public ItemBuilder name(@Nullable final Component name) {
      if (meta == null) meta = item.getItemMeta();
      meta.displayName(name);
    	return this;
    }

    public ItemBuilder addLore(final String s) {
      if (s==null) return this;
      if (lore == null) lore = new ArrayList<>();
      if (s.isEmpty()) lore.add(Component.text(""));
      else lore.add(TCUtils.format(s));
      return this;
    }

    public ItemBuilder addLore(final Collection<String> sc) {
      if (sc==null || sc.isEmpty()) return this;
      if (lore == null) lore = new ArrayList<>();
      for (final String s : sc) lore.add(TCUtils.format(s));
      return this;
    }

    public ItemBuilder addLore(final Component c) {
      if (c==null) return this;
      if (lore == null) lore = new ArrayList<>();
      else lore.add(c);
      return this;
    }

    public ItemBuilder addLore(final List<Component> lc) {
      if (lc==null || lc.isEmpty()) return this;
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
    }
    
    @Deprecated
    public ItemBuilder addLore(Object o) {
      if (o==null) return this;
      if (lore == null) lore = new ArrayList<>();
      switch (o) {
          case String s -> {
              if (s.isEmpty()) lore.add(Component.text(""));
              else lore.add(TCUtils.format(s));
          }
          case Component c -> lore.add(c);
          case Collection<?> c -> {
              for (Object x : c) {
                  addLore(x);
              }
          }
          case String[] ss -> {
              for (final String s : ss) {
                  lore.add(TCUtils.format(s));
              }
          }
          case Component[] cc -> Collections.addAll(lore, cc);
          default -> {
          }
      }
      //if (s.isEmpty()) lore.add(Component.text(""));
      //else lore.add(TCUtils.format(s));
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

    //иногда нужен простой быстрый метод
    public ItemBuilder setLore(final List<Component> lore) {
        this.lore = lore;
        return this;
    }

    @Deprecated
    public ItemBuilder setLore(final Object o) {
        if (o==null) {
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
        if (lores==null) {
            lore = null;
            return this;
        }
        if (lores.length == 0) return this;
        if (lore != null) {
            lore.clear();
        }    	
        return addLore(lores);
    }


    
    
    
    public ItemBuilder clearLore() {
        if (lore==null) lore = new ArrayList<>();
        else lore.clear();
        return this;
    }
    
    public ItemBuilder replaceLore(final String from, final String to) {
        return replaceLore(TCUtils.format(from), TCUtils.format(to));
    }
    
    public ItemBuilder replaceLore(final Component from, final Component to) {
    	//final List<Component> lores = meta.lore();
        if (lore==null || lore.isEmpty()) return this;
        for (int i=0; i<lore.size(); i++) {
            if (TCUtils.compare(lore.get(i), from)) {
                lore.set(i, to);
            }
        }
        //meta.lore(lores);
        return this;
    }


   public ItemBuilder addFlags(final ItemFlag... flags) {
     if (meta == null) meta = item.getItemMeta();
     meta.addItemFlags(flags);
     return this;
   }


    public void setTrim(final TrimMaterial mat, final TrimPattern pat) {
      if (meta == null) meta = item.getItemMeta();
      if (meta instanceof ArmorMeta) {
          ((ArmorMeta) meta).setTrim(new ArmorTrim(mat, pat));
      }
    }
   
    public ItemBuilder addEnchant(final Enchantment enchantment) {
       return addEnchant(enchantment, 1);
    }
    
    public ItemBuilder addEnchant(final Enchantment enchantment, final int level) {
      if (enchants==null) enchants = new HashMap<>();
      enchants.put(enchantment, level);
      return this;
    }

    @Deprecated
    public ItemBuilder unsafeEnchantment(final Enchantment enchantment, final int level) {
       return addEnchant(enchantment, level);
    }
   
    @Deprecated
    public ItemBuilder clearEnchantment() {
      return clearEnchants();
    }

    public ItemBuilder clearEnchants() {
      if (meta != null) meta.removeEnchantments();
      if (enchants!=null) enchants.clear();
      return this;
    }
    
    
    
    
    

    public ItemBuilder setUnbreakable(final boolean unbreakable) {
      if (meta == null) meta = item.getItemMeta();
      meta.setUnbreakable(unbreakable);
      return this;
    }
    
    public ItemBuilder setItemFlag(final ItemFlag flag) {
      if (meta == null) meta = item.getItemMeta();
      meta.addItemFlags(flag);
      return this;
    }

    public ItemBuilder setAttribute(final Attribute attribute, final double amount, final Operation op) {
      setAttribute(attribute, amount, op, item.getType().getEquipmentSlot().getGroup());
      return this;
    }

    public ItemBuilder setAttribute(final Attribute attribute, final double amount, final Operation op, @Nullable final EquipmentSlotGroup slot) {
      if (meta == null) meta = item.getItemMeta();
      meta.addAttributeModifier(attribute, new AttributeModifier(attribute.getKey(), amount, op, slot));
      return this;
    }

    @Deprecated
    public ItemBuilder setAttribute(final Attribute attribute, final double amount, final Operation op, @Nullable final EquipmentSlot slot) {
      if (meta == null) meta = item.getItemMeta();
      meta.addAttributeModifier(attribute, new AttributeModifier(attribute.getKey(), amount, op, slot.getGroup()));
      return this;
    }
    
    public ItemBuilder removeAttribute(final Attribute attribute) {
      if (meta == null) meta = item.getItemMeta();
      meta.removeAttributeModifier(attribute);
      return this;
    }
    
    public ItemBuilder removeSlotAttribute() {
      if (meta == null) meta = item.getItemMeta();
      meta.removeAttributeModifier(item.getType().getEquipmentSlot());
      return this;
    }
    
    public ItemBuilder setModelData(final int data) {
      if (meta == null) meta = item.getItemMeta();
      meta.setCustomModelData(data);
      return this;
    }
    
    public ItemBuilder setDurability(final int dur) {
    	final int mdr = item.getType().getMaxDurability();
      if (meta == null) meta = item.getItemMeta();
    	if (meta instanceof Damageable) ((Damageable) meta).setDamage(dur < mdr ? mdr - dur : 0);
      return this;
    }

    public <M extends ItemMeta> ItemBuilder applyCustomMeta(final Class<M> metaType, final Consumer<M> metaApplier) {
      if (meta == null) meta = item.getItemMeta();
      if (metaType.isInstance(meta)) {
        metaApplier.accept(metaType.cast(meta));
      }
      return this;
    }
   

   
   
   
   
   
   
   
   
   
   
   
    public ItemBuilder setSkullOwner(final OfflinePlayer player) {
        skullOwnerUuid = player.getUniqueId().toString();
        return this;
    }
    public ItemBuilder setSkullOwnerUuid(final String uuidAsString) {
        skullOwnerUuid = uuidAsString;
        return this;
    }

    /**
     * @param texture <a href="https://minecraft-heads.com/custom-heads/">...</a>
     * @return
     */
    public ItemBuilder setCustomHeadTexture(final String texture) {
        //if (texture.length()<70) return setCustomHeadUrl(texture); //фикс!!
        this.skullTexture = texture;
        return this;
    }
    
    public ItemBuilder setCustomHeadTexture(final Texture texture) {
        return setCustomHeadTexture(texture.value);
    }
    
   // public ItemBuilder setCustomHeadUrl(final String url) {
   //     if (!url.startsWith("http://")) skullTexture = "http://textures.minecraft.net/texture/" + url;
  //      else skullTexture = url;
  //      return this;
  //  }
    
    

    public ItemBuilder setColor(final Color color) {
        this.color=color;
        return this;
    }
   
   

   
   
   
   
   
   
   
   

    public ItemBuilder setBasePotionType(final PotionType type) {
        this.basePotionType = type;
        return this;
    }

    public ItemBuilder addCustomPotionEffect(final PotionEffect customPotionEffect) {
        if (customPotionEffect!=null && (customPotionEffects==null)) {
          customPotionEffects = new ArrayList<>();
        }
        customPotionEffects.add(customPotionEffect);
        return this;
    }

    
    
    
    
    
    

    
    
    
    
    
    
    
    
    
    
    public ItemStack build() {

        if (!lore.isEmpty()) {
          if (meta == null) meta = item.getItemMeta();
          meta.lore(lore);
        }
        
        switch (getType()) {
            
            case POTION, TIPPED_ARROW, LINGERING_POTION, SPLASH_POTION:
              if (basePotionType!=null || customPotionEffects!=null) {
                if (meta == null) meta = item.getItemMeta();
                final PotionMeta potionMeta = (PotionMeta) meta;
                if (basePotionType!=null) potionMeta.setBasePotionType(basePotionType);
                if (customPotionEffects!=null && !customPotionEffects.isEmpty()) {
                    for (final PotionEffect ef : customPotionEffects) {
                        potionMeta.addCustomEffect(ef,true);
                    }
                }
                if (color!=null) {
                    potionMeta.setColor(color);
                }
              }
              break;
            
            case PLAYER_HEAD:
              if (meta == null) meta = item.getItemMeta();
              final SkullMeta skullMeta = (SkullMeta) meta;
              
              if (skullOwnerUuid!=null && !skullOwnerUuid.isEmpty()) {
                final UUID uuid = UUID.fromString(skullOwnerUuid);
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                skullMeta.setOwningPlayer(offlinePlayer);
              }

              if (skullTexture!=null && !skullTexture.isEmpty()) {
                ItemUtils.setHeadTexture(skullMeta, skullTexture);
              }
              break;
            
            case LEATHER_BOOTS, LEATHER_CHESTPLATE, LEATHER_HELMET, 
            LEATHER_LEGGINGS, LEATHER_HORSE_ARMOR:
                if (color!=null) {
                  if (meta == null) meta = item.getItemMeta();
                  final LeatherArmorMeta leatherMeta = (LeatherArmorMeta) meta;
                  leatherMeta.setColor(color);
                }
                break;
            
            case ENCHANTED_BOOK://для книг чары в storage
                if (enchants!=null && !enchants.isEmpty()) {
                  if (meta == null) meta = item.getItemMeta();
                  final EnchantmentStorageMeta enchantedBookMeta = (EnchantmentStorageMeta) meta;
                  for (final Map.Entry<Enchantment, Integer> en : enchants.entrySet()) {//ignoreLevelRestriction
                    enchantedBookMeta.addStoredEnchant(en.getKey(), en.getValue(), false);
                  }
                }
                enchants = null;
                break;

            default: break; //для обычных предметов просто кидаем чары - а для дригих не кидаем????? не заслужили тип????
        }

        if (enchants!=null && !enchants.isEmpty()) {
          if (meta == null) meta = item.getItemMeta();
          for (final Map.Entry<Enchantment, Integer> en : enchants.entrySet()) {
            meta.addEnchant(en.getKey(), en.getValue(), true);
          }
        }

      if (meta != null) item.setItemMeta(meta);
      return item;
    }
}
