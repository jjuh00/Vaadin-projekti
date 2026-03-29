package com.prodeca.services;

import com.prodeca.data.Supplier;
import com.prodeca.data.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {
    
    private final SupplierRepository repository;

    public SupplierService(SupplierRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<Supplier> getById(Long id) {
        return repository.findById(id);
    }

    // Palautetaan kaikki toimittajat
    @Transactional(readOnly = true)
    public List<Supplier> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Supplier> getWithPageable(Pageable pageable) {
        return repository.findAll(pageable);
    }   

    // Sivutettu lista valinnaisella Specifition-parametrillä Criteria API-suodatusta varten
    @Transactional(readOnly = true)
    public Page<Supplier> getWithSpec(Pageable pageable, Specification<Supplier> filter) {
        return repository.findAll(filter, pageable);
    }

    @Transactional(readOnly = true)
    public int count() {
        return (int) repository.count();
    }

    @Transactional
    public Supplier save(Supplier supplier) {
        return repository.save(supplier);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}