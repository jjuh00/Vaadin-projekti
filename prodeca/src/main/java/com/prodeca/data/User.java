package com.prodeca.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Käyttäjäentiteetti
 * 
 * Validoitavat kentät:
 * 1. Käyttäjänimi (pakollinen, merkkijonon pituus, uniikki)
 * 2. Näyttönimi (pakollinen, merkkijonon pituus)
 * 3. Sähköposti (pakolloinen, oikea muoto)
 * 4. Salasana (pakollinen)
 * 5. Rooli (pakollinen, Role-enum)
 */

@Entity
@Table(name = "application_user")
public class User extends AbstractEntity {

    @NotBlank(message = "Käyttäjänimi on pakollinen tieto")
    @Size(min = 3, max = 50, message = "Käyttäjänimen pitää olla 3-50 merkkiä pitkä")
    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @NotBlank(message = "Näyttönimi on pakollinen tieto")
    @Size(min = 2, max = 80, message = "Näyttönimen pitää olla 2-80 merkkiä pitkä")
    @Column(nullable = false, length = 80)
    private String name;

    @NotBlank(message = "Sähköposti on pakollinen tieto")
    @Email(message = "Sähköpostin pitää olla oikeassa muodossa")
    @Column(nullable = false, length = 100)
    private String email;

    @JsonIgnore
    @NotBlank(message = "Salasana on pakollinen tieto")
    @Column(nullable = false)
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    private Set<Role> roles;

    // Profiilikuva (tavutaulukko)
    @Lob
    @Column(length = 1_000_000)
    private byte[] profilePicture;

    // Getterit ja setterit

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
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
    public String getHashedPassword() {
        return hashedPassword;
    }
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    public Set<Role> getRoles() {
        return roles;
    }
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    public byte[] getProfilePicture() {
        return profilePicture;
    }
    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }
}