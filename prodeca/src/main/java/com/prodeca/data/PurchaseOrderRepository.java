package com.prodeca.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PurchaseOrderRepository extends
    JpaRepository<PurchaseOrder, Long>,
    JpaSpecificationExecutor<PurchaseOrder> {
    
    // Haetaan tilaus tilausnumeron perusteella
    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);
}