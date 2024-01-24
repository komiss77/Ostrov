package ru.komiss77.version.remapper;

final class NoopReflectionRemapper implements ReflectionRemapper {

    static NoopReflectionRemapper INSTANCE = new NoopReflectionRemapper();

    private NoopReflectionRemapper() {}

    public String remapClassName(final String className) {
        return className;
    }

    public String remapFieldName(final Class holdingClass, final String fieldName) {
        return fieldName;
    }

    public String remapMethodName(final Class holdingClass, final String methodName, final Class... paramTypes) {
        return methodName;
    }

    public String remapClassOrArrayName(final String name) {
        return name;
    }
}
