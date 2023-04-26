package discord.services;

import discord.exception.client.CorruptGiteaUserException;
import discord.exception.main.EmptyOptionalException;
import discord.exception.GiteaApiException;
import discord.logging.LogMessage;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GuildPersistableDataService {

    private final GuildSettingsRepository guildSettingsRepository;
    private final GiteaUserService giteaUserService;
    private final GuildRoleService guildRoleService;
    private final GatewayDiscordClient gatewayDiscordClient;

    @Transactional
    public void remove(GuildSettings guildSettings) {
        try {
            giteaUserService.deleteUser(guildSettings.getGuildId());
        } catch (GiteaApiException e) {
            throw new CorruptGiteaUserException(LogMessage.ALERT_20001, guildSettings.getGuildId(), e);
        }
        guildSettingsRepository.delete(guildSettings);
    }

    @Transactional
    public void create(String guildId) {
        final var guildSettings = GuildSettings.builder()
                .guildId(guildId)
                .build();
        final var guildSettingsId = guildSettingsRepository.save(guildSettings).getId();
        try {
            giteaUserService.createUser(guildSettingsId);
        } catch (GiteaApiException e) {
            throw new CorruptGiteaUserException(LogMessage.ALERT_20032, guildId, e);
        }
        gatewayDiscordClient.getGuildById(Snowflake.of(guildId)).blockOptional()
                .ifPresentOrElse(
                        guildRoleService::createModeratorRole,
                        () -> {throw new EmptyOptionalException(LogMessage.ALERT_20064);}
                );
    }
}
