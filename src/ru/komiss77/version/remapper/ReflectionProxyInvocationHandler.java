package ru.komiss77.version.remapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
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
import ru.komiss77.version.remapper.annotation.ConstructorInvoker;
import ru.komiss77.version.remapper.annotation.FieldGetter;
import ru.komiss77.version.remapper.annotation.FieldSetter;
import ru.komiss77.version.remapper.annotation.MethodName;
import ru.komiss77.version.remapper.annotation.Static;
import ru.komiss77.version.remapper.annotation.Type;

final class ReflectionProxyInvocationHandler implements InvocationHandler {

    private static final Lookup LOOKUP = MethodHandles.lookup();
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private final Class interfaceClass;
    private final Class proxiedClass;
    private final Map methods = new HashMap();
    private final Map getters = new HashMap();
    private final Map setters = new HashMap();
    private final Map staticGetters = new HashMap();
    private final Map staticSetters = new HashMap();
    private final Map defaultMethods = new ConcurrentHashMap();

    ReflectionProxyInvocationHandler(final Class interfaceClass, final Class proxiedClass, final ReflectionRemapper reflectionRemapper) {
        this.interfaceClass = interfaceClass;
        this.proxiedClass = proxiedClass;
        this.scanInterface(reflectionRemapper);
    }

