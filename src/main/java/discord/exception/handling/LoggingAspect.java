package discord.exception.handling;

import discord.exception.ClientGuildAwareException;
import discord.exception.EmptyOptionalException;
import discord.exception.LogMessageException;
import discord.exception.MainGuildAwareException;
import discord.localisation.LogMessage;
import discord.services.MessageChannelService;
import discord.structure.ChannelRole;
import discord.structure.EmbedBuilder;
import discord.structure.EmbedType;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.MessageCreateSpec;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static discord.structure.EmbedBuilder.LOG_ATTACHMENT_FILE_NAME;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    @Value("${discord.mainGuildId}")
    private String mainGuildId;

    private final GatewayDiscordClient gatewayDiscordClient;
    private final MessageChannelService messageChannelService;

    @AfterThrowing(value = "(execution(* discord..*..*(..)))", throwing = "exception")
    public void logForClientGuild(ClientGuildAwareException exception) {
        logException(exception, exception.getGuildId());
        System.out.println("1");
    }

    @AfterThrowing(value = "(execution(* discord..*..*(..)))", throwing = "exception")
    public void logForMainGuild(MainGuildAwareException exception) {
        logException(exception, mainGuildId);
        System.out.println("2");
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

        gatewayDiscordClient.getGuildById(Snowflake.of(guildId)).blockOptional()
                .map(guild -> messageChannelService.getChannel(guild, ChannelRole.LOG))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20045))
                .createMessage(messageSpecs)
                .block();
    }
}
