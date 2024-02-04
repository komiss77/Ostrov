package ru.komiss77.objects;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import io.netty.buffer.Unpooled;
import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ru.komiss77.utils.TCUtils;
import ru.komiss77.version.VM;

//https://github.com/Owen1212055/CustomNames
//https://github.com/jpenilla/reflection-remapper
// мапы в файле \versions\1.20.х\paper-1.20.х.jar\META-INF\mappings\reobf.tiny

public class CustomTag {

//    public static final boolean SELF_VIEW = true; //удобно для отладки видеть свой тэг //пускай всегда будет вкл, пока что, чтоб люди видели как выглядит их тег
    private static final byte FLAGS = (/*shadow*/1 /*| seethrough 0*/) & /*background*/ ~4;
    private final WeakReference<LivingEntity> target; //@Nullable может быть содержимое, но не контейнер
    private final boolean real;
    private final int tagEntId;
    private final int[] idArr;
    private final double passengerOffset;
//    private boolean replaceName = true;
//    private boolean sneak = false;
    private Predicate<Player> canSee = p -> true;
    private IChatBaseComponent name;
    private boolean visible = true;


    private static final DataWatcherObject<?> DATA_POSE, DATA_BILLBOARD_RENDER_CONSTRAINTS_ID,
            DATA_TEXT_ID, DATA_BACKGROUND_COLOR_ID, DATA_LINE_WIDTH_ID, DATA_STYLE_FLAGS_ID;

    static {
        DATA_POSE = VM.getDataWatcher( net.minecraft.world.entity.Entity.class, "DATA_POSE");
        DATA_BILLBOARD_RENDER_CONSTRAINTS_ID = VM.getDataWatcher(net.minecraft.world.entity.Display.class, "DATA_BILLBOARD_RENDER_CONSTRAINTS_ID");
        DATA_TEXT_ID = VM.getDataWatcher(net.minecraft.world.entity.Display.TextDisplay.class, "DATA_TEXT_ID");
        DATA_LINE_WIDTH_ID = VM.getDataWatcher(net.minecraft.world.entity.Display.TextDisplay.class, "DATA_LINE_WIDTH_ID");
        DATA_BACKGROUND_COLOR_ID = VM.getDataWatcher(net.minecraft.world.entity.Display.TextDisplay.class, "DATA_BACKGROUND_COLOR_ID");
        DATA_STYLE_FLAGS_ID = VM.getDataWatcher(net.minecraft.world.entity.Display.TextDisplay.class, "DATA_STYLE_FLAGS_ID");
    }


    public CustomTag(final LivingEntity ent) {
        tagEntId = net.minecraft.world.entity.Entity.nextEntityId();//Bukkit.getUnsafe().nextEntityId();
        idArr = new int[]{tagEntId};
        target = new WeakReference<>(ent);//entity;
        passengerOffset = ent.getHeight(); //= ridingOffset;
        real = ent.isValid();
        name = PaperAdventure.asVanilla(TCUtils.format(ent.getName() + "\n"));
    }

    //Can contain \n for >1 lines
    public void content(final String name) {
//Ostrov.log("CustomTag content="+name);
        this.name = PaperAdventure.asVanilla(TCUtils.format(name + "\n"));
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
        final LivingEntity tgt = target.get();
        if (tgt == null) {
            return;
        }
        final PacketPlayOutEntityDestroy not = killPacket();
        if (real) {
            for (final Player p : tgt.getTrackedBy()) {
                VM.server().sendPacket(p, canSee.test(p) ? packet : not);
            }

            if (tgt instanceof final Player pl) {
//Ostrov.log("CustomTag sendPacket real=true "+pl.getName());
                VM.server().sendPacket(pl, packet);
            }
        } else {
            for (final Player p : tgt.getWorld().getPlayers()) {
//Ostrov.log("CustomTag sendPacket real=false "+p.getName());
                VM.server().sendPacket(p, canSee.test(p) ? packet : not);
            }
        }
    }

    public void showTo(final Player p) {
        if (visible && canSee.test(p)) {
            VM.server().sendPacket(p, spawnPacket());
        }
    }

    public void hideTo(final Player p) {
        VM.server().sendPacket(p, killPacket());
    }

