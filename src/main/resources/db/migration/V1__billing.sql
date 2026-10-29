CREATE TABLE tenants (
    id         UUID PRIMARY KEY,
    plan_code  VARCHAR(32) NOT NULL DEFAULT 'demo',
    status     VARCHAR(16) NOT NULL DEFAULT 'DEMO'
);

CREATE TABLE payments (
    id                 UUID PRIMARY KEY,
    tenant_id          UUID NOT NULL REFERENCES tenants(id),
    provider_event_id  VARCHAR(255) NOT NULL UNIQUE,
    amount             NUMERIC(12,2) NOT NULL,
    currency           CHAR(3) NOT NULL,
    paid_at            TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_payments_tenant ON payments(tenant_id);

CREATE TABLE processed_payment_events (
    provider_event_id  VARCHAR(255) PRIMARY KEY,
    processed_at       TIMESTAMPTZ NOT NULL
);

CREATE TABLE outbox_events (
    id            BIGSERIAL PRIMARY KEY,
    aggregate_id  VARCHAR(64) NOT NULL,
    event_type    VARCHAR(64) NOT NULL,
    payload       JSONB NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL,
    published_at  TIMESTAMPTZ,
    attempts      INT NOT NULL DEFAULT 0
);
CREATE INDEX ix_outbox_unpublished ON outbox_events(id) WHERE published_at IS NULL;
