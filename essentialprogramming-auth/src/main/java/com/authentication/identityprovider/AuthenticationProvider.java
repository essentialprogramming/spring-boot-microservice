package com.authentication.identityprovider;


import com.authentication.identityprovider.internal.entities.Account;
import com.authentication.identityprovider.internal.model.PasswordInput;
import com.authentication.identityprovider.internal.model.ResetPasswordInput;
import com.authentication.request.AuthRequest;
import com.util.password.PasswordException;
import com.util.enums.Language;
import com.util.exceptions.ApiException;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.Locale;

public interface AuthenticationProvider {

    Account authenticate(AuthRequest authRequest, Locale locale) throws ApiException;
    Serializable generateOtp(String email, Locale locale) throws ApiException;

    Serializable resetPassword(ResetPasswordInput resetPasswordInput, Locale locale) throws ApiException, GeneralSecurityException;
    Serializable setPassword(PasswordInput passwordInput, Locale locale) throws GeneralSecurityException, ApiException, PasswordException;

}
