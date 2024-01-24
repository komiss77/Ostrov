package ru.komiss77.version.remapper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class StringPool {

    private final Map pool;

    public StringPool(final Map backingMap) {
        this.pool = backingMap;
    }

    public StringPool() {
        this(new HashMap());
    }

    public String string(final String string) {
        return (String) this.pool.computeIfAbsent(string, Function.identity());
    }
}
