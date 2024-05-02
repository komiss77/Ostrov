package ru.komiss77.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ClassFinder {
    
    private static final char PKG_SEPARATOR = '.';
    private static final char DIR_SEPARATOR = '/';
    
    public static  Class<?>[] getClasses(final File pluginFile, String packageName) {
        final List<Class<?>> classes = new ArrayList<>();
        
                    final String packagePrefix = packageName.replace(PKG_SEPARATOR, DIR_SEPARATOR)+ '/';
                    try {
                        final JarInputStream jarFile = new JarInputStream(new FileInputStream(pluginFile));
                        JarEntry jarEntry;
                        while (true)  {
                            jarEntry = jarFile.getNextJarEntry();
                            if (jarEntry == null) break;
                            final String classPath = jarEntry.getName();
                            if (classPath.startsWith(packagePrefix) && classPath.endsWith(".class")) {
                                if (!classPath.contains("$")) {
                                    final String className = classPath.substring(0, classPath.length() - 6).replace('/', '.');
                                    
                                    try {
                                        classes.add(Class.forName(className));
                                    } catch (final ClassNotFoundException x) {}
                                }
                            }
                        }
                        jarFile.close();
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
        return classes.toArray(Class[]::new); //classes.toArray(new Class[classes.size()]);
    }

}
