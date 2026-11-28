package de.voteeasy.seeding;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class CredentialsPrinterTest {

    @Test
    void mainWithoutParamsNoError() {
        assertThatCode(() -> CredentialsPrinter.main(null)).doesNotThrowAnyException();
    }

    @Test
    void mainWithEmptyParamsNoError() {
        assertThatCode(() -> CredentialsPrinter.main(new String[]{})).doesNotThrowAnyException();
    }

    @Test
    void mainWithParamsNoError() {
        assertThatCode(() -> CredentialsPrinter.main(new String[]{"Eene", "meene", "mopel."})).doesNotThrowAnyException();
    }
}