package ru.komiss77.version.remapper;

import java.lang.reflect.Proxy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

/**
 * Factory for reflection proxy instances.
 */
@DefaultQualifier(NonNull.class)
public final class ReflectionProxyFactory {
  private final ReflectionRemapper reflectionRemapper;
  private final ClassLoader classLoader;

  private ReflectionProxyFactory(final ReflectionRemapper reflectionRemapper, final ClassLoader classLoader) {
    this.reflectionRemapper = reflectionRemapper;
    this.classLoader = classLoader;
  }

  /**
   * Create a new instance of the given "reflection proxy interface".
   *
   * @param proxyInterface reflection proxy interface class
   * @param <I>            interface type
   * @return reflection proxy instance
   * @throws IllegalArgumentException when the specified proxy interface is malformed or invalid for the current environment
   * @see Proxies
   */
  @SuppressWarnings("unchecked")
  public <I> I reflectionProxy(final Class<I> proxyInterface) {
    return (I) Proxy.newProxyInstance(
      this.classLoader,
      new Class<?>[]{proxyInterface},
      new ReflectionProxyInvocationHandler<>(
        proxyInterface,
        this.reflectionRemapper
      )
    );
  }

  /**
   * Create a new {@link ReflectionProxyFactory} using the specified
   * {@link ReflectionRemapper} for remapping, and the specified {@link ClassLoader}
   * to load reflection proxy implementation classes.
   *
   * @param reflectionRemapper reflection remapper
   * @param classLoader        classloader
   * @return new {@link ReflectionProxyFactory}
   */
  public static ReflectionProxyFactory create(
    final ReflectionRemapper reflectionRemapper,
    final ClassLoader classLoader
  ) {
    return new ReflectionProxyFactory(reflectionRemapper, classLoader);
  }
}
