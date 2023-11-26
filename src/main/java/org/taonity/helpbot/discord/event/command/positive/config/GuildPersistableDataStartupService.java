package org.taonity.helpbot.discord.event.command.positive.config;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.taonity.helpbot.discord.GuildSettings;
import org.taonity.helpbot.discord.GuildSettingsRepository;
import org.taonity.helpbot.discord.event.joinleave.service.GuildPersistableDataService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GuildPersistableDataStartupService {

    private final GatewayDiscordClient gatewayDiscordClient;
    private final GuildPersistableDataService guildPersistableDataService;
    private final GuildSettingsRepository guildSettingsRepository;

    public Mono<Void> updatePersistableData() {
        return Flux.zip(getDiscordsGuildIds(), guildSettingsRepository.findAll().collectList())
                .next()
                .flatMap(t -> {
                    final var discordGuildIds = t.getT1();
                    final var guildSettings = t.getT2();
                    return Flux.just(guildSettings.stream()
                                    .filter(guildSetting -> !discordGuildIds.contains(guildSetting.getGuildId()))
                                    .collect(Collectors.toList()))
                            .flatMapIterable(guildIdsToRemove -> guildIdsToRemove)
                            .flatMap(guildPersistableDataService::remove)
                            .then()
                            .then(Mono.defer(() -> {
                                final var guildSettingsIds = guildSettings.stream()
                                        .map(GuildSettings::getGuildId)
                                        .toList();
                                final var discordGuildsToCreate = discordGuildIds.stream()
                                        .filter(discordGuildId -> !guildSettingsIds.contains(discordGuildId))
                                        .toList();
                                return Flux.fromIterable(discordGuildsToCreate)
                                        .concatMap(guildPersistableDataService::create)
                                        .then();
                            }));
                })
                .then();
    }

    private Mono<List<String>> getDiscordsGuildIds() {
        return gatewayDiscordClient.getGuilds().cache().collectList().map(guilds -> guilds.stream()
                .map(Guild::getId)
                .map(Snowflake::asString)
                .collect(Collectors.toList()));
    }
}
