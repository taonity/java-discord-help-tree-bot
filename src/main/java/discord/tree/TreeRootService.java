package discord.tree;

import discord.dto.WebhookEvent;
import discord.exception.client.CorruptGiteaUserException;
import discord.exception.main.EmptyOptionalException;
import discord.exception.GiteaApiException;
import discord.exception.NoCommitsException;
import discord.exception.main.FailedToCreateNewRootException;
import discord.exception.main.UnexpectedGiteaApiException;
import discord.logging.LogMessage;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord.services.GiteaUserService;
import discord.services.MessageChannelService;
import discord.structure.ChannelRole;
import discord.structure.EmbedBuilder;
import discord.structure.NodeAndError;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static discord.structure.EmbedType.SUCCESS_DIALOG_EMBED_TYPE;
import static discord.structure.EmbedType.WRONG_DIALOG_EMBED_TYPE;
import static java.util.Objects.isNull;

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

    @PostConstruct
    private void postConstruct() {
        StreamSupport.stream(guildSettingsRepository.findAll().spliterator(), true)
                .forEach(this::createExistingRoot);
    }

    private void createExistingRoot(GuildSettings guildSettings) {
        final String lastCommitsCorruptErrorMessage;
        try {
            lastCommitsCorruptErrorMessage = makeAndSetRoot(guildSettings);
        } catch (GiteaApiException e) {
            log.error("Unexpected gitea API exception on existing root creation with alert {}, guild {} gitea user {}",
                    LogMessage.ALERT_20004.name(),
                    guildSettings.getGuildId(),
                    guildSettings.getGiteaUserId()
            );
            return;
        }

        if(isNull(lastCommitsCorruptErrorMessage)) {
            log.info("Existing dialog root creation succeed with a structure {} for guild {}",
                    rootMap.get(guildSettings.getGuildId()).asIdJsonString(),
                    guildSettings.getGuildId());
        } else {
            log.error("Existing dialog update on last commit with message [{}] for guild {}",
                    lastCommitsCorruptErrorMessage,
                    guildSettings.getGuildId());
        }
    }

    public void createNewRoot(GuildSettings guildSettings) {
        final String lastCommitsCorruptErrorMessage;
        try {
            lastCommitsCorruptErrorMessage = makeAndSetRoot(guildSettings);
        } catch (GiteaApiException e) {
            throw new UnexpectedGiteaApiException(LogMessage.ALERT_20004, e);
        }

        if(isNull(lastCommitsCorruptErrorMessage)) {
            log.info("New dialog root creation succeed with a structure {} for guild {}",
                    rootMap.get(guildSettings.getGuildId()).asIdJsonString(),
                    guildSettings.getGuildId());
        } else {
            log.error("New dialog root creation on last commit with message [{}] for guild {}",
                    lastCommitsCorruptErrorMessage,
                    guildSettings.getGuildId());
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

        if(isNull(lastCommitsCorruptErrorMessage)) {
            sendSuccessMessage(guildSettings);

            log.info("Dialog root update succeed with a structure {} for guild {}",
                    rootMap.get(guildSettings.getGuildId()).asIdJsonString(),
                    guildSettings.getGuildId());
        } else {
            sendFailedOnLastCommitsMessage(guildSettings, lastCommitsCorruptErrorMessage);

            log.info("Dialog update on last commit with message [{}] for guild {}",
                    lastCommitsCorruptErrorMessage,
                    guildSettings.getGuildId());
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
        gatewayDiscordClient.getGuildById(Snowflake.of(guildSettings.getGuildId()))
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
}
