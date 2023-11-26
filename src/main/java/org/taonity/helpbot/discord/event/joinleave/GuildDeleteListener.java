package org.taonity.helpbot.discord.event.joinleave;

import static org.taonity.helpbot.discord.mdc.ContextRegistryMdcKeyRegister.GUILD_ID_MDC_KEY;

import discord4j.core.event.domain.guild.GuildDeleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.taonity.helpbot.discord.event.DiscordEventListener;
import org.taonity.helpbot.discord.event.joinleave.service.GuildDataService;
import org.taonity.helpbot.discord.mdc.OnCompleteSignalListenerBuilder;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuildDeleteListener implements DiscordEventListener<GuildDeleteEvent> {

    private final GuildDataService guildDataService;

    @Override
    public ContextView getContextView(GuildDeleteEvent event) {
        return Context.of(GUILD_ID_MDC_KEY, event.getGuildId().asString());
    }

    @Override
    @Transactional
    public Mono<Void> handle(GuildDeleteEvent event) {
        return Mono.just(event.getGuildId().asString())
                .flatMap(guildDataService::remove)
                .tap(OnCompleteSignalListenerBuilder.of(() -> log.info("Bot left the guild")));
    }
}
