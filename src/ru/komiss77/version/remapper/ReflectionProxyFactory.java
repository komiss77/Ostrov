package ru.komiss77.version.remapper;

import java.util.Objects;
import ru.komiss77.version.remapper.ReflectionRemapper;

public final class ReflectionProxyFactory {

    private final ReflectionRemapper reflectionRemapper;
    private final ClassLoader classLoader;

    private ReflectionProxyFactory(final ReflectionRemapper reflectionRemapper, final ClassLoader classLoader) {
        this.reflectionRemapper = reflectionRemapper;
        this.classLoader = classLoader;
    }

    public Object reflectionProxy(final Class proxyInterface) {
        ClassLoader classloader = this.classLoader;
        Class[] aclass = new Class[]{proxyInterface};
        ReflectionRemapper reflectionremapper = this.reflectionRemapper;

        Objects.requireNonNull(this.reflectionRemapper);
        return null; //Proxy.newProxyInstance(classloader, aclass, new ReflectionProxyInvocationHandler(proxyInterface, Util.findProxiedClass(proxyInterface, reflectionremapper::remapClassName), this.reflectionRemapper));
    }

    public static ReflectionProxyFactory create(final ReflectionRemapper reflectionRemapper, final ClassLoader classLoader) {
        return new ReflectionProxyFactory(reflectionRemapper, classLoader);
    }
}
