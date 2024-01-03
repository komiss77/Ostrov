
package ru.komiss77.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;


public class EntityUtil {

    public static EntityGroup group(final Entity e) {
        return group(e.getType());
    }
    
    public static EntityGroup group(final EntityType type) {
       // type.getKey();
        //EnumCreatureType enumcreaturetype = (net.minecraft.world.Entiti)entity.(net.minecraft.world.EntityTypes)ae().(net.minecraft.world.EnumCreatureType)f();
      //  EntityTypes et = EntityTypes.a("").get();
        switch (type) {

            case RAVAGER:																											
            case PILLAGER:																											
            case ZOGLIN:																											
            case PIGLIN:																											
            case DROWNED:																											
            case SHULKER:																											
            case ENDERMITE:																											
            case WITCH:																											
            case ENDER_DRAGON:																											
            case MAGMA_CUBE:																											
            case BLAZE:																											
            case SILVERFISH:																											
            case ENDERMAN:																											
            case ZOMBIFIED_PIGLIN:																											
            case GIANT:			
            case CREEPER:
            case SPIDER:																											
            case GHAST:																											
            case SLIME:																											
            case PHANTOM:
            case ZOMBIE:
            case SKELETON:
            case CAVE_SPIDER:																											
            case GUARDIAN:
            case ZOMBIE_VILLAGER:
            case VEX:
            case VINDICATOR:
            case EVOKER:
            case ILLUSIONER:
            case WITHER:
            case WITHER_SKELETON:
            case STRAY:                    
            case HUSK:
            case PIGLIN_BRUTE:
            case WARDEN:
            case ELDER_GUARDIAN:
                return EntityGroup.MONSTER;																										


            case PARROT:
            case LLAMA_SPIT:
            case LLAMA:
            case RABBIT:
            case CAT:
            case HORSE:
            case OCELOT:
            case FOX:
            case MUSHROOM_COW:
            case WOLF:
            case COW:
            case SHEEP:
            case POLAR_BEAR:
            case PIG:
            case PANDA:
            case BEE:
            case CHICKEN:
            case VILLAGER:
            case WANDERING_TRADER:
            case IRON_GOLEM:
            case SNOWMAN:
            case DONKEY:
            case MULE:
            case SKELETON_HORSE:
            case ZOMBIE_HORSE:
            case TURTLE:
            case HOGLIN:
            case GOAT:
    		case CAMEL:
    		case SNIFFER:
            case TRADER_LLAMA:
            case ALLAY:
            case STRIDER:
                return EntityGroup.CREATURE;
                
                
            case BAT:
                return EntityGroup.AMBIENT;
			
                                
            case DOLPHIN:
            case SQUID:
            case GLOW_SQUID:
            case AXOLOTL:
            case FROG:
            case TADPOLE:
                return EntityGroup.WATER_CREATURE;
			
                
            case TROPICAL_FISH:
            case COD:
            case SALMON:
            case PUFFERFISH:
                return EntityGroup.WATER_AMBIENT;
                
                
                
                
                
            case AREA_EFFECT_CLOUD:
            case ARMOR_STAND:
            case ARROW:
            case BOAT:
            case DRAGON_FIREBALL:
            case DROPPED_ITEM:
            case EGG:
            case ENDER_CRYSTAL:
            case ENDER_PEARL:
            case ENDER_SIGNAL:
            case EVOKER_FANGS:
            case EXPERIENCE_ORB:
            case FALLING_BLOCK:
            case FIREBALL:
            case FIREWORK:
            case FISHING_HOOK:
            case ITEM_FRAME:
            case LEASH_HITCH:
            case LIGHTNING:
            case MINECART:
            case MINECART_CHEST:
            case MINECART_COMMAND:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
            case PAINTING:
            case PRIMED_TNT:
            case SHULKER_BULLET:
            case SMALL_FIREBALL:
            case SNOWBALL:
            case SPECTRAL_ARROW:
            case SPLASH_POTION:
            case THROWN_EXP_BOTTLE:
            case TRIDENT:
            case UNKNOWN:
            case WITHER_SKULL:
            case PLAYER:
            case GLOW_ITEM_FRAME:
            case CHEST_BOAT:
                
            case MARKER:
    		case INTERACTION:
    		case ITEM_DISPLAY:
    		case TEXT_DISPLAY:
    		case BLOCK_DISPLAY:
                break;
				
                
        }
        
        //если выше ничего не выстрелило, то определяем о старинке
        return EntityGroup.UNDEFINED;
    }


    
    
    public enum EntityGroup {
    	/**Монстры, могут агрится на игрока*/
        MONSTER ("§4Монстры"), //не переименовывать! или придётся переделывать конфиги лимитера!!
    	/**Животные, могут быть скрещеными*/
        CREATURE ("§2Сухопутные животные"),
    	/**Обитатели, улучшают атмосферу*/
        AMBIENT ("§5Сухопутные обитатели"),
    	/**Спруты и делифины, декор*/
        WATER_CREATURE ("§bВодные животные"),
    	/**Рибки с которых падает рыба*/
        WATER_AMBIENT ("§1Водные обитатели"),
    	/**Прочие сущности, не мобы*/
        UNDEFINED ("§6Прочие")
        ;

        public static EntityGroup matchGroup(String groupName) {
            for(EntityGroup g: EntityGroup.values()){
                if(g.name().equalsIgnoreCase(groupName)){
                    return g;
                }
            }
            return EntityGroup.UNDEFINED;
        }
        
        public String displayName;
        
        private EntityGroup (final String displayName) {
            this.displayName = displayName;
        }

    }
    

}
