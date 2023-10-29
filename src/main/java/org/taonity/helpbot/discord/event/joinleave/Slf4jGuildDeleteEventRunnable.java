package org.taonity.helpbot.discord.event.joinleave;

import discord4j.core.event.domain.guild.GuildDeleteEvent;
import java.util.function.Consumer;

import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.taonity.helpbot.discord.event.Slf4jRunnable;

public class Slf4jGuildDeleteEventRunnable extends Slf4jRunnable<GuildDeleteEvent> {
    public Slf4jGuildDeleteEventRunnable(GuildDeleteEvent object) {
        super(object);
    }

    @Override
    public void setMdcParams() {
        MDC.put("guildId", getGuildId());
    }

    private String getGuildId() {
        return object.getGuildId().asString();
    }
}
