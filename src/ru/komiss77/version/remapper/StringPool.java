package ru.komiss77.version.remapper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class StringPool {
  private final Map<String, String> pool;

  public StringPool(final Map<String, String> backingMap) {
    this.pool = backingMap;
  }

  public StringPool() {
    this(new HashMap<>());
  }

  public String string(final String string) {
    return this.pool.computeIfAbsent(string, Function.identity());
  }
}
