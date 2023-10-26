package org.taonity.helpbot.discord.event.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import java.util.List;
import org.taonity.helpbot.discord.CommandName;

public abstract class AbstractNegativeSlashCommand implements SlashCommand {
    public abstract List<CommandName> getCommands();

    public boolean filterByCommands(ChatInputInteractionEvent event) {
        return getCommands().stream()
                .map(CommandName::getCommandName)
                .anyMatch(commandName -> commandName.equals(event.getCommandName()));
    }
}
