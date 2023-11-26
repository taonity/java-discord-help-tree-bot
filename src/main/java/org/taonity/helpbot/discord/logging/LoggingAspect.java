package org.taonity.helpbot.discord.logging;

import static org.taonity.helpbot.discord.embed.EmbedBuilder.LOG_ATTACHMENT_FILE_NAME;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.MessageCreateSpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.ChannelRole;
import org.taonity.helpbot.discord.GuildSettings;
import org.taonity.helpbot.discord.GuildSettingsRepository;
import org.taonity.helpbot.discord.MessageChannelService;
import org.taonity.helpbot.discord.embed.EmbedBuilder;
import org.taonity.helpbot.discord.embed.EmbedType;
import org.taonity.helpbot.discord.logging.exception.AspectEmptyOptionalException;
import org.taonity.helpbot.discord.logging.exception.LogMessageException;
import org.taonity.helpbot.discord.logging.exception.client.ClientGuildAwareException;
import org.taonity.helpbot.discord.logging.exception.main.MainGuildAwareException;
import reactor.core.publisher.Mono;

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

        getLogChannelId(exception, guildId)
                .switchIfEmpty(Mono.empty())
                .flatMap(guildSettings -> logException(exception, guildId))
                .subscribe();
    }

    @AfterThrowing(value = "(execution(* discord..*..*(..)))", throwing = "exception")
    public void logForMainGuild(MainGuildAwareException exception) {
        getLogChannelId(exception, mainGuildId)
                .switchIfEmpty(Mono.error(new AspectEmptyOptionalException(LogMessage.ALERT_20076, exception)))
                .flatMap(guildSettings -> logException(exception, mainGuildId))
                .subscribe();
    }

    private Mono<String> getLogChannelId(LogMessageException exception, String guildId) {
        return guildSettingsRepository
                .findGuildSettingByGuildId(guildId)
                .switchIfEmpty(Mono.error(new AspectEmptyOptionalException(LogMessage.ALERT_20074, exception)))
                .map(GuildSettings::getLogChannelId);
    }

    private Mono<Void> logException(LogMessageException exception, String guildId) {
        final var outputStream = new ByteArrayOutputStream();
        final var printStream = new PrintStream(outputStream);
        exception.printStackTrace(printStream);
        final var inputStream = new ByteArrayInputStream(outputStream.toString().getBytes(StandardCharsets.UTF_8));

        final var messageSpecs = MessageCreateSpec.builder()
                .addEmbed(EmbedBuilder.buildMessageEmbed(exception.getMessage(), EmbedType.EXPECTED_EMBED_TYPE))
                .addFile(LOG_ATTACHMENT_FILE_NAME, inputStream)
                .build();

        return gatewayDiscordClient
                .getGuildById(Snowflake.of(guildId))
                .switchIfEmpty(Mono.error(new AspectEmptyOptionalException(LogMessage.ALERT_20045, exception)))
                .flatMap(guild -> messageChannelService.getChannel(guild, ChannelRole.LOG))
                .flatMap(messageChannel -> messageChannel.createMessage(messageSpecs))
                .then();
    }
}
