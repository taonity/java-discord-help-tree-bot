package discord.messagehandlers;

import discord4j.core.event.domain.message.MessageCreateEvent;

public interface MessageHandler {
    boolean condition(MessageCreateEvent event);
    void handle(MessageCreateEvent event);
}
