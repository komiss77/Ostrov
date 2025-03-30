package ru.komiss77.boot;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Keyed;

class TagMap extends HashMap<RegistryKey<? extends Keyed>, RegTag<? extends Keyed>[]> {

    @SuppressWarnings({"unchecked"})
    public @Nullable <D extends Keyed> RegTag<D>[] get(final RegistryKey<D> type) {
        final Object val = super.get(type);
        return val == null ? null : (RegTag<D>[]) val;
    }

    @SuppressWarnings("unchecked")
    public <D extends Keyed> RegTag<D>[] add(final RegTag<D> tag) {
        final RegTag<D>[] last = get(tag.registryKey());
        if (last == null) {
            final RegTag<?>[] rt = {tag};
            super.put(tag.registryKey(), rt);
            return (RegTag<D>[]) rt; //trust
        }
        final RegTag<?>[] rt = new RegTag<?>[last.length + 1];
        System.arraycopy(last, 0, rt, 0, last.length);
        rt[rt.length - 1] = tag;
        return (RegTag<D>[]) rt; //trust
    }

    /**Does absolutely nothing,
     * @return always null*/
    @Deprecated(forRemoval = true)
    public RegTag<? extends Keyed>[] put(final RegistryKey<? extends Keyed> key, final RegTag<? extends Keyed>[] value) {
        return null;
    }

    /**Does absolutely nothing*/
    @Deprecated(forRemoval = true)
    public void putAll(Map<? extends RegistryKey<? extends Keyed>, ? extends RegTag<? extends Keyed>[]> m) {}

    /**Does absolutely nothing,
     * @return always null*/
    @Deprecated(forRemoval = true)
    public @Nullable RegTag<? extends Keyed>[] get(final Object key) {
        return null;
    }

    /**Does absolutely nothing,
     * @return always null*/
    @Deprecated(forRemoval = true)
    public @Nullable RegTag<? extends Keyed>[] getOrDefault(final Object key, final RegTag<? extends Keyed>[] def) {
        return null;
    }
}
