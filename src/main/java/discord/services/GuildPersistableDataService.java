package discord.services;

import discord.exception.EmptyOptionalException;
import discord.localisation.LogMessage;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord.utils.AlphaNumericGenerator;
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
        giteaUserService.deleteUser(guildSettings.getGuildId());
        guildSettingsRepository.delete(guildSettings);
    }

    @Transactional
    public void create(String guildId) {
        final var guildSettings = GuildSettings.builder()
                .guildId(guildId)
                .build();
        final var guildSettingsId = guildSettingsRepository.save(guildSettings).getId();
        giteaUserService.createUser(guildSettingsId);
        gatewayDiscordClient.getGuildById(Snowflake.of(guildId)).blockOptional()
                .ifPresentOrElse(
                        guildRoleService::createModeratorRole,
                        () -> {throw new EmptyOptionalException(LogMessage.ALERT_20064);}
                );
    }
}
