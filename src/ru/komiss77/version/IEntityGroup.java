package ru.komiss77.version;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;



public interface IEntityGroup {

	@Deprecated
    public EntityGroup getEntytyType (final Entity entity);
    public EntityGroup getEntityType (final Entity entity);
	@Deprecated
    public EntityGroup getEntytyGroup (final EntityType entityType);
    public EntityGroup getEntityGroup (final EntityType entityType);
    public EntityGroup byTag(final String tag_as_string);
    public boolean isGroup(final String tag_as_string);    

    
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
        
        public String displayName;
        
        private EntityGroup (final String displayName) {
            this.displayName = displayName;
        }

    }
    
    
    
    
    //ля фигур
    public void sendLookAtPlayerPacket (final Player p, final Entity e);
    
    public void sendLookResetPacket (final Player p, final Entity e);
    
	public void colorGlow(final Entity le, final char cr, final boolean fakeGlow);
    
    
    

}
