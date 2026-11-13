package com.digitalmoneyhouse.users.repository;

import com.digitalmoneyhouse.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDni(String dni);

    Optional<User> findByEmailIgnoreCase(String email);
}
