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
    
    private final AuditLogService auditLogService;
    private final SupplierContactRepository repository;

    public SupplierContactService(SupplierContactRepository repository, AuditLogService auditLogService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
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

    // Tallennetaan yhteyshenkikl ja luodaan auditointiloki
    @Transactional
    public SupplierContact save(SupplierContact contact) {
        /* Erityistapaus: yhteyshenkilölle ei ole vielä ID:tä, mutta toimittajalla on jo olemassa oleva
           yhteyshenkilö tietokannassa. Tällöin päivitetään olemassa olevaa
           yhteyshenkilöä sen sijaan, että luotaisiin uusi */
        if (contact.getId() == null && contact.getSupplier() != null) {
            Optional<SupplierContact> existing = this.repository.findBySupplier(contact.getSupplier());
            if (existing.isPresent()) {
                SupplierContact toUpdate = existing.get();
                toUpdate.setFirstName(contact.getFirstName());
                toUpdate.setLastName(contact.getLastName());
                toUpdate.setEmail(contact.getEmail());
                toUpdate.setPhone(contact.getPhone());
                toUpdate.setJobTitle(contact.getJobTitle());
                toUpdate.setNotes(contact.getNotes());
                SupplierContact saved = this.repository.save(toUpdate);
                this.auditLogService.logUpdate(
                    "SupplierContact",
                    saved.getId(),
                    saved.getFullName(),
                    details(saved)
                );
                return saved;
            }
        }

        // Tavallinen luonti tai päivitys
        boolean isNew = contact.getId() == null;
        SupplierContact saved = this.repository.save(contact);

        if (isNew) {
            this.auditLogService.logCreate(
                "SupplierContact",
                saved.getId(),
                saved.getFullName(),
                details(saved)
            );
        } else {
            this.auditLogService.logUpdate(
                "SupplierContact",
                saved.getId(),
                saved.getFullName(),
                details(saved)
            );
        }   
        
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        this.repository.findById(id).ifPresent(contact ->
            this.auditLogService.logDelete(
                "SupplierContact",
                id,
                contact.getFullName()
            )
        );
        this.repository.deleteById(id);
    }

    private static String details(SupplierContact contact) {
        return "Nimi: " + contact.getFullName() + ", Puhelin: " + contact.getPhone() + 
               ", Sähköposti: " + contact.getEmail() + ", Titteli: " + contact.getJobTitle() +
               ", Toimittaja: " + (contact.getSupplier() != null ? contact.getSupplier().getName() : "");
     }
}