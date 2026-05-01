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
        return this.repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<SupplierContact> getWithPageable(Pageable pageable) {
        return this.repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public int count() {
        return (int) this.repository.count();
    }

    // Palautetaan toimittajaan liitetyn yhteyshenkilön koko nimi tai tyhjä merkkijono,
    // jos yhteystietoja ei löydy
    @Transactional(readOnly = true)
    public String getContactFullName(Supplier supplier) {
        Optional<SupplierContact> contact = this.repository.findBySupplier(supplier);
        return contact.isPresent() ? contact.get().getFullName() : "";
    }

    // Palautetaan toimittajan yhteyshenkilö
    @Transactional(readOnly = true)
    public Optional<SupplierContact> getBySupplier(Supplier supplier) {
        return this.repository.findBySupplier(supplier);
    }

    @Transactional
    public SupplierContact save(SupplierContact contact) {
        if (contact.getId() == null && contact.getSupplier() != null) {
            Optional<SupplierContact> existing = this.repository.findBySupplier(contact.getSupplier());
            if (existing.isPresent()) {
                // Jos yhteystiedot löytyvät, päivitetään ne ja palautetaan vanhaa id:tä käyttäen
                SupplierContact toUpdate = existing.get();
                toUpdate.setFirstName(contact.getFirstName());
                toUpdate.setLastName(contact.getLastName());
                toUpdate.setEmail(contact.getEmail());
                toUpdate.setPhone(contact.getPhone());
                toUpdate.setJobTitle(contact.getJobTitle());
                toUpdate.setNotes(contact.getNotes());
                return this.repository.save(toUpdate);
            }
        }
        return this.repository.save(contact);
    }

    @Transactional
    public void delete(Long id) {
        this.repository.deleteById(id);
    }
}