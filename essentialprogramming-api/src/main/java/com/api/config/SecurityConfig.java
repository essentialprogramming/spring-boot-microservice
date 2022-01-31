package com.api.config;

import com.api.security.Http401AuthenticationEntryPoint;
import com.api.security.JWTAuthenticationManager;
import com.authentication.security.KeyStoreService;
import com.spring.ApplicationContextFactory;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true, // Enable PreAuthorize
        securedEnabled = true, // Enable Secured
        jsr250Enabled = true) //  Enable RolesAllowed
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] OPEN_API_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/apidoc/**"
    };

    private static final String[] AUTH_WHITELIST = {
            "/token"
    };



    @Override
    protected void configure(final HttpSecurity http) throws Exception {

        // disable csrf to enable POST, PUT, DELETE requests
        // https://docs.spring.io/spring-security/site/docs/current/reference/html/features.html#csrf-when
        http
                .cors().disable()
                .csrf().disable();

        // @formatter:off
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .authorizeRequests()
                     .antMatchers(anonymousURLs()).permitAll() // Allow access to @Anonymous Controller methods.
                     .antMatchers(AUTH_WHITELIST ).permitAll() // Permit access to these endpoints.
                     .antMatchers(OPEN_API_WHITELIST).permitAll() // Permit access to these endpoints.
                     .requestMatchers(EndpointRequest.to("info", "health", "prometheus"))
                        .permitAll() // Allow access to actuator endpoints.
                     .antMatchers("/**").authenticated() //Any other requests need to be authenticated.
            .and()
                .exceptionHandling()
                .authenticationEntryPoint(new Http401AuthenticationEntryPoint())

//          -------- To validate JWT, use either Spring Security built in method (79 - 82) ----------
//          -------------------------------   or   --------------------------------------------------
//          ----------------------- custom security filter (84 - 87) --------------------------------

            .and()
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtAuthenticationConverter());

            //.and()
                //.antMatcher("/v1/**")
                  //.addFilterAfter(new SecurityFilter(new JWTAuthenticationManager()), RememberMeAuthenticationFilter.class)
                  //.rememberMe().alwaysRemember(true);

        // @formatter:on
    }


    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) KeyStoreService.getInstance().getPublicKey()).build();
    }


    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
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
    }

    private static String[] anonymousURLs() {

        final Map<RequestMappingInfo, HandlerMethod> handlerMethods = ApplicationContextFactory.getBean(RequestMappingHandlerMapping.class).getHandlerMethods();
        final Set<String> anonymousURLs = new HashSet<>();

        handlerMethods.forEach((requestMappingInfo, handlerMethod) -> {
            if (handlerMethod.hasMethodAnnotation(Anonymous.class)) {
                assert requestMappingInfo.getPathPatternsCondition() != null;
                anonymousURLs.addAll(requestMappingInfo.getPathPatternsCondition().getPatternValues());
            }
        });

        return anonymousURLs.toArray(new String[0]);
    }

    //@Bean
    //Not used, left here just to remember this registration method
    public FilterRegistrationBean<SecurityFilter> registerFilter(){
        final FilterRegistrationBean<SecurityFilter> filterFilterRegistrationBean
                = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(new SecurityFilter(new JWTAuthenticationManager()));
        filterFilterRegistrationBean.addUrlPatterns("/v1/*");
        return filterFilterRegistrationBean;
    }

}