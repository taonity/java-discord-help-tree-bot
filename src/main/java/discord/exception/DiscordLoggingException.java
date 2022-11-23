package discord.exception;

import discord.localisation.LogMessage;
import discord.services.DiscordMessageService;
import discord.services.MessageChannelService;
import discord.structure.ChannelRole;
import discord.structure.EmbedBuilder;
import discord.structure.ErrorEmbedType;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static discord.structure.EmbedBuilder.LOG_ATTACHMENT_FILE_NAME;

public abstract class DiscordLoggingException extends RuntimeException {
    public DiscordLoggingException(LogMessage logMessage, MessageChannelService messageChannelService, ErrorEmbedType errorEmbedType) {
        super(logMessage.toString());

        final var outputStream = new ByteArrayOutputStream();
        final var printStream = new PrintStream(outputStream);
        printStackTrace(printStream);
        var inputStream = new ByteArrayInputStream(outputStream.toString().getBytes(StandardCharsets.UTF_8));

        messageChannelService.getChannel(ChannelRole.LOG).createMessage(
                MessageCreateSpec.builder()
                        .addEmbed(EmbedBuilder.buildLogEmbed(logMessage, errorEmbedType))
                        .addFile(LOG_ATTACHMENT_FILE_NAME, inputStream)
                        .build()
        ).subscribe();
    }
}
