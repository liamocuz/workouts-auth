package com.liamo.workouts.auth.exception;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler for the application.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private final static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Tracer tracer;

    public GlobalExceptionHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @ExceptionHandler(UserInfoValidationException.class)
    public String handleUserInfoValidationException(UserInfoValidationException ex) {
        logger.error("UserInfoValidationException: {}", ex.getMessage(), ex);

        return "redirect:/signin";
    }

    /**
     * This exception is thrown when a static resource does not exist for a path.
     * We just want to redirect back to /signin in that case.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(NoResourceFoundException ex) {
        logger.error("NoResourceFoundException: {}", ex.getMessage(), ex);

        return "redirect:/signin";
    }

    /**
     * Base catch-all exception handler.
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        logger.error("Global Exception: {}", ex.getMessage(), ex);

        String traceId = null;
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            traceId = currentSpan.context().traceId();
        }
        logger.info("traceId: {}", traceId != null ? traceId : "blah");
        model.addAttribute("traceId", traceId);

        return "errorPage";
    }
}
