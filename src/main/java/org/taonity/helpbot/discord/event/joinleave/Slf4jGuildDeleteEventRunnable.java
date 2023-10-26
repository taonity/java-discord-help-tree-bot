package org.taonity.helpbot.discord.event.joinleave;

import discord4j.core.event.domain.guild.GuildDeleteEvent;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;

@RequiredArgsConstructor
public class Slf4jGuildDeleteEventRunnable implements Runnable {
    private final GuildDeleteEvent guildDeleteEvent;
    private final Consumer<GuildDeleteEvent> eventConsumer;

    @Override
    public void run() {
        MDC.put("guildId", getGuildId());
        eventConsumer.accept(guildDeleteEvent);
    }

    private String getGuildId() {
        return guildDeleteEvent.getGuildId().asString();
    }
}
