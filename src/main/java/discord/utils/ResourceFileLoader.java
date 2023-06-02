package discord.utils;

import discord.exception.main.FailedToLoadResourceFileException;
import discord.logging.LogMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;

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
