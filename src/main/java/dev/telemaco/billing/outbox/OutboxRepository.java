package dev.telemaco.billing.outbox;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    /** SKIP LOCKED lets several relay replicas run without picking the same rows. */
    @Query(value = """
            SELECT * FROM outbox_events
            WHERE published_at IS NULL AND attempts < 10
            ORDER BY id
            LIMIT 50
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> lockNextBatch();
}
