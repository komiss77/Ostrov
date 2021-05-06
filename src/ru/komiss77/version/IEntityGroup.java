package ru.komiss77.version;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;



public interface IEntityGroup {

    public EntityGroup getEntytyType (final Entity entity);
    public EntityGroup getEntytyGroup (final EntityType entityType);
    public EntityGroup byTag(final String tag_as_string);
    public boolean isGroup(final String tag_as_string);    

    
    public enum EntityGroup { 
        MONSTER ("§4Монстры"), //не переименовывать! или придётся переделывать конфиги лимитера!!
        CREATURE ("§2Сухопутные животные"),
        AMBIENT ("§5Сухопутные обитатели"),
        WATER_CREATURE ("§bВодные животные"),
        WATER_AMBIENT ("§1Водные обитатели"),
        UNDEFINED ("§6Прочие")
        ;
        
        public String displayName;
        
        private EntityGroup (final String displayName) {
            this.displayName = displayName;
        }

    }
    
}



/*
public CreatureType getCreatureType(EntityType entityType) {
        if (Monster.class.isAssignableFrom(entityType.getEntityClass())
                || WaterMob.class.isAssignableFrom(entityType.getEntityClass())
                || Slime.class.isAssignableFrom(entityType.getEntityClass())
                || Ghast.class.isAssignableFrom(entityType.getEntityClass())
                ) {
            return CreatureType.MONSTER;
        } else if (Animals.class.isAssignableFrom(entityType.getEntityClass())) {
            return CreatureType.ANIMAL;
        } else if (Villager.class.isAssignableFrom(entityType.getEntityClass())) {
            return CreatureType.VILLAGER;
        } else if (Golem.class.isAssignableFrom(entityType.getEntityClass())) {
            return CreatureType.GOLEM;
        }
        return CreatureType.UNKNOWN;
    }
*/
