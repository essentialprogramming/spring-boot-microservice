package com.api.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;



public class JWTAuthenticationManager implements AuthenticationManager {

	public JWTAuthenticationManager() {
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		authentication.setAuthenticated(true);
		return authentication;
	}
}