    public Object invoke(final Object proxy, final Method method, Object[] args) throws Throwable {
        if (isEqualsMethod(method)) {
            return proxy == args[0];
        } else if (isHashCodeMethod(method)) {
            return 0;
        } else if (isToStringMethod(method)) {
            return String.format("ReflectionProxy[interface=%s, implementation=%s, proxies=%s]", this.interfaceClass.getTypeName(), proxy.getClass().getTypeName(), this.proxiedClass.getTypeName());
        } else {
            if (args == null) {
                args = ReflectionProxyInvocationHandler.EMPTY_OBJECT_ARRAY;
            }

            if (method.isDefault()) {
                return this.handleDefaultMethod(proxy, method, args);
            } else {
                MethodHandle methodHandle = (MethodHandle) this.methods.get(method);

                if (methodHandle != null) {
                    return args.length == 0 ? methodHandle.invokeExact() : methodHandle.invokeExact(args);
                } else {
                    MethodHandle getter = (MethodHandle) this.getters.get(method);

                    if (getter != null) {
                        return getter.invokeExact(args[0]);
                    } else {
                        MethodHandle setter = (MethodHandle) this.setters.get(method);

                        if (setter != null) {
                            return setter.invokeExact(args[0], args[1]);
                        } else {
                            MethodHandle staticGetter = (MethodHandle) this.staticGetters.get(method);

                            if (staticGetter != null) {
                                return staticGetter.invokeExact();
                            } else {
                                MethodHandle staticSetter = (MethodHandle) this.staticSetters.get(method);

                                if (staticSetter != null) {
                                    return staticSetter.invokeExact(args[0]);
                                } else {
                                    throw new IllegalStateException();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Nullable
    private Object handleDefaultMethod(final Object proxy, final Method method, final Object[] args) throws Throwable {
        MethodHandle handle = (MethodHandle) this.defaultMethods.computeIfAbsent(method, (mxx) -> {
            return adapt(((MethodHandle) Util.sneakyThrows(() -> {
                return Util.handleForDefaultMethod(this.interfaceClass, (Method) mxx);
            })).bindTo(proxy));
        });

        return args.length == 0 ? handle.invokeExact() : handle.invokeExact(args);
    }

    private void scanInterface(final ReflectionRemapper reflectionRemapper) {
    //    Objects.requireNonNull(reflectionRemapper);
     //   this.scanInterface(reflectionRemapper::remapClassOrArrayName, (fieldNamex) -> {
     //       return reflectionRemapper.remapFieldName(this.proxiedClass, fieldNamex);
     //   }, (methodNamex, parametersx) -> {
     //       return reflectionRemapper.remapMethodName(this.proxiedClass, methodNamex, parametersx);
     //   });
    }

    private void scanInterface(final UnaryOperator classMapper, final UnaryOperator fieldMapper, final BiFunction methodMapper) {
        Method[] amethod = this.interfaceClass.getDeclaredMethods();
        int i = amethod.length;

        for (int j = 0; j < i; ++j) {
            Method method = amethod[j];

            if (!isEqualsMethod(method) && !isHashCodeMethod(method) && !isToStringMethod(method) && !Util.isSynthetic(method.getModifiers()) && !method.isDefault()) {
                boolean constructorInvoker = method.getDeclaredAnnotation(ConstructorInvoker.class) != null;

                if (constructorInvoker) {
                    this.methods.put(method, adapt((MethodHandle) Util.sneakyThrows(() -> {
                        return ReflectionProxyInvocationHandler.LOOKUP.unreflectConstructor(this.findProxiedConstructor(method, classMapper));
                    })));
                } else {
                    FieldGetter getterAnnotation = (FieldGetter) method.getDeclaredAnnotation(FieldGetter.class);
                    FieldSetter setterAnnotation = (FieldSetter) method.getDeclaredAnnotation(FieldSetter.class);

                    if (getterAnnotation != null && setterAnnotation != null) {
                        throw new IllegalArgumentException("Method " + method.getName() + " in " + this.interfaceClass.getTypeName() + " is annotated with @FieldGetter and @FieldSetter, don't know which to use.");
                    }

                    boolean hasStaticAnnotation = method.getDeclaredAnnotation(Static.class) != null;
                    MethodHandle handle;

                    if (getterAnnotation != null) {
                        handle = (MethodHandle) Util.sneakyThrows(() -> {
                            return ReflectionProxyInvocationHandler.LOOKUP.unreflectGetter(this.findProxiedField(getterAnnotation.value(), fieldMapper));
                        });
                        if (hasStaticAnnotation) {
                            checkParameterCount(method, this.interfaceClass, 0, "Static @FieldGetters should have no parameters.");
                            this.staticGetters.put(method, handle.asType(MethodType.methodType(Object.class)));
                        } else {
                            checkParameterCount(method, this.interfaceClass, 1, "Non-static @FieldGetters should have one parameter.");
                            this.getters.put(method, handle.asType(MethodType.methodType(Object.class, Object.class)));
                        }
                    } else if (setterAnnotation != null) {
                        handle = (MethodHandle) Util.sneakyThrows(() -> {
                            return ReflectionProxyInvocationHandler.LOOKUP.unreflectSetter(this.findProxiedField(setterAnnotation.value(), fieldMapper));
                        });
                        if (hasStaticAnnotation) {
                            checkParameterCount(method, this.interfaceClass, 1, "Static @FieldSetters should have one parameter.");
                            this.staticSetters.put(method, handle.asType(MethodType.methodType(Object.class, Object.class)));
                        } else {
                            checkParameterCount(method, this.interfaceClass, 2, "Non-static @FieldSetters should have two parameters.");
                            this.setters.put(method, handle.asType(MethodType.methodType(Object.class, Object.class, Object.class)));
                        }
                    } else {
                        if (!hasStaticAnnotation && method.getParameterCount() < 1) {
                            throw new IllegalArgumentException("Non-static method invokers should have at least one parameter. Method " + method.getName() + " in " + this.interfaceClass.getTypeName() + " has " + method.getParameterCount());
                        }

                        this.methods.put(method, adapt((MethodHandle) Util.sneakyThrows(() -> {
                            return ReflectionProxyInvocationHandler.LOOKUP.unreflect(this.findProxiedMethod(method, classMapper, methodMapper));
                        })));
                    }
                }
            }
        }

    }

    private static MethodHandle adapt(final MethodHandle handle) {
        return handle.type().parameterCount() == 0 ? handle.asType(MethodType.methodType(Object.class)) : handle.asSpreader(Object[].class, handle.type().parameterCount()).asType(MethodType.methodType(Object.class, Object[].class));
    }

    private static void checkParameterCount(final Method method, final Class holder, final int expected, final String message) {
        if (method.getParameterCount() != expected) {
            throw new IllegalArgumentException(String.format("Unexpected amount of parameters for method %s in %s, got %d while expecting %d. %s", method.getName(), holder.getTypeName(), method.getParameterCount(), expected, message));
        }
    }

    private static boolean isToStringMethod(final Method method) {
        return method.getName().equals("toString") && method.getParameterCount() == 0 && method.getReturnType() == String.class;
    }

    private static boolean isHashCodeMethod(final Method method) {
        return method.getName().equals("hashCode") && method.getParameterCount() == 0 && method.getReturnType() == Integer.TYPE;
    }

    private static boolean isEqualsMethod(final Method method) {
        return method.getName().equals("equals") && method.getParameterCount() == 1 && method.getReturnType() == Boolean.TYPE;
    }

    private Field findProxiedField(final String fieldName, final UnaryOperator fieldMapper) {
        Field field;

        try {
            field = this.proxiedClass.getDeclaredField((String) fieldMapper.apply(fieldName));
        } catch (NoSuchFieldException nosuchfieldexception) {
            throw new IllegalArgumentException("Could not find field '" + fieldName + "' in " + this.proxiedClass.getTypeName(), nosuchfieldexception);
        }

        try {
            field.setAccessible(true);
            return field;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not set access for field '" + fieldName + "' in " + this.proxiedClass.getTypeName(), exception);
        }
    }

    private Constructor findProxiedConstructor(final Method method, final UnaryOperator classMapper) {
        Class[] actualParams = (Class[]) Arrays.stream(method.getParameters()).map((px) -> {
            return this.resolveParameterTypeClass(px, classMapper);
        }).toArray((x$0x) -> {
            return new Class[x$0x];
        });

        Constructor constructor;

        try {
            constructor = this.proxiedClass.getDeclaredConstructor(actualParams);
        } catch (NoSuchMethodException nosuchmethodexception) {
            throw new IllegalArgumentException("Could not find constructor of " + this.proxiedClass.getTypeName() + " with parameter types " + Arrays.toString(method.getParameterTypes()), nosuchmethodexception);
        }

        try {
            constructor.setAccessible(true);
            return constructor;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not set access for proxy method target constructor of " + this.proxiedClass.getTypeName() + " with parameter types " + Arrays.toString(method.getParameterTypes()), exception);
        }
    }

    private Method findProxiedMethod(final Method method, final UnaryOperator classMapper, final BiFunction methodMapper) {
        boolean hasStaticAnnotation = method.getDeclaredAnnotation(Static.class) != null;
        Class[] actualParams;

        if (hasStaticAnnotation) {
            actualParams = (Class[]) Arrays.stream(method.getParameters()).map((px) -> {
                return this.resolveParameterTypeClass(px, classMapper);
            }).toArray((x$0x) -> {
                return new Class[x$0x];
            });
        } else {
            actualParams = (Class[]) Arrays.stream(method.getParameters()).skip(1L).map((px) -> {
                return this.resolveParameterTypeClass(px, classMapper);
            }).toArray((x$0x) -> {
                return new Class[x$0x];
            });
        }

        MethodName methodAnnotation = (MethodName) method.getDeclaredAnnotation(MethodName.class);
        String methodName = methodAnnotation == null ? method.getName() : methodAnnotation.value();

        Method proxiedMethod;

        try {
            proxiedMethod = this.proxiedClass.getDeclaredMethod((String) methodMapper.apply(methodName, actualParams), actualParams);
        } catch (NoSuchMethodException nosuchmethodexception) {
            throw new IllegalArgumentException("Could not find proxy method target method: " + this.proxiedClass.getTypeName() + " " + methodName);
        }

        try {
            proxiedMethod.setAccessible(true);
            return proxiedMethod;
        } catch (Exception exception) {
            throw new IllegalStateException("Could not set access for proxy method target method: " + this.proxiedClass.getTypeName() + " " + methodName, exception);
        }
    }

    private Class resolveParameterTypeClass(final Parameter parameter, final UnaryOperator classMapper) {
        Type typeAnnotation = (Type) parameter.getDeclaredAnnotation(Type.class);

        if (typeAnnotation == null) {
            return parameter.getType();
        } else if (typeAnnotation.value() == Object.class && typeAnnotation.className().isEmpty()) {
            throw new IllegalArgumentException("@Type annotation must either have value() or className() set.");
        } else if (typeAnnotation.value() != Object.class) {
            return Util.findProxiedClass(typeAnnotation.value(), classMapper);
        } else {
            try {
                Class namedClass = Class.forName((String) classMapper.apply(typeAnnotation.className()));

                return namedClass;
            } catch (ClassNotFoundException classnotfoundexception) {
                throw new IllegalArgumentException("Class " + typeAnnotation.className() + " specified in @Type annotation not found.", classnotfoundexception);
            }
        }
    }
}
