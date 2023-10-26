package org.taonity.helpbot.discord.event;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;

@RequiredArgsConstructor
public class Slf4jMessageCreateEventRunnable implements Runnable {
    private final MessageCreateEvent messageCreateEvent;
    private final Consumer<MessageCreateEvent> eventConsumer;

    @Override
    public void run() {
        MDC.put("guildId", getGuildId());
        MDC.put("userId", getUserId());
        eventConsumer.accept(messageCreateEvent);
    }

    private String getUserId() {
        return messageCreateEvent
                .getMember()
                .map(User::getId)
                .map(Snowflake::asString)
                .orElse("NULL");
    }

    private String getGuildId() {
        return messageCreateEvent.getGuildId().map(Snowflake::asString).orElse("NULL");
    }
}
