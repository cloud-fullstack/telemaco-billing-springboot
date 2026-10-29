package dev.telemaco.billing.payment;

import dev.telemaco.billing.outbox.OutboxEvent;
import dev.telemaco.billing.outbox.OutboxRepository;
import dev.telemaco.billing.tenant.CurrentTenant;
import dev.telemaco.billing.tenant.Payment;
import dev.telemaco.billing.tenant.PaymentRepository;
import dev.telemaco.billing.tenant.Tenant;
import dev.telemaco.billing.tenant.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The only place where a paid plan is activated.
 * Idempotency record, payment, plan change and outbox event are written in ONE transaction:
 * either all of them happen or none does.
 */
@Service
public class PaymentWebhookService {

    private final ProcessedPaymentEventRepository processed;
    private final TenantRepository tenants;
    private final PaymentRepository payments;
    private final OutboxRepository outbox;

    public PaymentWebhookService(ProcessedPaymentEventRepository processed, TenantRepository tenants,
                                 PaymentRepository payments, OutboxRepository outbox) {
        this.processed = processed;
        this.tenants = tenants;
        this.payments = payments;
        this.outbox = outbox;
    }

    /** @return false if the event was already processed (provider retries are expected). */
    @Transactional
    public boolean handle(PaymentEvent event) {
        if (event.type() == PaymentEvent.Type.IGNORED) {
            return false;
        }
        if (processed.existsById(event.providerEventId())) {
            return false;
        }
        processed.save(new ProcessedPaymentEvent(event.providerEventId()));

        Tenant tenant = tenants.findById(event.tenantId())
                .orElseThrow(() -> new IllegalStateException("unknown tenant"));

        CurrentTenant.set(tenant.getId());
        try {
            switch (event.type()) {
                case PAYMENT_SUCCEEDED -> {
                    payments.save(new Payment(event.providerEventId(), event.amount(), event.currency()));
                    tenant.activate(event.planCode());
                    outbox.save(new OutboxEvent(tenant.getId().toString(), "SubscriptionActivated",
                            "{\"plan\":\"" + event.planCode() + "\"}"));
                }
                case PAYMENT_FAILED, SUBSCRIPTION_CANCELLED -> {
                    tenant.suspend();
                    outbox.save(new OutboxEvent(tenant.getId().toString(), "SubscriptionSuspended", "{}"));
                }
                default -> { }
            }
        } finally {
            CurrentTenant.clear();
        }
        return true;
    }
}
