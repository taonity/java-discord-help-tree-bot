package org.taonity.helpbot.discord.event.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.taonity.helpbot.discord.CommandName;

public abstract class AbstractPositiveSlashCommand implements SlashCommand {
    public abstract CommandName getCommand();

    public boolean filterByCommand(ChatInputInteractionEvent event) {
        return getCommand().getCommandName().equals(event.getCommandName());
    }
}
