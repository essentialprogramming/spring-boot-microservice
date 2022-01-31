package com.api.config;

import com.token.validation.auth.AuthUtils;
import com.token.validation.jwt.JwtClaims;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;


/**
 * Authenticates requests that contain an OAuth 2.0 Bearer Token
 * <p>
 * This filter should be used together with an {@link AuthenticationManager}
 * that can authenticate a BearerAuthenticationToken.
 */
public class SecurityFilter extends OncePerRequestFilter {

    private final AuthenticationEntryPoint authenticationEntryPoint = new BearerTokenAuthenticationEntryPoint();
    private final AuthenticationFailureHandler authenticationFailureHandler = (request, response, exception) -> {
        if (exception instanceof AuthenticationServiceException) {
            throw exception;
        } else {
            this.authenticationEntryPoint.commence(request, response, exception);
        }
    };

    private final AuthenticationManager authenticateManager;

    public SecurityFilter(AuthenticationManager authManager) {
        this.authenticateManager = authManager;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            logger.debug("Did not process request since did not find bearer token");
        } else {
            final String token = AuthUtils.extractBearerToken(authorization);
            final BearerTokenAuthenticationToken authenticationRequest = new BearerTokenAuthenticationToken(token);
            try {
                Authentication authenticationResult = authenticateManager.authenticate(authenticationRequest);
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authenticationResult);
                SecurityContextHolder.setContext(context);

            } catch (AuthenticationException exception) {
                SecurityContextHolder.clearContext();
                logger.debug("Failed to process authentication request", exception);
                this.authenticationFailureHandler.onAuthenticationFailure(request, response, exception);
            }

        }
        filterChain.doFilter(request, response);
    }

    public static boolean isUserAllowed(JwtClaims claims, final Set<String> rolesSet) {
        boolean isAllowed = false;

        if (claims.getRoles() == null) {
            return false;
        }
        String[] roles = claims.getRoles().split(",");

        if (rolesSet.stream().anyMatch(Arrays.asList(roles)::contains)) {
            isAllowed = true;
        }
        return isAllowed;
    }
}