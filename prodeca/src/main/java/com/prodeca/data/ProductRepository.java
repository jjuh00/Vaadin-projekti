package com.prodeca.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductRepository extends 
    JpaRepository<Product, Long>, 
    JpaSpecificationExecutor<Product> {
    // Listataan kaikki tuoteet tietylle toimittajalle (1:1 suhteen näyttämiseksi)
    List<Product> findBySupplier(Supplier supplier);

    // Listataan kaikki aktiiviset tuotteet
    List<Product> findByActiveTrue();
}