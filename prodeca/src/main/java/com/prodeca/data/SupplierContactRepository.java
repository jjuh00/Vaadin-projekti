package com.prodeca.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SupplierContactRepository extends 
    JpaRepository<SupplierContact, Long>,
    JpaSpecificationExecutor<SupplierContact> {
    
    // Haetaan kaikki yhteystiedot, jotka liittyvät tiettyyn toimittajaan
    Optional<SupplierContact> findBySupplier(Supplier supplier);
}