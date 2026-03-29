package com.prodeca.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PurchaseOrderItemRepository extends 
    JpaRepository<PurchaseOrderItem, Long>,
    JpaSpecificationExecutor<PurchaseOrderItem> {
    
    // Haetaan kaikki tilausrivit, jotka liittyvät tiettyyn tilaukseen
    List<PurchaseOrderItem> findByPurchaseOrder(PurchaseOrder purchaseOrder);

    // Haetaan kaikki tilausrivit, jotka liittyvät tiettyyn tuotteeseen
    List<PurchaseOrderItem> findByProduct(Product product);
}