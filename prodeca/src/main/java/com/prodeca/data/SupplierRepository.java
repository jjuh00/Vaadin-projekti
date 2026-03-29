package com.prodeca.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SupplierRepository extends 
    JpaRepository<Supplier, Long>, 
    JpaSpecificationExecutor<Supplier> {

    // Käytetään rekisteröintinumerojen uniikkiuden varmistamiseen service-kerroksessa
    Optional<Supplier> findByRegistrationNumber(String registrationNumber);

    // Käytetään sähköpostiosoitteiden uniikkiuden varmistamiseen service-kerroksessa
    Optional<Supplier> findByEmail(String email);
}