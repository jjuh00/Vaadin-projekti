package com.prodeca.services;

import com.prodeca.data.AuditAction;
import com.prodeca.data.AuditLogEntry;
import com.prodeca.data.AuditLogEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogEntryRepository repository;

    public AuditLogService(AuditLogEntryRepository repository) {
        this.repository = repository;
    }

    // Palautetaan nykyisen käyttäjän nimi, tai "system" jos käyttäjä ei ole kirjautunut
    private String currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName()))
            ? authentication.getName()
            : "system";
    }

    // Tallennetaan luontitapahtuma auditointilokiin (käytetään uuden entiteetin luomisen yhteydessä)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCreate(String entityType, Long entityId, String label, String details) {
        this.repository.save(new AuditLogEntry(
            entityType, entityId, label,
            AuditAction.CREATE, currentUser(), details
        ));
    }

    // Tallennetaan päivitystapahtuma auditointilokiin (käytetään entiteetin muokkaamisen yhteydessä)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUpdate(String entityType, Long entityId, String label, String details) {
        this.repository.save(new AuditLogEntry(
            entityType, entityId, label,
            AuditAction.UPDATE, currentUser(), details
        ));
    }

    // Tallennetaan poistotapahtuma auditointilokiin (käytetään entiteetin poistamisen yhteydessä)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logDelete(String entityType, Long entityId, String label) {
        this.repository.save(new AuditLogEntry(
            entityType, entityId, label,
            AuditAction.DELETE, currentUser(), null
        ));
    }

    // Haetaan kaikki auditointitietueet sivutettuna
    @Transactional(readOnly = true)
    public Page<AuditLogEntry> getWithPageable(Pageable pageable) {
        return this.repository.findAllByOrderByChangedAtDesc(pageable);
    }

    // Haetaan ja suodatetaan entiteettityypin mukaan
    @Transactional(readOnly = true)
    public Page<AuditLogEntry> getByEntityType(String entityType, Pageable pageable) {
        return this.repository.findByEntityTypeOrderByChangedAtDesc(entityType, pageable);
    }
}