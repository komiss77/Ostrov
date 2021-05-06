package ru.komiss77.utils;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import ru.komiss77.Ostrov;


        //im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        //Validate.isTrue(item.getType() == Material.PLAYER_HEAD, "skullOwner() only applicable for skulls!", new Object[0]);



public class ItemBuilder {
    
    
   private final ItemStack item;
   private ItemMeta meta;
   private Color color=null;;
   private String skullOwner = null;
   private String skullOwnerUuid = null;
   private String skullTexture = null;
   private PotionData basePotionData=null;
   private List<PotionEffect> customPotionEffects=null;
   private Map<Enchantment, Integer> enchants=null;
   
   
   public ItemBuilder(final Material material) {
      item = new ItemStack(material);
      meta = item.getItemMeta();
   }
   
   @Deprecated
   public ItemBuilder(final Material material, final short durability) {
      item = new ItemStack(material, 1, durability);
      meta = item.getItemMeta();
   }

   @Deprecated
   public ItemBuilder(final Material material, final byte data) {
      item = new ItemStack(material, 1, data);
      meta = item.getItemMeta();
   }

   public ItemBuilder(final ItemStack as_this) {
      item = as_this.clone();
      meta = item.getItemMeta().clone();
   }
   

    public ItemBuilder setType(final Material material) {
        item.setType(material);
        final ItemMeta oldMeta = meta.clone();
        meta = item.getItemMeta();
        if (oldMeta.hasDisplayName()) meta.setDisplayName(oldMeta.getDisplayName());
        if (oldMeta.hasLore()) addLore(oldMeta.getLore());
        if (oldMeta.hasCustomModelData()) meta.setCustomModelData(oldMeta.getCustomModelData());
        if (oldMeta.hasEnchants()) {
            oldMeta.getEnchants().keySet().stream().forEach( (enc) -> {meta.addEnchant(enc, oldMeta.getEnchantLevel(enc), true);} );
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

   
   
   
   
   
    
   public ItemBuilder setName(final String name) {
      meta.setDisplayName(name);
      return this;
   }
   public ItemBuilder name(final String name) {
      return setName(name);
   }

   
   
   
   
   
   
   
   
   
    public ItemBuilder setLore(final List<String> lore) {
       meta.setLore(lore);
       return this;
    }
    public ItemBuilder setLore(final String lore_as_string, final String base_color) {
       meta.setLore(ItemUtils.Gen_lore(meta.getLore(), lore_as_string, base_color));
       return this;
    }
    public ItemBuilder addLore(String lore_as_string) {
        List<String> lores = meta.getLore();
        if (lores==null) {
            lores = new ArrayList<>();
        }
        if (lore_as_string==null) lore_as_string = "";
        lores.add(lore_as_string);
        meta.setLore(lores);
        return this;
    }
    public ItemBuilder addLore(final String... lores) {
        for (String lore : lores) {
            addLore(lore);
        }
        return this;
    }
    public ItemBuilder addLore(final Collection<String> lores) {
        if (lores==null || lores.isEmpty()) return this;
        lores.forEach( (lore) -> {
            addLore(lore);
        });
        return this;
    }
    public ItemBuilder lore(final List<String> lore) {
        return setLore(lore);
    }
    public ItemBuilder lore(final String lore_as_string, final String base_color) {
        return addLore(base_color+lore_as_string);
    }
    public ItemBuilder lore(final String lore_as_string) {
        return addLore("§7"+lore_as_string);
    }
    public ItemBuilder clearLore() {
        this.meta.setLore((List)Lists.newArrayList());
        return this;
    }
    public ItemBuilder replaceLore(final String from, final String to) {
        List<String> lores = meta.getLore();
        if (lores==null || lores.isEmpty()) return this;
        for (int i=0; i<lores.size(); i++) {
            if (lores.get(i).equalsIgnoreCase(from)) {
                lores.set(i, to);
            }
        }
        meta.setLore(lores);
        return this;
    }    
   
   

    
    


   public ItemBuilder addFlags(final ItemFlag... flags) {
      meta.addItemFlags(flags);
      return this;
   }

   
   
   
   
    public ItemBuilder enchantment(final Enchantment enchantment) {
       return addEnchantment(enchantment, 1);
    }
    public ItemBuilder addEnchantment(final Enchantment enchantment, final int level) {
        if (enchants==null) enchants = new HashMap<>();
        enchants.put(enchantment, level);
       //meta.addEnchant(enchantment, level, true);
       return this;
    }
    public ItemBuilder unsaveEnchantment(final Enchantment enchantment, final int level) {
       item.addUnsafeEnchantment(enchantment, level);
       return this;
    }
   
    public ItemBuilder clearEnchantment() {
        final Iterator<Enchantment> iterator = this.item.getEnchantments().keySet().iterator();
        while (iterator.hasNext()) {
            item.removeEnchantment((Enchantment)iterator.next());
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
    
    public ItemBuilder setAttribute(final Attribute attribute, final double amount, final EquipmentSlot slot) {
        meta.addAttributeModifier(attribute, new AttributeModifier(UUID.randomUUID(), "itembuilder", amount, AttributeModifier.Operation.ADD_NUMBER, slot));
        return this;
    }
    
    public ItemBuilder removeAttribute(final Attribute attribute) {
        meta.removeAttributeModifier(attribute);
        return this;
    }
    
    public ItemBuilder setModelData(final int data) {
        meta.setCustomModelData(data);
        return this;
    }


   
   

   public ItemBuilder applyCustomMeta(final Class metaType, final Consumer metaApplier) {
      if(!metaType.isInstance(meta)) {
         return this;
      } else {
         ItemMeta specificMeta = (ItemMeta)metaType.cast(meta);
         metaApplier.accept(specificMeta);
         return this;
      }
    }
   

   
   
   
   
   
   
   
   
   
   
   
    public ItemBuilder setSkullOwner(final OfflinePlayer player) {
        skullOwnerUuid = player.getUniqueId().toString();
        return this;
    }
    public ItemBuilder setSkullOwnerUuid(final String uuidAsString) {
        skullOwnerUuid = uuidAsString;
        return this;
    }
    @Deprecated
    public ItemBuilder setSkullOwner(final String name) {
        skullOwner = name;
        return this;
    }
    /**
     * @param texture https://minecraft-heads.com/custom-heads/
     * @return 
     */
    public ItemBuilder setCustomHeadTexture(final String texture) {
        if (texture.length()<70) return setCustomHeadUrl(texture); //фикс!!
        this.skullTexture = texture;
        return this;
    }
    
    public ItemBuilder setCustomHeadUrl(final String url) {
        if (!url.startsWith("http://")) skullTexture = "http://textures.minecraft.net/texture/" + url;
        else skullTexture = url;
        return this;
    }
    
    
    
    @Deprecated
    public ItemBuilder setLeatherColor(final Color color) {
        this.color=color;
        return this;
    }
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

        item.setItemMeta(meta);
        
        if ( getType()==Material.POTION ||getType()==Material.TIPPED_ARROW || getType()==Material.LINGERING_POTION || getType()==Material.SPLASH_POTION ) {
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
            
        } else if (item.getType().toString().startsWith("LEATHER_")) {
            if (color!=null) {
                final LeatherArmorMeta leatherMeta = (LeatherArmorMeta) item.getItemMeta();
                leatherMeta.setColor(color);
                item.setItemMeta(leatherMeta);
            }
            
        } else if (item.getType()==Material.PLAYER_HEAD) {
            
            final SkullMeta skullMeta = (SkullMeta)item.getItemMeta();
            if (skullOwner!=null && !skullOwner.isEmpty()) {
                //final SkullMeta skullMeta = (SkullMeta)item.getItemMeta();
                skullMeta.setOwner(skullOwner);
                item.setItemMeta(skullMeta);
            }
            if (skullOwnerUuid!=null && !skullOwnerUuid.isEmpty()) {
                final UUID uuid = UUID.fromString(skullOwnerUuid);
                if (uuid!=null) {
                    final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                    //final SkullMeta skullMeta = (SkullMeta)item.getItemMeta();
                    skullMeta.setOwningPlayer(offlinePlayer);
                    item.setItemMeta(skullMeta);
                }
            }
            
            if (skullTexture!=null && !skullTexture.isEmpty()) {
                
              /*  final GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
                gameProfile.getProperties().put("textures",new Property("textures", skullTexture));
                try {
                    final Field profileField = skullMeta.getClass().getDeclaredField("profile");
                    if (profileField!=null) {
                        profileField.setAccessible(true);
                        profileField.set(skullMeta, gameProfile);
                    } else {
                        Ostrov.log_warn("Itembuilder skullTexture profileField = null !");
                    }
                } catch (SecurityException | NoSuchFieldException | IllegalAccessException ex) {
                    Ostrov.log_warn("Itembuilder set skullTexture :"+ex.getMessage() );
                }*/
//System.out.println("--- skullTexture="+skullTexture);
                //skullTexture = "http://textures.minecraft.net/texture/" + skullTexture;
                try {
                    final GameProfile gameProfile;
                    if (skullTexture.startsWith("http://")) {
                        gameProfile = ItemUtils.getUrlGameProfile(skullTexture);
                    } else {
                        gameProfile = ItemUtils.getTextureGameProfile(skullTexture);
                    }
                    //final GameProfile gameProfile = ItemUtils.getUrlGameProfile(skullTexture);//new GameProfile(UUID.randomUUID(), null);
                    //final byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", skullTexture).getBytes());
                    //gameProfile.getProperties().put("textures", new Property("textures", new String(encodedData)));

                    final Field profileField = skullMeta.getClass().getDeclaredField("profile");
                    if (profileField!=null) {
                        profileField.setAccessible(true);
                        profileField.set(skullMeta, gameProfile);
                    } else {
                        Ostrov.log_warn("Itembuilder skullTexture profileField = null !");
                    }
                } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                    Ostrov.log_warn("Itembuilder skullTexture error: "+e.getMessage());
                }
                item.setItemMeta(skullMeta);
            }
            
        } else if (item.getType()==Material.ENCHANTED_BOOK) {
            //для книг  чары в storage
            if (enchants!=null && !enchants.isEmpty()) {
                final EnchantmentStorageMeta enchantedBookMeta = (EnchantmentStorageMeta) item.getItemMeta();
                for (Enchantment enchant : enchants.keySet()) {    //ignoreLevelRestriction
                    enchantedBookMeta.addStoredEnchant(enchant, enchants.get(enchant), false);
                }
                item.setItemMeta(enchantedBookMeta);
            }
                
        } else {
            //для обычных предметоа просто кидаем чары
            if (enchants!=null && !enchants.isEmpty()) {
                for (Enchantment enchant : enchants.keySet()) {
                    try {
                        item.addEnchantment(enchant, enchants.get(enchant));
                    } catch (IllegalArgumentException ex) {
                        Ostrov.log_err("ItemBuilder: невозможно добавить чары "+enchant.getKey().getKey()+" к предмету "+item.getType().toString()+" : "+ex.getMessage());
                    }
                }
            }
        }
        
        
//System.out.println("-- getBiomeIcon name 1="+meta.getDisplayName()+" 2="+item.getItemMeta().getDisplayName());        
        return item;
    }




    
}
