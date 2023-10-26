package org.taonity.helpbot.discord.event.command.tree;

import static java.util.Objects.isNull;
import static org.taonity.helpbot.discord.embed.EmbedType.SUCCESS_DIALOG_EMBED_TYPE;
import static org.taonity.helpbot.discord.embed.EmbedType.WRONG_DIALOG_EMBED_TYPE;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.EmbedCreateSpec;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.ChannelRole;
import org.taonity.helpbot.discord.GuildSettings;
import org.taonity.helpbot.discord.GuildSettingsRepository;
import org.taonity.helpbot.discord.MessageChannelService;
import org.taonity.helpbot.discord.embed.EmbedBuilder;
import org.taonity.helpbot.discord.event.MdcAwareThreadPoolExecutor;
import org.taonity.helpbot.discord.event.command.gitea.services.GiteaUserService;
import org.taonity.helpbot.discord.event.command.positive.config.WebhookEvent;
import org.taonity.helpbot.discord.event.command.tree.model.Node;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.GiteaApiException;
import org.taonity.helpbot.discord.logging.exception.NoCommitsException;
import org.taonity.helpbot.discord.logging.exception.client.CorruptGiteaUserException;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;
import org.taonity.helpbot.discord.logging.exception.main.FailedToCreateNewRootException;
import org.taonity.helpbot.discord.logging.exception.main.UnexpectedGiteaApiException;

@Slf4j
@Component
@RequiredArgsConstructor
@DependsOn("updatePersistableData")
public class TreeRootService {
    @Getter
    private final Map<String, Node> rootMap = new HashMap<>();

    private final GiteaUserService giteaUserService;
    private final GuildSettingsRepository guildSettingsRepository;
    private final GatewayDiscordClient gatewayDiscordClient;
    private final MessageChannelService messageChannelService;
    MdcAwareThreadPoolExecutor threadPoolExecutor = new MdcAwareThreadPoolExecutor(1, 1);

    private static boolean created = false;

    @PostConstruct
    private void postConstruct() {
        // TODO Maybe it is worth to wait for threads to finish
        StreamSupport.stream(guildSettingsRepository.findAll().spliterator(), false)
                .forEach(this::createExistingRootWithMdc);
        created = true;
    }

    private void createExistingRootWithMdc(GuildSettings guildSettings) {
        final Slf4jGuildSettingsRunnable runnable =
                new Slf4jGuildSettingsRunnable(guildSettings, this::createExistingRoot);
        threadPoolExecutor.submit(runnable);
    }

    private void createExistingRoot(GuildSettings guildSettings) {
        final String lastCommitsCorruptErrorMessage;
        try {
            lastCommitsCorruptErrorMessage = makeAndSetRoot(guildSettings);
        } catch (GiteaApiException e) {
            log.error(
                    "Unexpected gitea API exception on existing root creation with alert {}, guild {} gitea user {}",
                    LogMessage.ALERT_20004.name(),
                    guildSettings.getGuildId(),
                    guildSettings.getGiteaUserId());
            return;
        }

        if (isNull(lastCommitsCorruptErrorMessage)) {
            log.info(
                    "Existing dialog root creation succeed with a structure {} for guild {}",
                    rootMap.get(guildSettings.getGuildId()).asIdJsonString(),
                    guildSettings.getGuildId());
        } else {
            log.error(
                    "Existing dialog update on last commit with message [{}] for guild {}",
                    lastCommitsCorruptErrorMessage,
                    guildSettings.getGuildId());
        }
    }

    private String getTreeStructure(GuildSettings guildSettings) {
        return rootMap.get(guildSettings.getGuildId()).asIdJsonString();
    }

    public void createNewRoot(GuildSettings guildSettings) {
        final String lastCommitsCorruptErrorMessage;
        try {
            lastCommitsCorruptErrorMessage = makeAndSetRoot(guildSettings);
        } catch (GiteaApiException e) {
            throw new UnexpectedGiteaApiException(LogMessage.ALERT_20004, e);
        }

        if (isNull(lastCommitsCorruptErrorMessage)) {
            log.info("New dialog root creation succeed with a structure {}", getTreeStructure(guildSettings));
        } else {
            log.error("New dialog root creation on last commit with message [{}]", lastCommitsCorruptErrorMessage);
            throw new FailedToCreateNewRootException(LogMessage.ALERT_20005);
        }
    }

    public void updateRoot(WebhookEvent event) {
        final var giteaRepoUserId = event.getRepository().getOwner().getId();
        final GuildSettings guildSettings = getGuildSettings(giteaRepoUserId);

        final String lastCommitsCorruptErrorMessage;
        try {
            lastCommitsCorruptErrorMessage = makeAndSetRoot(guildSettings);
        } catch (GiteaApiException e) {
            throw new CorruptGiteaUserException(LogMessage.ALERT_20002, guildSettings.getGuildId(), e);
        }

        if (isNull(lastCommitsCorruptErrorMessage)) {
            if (!isNull(guildSettings.getLogChannelId())) {
                sendSuccessMessage(guildSettings);
            }

            log.info("Dialog root update succeed with a structure {}", getTreeStructure(guildSettings));
        } else {
            sendFailedOnLastCommitsMessage(guildSettings, lastCommitsCorruptErrorMessage);

            log.info("Dialog update on last commit with message [{}]", lastCommitsCorruptErrorMessage);
        }
    }

    private GuildSettings getGuildSettings(int giteaRepoUserId) {
        return guildSettingsRepository
                .findGuildSettingByGiteaUserId(giteaRepoUserId)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20050));
    }

    private void sendFailedOnLastCommitsMessage(GuildSettings guildSettings, String message) {
        final var embed = EmbedBuilder.buildMessageEmbed(message, WRONG_DIALOG_EMBED_TYPE);
        sendEmbedLog(guildSettings, embed);
    }

    private void sendSuccessMessage(GuildSettings guildSettings) {
        final var embed = EmbedBuilder.buildMessageEmbed("", SUCCESS_DIALOG_EMBED_TYPE);
        sendEmbedLog(guildSettings, embed);
    }

    private void sendEmbedLog(GuildSettings guildSettings, EmbedCreateSpec embedCreateSpec) {
        gatewayDiscordClient
                .getGuildById(Snowflake.of(guildSettings.getGuildId()))
                .blockOptional()
                .map(guild -> messageChannelService.getChannel(guild, ChannelRole.LOG))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20017))
                .createMessage(embedCreateSpec)
                .subscribe();
    }

    private String makeAndSetRoot(GuildSettings guildSettings) throws NoCommitsException, GiteaApiException {
        final var dialogRootAndError = giteaUserService.getDialogRoot(guildSettings);
        final var root = dialogRootAndError.getNode();

        root.identifyNodes();
        rootMap.put(guildSettings.getGuildId(), root);
        return dialogRootAndError.getErrorMessage();
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
