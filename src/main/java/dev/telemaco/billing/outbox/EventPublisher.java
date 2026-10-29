package dev.telemaco.billing.outbox;

/** Destination of outbox events: in-process today, a message queue when consumers grow. */
public interface EventPublisher {
    void publish(OutboxEvent event);
}
