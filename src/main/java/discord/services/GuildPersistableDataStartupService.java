package discord.services;

import discord.exception.main.EmptyOptionalException;
import discord.logging.LogMessage;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GuildPersistableDataStartupService {

    private final GatewayDiscordClient gatewayDiscordClient;
    private final GuildPersistableDataService guildPersistableDataService;
    private final GuildSettingsRepository guildSettingsRepository;

    // TODO: is there a way to do it with proper way?
    @Bean
    public void updatePersistableData() {
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

        final var guildSettingsList = StreamSupport.stream(
                        guildSettingsRepository.findAll().spliterator(), true)
                .collect(Collectors.toList());
        ;

        guildSettingsList.stream()
                .filter(guildSettings -> !discordGuildIdList.contains(guildSettings.getGuildId()))
                .forEach(guildPersistableDataService::remove);

        final var guildSettingsIdList =
                guildSettingsList.stream().map(GuildSettings::getGuildId).collect(Collectors.toList());

        discordGuildIdList.stream()
                .filter(discordGuildId -> !guildSettingsIdList.contains(discordGuildId))
                .forEach(guildPersistableDataService::create);
    }
}
