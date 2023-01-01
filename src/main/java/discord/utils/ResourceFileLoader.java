package discord.utils;

import discord.exception.FailedToLoadResourceFileException;
import discord.localisation.LogMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ResourceFileLoader {
    public static String loadFile(String path){
        try {
            final var inputStream = new ClassPathResource(path).getInputStream();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FailedToLoadResourceFileException(LogMessage.ALERT_20027);
        }
    }
}
