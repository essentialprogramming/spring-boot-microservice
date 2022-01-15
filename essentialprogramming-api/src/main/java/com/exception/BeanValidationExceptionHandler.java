package com.exception;


import com.util.web.JsonResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.*;

/**
 * Custom ExceptionMapper to return an appropriate response to the client when
 * a validation exception occurred.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class BeanValidationExceptionHandler {

    private static final String REMOTE_ADDRESS = "unknown";

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ResponseEntity<JsonResponse> toResponse(final HttpServletRequest request,
                                                   final ConstraintViolationException e) {

        String url = "";
        String remoteAddress = REMOTE_ADDRESS;
        if (request != null) {
            url = request.getRequestURL() != null ? request.getRequestURL().toString() : "";
            remoteAddress = request.getRemoteAddr();
        }

        Set<ConstraintViolation<?>> constraintValidations = e.getConstraintViolations();
        ConstraintViolation<?> constraintValidation = constraintValidations.iterator().next();

        final JsonResponse jsonResponse;
        jsonResponse = new JsonResponse()
                .with("From", remoteAddress)
                .with("To", url)
                .with("Message", constraintValidation.getMessage())
                .with("Code", HttpStatus.BAD_REQUEST)
                .done();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(jsonResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<JsonResponse> handleMissingRequestBody(final HttpServletRequest request,
                                                                           final Exception e) {
        String url = "";
        String remoteAddress = REMOTE_ADDRESS;
        if (request != null) {
            url = request.getRequestURL() != null ? request.getRequestURL().toString() : "";
            remoteAddress = request.getRemoteAddr();
        }

        final JsonResponse jsonResponse;
        jsonResponse = new JsonResponse()
                .with("From", remoteAddress)
                .with("To", url)
                .with("Code", HttpStatus.BAD_REQUEST)
                .done();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<JsonResponse> handleMethodNotSupported(final HttpServletRequest request, final Exception e) {
        String url = "";
        String remoteAddress = REMOTE_ADDRESS;
        if (request != null) {
            url = request.getRequestURL() != null ? request.getRequestURL().toString() : "";
            remoteAddress = request.getRemoteAddr();
        }

        final JsonResponse jsonResponse;
        jsonResponse = new JsonResponse()
                .with("From", remoteAddress)
                .with("To", url)
                .with("Code", HttpStatus.BAD_REQUEST)
                .done();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).allow(HttpMethod.POST).body(jsonResponse);
    }
}

