package com.authentication.exception;

import com.util.exceptions.ApiException;
import com.util.web.JsonResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

public class ExceptionHandler {

    @FunctionalInterface
    interface Strategy<T> {
        ResponseEntity<JsonResponse> getResponse(T exception);
    }

    private final static Strategy<ApiException> apiExceptionStrategy = (exception) -> {
        JsonResponse jsonResponse;
        jsonResponse = new JsonResponse()
                .with("message", exception.getMessage())
                .with("code", exception.getHttpCode())
                .done();
        return ResponseEntity
                .status(exception.getHttpCode().value())
                .body(jsonResponse);
    };

    private final static Strategy<HttpClientErrorException> httpClientErrorException = (exception) -> {
        JsonResponse jsonResponse;
        jsonResponse = new JsonResponse()
                .with("message", exception.getMessage())
                .with("code", exception.getStatusCode().value())
                .done();

        return ResponseEntity
                .status(exception.getStatusCode().value())
                .body(jsonResponse);
    };

    private final static Strategy<Throwable> defaultStrategy = (exception) -> {
        JsonResponse jsonResponse;
        exception.printStackTrace();
        jsonResponse = new JsonResponse()
                .with("message", "INTERNAL SERVER ERROR")
                .with("code", HttpStatus.INTERNAL_SERVER_ERROR)
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
