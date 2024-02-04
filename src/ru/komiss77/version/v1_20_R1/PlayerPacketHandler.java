package ru.komiss77.version.v1_20_R1;
/*
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import org.bukkit.entity.Player;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.bots.BotEntity;
import ru.komiss77.modules.bots.BotManager;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.utils.PlayerInput;
import ru.komiss77.utils.inventory.InputButton;

import java.util.Iterator;



public class PlayerPacketHandler extends ChannelDuplexHandler {

    private final Oplayer op;
    
    PlayerPacketHandler(final Oplayer op) {
        this.op = op;
    }

    @Override
    public void channelRead(final ChannelHandlerContext chc, final Object packet) throws Exception {
        
        if (packet instanceof final PacketPlayInUseEntity useEntityPacket) {
            if (BotManager.enable.get()) {
                final int id = useEntityPacket.getEntityId();
                for (final BotEntity bot : BotManager.botById.values()) {
                    if (bot.af() == id) {
                        Server.useIdField.set(useEntityPacket, bot.rid);
                        break;
                    }
                }
//                if (useEntityPacket.getActionType() == PacketPlayInUseEntity.b.b) {}
            }
            
        } else if (packet instanceof PacketPlayInUpdateSign signPacket) {
            final Player p = op.getPlayer();//Bukkit.getPlayerExact(name);
            if (p!=null && PlayerInput.inputData.containsKey(p)) {  // в паспорте final String[] split = msg.split(" ");
                final String result = signPacket.d()[0] + " " + signPacket.d()[1] + " " + signPacket.d()[2] + " " + signPacket.d()[3];
                Ostrov.sync(  () -> PlayerInput.onInput(p, InputButton.InputType.SIGN, result), 0 );
                return; //пакет ввода с таблички не отдаём в сервер!
            }
        }
        
        super.channelRead(chc, packet);
    }

    
    @Override
    public void write(final ChannelHandlerContext chc, final Object packet, final ChannelPromise channelPromise) throws Exception {

        if (BotManager.enable.get()) {
            int id = 0;
            if (packet instanceof final PacketPlayOutSpawnEntity p) {
                id = p.a();
            } else if (packet instanceof final PacketPlayOutEntityMetadata p) {
                id = p.a();
            } else if (packet instanceof final PacketPlayOutEntityTeleport p) {
                id = p.a();
            } else if (packet instanceof final PacketPlayOutUpdateAttributes p) {
                id = p.a();
            } else if (packet instanceof PacketPlayOutEntity) {
                id = (int) Server.entityIdField.get(packet);
            }
            
            if (id != 0 && BotManager.botById.containsKey(id)) {
                return; //не пропускать пакеты дальше
            }
            
            if (packet instanceof ClientboundBundlePacket clientboundBundlePacket) {
                final Iterator<Packet<PacketListenerPlayOut>> pit = clientboundBundlePacket.a().iterator();
                while (pit.hasNext()) {
                    final Packet<?> pc = pit.next();
                    if (pc instanceof final PacketPlayOutSpawnEntity p) {
                        id = p.a();
                    } else if (pc instanceof final PacketPlayOutEntityMetadata p) {
                        id = p.a();
                    } else if (pc instanceof final PacketPlayOutEntityTeleport p) {
                        id = p.a();
                    } else if (pc instanceof final PacketPlayOutUpdateAttributes p) {
                        id = p.a();
                    } else if (pc instanceof PacketPlayOutEntity) {
                        id = (int) Server.entityIdField.get(pc);
                    }

                    if (id!=0 && BotManager.botById.containsKey(id)) {
                        pit.remove(); //вырезать пакет из кучи
                    }
                }
            }

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
                }


                Bukkit.getConsoleSender().sendMessage("p-" + packet);/
        }

        /*
        enum a
        a profile
        b chat
        c gamemode
        d UPDATE_LISTED
        e 
        f UPDATE_DISPLAY_NAME
        /
       // if (packet instanceof ClientboundPlayerInfoUpdatePacket  pip) {
//Ostrov.log_warn("playerInfoPacket ="+pip.toString());
            //pip.
       // }
        
        super.write(chc, packet, channelPromise);
    }
}

*/











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