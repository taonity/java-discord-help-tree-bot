package org.taonity.helpbot.discord.event;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.MDC;

import java.util.function.Consumer;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public abstract class Slf4jRunnable<E> implements Runnable {

    @Setter
    private Consumer<E> consumer;
    public final E object;

    public abstract void setMdcParams();

    @Override
    public void run() {
        setMdcParams();
        if(isNull(consumer)) {
            throw new NullPointerException("consumer is not set");
        } else {
            consumer.accept(object);
        }
    }
}
