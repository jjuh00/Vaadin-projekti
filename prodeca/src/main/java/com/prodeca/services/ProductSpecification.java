package com.prodeca.services;

import com.prodeca.data.Product;
import com.prodeca.data.PurchaseOrder;
import com.prodeca.data.PurchaseOrderItem;
import com.prodeca.data.Supplier;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

// Luokka, joka muodostaa dynaamisen JPA Criteria API-kyselypredikaatin ProductSearchFilter-olion pohjalta
public class ProductSpecification {
    // Funktio, joka muodostaa Specification<Product>-olion filterin perusteella
    public static Specification<Product> build(ProductSearchFilter filter) {
        return (Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Nimi TAI tuotekoodi
            if (filter.getNameOrSku() != null && !filter.getNameOrSku().isBlank()) {
                String pattern = "%" + filter.getNameOrSku().toLowerCase() + "%";

                Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
                Predicate skuLike = cb.like(cb.lower(root.get("sku")), pattern);

                predicates.add(cb.or(nameLike, skuLike));
            }

            // Kategoria
            if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
                String pattern = "%" + filter.getCategory().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("category")), pattern));
            }

            // Toimittajan nimi
            if (filter.getSupplierName() != null && !filter.getSupplierName().isBlank()) {
                Join<Product, Supplier> supplierJoin = root.join("supplier", JoinType.INNER);
                String pattern = "%" + filter.getSupplierName().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(supplierJoin.get("name")), pattern));
            }

            // Yksikköhinta välillä
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("unitPrice"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("unitPrice"), filter.getMaxPrice()));
            }

            // Vain aktiiviset tuotteet
            if (Boolean.TRUE.equals(filter.getActiveOnly())) {
                predicates.add(cb.isTrue(root.get("active")));
            }

            // Tilauspäivän vaihteluväli
            if (filter.getOrderDateFrom() != null || filter.getOrderDateTo() != null) {
                Join<Product, PurchaseOrderItem> itemJoin = root.join("orderItems", JoinType.LEFT);
                Join<PurchaseOrderItem, PurchaseOrder> orderJoin = itemJoin.join("purchaseOrder", JoinType.LEFT);

                if (filter.getOrderDateFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(
                        orderJoin.get("orderDate"), filter.getOrderDateFrom())
                    );
                }
                if (filter.getOrderDateTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(
                        orderJoin.get("orderDate"), filter.getOrderDateTo())
                    );
                }

                query.distinct(true);
            }

            // Yhdistetään kaikki predikaatit AND-operaattorilla
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}