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

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<Product> getById(Long id) {
        return repository.findById(id);
    }

    // Hakee kaikki aktiiviset tuottet (käytetään M:N valitsijan listaukseen tilauslomakkeella)
    @Transactional(readOnly = true)
    public List<Product> getActive() {
        return repository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Product> getBySupplier(Supplier supplier) {
        return repository.findBySupplier(supplier);
    }

    @Transactional(readOnly = true)
    public Page<Product> getWithPageable(Pageable pageable) {
        return repository.findAll(pageable);
    }

    // Sivutettu haku, joka hyödyntää dynaamista Specificationia suodatinparametrien perusteella
    @Transactional(readOnly = true)
    public Page<Product> getWithSpec(Pageable pageable, ProductSearchFilter filter) {
        // Muodostetaan Criteria API-specification suodatinparametrien perusteella
        Specification<Product> spec = ProductSpecification.build(filter);
        return repository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public int count() {
        return (int) repository.count();
    }

    @Transactional
    public Product save(Product product) {
        return repository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}