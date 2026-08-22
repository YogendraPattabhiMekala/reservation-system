package com.yogendra.reservation_system.service.impl;

import com.yogendra.reservation_system.dto.RegisterRequest;
import com.yogendra.reservation_system.entity.User;
import com.yogendra.reservation_system.repository.UserRepository;
import com.yogendra.reservation_system.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.yogendra.reservation_system.dto.LoginRequest;
import java.util.Optional;
import com.yogendra.reservation_system.service.JwtService;
import com.yogendra.reservation_system.dto.LoginResponse;
import com.yogendra.reservation_system.exception.InvalidCredentialsException;
import com.yogendra.reservation_system.exception.UserAlreadyExistsException;
@Service
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public String register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException(
                    "Username already exists"
            );
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(
                    "Email already exists"
            );
        }

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        userRepository.save(user);

        return "User Registered Successfully";
    }
    @Override
    public LoginResponse login(LoginRequest request) {

        Optional<User> optionalUser =
                userRepository.findByUsername(request.getUsername());

        if (optionalUser.isEmpty()) {
            throw new InvalidCredentialsException(
                    "Invalid username or password"
            );
        }

        User user = optionalUser.get();

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new InvalidCredentialsException(
                    "Invalid username or password"
            );
        }

        String token = jwtService.generateToken(
                user.getUsername(),
                user.getRole()
        );

        return new LoginResponse(
                token,
                "Bearer",
                user.getUsername(),
                user.getRole()
        );
    }
}