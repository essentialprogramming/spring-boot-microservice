package com.api.security.filter;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter - prevents from OPTIONS and HEAD Rest calls.
 */
@Component
public class HttpMethodTypeSecurityFilter extends OncePerRequestFilter {

    /**
     * Filter internal
     *
     * @param request     HttpServletRequest
     * @param response    HttpServletResponse
     * @param filterChain FilterChain
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (request.getMethod().equals(RequestMethod.OPTIONS.name())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        } else if (request.getMethod().equals(RequestMethod.HEAD.name())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
