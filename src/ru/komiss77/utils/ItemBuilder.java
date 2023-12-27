package ru.komiss77.utils;

import java.util.*;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import net.kyori.adventure.text.Component;
import ru.komiss77.Ostrov;
import ru.komiss77.utils.ItemUtils.Texture;

        //im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        //Validate.isTrue(item.getType() == Material.PLAYER_HEAD, "skullOwner() only applicable for skulls!", new Object[0]);


public class ItemBuilder {
    
    
    private final ItemStack item;
    private ItemMeta meta;
    private Color color;
    private List<Component> lore;
    private String skullOwnerUuid;
    private String skullTexture;
    private PotionData basePotionData;
    private List<PotionEffect> customPotionEffects=null;
    private Map<Enchantment, Integer> enchants=null;

   
    public ItemBuilder(final Material material) {
       item = new ItemStack(material);
       meta = item.getItemMeta();
       lore = new ArrayList<>();
    }
   

    public ItemBuilder(final ItemStack from) {
        item = from==null ? new ItemStack(Material.AIR) : new ItemStack(from.getType(), from.getAmount());
        meta = from.hasItemMeta() ? from.getItemMeta() : item.getItemMeta();
        lore = meta != null && meta.hasLore() ? meta.lore() : new ArrayList<>();
        
    }
   
    
    public ItemBuilder persistentData(final String key, final String data) {
        meta.getPersistentDataContainer().set(new NamespacedKey(Ostrov.instance, key), PersistentDataType.STRING, data);
        return this;
    }   
    public ItemBuilder persistentData(final String key, final int data) {
        meta.getPersistentDataContainer().set(new NamespacedKey(Ostrov.instance, key), PersistentDataType.INTEGER, data);
        return this;
    }
    public ItemBuilder persistentData(final String data) {
        meta.getPersistentDataContainer().set(ItemUtils.key, PersistentDataType.STRING, data);
        return this;
    }   
    public ItemBuilder persistentData(final int data) {
        meta.getPersistentDataContainer().set(ItemUtils.key, PersistentDataType.INTEGER, data);
        return this;
    }
    
    
    
    
    public ItemBuilder setType(final Material material) {
        if (material==null) return this;
        item.setType(material);
        final ItemMeta oldMeta = meta.clone();
        meta = item.getItemMeta();
        if (oldMeta.hasDisplayName()) meta.displayName(oldMeta.displayName());
        if (oldMeta.hasLore()) lore = oldMeta.lore();// meta.lore(oldMeta.lore());
        if (oldMeta.hasCustomModelData()) meta.setCustomModelData(oldMeta.getCustomModelData());
        if (oldMeta.hasEnchants()) {
            oldMeta.getEnchants().keySet().stream().forEach( enc -> meta.addEnchant(enc, oldMeta.getEnchantLevel(enc), true) );
        }
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
    	meta.displayName(TCUtils.format(name));
    	return this;
    }
    
    public ItemBuilder name(@Nullable final Component name) {
    	meta.displayName(name);
    	return this;
    }
    
    
    public ItemBuilder addLore(Object o) {
        if (o==null) return this;
    	if (lore == null) lore = new ArrayList<>();
        if (o instanceof String s) {
            if (s.isEmpty()) lore.add(Component.text(""));
            else lore.add(TCUtils.format(s));
        } else if (o instanceof Component c) {
            lore.add(c);
        } else if (o instanceof Collection<?> c) {
            for (Object x : c) {
                addLore(x);
            }
        } else if (o instanceof String[] ss) {
            for (final String s : ss) {
                lore.add(TCUtils.format(s));
            }
        } else if (o instanceof Component[] cc) {
            Collections.addAll(lore, cc);
        }
        //if (s.isEmpty()) lore.add(Component.text(""));
        //else lore.add(TCUtils.format(s));
    	return this;
    }
    
    public ItemBuilder addLore(final Object... lores) {
        if (lores == null) return this;
    	for (final Object o : lores) {
            addLore(o);
        }
    	return this;
    }

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

    
    @Deprecated
    public ItemBuilder lore(final Object o) {
    	return setLore(o);
    }
    
    @Deprecated
    public ItemBuilder lore(final Object... oo) {
    	return setLore(oo);
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
      meta.addItemFlags(flags);
      return this;
   }


