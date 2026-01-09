package com.liamo.workouts.auth.controller;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to handle authentication errors and display an error page with trace information.
 */
@Controller
public class AuthErrorController implements ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(AuthErrorController.class);

    private final Tracer tracer;

    public AuthErrorController(Tracer tracer) {
        this.tracer = tracer;
    }

    @RequestMapping("/error")
    public String error(Model model) {
        logger.error("An error occurred while processing a request");

        String traceId = null;
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            traceId = currentSpan.context().traceId();
        }
        model.addAttribute("traceId", traceId);

        logger.debug("Error TraceId: {}", traceId);

        return "errorPage";
    }
}
