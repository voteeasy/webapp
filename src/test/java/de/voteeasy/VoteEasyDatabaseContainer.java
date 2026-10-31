package de.voteeasy;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
final class VoteEasyDatabaseContainer extends PostgreSQLContainer<VoteEasyDatabaseContainer> {
    private static final String IMAGE_VERSION = "postgres:16.9"; // keep in sync with version of live deployment
    private static VoteEasyDatabaseContainer container;

    private VoteEasyDatabaseContainer() {
        super(IMAGE_VERSION);
    }

    public static VoteEasyDatabaseContainer getInstance() {
        if (container == null) {
            container = new VoteEasyDatabaseContainer(); //.withReuse(true); // reuse triggers new shutdown hook as can be seen in start()
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("VE_DB_URL", container.getJdbcUrl());
        System.setProperty("VE_DB_USERNAME", container.getUsername());
        System.setProperty("VE_DB_PASSWORD", container.getPassword());
        log.info("Starting with URL: {}, USER: {}, PW: {}", container.getJdbcUrl(), container.getUsername(), container.getPassword());
        log.info("Finished starting our own PostgreSQL-container and set environment variables.");

        // make sure that containers will stop faster as Ryuk can be slow
        /*
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Docker container stop hook invoked.");
            container.stop();
        }));
         */
    }

    @Override
    public void stop() {
        // do nothing as JVM handles shut down
        // in order to avoid https://stackoverflow.com/questions/47141176/spring-boot-psqlexception-fatal-sorry-too-many-clients-already-when-running#54350770
        // super.stop();
    }
}