package com.api.controller;

import com.api.config.Anonymous;
import com.api.config.LanguageContextProvider;
import com.api.output.UserJSON;
import com.api.security.AllowUserIf;
import com.api.service.UserService;
import com.api.model.*;
import com.token.validation.auth.AuthUtils;
import com.util.enums.Language;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.annotation.security.RolesAllowed;
import java.security.GeneralSecurityException;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@Tag(description = "User API", name = "User Services")
@RestController
@RequestMapping("/v1/")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    private final LanguageContextProvider languageContextProvider;


    @PostMapping(value = "user/create", consumes = {"application/json"}, produces = {"application/json"})
    @Operation(summary = "Create user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Return user if successfully added",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserJSON.class)))
            })
    public ResponseEntity<UserJSON> createUser(@RequestBody UserInput userInput) {

        return new ResponseEntity<>(userService.createUser(userInput, languageContextProvider.getObject()), HttpStatus.CREATED);
    }


    @GetMapping(value = "user/load", consumes = {"application/json"}, produces = {"application/json"})
    @Operation(summary = "Load user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Return authenticated user if it was successfully found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserJSON.class)))
            })
    @RolesAllowed({"visitor", "administrator"})
    @AllowUserIf("hasAnyRole(@privilegeService.getPrivilegeRoles(\"LOAD.USER\")) OR hasAnyAuthority('PERMISSION_read:user', 'PERMISSION_edit:user') AND @userService.checkEmailExists(authentication.getPrincipal())")
    public ResponseEntity<UserJSON> load(@RequestHeader(name="Authorization") String authorization) {

        final String bearer = AuthUtils.extractBearerToken(authorization);
        final String email = AuthUtils.getClaim(bearer, "email");

        return new ResponseEntity<>(userService.loadUser(email,  languageContextProvider.getObject()), HttpStatus.OK);
    }
}