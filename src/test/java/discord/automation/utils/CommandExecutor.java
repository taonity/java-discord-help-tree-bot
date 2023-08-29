package discord.automation.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.testcontainers.shaded.org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CommandExecutor {
    final StringBuilder stringBuffer = new StringBuilder();

    public String executeCommand(String command) {
        stringBuffer.setLength(0);
        final var executorService = Executors.newFixedThreadPool(2);
        Process process;
        try {
            if (IS_OS_WINDOWS) {
                process = Runtime.getRuntime().exec(String.format("wsl %s", command));
            } else {
                process = Runtime.getRuntime().exec(String.format("/bin/sh -c %s", command));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final var inputStreamFuture =
                CompletableFuture.runAsync(() -> consumeStream(process.getInputStream(), consumer), executorService);

        final var errorStreamFuture =
                CompletableFuture.runAsync(() -> consumeStream(process.getErrorStream(), consumer), executorService);

        final var combinedFuture = CompletableFuture.allOf(inputStreamFuture, errorStreamFuture);

        assertDoesNotThrow(() -> combinedFuture.get(10, TimeUnit.SECONDS));

        try {
            var exitCode = process.waitFor();
            assertEquals(0, exitCode);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return stringBuffer.toString();
    }

    private static void consumeStream(InputStream inputStream, Consumer<String> consumer) {
        new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
    }

    private final Consumer<String> consumer = line -> {
        stringBuffer.append("\n").append(line);
    };
}
