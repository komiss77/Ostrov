package ru.komiss77.version;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ru.komiss77.utils.TCUtils;

//https://github.com/Owen1212055/CustomNames
//https://wiki.vg/Entity_metadata#Display
public class CustomTag {

    //    public static final boolean SELF_VIEW = true; //удобно для отладки видеть свой тэг //пускай всегда будет вкл, пока что, чтоб люди видели как выглядит их тег
    private static final byte FLAGS = (/*shadow*/1 /*| seethrough 0*/) & /*background*/ ~4;
    private final WeakReference<LivingEntity> taggedLink; //@Nullable может быть содержимое, но не контейнер
    private final boolean real;
    public final int tagEntityId;
    private final int[] idArray;
    private final double passengerOffset;
    //    private boolean replaceName = true;
//    private boolean sneak = false;
    private Predicate<Player> canSee = p -> true;
    private Component name;
    private boolean visible = true;


    private static final EntityDataAccessor<?> DATA_POSE, DATA_BILLBOARD_RENDER_CONSTRAINTS_ID,
        DATA_TEXT_ID, DATA_BACKGROUND_COLOR_ID, DATA_LINE_WIDTH_ID, DATA_STYLE_FLAGS_ID;

    //      new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), glowingByte
    //https://wiki.vg/Entity_metadata#Display
    static {
        DATA_POSE = new EntityDataAccessor<>(6, EntityDataSerializers.POSE);
        DATA_BILLBOARD_RENDER_CONSTRAINTS_ID = new EntityDataAccessor<>(15, EntityDataSerializers.BYTE);
        DATA_TEXT_ID = new EntityDataAccessor<>(23, EntityDataSerializers.COMPONENT);
        DATA_LINE_WIDTH_ID = Display.TextDisplay.DATA_LINE_WIDTH_ID;
        DATA_BACKGROUND_COLOR_ID = Display.TextDisplay.DATA_BACKGROUND_COLOR_ID;
        DATA_STYLE_FLAGS_ID = new EntityDataAccessor<>(27, EntityDataSerializers.BYTE);
    }

    public CustomTag(final LivingEntity tagged) {
        tagEntityId = net.minecraft.world.entity.Entity.nextEntityId();//Bukkit.getUnsafe().nextEntityId();
        idArray = new int[]{tagEntityId};
        taggedLink = new WeakReference<>(tagged);//entity;
        passengerOffset = tagged.getHeight(); //= ridingOffset;
        real = tagged.isValid();
        name = PaperAdventure.asVanilla(TCUtils.form(tagged.getName() + "\n\n"));
    }

    //Can contain \n for >1 lines
    public void content(final String name) {
//Ostrov.log("CustomTag content="+name);
        this.name = PaperAdventure.asVanilla(TCUtils.form(name + "\n\n"));
        if (visible) {
            sendTrackersPacket(spawnPacket());
        }
    }


    public void visible(final boolean visible) {
        this.visible = visible;
        sendTrackersPacket(visible ? spawnPacket() : killPacket());
    }

    public void canSee(final Predicate<Player> canSee) {
        this.canSee = canSee;
        if (visible) {
            sendTrackersPacket(spawnPacket());
        }
    }

    public boolean canSee(final Player pl) {
        return canSee.test(pl);
    }


    private void sendTrackersPacket(final Packet<?> packet) {
        final LivingEntity tgt = taggedLink.get();
        if (tgt == null) {
            return;
        }
        final ClientboundRemoveEntitiesPacket not = killPacket();
        if (real) {
            for (final Player p : tgt.getTrackedBy()) {
                Nms.sendPacket(p, canSee.test(p) ? packet : not);
            }

            if (tgt instanceof final Player pl) {
//Ostrov.log("CustomTag sendPacket real=true "+pl.getName());
                Nms.sendPacket(pl, packet);
            }
        } else {
            for (final Player p : tgt.getWorld().getPlayers()) {
//Ostrov.log("CustomTag sendPacket real=false "+p.getName());
                Nms.sendPacket(p, canSee.test(p) ? packet : not);
            }
        }
    }


    public void showTo(final Player p) {
        if (visible && canSee.test(p)) {
            Nms.sendPacket(p, spawnPacket());
        }
    }

    public void hideTo(final Player p) {
        Nms.sendPacket(p, killPacket());
    }

    public ClientboundBundlePacket spawnPacket() {
        final LivingEntity tgt = taggedLink.get();
        if (tgt == null) {
            return new ClientboundBundlePacket(List.of());
        }

        final Location location = tgt.getLocation();
        final ClientboundAddEntityPacket spawnPacket = new ClientboundAddEntityPacket(
            tagEntityId,
            UUID.randomUUID(),
            location.x(),
            location.y() + passengerOffset,
            location.z(),
            0.0F,
            0.0F,
            EntityType.TEXT_DISPLAY, //1201 EntityTypes.aX, //Interaction=ab, ItemDisplay=ae;   TextDisplay=aX;   BlockDisplay=j;
            0,
            Vec3.ZERO,//Vec3D.b,
            0.0D
        );

        final ClientboundSetEntityDataPacket initialCreatePacket = new ClientboundSetEntityDataPacket(
            tagEntityId,
            List.of(ofData(DATA_POSE, Pose.CROAKING),
                ofData(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, (byte) 3))//center view
        );

        final ClientboundSetEntityDataPacket syncDataPacket = syncPacket();

        //final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        //buf.writeVarInt(tgt.getEntityId());//1201 buf.d(tgt.getEntityId());
        //buf.writeVarIntArray(idArray);
        //final ClientboundSetPassengersPacket mountPacket = new ClientboundSetPassengersPacket(buf);
        final ClientboundSetPassengersPacket mountPacket = new ClientboundSetPassengersPacket(Craft.toNMS(tgt));

        return new ClientboundBundlePacket(
            List.of(spawnPacket, initialCreatePacket, syncDataPacket, mountPacket)
        );
    }


    public ClientboundSetEntityDataPacket syncPacket() {
        return new ClientboundSetEntityDataPacket(tagEntityId,
            List.of(ofData(DATA_TEXT_ID, name),
                ofData(DATA_LINE_WIDTH_ID, 1000),
                ofData(DATA_STYLE_FLAGS_ID, FLAGS),
                ofData(DATA_BACKGROUND_COLOR_ID, 1))
        );
    }

    public ClientboundRemoveEntitiesPacket killPacket() {
        return new ClientboundRemoveEntitiesPacket(tagEntityId);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static SynchedEntityData.DataValue<?> ofData(EntityDataAccessor key, Object value) {
        return SynchedEntityData.DataValue.create(key, value);
    }
}
