package com.zenith.network;

import com.zenith.network.server.ServerConnection;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundKeepAlivePacket;

import java.util.concurrent.TimeUnit;

import static com.zenith.Shared.EXECUTOR;

public class KeepAliveTask implements Runnable {
    private final ServerConnection session;

    public KeepAliveTask(ServerConnection session) {
        this.session = session;
    }

    @Override
    public void run() {
        if (this.session.isConnected()) {
            session.setLastPingTime(System.currentTimeMillis());
            session.setLastPingId((int) session.getLastPingTime());
            this.session.sendAsync(new ClientboundKeepAlivePacket(session.getLastPingId()));
            EXECUTOR.schedule(this, 2L, TimeUnit.SECONDS);
        }
    }
}
