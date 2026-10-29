package dev.telemaco.billing.web;

import dev.telemaco.billing.payment.InvalidSignatureException;
import dev.telemaco.billing.payment.PaymentProvider;
import dev.telemaco.billing.payment.PaymentWebhookService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentWebhookController {

    private final Map<String, PaymentProvider> providers;
    private final PaymentWebhookService service;

    public PaymentWebhookController(List<PaymentProvider> providers, PaymentWebhookService service) {
        this.providers = providers.stream()
                .collect(java.util.stream.Collectors.toMap(PaymentProvider::id, p -> p));
        this.service = service;
    }

    /** The raw body is required to verify the provider signature. */
    @PostMapping("/webhooks/payments/{provider}")
    public ResponseEntity<Void> receive(@PathVariable String provider,
                                        @RequestBody String rawBody,
                                        @RequestHeader Map<String, String> headers) {
        PaymentProvider adapter = providers.get(provider);
        if (adapter == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            service.handle(adapter.parseWebhook(rawBody, headers));
            return ResponseEntity.ok().build();   // 200 also for duplicates, so the provider stops retrying
        } catch (InvalidSignatureException e) {
            return ResponseEntity.status(401).build();
        }
    }
}
