package com.prodeca.services;

import com.prodeca.data.User;
import com.prodeca.data.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<User> get(Long id) {
        return this.repository.findById(id);
    }

    public User save(User entity) {
        return this.repository.save(entity);
    }

    public void delete(Long id) {
        this.repository.deleteById(id);
    }

    public Page<User> list(Pageable pageable) {
        return this.repository.findAll(pageable);
    }

    public Page<User> list(Pageable pageable, Specification<User> filter) {
        return this.repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) this.repository.count();
    }
}