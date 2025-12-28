package com.liamo.workouts.auth.exception;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private Tracer tracer;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleUserInfoValidationException_thenRedirectToSignin() {
        // Arrange
        UserInfoValidationException exception = new UserInfoValidationException(
            java.util.Map.of("field", "error message")
        );

        // Act
        String result = globalExceptionHandler.handleUserInfoValidationException(exception);

        // Assert
        assertEquals("redirect:/signin", result);
    }

    @Test
    void handleNoResourceFoundException_thenRedirectToSignin() {
        // Arrange
        NoResourceFoundException exception = mock(NoResourceFoundException.class);
        when(exception.getMessage()).thenReturn("Resource not found");

        // Act
        String result = globalExceptionHandler.handleNoResourceFoundException(exception);

        // Assert
        assertEquals("redirect:/signin", result);
    }

    @Test
    void handleException_whenTraceIdExists_thenReturnErrorPageWithTraceId() {
        // Arrange
        Exception exception = new RuntimeException("Test exception");
        Model model = mock(Model.class);
        
        Span span = mock(Span.class);
        TraceContext context = mock(TraceContext.class);
        when(context.traceId()).thenReturn("test-trace-id-123");
        when(span.context()).thenReturn(context);
        when(tracer.currentSpan()).thenReturn(span);

        // Act
        String result = globalExceptionHandler.handleException(exception, model);

        // Assert
        assertEquals("errorPage", result);
        verify(model).addAttribute(eq("traceId"), eq("test-trace-id-123"));
    }

    @Test
    void handleException_whenNoTraceId_thenReturnErrorPageWithNullTraceId() {
        // Arrange
        Exception exception = new RuntimeException("Test exception");
        Model model = mock(Model.class);
        
        when(tracer.currentSpan()).thenReturn(null);

        // Act
        String result = globalExceptionHandler.handleException(exception, model);

        // Assert
        assertEquals("errorPage", result);
        verify(model).addAttribute(eq("traceId"), eq(null));
    }

    @Test
    void handleException_whenTracerReturnsSpan_thenLogTraceId() {
        // Arrange
        Exception exception = new RuntimeException("Test exception");
        Model model = mock(Model.class);
        
        Span span = mock(Span.class);
        TraceContext context = mock(TraceContext.class);
        when(context.traceId()).thenReturn("trace-123");
        when(span.context()).thenReturn(context);
        when(tracer.currentSpan()).thenReturn(span);

        // Act
        String result = globalExceptionHandler.handleException(exception, model);

        // Assert
        assertEquals("errorPage", result);
        verify(tracer).currentSpan();
    }
}
