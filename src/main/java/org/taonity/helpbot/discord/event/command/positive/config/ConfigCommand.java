package org.taonity.helpbot.discord.event.command.positive.config;

import static org.taonity.helpbot.discord.CommandName.CONFIG;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.*;
import org.taonity.helpbot.discord.embed.EmbedBuilder;
import org.taonity.helpbot.discord.embed.EmbedType;
import org.taonity.helpbot.discord.event.command.AbstractPositiveSlashCommand;
import org.taonity.helpbot.discord.event.command.AlphaNumericGenerator;
import org.taonity.helpbot.discord.event.command.EventPredicates;
import org.taonity.helpbot.discord.event.command.gitea.services.GiteaUserService;
import org.taonity.helpbot.discord.localisation.SimpleMessage;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.GiteaApiException;
import org.taonity.helpbot.discord.logging.exception.client.CorruptGiteaUserException;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;
import org.taonity.helpbot.discord.mdc.OnCompleteSignalListenerBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigCommand extends AbstractPositiveSlashCommand {
    @Getter
    private final CommandName command = CONFIG;

    private final EventPredicates eventPredicates;
    private final GiteaUserService giteaUserService;
    private final GuildSettingsRepository guildSettingsRepository;

    @Value("${gitea.public.url}/user/login")
    private String loginUrl;

    @Value("${gitea.public.url}/%s/%s/src/branch/main/%s")
    private String dialogUrlFormat;

    @Override
    public final List<Function<ChatInputInteractionEvent, Mono<Boolean>>> getFilterPredicates() {
        return Arrays.asList(
                eventPredicates::filterBot,
                this::filterByCommand,
                eventPredicates::filterIfChannelsExistInSettings,
                eventPredicates::filterByModeratorRole);
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {

        final var guildId = event.getInteraction()
                .getGuildId()
                .map(Snowflake::asString)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20037));

        return giteaUserService
                .resetPassword(guildId)
                .onErrorResume(
                        GiteaApiException.class,
                        e -> Mono.error(new CorruptGiteaUserException(LogMessage.ALERT_20053, guildId, e)))
                .zipWith(
                        buildDialogUrl(guildId),
                        (editUserOption, dialogUrl) -> EmbedBuilder.buildSimpleMessage(
                                SimpleMessage.GITEA_USER_CREDS_MESSAGE_FORMAT.format(
                                        loginUrl,
                                        dialogUrl,
                                        editUserOption.getLoginName(),
                                        editUserOption.getPassword()),
                                EmbedType.SIMPLE_MESSAGE_EMBED_TYPE))
                .flatMap(embedCreateSpec ->
                        event.reply().withEmbeds(embedCreateSpec).withEphemeral(true))
                .tap(OnCompleteSignalListenerBuilder.of(() -> log.info("Command successfully executed")));
    }

    private Mono<String> buildDialogUrl(String guildId) {
        return guildSettingsRepository
                .findGuildSettingByGuildId(guildId)
                .switchIfEmpty(Mono.error(new EmptyOptionalException(LogMessage.ALERT_20070)))
                .map(GuildSettings::getId)
                .map(AlphaNumericGenerator::generateFourCharFromNumber)
                .map(giteaUserAlphaNumeric -> {
                    final var userName = String.format(GiteaUserService.USER_NAME_FORMAT, giteaUserAlphaNumeric);
                    final var repoName = String.format(GiteaUserService.REPO_NAME_FORMAT, giteaUserAlphaNumeric);
                    return String.format(dialogUrlFormat, userName, repoName, GiteaUserService.FILE_NAME);
                });
    }
}
