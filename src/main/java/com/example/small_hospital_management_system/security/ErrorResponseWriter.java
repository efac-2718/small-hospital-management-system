package com.example.small_hospital_management_system.security;

import com.example.small_hospital_management_system.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Writes an {@link ErrorResponse} as JSON from inside the security filter chain, where
 * {@code @RestControllerAdvice} cannot help (the request never reaches a controller).
 */
@Component
@RequiredArgsConstructor
public class ErrorResponseWriter {

    private final JsonMapper jsonMapper;

    public void write(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, String message) throws IOException {
        if (response.isCommitted()) {
            return;
        }
        if (status == HttpStatus.UNAUTHORIZED) {
            // RFC 6750: tell the client which authentication scheme is expected.
            String header = request.getHeader(HttpHeaders.AUTHORIZATION) == null
                    ? "Bearer"
                    : "Bearer error=\"invalid_token\"";
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, header);
        }
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), message,
                request.getRequestURI());
        jsonMapper.writeValue(response.getOutputStream(), body);
    }
}
