package com.digitalmoneyhouse.users.service;

import com.digitalmoneyhouse.users.client.AccountProvisioning;
import com.digitalmoneyhouse.users.dto.AccountResponse;
import com.digitalmoneyhouse.users.dto.UserProfileResponse;
import com.digitalmoneyhouse.users.dto.UserRegisterRequest;
import com.digitalmoneyhouse.users.dto.UserResponse;
import com.digitalmoneyhouse.users.dto.UserUpdateRequest;
import com.digitalmoneyhouse.users.entity.User;
import com.digitalmoneyhouse.users.exception.ForbiddenException;
import com.digitalmoneyhouse.users.exception.ResourceNotFoundException;
import com.digitalmoneyhouse.users.exception.UnauthorizedException;
import com.digitalmoneyhouse.users.exception.ValidationException;
import com.digitalmoneyhouse.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private static final String INVALID_CREDENTIALS = "Email o contraseña incorrectos";

    private final UserRepository userRepository;
    private final AccountProvisioning accountProvisioning;
    private final PasswordEncoder passwordEncoder;
    private final String dummyHash;

    public UserService(
            UserRepository userRepository,
            AccountProvisioning accountProvisioning,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountProvisioning = accountProvisioning;
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
        User saved = userRepository.save(user);
        AccountResponse account = accountProvisioning.openAccount(saved.getId(), saved.getEmail());
        return UserResponse.of(saved, account);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse profile(Long id, String email) {
        return toProfile(ownedUser(id, email));
    }

    @Transactional
    public UserProfileResponse update(Long id, String email, UserUpdateRequest request) {
        User user = ownedUser(id, email);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhoneNumber(request.phoneNumber().trim());
        return toProfile(userRepository.save(user));
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

    private User ownedUser(Long id, String email) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no existe"));
        if (!user.getEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenException("No tenés permiso para ver este usuario");
        }
        return user;
    }

    private UserProfileResponse toProfile(User user) {
        AccountResponse account = accountProvisioning.findByUserId(user.getId());
        return new UserProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getDni(),
                user.getEmail(),
                user.getPhoneNumber(),
                account.cvu(),
                account.alias());
    }
}
