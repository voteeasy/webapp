package de.voteeasy.seeding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CredentialsPrinterTest {

    @Test
    void mainWithoutParamsNoError() {
        assertDoesNotThrow(() -> CredentialsPrinter.main(null));
    }

    @Test
    void mainWithEmptyParamsNoError() {
        assertDoesNotThrow(() -> CredentialsPrinter.main(new String[]{}));
    }

    @Test
    void mainWithParamsNoError() {
        assertDoesNotThrow(() -> CredentialsPrinter.main(new String[]{"Eene", "meene", "mopel."}));
    }
}