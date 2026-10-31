package de.voteeasy.selfservice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialsGeneratorTest {

    @Test
    void generateSecureRandomString() {
        assertThat(CredentialsGenerator.generateSecureRandomString()).isNotNull();
    }

    @Test
    void asBcryptHashAddsPrefixCorrectly() {
        assertThat(CredentialsGenerator.asBcryptHash("test")).startsWith("{bcrypt}");
        assertThat(CredentialsGenerator.asBcryptHash("test")).hasSizeGreaterThanOrEqualTo(10);
    }
}