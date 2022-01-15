package com.api.config;

import com.util.enums.Language;
import com.util.web.SmartLocaleResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;
import java.util.Optional;


@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LanguageContextProvider implements FactoryBean<Language> {

    @Override
    public Language getObject() {
        return SmartLocaleResolver.resolveLanguage(getLocale());
    }

    @Override
    public Class<?> getObjectType() {
        return Language.class;
    }

    public static Locale getLocale() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(request-> request.getHeader("Accept-Language"))
                .map(Locale.LanguageRange::parse)
                .map(range -> Locale.lookup(range, SmartLocaleResolver.acceptedLocales))
                .orElse(Locale.ENGLISH);
    }
}
