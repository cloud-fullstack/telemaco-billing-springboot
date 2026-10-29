# Billing service – Spring Boot showcase

Extract of the account & billing service of **Telemaco**, a multi-tenant conversational AI platform
(voice and text assistants that book appointments, rooms and vehicles for small businesses).

This repository is a **code sample for portfolio review**: it shows the patterns used in the real
service, with simplified domain code. Provider adapters, pricing and business rules are not included.

## What it shows

| Pattern | Where |
|---|---|
| **Payment provider behind a port** – switching provider touches only an adapter | `payment/PaymentProvider.java` |
| **Webhook processing in one transaction** – idempotency record, payment, plan activation and outbox event commit together | `payment/PaymentWebhookService.java` |
| **Idempotent webhooks** – provider retries are safe | `processed_payment_events` table |
| **Transactional outbox + relay** – at-least-once delivery, `FOR UPDATE SKIP LOCKED` for multiple replicas | `outbox/` |
| **Tenant isolation at ORM level** with Hibernate 6 `@TenantId` | `tenant/Payment.java`, `TenantIdentifierResolver.java` |
| **Plan read from the database, never from the browser or the JWT** | `tenant/Tenant.java` |
| Flyway migrations, unit tests with Mockito | `db/migration`, `src/test` |

## Flow

```mermaid
sequenceDiagram
    participant P as Payment provider
    participant C as WebhookController
    participant S as WebhookService
    participant DB as PostgreSQL
    participant R as OutboxRelay
    P->>C: POST /webhooks/payments/{provider}
    C->>C: verify signature (adapter)
    C->>S: PaymentEvent
    S->>DB: BEGIN
    S->>DB: insert processed_payment_events (idempotency)
    S->>DB: insert payment, activate tenant plan
    S->>DB: insert outbox_events
    S->>DB: COMMIT
    R->>DB: lock unpublished events (SKIP LOCKED)
    R-->>R: publish (email, widget unlock, commissions)
```

## Run the tests

```bash
mvn test
```

Stack: Java 21, Spring Boot 3, Spring Data JPA / Hibernate 6, PostgreSQL, Flyway, JUnit 5, Mockito.
