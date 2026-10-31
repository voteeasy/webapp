package de.voteeasy;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// ?@ContextConfiguration(initializers = AbstractIT.DockerPostgresDataSourceInitializer.class)
@Testcontainers
@AutoConfigureMockMvc
public abstract class AbstractIT {
    @Container
    public static final VoteEasyDatabaseContainer postgreSQLContainer = VoteEasyDatabaseContainer.getInstance();

}
