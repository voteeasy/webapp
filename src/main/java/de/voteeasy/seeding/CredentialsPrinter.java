package de.voteeasy.seeding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Slf4j
public final class CredentialsPrinter {
    // run in order to see passwords and their hashes in case you add new users or change existing passwords
    public static void main(String[] args) {
        if (args != null && args.length >= 1) {
            for (String arg : args) {
                log.warn("'{}' results in '{}'", arg, new BCryptPasswordEncoder().encode(arg));
            }
            log.warn("HINT! you need to add a prefix '{bcrypt}'");
        }
    }

}
