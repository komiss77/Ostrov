package ru.komiss77.version;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.item.EnumColor;
import org.bukkit.Bukkit;
import ru.komiss77.Ostrov;
import ru.komiss77.version.remapper.ReflectionRemapper;

// фикс чата https://github.com/e-im/FreedomChat https://www.libhunt.com/r/FreedomChat
// https://github.com/jpenilla/reflection-remapper
// https://github.com/md-5/SpecialSource/blob/master/src/main/java/net/md_5/specialsource/RemapperProcessor.java

/*
getFields предоставит все общедоступные поля во всей иерархии классов.
getDeclaredFields предоставит все поля (независимо от доступности), но только текущего класса (а не каких-либо базовых классов).
*/
public class VM {
    public static final ReflectionRemapper REMAPPER;
    public static final EntityPose POSE_CROAKING;
    public static final EnumColor COLOR_WHITE;
    //private static final Method SignFill;

    //прогружаем всё нужное тут, чтобы потом не ловить внезапные ошибки при обращении к классам
    static {
        REMAPPER = ReflectionRemapper.forReobfMappingsInPaperJar();
        POSE_CROAKING = EntityPose.valueOf("CROAKING");//EntityPose.i final String nmsName = VM.REMAPPER.remapFieldName(EntityPose.class, "CROAKING"); //IllegalArgumentException
        COLOR_WHITE = EnumColor.valueOf("WHITE");// EnumColor.a

        //for (EntityPose ep : EntityPose.values()) {
//Ostrov.log("EntityPose."+ep.name());
        //}

        //try {
            //EntityPose CROAKING = EntityPose.valueOf("CROAKING");
//Ostrov.log("nmsName="+nmsName+" res="+CROAKING);
        //} catch (IllegalArgumentException ex) {
            //Ostrov.log_err("nmsName="+ex.getMessage());
        //}

    }



