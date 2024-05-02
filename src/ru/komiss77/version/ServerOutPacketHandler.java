package ru.komiss77.version;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;

//https://github.com/e-im/FreedomChat/blob/main/src/main/java/ru/bk/oharass/freedomchat/FreedomHandler.java
//https://github.com/e-im/FreedomChat
//https://www.libhunt.com/r/FreedomChat

//декомпилить плагин!

@Sharable
public class ServerOutPacketHandler extends MessageToByteEncoder<Object> {

    private final boolean rewriteChat = true;
    private final boolean claimSecureChatEnforced = true;
    private final boolean noChatReports = true;
    
    @Override
    public boolean acceptOutboundMessage(Object msg) {
        return rewriteChat && msg instanceof ClientboundPlayerChatPacket
            || noChatReports && msg instanceof ClientboundStatusResponsePacket
            || claimSecureChatEnforced && msg instanceof ClientboundServerDataPacket;
    }

  @Override
  protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) {

  }

    /*@Override
    protected void encode(final ChannelHandlerContext ctx, final Object msg, final ByteBuf out) {

        if (msg instanceof ClientboundPlayerChatPacket p) {
            encode(ctx, p, new FriendlyByteBuf(out));
        } else if (msg instanceof ClientboundServerDataPacket p) {
            encode(ctx, p, new FriendlyByteBuf(out));
        } else if (msg instanceof ClientboundStatusResponsePacket p) {
            encode(ctx, p, new FriendlyByteBuf(out));
        } //else if (msg instanceof PacketPlayOutOpenSignEditor p) {
//Ostrov.log("--PacketPlayOutOpenSignEditor ");
//        }
    }



      private void encode(final ChannelHandlerContext ctx, final ClientboundPlayerChatPacket msg, final FriendlyByteBuf buf) {
        final Component content = Objects.requireNonNullElseGet(msg.unsignedContent(), () -> Component.literal(msg.body().content()));

        final Optional<ChatType.Bound> ctbo = msg.chatType().resolve(MinecraftServer.getServer().registryAccess());
        if (ctbo.isEmpty()) {
          Ostrov.log_warn("Processing packet with unknown ChatType " + msg.chatType().chatType());
          return;
        }
        final Component decoratedContent = ctbo.orElseThrow().decorate(content);

        final ClientboundSystemChatPacket system = new ClientboundSystemChatPacket(decoratedContent, false);
        writeId(ctx, system, buf);
        system.write(buf);
      }

      private void encode(final ChannelHandlerContext ctx, final ClientboundServerDataPacket msg, final FriendlyByteBuf buf) {
        writeId(ctx, msg, buf);
        buf.writewriteComponent(msg.motd());
        buf.writeOptional(msg.iconBytes(), (bf, value) -> {
          FriendlyByteBuf.writeByteArray(bf, value);
        });
        buf.writeBoolean(true);
      }

      private void encode(final ChannelHandlerContext ctx, final ClientboundStatusResponsePacket msg, final FriendlyByteBuf buf) {
        final JsonObject status = ServerStatus.CODEC
          .encodeStart(JsonOps.INSTANCE, msg.status())
          .get()
          .left()
          .orElseThrow(() -> new EncoderException("Failed to encode ServerStatus"))
          .getAsJsonObject();

        status.addProperty("preventsChatReports", true);

        writeId(ctx, msg, buf);
        buf.writeUtf(GsonComponentSerializer.gson().serializer().toJson(status));
      }

      private void writeId(final ChannelHandlerContext ctx, final Packet<?> packet, final FriendlyByteBuf buf) {
        buf.writeVarInt(ctx.channel().attr(Connection.ATTRIBUTE_CLIENTBOUND_PROTOCOL).get().packetId(packet));
      }
*/


        /*

    private void encode(final ChannelHandlerContext ctx, final ClientboundPlayerChatPacket msg, final ByteBuf out) {
        final PacketDataSerializer buf = new PacketDataSerializer(out);
        
        final IChatBaseComponent content = Objects.requireNonNullElseGet(msg.g(), () -> {
            return IChatBaseComponent.b(msg.f().a());
        });
        final Optional<?> ctbo = msg.i().a(MinecraftServer.getServer().aZ());

        if (ctbo.isEmpty()) {
            Ostrov.log_warn("Processing packet with unknown ChatType " + msg.h().a());
        } else {
            final IChatBaseComponent decoratedContent = ((a) ctbo.orElseThrow()).a(content);
            final ClientboundSystemChatPacket system = new ClientboundSystemChatPacket(decoratedContent, false);
            writeId(ctx, system, buf);
            system.a(buf);
        }
    }

    private void encode(ChannelHandlerContext ctx, ClientboundServerDataPacket msg, final ByteBuf out) {
        final PacketDataSerializer buf = new PacketDataSerializer(out);
        writeId(ctx, msg, buf);
        buf.a(msg.a());
        buf.a(msg.d(), (PacketDataSerializer packetDataSerializer, byte[] array) -> packetDataSerializer.a(array));
        buf.a(true); //1201 buf.writeBoolean(true);
    }

    private void encode(final ChannelHandlerContext ctx, final PacketStatusOutServerInfo msg, final ByteBuf out) {
        final PacketDataSerializer buf = new PacketDataSerializer(out);
        final JsonObject status = ServerPing.a.encodeStart(JsonOps.INSTANCE, msg.a()).get().left().orElseThrow(() -> {
            return new EncoderException("Failed to encode ServerStatus");
        }).getAsJsonObject();

        status.addProperty("preventsChatReports", true);
        writeId(ctx, msg, buf);
        buf.a(GsonComponentSerializer.gson().serializer().toJson(status));
    }

    private void writeId(final ChannelHandlerContext ctx, final Packet<?> packet, final PacketDataSerializer buf) {
        //1201 buf.d(ctx.channel().attr(NetworkManager.f).get().a(packet));
        buf.c(((net.minecraft.network.EnumProtocol.a) ctx.channel().attr(NetworkManager.f).get()).a(packet));
   }*/
    
}
