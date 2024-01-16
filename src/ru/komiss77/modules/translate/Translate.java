package ru.komiss77.modules.translate;

import com.destroystokyo.paper.ClientOption;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import ru.komiss77.ApiOstrov;
import ru.komiss77.utils.ItemUtils;
import ru.komiss77.utils.TCUtils;

import java.util.Map;


@Deprecated
public class Translate {

    @Deprecated
    public static String getItemDisplayName(final ItemStack item, final EnumLang lang) {
    	final String nm = ItemUtils.getName(item);
        return nm.isEmpty() ? getItemName(item, lang) : nm;
    }

    @Deprecated
    public static String getItemDisplayName(final ItemStack item, final Player p) {
        //return getItemDisplayName(item, TranslateHelper.getPlayerLanguage(player));
        final boolean eng = !p.getClientOption(ClientOption.LOCALE).equals("ru_ru");
        return getItemDisplayName(item, eng ? EnumLang.EN_US : EnumLang.RU_RU);
    }

    @Deprecated
    public static String getItemName(final ItemStack item, final EnumLang lang) {
        // Potion & SpawnEgg & Player Skull
    	if (item == null) return "{}";
        return switch (item.getType()) {
            case POTION, SPLASH_POTION, LINGERING_POTION, TIPPED_ARROW ->
                translateToLocal(ApiOstrov.nrmlzStr(item.getType().toString()), getItemUnlocalizedName(item.getType()) + ".effect." +
                    ((PotionMeta) item.getItemMeta()).getBasePotionData().getType().toString().toLowerCase(), lang);
            case PLAYER_HEAD, PLAYER_WALL_HEAD -> getPlayerSkullName(item, lang);
            default -> getMaterialName(item.getType(), lang);
        };
    }
    
    @Deprecated
    public static String getMaterialName(final Material mat, final EnumLang lang) {
        return TCUtils.toString(Lang.t(mat, lang==EnumLang.RU_RU ? Lang.RU : Lang.EN));//return translateToLocal(mat.toString(), getItemUnlocalizedName(mat), lang);

    }
    
