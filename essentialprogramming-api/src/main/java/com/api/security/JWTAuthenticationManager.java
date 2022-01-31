package com.api.security;

import com.authentication.security.KeyStoreService;
import com.token.validation.jwt.JwtClaims;
import com.token.validation.jwt.JwtUtil;
import com.token.validation.jwt.exception.TokenValidationException;
import com.token.validation.response.ValidationResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.security.PublicKey;


public class JWTAuthenticationManager implements AuthenticationManager {

    public JWTAuthenticationManager() {
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
        final PublicKey publicKey = KeyStoreService.getInstance().getPublicKey();
        final TokenAuthentication token;
        try {
            final ValidationResponse<JwtClaims> validationResponse = JwtUtil
                    .verifyJwt(bearer.getToken(), publicKey);
            token = new TokenAuthentication(validationResponse.getClaims());
            token.setAuthenticated(validationResponse.isValid());
        } catch (TokenValidationException exception) {
            throw new InvalidBearerTokenException(exception.getMessage(), exception);
        } catch (Exception exception) {
            throw new AuthenticationServiceException(exception.getMessage(), exception);
        }

        return token;
    }
}
