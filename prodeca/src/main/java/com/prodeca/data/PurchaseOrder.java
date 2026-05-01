package com.prodeca.data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entiteetti 4: Tilaus (PurchaseOrder)
 * 
 * Tietokantasuhde:
 * - M:N suhde tuotteen (Product) kanssa, joka toteutetaan tilausrivin (PurchaseOrderItem) kautta.
 * 
 * Validoitavat kentät:
 * - 1. Tilausnumero (pakollinen, merkkijonon pituus, uniikki)
 * - 2. Tilauspäivä (pakollinen, ei tulevaisuudessa)
 * - 3. Tila (pakollinen, enum PurchaseOrderStatus)
 * - 4. Kokonaishinta (pakollinen, positiivinen desimaaliluku))
 * - 5. Huomautukset/lisätiedot (valinnainen, merkkijonon pituus)
 */

@Entity
@Table(name = "purchase_order")
public class PurchaseOrder extends AbstractEntity {
    
    // Validoitavat kentät
    @NotBlank(message = "Tilausnumero on pakollinen tieto")
    @Size(min = 3, max = 30, message = "Tilausnumeron pitää olla 3-30 merkkiä pitkä")
    @Column(nullable = false, length = 30, unique = true)
    private String orderNumber;

    @NotNull(message = "Tilauspäivä on pakollinen tieto")
    @PastOrPresent(message = "Tilauspäivä ei voi olla tulevaisuudessa")
    @Column(nullable = false)
    private LocalDate orderDate;

    @NotNull(message = "Tila on pakollinen tieto")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PurchaseOrderStatus status = PurchaseOrderStatus.LUONNOS;

    @NotNull(message = "Tilauksen kokonaishinta on pakollinen tieto")
    @DecimalMin(value = "0.00", message = "Tilauksen kokonaishinta ei voi olla negatiivinen")
    @Digits(integer = 12, fraction = 2, message = "Tilauksen kokonaishinnan pitää olla muodossa x.xx")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column
    private LocalDate expectedDeliveryDate;

    @Size(max = 500, message = "Toimituksen huomautusten enimmäispituus on 500 merkkiä")
    @Column(length = 500)
    private String notes;

    // Tietokantasuhteet
    // 1:N suhde tilausrivin (PurchaseOrderItem) kanssa. Tämä on omistava puoli M:N suhteesta
    @OneToMany(mappedBy = "purchaseOrder", 
                cascade = CascadeType.ALL,
                fetch = FetchType.EAGER,
                orphanRemoval = true)
    private List<PurchaseOrderItem> orderItems = new ArrayList<>();

    // Getterit ja setterit
    public String getOrderNumber() {
        return orderNumber;
    }
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    public LocalDate getOrderDate() {
        return orderDate;
    }
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
    public PurchaseOrderStatus getStatus() {
        return status;
    }
    public void setStatus(PurchaseOrderStatus status) {
        this.status = status;
    }
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public List<PurchaseOrderItem> getOrderItems() {
        return orderItems;
    }
    public void setOrderItems(List<PurchaseOrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    // Metodi, joka palauttaa pilkulla erotellun yhteenvedon tilauksen tuotenimistä käyttöliittymää varten
    public String getProductSummary() {
        if (orderItems == null || orderItems.isEmpty()) return "";
        List<String> names = new ArrayList<>();
        for (PurchaseOrderItem item : orderItems) {
            names.add(item.getProduct().getName());
        }
        return String.join(", ", names);
    }
}