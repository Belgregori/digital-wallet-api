package com.digitalmoneyhouse.users.service;

import com.digitalmoneyhouse.users.dto.UserRegisterRequest;
import com.digitalmoneyhouse.users.dto.UserResponse;
import com.digitalmoneyhouse.users.entity.User;
import com.digitalmoneyhouse.users.exception.UnauthorizedException;
import com.digitalmoneyhouse.users.exception.ValidationException;
import com.digitalmoneyhouse.users.repository.AccountRepository;
import com.digitalmoneyhouse.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountIdentifierGenerator identifierGenerator;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, accountRepository, identifierGenerator, passwordEncoder);
    }

    @Test
    void registerSavesUserAndAccountAndStoresBcryptHash() {
        when(identifierGenerator.generateCvu()).thenReturn("1234567890123456789012");
        when(identifierGenerator.generateAlias()).thenReturn("sol.luna.rio");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.getAccount().setId(2L);
            return user;
        });

        UserResponse response = userService.register(request());

        assertEquals(1L, response.id());
        assertEquals("Ana", response.firstName());
        assertEquals("ana@example.com", response.email());
        assertEquals("1234567890123456789012", response.account().cvu());
        assertEquals("sol.luna.rio", response.account().alias());
        assertEquals(new BigDecimal("0.00"), response.account().balance());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertTrue(saved.getPassword().startsWith("$2a$"));
        assertFalse(saved.getPassword().contains("secreta12"));
        assertTrue(passwordEncoder.matches("secreta12", saved.getPassword()));
        assertSame(saved, saved.getAccount().getUser());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class, () -> userService.register(request()));

        assertEquals("El email ya está registrado", exception.getMessage());
    }

    @Test
    void verifyCredentialsAcceptsMatchingPassword() {
        User user = new User();
        user.setEmail("ana@example.com");
        user.setPassword(passwordEncoder.encode("secreta12"));
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(user));

        assertEquals("ana@example.com", userService.verifyCredentials(" Ana@Example.com ", "secreta12"));
    }

    @Test
    void verifyCredentialsRejectsUnknownUserAndWrongPasswordWithTheSameMessage() {
        User user = new User();
        user.setEmail("ana@example.com");
        user.setPassword(passwordEncoder.encode("secreta12"));
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmailIgnoreCase("otro@example.com")).thenReturn(Optional.empty());

        UnauthorizedException wrongPassword = assertThrows(
                UnauthorizedException.class,
                () -> userService.verifyCredentials("ana@example.com", "otraClave1"));
        UnauthorizedException unknownUser = assertThrows(
                UnauthorizedException.class,
                () -> userService.verifyCredentials("otro@example.com", "secreta12"));

        assertEquals("Email o contraseña incorrectos", wrongPassword.getMessage());
        assertEquals(wrongPassword.getMessage(), unknownUser.getMessage());
    }

    private UserRegisterRequest request() {
        return new UserRegisterRequest(" Ana ", "Perez", " Ana@Example.com ", "12345678", "1122334455", "secreta12");
    }
}
