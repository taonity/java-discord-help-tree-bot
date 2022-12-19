package discord.exception;

import discord.localisation.LogMessage;
import discord.services.MessageChannelService;
import discord.structure.EmbedType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public abstract class DiscordLoggingException extends RuntimeException {
    public DiscordLoggingException(String message, MessageChannelService messageChannelService, EmbedType embedType) {
        super(message);

        final var outputStream = new ByteArrayOutputStream();
        final var printStream = new PrintStream(outputStream);
        printStackTrace(printStream);
        var inputStream = new ByteArrayInputStream(outputStream.toString().getBytes(StandardCharsets.UTF_8));

        /*messageChannelService.getChannel(ChannelRole.LOG).createMessage(
                MessageCreateSpec.builder()
                        .addEmbed(EmbedBuilder.buildLogEmbed(message, errorEmbedType))
                        .addFile(LOG_ATTACHMENT_FILE_NAME, inputStream)
                        .build()
        ).subscribe();*/
    }
    public DiscordLoggingException(LogMessage logMessage, MessageChannelService messageChannelService, EmbedType embedType) {
        this(logMessage.name(), messageChannelService, embedType);
    }
}
