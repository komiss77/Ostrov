package ru.komiss77.version.empty;


import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import ru.komiss77.version.IEntityGroup;


public class EntityGroup implements IEntityGroup { 

    
    @Override
    public EntityGroup getEntytyGroup(final EntityType type) {
    	return getEntityGroup(type);
    }
    
    @Override
    public EntityGroup getEntityGroup(final EntityType type) {
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
        return EntityGroup.UNDEFINED;
    }

    @Override
    public boolean isGroup(final String tag_as_string){

        return false;
    }


    
    @Override
    public void sendLookAtPlayerPacket(final Player p, final Entity e) {
    }

    @Override
    public void sendLookResetPacket(final Player p, final Entity e) {
    }

    @Override
	public void colorGlow(final Entity le, final char cr, final boolean fakeGlow) {
    }

}
