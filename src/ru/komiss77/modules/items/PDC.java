package ru.komiss77.modules.items;

import java.io.Serializable;
import java.util.ArrayList;
import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import ru.komiss77.OStrap;
import ru.komiss77.objects.Duo;

public class PDC implements DataComponentType.Valued<PDC.Data> {
    public static final String ID = "pdc";
    public boolean isPersistent() {
        return true;
    }
    public @NotNull NamespacedKey getKey() {
        return OStrap.key(ID);
    }
    public static class Data extends ArrayList<Duo<NamespacedKey, Serializable>> {
        public boolean add(final NamespacedKey key, final Serializable val) {
            return add(new Duo<>(key, val));
        }

        @ApiStatus.Internal
        @Deprecated(forRemoval = true)
        public boolean add(final Duo<NamespacedKey, Serializable> e) {
            return super.add(e);
        }
    }
}
