package com.zenith.network.server.handler.player.postoutgoing;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.zenith.Proxy;
import com.zenith.cache.DataCache;
import com.zenith.event.proxy.ProxyClientLoggedInEvent;
import com.zenith.network.registry.PostOutgoingPacketHandler;
import com.zenith.network.server.ServerSession;
import com.zenith.util.ComponentSerializer;
import lombok.NonNull;
import net.raphimc.vialoader.netty.VLPipeline;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;

import static com.zenith.Shared.*;

public class LoginPostHandler implements PostOutgoingPacketHandler<ClientboundLoginPacket, ServerSession> {
    @Override
    public void accept(@NonNull ClientboundLoginPacket packet, @NonNull ServerSession session) {
        if (CONFIG.server.extra.whitelist.enable && !session.isWhitelistChecked()) {
            // we shouldn't be able to get to this point without whitelist checking, but just in case
            session.disconnect("Login without whitelist check?");
            return;
        }
        checkDisableServerVia(session);
        session.setLoggedIn(); // allows server packets to start being sent to player
        EVENT_BUS.postAsync(new ProxyClientLoggedInEvent(session));
        DataCache.sendCacheData(CACHE.getAllData(), session);
        session.initializeTeam();
        session.syncTeamMembers();
        var connections = Proxy.getInstance().getActiveConnections().getArray();
        for (int i = 0; i < connections.length; i++) {
            var connection = connections[i];
            if (connection.equals(session)) continue;
            if (connection.hasCameraTarget()) continue;
            session.send(connection.getEntitySpawnPacket());
            session.send(connection.getEntityMetadataPacket());
        }
        if (CONFIG.client.extra.chat.hideChat) {
            session.send(new ClientboundSystemChatPacket(ComponentSerializer.minedown("&7Chat is currently disabled. To enable chat, type &c/togglechat&7."), false));
        }
        if (CONFIG.client.extra.chat.hideWhispers) {
            session.send(new ClientboundSystemChatPacket(ComponentSerializer.minedown("&7Whispers are currently disabled. To enable whispers, type &c/toggleprivatemsgs&7."), false));
        }
        if (CONFIG.client.extra.chat.showConnectionMessages) {
            session.send(new ClientboundSystemChatPacket(ComponentSerializer.minedown("&7Connection messages enabled. To disable, type &c/toggleconnectionmsgs&7."), false));
        }
        if (CONFIG.client.extra.chat.hideDeathMessages) {
            session.send(new ClientboundSystemChatPacket(ComponentSerializer.minedown("&7Death messages are currently disabled. To enable death messages, type &c/toggledeathmsgs&7."), false));
        }
        session.sendAsyncAlert("&aConnected to &r&c" + CACHE.getProfileCache().getProfile().getName());
        if (CONFIG.inGameCommands.enable && !CONFIG.inGameCommands.slashCommands) {
            session.send(new ClientboundSystemChatPacket(ComponentSerializer.minedown("&aCommand Prefix : \"" + CONFIG.inGameCommands.prefix + "\""), false));
            session.send(new ClientboundSystemChatPacket(ComponentSerializer.minedown("&chelp &7- &8List Commands"), false));
        }
    }

    private void checkDisableServerVia(ServerSession session) {
        if (CONFIG.server.viaversion.enabled && CONFIG.server.viaversion.autoRemoveFromPipeline) {
            // the ConnectedClients map is not populated with the connection until ClientboundLoginPacket is sent to the player
            Via.getManager().getConnectionManager().getConnectedClients().values().stream()
                .filter(c -> c.getChannel() == session.getChannel())
                .findAny()
                .filter(c -> c.getProtocolInfo().protocolVersion() == ProtocolVersion.getProtocol(session.getProtocolVersion()))
                .ifPresent(c -> {
                    SERVER_LOG.debug("Disabling ViaVersion for player: {}", session.getProfileCache().getProfile().getName());
                    // remove via codec from channel pipeline
                    c.getChannel().pipeline().remove(VLPipeline.VIA_CODEC_NAME);
                    // dispose via connection state
                    Via.getManager().getConnectionManager().onDisconnect(c);
                });
        }
    }
}
