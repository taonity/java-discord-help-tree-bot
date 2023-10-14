package org.taonity.helpbot.discord.event.joinleave;

import discord4j.core.event.domain.guild.GuildDeleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.taonity.helpbot.discord.event.DiscordEventListener;
import org.taonity.helpbot.discord.event.joinleave.service.GuildDataService;

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

        log.info("Bot left guild {}", guildId);
    }
}
