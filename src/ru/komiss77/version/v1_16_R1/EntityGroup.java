package ru.komiss77.version.v1_16_R1;


import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import ru.komiss77.version.IEntityGroup;


public class EntityGroup implements IEntityGroup { 

    
    //net.minecraft.server.v1_16_R1.EntityTypes - есть все типы   EntityTypes.ZOMBIFIED_PIGLIN.e();
    @Override
    public EntityGroup getEntytyGroup(final EntityType type) {
        
        
        
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
            case IRON_GOLEM:
            case SNOWMAN:
            case DONKEY:
            case MULE:
            case SKELETON_HORSE:
            case ZOMBIE_HORSE:
            case TURTLE:
            case HOGLIN:
                return EntityGroup.CREATURE;
		
                                
            //case HOGLIN:																											
            //case WANDERING_TRADER:
            case STRIDER:
            case BAT:                
            case ELDER_GUARDIAN:
                return EntityGroup.AMBIENT;
			
                                
            case DOLPHIN:
            case SQUID:
                return EntityGroup.WATER_CREATURE;
			
                
            case TROPICAL_FISH:
            case COD:
            case SALMON:
            case PUFFERFISH:
                return EntityGroup.WATER_AMBIENT;
                
                
                
                
                
            case TRADER_LLAMA:
            case WANDERING_TRADER:
                
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
                return EntityGroup.UNDEFINED;
				
                
        }
        
        //если выше ничего не выстрелило, то определяем о старинке
      /*  if (Monster.class.isAssignableFrom(type.getEntityClass())
                || Slime.class.isAssignableFrom(type.getEntityClass())
                || Ghast.class.isAssignableFrom(type.getEntityClass())
                ) {
            return EntityGroup.MONSTER;
            
        } else if (Animals.class.isAssignableFrom(type.getEntityClass())) {
            return EntityGroup.CREATURE;
            
        } else if (WaterMob.class.isAssignableFrom(type.getEntityClass())) {
            return EntityGroup.WATER_CREATURE;
        }
        */
        return EntityGroup.UNDEFINED;
    }
    
    
    @Override
    public EntityGroup getEntytyType (final Entity entity) {
        //net.minecraft.server.v1_16_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        //EntityTypes nmstype = nmsEntity.getEntityType();
        //EnumCreatureType ect = nmstype.e();
//System.out.println("----getEntytyType isDead?"+entity.isDead()+" nms="+((CraftEntity) entity).getHandle().getEntityType().e() );        

        if (entity == null || entity.isDead()) return EntityGroup.UNDEFINED;
        return getEntytyGroup(entity.getType());
        //switch (((CraftEntity) entity).getHandle().getEntityType().e()) {
       //     case MONSTER : return MONSTER;
        //    case CREATURE : return CREATURE;
       //     case AMBIENT : return AMBIENT;
       //     case WATER_CREATURE : return WATER_CREATURE;
       //     case MISC : return UNDEFINED;
       //         default: return UNDEFINED;
       // }
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


}
