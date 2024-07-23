package ru.komiss77.version;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.scoreboard.CraftScoreboard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Scoreboard;

public class Craft {

    public static DedicatedServer toNMS() {
        return ((CraftServer) Bukkit.getServer()).getServer();
    }

    public static ServerLevel toNMS(final World w) {
        return ((CraftWorld) w).getHandle();
    }

    public static ServerPlayer toNMS(final Player p) {
        return ((CraftPlayer) p).getHandle();
    }

    public static net.minecraft.world.scores.Scoreboard toNMS(final Scoreboard sb) {
        return ((CraftScoreboard) sb).getHandle();
    }

    public static net.minecraft.world.entity.LivingEntity toNMS(final LivingEntity le) {
        return ((CraftLivingEntity) le).getHandle();
    }

    public static net.minecraft.world.entity.Mob toNMS(final Mob mb) {
        return ((CraftMob) mb).getHandle();
    }

    public static net.minecraft.world.entity.Entity toNMS(final Entity ent) {
        return ((CraftEntity) ent).getHandle();
    }

    public static BlockData fromNMS(final BlockState bs) {
        return CraftBlockData.fromData(bs);
    }

    public static PlayerInventory fromNMS(final Inventory inv) {
        return new CraftInventoryPlayer(inv);
    }
}
