package discord.automation.runners;

import static org.testcontainers.shaded.org.apache.commons.lang3.SystemUtils.*;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startables;

public abstract class AbstractContainerRunner {

    private static final DockerComposeContainer<?> environment;
    static Logger log = LoggerFactory.getLogger("automation-tests");

    static {
        if (IS_OS_WINDOWS) {
            environment = new DockerComposeContainer<>(getComposeFile())
                    .withLocalCompose(true)
                    .withOptions("--compatibility");
        } else if (IS_OS_UNIX) {
            environment = new DockerComposeContainer<>(getComposeFile()).withLocalCompose(true);
        } else {
            throw new RuntimeException(String.format("Unknown os encountered: %s", OS_NAME));
        }
        environment
                .withLogConsumer(
                        "app", new Slf4jLogConsumer(log).withPrefix("app-1").withSeparateOutputStreams())
                .withLogConsumer(
                        "gitea", new Slf4jLogConsumer(log).withPrefix("gitea-1").withSeparateOutputStreams())
                .withLogConsumer(
                        "db", new Slf4jLogConsumer(log).withPrefix("db-1").withSeparateOutputStreams())
                .waitingFor("app", Wait.forHealthcheck().withStartupTimeout(Duration.ofSeconds(1000)));
        Startables.deepStart(environment).join();
    }

    // TODO: Without this test db beans are being created without container being riced
    @Test
    void dummyTest() {}

    private static File getComposeFile() {
        final var file = Paths.get("target/docker/test/docker-compose-test.yml").toFile();
        log.info("Trying to open compose file with path: {}", file.getAbsolutePath());
        return file;
    }
}
