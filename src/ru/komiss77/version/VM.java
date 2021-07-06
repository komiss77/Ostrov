package ru.komiss77.version;

import org.bukkit.Bukkit;
import ru.komiss77.Ostrov;




public class VM {
    private static final String PACKAGE = "ru.komiss77.version";
    private static ServerVersion serverVersion;
    private static IServer nmsServer;
    private static INbt nmsNbtUtil;
    private static IEntityGroup nmsEntitygroup;
    private static INameTag nmsNameTag;

    public VM(Ostrov plugin) {
        String mcVersion = "1.0";

        try {
            mcVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            Ostrov.log_ok("VM : §fверсия сервера определена как §e"+mcVersion);            
        } catch (ArrayIndexOutOfBoundsException ex) {
            Ostrov.log_err("VM detect mcVersion : "+ex.getMessage());
            return;
        }
        
        serverVersion = null;
        try {
            serverVersion = ServerVersion.valueOf(mcVersion);
        } catch (Exception ex) {
            Ostrov.log_err("VM ServerVersion : "+ex.getMessage());
            return;
        }
        
        try {
            nmsServer = loadModule("Server");
            nmsNbtUtil = loadModule("Nbt");
            nmsEntitygroup = loadModule("EntityGroup");
            nmsNameTag = loadModule("NameTag");
        } catch (ReflectiveOperationException ex) {
            Ostrov.log_err("VM load module : "+ex.getMessage());
            return;
        }
        Ostrov.log_ok("VM : §fменеджер версий загрузил версию §e"+serverVersion);
    }


    
    @SuppressWarnings("unchecked")
    public static <T> T loadModule(final String name) throws ReflectiveOperationException{
        return (T) ReflectionUtils.instantiateObject(Class.forName(PACKAGE + "." + serverVersion + "." + name));
    }

    
    
    
    
    public static INbt getNmsNbtUtil() {
//System.out.println("============= getNmsNbtUtil="+nmsNbtUtil);
        return nmsNbtUtil;
    }

    public static IServer getNmsServer() {
        return nmsServer;
    }

    public static IEntityGroup getNmsEntitygroup() {
        return nmsEntitygroup;
    }

    public static INameTag getNmsNameTag() {
        return nmsNameTag;
    }

 



}
