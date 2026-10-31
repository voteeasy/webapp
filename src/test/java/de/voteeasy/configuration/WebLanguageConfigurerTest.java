package de.voteeasy.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    void germanIsProperlyMapped() {
        for (Locale l : Arrays.asList(Locale.GERMAN, Locale.GERMANY)) {
            when(request.getLocale()).thenReturn(l);

            assertThat(new WebLanguageConfigurer.OnlyHardcodedLanguages().resolveLocale(request)).isEqualTo(l);
        }
    }

}