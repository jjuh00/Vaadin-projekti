package com.prodeca.services;

import com.prodeca.data.Product;
import com.prodeca.data.ProductRepository;
import com.prodeca.data.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
 
    private final ProductRepository repository;
    private final AuditLogService auditLogService;
    private final InventoryBroadcaster broadcaster;

    public ProductService(ProductRepository repository, AuditLogService auditLogService, InventoryBroadcaster broadcaster) {
        this.repository = repository;
        this.auditLogService = auditLogService;
        this.broadcaster = broadcaster;
    }

    @Transactional(readOnly = true)
    public Optional<Product> getById(Long id) {
        return this.repository.findById(id);
    }

    // Haetaan kaikki aktiiviset tuottet (käytetään M:N valitsijan listaukseen tilauslomakkeella)
    @Transactional(readOnly = true)
    public List<Product> getActive() {
        return this.repository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Product> getBySupplier(Supplier supplier) {
        return this.repository.findBySupplier(supplier);
    }

    @Transactional(readOnly = true)
    public Page<Product> getWithPageable(Pageable pageable) {
        return this.repository.findAll(pageable);
    }

    // Sivutettu haku, joka hyödyntää dynaamista Specificationia suodatinparametrien perusteella
    @Transactional(readOnly = true)
    public Page<Product> getWithSpec(Pageable pageable, ProductSearchFilter filter) {
        // Muodostetaan Criteria API-specification suodatinparametrien perusteella
        Specification<Product> spec = ProductSpecification.build(filter);
        return this.repository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public int count() {
        return (int) this.repository.count();
    }

    // Tallennetaan tuote ja luodaan auditointiloki
    @Transactional
    public Product save(Product product) {
        boolean isNew = product.getId() == null;
        Product saved = this.repository.save(product);
        String details = "SKU: " + saved.getSku() + ", Hinta: " + saved.getUnitPrice() +
                         ", Varastossa: " + saved.getStockQuantity();
        
        if (isNew) {
            this.auditLogService.logCreate("Product", saved.getId(), saved.getName(), details);
            this.broadcaster.broadcast(saved.getName() + " luotu");
        } else {
            this.auditLogService.logUpdate("Product", saved.getId(), saved.getName(), details);
            this.broadcaster.broadcast(saved.getName() + " päivitetty");
        }

        return saved;
    }

    // Poistetaan tuote ja luodaan auditointiloki
    @Transactional
    public void delete(Long id) {
        this.repository.findById(id).ifPresent(p -> {
            this.auditLogService.logDelete("Product", id, p.getName());
            this.broadcaster.broadcast(p.getName() + " poistettu");
        });
        this.repository.deleteById(id);
    }
}