    public void setTrim(final TrimMaterial mat, final TrimPattern pat) {
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
    
    public ItemBuilder unsafeEnchantment(final Enchantment enchantment, final int level) {
       item.addUnsafeEnchantment(enchantment, level);
       return this;
    }
   
    public ItemBuilder clearEnchantment() {
        final Iterator<Enchantment> iterator = this.item.getEnchantments().keySet().iterator();
        while (iterator.hasNext()) {
            item.removeEnchantment(iterator.next());
        }
        return this;
    }
    
    
    
    
    

    public ItemBuilder setUnbreakable(final boolean unbreakable) {
       meta.setUnbreakable(true);
       return this;
    }
    
    public ItemBuilder setItemFlag(final ItemFlag flag) {
        meta.addItemFlags(new ItemFlag[] { flag });
        return this;
    }
    
    public ItemBuilder setAttribute(final Attribute attribute, final double amount, final Operation op) {
    	setAttribute(attribute, amount, op, item.getType().getEquipmentSlot());
        return this;
    }
    
    public ItemBuilder setAttribute(final Attribute attribute, final double amount, final Operation op, @Nullable final EquipmentSlot slot) {
        meta.addAttributeModifier(attribute, new AttributeModifier(UUID.randomUUID(), "atbuilder", amount, op, slot));
        return this;
    }
    
    public ItemBuilder removeAttribute(final Attribute attribute) {
        meta.removeAttributeModifier(attribute);
        return this;
    }
    
    public ItemBuilder removeSlotAttribute() {
        meta.removeAttributeModifier(item.getType().getEquipmentSlot());
        return this;
    }
    
    public ItemBuilder setModelData(final int data) {
        meta.setCustomModelData(data);
        return this;
    }
    
    public ItemBuilder setDurability(final int dur) {
    	final int mdr = item.getType().getMaxDurability();
    	if (meta instanceof Damageable) ((Damageable) meta).setDamage(dur < mdr ? mdr - dur : 0);
        return this;
    }

   public <M extends ItemMeta> ItemBuilder applyCustomMeta(final Class<M> metaType, final Consumer<M> metaApplier) {
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
        return setCustomHeadTexture(texture.texture);
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
   
   

   
   
   
   
   
   
   
   

    public ItemBuilder setBasePotionData(final PotionData basePotionData) {
        this.basePotionData = basePotionData;
        return this;
    }

    public ItemBuilder addCustomPotionEffect(final PotionEffect customPotionEffect) {
        if (customPotionEffect!=null) {
            if (customPotionEffects==null) customPotionEffects = new ArrayList<>();
        }
        customPotionEffects.add(customPotionEffect);
        return this;
    }

    
    
    
    
    
    

    
    
    
    
    
    
    
    
    
    
    public ItemStack build() {

        if (meta != null) meta.lore(lore.isEmpty() ? null : lore);
        item.setItemMeta(meta);
        
        switch (getType()) {
            
            case POTION, TIPPED_ARROW, LINGERING_POTION, SPLASH_POTION:
                if (basePotionData!=null || customPotionEffects!=null) {
                    final PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
                    if (basePotionData!=null) potionMeta.setBasePotionData(basePotionData);
                    if (customPotionEffects!=null && !customPotionEffects.isEmpty()) {
                        for (PotionEffect customPotionEffect : customPotionEffects) {
                            potionMeta.addCustomEffect(customPotionEffect,true);
                        }
                    }
                    if (color!=null) {
                        potionMeta.setColor(color);
                    }
                    item.setItemMeta(potionMeta);
                }
                break;
            
            case PLAYER_HEAD:
                final SkullMeta skullMeta = (SkullMeta)item.getItemMeta();
                
                if (skullOwnerUuid!=null && !skullOwnerUuid.isEmpty()) {
                    final UUID uuid = UUID.fromString(skullOwnerUuid);
                    final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                    skullMeta.setOwningPlayer(offlinePlayer);
                    item.setItemMeta(skullMeta);
                }

                if (skullTexture!=null && !skullTexture.isEmpty()) {
                    if (skullTexture.length()>72) { //определяяем зашифрованную ссылку
                        final String decoded = new String(Base64.getDecoder().decode(skullTexture));
                        skullTexture = decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length());
                    }
                    com.destroystokyo.paper.profile.PlayerProfile profile = ItemUtils.getProfile(skullTexture);
                    skullMeta.setPlayerProfile(profile);
                    item.setItemMeta(skullMeta);
                }
                break;
            
            case LEATHER_BOOTS, LEATHER_CHESTPLATE, LEATHER_HELMET, 
            LEATHER_LEGGINGS, LEATHER_HORSE_ARMOR:
                if (color!=null) {
                    final LeatherArmorMeta leatherMeta = (LeatherArmorMeta) item.getItemMeta();
                    leatherMeta.setColor(color);
                    item.setItemMeta(leatherMeta);
                }
                break;
            
            case ENCHANTED_BOOK://для книг  чары в storage
                if (enchants!=null && !enchants.isEmpty()) {
                    final EnchantmentStorageMeta enchantedBookMeta = (EnchantmentStorageMeta) item.getItemMeta();
                    for (Enchantment enchant : enchants.keySet()) {    //ignoreLevelRestriction
                        enchantedBookMeta.addStoredEnchant(enchant, enchants.get(enchant), false);
                    }
                    item.setItemMeta(enchantedBookMeta);
                }
                return item;
            
            default: break; //для обычных предметов просто кидаем чары - а для дригих не кидаем????? не заслужили тип????
        }

        if (meta != null && enchants!=null && !enchants.isEmpty()) {
            for (Enchantment enchant : enchants.keySet()) {
                try { //item.addEnchantment(enchant, enchants.get(enchant));
                    meta.addEnchant(enchant, enchants.get(enchant), true);
                } catch (IllegalArgumentException ex) {
                    Ostrov.log_err("ItemBuilder: невозможно добавить чары "+enchant.getKey().getKey()+" к предмету "+item.getType().toString()+" : "+ex.getMessage());
                }
            }
            item.setItemMeta(meta);
        }

        return item;
    }   
}
