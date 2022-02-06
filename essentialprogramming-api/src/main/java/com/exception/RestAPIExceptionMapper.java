package com.exception;

import com.util.enums.HTTPCustomStatus;
import com.util.exceptions.ApiException;
import com.util.web.JsonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * Custom ExceptionMapper to return an appropriate response to the client when
 * an api exception occurred.
 *
 */
@Order(400)
@ControllerAdvice
@Slf4j
public class RestAPIExceptionMapper {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<JsonResponse> toResponse(final HttpServletRequest request,
                                                   final ApiException exception) {

        // get request url and remote address
        String url = null;
        String remoteAddress = "unknown";
        if (request != null) {
            url = request.getRequestURL() != null ? request.getRequestURL().toString() : null;
            remoteAddress = request.getRemoteAddr();
        }

        final HTTPCustomStatus status = exception.getHttpCode() == null ? HTTPCustomStatus.INVALID_REQUEST : exception.getHttpCode();
        log.warn("Client request can not be processed ({}) from {} to {}. Business failure: {}", status,
                remoteAddress, url, exception.getMessage());

        final JsonResponse jsonResponse = new JsonResponse()
                .with("message", exception.getMessage())
                .with("status", status.value() + " (" + status + ")")
                .done();

        return ResponseEntity.status(status.value()).body(jsonResponse);
    }
}
