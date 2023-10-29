package org.taonity.helpbot.discord.event;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.MDC;

public class Slf4jMessageCreateEventRunnable extends Slf4jRunnable<MessageCreateEvent> {
    public Slf4jMessageCreateEventRunnable(MessageCreateEvent object) {
        super(object);
    }

    @Override
    public void setMdcParams() {
        MDC.put("guildId", getGuildId());
        MDC.put("userId", getUserId());
    }

    private String getUserId() {
        return object
                .getMember()
                .map(User::getId)
                .map(Snowflake::asString)
                .orElse("NULL");
    }

    private String getGuildId() {
        return object.getGuildId()
                .map(Snowflake::asString)
                .orElse("NULL");
    }
}
