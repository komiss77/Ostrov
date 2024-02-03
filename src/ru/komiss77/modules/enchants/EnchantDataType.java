package ru.komiss77.modules.enchants;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

public class EnchantDataType implements PersistentDataType<String, CustomEnchant> {

    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public Class<CustomEnchant> getComplexType() {
        return CustomEnchant.class;
    }

    @Override
    public String toPrimitive(final CustomEnchant ce, final PersistentDataAdapterContext cont) {
        return ce.getKey().getKey();
    }

    @Override
    public CustomEnchant fromPrimitive(final String nm, final PersistentDataAdapterContext cont) {
        return CustomEnchant.getByKey(NamespacedKey.minecraft(nm));
    }
}
