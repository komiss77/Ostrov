package ru.komiss77.modules.items;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unchecked")
public class ItemData extends HashMap<DataComponentType, Object> {

    public @Nullable <D> D get(final DataComponentType.Valued<D> type) {
        final Object val = super.get(type);
        return val == null ? null : (D) val;//trust
    }

    public <D> D getOr(final DataComponentType.Valued<D> type, final D val) {
        final Object v = super.get(type);
        return v == null ? val : (D) v;//trust
    }

    public <D> D put(final DataComponentType.Valued<D> key, final D val) {
        final Object last = super.put(key, val);
        return last == null ? null : (D) last;//trust
    }

    public void put(final DataComponentType.NonValued key) {
        super.put(key, null);
    }

    public void putOf(final ItemStack it) {
        final Set<DataComponentType> datas = it.getDataTypes();
        if (datas.isEmpty()) return;
        for (final DataComponentType dtc : datas) {
            switch (dtc) {
                case final DataComponentType.NonValued nvd -> put(nvd);
                case final DataComponentType.Valued<?> vld -> addVal(vld, it);
                default -> {}
            }
        }
    }


    /**Does absolutely nothing*/
    @Deprecated(forRemoval = true)
    public void putAll(Map<? extends DataComponentType, ?> m) {}

    /**Does absolutely nothing,
     * @return always null*/
    @Deprecated(forRemoval = true)
    public @Nullable Object get(final Object key) {
        return null;
    }

    /**Does absolutely nothing,
     * @return always null*/
    @Deprecated(forRemoval = true)
    public @Nullable Object getOrDefault(final Object key, final Object def) {
        return null;
    }

    /**Does absolutely nothing,
     * @return always null*/
    @Deprecated(forRemoval = true)
    public @Nullable Object put(final DataComponentType key, final Object val) {
        return null;
    }

    public ItemStack addTo(final ItemStack it) {
        for (final DataComponentType dt : keySet()) {
            if (dt instanceof final DataComponentType.NonValued nvd) it.setData(nvd);
            else if (dt instanceof final DataComponentType.Valued<?> vld) setVal(vld, it);
        }
        return it;
    }

    private <D> void setVal(final DataComponentType.Valued<D> type, final ItemStack item) {
        final D val = get(type); if (val != null) item.setData(type, val);
    }

    private <D> void addVal(final DataComponentType.Valued<D> type, final ItemStack it) {
        put(type, it.getData(type));
    }

    public static @Nullable ItemData of(final ItemStack it) {
        final Set<DataComponentType> datas = it.getDataTypes();
        if (datas.isEmpty()) return null;
        final ItemData data = new ItemData();
        for (final DataComponentType dtc : datas) {
            switch (dtc) {
                case final DataComponentType.NonValued nvd -> data.put(nvd);
                case final DataComponentType.Valued<?> vld -> data.addVal(vld, it);
                default -> {}
            }
        }
        return data;
    }
}
