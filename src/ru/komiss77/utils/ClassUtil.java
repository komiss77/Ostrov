package ru.komiss77.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
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

    /*
    в ванблоке например фазы копировались так:
     private static void copyPhasesFromAddonJar(File file) {
        try (JarFile jar = new JarFile(Core.file())) {
            //Obtain any locale files, save them and update
            listJarFiles(jar, PHASES, ".yml").forEach(lf -> saveResourceFromJar(jar, lf, file, false, true));
        } catch (Exception e) {
            Core.log_err("copyPhasesFromAddonJar : "+e.getMessage());
        }
    }
    в главном классе:
    public static File file() {
        return plugin.getFile();
    }
     */

  public static List<String> listJarFiles(JarFile pluginJar, String folderPath, String fileSuffix) {
    List<String> result = new ArrayList();
    Enumeration<JarEntry> entries = pluginJar.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = (JarEntry) entries.nextElement();
      String path = entry.getName();
      if (path.startsWith(folderPath) && entry.getName().endsWith(fileSuffix)) {
        result.add(entry.getName());
      }
    }
    return result;
  }

  public static File saveResourceFromJar(JarFile jar, String jarResource, File destinationFolder, boolean replace, boolean ignoreJarPath) {
    if (jarResource != null && !jarResource.equals("")) {
      jarResource = jarResource.replace('\\', '/');

      try {
        File writedFile;
        try {
          JarEntry jarConfig = jar.getJarEntry(jarResource);
          if (jarConfig == null) {
            throw new IllegalArgumentException("The embedded resource '" + jarResource + "' cannot be found in " + jar.getName());
          }

          //InputStream in = jar.getInputStream(jarConfig);
          try (InputStream in = jar.getInputStream(jarConfig)) {
            if (in == null) {
              throw new IllegalArgumentException("The embedded resource '" + jarResource + "' cannot be found in " + jar.getName());
            }

            File outFile = new File(destinationFolder, jarResource.replaceAll("/", Matcher.quoteReplacement(File.separator)));
            if (ignoreJarPath) {
              outFile = new File(destinationFolder, outFile.getName());
            }

            outFile.getParentFile().mkdirs();
            if (!outFile.exists() || replace) {
              Files.copy(in, outFile.toPath(), new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
            }

            writedFile = outFile;
          } catch (Throwable t) {
            throw t;
          }
        } catch (Throwable t) {
          try {
            jar.close();
          } catch (Throwable var10) {
            t.addSuppressed(var10);
          }

          throw t;
        }

        jar.close();
        return writedFile;
      } catch (IOException var14) {
        Ostrov.log_err("Could not save from jar file. From " + jarResource + " to " + destinationFolder.getAbsolutePath());
        return null;
      }
    } else {
      throw new IllegalArgumentException("ResourcePath cannot be null or empty");
    }
  }


}
