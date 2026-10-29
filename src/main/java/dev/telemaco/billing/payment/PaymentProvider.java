package dev.telemaco.billing.payment;

import java.util.Map;

/**
 * Port to an external payment provider (hosted checkout only, no card data on the platform).
 * Each provider adapter translates its own webhook format into a common {@link PaymentEvent},
 * so switching provider touches only the adapter, never the billing logic.
 */
public interface PaymentProvider {

    /** Identifier stored on each subscription, e.g. "provider-a". */
    String id();

    /** Creates a hosted checkout session and returns the URL the user is redirected to. */
    String createCheckout(CheckoutRequest request);

    /**
     * Verifies the webhook signature and parses the payload.
     *
     * @throws InvalidSignatureException if the signature does not match
     */
    PaymentEvent parseWebhook(String rawBody, Map<String, String> headers);
}
