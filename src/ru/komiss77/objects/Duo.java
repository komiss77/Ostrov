package ru.komiss77.objects;

import javax.annotation.Nullable;

public record Duo<F, S>(F key, S val) {
    @Override
    public String toString() {
        return key.toString() + ":" + val.toString();
    }

    public static <F, S> Duo<F, S> of(final F key, final S val) {
        return new Duo<>(key, val);
    }

    public static class Array<F, S> {
        private final Duo<F, S>[] arr;
        @SuppressWarnings("unchecked")
        public Array(final F[] keys, final S[] vals, final boolean strict) {
            arr = new Duo[strict ? Math.min(keys.length, vals.length) : Math.max(keys.length, vals.length)];
            for (int i = 0; i != arr.length; i++) {
                arr[i] = new Duo<>(i < keys.length ? keys[i] : null, i < vals.length ? vals[i] : null);
            }
        }

        public @Nullable F getKey(final int ix) {
            return ix < arr.length ? arr[ix].key : null;
        }

        public @Nullable S getVal(final int ix) {
            return ix < arr.length ? arr[ix].val : null;
        }

        public Duo<F, S> get(final int ix) {
            return ix < arr.length ? arr[ix] : new Duo<>(null, null);
        }

        public int size() {return arr.length;}
    }
}
