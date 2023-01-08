package discord.tree;

import discord.dto.WebhookEvent;
import discord.exception.client.CorruptGiteaUserException;
import discord.exception.main.EmptyOptionalException;
import discord.exception.GiteaApiException;
import discord.exception.NoCommitsException;
import discord.localisation.LogMessage;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord.services.GiteaUserService;
import discord.services.MessageChannelService;
import discord.structure.ChannelRole;
import discord.structure.EmbedBuilder;
import discord.structure.NodeAndError;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
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
                .forEach(this::makeAndSetRoot);
    }

    public void updateRoot(WebhookEvent event) {
        final var guildSettings = guildSettingsRepository
                .findGuildSettingByGiteaUserId(event.getRepository().getOwner().getId())
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20050));

        final var firstCommitIsCorruptError = makeAndSetRoot(guildSettings);

        final var embedCreateSpec = Optional.ofNullable(firstCommitIsCorruptError)
                .map(message -> EmbedBuilder.buildMessageEmbed(message, WRONG_DIALOG_EMBED_TYPE))
                .orElseGet(() -> EmbedBuilder.buildMessageEmbed("", SUCCESS_DIALOG_EMBED_TYPE));

        gatewayDiscordClient.getGuildById(Snowflake.of(guildSettings.getGuildId()))
                .blockOptional()
                .map(guild -> messageChannelService.getChannel(guild, ChannelRole.LOG))
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20017))
                .createMessage(embedCreateSpec)
                .subscribe();
    }

    public String makeAndSetRoot(GuildSettings guildSettings) {
        final NodeAndError dialogRootAndError;
        try {
            dialogRootAndError = giteaUserService.getDialogRoot(guildSettings);
        } catch (GiteaApiException e) {
            throw new CorruptGiteaUserException(LogMessage.ALERT_20002, guildSettings.getGuildId(), e);
        } catch (NoCommitsException e) {
            throw new CorruptGiteaUserException(LogMessage.ALERT_20004, guildSettings.getGuildId(), e);
        }
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
