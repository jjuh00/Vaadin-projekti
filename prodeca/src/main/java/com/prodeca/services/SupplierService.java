package com.prodeca.services;

import com.prodeca.data.Supplier;
import com.prodeca.data.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {
    
    private final SupplierRepository repository;
    private final AuditLogService auditLogService;

    public SupplierService(SupplierRepository repository, AuditLogService auditLogService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Optional<Supplier> getById(Long id) {
        return this.repository.findById(id);
    }

    // Palautetaan kaikki toimittajat
    @Transactional(readOnly = true)
    public List<Supplier> getAll() {
        return this.repository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Supplier> getWithPageable(Pageable pageable) {
        return this.repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public int count() {
        return (int) this.repository.count();
    }

    // Tallennetaan toimittaja ja luodaan auditointiloki
    @Transactional
    public Supplier save(Supplier supplier) {
        boolean isNew = (supplier.getId() == null);
        Supplier saved = this.repository.save(supplier);

        String details = "Sähköposti: " + saved.getEmail() + ", Puhelin: " + saved.getPhone() +
                         ", Maa: " + saved.getCountry() + ", Rekisteröintinumero: " + saved.getRegistrationNumber();
        if (isNew) {
            this.auditLogService.logCreate("Supplier", saved.getId(), saved.getName(), details);
        } else {
            this.auditLogService.logUpdate("Supplier", saved.getId(), saved.getName(), details);
        }

        return saved;
    }

    // Poistetaan toimittaja ja luodaan auditointiloki
    @Transactional
    public void delete(Long id) {
        this.repository.findById(id).ifPresent(s ->
            this.auditLogService.logDelete("Supplier", id, s.getName())
        );
        this.repository.deleteById(id);
    }
}