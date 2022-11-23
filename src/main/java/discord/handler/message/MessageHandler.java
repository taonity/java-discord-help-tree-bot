package discord.handler.message;

import discord.handler.DiscordEventHandler;
import discord4j.core.event.domain.message.MessageCreateEvent;

public interface MessageHandler extends DiscordEventHandler<MessageCreateEvent> {
}
