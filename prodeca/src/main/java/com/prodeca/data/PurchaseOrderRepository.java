package com.prodeca.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PurchaseOrderRepository extends
    JpaRepository<PurchaseOrder, Long>,
    JpaSpecificationExecutor<PurchaseOrder> {
    
    // Haetaan tilaus tilausnumeron perusteella
    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    /**
     * Lataa tilauksen yhdessä sen tilausrivien ja tuotteiden kanssa. Estää
     * N+1 kyselyt, kun getProductSummary()-metodia kutsutaan jokaista riviä varten listanäkymässä
     */
    @Query("""
        select distinct po from PurchaseOrder po
        left join fetch po.orderItems oi
        left join fetch oi.product
        where po.id = :id
    """)
    Optional<PurchaseOrder> findByIdWithItems(Long id);
}