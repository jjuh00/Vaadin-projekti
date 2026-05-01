package com.prodeca.data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log_entry", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_timestamp", columnList = "changed_at")
})
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_idgenerator")
    @SequenceGenerator(name = "audit_idgenerator", initialValue = 1000)
    private Long id;

    // Auditoitavan entiteetin tyyppi (esim. "Product", "Supplier")
    @Column(nullable = false, length = 50)
    private String entityType;

    // Muokattavan entiteetin ID
    @Column(nullable = false)
    private Long entityId;

    // Luettava teksti (esim. tuotteen tai toimittajan nimi)
    @Column(nullable = false, length = 200)
    private String entityLabel;

    // Muutosten tyyppi (CREATE, UPDATE, DELETE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AuditAction action;

    // Käyttäjänimi, joka teki muutoksen
    @Column(length = 80)
    private String changedBy;

    // Muutosten aikaleima
    @Column(nullable = false)
    private LocalDateTime changedAt;

    // Vaihtoehtoiset JSON-tiedot muutoksista
    @Column(columnDefinition = "TEXT")
    private String details;

    // Oletusalustin
    public AuditLogEntry() { }

    // Parametrillinen alustin
    public AuditLogEntry(String entityType, Long entityId, String entityLabel,
                        AuditAction action, String changedBy, String details) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityLabel = entityLabel;
        this.action = action;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
        this.details = details;                    
    }

    // Getterit
    public Long getId() {
        return id;
    }
    public String getEntityType() {
        return entityType;
    }
    public Long getEntityId() {
        return entityId;
    }
    public String getEntityLabel() {
        return entityLabel;
    }
    public AuditAction getAction() {
        return action;
    }
    public String getChangedBy() {
        return changedBy;
    }
    public LocalDateTime getChangedAt() {
        return changedAt;
    }
    public String getDetails() {
        return details;
    }
}