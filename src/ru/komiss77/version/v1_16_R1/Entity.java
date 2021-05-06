package ru.komiss77.version.v1_16_R1;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.server.v1_16_R1.MathHelper;
import net.minecraft.server.v1_16_R1.WorldServer;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import ru.komiss77.version.IEntity;




public class Entity implements IEntity {

    
        //регистрация
        //try {
        //    registerCustomEntity(CustomSheep.class, 11);
        //} catch (Exception ex) {
        //    ex.printStackTrace();
        //}
    
    
    
    @Override
    //public void Register(Class<? extends net.minecraft.server.v1_16_R1.Entity> entityClass, int id) {  //на основе holographic displays
    public void Register( int id ) {  //на основе holographic displays
        
        
        
        try {
            
          //  final ReflectField<RegistryID<EntityTypes<?>>> REGISTRY_ID_FIELD = new ReflectField<>(RegistryMaterials.class, "b");
         //   final ReflectField<Object[]> ID_TO_CLASS_MAP_FIELD = new ReflectField<>(RegistryID.class, "d");

            // Use reflection to get the RegistryID of entities.
          //  RegistryID<EntityTypes<?>> registryID = REGISTRY_ID_FIELD.get(IRegistry.ENTITY_TYPE);
       //     Object[] idToClassMap = ID_TO_CLASS_MAP_FIELD.get(registryID);

            // Save the the ID -> EntityTypes mapping before the registration.
        //    Object oldValue = idToClassMap[id];
            
            //EnumCreatureType type = ((CraftEntity) entity).getHandle().getEntityType().e();
            // Register the EntityTypes object.
            //registryID.a(EntityTypes.a.a(EnumCreatureType.MONSTER).a(sizeWidth, sizeHeight).b().a((String) null), id);
        //    registryID.a( EntityTypes.a.a(EnumCreatureType.MONSTER).b().a((String) null), id);

            // Restore the ID -> EntityTypes mapping.
        //    idToClassMap[id] = oldValue;
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        
    }

    @Override
    public void UnRegister() {
    }


    
    
    
    

    
    
    
    
    
    
    public Entity spawnNMSSlime(org.bukkit.World bukkitWorld, net.minecraft.server.v1_16_R1.Entity nmsEntity, double x, double y, double z) {
            WorldServer nmsWorld = ((CraftWorld) bukkitWorld).getHandle();
            
            //EntityNMSSlime touchSlime = new EntityNMSSlime(nmsWorld, parentPiece);
           // nmsEntity.setLocationNMS(x, y, z);
            if (!addEntityToWorld(nmsWorld, nmsEntity)) {
                    //ConsoleLogger.handleSpawnFail(parentPiece);
            }
            //return nmsEntity;
            return null;
    }    
    
    
    
    
	private boolean addEntityToWorld(WorldServer nmsWorld, net.minecraft.server.v1_16_R1.Entity nmsEntity) {
	//Validator.isTrue(Bukkit.isPrimaryThread(), "Async entity add");
		
            final int chunkX = MathHelper.floor(nmsEntity.locX() / 16.0);
            final int chunkZ = MathHelper.floor(nmsEntity.locZ() / 16.0);

            if (!nmsWorld.isChunkLoaded(chunkX, chunkZ)) {
                    // This should never happen
                nmsEntity.dead = true;
                return false;
            }

            nmsWorld.getChunkAt(chunkX, chunkZ).a(nmsEntity);
            try {
                ReflectMethod<Void> REGISTER_ENTITY_METHOD = new ReflectMethod<>(WorldServer.class, "registerEntity", net.minecraft.server.v1_16_R1.Entity.class);
                REGISTER_ENTITY_METHOD.invoke(nmsWorld, nmsEntity);
                return true;
            } catch (Exception e) {
                    e.printStackTrace();
                    return false;
            }
        }    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    
 @SuppressWarnings("unchecked")
public class ReflectField<T> {
	
	private final Class<?> clazz;
	private final String name;
	
	private Field field;
	
	public ReflectField(Class<?> clazz, String name) {
		this.clazz = clazz;
		this.name = name;
	}
	
	private void init() throws Exception {
		if (field == null) {
			field = clazz.getDeclaredField(name);
			field.setAccessible(true);
		}
	}
	
	public T get(Object instance) throws Exception {
		init();
		return (T) field.get(instance);
	}
	
	public T getStatic() throws Exception {
		init();
		return (T) field.get(null);
	}
	
	public void set(Object instance, T value) throws Exception {
		init();
		field.set(instance, value);
	}
	
	public void setStatic(T value) throws Exception {
		init();
		field.set(null, value);
	}

}   
    
    
    
@SuppressWarnings("unchecked")
public class ReflectMethod<T> {
	
	private final Class<?> clazz;
	private final String name;
	private final Class<?>[] parameterTypes;
	
	private Method method;
	
	public ReflectMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		this.clazz = clazz;
		this.name = name;
		this.parameterTypes = parameterTypes;
	}
	
	private void init() throws Exception {
		if (method == null) {
			method = clazz.getDeclaredMethod(name, parameterTypes);
			method.setAccessible(true);
		}
	}
	
	public T invoke(Object instance, Object... args) throws Exception {
		init();
		return (T) method.invoke(instance, args);
	}
	
	public T invokeStatic(Object... args) throws Exception {
		init();
		return (T) method.invoke(null, args);
	}

}    
    
    
    
    
    
    
    
    
    
}
