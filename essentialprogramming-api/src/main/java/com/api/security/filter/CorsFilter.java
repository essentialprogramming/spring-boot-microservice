package com.api.security.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Priority;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Priority(1)
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class CorsFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader(
                "Access-Control-Allow-Headers",
                "origin, content-type, accept, authorization,  X-Requested-With, Content-Length");
        response.setHeader(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD");

        // If it's a preflight request, we abort the request with a 200 status
        if (isPreflightRequest(request)) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            filterChain.doFilter(request, response);
        }

    }


    /**
     * A preflight request is an OPTIONS request
     * with an Origin header.
     */
    private static boolean isPreflightRequest(HttpServletRequest request) {
        return request.getHeader("Origin") != null
                && request.getMethod().equalsIgnoreCase("OPTIONS");
    }

}