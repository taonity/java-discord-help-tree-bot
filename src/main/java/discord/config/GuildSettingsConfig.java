package discord.config;

import discord.exception.GuildSettingsNotFound;
import discord.model.GuildSettings;
import discord.repository.GuildSettingsRepository;
import discord4j.core.object.entity.Guild;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GuildSettingsConfig {
    @Bean
    GuildSettings guildSettings(final GuildSettingsRepository repository, final Guild guild) {
        return repository.findGuildSettingById(guild.getId().asString())
                .orElseThrow(GuildSettingsNotFound::new);
    }
}