    public ClientboundBundlePacket spawnPacket() {
        final LivingEntity tgt = target.get();
        if (tgt == null) {
            return new ClientboundBundlePacket(List.of());
        }

        final Location location = tgt.getLocation();
        final PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(
                tagEntId,
                UUID.randomUUID(),
                location.x(),
                location.y() + passengerOffset,
                location.z(),
                0.0F,
                0.0F,
                EntityTypes.aY, //1201 EntityTypes.aX, //Interaction=ab, ItemDisplay=ae;   TextDisplay=aX;   BlockDisplay=j;
                0,
                Vec3D.b,
                0.0D
        );

        final PacketPlayOutEntityMetadata initialCreatePacket = new PacketPlayOutEntityMetadata(
                tagEntId,
                List.of(ofData(DATA_POSE, VM.POSE_CROAKING), //EntityPose.i
                        ofData(DATA_BILLBOARD_RENDER_CONSTRAINTS_ID, (byte) 3))//center view
        );

        final PacketPlayOutEntityMetadata syncDataPacket = syncPacket();

        final PacketDataSerializer buf = new PacketDataSerializer(Unpooled.buffer());
        buf.c(tgt.getEntityId());//1201 buf.d(tgt.getEntityId());
        buf.a(idArr);
        final PacketPlayOutMount mountPacket = new PacketPlayOutMount(buf);

        return new ClientboundBundlePacket(
                List.of(spawnPacket, initialCreatePacket, syncDataPacket, mountPacket)
        );
    }

    public PacketPlayOutEntityMetadata syncPacket() {
        return new PacketPlayOutEntityMetadata(tagEntId,
                List.of(ofData(DATA_TEXT_ID, name),
                        //                ofData(DATA_SHARED_FLAGS_ID, (byte) (sneak ? 2 : 0)), //(byte) не убирать!!
                        ofData(DATA_LINE_WIDTH_ID, 1000),
                        ofData(DATA_STYLE_FLAGS_ID, FLAGS),
                        ofData(DATA_BACKGROUND_COLOR_ID, 1)));
    }

    public PacketPlayOutEntityDestroy killPacket() {
        return new PacketPlayOutEntityDestroy(idArr);
    }

    @SuppressWarnings("rawtypes")
    private static DataWatcher.b<?> ofData(DataWatcherObject<?> data, Object value) {
        return (new DataWatcher.Item(data, value)).e();
    }

}




/*
//    public static DataWatcherObject<?> DATA_SHARED_FLAGS_ID, DATA_POSE, DATA_CUSTOM_NAME, DATA_CUSTOM_NAME_VISIBLE, DATA_WIDTH_ID, DATA_HEIGHT_ID;
        //final ReflectionRemapper reflectionRemapper = ReflectionRemapper.forReobfMappingsInPaperJar();
//        DATA_SHARED_FLAGS_ID = get(reflectionRemapper, net.minecraft.world.entity.Entity.class, "DATA_SHARED_FLAGS_ID");
//        DATA_CUSTOM_NAME_VISIBLE = get(reflectionRemapper, net.minecraft.world.entity.Entity.class, "DATA_CUSTOM_NAME_VISIBLE");
//        DATA_WIDTH_ID = get(reflectionRemapper, Interaction.class, "DATA_WIDTH_ID");
//        DATA_HEIGHT_ID = get(reflectionRemapper, Interaction.class, "DATA_HEIGHT_ID");

    private static DataWatcherObject<?> get(ReflectionRemapper reflectionRemapper, Class<?> clazz, String name) {
        try {
            return (DataWatcherObject<?>) MethodHandles
                    .privateLookupIn(clazz, MethodHandles.lookup())
                    .findStaticGetter(clazz, reflectionRemapper.remapFieldName(clazz, name), DataWatcherObject.class)
                    .invoke();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    //Can contain \n for >1 lines
    public void content(final String name, final @Nullable Boolean visible) {
        this.name = PaperAdventure.asVanilla(TCUtils.format(name + "\n\n"));
        visible(visible == null ? this.visible : visible);
    }

    public void sneaking(final boolean sneak) {
        this.sneak = sneak;
        if (visible) {
            sendTrackersPacket(initialSpawnPacket());
        }
    }

    public void replaceName(boolean replace) {
        this.replaceName = replace;
        if (visible) {
            sendTrackersPacket(initialSpawnPacket());
        }
    }

*/