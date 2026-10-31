package de.voteeasy.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebLanguageConfigurerTest {

    @Mock
    private HttpServletRequest request;

    @Test
    void verifyUnknownLanguageIsMappedToDefaultEnglish() {
        when(request.getLocale()).thenReturn(Locale.CHINA);

        final WebLanguageConfigurer.OnlyHardcodedLanguages languages = new WebLanguageConfigurer.OnlyHardcodedLanguages();
        assertThat(languages.resolveLocale(request)).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void configuredDefaultIsGermanEvenWithAnEnglishBrowser() {
        MockHttpServletRequest englishRequest = new MockHttpServletRequest();
        englishRequest.addPreferredLocale(Locale.ENGLISH);

        LocaleResolver resolver = new WebLanguageConfigurer().localeResolver();
        assertThat(resolver.resolveLocale(englishRequest)).isEqualTo(Locale.GERMAN);
    }

    @Test
    void explicitEnglishSelectionStillWorks() {
        MockHttpServletRequest germanRequest = new MockHttpServletRequest();
        germanRequest.addPreferredLocale(Locale.GERMAN);
        LocaleResolver resolver = new WebLanguageConfigurer().localeResolver();
        resolver.setLocale(germanRequest, new MockHttpServletResponse(), Locale.ENGLISH);

        assertThat(resolver.resolveLocale(germanRequest)).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void localeLookupDoesNotChangeTheGlobalDefault() {
        MockHttpServletRequest germanRequest = new MockHttpServletRequest();
        Locale before = LocaleContextHolder.getLocale();

        new WebLanguageConfigurer().localeResolver().resolveLocale(germanRequest);

        assertThat(LocaleContextHolder.getLocale()).isEqualTo(before);
    }

    @Test
    void germanIsProperlyMapped() {
        for (Locale l : Arrays.asList(Locale.GERMAN, Locale.GERMANY)) {
            when(request.getLocale()).thenReturn(l);

            assertThat(new WebLanguageConfigurer.OnlyHardcodedLanguages().resolveLocale(request)).isEqualTo(l);
        }
    }

}