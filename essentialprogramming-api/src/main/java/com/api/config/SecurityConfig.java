package com.api.config;

import com.api.security.JWTAuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.RememberMeServices;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true, // Enable PreAuthorize
        securedEnabled = true, // Enable Secured
        jsr250Enabled = true) //  Enable RolesAllowed
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] AUTH_WHITELIST = {
            // OpenAPI
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/apidoc/**",
            "/v1/user/create/**",
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
            .and()
                .authorizeRequests()
                     .antMatchers(AUTH_WHITELIST).permitAll()
                     .antMatchers("/**").authenticated();
            //.and()
                //.oauth2ResourceServer()
                //.jwt();
        // @formatter:on

        http.antMatcher("/v1/user/**")
                .addFilterAfter(new SecurityFilter(getAuthenticationManager(), "/v1/user/**"), AnonymousAuthenticationFilter.class)
                .rememberMe().alwaysRemember(true);
    }

    @Bean
    public AuthenticationManager getAuthenticationManager() {
        return new JWTAuthenticationManager();
    }

}