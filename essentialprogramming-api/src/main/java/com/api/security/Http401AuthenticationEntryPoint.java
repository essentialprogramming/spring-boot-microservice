package com.api.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class Http401AuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.debug("HTTP 401 authentication entry point called. Rejecting access");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null) {
            response.getWriter().write("{\"error\":\"missing_authorization_header\"}");
            return;
        }
        if (!authorization.startsWith("Bearer ")) {
            response.getWriter().write("{\"error\":\"invalid_authorization_scheme\"}");
            return;
        }

        response.getWriter().write("{\"error\":\"invalid_credentials\"}");
    }
}
