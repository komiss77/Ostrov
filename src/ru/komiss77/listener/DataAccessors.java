package ru.komiss77.listener;

import java.lang.invoke.MethodHandles;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import ru.komiss77.version.remapper.ReflectionRemapper;

class DataAccessors {

    public static DataWatcherObject DATA_SHARED_FLAGS_ID;
    public static DataWatcherObject DATA_POSE;
    public static DataWatcherObject DATA_CUSTOM_NAME;
    public static DataWatcherObject DATA_CUSTOM_NAME_VISIBLE;
    public static DataWatcherObject DATA_WIDTH_ID;
    public static DataWatcherObject DATA_HEIGHT_ID;

    private static DataWatcherObject get(ReflectionRemapper reflectionRemapper, Class clazz, String name) {
        try {
            return (DataWatcherObject) MethodHandles
                    .privateLookupIn(clazz, MethodHandles.lookup())
                    .findStaticGetter(clazz, reflectionRemapper.remapFieldName(clazz, name), DataWatcherObject.class)
                    .invoke();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
    /*@SuppressWarnings("unchecked")
    private static <T> EntityDataAccessor<T> get(ReflectionRemapper reflectionRemapper, Class<?> clazz, String name) {
        try {
            return (EntityDataAccessor<T>) MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
                    .findStaticGetter(clazz, reflectionRemapper.remapFieldName(clazz, name), EntityDataAccessor.class).invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }*/
    
    static {
        ReflectionRemapper reflectionRemapper = ReflectionRemapper.forReobfMappingsInPaperJar();

        DataAccessors.DATA_SHARED_FLAGS_ID = get(reflectionRemapper, Entity.class, "DATA_SHARED_FLAGS_ID");
        DataAccessors.DATA_POSE = get(reflectionRemapper, Entity.class, "DATA_POSE");
        DataAccessors.DATA_CUSTOM_NAME = get(reflectionRemapper, Entity.class, "DATA_CUSTOM_NAME");
        DataAccessors.DATA_CUSTOM_NAME_VISIBLE = get(reflectionRemapper, Entity.class, "DATA_CUSTOM_NAME_VISIBLE");
        DataAccessors.DATA_WIDTH_ID = get(reflectionRemapper, Interaction.class, "DATA_WIDTH_ID");
        DataAccessors.DATA_HEIGHT_ID = get(reflectionRemapper, Interaction.class, "DATA_HEIGHT_ID");
    }
}


/*
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.Pose;
import xyz.jpenilla.reflectionremapper.ReflectionRemapper;

import java.lang.invoke.MethodHandles;
import java.util.Optional;


//https://github.com/jpenilla/reflection-remapper

class CnDataAccessors {

    public static EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID;
    public static EntityDataAccessor<Pose> DATA_POSE;
    public static EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME;
    public static EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE;

    // Interaction entity
    public static EntityDataAccessor<Float> DATA_WIDTH_ID;
    public static EntityDataAccessor<Float> DATA_HEIGHT_ID;

    static {
        final ReflectionRemapper reflectionRemapper = ReflectionRemapper.forReobfMappingsInPaperJar();

        DATA_SHARED_FLAGS_ID = get(reflectionRemapper, Entity.class, "DATA_SHARED_FLAGS_ID");
        DATA_POSE = get(reflectionRemapper, Entity.class, "DATA_POSE");
        DATA_CUSTOM_NAME = get(reflectionRemapper, Entity.class, "DATA_CUSTOM_NAME");
        DATA_CUSTOM_NAME_VISIBLE = get(reflectionRemapper, Entity.class, "DATA_CUSTOM_NAME_VISIBLE");

        DATA_WIDTH_ID = get(reflectionRemapper, Interaction.class, "DATA_WIDTH_ID");
        DATA_HEIGHT_ID = get(reflectionRemapper, Interaction.class, "DATA_HEIGHT_ID");
    }

    @SuppressWarnings("unchecked")
    private static <T> EntityDataAccessor<T> get(ReflectionRemapper reflectionRemapper, Class<?> clazz, String name) {
        try {
            return (EntityDataAccessor<T>) MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
                    .findStaticGetter(clazz, reflectionRemapper.remapFieldName(clazz, name), EntityDataAccessor.class).invoke();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
*/