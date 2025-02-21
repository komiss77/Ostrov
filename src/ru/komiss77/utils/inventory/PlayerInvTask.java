package ru.komiss77.utils.inventory;

import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


class PlayerInvTask extends BukkitRunnable {

    private final Player player;
    private final InventoryProvider provider;
    private final InventoryContent contents;

    public PlayerInvTask(Player player, InventoryProvider provider, InventoryContent contents) {
        this.player = Objects.requireNonNull(player);
        this.provider = Objects.requireNonNull(provider);
        this.contents = Objects.requireNonNull(contents);
    }

    @Override
    public void run() {
        provider.update(this.player, this.contents);
    }

}