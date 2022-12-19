package discord.listeners;

import discord.services.GuildDataService;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuildDeleteListener implements DiscordEventListener<GuildDeleteEvent> {

    private final GuildDataService guildDataService;

    @Override
    @Transactional
    public void handle(GuildDeleteEvent event) {
        final var guildId = event.getGuildId().asString();

        guildDataService.remove(guildId);
    }
}
