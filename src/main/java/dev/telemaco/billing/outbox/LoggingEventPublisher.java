package dev.telemaco.billing.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class LoggingEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventPublisher.class);

    @Override
    public void publish(OutboxEvent event) {
        // Only type and aggregate are logged: payloads never go to logs.
        log.info("event published type={} aggregate={}", event.getEventType(), event.getAggregateId());
    }
}