    public static String mcVersion;
    private static IServer nmsServer;
    private static IEntityGroup nmsEntGroup;
    private static INameTag nmsNameTag;
    protected static IAnwillWrapper anwillWrapper;
    
    
    public static DataWatcherObject<?> getDataWatcher(Class<?> clazz, String name) {
        try {
            final String nmsName = REMAPPER.remapFieldName(clazz, name);
            return (DataWatcherObject<?>) MethodHandles
                    .privateLookupIn(clazz, MethodHandles.lookup())
                    .findStaticGetter(clazz, nmsName, DataWatcherObject.class)
                    .invoke();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static Method getMethod(Class<?> clazz, String name) {

        //final String runtimeName = REMAPPER.remapClassName("net.minecraft.server.level.ServerPlayer");
        //final Class<?> serverPlayerClass = Class.forName(runtimeName); // Exception handling omitted for brevity
        //final String runtimeFieldName = REMAPPER.remapFieldName(serverPlayerClass, "seenCredits");

        //final String runtimeFieldName = REMAPPER.remapFieldName(net.minecraft.server.level.ServerPlayer., "seenCredits");

       /* try {
            return (DataWatcherObject<?>) MethodHandles
                    .privateLookupIn(clazz, MethodHandles.lookup())
                    .findStaticGetter(clazz, REMAPPER.remapFieldName(clazz, name), DataWatcherObject.class)
                    .invoke();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }*/
       return null;
    }







    public VM(Ostrov plugin) {

        try {          
            mcVersion = Bukkit.getServer().getClass().getPackage().getName().split("[.]")[3];
            Ostrov.log_ok("§fVM : версия сервера определена как "+mcVersion);            
        } catch (ArrayIndexOutOfBoundsException ex) {
            Ostrov.log_err("VM detect mcVersion : "+ex.getMessage());
            nmsServer = new ru.komiss77.version.empty.Server();
            nmsEntGroup = new ru.komiss77.version.empty.EntityGroup();
            nmsNameTag = new ru.komiss77.version.empty.NameTag();
            anwillWrapper = new ru.komiss77.version.empty.AnwillWrapper();
            return;
        }
        
        boolean error = false;
        
        try {
            nmsServer = (IServer)Class.forName("ru.komiss77.version."+mcVersion+".Server").getDeclaredConstructor().newInstance(); //loadModule("Server");
            nmsServer.pathServer();
            nmsServer.chatFix();
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            Ostrov.log_err("VM load module Server: "+ ex.getMessage());
            nmsServer = new ru.komiss77.version.empty.Server();
            error = true;
        }
        
           
        try {
            nmsEntGroup = (IEntityGroup)Class.forName("ru.komiss77.version."+mcVersion+".EntityGroup").getDeclaredConstructor().newInstance(); //loadModule("EntityGroup");
            //Ostrov.log_ok("VM : §fменеджер версий загрузил версию §e"+serverVersion);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            Ostrov.log_err("VM load module IEntityGroup: "+ ex.getMessage());
            nmsEntGroup = new ru.komiss77.version.empty.EntityGroup();
            error = true;
        }
        
        try {
            nmsNameTag = (INameTag)Class.forName("ru.komiss77.version."+mcVersion+".NameTag").getDeclaredConstructor().newInstance(); //loadModule("NameTag");
            //Ostrov.log_ok("VM : §fменеджер версий загрузил версию §e"+serverVersion);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            Ostrov.log_err("VM load module INameTag: "+ ex.getMessage());
            nmsNameTag = new ru.komiss77.version.empty.NameTag();
            error = true;
        }
        
        try {
            anwillWrapper = (IAnwillWrapper)Class.forName("ru.komiss77.version."+mcVersion+".AnwillWrapper").getDeclaredConstructor().newInstance(); //loadModule("NameTag");
            //Ostrov.log_ok("VM : §fменеджер версий загрузил версию §e"+serverVersion);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            Ostrov.log_err("VM load module IAnwillWrapper: "+ ex.getMessage());
            anwillWrapper = new ru.komiss77.version.empty.AnwillWrapper();
            error = true;
        }
        

        if (error) {
            Ostrov.log_warn("VM : ошибка при инициализации. Некоторые nms методы будут недоступны!");
        } else {
            Ostrov.log_ok("§fVM : менеджер версий загрузил версию "+mcVersion);
        }
    }


    
    //@SuppressWarnings("unchecked")
   // public static <T> T loadModule(final String name) throws ReflectiveOperationException{
   //     return (T) ReflectionUtils.instantiateObject(Class.forName(PACKAGE + "." + serverVersion + "." + name));
   // }   
    
    //@SuppressWarnings("unchecked")
    //public static <T> T loadModule(final String name) throws ReflectiveOperationException{
    //    return Class.forName(PACKAGE + "." + serverVersion + "." + name).getDeclaredConstructor().newInstance();
   // }

    
    
    


    public static IServer server() {
        return nmsServer;
    }
    @Deprecated
    public static IServer getNmsServer() {
        return nmsServer;
    }
    @Deprecated
    public static IEntityGroup getNmsEntitygroup() {
        return nmsEntGroup;
    }
    @Deprecated
    public static INameTag getNmsNameTag() {
        return nmsNameTag;
    }

 /*
    public static WorldServer getWorldServer(final World world) {
        try {
            Class<?> craftWorld = Class.forName("org.bukkit.craftbukkit."+mcVersion+".CraftWorld");
            return (WorldServer) craftWorld.getDeclaredMethod("getHandle").invoke(world);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
            Ostrov.log_warn("VM getWorldServer : "+ex.getMessage() );
            return null;
        }
    }

    public static org.bukkit.block.data.BlockData getBlockData(final IBlockData iBlockData) {
        try {
            Class<?> craftBlockData = Class.forName("org.bukkit.craftbukkit."+mcVersion+".block.data.CraftBlockData");
            BlockData b = (org.bukkit.block.data.BlockData) craftBlockData.getDeclaredMethod("fromData").invoke(iBlockData);
            return CraftBlockData.fromData(iBlockData);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException ex) {
     }
           Ostrov.log_warn("VM getBlockData : "+ex.getMessage() );
            return null;
        }
*/


}
