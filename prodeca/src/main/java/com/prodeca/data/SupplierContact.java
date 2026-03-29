package com.prodeca.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Entiteetti 2: Toimittajan yhteystiedot (SupplierContact)
 * 
 * Tämä on 1:1 suhteen omistaja toimittajan (Supplier) kanssa eli
 * se pitää sisällään vierasavaimen supplier_id
 * 
 * Validoitavat kentät:
 * - 1. Etunimi (pakollinen, merkkijonon pituus)
 * - 2. Sukunimi (pakollinen, merkkijonon pituus)
 * - 3. Sähköposti (pakollinen, oikea muoto)
 * - 4. Puhelin (pakollinen, oikea muoto)
 * - 5. Titteli (pakollinen, merkkijonon pituus)
 * - 6. Huomautukset/lisätiedot (valinnainen, merkkijonon pituus)
 */

@Entity
@Table(name = "supplier_contact")
public class SupplierContact extends AbstractEntity {
    
    // Validoitavat kentät
    @NotBlank(message = "Toimittajan yhteystiedon etunimi on pakollinen tieto")
    @Size(min = 1, max = 40, message = "Toimittajan yhteystiedon etunimen pitää olla 1-40 merkkiä pitkä")
    @Column(nullable = false, length = 40)
    private String firstName;

    @NotBlank(message = "Toimittajan yhteystiedon sukunimi on pakollinen tieto")
    @Size(min = 1, max = 40, message = "Toimittajan yhteystiedon sukunimen pitää olla 1-40 merkkiä pitkä")
    @Column(nullable = false, length = 40)
    private String lastName;

    @NotBlank(message = "Toimittajan yhteystiedon sähköposti on pakollinen tieto")
    @Email(message = "Toimittajan yhteystiedon sähköpostin pitää olla oikeassa muodossa")
    @Column(nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Toimittajan yhteystiedon puhelinnumero on pakollinen tieto")
    @Pattern(regexp = "^[+]?[0-9 \\-()]{7,15}$", message = "Toimittajan yhteystiedon puhelinnumeron pitää olla oikeassa muodossa (7-15 merkkiä)")
    @Column(nullable = false, length = 15)
    private String phone;

    @NotBlank(message = "Toimittajan yhteystiedon titteli on pakollinen tieto")
    @Size(min = 2, max = 100, message = "Toimittajan yhteystiedon tittelien pitää olla 2-100 merkkiä pitkä")
    @Column(nullable = false, length = 100)
    private String jobTitle;

    @Size(max = 500, message = "Huomautusten pitää olla enintään 500 merkkiä pitkä")
    @Column(length = 500)
    private String notes;

    // Tietokantasuhteet
    // 1:1 suhde Supplier-luokan kanssa, sisältää vierasavaimen supplier_id.
    // Pitää olla uniikki, koska jokaisella SupplierContactilla saa olla vain yksi Supplier
    @NotNull(message = "Toimittaja on pakollinen tieto")
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id", nullable = false, unique = true)
    private Supplier supplier;

    // Getterit ja setterit
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
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
    public String getJobTitle() {
        return jobTitle;
    }
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public Supplier getSupplier() {
        return supplier;
    }
    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    // Metodi, joka voidaan käyttää käyttöliittymässä
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " +
        (lastName != null ? lastName : "");
    }
}
