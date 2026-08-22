package com.yogendra.reservation_system.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import org.slf4j.MDC;
class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter =
            new CorrelationIdFilter();

    @Test
    void shouldGenerateCorrelationId_WhenHeaderIsMissing()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        String correlationId =
                response.getHeader(
                        CorrelationIdFilter.CORRELATION_ID_HEADER
                );

        assertNotNull(correlationId);

        assertFalse(
                correlationId.isBlank()
        );

        assertDoesNotThrow(
                () -> java.util.UUID.fromString(correlationId)
        );
    }
    @Test
    void shouldPreserveCorrelationId_WhenHeaderIsProvided()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

        String existingCorrelationId =
                "client-request-123";

        request.addHeader(
                CorrelationIdFilter.CORRELATION_ID_HEADER,
                existingCorrelationId
        );

        filter.doFilter(
                request,
                response,
                filterChain
        );

        String responseCorrelationId =
                response.getHeader(
                        CorrelationIdFilter.CORRELATION_ID_HEADER
                );

        assertEquals(
                existingCorrelationId,
                responseCorrelationId
        );
    }
    @Test
    void shouldClearCorrelationIdFromMdc_AfterRequestCompletes()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain filterChain =
                mock(FilterChain.class);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                org.slf4j.MDC.get(
                        CorrelationIdFilter.CORRELATION_ID_MDC_KEY
                )
        );
        assertNull(
                MDC.get(
                        CorrelationIdFilter.CORRELATION_ID_MDC_KEY
                )
        );
    }
}