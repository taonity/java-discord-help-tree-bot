package discord.exception.handling;

import discord.exception.ClientGuildAwareException;
import discord.exception.EmptyOptionalException;
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

    private final GatewayDiscordClient gatewayDiscordClient;
    private final MessageChannelService messageChannelService;

    @AfterThrowing(value = "(execution(* discord..*..*(..)))", throwing = "exception")
    public void logAfterThrowingAllMethods(ClientGuildAwareException exception) throws InterruptedException {
        final var outputStream = new ByteArrayOutputStream();
        final var printStream = new PrintStream(outputStream);
        exception.printStackTrace(printStream);
        final var inputStream = new ByteArrayInputStream(outputStream.toString().getBytes(StandardCharsets.UTF_8));

        final var messageSpecs = MessageCreateSpec.builder()
                .addEmbed(EmbedBuilder.buildLogEmbed(exception.getMessage(), EmbedType.EXPECTED_EMBED_TYPE))
                .addFile(LOG_ATTACHMENT_FILE_NAME, inputStream)
                .build();

        gatewayDiscordClient.getGuildById(Snowflake.of(exception.getGuildId())).blockOptional()
                .map(guild -> messageChannelService.getChannel(guild, ChannelRole.LOG))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20045))
                .createMessage(messageSpecs)
                .block();
Thread.sleep(1000);
        System.out.println("YEWWWWWW+++++++++++++++++++++++++++++++++++++++");
    }
}
