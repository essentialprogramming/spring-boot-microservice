package com.api.config;

import com.api.security.TokenAuthentication;
import com.authentication.security.KeyStoreService;
import com.token.validation.auth.AuthUtils;
import com.token.validation.jwt.JwtClaims;
import com.token.validation.jwt.JwtUtil;
import com.token.validation.jwt.exception.TokenValidationException;
import com.token.validation.response.ValidationResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Set;


/**
 * Authenticates requests that contain an OAuth 2.0 Bearer Token
 * <p>
 * This filter should be used together with an {@link AuthenticationManager}
 * that can authenticate a BearerAuthenticationToken.
 */
public class SecurityFilter extends AbstractAuthenticationProcessingFilter {

    private final AuthenticationManager authenticateManager;

    public SecurityFilter(AuthenticationManager authManager, String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
        this.authenticateManager = authManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {

        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "{\"error\":\"missing_authorization_header\"}");
            return null;
        }

        if (!authorization.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ("{\"error\":\"invalid_authorization_scheme\"}"));
            return null;
        }

        final String token = AuthUtils.extractBearerToken(authorization);
        final PublicKey publicKey;
        try {
            publicKey = KeyStoreService.getInstance().getPublicKey();
            ValidationResponse<JwtClaims> validationResponse = JwtUtil.verifyJwt(token, publicKey);
            if (!validationResponse.isValid()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "{\"error\":\"invalid_credentials\"}");
                return null;
            }

            return authenticateManager.authenticate(new TokenAuthentication(validationResponse.getClaims()));
        } catch (TokenValidationException exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ("{\"error\":\"invalid_token_format\"}"));
        } catch (Exception exception) {
            exception.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ("{\"error\":\"Unable to process your request, due to: \n" + ExceptionUtils.getStackTrace(exception) + "\n\"}"));

        }
        return null;

    }

    @Override
    protected final void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                  FilterChain chain, Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        if (!requiresAuthentication(request, response)) {
            chain.doFilter(request, response);
            return;
        }

        Authentication authResult;
        try {
            authResult = attemptAuthentication(request, response);
            if (authResult == null) {
                // return immediately as authentication hasn't completed
                return;
            }
            successfulAuthentication(request, response, chain, authResult);

        } catch (AuthenticationException failed) {
            chain.doFilter(request, response);
        }
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