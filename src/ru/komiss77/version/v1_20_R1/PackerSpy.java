package ru.komiss77.version.v1_20_R1;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.util.Iterator;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.protocol.game.PacketPlayOutUpdateAttributes;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.bots.BotEntity;
import ru.komiss77.modules.bots.BotManager;



class PackerSpy extends ChannelDuplexHandler {

    @Override
    public void channelRead(final ChannelHandlerContext chc, final Object packet) throws Exception {
        if (BotManager.enable.get()) {
            if (packet instanceof final PacketPlayInUseEntity uep) {
                if (uep.getActionType() == PacketPlayInUseEntity.b.b) {
                    final int id = uep.getEntityId();
                    for (final BotEntity bot : BotManager.rIdBots.values()) {
                        if (bot.af() == id) {
                            Server.useId.set(uep, bot.rid);
                            break;
                        }
                    }
                }
            }
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
                id = (int) Server.entId.get(packet);
            }
            
            if (id!=0 && BotManager.rIdBots.containsKey(id)) {
Ostrov.log_warn("packet "+packet.getClass().getSimpleName()+" id="+id+" - return!");
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
                        id = (int) Server.entId.get(pc);
                    }
                }
                
                if (id!=0 && BotManager.rIdBots.containsKey(id)) {
Ostrov.log_warn("packet "+packet.getClass().getSimpleName()+" id="+id+" - remove!");
                    pit.remove();
                }
            }
        }

        
        
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