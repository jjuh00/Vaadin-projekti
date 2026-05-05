package com.prodeca.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogEntryRepository extends JpaRepository<AuditLogEntry, Long> {
    // Haetaan kaikkien entiteettien auditointitietueet (lajiteltu ajan mukaan laskevasssa järjestyksessä)
    Page<AuditLogEntry> findAllByOrderByChangedAtDesc(Pageable pageable);

    // Haetaan kaikki tietueet annetulle entiteettityypille (lajiteltu ajan mukaan laskevasssa järjestyksessä)
    Page<AuditLogEntry> findByEntityTypeOrderByChangedAtDesc(String entityType, Pageable pageable);
}