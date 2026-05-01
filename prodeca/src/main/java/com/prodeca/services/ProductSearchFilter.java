package com.prodeca.services;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO-luokka, joka pitää sisällään käyttäjän syöttämät hakukriteerit. Välitetään ProductSpecification-luokalle Criteria API-
 * kyselyä varten. Sisältää useita hakukenttiä, päivämäärähaun ja toimittajan nimen haun
 */

public class ProductSearchFilter {
    
    private String nameOrSku; // Hakuteksti tuotenimelle TAI tuotekoodille
    private String category; // Hakuteksti tuotekategorioille
    private String supplierName; // Hakuteksti toimittajan nimelle
    private BigDecimal minPrice; // Minimihinta
    private BigDecimal maxPrice; // Maksimihinta
    private Boolean activeOnly; // Jos true, haetaan vain aktiiviset tuotteet
    private LocalDate orderDateFrom; // Tilauspäivän alaraja
    private LocalDate orderDateTo; // Tilauspäivän yläraja

    // Getterit ja setterit
    public String getNameOrSku() {
        return nameOrSku;
    }
    public void setNameOrSku(String nameOrSku) {
        this.nameOrSku = nameOrSku;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getSupplierName() {
        return supplierName;
    }
    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }
    public BigDecimal getMinPrice() {
        return minPrice;
    }
    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }
    public BigDecimal getMaxPrice() {
        return maxPrice;
    }
    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }
    public Boolean getActiveOnly() {
        return activeOnly;
    }
    public void setActiveOnly(Boolean activeOnly) {
        this.activeOnly = activeOnly;
    }
    public LocalDate getOrderDateFrom() {
        return orderDateFrom;
    }
    public void setOrderDateFrom(LocalDate orderDateFrom) {
        this.orderDateFrom = orderDateFrom;
    }
    public LocalDate getOrderDateTo() {
        return orderDateTo;
    }
    public void setOrderDateTo(LocalDate orderDateTo) {
        this.orderDateTo = orderDateTo;
    }

    // Funktio, joka palauttaa true, jos käyttäjä on syöttänyt väh. 1 hakukentän
    public boolean hasAnyFilter() {
        return (nameOrSku != null && !nameOrSku.isBlank()) ||
               (category != null && !category.isBlank()) ||
               (supplierName != null && !supplierName.isBlank()) ||
               minPrice != null ||
               maxPrice != null ||
               (activeOnly != null && activeOnly) ||
               orderDateFrom != null ||
               orderDateTo != null;
    }
}