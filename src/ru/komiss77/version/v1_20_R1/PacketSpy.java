package ru.komiss77.version.v1_20_R1;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.util.Iterator;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayInUpdateSign;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.protocol.game.PacketPlayOutUpdateAttributes;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.bots.BotEntity;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.inventory.InputButton;



public class PacketSpy extends ChannelDuplexHandler {

    private final Oplayer op;
    
    PacketSpy(final Oplayer op) {
        this.op = op;
    }

    @Override
    public void channelRead(final ChannelHandlerContext chc, final Object packet) throws Exception {
        
        if (packet instanceof final PacketPlayInUseEntity useEntityPacket) {
            if (BotManager.enable.get()) {
                if (useEntityPacket.getActionType() == PacketPlayInUseEntity.b.b) {
                    final int id = useEntityPacket.getEntityId();
                    for (final BotEntity bot : BotManager.rIdBots.values()) {
                        if (bot.af() == id) {
                            Server.useIdField.set(useEntityPacket, bot.rid);
                            break;
                        }
                    }
                }
            }
            
        } else if (packet instanceof PacketPlayInUpdateSign signPacket) {
            //final String chName = chc.name();
//Ostrov.log_warn("inUpdateSign chc="+chName);
            //if (chName.startsWith("ostrov_")) {
                //final String name = chc.name().substring(7);
                //if (!name.isEmpty()) {
                    final Player p = op.getPlayer();//Bukkit.getPlayerExact(name);
                    if (p!=null && PlayerInput.inputData.containsKey(p)) {  // в паспорте final String[] split = msg.split(" ");
                        final String result = signPacket.d()[0] + " " + signPacket.d()[1] + " " + signPacket.d()[2] + " " + signPacket.d()[3];
                        Ostrov.sync(  () -> PlayerInput.onInput(p, InputButton.InputType.SIGN, result), 0 );
                        return; //пакет ввода с таблички не отдаём в сервер!
                    }
                //}
            //}
        }
        
        super.channelRead(chc, packet);
    }

    
    @Override
    public void write(final ChannelHandlerContext chc, final Object packet, final ChannelPromise channelPromise) throws Exception {

        if (BotManager.enable.get()) {
            int id = 0;
            if (packet instanceof PacketPlayOutSpawnEntity packetPlayOutSpawnEntity) {
                id = packetPlayOutSpawnEntity.a();
            } else if (packet instanceof PacketPlayOutEntityMetadata packetPlayOutEntityMetadata) {
                id = packetPlayOutEntityMetadata.a();
            } else if (packet instanceof PacketPlayOutEntityTeleport packetPlayOutEntityTeleport) {
                id = packetPlayOutEntityTeleport.a();
            } else if (packet instanceof PacketPlayOutUpdateAttributes packetPlayOutUpdateAttributes) {
                id = packetPlayOutUpdateAttributes.a();
            } else if (packet instanceof PacketPlayOutEntity) {
                id = (int) Server.entityIdField.get(packet);
            }
            
            if (id!=0 && BotManager.rIdBots.containsKey(id)) {
//Ostrov.log_warn("packet "+packet.getClass().getSimpleName()+" id="+id+" - return!");
                return; //не пропускать пакеты дальше
            }
            
            if (packet instanceof ClientboundBundlePacket clientboundBundlePacket) {
                final Iterator<Packet<PacketListenerPlayOut>> pit = clientboundBundlePacket.a().iterator();
                while (pit.hasNext()) {
                    final Packet<?> pc = pit.next();
                    if (pc instanceof PacketPlayOutSpawnEntity packetPlayOutSpawnEntity) {
                        id = packetPlayOutSpawnEntity.a();
                    } else if (pc instanceof PacketPlayOutEntityMetadata packetPlayOutEntityMetadata) {
                        id = packetPlayOutEntityMetadata.a();
                    } else if (pc instanceof PacketPlayOutEntity) {
                        id = (int) Server.entityIdField.get(pc);
                    }
                }
                
                if (id!=0 && BotManager.rIdBots.containsKey(id)) {
//Ostrov.log_warn("packet "+packet.getClass().getSimpleName()+" id="+id+" - remove!");
                    pit.remove();
                }
            }
        }

        /*
        enum a
        a profile
        b chat
        c gamemode
        d UPDATE_LISTED
        e 
        f UPDATE_DISPLAY_NAME
        */
       // if (packet instanceof ClientboundPlayerInfoUpdatePacket  pip) {
//Ostrov.log_warn("playerInfoPacket ="+pip.toString());
            //pip.
       // }
        
        super.write(chc, packet, channelPromise);
    }
}













//                if (packet instanceof PacketPlayOutScoreboardTeam) {
//                	p.sendMessage(((PacketPlayOutScoreboardTeam) packet).toString());
//                }
                /*if (packet instanceof PacketPlayOutKeepAlive 
                || packet instanceof PacketPlayOutUnloadChunk
                || packet instanceof ClientboundBundlePacket
                || packet instanceof PacketPlayOutViewCentre
                || packet instanceof ClientboundLevelChunkWithLightPacket
                || packet instanceof PacketPlayOutEntity
                || packet instanceof PacketPlayOutEntityDestroy) {
            super.write(chc, packet, channelPromise);
                return;
        }

        if (packet instanceof PacketPlayOutEntityMetadata 
                || packet instanceof ClientboundChunksBiomesPacket
                || packet instanceof PacketPlayOutUpdateTime
                || packet instanceof PacketPlayOutEntityHeadRotation
                || packet instanceof ClientboundSetActionBarTextPacket
                || packet instanceof PacketPlayOutEntityVelocity
                || packet instanceof PacketPlayOutUpdateAttributes) {
                return;
        }*/
//                Bukkit.getConsoleSender().sendMessage("p-" + packet);