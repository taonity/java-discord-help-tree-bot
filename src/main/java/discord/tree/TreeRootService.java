package discord.tree;

import discord.dao.WebhookEvent;
import discord.exception.EmptyOptionalException;
import discord.localisation.LogMessage;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord.services.GiteaUserService;
import discord.services.MessageChannelService;
import discord.structure.ChannelRole;
import discord.structure.EmbedBuilder;
import discord.structure.EmbedType;
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
import java.util.stream.StreamSupport;

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
                .forEach(this::makeAndSetRoot);
    }

    public void updateRoot(WebhookEvent event) {
        final var guildSettings = guildSettingsRepository
                .findGuildSettingByGiteaUserId(event.getPusher().getId())
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20050));

        final var firstCommitIsCorruptError = makeAndSetRoot(guildSettings);

        if(!isNull(firstCommitIsCorruptError)) {
            gatewayDiscordClient.getGuildById(Snowflake.of(guildSettings.getGuildId()))
                    .blockOptional()
                    .map(guild -> messageChannelService.getChannel(guild, ChannelRole.LOG))
                    .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20017))
                    .createMessage(EmbedBuilder.buildMessageEmbed(firstCommitIsCorruptError, EmbedType.WRONG_DIALOG_EMBED_TYPE))
                    .subscribe();
        }
    }

    public String makeAndSetRoot(GuildSettings guildSettings) {
        final var dialogRootAndError = giteaUserService.getDialogRoot(guildSettings.getId());
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
