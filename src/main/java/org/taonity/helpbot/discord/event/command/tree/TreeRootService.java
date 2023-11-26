package org.taonity.helpbot.discord.event.command.tree;

import static java.util.Objects.isNull;
import static org.taonity.helpbot.discord.embed.EmbedType.SUCCESS_DIALOG_EMBED_TYPE;
import static org.taonity.helpbot.discord.embed.EmbedType.WRONG_DIALOG_EMBED_TYPE;
import static org.taonity.helpbot.discord.mdc.ContextRegistryMdcKeyRegister.GUILD_ID_MDC_KEY;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.EmbedCreateSpec;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.*;
import org.taonity.helpbot.discord.embed.EmbedBuilder;
import org.taonity.helpbot.discord.event.command.gitea.services.GiteaUserService;
import org.taonity.helpbot.discord.event.command.positive.config.WebhookEvent;
import org.taonity.helpbot.discord.event.command.tree.model.Node;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.GiteaApiException;
import org.taonity.helpbot.discord.logging.exception.client.CorruptGiteaUserException;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;
import org.taonity.helpbot.discord.logging.exception.main.FailedToCreateNewRootException;
import org.taonity.helpbot.discord.logging.exception.main.UnexpectedGiteaApiException;
import org.taonity.helpbot.discord.mdc.OnCompleteSignalListenerBuilder;
import org.taonity.helpbot.discord.mdc.OnErrorSignalListenerBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class TreeRootService {
    @Getter
    private final Map<String, Node> rootMap = new HashMap<>();

    private final GiteaUserService giteaUserService;
    private final GuildSettingsRepository guildSettingsRepository;
    private final GatewayDiscordClient gatewayDiscordClient;
    private final MessageChannelService messageChannelService;

    private static boolean created = false;

    public Mono<Void> init() {
        return guildSettingsRepository
                .findAll()
                .collectList()
                .doOnSuccess(guildSettings -> {
                    if (guildSettings.isEmpty()) {
                        log.warn("There is no guild in DB for TreeRootService!");
                    }
                })
                .flatMapMany(Flux::fromIterable)
                .concatMap(guildSettings -> createExistingRoot(guildSettings)
                        .contextWrite(Context.of(GUILD_ID_MDC_KEY, guildSettings.getGuildId())))
                .doOnComplete(() -> created = true)
                .then();
    }

    private Mono<Void> createExistingRoot(GuildSettings guildSettings) {
        return makeAndSetRoot(guildSettings)
                .tap(OnErrorSignalListenerBuilder.of(e -> log.error(
                        "Unexpected gitea API exception on existing root creation with alert {}, guild {} gitea user {}",
                        LogMessage.ALERT_20004.name(),
                        guildSettings.getGuildId(),
                        guildSettings.getGiteaUserId())))
                .switchIfEmpty(Mono.<String>empty()
                        .tap(OnCompleteSignalListenerBuilder.of(() -> logSuccessfulRootCreation(guildSettings))))
                .flatMap(lastCommitsCorruptErrorMessage -> Mono.<Void>empty()
                        .tap(OnCompleteSignalListenerBuilder.of(() -> {
                            log.info(
                                    "Existing dialog update on last commit with message [{}] for guild {}",
                                    lastCommitsCorruptErrorMessage,
                                    guildSettings.getGuildId());
                            logSuccessfulRootCreation(guildSettings);
                        })))
                .then();
    }

    private void logSuccessfulRootCreation(GuildSettings guildSettings) {
        log.info(
                "Existing dialog root creation succeed with a structure {} for guild {}",
                rootMap.get(guildSettings.getGuildId()).asIdJsonString(),
                guildSettings.getGuildId());
    }

    private String getTreeStructure(GuildSettings guildSettings) {
        return rootMap.get(guildSettings.getGuildId()).asIdJsonString();
    }

    public Mono<Void> createNewRoot(GuildSettings guildSettings) {
        return makeAndSetRoot(guildSettings)
                .onErrorResume(
                        GiteaApiException.class,
                        e -> Mono.error(new UnexpectedGiteaApiException(LogMessage.ALERT_20004, e)))
                .flatMap(lastCommitsCorruptErrorMessage -> {
                    if (isNull(lastCommitsCorruptErrorMessage)) {
                        return Mono.<Void>empty()
                                .tap(OnCompleteSignalListenerBuilder.of(() -> log.info(
                                        "New dialog root creation succeed with a structure {}",
                                        getTreeStructure(guildSettings))));
                    } else {
                        return Mono.<Void>empty()
                                .tap(OnCompleteSignalListenerBuilder.of(() -> log.error(
                                        "New dialog root creation on last commit with message [{}]",
                                        lastCommitsCorruptErrorMessage)))
                                .then(Mono.error(new FailedToCreateNewRootException(LogMessage.ALERT_20005)));
                    }
                });
    }

    public Mono<Void> updateRoot(WebhookEvent event) {
        final var giteaRepoUserId = event.getRepository().getOwner().getId();
        return getGuildSettings(giteaRepoUserId)
                .flatMap(guildSettings -> makeAndSetRoot(guildSettings)
                        .onErrorResume(
                                CorruptGiteaUserException.class,
                                e -> Mono.error(new CorruptGiteaUserException(
                                        LogMessage.ALERT_20002, guildSettings.getGuildId(), e)))
                        .flatMap(lastCommitsCorruptErrorMessage -> {
                            if (isNull(lastCommitsCorruptErrorMessage)) {
                                if (!isNull(guildSettings.getLogChannelId())) {
                                    return sendSuccessMessage(guildSettings);
                                }
                                return Mono.empty()
                                        .tap(OnCompleteSignalListenerBuilder.of(() -> log.info(
                                                "Dialog root update succeed with a structure {}",
                                                getTreeStructure(guildSettings))));
                            } else {
                                return sendFailedOnLastCommitsMessage(guildSettings, lastCommitsCorruptErrorMessage)
                                        .tap(OnCompleteSignalListenerBuilder.of(() -> log.info(
                                                "Dialog update on last commit with message [{}]",
                                                lastCommitsCorruptErrorMessage)));
                            }
                        }))
                .then();
    }

    private Mono<GuildSettings> getGuildSettings(int giteaRepoUserId) {
        return guildSettingsRepository
                .findGuildSettingByGiteaUserId(giteaRepoUserId)
                .switchIfEmpty(Mono.error(new EmptyOptionalException(LogMessage.ALERT_20050)));
    }

    private Mono<Void> sendFailedOnLastCommitsMessage(GuildSettings guildSettings, String message) {
        final var embed = EmbedBuilder.buildMessageEmbed(message, WRONG_DIALOG_EMBED_TYPE);
        return sendEmbedLog(guildSettings, embed);
    }

    private Mono<Void> sendSuccessMessage(GuildSettings guildSettings) {
        final var embed = EmbedBuilder.buildMessageEmbed("", SUCCESS_DIALOG_EMBED_TYPE);
        return sendEmbedLog(guildSettings, embed);
    }

    private Mono<Void> sendEmbedLog(GuildSettings guildSettings, EmbedCreateSpec embedCreateSpec) {
        return gatewayDiscordClient
                .getGuildById(Snowflake.of(guildSettings.getGuildId()))
                .switchIfEmpty(Mono.error(new EmptyOptionalException(LogMessage.ALERT_20017)))
                .flatMap(guild -> messageChannelService.getChannel(guild, ChannelRole.LOG))
                .flatMap(messageChannel -> messageChannel.createMessage(embedCreateSpec))
                .then();
    }

    private Mono<String> makeAndSetRoot(GuildSettings guildSettings) {
        return giteaUserService.getDialogRoot(guildSettings).flatMap(nodeAndError -> {
            final var root = nodeAndError.getNode();

            root.identifyNodes();
            rootMap.put(guildSettings.getGuildId(), root);
            return isNull(nodeAndError.getErrorMessage()) ? Mono.empty() : Mono.just(nodeAndError.getErrorMessage());
        });
    }

    public Node getRootByGuildId(String guildId) {
        return rootMap.get(guildId);
    }

    public void removeRootByGuildId(String guildId) {
        rootMap.remove(guildId);
    }

    public static boolean wasCreated() {
        return created;
    }
}
