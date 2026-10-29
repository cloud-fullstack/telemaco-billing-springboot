package dev.telemaco.billing.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentEvent(
        String providerEventId,   // used for idempotency
        Type type,
        UUID tenantId,
        String planCode,
        BigDecimal amount,
        String currency) {

    public enum Type { PAYMENT_SUCCEEDED, PAYMENT_FAILED, SUBSCRIPTION_CANCELLED, IGNORED }
}
