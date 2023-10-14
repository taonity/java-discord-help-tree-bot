package org.taonity.helpbot.discord;

import discord4j.core.event.domain.message.MessageCreateEvent;
import org.taonity.helpbot.discord.event.command.DiscordEventHandler;

public interface MessageHandler extends DiscordEventHandler<MessageCreateEvent> {}
