package com.prodeca.services;

import com.prodeca.data.Role;
import com.prodeca.data.User;
import com.prodeca.data.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> get(Long id) {
        return this.repository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return this.repository.findByUsername(username);
    }

    // Tallennetaan käyttäjätiedot (salatulla salasanalla)
    @Transactional
    public User save(User user) {
        return this.repository.save(user);
    }

    // Rekisteröi uuden käyttäjän ja salaa salasanan BCryptillä ennen tallennusta
    @Transactional
    public User registerUser(String username, String displayName, String email, String rawPassword, Set<Role> roles) {
        
        // Tarkistetaan, että käyttäjänimi ei ole jo käytössä
        if (this.repository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Käyttäjänimi '" + username + "' on jo käytössä");
        }

        User user = new User();
        user.setUsername(username);
        user.setName(displayName);
        user.setEmail(email);
        user.setHashedPassword(this.passwordEncoder.encode(rawPassword));
        user.setRoles(roles);

        return this.repository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        this.repository.deleteById(id);
    }

    public Page<User> getWithPageable(Pageable pageable) {
        return this.repository.findAll(pageable);
    }

    public Page<User> getWithSpec(Pageable pageable, Specification<User> filter) {
        return this.repository.findAll(filter, pageable);
    }

    public long count() {
        return this.repository.count();
    }
}