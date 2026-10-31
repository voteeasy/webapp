package de.voteeasy.selfservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class CredentialsGenerator {
    private static final int LENGTH = 6;

    private static byte[] generateSecureRandomBytes() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[LENGTH];
        secureRandom.nextBytes(randomBytes);
        return randomBytes;
    }

    private static String hashWithSHA256(byte[] inputBytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(inputBytes);
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

    public static String generateSecureRandomString() {
        try {
            return hashWithSHA256(generateSecureRandomBytes()).substring(0, LENGTH);
        } catch (NoSuchAlgorithmException | IndexOutOfBoundsException e) {
            log.error("Unable to generate secure credentials", e);
            return "ChangeMeImmediately";
        }
    }

    /**
     * Hashes the given input with BCRYPT.
     *
     * @param input value to encrypt/hash.
     * @return properly hashed value of the given input, prefixed to be bcrypt encoded.
     * @throws IllegalArgumentException in case the given input is null.
     */
    public static String asBcryptHash(@NonNull String input) {
        return "{bcrypt}" + new BCryptPasswordEncoder().encode(input);
    }

}
