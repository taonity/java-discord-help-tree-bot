package discord.listeners;

import discord.localisation.LogMessage;
import discord.repository.GuildSettingsRepository;
import discord.services.GuildDataService;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuildCreateListener implements DiscordEventListener<GuildCreateEvent> {

    private final GuildSettingsRepository guildSettingsRepository;
    private final GuildDataService guildDataService;

    @Override
    @Transactional
    public void handle(GuildCreateEvent event) {
        final var guildId = event.getGuild().getId().asString();
        final var guildIsNotPresent = guildSettingsRepository
                .findGuildSettingByGuildId(guildId)
                .isEmpty();

        if(guildIsNotPresent) {
            guildDataService.removeIfLeftInDiscord();
            guildDataService.create(guildId);
        } else {
            // means that bot didn't really join the guild
            log.warn(LogMessage.ALERT_20055.name());
        }
    }
}
