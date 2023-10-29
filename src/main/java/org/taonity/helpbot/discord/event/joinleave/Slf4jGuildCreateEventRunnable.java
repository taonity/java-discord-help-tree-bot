package org.taonity.helpbot.discord.event.joinleave;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.taonity.helpbot.discord.event.Slf4jRunnable;

public class Slf4jGuildCreateEventRunnable extends Slf4jRunnable<GuildCreateEvent> {
    public Slf4jGuildCreateEventRunnable(GuildCreateEvent object) {
        super(object);
    }

    @Override
    public void setMdcParams() {
        MDC.put("guildId", getGuildId());
    }

    private String getGuildId() {
        return object.getGuild().getId().asString();
    }
}
