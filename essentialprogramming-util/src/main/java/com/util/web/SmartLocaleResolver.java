package com.util.web;

import java.util.*;

import com.util.enums.Language;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpHeaders;

/**
 * LocaleResolver implementation that  uses the  locale specified in the "accept-language" header of the HTTP request
 * (that is, the locale sent by the client browser, normally that of the client's OS) with a fallback on a locale set in cookies.
 *
 * @author Razvan Prichici
 */
public class SmartLocaleResolver {
    final static List<Locale> acceptedLocales = Arrays.asList(
            Locale.ENGLISH,
            Locale.GERMAN,
            Locale.FRENCH,
            Locale.ITALIAN,
            new Locale("fr"),
            new Locale("ro"),
            new Locale("nl"));

    public static Locale resolveLocale(HttpHeaders headers) {
        Locale defaultLanguage;
        List<Locale> locales;
        try {
            locales = headers.getAcceptLanguageAsLocales();
            defaultLanguage = locales.get(0);
        } catch (Exception e) {

            List<String> cookies = headers.get(HttpHeaders.SET_COOKIE);

            System.out.println("a");
         /*   defaultLanguage = cookies.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase("Accept-Language"))
                    .map(Map.Entry::getValue)
                    .map(Cookie::getValue)
                    .map(Locale.LanguageRange::parse)
                    .map(range -> Locale.lookup(range, acceptedLocales))
                    .findFirst()
                    .orElse(Locale.ENGLISH);*/
        }
        //return defaultLanguage;
        return null;
    }

    public static Language resolveLanguage(HttpHeaders headers) {
        Locale defaultLanguage = resolveLocale(headers);
        return Language.fromLocaleString(Objects.requireNonNull(defaultLanguage).getLanguage());
    }

}