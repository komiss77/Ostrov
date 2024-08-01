package ru.komiss77.utils;

import org.bukkit.entity.Player;

public class InvStatus {

    private final boolean isArmor;
    private final Player player;

    public InvStatus(boolean isArmor, Player p) {
        this.isArmor = isArmor;
        this.player = p;
    }

    public boolean isArmor() {
        return this.isArmor;
    }

    public boolean isOnline() {
        return this.player != null && this.player.isOnline();
    }

    public Player getPlayer() {
        return this.player;
    }
}
