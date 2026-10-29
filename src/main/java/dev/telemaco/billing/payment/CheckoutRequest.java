package dev.telemaco.billing.payment;

import java.util.UUID;

/** The price is resolved server-side from the plan catalogue, never sent by the browser. */
public record CheckoutRequest(UUID tenantId, String planCode, String providerPriceId, String customerEmail) {}
