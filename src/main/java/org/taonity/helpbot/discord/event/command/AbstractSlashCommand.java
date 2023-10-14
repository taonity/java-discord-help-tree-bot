package org.taonity.helpbot.discord.event.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.taonity.helpbot.discord.CommandName;

public abstract class AbstractSlashCommand implements DiscordEventHandler<ChatInputInteractionEvent> {
    public abstract CommandName getCommand();

    public boolean filterByCommand(ChatInputInteractionEvent event) {
        final var command = getCommand();
        if (command == CommandName.ANY) {
            return true;
        } else {
            return command.getCommandName().equals(event.getCommandName());
        }
    }
}
