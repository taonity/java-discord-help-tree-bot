package discord.exception.handling;

import static discord.structure.EmbedBuilder.LOG_ATTACHMENT_FILE_NAME;

import discord.exception.*;
import discord.exception.client.ClientGuildAwareException;
import discord.exception.main.MainGuildAwareException;
import discord.logging.LogMessage;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord.services.MessageChannelService;
import discord.structure.ChannelRole;
import discord.structure.EmbedBuilder;
import discord.structure.EmbedType;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.MessageCreateSpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    @Value("${discord.mainGuildId}")
    private String mainGuildId;

    private final GatewayDiscordClient gatewayDiscordClient;
    private final MessageChannelService messageChannelService;
    private final GuildSettingsRepository guildSettingsRepository;

    @AfterThrowing(value = "(execution(* discord..*..*(..)))", throwing = "exception")
    public void logForClientGuild(ClientGuildAwareException exception) {
        final var guildId = exception.getGuildId();

        getLogChannelId(exception, guildId).ifPresent(guildSettings -> logException(exception, guildId));
    }

    @AfterThrowing(value = "(execution(* discord..*..*(..)))", throwing = "exception")
    public void logForMainGuild(MainGuildAwareException exception) {
        getLogChannelId(exception, mainGuildId)
                .ifPresentOrElse(guildSettings -> logException(exception, mainGuildId), () -> {
                    throw new AspectEmptyOptionalException(LogMessage.ALERT_20076, exception);
                });
    }

    private Optional<String> getLogChannelId(LogMessageException exception, String guildId) {
        final var logChannelId = guildSettingsRepository
                .findGuildSettingByGuildId(guildId)
                .map(GuildSettings::getLogChannelId)
                .orElseThrow(() -> new AspectEmptyOptionalException(LogMessage.ALERT_20074, exception));
        return Optional.ofNullable(logChannelId);
    }

    private void logException(LogMessageException exception, String guildId) {
        final var outputStream = new ByteArrayOutputStream();
        final var printStream = new PrintStream(outputStream);
        exception.printStackTrace(printStream);
        final var inputStream = new ByteArrayInputStream(outputStream.toString().getBytes(StandardCharsets.UTF_8));

        final var messageSpecs = MessageCreateSpec.builder()
                .addEmbed(EmbedBuilder.buildMessageEmbed(exception.getMessage(), EmbedType.EXPECTED_EMBED_TYPE))
                .addFile(LOG_ATTACHMENT_FILE_NAME, inputStream)
                .build();

        gatewayDiscordClient
                .getGuildById(Snowflake.of(guildId))
                .blockOptional()
                .map(guild -> messageChannelService.getChannel(guild, ChannelRole.LOG))
                .orElseThrow(() -> new AspectEmptyOptionalException(LogMessage.ALERT_20045, exception))
                .createMessage(messageSpecs)
                .block();
    }
}
