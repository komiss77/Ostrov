package ru.komiss77.utils.inventory;


import org.bukkit.entity.Player;

public interface InventoryProvider {

    void init(final Player player, final InventoryContent content);
    
    default void update(final Player p, final InventoryContent content) {
    };
    

    default void onClose(final Player p, final InventoryContent content) {
    }
    

    default void reopen(final Player p, final InventoryContent content) {
        content.getHost().open(p, content.pagination().getPage());
    }

}
