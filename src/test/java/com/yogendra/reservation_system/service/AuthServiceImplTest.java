package com.yogendra.reservation_system.service;

import com.yogendra.reservation_system.repository.UserRepository;
import com.yogendra.reservation_system.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.yogendra.reservation_system.dto.RegisterRequest;
import com.yogendra.reservation_system.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.ArgumentCaptor;
import com.yogendra.reservation_system.dto.LoginRequest;
import com.yogendra.reservation_system.dto.LoginResponse;
import com.yogendra.reservation_system.exception.InvalidCredentialsException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.Optional;
import com.yogendra.reservation_system.exception.UserAlreadyExistsException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    @Test
    void register_ShouldSaveUserSuccessfully() {

        RegisterRequest request = new RegisterRequest(
                "mike",
                "mike@example.com",
                "Password123",
                "ADMIN"
        );

        when(userRepository.findByUsername("mike"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("mike@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("Password123"))
                .thenReturn("encodedPassword");

        String result = authService.register(request);

        assertEquals("User Registered Successfully", result);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("mike", savedUser.getUsername());
        assertEquals("mike@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals("USER", savedUser.getRole());

        assertNotNull(savedUser);
        verify(passwordEncoder).encode("Password123");
    }
    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() {

        // Arrange
        RegisterRequest request = new RegisterRequest(
                "john",
                "john@example.com",
                "Password123",
                "USER"
        );

        User existingUser = new User();
        existingUser.setUsername("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(existingUser));

        // Act + Assert
        UserAlreadyExistsException exception =
                assertThrows(
                        UserAlreadyExistsException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                "Username already exists",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }
    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {

        RegisterRequest request = new RegisterRequest(
                "mike",
                "mike@example.com",
                "Password123",
                "ADMIN"
        );

        User existingUser = new User();
        existingUser.setEmail("mike@example.com");

        when(userRepository.findByUsername("mike"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("mike@example.com"))
                .thenReturn(Optional.of(existingUser));

        UserAlreadyExistsException exception =
                assertThrows(
                        UserAlreadyExistsException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                "Email already exists",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }
    @Test
    void login_ShouldReturnLoginResponse_WhenCredentialsAreValid() {

        LoginRequest request = new LoginRequest(
                "john",
                "Password123"
        );

        User user = new User();
        user.setUsername("john");
        user.setPassword("encodedPassword");
        user.setRole("USER");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password123",
                "encodedPassword"
        )).thenReturn(true);

        when(jwtService.generateToken(
                "john",
                "USER"
        )).thenReturn("fake-jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("john", response.getUsername());
        assertEquals("USER", response.getRole());

        verify(jwtService).generateToken("john", "USER");
    }
    @Test
    void login_ShouldThrowException_WhenUserNotFound() {

        LoginRequest request = new LoginRequest(
                "unknown",
                "Password123"
        );

        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        InvalidCredentialsException exception =
                assertThrows(
                        InvalidCredentialsException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Invalid username or password",
                exception.getMessage()
        );

        verify(passwordEncoder, never())
                .matches(any(), any());

        verify(jwtService, never())
                .generateToken(any(), any());
    }
    @Test
    void login_ShouldThrowException_WhenPasswordIsIncorrect() {

        LoginRequest request = new LoginRequest(
                "john",
                "WrongPassword"
        );

        User user = new User();
        user.setUsername("john");
        user.setPassword("encodedPassword");
        user.setRole("USER");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                "encodedPassword"
        )).thenReturn(false);

        InvalidCredentialsException exception =
                assertThrows(
                        InvalidCredentialsException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                "Invalid username or password",
                exception.getMessage()
        );

        verify(jwtService, never())
                .generateToken(any(), any());
    }

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

}