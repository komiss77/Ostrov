package ru.komiss77.version;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;

import ru.komiss77.Ostrov;

// фикс чата
// https://github.com/e-im/FreedomChat https://www.libhunt.com/r/FreedomChat
// 


public class VM {
	
    public static String mcVersion;
    
    private static IServer nmsServer;
    private static IEntityGroup nmsEntGroup;
    private static INameTag nmsNameTag;
    protected static IAnwillWrapper anwillWrapper;
    
    
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

    
    
    


    public static IServer getNmsServer() {
        return nmsServer;
    }

    public static IEntityGroup getNmsEntitygroup() {
        return nmsEntGroup;
    }

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
