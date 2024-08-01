package ru.komiss77.version;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;

//https://github.com/e-im/FreedomChat/blob/main/src/main/java/ru/bk/oharass/freedomchat/FreedomHandler.java
//https://github.com/e-im/FreedomChat
//https://www.libhunt.com/r/FreedomChat

//декомпилить плагин!

@Sharable
public class ServerOutPacketHandler extends MessageToByteEncoder<Packet<?>> {

    private final boolean rewriteChat = true;
    private final boolean claimSecureChatEnforced = true;
    private final boolean noChatReports = true;
    private static final int STATUS_RESPONSE_PACKET_ID = 0x00;
    private final StreamCodec<ByteBuf, Packet<? super ClientGamePacketListener>> s2cPlayPacketCodec;

    public ServerOutPacketHandler() {
        final RegistryAccess registryAccess = MinecraftServer.getServer().registryAccess();
        final Function<ByteBuf, RegistryFriendlyByteBuf> bufRegistryAccess = RegistryFriendlyByteBuf.decorator(registryAccess);
        this.s2cPlayPacketCodec = GameProtocols.CLIENTBOUND_TEMPLATE.bind(bufRegistryAccess).codec();
    }


    @Override
    public boolean acceptOutboundMessage(final Object msg) {
        return rewriteChat && msg instanceof ClientboundPlayerChatPacket
            || noChatReports && msg instanceof ClientboundStatusResponsePacket
            || claimSecureChatEnforced && msg instanceof ClientboundLoginPacket;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, final Packet msg, final ByteBuf out) {
        final FriendlyByteBuf fbb = new FriendlyByteBuf(out);

        if (msg instanceof final ClientboundPlayerChatPacket packet) {
            encode(ctx, packet, fbb);
        } else if (msg instanceof final ClientboundStatusResponsePacket packet) {
            encode(ctx, packet, fbb);
        } else if (msg instanceof final ClientboundLoginPacket packet) {
            encode(ctx, packet, fbb);
        }
    }

    private void encode(@SuppressWarnings("unused") final ChannelHandlerContext ctx, final ClientboundPlayerChatPacket msg, final FriendlyByteBuf buf) {
        final Component content = Objects.requireNonNullElseGet(msg.unsignedContent(), () -> Component.literal(msg.body().content()));

        final ChatType.Bound chatType = msg.chatType();
        final Component decoratedContent = chatType.decorate(content);

        final ClientboundSystemChatPacket system = new ClientboundSystemChatPacket(decoratedContent, false);

        s2cPlayPacketCodec.encode(buf, system);
    }

    private void encode(@SuppressWarnings("unused") final ChannelHandlerContext ctx, final ClientboundLoginPacket msg, final FriendlyByteBuf buf) {
        final ClientboundLoginPacket rewritten = new ClientboundLoginPacket(
            msg.playerId(),
            msg.hardcore(),
            msg.levels(),
            msg.maxPlayers(),
            msg.chunkRadius(),
            msg.simulationDistance(),
            msg.reducedDebugInfo(),
            msg.showDeathScreen(),
            msg.doLimitedCrafting(),
            msg.commonPlayerSpawnInfo(),
            true // Enforced secure chat
        );
        s2cPlayPacketCodec.encode(buf, rewritten);
    }

    private void encode(@SuppressWarnings("unused") final ChannelHandlerContext ctx, final ClientboundStatusResponsePacket msg, final FriendlyByteBuf buf) {
        final ServerStatus status = msg.status();

        final CustomServerMetadata customStatus = new CustomServerMetadata(
            status.description(),
            status.players(),
            status.version(),
            status.favicon(),
            status.enforcesSecureChat(),
            true
        );

        buf.writeVarInt(STATUS_RESPONSE_PACKET_ID);
        buf.writeJsonWithCodec(CustomServerMetadata.CODEC, customStatus);
    }






/*
    @Override
    public boolean acceptOutboundMessage(Object msg) {
        return rewriteChat && msg instanceof ClientboundPlayerChatPacket
                || noChatReports && msg instanceof ClientboundStatusResponsePacket
                || claimSecureChatEnforced && msg instanceof ClientboundServerDataPacket;
    }

    @Override
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
*/


}

record CustomServerMetadata(Component description, Optional<ServerStatus.Players> players,
                            Optional<ServerStatus.Version> version, Optional<ServerStatus.Favicon> favicon,
                            boolean enforcesSecureChat, boolean preventsChatReports) {
    public static final Codec<CustomServerMetadata> CODEC = RecordCodecBuilder
        .create((instance) -> instance.group(
                ComponentSerialization.CODEC.lenientOptionalFieldOf("description", CommonComponents.EMPTY)
                    .forGetter(CustomServerMetadata::description),
                ServerStatus.Players.CODEC.lenientOptionalFieldOf("players")
                    .forGetter(CustomServerMetadata::players),
                ServerStatus.Version.CODEC.lenientOptionalFieldOf("version")
                    .forGetter(CustomServerMetadata::version),
                ServerStatus.Favicon.CODEC.lenientOptionalFieldOf("favicon")
                    .forGetter(CustomServerMetadata::favicon),
                Codec.BOOL.lenientOptionalFieldOf("enforcesSecureChat", false)
                    .forGetter(CustomServerMetadata::enforcesSecureChat),
                Codec.BOOL.lenientOptionalFieldOf("preventsChatReports", false)
                    .forGetter(CustomServerMetadata::preventsChatReports))
            .apply(instance, CustomServerMetadata::new));

    public Component description() {
        return this.description;
    }

    public Optional<ServerStatus.Players> players() {
        return this.players;
    }

    public Optional<ServerStatus.Version> version() {
        return this.version;
    }

    public Optional<ServerStatus.Favicon> favicon() {
        return this.favicon;
    }

    public boolean enforcesSecureChat() {
        return this.enforcesSecureChat;
    }

    public boolean preventsChatReports() {
        return this.preventsChatReports;
    }
}