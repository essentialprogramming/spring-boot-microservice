package com.util.web;

import java.util.*;

import com.util.text.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

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
    public Locale resolveLocale(HttpServletRequest request) {
        if (StringUtils.isEmpty(request.getHeader("Accept-Language"))) {
            return Locale.getDefault();
        }

        List<Locale.LanguageRange> localeList = Locale.LanguageRange.parse(request.getHeader("Accept-Language"));

        return Locale.lookup(localeList, acceptedLocales);
    }

}