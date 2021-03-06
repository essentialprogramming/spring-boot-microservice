package com.exception;

import com.util.enums.HTTPCustomStatus;
import com.util.exceptions.ApiException;
import com.util.web.JsonResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

@Slf4j
public class ExceptionHandler {

    @FunctionalInterface
    interface Strategy<T> {
        ResponseEntity<JsonResponse> getResponse(T exception);
    }

    private final static Strategy<ApiException> apiExceptionStrategy = (exception) -> {
        final HTTPCustomStatus status = exception.getHttpCode() == null ? HTTPCustomStatus.INVALID_REQUEST : exception.getHttpCode();
        final JsonResponse jsonResponse = new JsonResponse()
                .with("message", exception.getMessage())
                .with("status", status.value() + " (" + status + ")")
                .done();

        log.warn("Client request can not be processed ({}). Business failure: {}", status, exception.getMessage());

        return ResponseEntity
                .status(exception.getHttpCode().value())
                .body(jsonResponse);
    };

    private final static Strategy<HttpClientErrorException> httpClientErrorException = (exception) -> {
        final JsonResponse jsonResponse = new JsonResponse()
                .with("message", exception.getMessage())
                .with("status", exception.getStatusCode().value() + " (" + exception.getStatusCode().getReasonPhrase() + ")")
                .done();
        log.warn("Client request can not be processed ({}). Business failure: {}", exception.getStatusCode(), exception.getMessage());
        return ResponseEntity
                .status(exception.getStatusCode().value())
                .body(jsonResponse);
    };

    private final static Strategy<Throwable> defaultStrategy = (exception) -> {
        final JsonResponse jsonResponse = new JsonResponse()
                .with("message", "INTERNAL SERVER ERROR")
                .with("status", HttpStatus.INTERNAL_SERVER_ERROR.value() + " (" + HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() + ")")
                .with("exception", ExceptionUtils.getStackTrace(exception))
                .with("rootCause", ExceptionUtils.getRootCauseStackTrace(exception))
                .done();

        return ResponseEntity
                .internalServerError()
                .body(jsonResponse);
    };


    private final static Map<Class<? extends Throwable>, ExceptionHandler.Strategy<? extends Throwable>> strategiesMap = new HashMap<>();
    static {
        strategiesMap.put(ApiException.class, apiExceptionStrategy);
        strategiesMap.put(HttpClientErrorException.class, httpClientErrorException);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> ResponseEntity<T> handleException(CompletionException completionException) {

        Strategy strategy = strategiesMap.getOrDefault(completionException.getCause().getClass(), defaultStrategy);
        return strategy.getResponse(completionException.getCause());
    }

}
