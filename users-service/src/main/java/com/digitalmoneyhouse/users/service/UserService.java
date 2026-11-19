package com.digitalmoneyhouse.users.service;

import com.digitalmoneyhouse.users.dto.UserRegisterRequest;
import com.digitalmoneyhouse.users.dto.UserResponse;
import com.digitalmoneyhouse.users.entity.Account;
import com.digitalmoneyhouse.users.entity.User;
import com.digitalmoneyhouse.users.exception.UnauthorizedException;
import com.digitalmoneyhouse.users.exception.ValidationException;
import com.digitalmoneyhouse.users.repository.AccountRepository;
import com.digitalmoneyhouse.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class UserService {

    private static final int MAX_GENERATION_ATTEMPTS = 10;
    private static final String INVALID_CREDENTIALS = "Email o contraseña incorrectos";

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountIdentifierGenerator identifierGenerator;
    private final PasswordEncoder passwordEncoder;
    private final String dummyHash;

    public UserService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            AccountIdentifierGenerator identifierGenerator,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.identifierGenerator = identifierGenerator;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("dummy-password");
    }

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
        user.setPassword(passwordEncoder.encode(request.password()));

        Account account = new Account();
        account.setCvu(uniqueCvu());
        account.setAlias(uniqueAlias());
        account.setBalance(new BigDecimal("0.00"));
        account.setUser(user);
        user.setAccount(account);

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public String verifyCredentials(String email, String rawPassword) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        if (normalized.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new ValidationException("El email y la contraseña son obligatorios");
        }

        Optional<User> user = userRepository.findByEmailIgnoreCase(normalized);
        String hash = user.map(User::getPassword).orElse(dummyHash);
        boolean matches = passwordEncoder.matches(rawPassword, hash);
        if (user.isEmpty() || !matches) {
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }
        return user.get().getEmail();
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
