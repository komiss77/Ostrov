package ru.komiss77.Objects;

import java.util.Collection;
import java.util.TreeSet;

public class CaseInsensitiveSet extends TreeSet<String> {

    public CaseInsensitiveSet() {
        super(String.CASE_INSENSITIVE_ORDER);
    }

    public CaseInsensitiveSet(Collection<? extends String> c) {
        this();
        addAll(c);
    }
}