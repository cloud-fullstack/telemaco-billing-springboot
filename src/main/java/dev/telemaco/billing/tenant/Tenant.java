package dev.telemaco.billing.tenant;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * The plan lives in the database, not in the JWT: a payment or a suspension
 * takes effect on the next request, without waiting for the token to expire.
 */
@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    private UUID id;

    private String planCode = "demo";

    @Enumerated(EnumType.STRING)
    private TenantStatus status = TenantStatus.DEMO;

    protected Tenant() {}

    public Tenant(UUID id) {
        this.id = id;
    }

    /** Only called from the payment webhook flow. */
    public void activate(String planCode) {
        this.planCode = planCode;
        this.status = TenantStatus.ACTIVE;
    }

    public void suspend() {
        this.status = TenantStatus.SUSPENDED;
    }

    public UUID getId() { return id; }
    public String getPlanCode() { return planCode; }
    public TenantStatus getStatus() { return status; }
}
