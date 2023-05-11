package discord.automation.runners;

import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

@Testcontainers
public abstract class AbstractContainerRunner {

    private final static DockerComposeContainer<?> environment;

    static {
        environment = new DockerComposeContainer<>(getComposeFile())
                .withLocalCompose(true)
                .withOptions("--compatibility")
                .waitingFor("app", Wait.forHealthcheck());
        Startables.deepStart(environment).join();
    }

    private static File getComposeFile() {
        return Paths.get("target/docker/docker-compose.yml").toFile();
    }
}
