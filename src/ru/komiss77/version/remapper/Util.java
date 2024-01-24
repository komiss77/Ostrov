package ru.komiss77.version.remapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import ru.komiss77.version.remapper.annotation.Proxies;

public final class Util {

    @Nullable
    private static final Method PRIVATE_LOOKUP_IN = findMethod(MethodHandles.class, "privateLookupIn", Class.class, Lookup.class);
    @Nullable
    private static final Method DESCRIPTOR_STRING = findMethod(Class.class, "descriptorString");

    private Util() {}

    public static boolean mojangMapped() {
        return classExists("net.minecraft.server.level.ServerPlayer");
    }

    public static boolean classExists(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException classnotfoundexception) {
            return false;
        }
    }

    public static Throwable sneakyThrow(final Throwable ex) throws Throwable {
        throw ex;
    }

    public static Object sneakyThrows(final Util.ThrowingSupplier supplier)  {
        try {
            return supplier.get();
        } catch (Throwable throwable) {
            try {
                throw (RuntimeException ) sneakyThrow(throwable);
            } catch (Throwable ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public static boolean isSynthetic(final int modifiers) {
        return (modifiers & 4096) != 0;
    }

    public static Class findProxiedClass(final Class proxyInterface, final UnaryOperator classMapper) {
        if (!proxyInterface.isInterface()) {
            throw new IllegalArgumentException(proxyInterface.getTypeName() + " is not an interface annotated with @Proxies.");
        } else {
            Proxies proxies = (Proxies) proxyInterface.getDeclaredAnnotation(Proxies.class);

            if (proxies == null) {
                throw new IllegalArgumentException("interface " + proxyInterface.getTypeName() + " is not annotated with @Proxies.");
            } else if (proxies.value() == Object.class && proxies.className().isEmpty()) {
                throw new IllegalArgumentException("@Proxies annotation must either have value() or className() set. Interface: " + proxyInterface.getTypeName());
            } else if (proxies.value() != Object.class) {
                return proxies.value();
            } else {
                try {
                    return Class.forName((String) classMapper.apply(proxies.className()));
                } catch (ClassNotFoundException classnotfoundexception) {
                    throw new IllegalArgumentException("Could not find class for @Proxied className() " + proxies.className() + ".");
                }
            }
        }
    }

    @Nullable
    private static Method findMethod(final Class holder, final String name, final Class... paramTypes) {
        try {
            return holder.getDeclaredMethod(name, paramTypes);
        } catch (ReflectiveOperationException reflectiveoperationexception) {
            return null;
        }
    }

    public static MethodHandle handleForDefaultMethod(final Class interfaceClass, final Method method) throws Throwable {
        if (Util.PRIVATE_LOOKUP_IN == null) {
            Constructor constructor = Lookup.class.getDeclaredConstructor(Class.class);

            constructor.setAccessible(true);
            return ((Lookup) constructor.newInstance(interfaceClass)).in(interfaceClass).unreflectSpecial(method, interfaceClass);
        } else {
            return ((Lookup) Util.PRIVATE_LOOKUP_IN.invoke((Object) null, interfaceClass, MethodHandles.lookup())).findSpecial(interfaceClass, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()), interfaceClass);
        }
    }

    public static String descriptorString(final Class clazz) {
        if (Util.DESCRIPTOR_STRING != null) {
            try {
                return (String) Util.DESCRIPTOR_STRING.invoke(clazz);
            } catch (ReflectiveOperationException reflectiveoperationexception) {
                throw new RuntimeException("Failed to call Class#descriptorString", reflectiveoperationexception);
            }
        } else {
            return clazz == Long.TYPE ? "J" : (clazz == Integer.TYPE ? "I" : (clazz == Character.TYPE ? "C" : (clazz == Short.TYPE ? "S" : (clazz == Byte.TYPE ? "B" : (clazz == Double.TYPE ? "D" : (clazz == Float.TYPE ? "F" : (clazz == Boolean.TYPE ? "Z" : (clazz == Void.TYPE ? "V" : (clazz.isArray() ? "[" + descriptorString(clazz.getComponentType()) : 'L' + clazz.getName().replace('.', '/') + ';')))))))));
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier {

        Object get() throws Throwable;
    }
}
