package org.taonity.helpbot.discord.event.joinleave.service;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.taonity.helpbot.discord.GuildSettings;
import org.taonity.helpbot.discord.GuildSettingsRepository;
import org.taonity.helpbot.discord.event.command.positive.question.selectmenu.SelectMenuService;
import org.taonity.helpbot.discord.event.command.tree.TreeRootService;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GuildDataService {
    private final GuildSettingsRepository guildSettingsRepository;
    private final SelectMenuService selectMenuService;
    private final TreeRootService treeRootService;
    private final GatewayDiscordClient gatewayDiscordClient;
    private final GuildPersistableDataService guildPersistableDataService;

    @Transactional
    public Mono<Void> remove(String guildId) {
        return Mono.just(guildId)
                .flatMap(guildSettingsRepository::findGuildSettingByGuildId)
                .switchIfEmpty(Mono.error(new EmptyOptionalException(LogMessage.ALERT_20054)))
                .flatMap(guildPersistableDataService::remove)
                .doOnSuccess(result -> {
                    selectMenuService.removeSmManagerList(guildId);
                    treeRootService.removeRootByGuildId(guildId);
                });
    }

    @Transactional
    public Mono<Void> create(String guildId) {
        selectMenuService.createSmManagerList(guildId);
        return Flux.zip(
                        guildPersistableDataService.create(guildId),
                        guildSettingsRepository
                                .findGuildSettingByGuildId(guildId)
                                .switchIfEmpty(Mono.error(new EmptyOptionalException(LogMessage.ALERT_20070))))
                .flatMap(tuple -> treeRootService.createNewRoot(tuple.getT2()))
                .then();
    }

    public Mono<Void> removeIfLeftInDiscord() {
        return Flux.zip(getDiscordsGuildIds(), getDbGuildIds(), (discordGuildIds, dbGuildIds) -> dbGuildIds.stream()
                        .filter(dbGuildId -> !discordGuildIds.contains(dbGuildId))
                        .collect(Collectors.toList()))
                .flatMapIterable(guildIdsToRemove -> guildIdsToRemove)
                .flatMap(this::remove)
                .then();
    }

    private Mono<List<String>> getDiscordsGuildIds() {
        return gatewayDiscordClient.getGuilds().cache().collectList().map(guilds -> guilds.stream()
                .map(Guild::getId)
                .map(Snowflake::asString)
                .collect(Collectors.toList()));
    }

    private Mono<List<String>> getDbGuildIds() {
        return guildSettingsRepository.findAll().collectList().map(guildSettings -> guildSettings.stream()
                .map(GuildSettings::getGuildId)
                .collect(Collectors.toList()));
    }
}
