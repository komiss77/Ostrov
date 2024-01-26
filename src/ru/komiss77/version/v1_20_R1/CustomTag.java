package ru.komiss77.version.v1_20_R1;

import java.lang.invoke.MethodHandles;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import io.netty.buffer.Unpooled;
import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutMount;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.entity.Player;
import ru.komiss77.version.VM;
import ru.komiss77.version.remapper.ReflectionRemapper;

//https://github.com/Owen1212055/CustomNames
//https://github.com/jpenilla/reflection-remapper

// мапы в файле \versions\1.20.х\paper-1.20.х.jar\META-INF\mappings\reobf.tiny

public class CustomTag {

    private static final boolean SELF_VIEW = true; //удобно для отладки видеть свой тэг
    protected boolean visible = true;
    protected boolean replaceName = true;
    @Nullable
    private Component name;
    private final WeakReference<Player> targetEntity;
    private final int nametagEntityId;
    private final double passengerOffset;
    private final float effectiveHeight;
    private boolean targetEntitySneaking;

    public static DataWatcherObject DATA_SHARED_FLAGS_ID, DATA_POSE, DATA_CUSTOM_NAME, DATA_CUSTOM_NAME_VISIBLE, DATA_WIDTH_ID, DATA_HEIGHT_ID;

    static {
        ReflectionRemapper reflectionRemapper = ReflectionRemapper.forReobfMappingsInPaperJar();
        DATA_SHARED_FLAGS_ID = get(reflectionRemapper, net.minecraft.world.entity.Entity.class, "DATA_SHARED_FLAGS_ID");
        DATA_POSE = get(reflectionRemapper, net.minecraft.world.entity.Entity.class, "DATA_POSE");
        DATA_CUSTOM_NAME = get(reflectionRemapper, net.minecraft.world.entity.Entity.class, "DATA_CUSTOM_NAME");
        DATA_CUSTOM_NAME_VISIBLE = get(reflectionRemapper, net.minecraft.world.entity.Entity.class, "DATA_CUSTOM_NAME_VISIBLE");
        DATA_WIDTH_ID = get(reflectionRemapper, Interaction.class, "DATA_WIDTH_ID");
        DATA_HEIGHT_ID = get(reflectionRemapper, Interaction.class, "DATA_HEIGHT_ID");
    }
    
