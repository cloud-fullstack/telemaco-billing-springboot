package dev.telemaco.billing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.telemaco.billing.outbox.OutboxRepository;
import dev.telemaco.billing.payment.PaymentEvent;
import dev.telemaco.billing.payment.PaymentWebhookService;
import dev.telemaco.billing.payment.ProcessedPaymentEventRepository;
import dev.telemaco.billing.tenant.PaymentRepository;
import dev.telemaco.billing.tenant.Tenant;
import dev.telemaco.billing.tenant.TenantRepository;
import dev.telemaco.billing.tenant.TenantStatus;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentWebhookServiceTest {

    private final ProcessedPaymentEventRepository processed = mock(ProcessedPaymentEventRepository.class);
    private final TenantRepository tenants = mock(TenantRepository.class);
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final OutboxRepository outbox = mock(OutboxRepository.class);
    private final PaymentWebhookService service =
            new PaymentWebhookService(processed, tenants, payments, outbox);

    private final UUID tenantId = UUID.randomUUID();
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = new Tenant(tenantId);
        when(tenants.findById(tenantId)).thenReturn(Optional.of(tenant));
    }

    private PaymentEvent succeeded(String eventId) {
        return new PaymentEvent(eventId, PaymentEvent.Type.PAYMENT_SUCCEEDED, tenantId,
                "silver", new BigDecimal("45.00"), "EUR");
    }

    @Test
    void successfulPaymentActivatesPlanAndWritesOutbox() {
        assertThat(service.handle(succeeded("evt_1"))).isTrue();

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(tenant.getPlanCode()).isEqualTo("silver");
        verify(payments).save(any());
        verify(outbox).save(any());
    }

    @Test
    void duplicateEventIsIgnored() {
        when(processed.existsById("evt_1")).thenReturn(true);

        assertThat(service.handle(succeeded("evt_1"))).isFalse();

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.DEMO);
        verify(payments, never()).save(any());
        verify(outbox, never()).save(any());
    }

    @Test
    void failedPaymentSuspendsTenant() {
        tenant.activate("gold");
        PaymentEvent failed = new PaymentEvent("evt_2", PaymentEvent.Type.PAYMENT_FAILED, tenantId,
                null, null, null);

        service.handle(failed);

        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
        verify(payments, never()).save(any());
    }
}
