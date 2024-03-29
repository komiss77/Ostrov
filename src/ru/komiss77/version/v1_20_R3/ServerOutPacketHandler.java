package ru.komiss77.version.v1_20_R3;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.ChatMessageType.a;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.status.PacketStatusOutServerInfo;
import net.minecraft.network.protocol.status.ServerPing;
import net.minecraft.server.MinecraftServer;
import ru.komiss77.Ostrov;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.network.protocol.game.PacketPlayOutOpenSignEditor;

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
            || noChatReports && msg instanceof PacketStatusOutServerInfo
            || claimSecureChatEnforced && msg instanceof ClientboundServerDataPacket;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final Object msg, final ByteBuf out) {

        if (msg instanceof ClientboundPlayerChatPacket p) {
            encode(ctx, p, out);
        } else if (msg instanceof ClientboundServerDataPacket p) {
            encode(ctx, p, out);
        } else if (msg instanceof PacketStatusOutServerInfo p) {
            encode(ctx, p, out);
        } //else if (msg instanceof PacketPlayOutOpenSignEditor p) {
//Ostrov.log("--PacketPlayOutOpenSignEditor ");
//        }
    }

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
   }
    
}