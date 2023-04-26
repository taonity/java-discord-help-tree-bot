package discord.services;

import discord.exception.main.EmptyOptionalException;
import discord.logging.LogMessage;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord.tree.TreeRootService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class GuildDataService {
    private final GuildSettingsRepository guildSettingsRepository;
    private final SelectMenuService selectMenuService;
    private final TreeRootService treeRootService;
    private final GatewayDiscordClient gatewayDiscordClient;
    private final GuildPersistableDataService guildPersistableDataService;

    @Transactional
    public void remove(String guildId) {
        final var guildSettings = guildSettingsRepository
                .findGuildSettingByGuildId(guildId)
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20054));

        guildPersistableDataService.remove(guildSettings);

        selectMenuService.removeSmManagerList(guildId);
        treeRootService.removeRootByGuildId(guildId);
    }

    @Transactional
    public void create(String guildId) {
        guildPersistableDataService.create(guildId);

        selectMenuService.createSmManagerList(guildId);
        guildSettingsRepository.findGuildSettingByGuildId(guildId)
                .ifPresentOrElse(treeRootService::makeAndSetRoot,
                        () -> {throw new EmptyOptionalException(LogMessage.ALERT_20070);});
    }

    public void removeIfLeftInDiscord() {
        final var discordGuildIdList = gatewayDiscordClient.getGuilds().cache().collectList()
                .blockOptional()
                .orElseThrow(() -> new EmptyOptionalException(LogMessage.ALERT_20039))
                .stream()
                .map(Guild::getId)
                .map(Snowflake::asString)
                .collect(Collectors.toList());

        StreamSupport.stream(guildSettingsRepository.findAll().spliterator(), true)
                .map(GuildSettings::getGuildId)
                .filter(guildId -> !discordGuildIdList.contains(guildId))
                .forEach(this::remove);
    }
}
