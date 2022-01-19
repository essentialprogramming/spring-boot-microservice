package com.authentication.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "Authentication", description = "An authentication object that contains the user credentials.")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthRequest implements TokenRequest {

    private String email;
    private String password;

}

