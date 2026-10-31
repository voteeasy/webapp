package de.voteeasy;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class VoteEasyApplicationTests extends AbstractIT {

    @Test
    void connectionToTestcontainersDatabaseHasBeenEstabilshed() {
        assertThat(postgreSQLContainer).isNotNull();
    }

    @Test
    void contextLoads() {
        log.info("Context loaded OK");
    }
}
