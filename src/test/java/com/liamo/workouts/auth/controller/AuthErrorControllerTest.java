package com.liamo.workouts.auth.controller;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthErrorController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthErrorControllerTest {

    @Autowired
    MockMvc mockMvc;


    @MockitoBean
    private Tracer tracer;

    @Test
    void get_error() throws Exception {
        Span span = mock(Span.class);
        TraceContext context = mock(TraceContext.class);
        when(context.traceId()).thenReturn("test-trace-id");
        when(span.context()).thenReturn(context);

        when(tracer.currentSpan()).thenReturn(span);

        mockMvc
            .perform(get("/error"))
            .andExpect(status().isOk())
            .andExpect(view().name("errorPage"))
            .andExpect(model().attribute("traceId", "test-trace-id"));
    }
}