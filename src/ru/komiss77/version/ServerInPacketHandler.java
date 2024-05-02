package ru.komiss77.version;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import ru.komiss77.Ostrov;

import java.util.List;

//ловит все ходящие пакеты на сервер
@Sharable
public class ServerInPacketHandler extends MessageToMessageDecoder<Object> {


    @Override
    public boolean acceptInboundMessage(Object msg) {
      if (msg instanceof ServerboundInteractPacket) {
Ostrov.log_warn("In acceptInboundMessage msg="+msg);
      }
        return  msg instanceof ServerboundInteractPacket;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
        if (packet instanceof ServerboundInteractPacket p) {
            final String chName = ctx.name();
Ostrov.log_warn("In channelRead chName="+chName);
            
        }
        super.channelRead(ctx, packet);
        
    }

    @Override
    protected void decode(ChannelHandlerContext chc, Object i, List<Object> list) {
      //  super.decode(chc, i, list);
    }

    
}
