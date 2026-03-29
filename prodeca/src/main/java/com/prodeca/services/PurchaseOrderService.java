package com.prodeca.services;

import com.prodeca.data.Product;
import com.prodeca.data.PurchaseOrder;
import com.prodeca.data.PurchaseOrderItem;
import com.prodeca.data.PurchaseOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderService {
    
    private final PurchaseOrderRepository repository;

    public PurchaseOrderService(PurchaseOrderRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<PurchaseOrder> getById(Long id) {
        // Käyttää JPQL-kyselyä hakeakseen tilaukseen liittyvät tilausrivit ja tuotteet
        return repository.findByIdWithItems(id);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrder> getWithPageable(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrder> getWithSpec(Pageable pageable, Specification<PurchaseOrder> filter) {
        return repository.findAll(filter, pageable);
    }

    @Transactional(readOnly = true)
    public int count() {
        return (int) repository.count();
    }

    /**
     * Tallentaa tilauksen yhdessä sen tuotteiden ja tilausrivien kanssa.
     * 
     * List<Product> selectedProducts korvaa KAIKKI olemassaolevat tilausrivit tilaukseen.
     * Yksikköhinnat haetaan nykyisen tuotteen hinnasta ja määrä asetetaan oletuksena 1:ksi per tuote, jos
     * sitä ei ole erikseen määritelty toisella tavalla
     */
    @Transactional
    public PurchaseOrder saveWithProducts(PurchaseOrder order, List<Product> selectedProducts) {
        // Poistetaan kaikki vanhat tilausrivit
        order.getOrderItems().clear();

        // Luodaan uudet tilausrivit jokaiselle valitulle tuotteelle
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (Product p : selectedProducts) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(order);
            item.setProduct(p);
            item.setQuantity(1); // Oletus 1
            item.setUnitPrice(p.getUnitPrice()); // Nykyinen tuotteen hinta
            order.getOrderItems().add(item);
            totalPrice = totalPrice.add(item.getUnitPrice());
        }

        // Lasketaan kokonaishinta
        order.setTotalAmount(totalPrice);

        return repository.save(order);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
