package org.taonity.helpbot.discord.event.joinleave.service;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.taonity.helpbot.discord.GuildSettings;
import org.taonity.helpbot.discord.GuildSettingsRepository;
import org.taonity.helpbot.discord.event.command.positive.question.selectmenu.SelectMenuService;
import org.taonity.helpbot.discord.event.command.tree.TreeRootService;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;

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
        guildSettingsRepository
                .findGuildSettingByGuildId(guildId)
                .ifPresentOrElse(treeRootService::createNewRoot, () -> {
                    throw new EmptyOptionalException(LogMessage.ALERT_20070);
                });
    }

    public void removeIfLeftInDiscord() {
        final var discordGuildIdList = gatewayDiscordClient
                .getGuilds()
                .cache()
                .collectList()
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
