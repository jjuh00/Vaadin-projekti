package com.prodeca.services;

import com.prodeca.data.Product;
import com.prodeca.data.PurchaseOrder;
import com.prodeca.data.PurchaseOrderItem;
import com.prodeca.data.PurchaseOrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderService {
    
    private final PurchaseOrderRepository repository;
    private final AuditLogService auditLogService;

    @PersistenceContext
    private EntityManager entityManager;

    public PurchaseOrderService(PurchaseOrderRepository repository, AuditLogService auditLogService) {
        this.repository = repository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Optional<PurchaseOrder> getById(Long id) {
        return this.repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrder> getWithPageable(Pageable pageable) {
        return this.repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public int count() {
        return (int) this.repository.count();
    }

    // Tallennetaan tilaus ja korvataan sen tilausrivit uusilla tuotteilla sekä luodaan auditointiloki
    @Transactional
    public PurchaseOrder saveWithProducts(PurchaseOrder order, List<Product> selectedProducts) {
        boolean isNew = order.getId() == null;

        PurchaseOrder target;

        if (isNew) {
            // Uusi tilaus, luodaan uusi entiteetti
            target = order;
        } else {
            // Haetaan hallittu tilausentiteetti, että JPA voi seurata orderItems-kokoelman muutoksia
            target = this.repository.findById(order.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Tilausta ei löytynyt ID:llä: " + order.getId()
                    ));

            // Kopioidaan tilauksen perusominaisuudet hallittuun entiteettiin
            target.setOrderNumber(order.getOrderNumber());
            target.setOrderDate(order.getOrderDate());
            target.setStatus(order.getStatus());
            target.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
            target.setNotes(order.getNotes());

            // Poistetaan vanhat tilausrivit
            target.getOrderItems().clear();
            entityManager.flush();
        }

        // Lisätään uudet tilausrivit valittujen tuotteiden perusteella
        BigDecimal total = BigDecimal.ZERO;
        for (Product product : selectedProducts) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(target);
            item.setProduct(product);
            item.setQuantity(1); // Oletus 1
            item.setUnitPrice(product.getUnitPrice());
            target.getOrderItems().add(item);
            total = total.add(product.getUnitPrice());
        }
        target.setTotalAmount(total);

        PurchaseOrder saved = this.repository.save(target);

        String details = "Tilausnumero: " + saved.getOrderNumber() + ", Tila: " + saved.getStatus() +
                         ", Tuotteet: " + saved.getProductSummary() + ", Hinta: " + saved.getTotalAmount();

        if (isNew) {
            this.auditLogService.logCreate("PurchaseOrder", saved.getId(), saved.getOrderNumber(), details);
        } else {
            this.auditLogService.logUpdate("PurchaseOrder", saved.getId(), saved.getOrderNumber(), details);
        }

        return saved;
    }

    // Poistetaan tilaus ja luodaan auditointiloki
    @Transactional
    public void delete(Long id) {
        this.repository.findById(id).ifPresent(po ->
            this.auditLogService.logDelete("PurchaseOrder", id, po.getOrderNumber())
        );
        this.repository.deleteById(id);
    }
}