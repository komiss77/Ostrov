package ru.komiss77.version.v1_20_R1;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import net.minecraft.network.protocol.game.PacketPlayInUpdateSign;
import ru.komiss77.Ostrov;

//ловит все ходящие пакеты на сервер
@Sharable
public class ServerInPacketHandler extends MessageToMessageDecoder<Object> {


    @Override
    public boolean acceptInboundMessage(Object msg) {
//Ostrov.log_warn("In acceptInboundMessage msg="+msg);
        return  msg instanceof PacketPlayInUpdateSign;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
        if (packet instanceof PacketPlayInUpdateSign signPacket) {
            final String chName = ctx.name();
//Ostrov.log_warn("In channelRead chName="+chName);
            
        }
        super.channelRead(ctx, packet);
        
    }

    @Override
    protected void decode(ChannelHandlerContext chc, Object i, List<Object> list) throws Exception {
      //  super.decode(chc, i, list);
    }

    
} 
    
    
