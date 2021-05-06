package ru.komiss77.version.v1_15_R1;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Slime;
import org.bukkit.entity.WaterMob;
import ru.komiss77.version.IEntityGroup;


public class EntityGroup implements IEntityGroup { 

    

    @Override
    public IEntityGroup.EntityGroup getEntytyGroup(final EntityType type) {
        if (type==EntityType.BAT) return IEntityGroup.EntityGroup.UNDEFINED;

        if (Monster.class.isAssignableFrom(type.getEntityClass())
                || WaterMob.class.isAssignableFrom(type.getEntityClass())
                || Slime.class.isAssignableFrom(type.getEntityClass())
                || Ghast.class.isAssignableFrom(type.getEntityClass())
                ) {
            return IEntityGroup.EntityGroup.MONSTER;
        } else if (Animals.class.isAssignableFrom(type.getEntityClass())) {
            return IEntityGroup.EntityGroup.CREATURE;
        }
        return IEntityGroup.EntityGroup.UNDEFINED;
    }
    
    
    @Override
    public IEntityGroup.EntityGroup getEntytyType (final Entity entity) {
        //net.minecraft.server.v1_16_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        //EntityTypes nmstype = nmsEntity.getEntityType();
        //EnumCreatureType ect = nmstype.e();
//System.out.println("----getEntytyType isDead?"+entity.isDead()+" nms="+((CraftEntity) entity).getHandle().getEntityType().e() );        

        if (entity == null || entity.isDead()) return IEntityGroup.EntityGroup.UNDEFINED;
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
    public IEntityGroup.EntityGroup byTag(final String tag_as_string){
        for(IEntityGroup.EntityGroup set: IEntityGroup.EntityGroup.values()){
                if(String.valueOf(set.toString()).equals(tag_as_string.toUpperCase())){
                        return set;
                }
        }
        return IEntityGroup.EntityGroup.UNDEFINED;
    }

    @Override
    public boolean isGroup(final String tag_as_string){
        for(IEntityGroup.EntityGroup set: IEntityGroup.EntityGroup.values()){
                if(String.valueOf(set.toString()).equals(tag_as_string.toUpperCase())){
                        return true;
                }
        }
        return false;
    }


}
