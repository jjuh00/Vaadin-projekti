package com.prodeca.data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.ArrayList;
import java.util.List;


/**
 * Entiteetti 1: Toimittaja (supplier)
 * 
 * Tietokantasuhteet:
 * - 1:1 suhde toimittajan yhteistiedojen kanssa (SupplierContact). Ei ole omistaja.
 * - 1:N suhde tuotteen kanssa (Product). Ei ole omistaja.
 * 
 * Validoitavat kentät:
 * - 1. Nimi (pakollinen, merkkijonon pituus)
 * - 2. Sähköposti (pakollinen, oikea muoto)
 * - 3. Puhelin (pakollinen, oikea muoto)
 * - 4. Maa (pakollinen, merkkijonon pituus)
 * - 5. Rekisteröintinumero (pakollinen, merkkijonon pituus, uniikki)
 * - 6. Verkko-osoite (pakollinen, oikea muoto)
 * - 7. Kuvaus (merkkijonon pituus)
 */
@Entity
@Table(name = "supplier")
public class Supplier extends AbstractEntity {
    
    // Validoitavat kentät 
    @NotBlank(message = "Toimittajan nimi on pakollinen tieto")
    @Size(min = 2, max = 80, message = "Toimittajan nimen pitää olla 2-80 merkkiä pitkä")
    @Column(nullable = false, length = 80)
    private String name;

    @NotBlank(message = "Toimittajan sähköposti on pakollinen tieto")
    @Email(message = "Toimittajan sähköpostin pitää olla oikeassa muodossa")
    @Column(nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Toimittajan puhelinnumero on pakollinen tieto")
    @Pattern(regexp = "^[+]?[0-9 \\-()]{7,15}$", message = "Toimittajan puhelinnumeron pitää olla oikeassa muodossa (7-15 merkkiä)")
    @Column(nullable = false, length = 15)
    private String phone;

    @NotBlank(message = "Toimittajan maa on pakollinen tieto")
    @Size(min = 2, max = 50, message = "Toimittajan maan pitää olla 2-50 merkkiä pitkä")
    @Column(nullable = false, length = 50)
    private String country;

    @NotBlank(message = "Rekisteröintinumero on pakollinen tieto")
    @Size(min = 3, max = 30, message = "Rekisteröintinumeron pitää olla 3-30 merkkiä pitkä")
    @Column(nullable = false, length = 30, unique = true)
    private String registrationNumber;

    @URL(message = "Verkko-osoitteen pitää olla oikeassa muodossa")
    @Column(length = 200)
    private String website;

    @Size(max = 500, message = "Toimittajan kuvauksen enimmäispituus on 500 merkkiä")
    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    // Tietokantasuhteet
    // 1:1 suhde SupplierContact-luokan kanssa. Ei omistaja, joten käytetään mappedBy
    @OneToOne(mappedBy = "supplier",
                cascade = CascadeType.ALL,
                orphanRemoval = true,
                fetch = FetchType.LAZY)
    private SupplierContact primaryContact;

    // 1:1 suhde Product-luokan kanssa. Ei omistaja, joten käytetään mappedBy
    @OneToOne(mappedBy = "supplier",
                cascade = CascadeType.ALL,
                orphanRemoval = true,
                fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    // Getterit ja setterit
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getRegistrationNumber() {
        return registrationNumber;
    }
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }
    public String getWebsite() {
        return website;
    }
    public void setWebsite(String website) {
        this.website = website;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public SupplierContact getPrimaryContact() {
        return primaryContact;
    }
    public void setPrimaryContact(SupplierContact primaryContact) {
        this.primaryContact = primaryContact;
    }
    public List<Product> getProducts() {
        return products;
    }
    public void setProducts(List<Product> products) {
        this.products = products;
    }

    // toString-metodi toimittajan nimen näyttämiseksi
    @Override
    public String toString() {
        return name != null ? name : "Toimittaja #" + getId();
    }
}