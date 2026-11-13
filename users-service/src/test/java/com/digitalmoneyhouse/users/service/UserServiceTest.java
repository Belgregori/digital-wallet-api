package com.digitalmoneyhouse.users.service;

import com.digitalmoneyhouse.users.dto.UserCredentialsResponse;
import com.digitalmoneyhouse.users.dto.UserRegisterRequest;
import com.digitalmoneyhouse.users.dto.UserResponse;
import com.digitalmoneyhouse.users.entity.User;
import com.digitalmoneyhouse.users.exception.ResourceNotFoundException;
import com.digitalmoneyhouse.users.exception.ValidationException;
import com.digitalmoneyhouse.users.repository.AccountRepository;
import com.digitalmoneyhouse.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @InjectMocks
    private UserService userService;

    @Test
    void registerSavesUserAndAccountAndOmitsPasswordFromResponse() {
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
        assertEquals("secreta", saved.getPassword());
        assertSame(saved, saved.getAccount().getUser());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("ana@example.com")).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class, () -> userService.register(request()));

        assertEquals("El email ya está registrado", exception.getMessage());
    }

    @Test
    void findCredentialsReturnsStoredPassword() {
        User user = new User();
        user.setEmail("ana@example.com");
        user.setPassword("secreta");
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(user));

        UserCredentialsResponse credentials = userService.findCredentialsByEmail(" Ana@Example.com ");

        assertEquals("ana@example.com", credentials.email());
        assertEquals("secreta", credentials.password());
    }

    @Test
    void findCredentialsThrowsNotFoundWhenEmailDoesNotExist() {
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findCredentialsByEmail("ana@example.com"));
    }

    private UserRegisterRequest request() {
        return new UserRegisterRequest(" Ana ", "Perez", " Ana@Example.com ", "12345678", "1122334455", "secreta");
    }
}
