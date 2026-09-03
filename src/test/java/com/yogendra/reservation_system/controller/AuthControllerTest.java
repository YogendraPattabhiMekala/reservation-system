package com.yogendra.reservation_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yogendra.reservation_system.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.yogendra.reservation_system.dto.RegisterRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import com.yogendra.reservation_system.exception.UserAlreadyExistsException;
import com.yogendra.reservation_system.service.JwtService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.yogendra.reservation_system.dto.LoginRequest;
import com.yogendra.reservation_system.dto.LoginResponse;
import com.yogendra.reservation_system.exception.InvalidCredentialsException;
import com.yogendra.reservation_system.exception.UserAlreadyExistsException;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
    @Test
    void register_ShouldReturnSuccessMessage() throws Exception {

        RegisterRequest request =
                new RegisterRequest(
                        "john",
                        "john@example.com",
                        "Password123",
                        "USER"
                );

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn("User Registered Successfully");

        mockMvc.perform(

                        post("/auth/register")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )

                )

                .andExpect(status().isOk())
                .andExpect(content().string("User Registered Successfully"));

    }
    @Test
    void login_ShouldReturnLoginResponse() throws Exception {

        LoginRequest request = new LoginRequest(
                "john",
                "Password123"
        );

        LoginResponse response = new LoginResponse(
                "fake-jwt-token",
                "Bearer",
                "john",
                "USER"
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/auth/login")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
    @Test
    void register_ShouldReturnBadRequest_WhenRequestIsInvalid() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "",
                "wrong-email",
                "123",
                "USER"
        );

        mockMvc.perform(
                        post("/auth/register")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.username")
                        .value("Username is required"))
                .andExpect(jsonPath("$.fieldErrors.email")
                        .value("Email must be valid"))
                .andExpect(jsonPath("$.fieldErrors.password")
                        .value("Password must contain at least 8 characters"));
    }
    @Test
    void register_ShouldReturnConflict_WhenUsernameAlreadyExists() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "john",
                "john@example.com",
                "Password123",
                "USER"
        );

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Username already exists"));

        mockMvc.perform(
                        post("/auth/register")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }
    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() throws Exception {

        LoginRequest request = new LoginRequest(
                "john",
                "WrongPassword"
        );

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException(
                        "Invalid username or password"
                ));

        mockMvc.perform(
                        post("/auth/login")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid username or password"));
    }
    @Test
    void register_ShouldReturnBadRequest_WhenJsonIsMalformed()
            throws Exception {

        String malformedJson = """
            {
              "username": "john",
              "email": "john@test.com",
              "password": "Password123",
            }
            """;

        mockMvc.perform(
                        post("/auth/register")
                                .contentType("application/json")
                                .content(malformedJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                );
    }
    @Test
    void register_ShouldReturnConflict_WhenUserAlreadyExists()
            throws Exception {

        RegisterRequest request =
                new RegisterRequest(
                        "john",
                        "john@test.com",
                        "Password123",
                        "USER"
                );

        when(
                authService.register(
                        any(RegisterRequest.class)
                )
        ).thenThrow(
                new UserAlreadyExistsException(
                        "User already exists"
                )
        );

        mockMvc.perform(
                        post("/auth/register")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(
                        jsonPath("$.message")
                                .value("User already exists")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/auth/register")
                );
    }
    @Test
    void register_ShouldReturnInternalServerError_WhenUnexpectedExceptionOccurs()
            throws Exception {

        RegisterRequest request =
                new RegisterRequest(
                        "john",
                        "john@test.com",
                        "Password123",
                        "USER"
                );

        when(
                authService.register(
                        any(RegisterRequest.class)
                )
        ).thenThrow(
                new RuntimeException(
                        "Unexpected database failure"
                )
        );

        mockMvc.perform(
                        post("/auth/register")
                                .contentType("application/json")
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(
                        jsonPath("$.error")
                                .value("Internal Server Error")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("An unexpected error occurred")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/auth/register")
                );
    }


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtService jwtService;

}