package ru.komiss77.version.v1_20_R1;

import java.util.Objects;
import java.util.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.EnumProtocol;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.ChatMessageType.a;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.status.PacketStatusOutServerInfo;
import net.minecraft.network.protocol.status.ServerPing;
import net.minecraft.server.MinecraftServer;
import ru.komiss77.Ostrov;

//https://github.com/e-im/FreedomChat/blob/main/src/main/java/ru/bk/oharass/freedomchat/FreedomHandler.java
//https://github.com/e-im/FreedomChat
//https://www.libhunt.com/r/FreedomChat

//декомпилить плагин!

@Sharable
public class ChatHandler extends MessageToByteEncoder<Object> {

    private final boolean rewriteChat = true;
    private final boolean claimSecureChatEnforced = true;
    private final boolean noChatReports = true;
    
    @Override
    public boolean acceptOutboundMessage(Object msg) {
        return rewriteChat && msg instanceof ClientboundPlayerChatPacket
            || noChatReports && msg instanceof PacketStatusOutServerInfo
            || claimSecureChatEnforced && msg instanceof ClientboundServerDataPacket;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final Object msg, final ByteBuf out) {

        final PacketDataSerializer fbb = new PacketDataSerializer(out);

        if (msg instanceof ClientboundPlayerChatPacket) {
            encode(ctx, (ClientboundPlayerChatPacket)msg, fbb);
        } else if (msg instanceof ClientboundServerDataPacket) {
            encode(ctx, (ClientboundServerDataPacket)msg, fbb);
        } else if (msg instanceof PacketStatusOutServerInfo) {
            encode(ctx, (PacketStatusOutServerInfo)msg, fbb);
        }
    }

    private void encode(final ChannelHandlerContext ctx, final ClientboundPlayerChatPacket msg, final PacketDataSerializer buf) {

        final IChatBaseComponent content = (IChatBaseComponent) Objects.requireNonNullElseGet(msg.f(), () -> {
            return IChatBaseComponent.b(msg.e().a());
        });
        final Optional<?> ctbo = msg.h().a(MinecraftServer.getServer().aV());

        if (ctbo.isEmpty()) {
            Ostrov.log_warn("Processing packet with unknown ChatType " + msg.h().a());
        } else {
            final IChatBaseComponent decoratedContent = ((a) ctbo.orElseThrow()).a(content);
            final ClientboundSystemChatPacket system = new ClientboundSystemChatPacket(decoratedContent, false);
            writeId(ctx, system, buf);
            system.a(buf);
        }
    }

    private void encode(ChannelHandlerContext ctx, ClientboundServerDataPacket msg, PacketDataSerializer buf) {
        writeId(ctx, msg, buf);
        buf.a(msg.a());
        buf.a(msg.c(), (PacketDataSerializer packetDataSerializer, byte[] array) -> packetDataSerializer.a(array));
        buf.writeBoolean(true);
    }

    private void encode(final ChannelHandlerContext ctx, final PacketStatusOutServerInfo msg, final PacketDataSerializer buf) {
        final JsonObject status = ((JsonElement) ServerPing.a.encodeStart(JsonOps.INSTANCE, msg.a()).get().left().orElseThrow(() -> {
            return new EncoderException("Failed to encode ServerStatus");
        })).getAsJsonObject();

        status.addProperty("preventsChatReports", true);
        writeId(ctx, msg, buf);
        buf.a(GsonComponentSerializer.gson().serializer().toJson(status));
    }

    private void writeId(final ChannelHandlerContext ctx, final Packet<?> packet, final PacketDataSerializer buf) {
        buf.d(((EnumProtocol) ctx.channel().attr(NetworkManager.e).get()).a(EnumProtocolDirection.b, packet));
    }
    
} 
    
    
