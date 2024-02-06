package ru.komiss77.objects;

public class Duo<F, S> {

    public final F key;
    public final S val;

    public Duo(final F key, final S val) {
        this.key = key; this.val = val;
    }
}
