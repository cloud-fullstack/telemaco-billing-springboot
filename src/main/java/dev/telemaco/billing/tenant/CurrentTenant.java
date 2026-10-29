package dev.telemaco.billing.tenant;

import java.util.UUID;

/** Holds the tenant of the current request (set from the JWT or, in webhooks, from the payment metadata). */
public final class CurrentTenant {

    private static final ThreadLocal<UUID> HOLDER = new ThreadLocal<>();

    private CurrentTenant() {}

    public static void set(UUID tenantId) { HOLDER.set(tenantId); }
    public static UUID get() { return HOLDER.get(); }
    public static void clear() { HOLDER.remove(); }
}
