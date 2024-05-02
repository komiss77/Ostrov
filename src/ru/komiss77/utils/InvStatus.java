package ru.komiss77.utils;

import org.bukkit.entity.Player;

public record InvStatus(boolean isArmor, Player player) {

    public boolean isOnline() {
        return this.player != null && this.player.isOnline();
    }
}
