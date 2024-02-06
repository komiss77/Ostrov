package ru.komiss77.version;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;


@Deprecated
public interface IEntityGroup {

	@Deprecated
    EntityGroup getEntytyType(final Entity entity);
	@Deprecated
    EntityGroup getEntytyGroup(final EntityType entityType);
    
    
    //ПЕРЕЗЖАЕМ в EntitiUtil, один фиг мультивесии не поддерживаются
    
    @Deprecated
    EntityGroup getEntityType(final Entity entity);
    
    @Deprecated
    EntityGroup getEntityGroup(final EntityType entityType);
    
    @Deprecated
    EntityGroup byTag(final String tag_as_string);
    @Deprecated
    boolean isGroup(final String tag_as_string);

    
    @Deprecated
    enum EntityGroup {
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
        
        public final String displayName;
        
        EntityGroup(final String displayName) {
            this.displayName = displayName;
        }

    }
    
    
    
    
    //ля фигур
    @Deprecated
    void sendLookAtPlayerPacket(final Player p, final Entity e);

    @Deprecated
    void sendLookResetPacket(final Player p, final Entity e);

    @Deprecated
	void colorGlow(final Entity le, final char cr, final boolean fakeGlow);
    
    
    

}
