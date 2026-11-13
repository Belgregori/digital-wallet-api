package com.digitalmoneyhouse.users.service;

import com.digitalmoneyhouse.users.dto.UserCredentialsResponse;
import com.digitalmoneyhouse.users.dto.UserRegisterRequest;
import com.digitalmoneyhouse.users.dto.UserResponse;
import com.digitalmoneyhouse.users.entity.Account;
import com.digitalmoneyhouse.users.entity.User;
import com.digitalmoneyhouse.users.exception.ResourceNotFoundException;
import com.digitalmoneyhouse.users.exception.ValidationException;
import com.digitalmoneyhouse.users.repository.AccountRepository;
import com.digitalmoneyhouse.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountIdentifierGenerator identifierGenerator;

    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ValidationException("El email ya está registrado");
        }

        String dni = request.dni().trim();
        if (userRepository.existsByDni(dni)) {
            throw new ValidationException("El DNI ya está registrado");
        }

        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setDni(dni);
        user.setEmail(email);
        user.setPhoneNumber(request.phoneNumber().trim());
        user.setPassword(request.password());

        Account account = new Account();
        account.setCvu(uniqueCvu());
        account.setAlias(uniqueAlias());
        account.setBalance(new BigDecimal("0.00"));
        account.setUser(user);
        user.setAccount(account);

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserCredentialsResponse findCredentialsByEmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        if (normalized.isBlank()) {
            throw new ValidationException("El email es obligatorio");
        }
        User user = userRepository.findByEmailIgnoreCase(normalized)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no existe"));
        return new UserCredentialsResponse(user.getEmail(), user.getPassword());
    }

    private String uniqueCvu() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String cvu = identifierGenerator.generateCvu();
            if (!accountRepository.existsByCvu(cvu)) {
                return cvu;
            }
        }
        throw new IllegalStateException("No se pudo generar un CVU único");
    }

    private String uniqueAlias() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String alias = identifierGenerator.generateAlias();
            if (!accountRepository.existsByAlias(alias)) {
                return alias;
            }
        }
        throw new IllegalStateException("No se pudo generar un alias único");
    }
}
