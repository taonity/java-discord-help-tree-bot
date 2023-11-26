package org.taonity.helpbot.discord.event.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;

public interface SlashCommand extends DiscordEventHandler<ChatInputInteractionEvent> {}
