package com.prodeca.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entiteett 3: Tuote (Product)
 * 
 * Tietokantasuhteet:
 * - N:1 suhde toimittajaan (Supplier)
 * - M:N suhde tilauksen kanssa (PurchaseOrder) tilausrivin (PurchaseOrderItem) kautta
 * 
 * Validoitavat kentät:
 * - 1. Nimi (pakollinen, merkkijonon pituus)
 * - 2. Tuotekoodi (pakollinen, merkkijonon pituus, oikea muoto)
 * - 3. Yksikköhinta (pakollinen, positiivinen desimaaliluku)
 * - 4. Varastomäärä (pakollinen, ei-negatiivinen kokonaisluku)
 * - 5. Kategoria (pakollinen, merkkijonon pituus)
 * - 6. Kuvaus (valinnainen, merkkijonon pituus)
 * - 7. Paino (valinnainen, ei-negatiivinen desimaaliluku)
 */
@Entity
@Table(name = "product")
public class Product extends AbstractEntity {
    
    // Validoitavat kentät
    @NotBlank(message = "Tuotteen nimi on pakollinen tieto")
    @Size(min = 2, max = 100, message = "Tuotteen nimi pitää olla 2-100 merkkiä pitkä")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Tuotekoodi on pakollinen tieto")
    @Size(min = 3, max = 30, message = "Tuotekoodin pitää olla 3-30 merkkiä pitkä")
    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "Tuotekoodi saa sisältää vain isoja kirjaimia, numeroita ja väliviivoja")
    @Column(nullable = false, length = 30)
    private String sku;

    @NotNull(message = "Tuotteen yksikköhinta on pakollinen tieto")
    @DecimalMin(value = "0.01", message = "Tuotteen yksikköhinnan pitää olla vähintään 0.01")
    @Digits(integer = 10, fraction = 2, message = "Tuotteen yksikköhinnan pitää olla muodossa x.xx")
    @Column(nullable = false, precision = 12, scale = 12)
    private BigDecimal unitPrice;

    @NotNull(message = "Tuotteen varastomäärä on pakollinen tieto")
    @Min(value = 0, message = "Tuotteen varastomäärä ei voi olla negatiivinen")
    @Column(nullable = false)
    private Integer stockQuantity;

    @NotBlank(message = "Tuotteen kategoria on pakollinen tieto")
    @Size(min = 2, max = 50, message = "Tuotteen kategorian pitää olla 2-50 merkkiä pitkä")
    @Column(nullable = false, length = 50)
    private String category;

    @Size(max = 500, message = "Tuotteen kuvauksen enimmäispituus on 500 merkkiä")
    @Column(length = 500)
    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Tuotteen paino ei voi olla negatiivinen")
    @Digits(integer = 6, fraction = 3)
    @Column(precision = 9, scale = 3)
    private BigDecimal weight;

    @Column(nullable = false)
    private boolean active = true;

    // Tietokantasuhteet
    // N:1 suhde Supplier-luokan kanssa, sisältää vierasavaimen supplier_id
    @NotNull(message = "Tuotteen toimittaja on pakollinen tieto")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    // 1:N suhde tilausrivin (PurchaseOrderItem, M:N join entiteetti) kanssa. Ei omistaja, joten käytetään mappedBy
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> orderItems = new ArrayList<>();

    // Getterit ja setterit
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSku() {
        return sku;
    }
    public void setSku(String sku) {
        this.sku = sku;
    }
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public BigDecimal getWeight() {
        return weight;
    }
    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public Supplier getSupplier() {
        return supplier;
    }
    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }
    public List<PurchaseOrderItem> getOrderItems() {
        return orderItems;
    }
    public void setOrderItems(List<PurchaseOrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    // toString-metodi, joka voidaan käyttää käyttöliittymässä
    @Override
    public String toString() {
        return name != null ? name +  " (" + sku + ")" : "Tuote #" + getId();
    }
}