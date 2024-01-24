package ru.komiss77.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import io.netty.buffer.Unpooled;
import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutMount;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher.Item;
import net.minecraft.network.syncher.DataWatcher.b;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;

public class SkeletonInteraction {

    private final CustomName customName;

    public SkeletonInteraction(CustomName customName) {
        this.customName = customName;
    }

    public Packet initialSpawnPacket() {
        
        PacketDataSerializer buf = new PacketDataSerializer(Unpooled.buffer());
        buf.d(customName.getTargetEntity().getEntityId());
        buf.a(new int[]{customName.getNametagId()});
        
        PacketPlayOutEntityMetadata initialCreatePacket = new PacketPlayOutEntityMetadata(
                customName.getNametagId(),
                List.of(ofData(DataAccessors.DATA_WIDTH_ID, 0.0F), 
                        ofData(DataAccessors.DATA_HEIGHT_ID, 
                                (float) customName.getEffectiveHeight()), 
                        ofData(DataAccessors.DATA_POSE, EntityPose.i), 
                        ofData(DataAccessors.DATA_CUSTOM_NAME_VISIBLE, true)
                )
        );
        
        Packet syncData = syncDataPacket();
        
        PacketPlayOutEntityMetadata afterCreateData = new PacketPlayOutEntityMetadata(
                customName.getNametagId(),
                List.of(ofData(DataAccessors.DATA_HEIGHT_ID, 1.0E8F))
        );

        return new ClientboundBundlePacket(
                List.of(this.createPacket(), initialCreatePacket, syncData, new PacketPlayOutMount(buf), afterCreateData)
        );
    }

    private Packet createPacket() {
        Location location = customName.getTargetEntity().getLocation();
        return new PacketPlayOutSpawnEntity(
                customName.getNametagId(), 
                UUID.randomUUID(), 
                location.x(),
                location.y() + customName.getPassengerOffset(),
                location.z(), 
                0.0F, 
                0.0F, 
                EntityTypes.ab, 
                0, 
                Vec3D.b, 
                0.0D
        );
    }
    
    public Packet syncDataPacket() {
        ArrayList data = new ArrayList();

        data.add(ofData(DataAccessors.DATA_CUSTOM_NAME, Optional.ofNullable(PaperAdventure.asVanilla(customName.getName()))));
        byte value = (byte) (customName.isTargetEntitySneaking() ? 2 : 0);

        data.add(ofData(DataAccessors.DATA_SHARED_FLAGS_ID, value));
        return new PacketPlayOutEntityMetadata(customName.getNametagId(), data);
    }

    public Packet removePacket() {
        return new PacketPlayOutEntityDestroy(new int[]{customName.getNametagId()});
    }


    private static b ofData(DataWatcherObject data, Object value) {
        return (new Item(data, value)).e();
    }
}




/*
import io.netty.buffer.Unpooled;
import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

 //* This classed is used for sending packets related to the interaction entity
 //* sent to the client.
public class CnSkeletonInteraction {

    private final CustomName customName;

    public CnSkeletonInteraction(CustomName customName) {
        this.customName = customName;
    }

    public Packet<ClientGamePacketListener> removePacket() {
        return new ClientboundRemoveEntitiesPacket(this.customName.getNametagId());
    }

    public Packet<ClientGamePacketListener> syncDataPacket() {
        List<SynchedEntityData.DataValue<?>> data = new ArrayList<>();
        data.add(ofData(CnDataAccessors.DATA_CUSTOM_NAME, Optional.ofNullable(PaperAdventure.asVanilla(this.customName.getName()))));

        byte value = (byte) (this.customName.isTargetEntitySneaking() ? 1 << 1 : 0);
        value |= 0x20;
        data.add(ofData(CnDataAccessors.DATA_SHARED_FLAGS_ID, value));

        return new ClientboundSetEntityDataPacket(this.customName.getNametagId(), data);
    }

    public Packet<ClientGamePacketListener> getRiderPacket() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(this.customName.getTargetEntity().getEntityId());

        int[] passengerIds = this.passengerIds();
        buf.writeVarIntArray(passengerIds);

        return new ClientboundSetPassengersPacket(buf);
    }

    private int[] passengerIds() {
        List<Entity> passengers = this.customName.getTargetEntity().getPassengers(); //respect passengers
        int[] passengerIds = new int[passengers.size()];
        if (!this.customName.isHidden()) {
            int length = passengerIds.length;
            passengerIds = new int[length + 1];
            passengerIds[length] = this.customName.getNametagId(); //always add the entity if it is visible
        }
        for (int i = 0; i < passengers.size(); i++) {
            passengerIds[i] = passengers.get(i).getEntityId();
        }
        return passengerIds;
    }

    public Packet<?> initialSpawnPacket() {


        ClientboundSetEntityDataPacket initialCreatePacket = new ClientboundSetEntityDataPacket(this.customName.getNametagId(), List.of(
                ofData(CnDataAccessors.DATA_WIDTH_ID, 0f),
                ofData(CnDataAccessors.DATA_HEIGHT_ID, (float) this.customName.getEffectiveHeight()),
                ofData(CnDataAccessors.DATA_POSE, Pose.CROAKING),
                ofData(CnDataAccessors.DATA_CUSTOM_NAME_VISIBLE, true)
        ));
        Packet<ClientGamePacketListener> syncData = syncDataPacket();
        ClientboundSetEntityDataPacket afterCreateData = new ClientboundSetEntityDataPacket(this.customName.getNametagId(), List.of(
                ofData(CnDataAccessors.DATA_HEIGHT_ID, 511f)
        ));

        return new ClientboundBundlePacket(List.of(
                createPacket(), // Create entity
                initialCreatePacket,
                syncData,
                this.getRiderPacket(),
                afterCreateData
        ));
    }

    // int id, UUID uuid, double x, double y, double z, float pitch, float yaw, EntityType<?> entityType, int entityData, Vec3 velocity, double headYaw
    private Packet<ClientGamePacketListener> createPacket() {
        Location location = this.customName.getTargetEntity().getLocation();

        return new ClientboundAddEntityPacket(
                this.customName.getNametagId(),
                UUID.randomUUID(),
                location.x(),
                location.y() + this.customName.getPassengerOffset(), // Put the entity as close as possible to prevent lerping
                location.z(),
                0f,
                0f,
                EntityType.INTERACTION,
                0,
                Vec3.ZERO,
                0
        );
    }

    private static <T> SynchedEntityData.DataValue<T> ofData(EntityDataAccessor<T> data, T value) {
        return new SynchedEntityData.DataItem<>(data, value).value();
    }
}
*/