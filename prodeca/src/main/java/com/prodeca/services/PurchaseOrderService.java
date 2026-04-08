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

    @PersistenceContext
    private EntityManager entityManager;

    public PurchaseOrderService(PurchaseOrderRepository repository) {
        this.repository = repository;
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

    // Tallentaa tilauksen ja korvaa sen tilausrivit uusilla tuotteilla
    @Transactional
    public PurchaseOrder saveWithProducts(PurchaseOrder order, List<Product> selectedProducts) {
        PurchaseOrder managedOrder = this.repository.findById(order.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Tilausta ei löydy ID:llä " + order.getId()
                ));

        // Kopioidaan binderissä olevat kentät irrotetusta oliosta managed-olioon
        managedOrder.setOrderNumber(order.getOrderNumber());
        managedOrder.setOrderDate(order.getOrderDate());
        managedOrder.setStatus(order.getStatus());
        managedOrder.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
        managedOrder.setNotes(order.getNotes());

        // Siivotaan vanhat tilausrivit
        managedOrder.getOrderItems().clear();

        // Pakotetaan Hibernate päivittämään tietokanta ennen uusien tilausrivien lisäämistä
        entityManager.flush();

        // Luodaan uudet tilausrivit jokaiselle valitulle tuotteelle
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Product p : selectedProducts) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(managedOrder);
            item.setProduct(p);
            item.setQuantity(1); // Oletus 1
            item.setUnitPrice(p.getUnitPrice());
            managedOrder.getOrderItems().add(item);
            totalPrice = totalPrice.add(item.getUnitPrice());
        }

        managedOrder.setTotalAmount(totalPrice);

        return this.repository.save(managedOrder);
    }

    @Transactional
    public void delete(Long id) {
        this.repository.deleteById(id);
    }
}