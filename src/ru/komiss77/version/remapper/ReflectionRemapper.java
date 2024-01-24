package ru.komiss77.version.remapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public interface ReflectionRemapper {

    String remapClassName(String className);

    String remapFieldName(Class holdingClass, String fieldName);

    String remapMethodName(Class holdingClass, String methodName, Class... paramTypes);

    default String remapClassOrArrayName(final String name) {
        Objects.requireNonNull(name, "name");
        if (name.isEmpty()) {
            return name;
        } else if (name.charAt(0) == '[') {
            int last = name.lastIndexOf(91);

            try {
                if (name.charAt(last + 1) == 'L') {
                    String cls = name.substring(last + 2, name.length() - 1);

                    return name.substring(0, last + 2) + this.remapClassName(cls) + ';';
                } else {
                    return name;
                }
            } catch (IndexOutOfBoundsException indexoutofboundsexception) {
                return name;
            }
        } else {
            return this.remapClassName(name);
        }
    }

    static ReflectionRemapper noop() {
        return NoopReflectionRemapper.INSTANCE;
    }

    static ReflectionRemapper forMappings(final InputStream mappings, final String fromNamespace, final String toNamespace) {
        try {
            MemoryMappingTree tree = new MemoryMappingTree(true);

            tree.setSrcNamespace(fromNamespace);
            tree.setDstNamespaces(new ArrayList(Collections.singletonList(toNamespace)));
            MappingReader.read((Reader) (new InputStreamReader(mappings, StandardCharsets.UTF_8)), tree);
            return ReflectionRemapperImpl.fromMappingTree(tree, fromNamespace, toNamespace);
        } catch (IOException ioexception) {
            throw new RuntimeException("Failed to read mappings.", ioexception);
        }
    }

    static ReflectionRemapper forMappings(final Path mappings, final String fromNamespace, final String toNamespace) {
        try {
            InputStream stream = Files.newInputStream(mappings);

            ReflectionRemapper reflectionremapper;

            try {
                reflectionremapper = forMappings(stream, fromNamespace, toNamespace);
            } catch (Throwable throwable) {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                }

                throw throwable;
            }

            if (stream != null) {
                stream.close();
            }

            return reflectionremapper;
        } catch (IOException ioexception) {
            throw new RuntimeException(ioexception);
        }
    }

    static ReflectionRemapper forPaperReobfMappings(final Path mappings) {
        return Util.mojangMapped() ? noop() : forMappings(mappings, "mojang+yarn", "spigot");
    }

    static ReflectionRemapper forPaperReobfMappings(final InputStream mappings) {
        return Util.mojangMapped() ? noop() : forMappings(mappings, "mojang+yarn", "spigot");
    }

    static ReflectionRemapper forReobfMappingsInPaperJar() {
        if (Util.mojangMapped()) {
            return noop();
        } else {
            Class craftServerClass;

            try {
                Class bukkitClass = Class.forName("org.bukkit.Bukkit");
                Method getServerMethod = bukkitClass.getDeclaredMethod("getServer");

                craftServerClass = getServerMethod.invoke((Object) null).getClass();
            } catch (ReflectiveOperationException reflectiveoperationexception) {
                throw new RuntimeException(reflectiveoperationexception);
            }

            try {
                InputStream reobfIn = craftServerClass.getClassLoader().getResourceAsStream("META-INF/mappings/reobf.tiny");

                ReflectionRemapper reflectionremapper;

                try {
                    if (reobfIn == null) {
                        throw new IllegalStateException("Could not find mappings in expected location.");
                    }

                    reflectionremapper = forPaperReobfMappings(reobfIn);
                } catch (Throwable throwable) {
                    if (reobfIn != null) {
                        try {
                            reobfIn.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    }

                    throw throwable;
                }

                if (reobfIn != null) {
                    reobfIn.close();
                }

                return reflectionremapper;
            } catch (IOException ioexception) {
                throw new RuntimeException(ioexception);
            }
        }
    }
}
