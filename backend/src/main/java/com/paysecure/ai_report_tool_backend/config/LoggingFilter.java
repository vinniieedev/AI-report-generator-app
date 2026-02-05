package com.paysecure.ai_report_tool_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
    private static final ObjectMapper objectMapper =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().substring(0, 8);

        ContentCachingRequestWrapper requestWrapper =
                new ContentCachingRequestWrapper(request);

        ContentCachingResponseWrapper responseWrapper =
                new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {

            long duration = System.currentTimeMillis() - startTime;

            String requestBody = getFormattedBody(
                    requestWrapper.getContentAsByteArray()
            );

            String responseBody = getFormattedBody(
                    responseWrapper.getContentAsByteArray()
            );

            // Mask auth endpoints
            if (request.getRequestURI().contains("/auth")) {
                requestBody = "PROTECTED";
                responseBody = "PROTECTED";
            }

            log.info("""
                    
                    ==================== API REQUEST ====================
                    Request ID  : {}
                    Method      : {}
                    Endpoint    : {}
                    Status      : {}
                    Duration    : {} ms
                    -----------------------------------------------------
                    Request Body:
                    {}
                    -----------------------------------------------------
                    Response Body:
                    {}
                    =====================================================
                    """,
                    requestId,
                    request.getMethod(),
                    request.getRequestURI(),
                    responseWrapper.getStatus(),
                    duration,
                    requestBody,
                    responseBody
            );

            responseWrapper.copyBodyToResponse();
        }
    }

    private String getFormattedBody(byte[] content) {
        if (content == null || content.length == 0) {
            return "EMPTY";
        }

        String body = new String(content, StandardCharsets.UTF_8);

        try {
            Object json = objectMapper.readValue(body, Object.class);
            return objectMapper.writeValueAsString(json);
        } catch (Exception e) {
            return body;
        }
    }
}