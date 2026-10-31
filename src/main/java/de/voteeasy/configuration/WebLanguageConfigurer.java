package de.voteeasy.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

@Configuration
public class WebLanguageConfigurer implements WebMvcConfigurer {
    private static final String LANG_PARAM = "lang";

    @Bean
    LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName(LANG_PARAM);
        interceptor.setIgnoreInvalidLocale(true);
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Bean
    public LocaleResolver localeResolver() {
        OnlyHardcodedLanguages resolver = new OnlyHardcodedLanguages();
        // Security redirects and logout can discard the lang parameter/session.
        // Use the same default on the host and in Docker; lang=en still overrides it.
        resolver.setDefaultLocale(Locale.GERMAN);
        return resolver;
    }

    @Slf4j
    static class OnlyHardcodedLanguages extends SessionLocaleResolver {
        private final List<Locale> allowedLocales = List.of(Locale.GERMAN, Locale.GERMANY);

        @Override
        public Locale resolveLocale(@NonNull HttpServletRequest request) {
            Locale locale = super.resolveLocale(request);
            log.debug("Language seen: {}", locale);
            if (!allowedLocales.contains(locale)) {
                //return determineDefaultLocale(request); - does not properly fallback to the i18n.properties file without language
                log.debug("Fallback to EN as default");
                return Locale.ENGLISH;
            }
            return locale;

        }
    }

}