    @Deprecated
    private static String getPlayerSkullName(final ItemStack skull, final EnumLang lang) {
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta!=null && meta.hasOwner()) {
            return String.format(Translate.translateToLocal(skull.getType().toString(), "block.minecraft.player_head.named", lang), meta.getOwningPlayer().getName());
        } else {
            return Translate.translateToLocal(skull.getType().toString(), "block.minecraft.player_head", lang);
        }
    }    
    
    @Deprecated
    public static String getItemName(final ItemStack item, final Player player) {
        return getItemName(item, TranslateHelper.getPlayerLanguage(player));
    }

    @Deprecated
    public static String getBiomeName(final Biome biome, final EnumLang lang) {
        return TCUtils.toString(Lang.t(biome, lang==EnumLang.RU_RU ? Lang.RU : Lang.EN));//translateToLocal(biome.toString(), getBiomeUnlocalizedName(biome), lang);
    }

    @Deprecated
    public static String getBiomeName(final Biome biome, final Player player) {
        return getBiomeName(biome, TranslateHelper.getPlayerLanguage(player));
    }

    @Deprecated
    public static String getEntityDisplayName(final Entity entity, final Player player) {
        return getEntityDisplayName(entity, TranslateHelper.getPlayerLanguage(player));
    }

    @Deprecated
    public static String getEntityDisplayName(final Entity entity, final EnumLang lang) {
        return ( entity.customName() != null && entity.isCustomNameVisible() ) ? TCUtils.toString(entity.customName()) : getEntityName(entity, lang);
    }

    @Deprecated
    public static String getEntityName(final Entity entity, final Player player) {
        return getEntityName(entity, TranslateHelper.getPlayerLanguage(player));
    }

    @Deprecated
    public static String getEntityName(final Entity entity, final EnumLang lang) {
        //return translateToLocal(entity.getType().toString(), getEntityUnlocalizedName(entity), locale);
        //if (entity.getType()==EntityType.DROPPED_ITEM) {
            //
        //}
        return getEntityName(entity.getType(), lang);
    }

    @Deprecated
    public static String getEntityName(final EntityType entityType, final EnumLang lang) {
        
        return TCUtils.toString(Lang.t(entityType, lang==EnumLang.RU_RU ? Lang.RU : Lang.EN));//return translateToLocal(entityType.toString(), getEntityUnlocalizedName(entityType), lang);
    }

    @Deprecated
    public static String getEntityName(final EntityType entityType, final Player player) {
        return TCUtils.toString(Lang.t(player, entityType));//return getEntityName(entityType, TranslateHelper.getPlayerLanguage(player));
    }

    @Deprecated
    public static String getEnchantmentLevelName(final int level, final Player player) {
        return translateToLocal(getEnchantmentLevelUnlocalizedName(level), TranslateHelper.getPlayerLanguage(player));
    }

    @Deprecated
    public static String getEnchantmentLevelName(final int level, final EnumLang lang) {
        return translateToLocal(getEnchantmentLevelUnlocalizedName(level), lang);
    }

    @Deprecated
    public static String getEnchantmentName(final Enchantment enchantment, final Player player) {
        return getEnchantmentName(enchantment, TranslateHelper.getPlayerLanguage(player));
    }

    @Deprecated
    public static String getEnchantmentName(final Enchantment enchantment, final EnumLang lang) {
        return TCUtils.toString(Lang.t(enchantment, lang==EnumLang.RU_RU ? Lang.RU : Lang.EN));//return translateToLocal(getEnchantmentUnlocalizedName(enchantment), lang);
    }

    @Deprecated
    public static String getEnchantmentDisplayName(final Enchantment enchantment, final int level, final Player player) {
        return getEnchantmentDisplayName(enchantment, level, TranslateHelper.getPlayerLanguage(player));
    }

    @Deprecated
    public static String getEnchantmentDisplayName(final Enchantment enchantment, final int level, final EnumLang lang) {
        String name = getEnchantmentName(enchantment, lang);
        String enchLevel = getEnchantmentLevelName(level, lang);
        return name + (enchLevel.length() > 0 ? " " + enchLevel : "");
    }

    @Deprecated
    public static String getEnchantmentDisplayName(final Map.Entry<Enchantment, Integer> entry, final EnumLang lang) {
        return getEnchantmentDisplayName(entry.getKey(), entry.getValue(), lang);
    }

    @Deprecated
    public static String getEnchantmentDisplayName(final Map.Entry<Enchantment, Integer> entry, final Player player) {
        return getEnchantmentDisplayName(entry.getKey(), entry.getValue(), player);
    }

    @Deprecated
    public static String getItemUnlocalizedName(final Material mat) {
        //EnumItem enumItem = EnumItem.get(item.getType());
        //return enumItem != null ? enumItem.getUnlocalizedName() : item.getType().toString();
        return (mat.isBlock()?"block":"item") + "."
                + mat.getKey().getNamespace() + "." 
                + mat.getKey().getKey();
    }

    @Deprecated
    public static String getBiomeUnlocalizedName(final Biome biome) {
        //EnumBiome enumBiome = EnumBiome.get(biome);
        //return enumBiome != null ? enumBiome.getUnlocalizedName() : biome.toString();
        return "biome.minecraft."+biome.toString().toLowerCase();
    }

    @Deprecated
    public static String getEntityUnlocalizedName(final Entity entity) {
        //EnumEntity enumEntity = EnumEntity.get(entity.getType());
        //return enumEntity != null ? enumEntity.getUnlocalizedName() : entity.getType().toString();
        return getEntityUnlocalizedName(entity.getType());
    }

    @Deprecated
    public static String getEntityUnlocalizedName(final EntityType entityType) {
        //EnumEntity enumEntity = EnumEntity.get(entityType);
        //return enumEntity != null ? enumEntity.getUnlocalizedName() : entityType.toString();
        if (entityType==EntityType.SNOWMAN) {
            return "entity.minecraft.snow_golem";
        }
        return "entity.minecraft."+entityType.toString().toLowerCase();
    }

    @Deprecated
    public static String getEnchantmentLevelUnlocalizedName(final int level) {
        //EnumEnchantmentLevel enumEnchLevel = EnumEnchantmentLevel.get(level);
        //return enumEnchLevel != null ? enumEnchLevel.getUnlocalizedName() : Integer.toString(level);
        return "enchantment.level."+level;
    }

    @Deprecated
    public static String getEnchantmentUnlocalizedName(final Enchantment enchantment) {
        //EnumEnchantment enumEnch = EnumEnchantment.get(enchantment);
        //return (enumEnch != null ? enumEnch.getUnlocalizedName() : enchantment.getKey().getKey());
        return "enchantment.minecraft."+enchantment.getKey().getKey();
    }

    @Deprecated
    public static String translateToLocal(final String unlocalizedName, final EnumLang lang) {
        return translateToLocal(unlocalizedName, unlocalizedName, lang);
    }
    
    @Deprecated
    public static String translateToLocal(final String sourceName, final String unlocalizedName, final EnumLang lang) {
//System.out.println("translateToLocal() unlocalizedName="+unlocalizedName);
//final Map<String, String> langMap = EnumLang.get(locale.toLowerCase()).getMap();
return  sourceName;
      /*  String result = lang.getMap().get(unlocalizedName);
//System.out.println("translateToLocal() 1 result="+result+"    contains?"+langMap.containsKey(unlocalizedName));
        if (result != null && !result.isEmpty())
            return result;
        else {
            //result = EnumLang.get(LangUtils.plugin.config.getString("FallbackLanguage")).getMap().get(unlocalizedName);
            result = EnumLang.get("en_us").getMap().get(unlocalizedName);
            if (result == null || result.isEmpty())// when fallback language doesn't exist
                result = EnumLang.EN_US.getMap().get(unlocalizedName);
        }
        return result == null ? sourceName : result;*/
    }
}
