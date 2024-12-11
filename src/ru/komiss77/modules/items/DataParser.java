package ru.komiss77.modules.items;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import io.papermc.paper.datacomponent.DataComponentType;

public class DataParser extends HashMap<DataComponentType.Valued<?>, DataParser.Parser<?>> {

    public @Nullable <D> DataParser.Parser<D> get(final DataComponentType.Valued<D> type) {
        final Object val = super.get(type);
        return val == null ? null : (DataParser.Parser<D>) val;//trust
    }

    public <D> DataParser.Parser<D> getOr(final DataComponentType.Valued<D> type, final DataParser.Parser<D> val) {
        final Object v = super.get(type);
        return v == null ? val : (DataParser.Parser<D>) v;//trust
    }

    public <D> DataParser.Parser<D> put(final DataComponentType.Valued<D> key, final DataParser.Parser<D> val) {
        final Object last = super.put(key, val);
        return last == null ? null : (DataParser.Parser<D>) last;//trust
    }


    /**Does absolutely nothing*/
    @Deprecated(forRemoval = true)
    public void putAll(Map<? extends DataComponentType.Valued<?>, ? extends Parser<?>> m) {}

    /**Does absolutely nothing,
     * @return always null*/
    @Deprecated(forRemoval = true)
    public @Nullable DataParser.Parser<?> get(final Object key) {
        return null;
    }

    /**Does absolutely nothing,
     * @return always null*/
    @Deprecated(forRemoval = true)
    public @Nullable DataParser.Parser<?> getOrDefault(final Object key, final DataParser.Parser<?> def) {
        return null;
    }

    public interface Parser<D> {
        String write(final D val, final String... seps);
        D parse(final String str, final String... seps);
    }
}
