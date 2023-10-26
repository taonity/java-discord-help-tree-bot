package org.taonity.helpbot.discord.event.joinleave;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;

@RequiredArgsConstructor
public class Slf4jGuildCreateEventRunnable implements Runnable {
    private final GuildCreateEvent guildCreateEvent;
    private final Consumer<GuildCreateEvent> eventConsumer;

    @Override
    public void run() {
        MDC.put("guildId", getGuildId());
        eventConsumer.accept(guildCreateEvent);
    }

    private String getGuildId() {
        return guildCreateEvent.getGuild().getId().asString();
    }
}
