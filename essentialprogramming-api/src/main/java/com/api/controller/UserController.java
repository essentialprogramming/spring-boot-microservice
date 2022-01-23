package com.api.controller;

import com.api.config.Anonymous;
import com.api.model.UserInput;
import com.api.output.Response;
import com.api.output.UserJSON;
import com.api.service.UserService;
import com.exception.ExceptionHandler;
import com.internationalization.Messages;
import com.token.validation.auth.AuthUtils;
import com.util.async.Computation;
import com.util.async.ExecutorsProvider;
import com.util.enums.HTTPCustomStatus;
import com.util.exceptions.ApiException;
import com.util.web.SmartLocaleResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.Locale;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;


@Tag(description = "User API", name = "User Services")
@RequestMapping("/v1/")
@RestController
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    private final SmartLocaleResolver smartLocaleResolver;


    @PostMapping(value = "user/create", consumes = {"application/json"}, produces = {"application/json"})
    @Operation(summary = "Create user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Return user if successfully added",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserJSON.class)))
            })
    @Anonymous
    public ResponseEntity<Serializable> createUser(@RequestBody @Valid UserInput userInput, HttpServletRequest request) throws GeneralSecurityException {

        ExecutorService executorService = ExecutorsProvider.getExecutorService();
        return Computation.computeAsync(() -> createUser(userInput, smartLocaleResolver.resolveLocale(request)), executorService)
                .thenApplyAsync(Response::created, executorService)
                .exceptionally(error -> ExceptionHandler.handleException((CompletionException) error))
                .join();
    }

    private Serializable createUser(UserInput userInput, Locale language) throws GeneralSecurityException, ApiException {
        boolean isValid = userService.checkAvailabilityByEmail(userInput.getEmail());
        if (!isValid) {
            throw new ApiException(Messages.get("EMAIL.ALREADY.TAKEN", language), HTTPCustomStatus.INVALID_REQUEST);
        }
        return userService.createUser(userInput, language);
    }


    @GetMapping(value = "user/load", produces = {"application/json"})
    @Operation(summary = "Load user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Return authenticated user if it was successfully found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserJSON.class)))
            })
    @RolesAllowed({"visitor", "administrator"})
    @PreAuthorize("hasAnyRole(@privilegeService.getPrivilegeRoles(\"LOAD.USER\")) AND hasAnyAuthority('PERMISSION_read:user', 'PERMISSION_edit:user')")
    public ResponseEntity<Serializable> load(@RequestHeader(name = "Authorization", required = false) String authorization,
                                             HttpServletRequest request) {

        final String bearer = AuthUtils.extractBearerToken(authorization);
        final String email = AuthUtils.getClaim(bearer, "email");

        ExecutorService executorService = ExecutorsProvider.getExecutorService();
        return Computation.computeAsync(() -> loadUser(email, smartLocaleResolver.resolveLocale(request)), executorService)
                .thenApplyAsync(Response::ok, executorService)
                .exceptionally(error -> ExceptionHandler.handleException((CompletionException) error))
                .join();
    }

    private Serializable loadUser(String email, Locale language) throws ApiException {
        return userService.loadUser(email, language);
    }

}