package com.util.web;

import java.util.*;

import com.util.enums.Language;
import com.util.text.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;


/**
 * LocaleResolver implementation that  uses the  locale specified in the "accept-language" header of the HTTP request
 * (that is, the locale sent by the client browser, normally that of the client's OS) with a fallback on a locale set in cookies.
 *
 * @author Razvan Prichici
 */
@Component
public class SmartLocaleResolver extends AcceptHeaderLocaleResolver {

    public final static List<Locale> acceptedLocales = Arrays.asList(
            Locale.ENGLISH,
            Locale.GERMAN,
            Locale.FRENCH,
            Locale.ITALIAN,
            new Locale("fr"),
            new Locale("ro"),
            new Locale("nl"));

    @Override
    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
        if (StringUtils.isEmpty(httpServletRequest.getHeader("Accept-Language"))) {
            return Locale.getDefault();
        }
        Locale defaultLanguage;
        try {
            defaultLanguage = Optional.of(httpServletRequest)
                    .map(request -> request.getHeader("Accept-Language"))
                    .map(Locale.LanguageRange::parse)
                    .map(range -> Locale.lookup(range, SmartLocaleResolver.acceptedLocales))
                    .orElse(Locale.GERMAN);
        } catch (Exception e) {
            Cookie[] cookies = httpServletRequest.getCookies();
            defaultLanguage = Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equalsIgnoreCase("Accept-Language"))
                    .map(Cookie::getValue)
                    .map(Locale.LanguageRange::parse)
                    .map(range -> Locale.lookup(range, acceptedLocales))
                    .findFirst()
                    .orElse(Locale.ENGLISH);
        }
        return defaultLanguage;
    }

    public Language resolveLanguage(HttpServletRequest httpServletRequest) {
        return Language.fromLocaleString(Objects.requireNonNull(resolveLocale(httpServletRequest)).getLanguage());
    }

}