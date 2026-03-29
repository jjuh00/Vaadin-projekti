package com.prodeca.services;

import com.prodeca.data.Supplier;
import com.prodeca.data.SupplierContact;
import com.prodeca.data.SupplierContactRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SupplierContactService {
    
    private final SupplierContactRepository repository;

    public SupplierContactService(SupplierContactRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<SupplierContact> getById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<SupplierContact> getWithPageable(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // Sivutettu lista valinnaisella Specifition-parametrillä Criteria API-suodatusta varten
    @Transactional(readOnly = true)
    public Page<SupplierContact> getWithSpec(Pageable pageable, org.springframework.data.jpa.domain.Specification<SupplierContact> filter) {
        return repository.findAll(filter, pageable);
    }

    @Transactional(readOnly = true)
    public int count() {
        return (int) repository.count();
    }

    // Palauttaa ture, jos annettulla toimittajallla on jo yhteystiedot, muuten false
    @Transactional(readOnly = true)
    public boolean supplierHasContactInfo(Supplier supplier) {
        return repository.existsBySupplier(supplier);
    }

    @Transactional
    public SupplierContact save(SupplierContact contact) {
        if (contact.getId() == null && contact.getSupplier() != null) {
            Optional<SupplierContact> existing = repository.findBySupplier(contact.getSupplier());
            if (existing.isPresent()) {
                // Jos yhteystiedot löytyvät, päivitetään ne ja palautetaan vanhaa id:tä käyttäen
                SupplierContact toUpdate = existing.get();
                toUpdate.setFirstName(contact.getFirstName());
                toUpdate.setLastName(contact.getLastName());
                toUpdate.setEmail(contact.getEmail());
                toUpdate.setPhone(contact.getPhone());
                toUpdate.setJobTitle(contact.getJobTitle());
                toUpdate.setNotes(contact.getNotes());
                return repository.save(toUpdate);
            }
        }
        return repository.save(contact);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}