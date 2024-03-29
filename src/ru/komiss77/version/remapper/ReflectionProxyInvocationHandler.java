package ru.komiss77.version.remapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import org.checkerframework.checker.nullness.qual.Nullable;
import static ru.komiss77.version.remapper.Util.handleForDefaultMethod;

final class ReflectionProxyInvocationHandler<I> implements InvocationHandler {
  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
  private static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};
  private final Class<I> interfaceClass;
  private final Map<Method, MethodHandle> methods = new HashMap<>();
  private final Map<Method, MethodHandle> getters = new HashMap<>();
  private final Map<Method, MethodHandle> setters = new HashMap<>();
  private final Map<Method, MethodHandle> staticGetters = new HashMap<>();
  private final Map<Method, MethodHandle> staticSetters = new HashMap<>();
  private final Map<Method, MethodHandle> defaultMethods = new ConcurrentHashMap<>(); // CHM as it's lazily populated

  ReflectionProxyInvocationHandler(
    final Class<I> interfaceClass,
    final ReflectionRemapper reflectionRemapper
  ) {
    this.interfaceClass = interfaceClass;
    this.scanInterface(reflectionRemapper);
  }

  @Override
  public @Nullable Object invoke(
    final Object proxy,
    final Method method,
    Object[] args
  ) throws Throwable {
    if (isEqualsMethod(method)) {
      return proxy == args[0];
    } else if (isHashCodeMethod(method)) {
      return 0;
    } else if (isToStringMethod(method)) {
      return String.format("ReflectionProxy[interface=%s, implementation=%s]", this.interfaceClass.getTypeName(), proxy.getClass().getTypeName());
    }

    if (args == null) {
      args = EMPTY_OBJECT_ARRAY;
    }

    if (method.isDefault()) {
      return this.handleDefaultMethod(proxy, method, args);
    }

    // Method or constructor
    final @Nullable MethodHandle methodHandle = this.methods.get(method);
    if (methodHandle != null) {
      if (args.length == 0) {
        return methodHandle.invokeExact();
      }
      return methodHandle.invokeExact(args);
    }

    // Getter
    final @Nullable MethodHandle getter = this.getters.get(method);
    if (getter != null) {
      return getter.invokeExact(args[0]);
    }

    // Setter
    final @Nullable MethodHandle setter = this.setters.get(method);
    if (setter != null) {
      return setter.invokeExact(args[0], args[1]);
    }

    // Static getter
    final @Nullable MethodHandle staticGetter = this.staticGetters.get(method);
    if (staticGetter != null) {
      return staticGetter.invokeExact();
    }

    // Static setter
    final @Nullable MethodHandle staticSetter = this.staticSetters.get(method);
    if (staticSetter != null) {
      return staticSetter.invokeExact(args[0]);
    }

    // ?
    throw new IllegalStateException();
  }

  private @Nullable Object handleDefaultMethod(
    final Object proxy,
    final Method method,
    final Object[] args
  ) throws Throwable {
    final MethodHandle handle = this.defaultMethods.computeIfAbsent(
      method,
      m -> adapt(Util.sneakyThrows(() -> handleForDefaultMethod(this.interfaceClass, m)).bindTo(proxy))
    );

    if (args.length == 0) {
      return handle.invokeExact();
    } else {
      return handle.invokeExact(args);
    }
  }

  private void scanInterface(final ReflectionRemapper reflectionRemapper) {
    Class<?> prevProxy = null;
    Class<?> prevProxied = null;

    for (final Class<?> cls : Util.topDownInterfaceHierarchy(this.interfaceClass)) {
      final Class<?> proxied = Util.findProxiedClass(cls, reflectionRemapper::remapClassName);

      if (prevProxied != null && !prevProxied.isAssignableFrom(proxied)) {
        throw new IllegalArgumentException(
          "Reflection proxy interface " + cls.getName() + " proxies " + proxied.getName() + ", and extends from reflection proxy interface "
            + prevProxy.getName() + " which proxies " + prevProxied.getName() + ", but the proxied types are not compatible."
        );
      }

      this.scanInterface(
        cls,
        proxied,
        reflectionRemapper::remapClassOrArrayName,
        fieldName -> reflectionRemapper.remapFieldName(proxied, fieldName),
        (methodName, parameters) -> reflectionRemapper.remapMethodName(proxied, methodName, parameters)
      );

      prevProxied = proxied;
      prevProxy = cls;
    }
  }

  private void scanInterface(
    final Class<?> interfaceClass,
    final Class<?> proxiedClass,
    final UnaryOperator<String> classMapper,
    final UnaryOperator<String> fieldMapper,
    final BiFunction<String, Class<?>[], String> methodMapper
  ) {
    for (final Method method : interfaceClass.getDeclaredMethods()) {
      if (isEqualsMethod(method) || isHashCodeMethod(method) || isToStringMethod(method) || Util.isSynthetic(method.getModifiers())) {
        continue;
      } else if (method.isDefault()) {
        // We just load default methods lazily, no mappings need to be resolved so there is no need to eagerly evaluate them before mappings are discarded.
        // Additionally, we can cache the bound handle.
        continue;
      }

      final boolean constructorInvoker = method.getDeclaredAnnotation(ConstructorInvoker.class) != null;
      if (constructorInvoker) {
        this.methods.put(
          method,
          adapt(Util.sneakyThrows(() -> LOOKUP.unreflectConstructor(this.findProxiedConstructor(proxiedClass, method, classMapper))))
        );
        continue;
      }

      final @Nullable FieldGetter getterAnnotation = method.getDeclaredAnnotation(FieldGetter.class);
      final @Nullable FieldSetter setterAnnotation = method.getDeclaredAnnotation(FieldSetter.class);
      if (getterAnnotation != null && setterAnnotation != null) {
        throw new IllegalArgumentException("Method " + method.getName() + " in " + interfaceClass.getTypeName() + " is annotated with @FieldGetter and @FieldSetter, don't know which to use.");
      }

      final boolean hasStaticAnnotation = method.getDeclaredAnnotation(Static.class) != null;

      if (getterAnnotation != null) {
        final MethodHandle handle = Util.sneakyThrows(() -> LOOKUP.unreflectGetter(findProxiedField(proxiedClass, getterAnnotation.value(), fieldMapper)));
        if (hasStaticAnnotation) {
          checkParameterCount(method, interfaceClass, 0, "Static @FieldGetters should have no parameters.");
          this.staticGetters.put(method, handle.asType(MethodType.methodType(Object.class)));
        } else {
          checkParameterCount(method, interfaceClass, 1, "Non-static @FieldGetters should have one parameter.");
          this.getters.put(method, handle.asType(MethodType.methodType(Object.class, Object.class)));
        }
        continue;
      }

      if (setterAnnotation != null) {
        final MethodHandle handle = Util.sneakyThrows(() -> LOOKUP.unreflectSetter(findProxiedField(proxiedClass, setterAnnotation.value(), fieldMapper)));
        if (hasStaticAnnotation) {
          checkParameterCount(method, interfaceClass, 1, "Static @FieldSetters should have one parameter.");
          this.staticSetters.put(method, handle.asType(MethodType.methodType(Object.class, Object.class)));
        } else {
          checkParameterCount(method, interfaceClass, 2, "Non-static @FieldSetters should have two parameters.");
          this.setters.put(method, handle.asType(MethodType.methodType(Object.class, Object.class, Object.class)));
        }
        continue;
      }

      if (!hasStaticAnnotation && method.getParameterCount() < 1) {
        throw new IllegalArgumentException("Non-static method invokers should have at least one parameter. Method " + method.getName() + " in " + interfaceClass.getTypeName() + " has " + method.getParameterCount());
      }

      this.methods.put(
        method,
        adapt(Util.sneakyThrows(() -> LOOKUP.unreflect(this.findProxiedMethod(proxiedClass, method, classMapper, methodMapper))))
      );
    }
  }

  private static MethodHandle adapt(final MethodHandle handle) {
    if (handle.type().parameterCount() == 0) {
      return handle.asType(MethodType.methodType(Object.class));
    }
    return handle.asSpreader(Object[].class, handle.type().parameterCount())
      .asType(MethodType.methodType(Object.class, Object[].class));
  }

  private static void checkParameterCount(final Method method, final Class<?> holder, final int expected, final String message) {
    if (method.getParameterCount() != expected) {
      throw new IllegalArgumentException(
        String.format("Unexpected amount of parameters for method %s in %s, got %d while expecting %d. %s", method.getName(), holder.getTypeName(), method.getParameterCount(), expected, message)
      );
    }
  }

  private static boolean isToStringMethod(final Method method) {
    return method.getName().equals("toString")
      && method.getParameterCount() == 0
      && method.getReturnType() == String.class;
  }

  private static boolean isHashCodeMethod(final Method method) {
    return method.getName().equals("hashCode")
      && method.getParameterCount() == 0
      && method.getReturnType() == int.class;
  }

  private static boolean isEqualsMethod(final Method method) {
    return method.getName().equals("equals")
      && method.getParameterCount() == 1
      && method.getReturnType() == boolean.class;
  }

  private static Field findProxiedField(
    final Class<?> proxiedClass,
    final String fieldName,
    final UnaryOperator<String> fieldMapper
  ) {
    final Field field;
    try {
      field = proxiedClass.getDeclaredField(fieldMapper.apply(fieldName));
    } catch (final NoSuchFieldException e) {
      throw new IllegalArgumentException("Could not find field '" + fieldName + "' in " + proxiedClass.getTypeName(), e);
    }
    try {
      field.setAccessible(true);
    } catch (final Exception ex) {
      throw new IllegalStateException("Could not set access for field '" + fieldName + "' in " + proxiedClass.getTypeName(), ex);
    }
    return field;
  }

  private Constructor<?> findProxiedConstructor(
    final Class<?> proxiedClass,
    final Method method,
    final UnaryOperator<String> classMapper
  ) {
    final Class<?>[] actualParams = Arrays.stream(method.getParameters())
      .map(p -> resolveParameterTypeClass(p, classMapper))
      .toArray(Class<?>[]::new);

    final Constructor<?> constructor;
    try {
      constructor = proxiedClass.getDeclaredConstructor(actualParams);
    } catch (final NoSuchMethodException ex) {
      throw new IllegalArgumentException("Could not find constructor of " + proxiedClass.getTypeName() + " with parameter types " + Arrays.toString(method.getParameterTypes()), ex);
    }
    try {
      constructor.setAccessible(true);
    } catch (final Exception ex) {
      throw new IllegalStateException("Could not set access for proxy method target constructor of " + proxiedClass.getTypeName() + " with parameter types " + Arrays.toString(method.getParameterTypes()), ex);
    }
    return constructor;
  }

  private Method findProxiedMethod(
    final Class<?> proxiedClass,
    final Method method,
    final UnaryOperator<String> classMapper,
    final BiFunction<String, Class<?>[], String> methodMapper
  ) {
    final boolean hasStaticAnnotation = method.getDeclaredAnnotation(Static.class) != null;

    final Class<?>[] actualParams;
    if (hasStaticAnnotation) {
      actualParams = Arrays.stream(method.getParameters())
        .map(p -> resolveParameterTypeClass(p, classMapper))
        .toArray(Class<?>[]::new);
    } else {
      actualParams = Arrays.stream(method.getParameters())
        .skip(1)
        .map(p -> resolveParameterTypeClass(p, classMapper))
        .toArray(Class<?>[]::new);
    }

    final @Nullable MethodName methodAnnotation = method.getDeclaredAnnotation(MethodName.class);
    final String methodName = methodAnnotation == null ? method.getName() : methodAnnotation.value();
    final Method proxiedMethod;
    try {
      proxiedMethod = proxiedClass.getDeclaredMethod(methodMapper.apply(methodName, actualParams), actualParams);
    } catch (final NoSuchMethodException e) {
      throw new IllegalArgumentException("Could not find proxy method target method: " + proxiedClass.getTypeName() + " " + methodName);
    }
    try {
      proxiedMethod.setAccessible(true);
    } catch (final Exception ex) {
      throw new IllegalStateException("Could not set access for proxy method target method: " + proxiedClass.getTypeName() + " " + methodName, ex);
    }

    return proxiedMethod;
  }

  private static Class<?> resolveParameterTypeClass(
    final Parameter parameter,
    final UnaryOperator<String> classMapper
  ) {
    final @Nullable Type typeAnnotation = parameter.getDeclaredAnnotation(Type.class);
    if (typeAnnotation == null) {
      return parameter.getType();
    }

    if (typeAnnotation.value() == Object.class && typeAnnotation.className().isEmpty()) {
      throw new IllegalArgumentException("@Type annotation must either have value() or className() set.");
    }

    if (typeAnnotation.value() != Object.class) {
      return Util.findProxiedClass(typeAnnotation.value(), classMapper);
    }

    final Class<?> namedClass;
    try {
      namedClass = Class.forName(classMapper.apply(typeAnnotation.className()));
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException("Class " + typeAnnotation.className() + " specified in @Type annotation not found.", e);
    }

    return namedClass;
  }
}
