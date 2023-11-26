package org.taonity.helpbot.discord.event.joinleave.service;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.taonity.helpbot.discord.GuildSettings;
import org.taonity.helpbot.discord.GuildSettingsRepository;
import org.taonity.helpbot.discord.event.command.gitea.services.GiteaUserService;
import org.taonity.helpbot.discord.logging.LogMessage;
import org.taonity.helpbot.discord.logging.exception.GiteaApiException;
import org.taonity.helpbot.discord.logging.exception.client.CorruptGiteaUserException;
import org.taonity.helpbot.discord.logging.exception.main.EmptyOptionalException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GuildPersistableDataService {

    private final GuildSettingsRepository guildSettingsRepository;
    private final GiteaUserService giteaUserService;
    private final GuildRoleService guildRoleService;
    private final GatewayDiscordClient gatewayDiscordClient;

    @Transactional
    public Mono<Void> remove(GuildSettings guildSettings) {
        // TODO: Check
        return giteaUserService
                .deleteUser(guildSettings.getGuildId())
                .onErrorResume(
                        GiteaApiException.class,
                        e -> Mono.error(
                                new CorruptGiteaUserException(LogMessage.ALERT_20001, guildSettings.getGuildId(), e)))
                .then(guildSettingsRepository.delete(guildSettings));
    }

    @Transactional
    public Mono<Void> create(String guildId) {
        final var guildSettings = GuildSettings.builder().guildId(guildId).build();
        return guildSettingsRepository
                .save(guildSettings)
                .map(GuildSettings::getId)
                .flatMap(giteaUserService::createUser)
                .onErrorResume(
                        GiteaApiException.class,
                        e -> Mono.error(new CorruptGiteaUserException(LogMessage.ALERT_20032, guildId, e)))
                .then(gatewayDiscordClient
                        .getGuildById(Snowflake.of(guildId))
                        .switchIfEmpty(Mono.error(new EmptyOptionalException(LogMessage.ALERT_20064)))
                        .flatMap(guildRoleService::createModeratorRole));
    }
}
