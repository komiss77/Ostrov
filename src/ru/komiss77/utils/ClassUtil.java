package ru.komiss77.utils;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import ru.komiss77.Ostrov;


public class ClassUtil {

    private static final char PKG_SEPARATOR = '.';
    private static final char DIR_SEPARATOR = '/';


    public static <T extends Enum> T rotateEnum(T t) {
        try {
            Method values = t.getClass().getMethod("values");
            if (t.ordinal() == ((T[]) values.invoke(t)).length - 1)
                return ((T[]) values.invoke(t))[0];
            else
                return ((T[]) values.invoke(t))[t.ordinal() + 1];
        } catch (Exception ex) {
            return t;
        }
    }


    @SuppressWarnings("unchecked")
    public static <G> G rndElmt(final G... arr) {
        return arr[Ostrov.random.nextInt(arr.length)];
    }

    public static <G> G[] shuffle(final G[] ar) {
        int chs = ar.length >> 2;
        if (chs == 0) {
            if (ar.length > 1) {
                final G ne = ar[0];
                ar[0] = ar[ar.length - 1];
                ar[ar.length - 1] = ne;
            }
            return ar;
        }
        for (int i = ar.length - 1; i > chs; i--) {
            final int ni = Ostrov.random.nextInt(i);
            final G ne = ar[ni];
            ar[ni] = ar[i];
            ar[i] = ne;
            chs += ((chs - ni) >> 31) + 1;
        }
        return ar;
    }

    public static <T> boolean check(final T[] split, final int length, final boolean extra) {
        if (split.length < length) {
            Ostrov.log_err("Tried parsing " + Arrays.toString(split) + ", len-" + split.length + " < " + length);
            return false;
        }
        if (!extra && split.length > length) {
            Ostrov.log_err("Tried parsing " + Arrays.toString(split) + ", len-" + split.length + " > " + length + " (no extras)");
            return false;
        }
        return true;
    }

    public static Class<?>[] getClasses(final File pluginFile, String packageName) {
        final List<Class<?>> classes = new ArrayList<>();

        final String packagePrefix = packageName.replace(PKG_SEPARATOR, DIR_SEPARATOR) + '/';
        try {
            final JarInputStream jarFile = new JarInputStream(new FileInputStream(pluginFile));
            JarEntry jarEntry;
            while (true) {
                jarEntry = jarFile.getNextJarEntry();
                if (jarEntry == null) break;
                final String classPath = jarEntry.getName();
                if (classPath.startsWith(packagePrefix) && classPath.endsWith(".class")) {
                    if (!classPath.contains("$")) {
                        final String className = classPath.substring(0, classPath.length() - 6).replace('/', '.');

                        try {
                            classes.add(Class.forName(className));
                        } catch (final ClassNotFoundException x) {
                        }
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
