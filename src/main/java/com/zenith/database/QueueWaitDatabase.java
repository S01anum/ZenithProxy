package com.zenith.database;

import com.zenith.event.proxy.QueueCompleteEvent;
import com.zenith.event.proxy.QueuePositionUpdateEvent;
import com.zenith.event.proxy.ServerRestartingEvent;
import com.zenith.event.proxy.StartQueueEvent;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.rfresh2.EventConsumer.of;
import static com.zenith.Shared.*;
import static java.util.Objects.nonNull;

public class QueueWaitDatabase extends Database {

    private AtomicInteger lastQueuePos = new AtomicInteger(-1);
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
        lastQueuePos.set(-1);
        lastQueuePosTime = null;
    }

    public void handleQueuePosition(final QueuePositionUpdateEvent event) {
        var time = Instant.now();
        var newQueuePos = event.position();
        var oldQueuePos = lastQueuePos.get();

        if (oldQueuePos == -1) {
            if (lastQueuePos.compareAndSet(oldQueuePos, newQueuePos)) {
                lastQueuePosTime = time;
                return;
            }
            oldQueuePos = lastQueuePos.get();
        }

        while (true) {
            if (newQueuePos == oldQueuePos) { // shouldn't happen, but better safe than sorry
                DATABASE_LOG.warn("Got duplicate queue pos update {}?", newQueuePos);
                return;
            }

            if (newQueuePos > oldQueuePos) { // shouldn't happen, but better safe than sorry
                DATABASE_LOG.warn("Got out of order queue pos update {}?", newQueuePos);
                return;
            }

            if (lastQueuePos.compareAndSet(oldQueuePos, newQueuePos)) {
                writeQueueWait(oldQueuePos++, lastQueuePosTime, time, lastServerRestart);
                lastQueuePosTime = time;
                while (oldQueuePos != newQueuePos) { // We skipped a queue position
                    writeQueueWait(oldQueuePos++, time, time, lastServerRestart);
                }
                return;
            }
            oldQueuePos = lastQueuePos.get();
        }
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
