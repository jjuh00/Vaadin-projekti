package com.prodeca.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Entiteetti 5: Tilausrivi (PurchaseOrderItem, join entiteetti)
 * Toteuttaa M:N suhteen tilauksen (PurchaseOrder) ja tuotteen (Product) välillä lisäattribuuteilla:
 * määrä ja yksikköhinta tilaushetkellä.
 * 
 * Validoitavat kentät:
 * - 1. Tilaus (pakollinen, viite PurchaseOrder)
 * - 2. Tuote (pakollinen, viite Product)
 * - 3. Määrä (pakollinen, positiivinen kokonaisluku)
 * - 4. Yksikköhinta tilaushetkellä (pakollinen, positiivinen desimaaliluku)
 */

@Entity
@Table(name = "purchase_order_item", uniqueConstraints = @UniqueConstraint(columnNames = {"purchase_order_id", "product_id"}))
public class PurchaseOrderItem extends AbstractEntity {
   
    // Validoitavat kentät
    @NotNull(message = "Viittaus tilaukseen on pakollinen tieto")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @NotNull(message = "Viittaus tuotteeseen on pakollinen tieto")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Määrä on pakollinen tieto")
    @Min(value = 1, message = "Määrän pitää olla vähintään 1")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Yksikköhinta tilaushetkellä on pakollinen tieto")
    @DecimalMin(value = "0.01", message = "Yksikköhinnan pitää olla positiivinen desimaaliluku")
    @Digits(integer = 10, fraction = 2, message = "Yksikköhinnan pitää olla muodossa x.xx")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    // Getterit ja setterit
    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }
    public Product getProduct() {
        return product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }
    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    // Laskettu kenttä tilausrivin kokonaishinnalle
    public BigDecimal getTotalPrice() {
        if (quantity == null || unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}