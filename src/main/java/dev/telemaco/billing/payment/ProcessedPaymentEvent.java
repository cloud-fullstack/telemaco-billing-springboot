package dev.telemaco.billing.payment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "processed_payment_events")
public class ProcessedPaymentEvent {

    @Id
    private String providerEventId;
    private Instant processedAt = Instant.now();

    protected ProcessedPaymentEvent() {}

    public ProcessedPaymentEvent(String providerEventId) {
        this.providerEventId = providerEventId;
    }

    public String getProviderEventId() {
        return providerEventId;
    }
}
