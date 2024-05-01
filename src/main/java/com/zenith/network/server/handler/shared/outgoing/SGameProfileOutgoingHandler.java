package com.zenith.network.server.handler.shared.outgoing;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.auth.service.SessionService;
import com.zenith.Proxy;
import com.zenith.event.proxy.NonWhitelistedPlayerConnectedEvent;
import com.zenith.network.registry.PacketHandler;
import com.zenith.network.server.ServerConnection;
import com.zenith.util.Wait;
import lombok.NonNull;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundGameProfilePacket;

import java.util.List;
import java.util.Optional;

import static com.zenith.Shared.*;
import static java.util.Objects.isNull;

public class SGameProfileOutgoingHandler implements PacketHandler<ClientboundGameProfilePacket, ServerConnection> {

    private static List<GameProfile.Property> spectatorProfileProperties = null;

    @Override
    public ClientboundGameProfilePacket apply(@NonNull ClientboundGameProfilePacket packet, @NonNull ServerConnection session) {
        try {
            final GameProfile clientGameProfile = session.getFlag(MinecraftConstants.PROFILE_KEY);
            if (isNull(clientGameProfile)) {
                session.disconnect("Failed to Login");
                return null;
            }
            // this has some bearing on authorization
            // can be set by cookie. or forcefully set if they're only on spectator whitelist
            // true: only spectator -> also set by authorization, overrides any cookie state
            // false: only controlling player
            // empty: no preference, whichever is available
            Optional<Boolean> onlySpectator = Optional.empty();
            if (session.isTransferring()) {
                onlySpectator = session.getCookieCache().getSpectatorCookieValue();
            }
            if (CONFIG.server.extra.whitelist.enable && !PLAYER_LISTS.getWhitelist().contains(clientGameProfile)) {
                if (CONFIG.server.spectator.allowSpectator && PLAYER_LISTS.getSpectatorWhitelist().contains(clientGameProfile)) {
                    onlySpectator = Optional.of(true);
                } else {
                    session.disconnect(CONFIG.server.extra.whitelist.kickmsg);
                    SERVER_LOG.warn("Username: {} UUID: {} [{}] tried to connect!", clientGameProfile.getName(), clientGameProfile.getIdAsString(), session.getRemoteAddress());
                    EVENT_BUS.post(new NonWhitelistedPlayerConnectedEvent(clientGameProfile, session.getRemoteAddress()));
                    return null;
                }
            }
            SERVER_LOG.info("Username: {} UUID: {} [{}] has passed the whitelist check!", clientGameProfile.getName(), clientGameProfile.getIdAsString(), session.getRemoteAddress());
            session.setWhitelistChecked(true);
            synchronized (this) {
                if (!Proxy.getInstance().isConnected()) {
                    if (CONFIG.client.extra.autoConnectOnLogin && !onlySpectator.orElse(false)) {
                        Proxy.getInstance().connect();
                    } else {
                        session.disconnect("Not connected to server!");
                    }
                }
            }
            if (!Wait.waitUntil(() -> {
                var client = Proxy.getInstance().getClient();
                return client != null
                    && CACHE.getProfileCache().getProfile() != null
                    && (client.isOnline()
                        || (client.isInQueue() && Proxy.getInstance().getQueuePosition() > 1));
            }, 15)) {
                SERVER_LOG.info("Timed out waiting for the proxy to login");
                session.disconnect("Timed out waiting for the proxy to login");
                return null;
            }
            SERVER_LOG.debug("User UUID: {}\nBot UUID: {}", clientGameProfile.getId().toString(), CACHE.getProfileCache().getProfile().getId().toString());
            session.getProfileCache().setProfile(clientGameProfile);
            if (!onlySpectator.orElse(false) && Proxy.getInstance().getCurrentPlayer().compareAndSet(null, session)) {
                return new ClientboundGameProfilePacket(CACHE.getProfileCache().getProfile(), false);
            }
            if (onlySpectator.isPresent() && !onlySpectator.get()) { // the above operation failed and we don't want to be put into spectator
                session.disconnect("Someone is already controlling the player");
                return null;
            }
            if (!CONFIG.server.spectator.allowSpectator) {
                session.disconnect("Spectator mode is disabled");
                return null;
            }
            SERVER_LOG.info("Logging in {} [{}] as spectator", clientGameProfile.getName(), clientGameProfile.getId().toString());
            session.setSpectator(true);
            final GameProfile spectatorFakeProfile = new GameProfile(CONFIG.server.spectator.spectatorUUID,
                                                                     clientGameProfile.getName());
            // caching assumes the spectatorUUID is immutable
            if (spectatorProfileProperties == null) {
                SessionService sessionService = new SessionService();
                sessionService.fillProfileProperties(spectatorFakeProfile);
                spectatorProfileProperties = spectatorFakeProfile.getProperties();
            } else {
                spectatorFakeProfile.setProperties(spectatorProfileProperties);
            }
            session.getSpectatorFakeProfileCache().setProfile(spectatorFakeProfile);
            return new ClientboundGameProfilePacket(spectatorFakeProfile, false);
        } catch (final Throwable e) {
            session.disconnect("Login Failed", e);
            return null;
        }
    }
}