    private static DataWatcherObject get(ReflectionRemapper reflectionRemapper, Class clazz, String name) {
        try {
            return (DataWatcherObject) MethodHandles
                    .privateLookupIn(clazz, MethodHandles.lookup())
                    .findStaticGetter(clazz, reflectionRemapper.remapFieldName(clazz, name), DataWatcherObject.class)
                    .invoke();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }




    
    public CustomTag(final Player entity) {
        nametagEntityId = net.minecraft.world.entity.Entity.nextEntityId();//Bukkit.getUnsafe().nextEntityId();
        targetEntity = new WeakReference<>(entity);//entity;
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        double ridingOffset = nmsEntity.bx();
        double nametagOffset = (double) nmsEntity.df();
        passengerOffset = ridingOffset; //= ridingOffset; 
        effectiveHeight = (float) (-ridingOffset - 0.5D + nametagOffset);//-ridingOffset - 0.5D + nametagOffset;
    }

    
    public void content(Component name) {
        this.name = name;
        visible(true);//visible = true;
        syncData();
    }

    public void visible(boolean visible) {
        //if (this.visible == visible) return;
        this.visible = visible  && targetEntity.get()!=null;
        if (visible) {
            sendTrackersPacket(initialSpawnPacket());
        } else {
            final Packet destroyPacket = new PacketPlayOutEntityDestroy(new int[]{nametagEntityId});
            sendTrackersPacket(destroyPacket);
        }
    }

    public void replaceName(boolean replace) {
        this.replaceName = replace;
        if (visible && targetEntity.get()!=null) {
            //final Packet destroyPacket = new PacketPlayOutEntityDestroy(new int[]{nametagEntityId});
            //sendTrackersPacket(destroyPacket);
            sendTrackersPacket(initialSpawnPacket());
        }
    }

    public void setTargetEntitySneaking(final boolean isSneaking) {
        this.targetEntitySneaking = isSneaking;//targetEntitySneaking;
        syncData();
    }

    
    
    private void syncData() {
        if (visible) {
            final Packet syncDataPacket = syncDataPacket();
            sendTrackersPacket(syncDataPacket);
        }
    }

    private void sendTrackersPacket(final Packet packet) {
        targetEntity.get().getTrackedPlayers().forEach(p -> {
            VM.getNmsServer().sendPacket(p, packet);//(CraftPlayer) p).getHandle().c.a(packet);
        });
        if (SELF_VIEW) VM.getNmsServer().sendPacket(targetEntity.get(), packet);//((CraftPlayer)targetEntity.get()).getHandle().c.a(packet);
    }
    
    
    public void showTo(final Player p) {
        if (visible && targetEntity.get()!=null) {
            VM.getNmsServer().sendPacket(p, initialSpawnPacket());
        }
    }

    public void hideFor(final Player p) {
//Ostrov.log("hide "+" for "+p.getName());
        final Packet destroyPacket = new PacketPlayOutEntityDestroy(new int[]{nametagEntityId});
        VM.getNmsServer().sendPacket(p, destroyPacket);
    }
   
    
    
    
    private Packet initialSpawnPacket() {
        final Location location = targetEntity.get().getLocation();
        final  Packet spawnPacket = new PacketPlayOutSpawnEntity(
                nametagEntityId, 
                UUID.randomUUID(), 
                location.x(),
                location.y() + passengerOffset,
                location.z(), 
                0.0F, 
                0.0F, 
                EntityTypes.ab, //Interaction=ab, ItemDisplay=ae;   TextDisplay=aX;   BlockDisplay=j;
                0, 
                Vec3D.b, 
                0.0D
        );
        
        final  PacketPlayOutEntityMetadata initialCreatePacket = new PacketPlayOutEntityMetadata(
                nametagEntityId,
                List.of(ofData(DATA_WIDTH_ID, 0.0F), 
                        //ofData(DATA_HEIGHT_ID, effectiveHeight), //(float) или крашит клиент!!
                        ofData(DATA_HEIGHT_ID, replaceName ? effectiveHeight - 0.4f : effectiveHeight), //(float) или крашит клиент!!
                        ofData(DATA_POSE, EntityPose.i), 
                        ofData(DATA_CUSTOM_NAME_VISIBLE, true)
                )
        );
        
        final  Packet syncDataPacket = syncDataPacket();
        
        final PacketDataSerializer buf = new PacketDataSerializer(Unpooled.buffer());
        buf.d(targetEntity.get().getEntityId());
        buf.a(new int[]{nametagEntityId});
        final  Packet mountPacket =  new PacketPlayOutMount(buf);
        
        final  PacketPlayOutEntityMetadata afterCreateData = new PacketPlayOutEntityMetadata(
                nametagEntityId,
                List.of(ofData(DATA_HEIGHT_ID, 1.0E8F))
        );

        return new ClientboundBundlePacket(
                List.of(spawnPacket, initialCreatePacket, syncDataPacket, mountPacket, afterCreateData)
        );
    }

    
    private Packet syncDataPacket() {
        final ArrayList data = new ArrayList();
        data.add(ofData(DATA_CUSTOM_NAME, Optional.ofNullable(PaperAdventure.asVanilla(name))));
        data.add(ofData(DATA_SHARED_FLAGS_ID, targetEntitySneaking ? (byte)2 : (byte)0)); //(byte) не убирать!!
//data.add(ofData(DATA_HEIGHT_ID, replaceName ? effectiveHeight - 0.3f : effectiveHeight)); //float или крашит клиент!!
        return new PacketPlayOutEntityMetadata(nametagEntityId, data);
    }



    private static DataWatcher.b ofData(DataWatcherObject data, Object value) {
        return (new DataWatcher.Item(data, value)).e();
    }

    
    
    
    
    
    
    
    
    
    
    
    
}


