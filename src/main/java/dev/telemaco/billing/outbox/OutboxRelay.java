package dev.telemaco.billing.outbox;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** At-least-once delivery: consumers must be idempotent (they receive the outbox id). */
@Component
public class OutboxRelay {

    private final OutboxRepository outbox;
    private final EventPublisher publisher;

    public OutboxRelay(OutboxRepository outbox, EventPublisher publisher) {
        this.outbox = outbox;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelayString = "${outbox.relay.delay-ms:2000}")
    @Transactional
    public void relay() {
        for (OutboxEvent event : outbox.lockNextBatch()) {
            try {
                publisher.publish(event);
                event.markPublished();
            } catch (RuntimeException e) {
                event.markFailedAttempt();   // retried on the next run, parked after 10 attempts
            }
        }
    }
}
