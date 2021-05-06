package ru.komiss77.utils.inventory;

import org.bukkit.entity.Player;

public interface InventoryProvider {
    
    void init(final Player player, final InventoryContent content);
    
    default void preInit(final Player player) {
    }
    
    default void onClose(final Player player, final InventoryContent content) {
    }
    
    default void reopen(final Player player, final InventoryContent content) {
        content.getHost().open(player, content.pagination().getPage());
    }
}
