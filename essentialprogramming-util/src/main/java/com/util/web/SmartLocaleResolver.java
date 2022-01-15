package com.util.web;

import java.util.*;

import com.util.enums.Language;

/**
 * LocaleResolver implementation that  uses the  locale specified in the "accept-language" header of the HTTP request
 * (that is, the locale sent by the client browser, normally that of the client's OS) with a fallback on a locale set in cookies.
 *
 * @author Razvan Prichici
 */
public class SmartLocaleResolver {
    public final static List<Locale> acceptedLocales = Arrays.asList(
            Locale.ENGLISH,
            Locale.GERMAN,
            Locale.FRENCH,
            Locale.ITALIAN,
            new Locale("fr"),
            new Locale("ro"),
            new Locale("nl"));


    public static Language resolveLanguage(Locale locale) {
        return Language.fromLocaleString(Objects.requireNonNull(locale).getLanguage());
    }

}