package com.api.config;

import com.api.security.JWTAuthenticationManager;
import com.authentication.security.PemUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true, // Enable PreAuthorize
        securedEnabled = true, // Enable Secured
        jsr250Enabled = true) //  Enable RolesAllowed
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String PUBLIC_KEY_FILE_PATH = "classpath:pem/public-key.pem";
    private static final String[] AUTH_WHITELIST = {
            // OpenAPI
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/apidoc/**",
            "/v1/user/create/**", //TODO Clean this array
            "/token/**",
            "/questions",
            "/test/**"
    };


    @Override
    protected void configure(final HttpSecurity http) throws Exception {

        http
                .cors().disable()
                .csrf().disable();

        // @formatter:off
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            .and()
//                .authorizeRequests()
//                     .antMatchers(AUTH_WHITELIST).permitAll()
//                     .antMatchers("/**").authenticated();
                .and()
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter());
        // @formatter:on

//        http.antMatcher("/v1/user/**")
//                .addFilterAfter(new SecurityFilter(getAuthenticationManager(), "/v1/user/**"), AnonymousAuthenticationFilter.class)
//                .rememberMe().alwaysRemember(true);

    }

    @Bean
    public AuthenticationManager getAuthenticationManager() {
        return new JWTAuthenticationManager();
    }

    @Bean
    public JwtDecoder jwtDecoder() throws IOException {
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) PemUtils.readPublicKeyFromPEMFile(PUBLIC_KEY_FILE_PATH)).build();
    }


    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            if (jwt.hasClaim("permissions"))
                grantedAuthorities.addAll(
                        jwt.getClaimAsStringList("permissions")
                                .stream()
                                .map(permissionName -> "PERMISSION_" + permissionName)
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList()));
            if (jwt.hasClaim("roles"))
                grantedAuthorities.addAll(
                        jwt.getClaimAsStringList("roles")
                                .stream()
                                .map(roleName -> "ROLE_" + roleName)
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList()));

            return grantedAuthorities;
        });

        return converter;
    }}