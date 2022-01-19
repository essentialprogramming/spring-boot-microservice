package com.api.service;

import com.api.entities.*;
import com.api.env.resources.AppResources;
import com.api.mapper.UserMapper;
import com.api.model.*;
import com.api.output.UserJSON;
import com.api.repository.*;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.crypto.Crypt;
import com.crypto.PasswordHash;
import com.api.template.Templates;
import com.email.service.EmailManager;
import com.google.inject.internal.util.ImmutableMap;
import com.internationalization.EmailMessages;
import com.internationalization.Messages;
import com.util.enums.HTTPCustomStatus;
import com.util.exceptions.ApiException;
//import org.jboss.weld.util.collections.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.GeneralSecurityException;
import java.util.*;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    private final UserRepository userRepository;
    private final EmailManager emailManager;

    @Autowired
    public UserService(UserRepository userRepository, EmailManager emailManager) {

        this.userRepository = userRepository;
        this.emailManager = emailManager;
    }

    @Transactional
    public UserJSON createUser(UserInput input, Locale locale) throws GeneralSecurityException {

        boolean isValid = checkAvailabilityByEmail(input.getEmail());
        if (!isValid)
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "EMAIL.ALREADY.TAKEN");

        final User user = UserMapper.inputToUser(input);
        final User result = saveUser(user, input);

        String validationKey = Crypt.encrypt(NanoIdUtils.randomNanoId(), AppResources.ENCRYPTION_KEY.value());
        String encryptedUserKey = Crypt.encrypt(result.getUserKey(), AppResources.ENCRYPTION_KEY.value());

        String url = AppResources.ACCOUNT_CONFIRMATION_URL.value() + "/" + validationKey + "/" + encryptedUserKey;

        Map<String, Object> templateVariables = ImmutableMap.<String, Object>builder()
                .put("fullName", result.getFullName())
                .put("confirmationLink", url)
                .build();

        emailManager.send(result.getEmail(), EmailMessages.get("new_user.subject", locale), Templates.NEW_USER, templateVariables, locale);

        return UserMapper.userToJson(result);

    }

    @Transactional
    public boolean checkAvailabilityByEmail(String email) {

        Optional<User> user = userRepository.findByEmail(email);
        return !user.isPresent();
    }

    @Transactional
    public boolean checkEmailExists(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.isPresent();
    }


    @Transactional
    public UserJSON loadUser(String email, Locale locale) throws ApiException {

        final User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ApiException(Messages.get("USER.NOT.EXIST", locale), HTTPCustomStatus.UNAUTHORIZED)
        );

        logger.info("User with email={} loaded", email);
        return UserMapper.userToJson(user);

    }


    private User saveUser(User user, UserInput input) {

        String uuid = NanoIdUtils.randomNanoId();
        user.setUserKey(uuid);

        userRepository.save(user);
        if (user.getId() > 0) {
            logger.debug("Start password hashing");
            String password = PasswordHash.encode(input.getPassword());
            logger.debug("Finished password hashing");

            user.setPassword(password);
        }

        return user;
    }

}
