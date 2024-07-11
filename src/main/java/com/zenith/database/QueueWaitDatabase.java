package com.zenith.database;

import com.zenith.event.proxy.QueueCompleteEvent;
import com.zenith.event.proxy.QueuePositionUpdateEvent;
import com.zenith.event.proxy.ServerRestartingEvent;
import com.zenith.event.proxy.StartQueueEvent;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static com.github.rfresh2.EventConsumer.of;
import static com.zenith.Shared.CONFIG;
import static com.zenith.Shared.EVENT_BUS;
import static java.util.Objects.nonNull;

public class QueueWaitDatabase extends Database {

    private volatile Integer lastQueuePos = null;
    private volatile Instant lastQueuePosTime = null;
    private Instant lastServerRestart = Instant.EPOCH;

    public QueueWaitDatabase(QueryExecutor queryExecutor) {
        super(queryExecutor);
    }

    @Override
    public void subscribeEvents() {
        EVENT_BUS.subscribe(this,
                            of(ServerRestartingEvent.class, this::handleServerRestart),
                            of(StartQueueEvent.class, this::handleStartQueue),
                            of(QueuePositionUpdateEvent.class, this::handleQueuePosition),
                            of(QueueCompleteEvent.class, this::handleQueueComplete)
        );
    }

    public void handleServerRestart(final ServerRestartingEvent event) {
        lastServerRestart = Instant.now();
    }

    public void handleStartQueue(final StartQueueEvent event) {
        lastQueuePos = null;
        lastQueuePosTime = null;
    }

    public void handleQueuePosition(final QueuePositionUpdateEvent event) {
        var time = Instant.now();
        var newQueuePos = event.position();
        if (lastQueuePos == null) {
            lastQueuePos = newQueuePos;
            lastQueuePosTime = time;
            return;
        }

        if (newQueuePos > lastQueuePos) { // shouldn't happen, but better safe than sorry
            return;
        }

        while (lastQueuePos != newQueuePos + 1) {} // again, shouldn't happen, but on the off chance these arrive out of order?

        writeQueueWait(lastQueuePos, lastQueuePosTime, time, lastServerRestart);

        lastQueuePos = newQueuePos;
        lastQueuePosTime = time;
    }

    public void handleQueueComplete(final QueueCompleteEvent event) {
        handleQueuePosition(new QueuePositionUpdateEvent(0));
    }

    private void writeQueueWait(int queuePos, Instant queuePosStartTime, Instant queuePosEndTime, Instant lastServerRestart) {
        try (var handle = this.queryExecutor.jdbi().open()) {
            handle.createUpdate("INSERT INTO queuewait (player_name, prio, queue_pos, start_pos_time, end_pos_time, last_server_restart) VALUES (:player_name, :prio, :queue_pos, :start_pos_time, :end_pos_time, :last_server_restart)")
                    .bind("player_name", CONFIG.authentication.username)
                    .bind("prio", CONFIG.authentication.prio)
                    .bind("queue_pos", queuePos)
                    .bind("start_pos_time", queuePosStartTime.atOffset(ZoneOffset.UTC))
                    .bind("end_pos_time", queuePosEndTime.atOffset(ZoneOffset.UTC))
                    .bind("last_server_restart", lastServerRestart.atOffset(ZoneOffset.UTC))
                    .execute();
        }
    }
}
