package dev.telemaco.billing.tenant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

/**
 * Tenant-owned table. With {@code @TenantId}, Hibernate adds the tenant filter to every query
 * and fills the column on insert, so one tenant can never read another tenant's rows.
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @TenantId
    private UUID tenantId;

    private String providerEventId;
    private BigDecimal amount;
    private String currency;
    private Instant paidAt = Instant.now();

    protected Payment() {}

    public Payment(String providerEventId, BigDecimal amount, String currency) {
        this.providerEventId = providerEventId;
        this.amount = amount;
        this.currency = currency;
    }

    public UUID getTenantId() { return tenantId; }
    public BigDecimal getAmount() { return amount; }
}
