package com.yogendra.reservation_system.filter;

import com.yogendra.reservation_system.service.JwtService;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    @Test
    void shouldContinueFilter_WhenAuthorizationHeaderIsNotBearer()
            throws Exception {

        JwtAuthenticationFilter filter =
                new JwtAuthenticationFilter(jwtService);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Basic abc123"
        );

        filter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(
                request,
                response
        );

        verifyNoInteractions(jwtService);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }
    @Test
    void shouldNotAuthenticate_WhenTokenIsInvalid()
            throws Exception {

        JwtAuthenticationFilter filter =
                new JwtAuthenticationFilter(jwtService);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer test-token"
        );

        when(jwtService.extractUsername("test-token"))
                .thenReturn("john");

        when(jwtService.extractRole("test-token"))
                .thenReturn("USER");

        when(jwtService.isTokenValid(
                "test-token",
                "john"
        )).thenReturn(false);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain).doFilter(
                request,
                response
        );
    }
    @Test
    void shouldReturnUnauthorized_WhenJwtProcessingThrowsException()
            throws Exception {

        JwtAuthenticationFilter filter =
                new JwtAuthenticationFilter(jwtService);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        request.addHeader(
                "Authorization",
                "Bearer invalid-token"
        );

        when(jwtService.extractUsername("invalid-token"))
                .thenThrow(
                        new RuntimeException("Invalid JWT")
                );

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertEquals(
                401,
                response.getStatus()
        );

        assertEquals(
                "Invalid or expired JWT token",
                response.getContentAsString()
        );

        verify(
                filterChain,
                never()
        ).doFilter(
                request,
                response
        );
    }

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}