package com.authentication.controller;

import com.authentication.channel.AccessChannel;
import com.authentication.identityprovider.internal.model.PasswordInput;
import com.authentication.request.AuthRequest;
import com.authentication.response.AccessToken;
import com.authentication.service.AuthenticationService;
import com.token.validation.auth.AuthUtils;
import com.util.exceptions.ApiException;
import com.util.web.JsonResponse;
import com.util.web.SessionUtils;
import com.util.web.SmartLocaleResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Tag(description = "Authorization API", name = "Authorization")
public class AuthenticationController {


    private final AuthenticationService authenticationService;

    private final SmartLocaleResolver smartLocaleResolver;


    @PostMapping(value = "/token", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Authenticate user, return JWT token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns JWT token if user successfully authenticated",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AccessToken.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JsonResponse.class))),
            })
    public ResponseEntity<AccessToken> authenticate(@RequestBody AuthRequest tokenRequest, HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authenticationService.authenticate(tokenRequest, AccessChannel.PASSWORD, smartLocaleResolver.resolveLocale(request)));
    }


    @PostMapping(value = "/authenticate", consumes = "application/json", produces = "application/json")
    @Operation(hidden = true)
    public ResponseEntity<JsonResponse> authenticate(@RequestParam("redirect_uri") String redirectUri,
                                     @RequestBody AuthRequest tokenRequest,
                                     HttpServletRequest httpRequest) {

        try {
            AccessToken accessToken = authenticationService.authenticate(tokenRequest, AccessChannel.PASSWORD, smartLocaleResolver.resolveLocale(httpRequest));

            if (accessToken != null) {

                final String email = AuthUtils.getClaim(accessToken.getAccessToken(), "email");
                SessionUtils.setAttribute(httpRequest, "email", email);

                JsonResponse response =  new JsonResponse()
                        .with("status", "Redirect")
                        .with("redirectUrl", redirectUri).done();

                return ResponseEntity.ok()
                        .body(response);

            } else {
                JsonResponse response = new JsonResponse()
                        .with("status", "Error")
                        .with("error", "The username or password you entered is incorrect.").done();

                return ResponseEntity.status(403)
                        .body(response);
            }

        } catch (ApiException exception) {
            JsonResponse response = new JsonResponse()
                    .with("status", "Error")
                    .with("error", exception.getMessage()).done();

            return ResponseEntity.status(422)
                    .body(response);
        }

    }


    @PostMapping(value = "/password/set", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Set password",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns ok if password was set successfully. ",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JsonResponse.class)))
            })
    public ResponseEntity<Serializable> setPassword(@RequestBody PasswordInput passwordInput, HttpServletRequest request) {

        Serializable response;

        try {
            response = authenticationService.setPassword(passwordInput, smartLocaleResolver.resolveLocale(request));

            return ResponseEntity.ok()
                    .body(response);

        } catch (Exception e) {
            response = new JsonResponse()
                    .with("status", "422")
                    .with("message", e.getMessage())
                    .done();

            return ResponseEntity.status(422)
                    .body(response);
        }
    }


    @PostMapping(value = "otp", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Authenticate with otp, return JWT token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns JWT token if user successfully authenticated",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AccessToken.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JsonResponse.class))),
            })
    public ResponseEntity<AccessToken> otpLogin(@RequestBody AuthRequest tokenRequest, HttpServletRequest request) {

        return ResponseEntity.ok()
                .body(authenticationService.authenticate(tokenRequest, AccessChannel.OTP, smartLocaleResolver.resolveLocale(request)));
    }


    @PostMapping(value = "otp/generate", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Generate OTP",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns 'status ok' if successfully generated",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AccessToken.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = JsonResponse.class))),
            })
    public ResponseEntity<Serializable> generateOtp(@RequestParam("email") String email, HttpServletRequest request) {

        return ResponseEntity.ok()
                .body(authenticationService.generateOtp(email, smartLocaleResolver.resolveLocale(request)));
    }
}
