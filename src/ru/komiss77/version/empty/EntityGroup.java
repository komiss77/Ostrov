package ru.komiss77.version.empty;


import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import ru.komiss77.version.IEntityGroup;


public class EntityGroup implements IEntityGroup { 

    
    //net.minecraft.server.v1_16_R1.EntityTypes - есть все типы   EntityTypes.ZOMBIFIED_PIGLIN.e();
    @Override
    public EntityGroup getEntytyGroup(final EntityType type) {
    	return getEntityGroup(type);
    }
    
    @Override
    public EntityGroup getEntityGroup(final EntityType type) {
        
        
        
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

    @Override
    public EntityGroup getEntytyType(final Entity entity) {
    	return getEntityType(entity);
    }
    
    @Override
    public EntityGroup getEntityType(final Entity entity) {    

        if (entity == null || entity.isDead()) return EntityGroup.UNDEFINED;
        return getEntityGroup(entity.getType());
    }

    @Override
    public EntityGroup byTag(final String tag_as_string){
        for(EntityGroup set: EntityGroup.values()){
                if(String.valueOf(set.toString()).equals(tag_as_string.toUpperCase())){
                        return set;
                }
        }
        return EntityGroup.UNDEFINED;
    }

    @Override
    public boolean isGroup(final String tag_as_string){
        for(EntityGroup set: EntityGroup.values()){
                if(String.valueOf(set.toString()).equals(tag_as_string.toUpperCase())){
                        return true;
                }
        }
        return false;
    }


    
    @Override
    public void sendLookAtPlayerPacket(final Player p, final Entity e) {
        return;
    }

    @Override
    public void sendLookResetPacket(final Player p, final Entity e) {
        return;
    }

    @Override
	public void colorGlow(final Entity le, final char cr, final boolean fakeGlow) {
    	return;
    }

}
