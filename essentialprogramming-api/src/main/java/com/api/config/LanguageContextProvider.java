package com.api.config;

import com.util.enums.Language;
import com.util.web.SmartLocaleResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;


@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LanguageContextProvider implements FactoryBean<Language> {

    private final HttpHeaders httpHeaders;

    @Override
    public Language getObject() throws Exception {
        return SmartLocaleResolver.resolveLanguage(httpHeaders);
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }
}
