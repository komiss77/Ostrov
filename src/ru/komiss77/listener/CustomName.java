package ru.komiss77.listener;

//import org.bukkit.entity.Entity;import org.bukkit.entity.Player;public class CustomName {public CustomName(Entity entity){}void setHidden(boolean b){}void close(){}void setTargetEntitySneaking(boolean sneaking){}void sendToClient(Player player) {}void removeFromClient(Player player){}}

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CustomName {

    private final SkeletonInteraction interaction = new SkeletonInteraction(this);
    private final double effectiveHeight;
    private final Entity targetEntity;
    private final double passengerOffset;
    private final int nametagEntityId = Bukkit.getUnsafe().nextEntityId();
    private boolean targetEntitySneaking;
    @Nullable
    private Component name;
    private boolean hidden;

    CustomName(Entity entity) {
        targetEntity = entity;
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        double ridingOffset = nmsEntity.bx();
        double nametagOffset = (double) nmsEntity.df();

        effectiveHeight = -ridingOffset - 0.5D + nametagOffset;
        passengerOffset = ridingOffset;
    }

    public void setName(Component name) {
        this.name = name;
        syncData();
    }

    public void setTargetEntitySneaking(boolean targetEntitySneaking) {
        this.targetEntitySneaking = targetEntitySneaking;
        this.syncData();
    }

    public void sendToClient(@NotNull Player entity) {
        if (!hidden) {
            ((CraftPlayer) entity).getHandle().c.a(interaction.initialSpawnPacket());
        }
    }

    public void removeFromClient(@NotNull Player entity) {
        ((CraftPlayer) entity).getHandle().c.a(interaction.removePacket());
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        runOnTrackers( (playerx) -> {
            if (hidden) {
                removeFromClient(playerx);
            } else {
                sendToClient(playerx);
            }

        });
    }

    @Nullable
    public Component getName() {
        return name;
    }

    public int getNametagId() {
        return nametagEntityId;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

    public boolean isTargetEntitySneaking() {
        return targetEntitySneaking;
    }

    public double getEffectiveHeight() {
        return effectiveHeight;
    }

    public double getPassengerOffset() {
        return passengerOffset;
    }

    private void syncData() {
        if (!hidden) {
            Packet dataPacket = interaction.syncDataPacket();

            runOnTrackers((playerx) -> {
                ((CraftPlayer) playerx).getHandle().c.a(dataPacket);
            });
        }
    }

    private void runOnTrackers(Consumer<Player> consumer) {
        targetEntity.getTrackedPlayers().forEach(player -> {
            consumer.accept(player);
        });

    }
}





/*
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;

//https://github.com/Owen1212055/CustomNames https://gist.github.com/Owen1212055/f5d59169d3a6a5c32f0c173d57eb199d
//https://github.com/jpenilla/reflection-remapper
//https://mappings.cephx.dev/index.html  FriendlyByteBuf

public class CustomName {

    private final CnSkeletonInteraction interaction = new CnSkeletonInteraction(this);

    // Target entity constants
    private final double effectiveHeight;
    private final Entity targetEntity;
    private final double passengerOffset;

    // Custom name constants
    private final int nametagEntityId;

    // States
    private boolean targetEntitySneaking;
    @Nullable
    private Component name;
    private boolean hidden;

    private final BukkitTask task;

    CustomName(Entity entity) {
        this.nametagEntityId = Bukkit.getUnsafe().nextEntityId();
        this.targetEntity = entity;

        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        double ridingOffset = nmsEntity.getPassengerRidingPosition(null).subtract(nmsEntity.position()).y;
        double nametagOffset = nmsEntity.getNameTagOffsetY();

        // First, negate the riding offset to get to the bounding of the entity's bounding box
        // Negate the natural nametag offset of interaction entities (0.5)
        // Add the actual offset of the nametag
        this.effectiveHeight = -ridingOffset - 0.5 + nametagOffset;
        this.passengerOffset = ridingOffset;

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                Packet<ClientGamePacketListener> riderPacket = CustomName.this.interaction.getRiderPacket();
                for (Player player : entity.getTrackedPlayers()) {
                    ((CraftPlayer) player).getHandle().c.send(riderPacket);
                }
            }
        }.runTaskTimer(CustomNamePlugin.getProvidingPlugin(CustomNamePlugin.class), 20, 20);
    }

    public void setName(Component name) {
        this.name = name;
        this.syncData();
    }

    public void setTargetEntitySneaking(boolean targetEntitySneaking) {
        this.targetEntitySneaking = targetEntitySneaking;
        this.syncData();
    }

    public void sendToClient(@NotNull Player entity) {
        if (this.hidden) {
            return;
        }

        ((CraftPlayer) entity).getHandle().c.send(this.interaction.initialSpawnPacket());
    }

    public void removeFromClient(@NotNull Player entity) {
        ((CraftPlayer) entity).getHandle().c.send(this.interaction.removePacket());
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        this.runOnTrackers((player) -> {
            if (hidden) {
                this.removeFromClient(player);
            } else {
                this.sendToClient(player);
            }
        });
    }

    @Nullable
    public Component getName() {
        return this.name;
    }

    public int getNametagId() {
        return this.nametagEntityId;
    }

    public Entity getTargetEntity() {
        return this.targetEntity;
    }

    public boolean isTargetEntitySneaking() {
        return this.targetEntitySneaking;
    }

    public double getEffectiveHeight() {
        return this.effectiveHeight;
    }

    public double getPassengerOffset() {
        return this.passengerOffset;
    }

    // Utilities
    private void syncData() {
        if (this.hidden) {
            return;
        }

        Packet<ClientGamePacketListener> dataPacket = this.interaction.syncDataPacket();
        this.runOnTrackers((player) -> {
            ((CraftPlayer) player).getHandle().connection.send(dataPacket);
        });
    }

    private void runOnTrackers(Consumer<Player> consumer) {
        for (Player player : this.targetEntity.getTrackedPlayers()) {
            consumer.accept(player);
        }
    }

    public void close() {
        this.task.cancel();
    }

    public boolean isHidden() {
        return this.hidden;
    }
}
*/