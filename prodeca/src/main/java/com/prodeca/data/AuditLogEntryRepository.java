package com.prodeca.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogEntryRepository extends JpaRepository<AuditLogEntry, Long> {
    // Haetaan kaikki auditointitietueet tietylle entiteetille (lajiteltu ajan mukaan laskevasssa järjestyksessä)
    List<AuditLogEntry> findByEntityTypeAndEntityIdOrderByChangedAtDesc(
        String entityType, Long entityId
    );

    // Haetaan kaikkien entiteettien auditointitietueet (lajiteltu ajan mukaan laskevasssa järjestyksessä)
    Page<AuditLogEntry> findAllByOrderByChangedAtDesc(Pageable pageable);

    // Haetaan kaikki tietueet annetulle entiteettityypille (lajiteltu ajan mukaan laskevasssa järjestyksessä)
    Page<AuditLogEntry> findByEntityTypeOrderByChangedAtDesc(String entityType, Pageable pageable);
}