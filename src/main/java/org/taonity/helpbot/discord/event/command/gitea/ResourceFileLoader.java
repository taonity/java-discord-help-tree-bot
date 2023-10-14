package org.taonity.helpbot.discord.event.command.gitea;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.FailedToLoadResourceFileException;

public class ResourceFileLoader {
    public static String loadFile(String path) {
        try {
            final var inputStream = new ClassPathResource(path).getInputStream();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FailedToLoadResourceFileException(LogMessage.ALERT_20027);
        }
    }
}
