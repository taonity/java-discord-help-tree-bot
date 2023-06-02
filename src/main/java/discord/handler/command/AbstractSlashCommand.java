package discord.handler.command;

import static discord.structure.CommandName.ANY;

import discord.handler.DiscordEventHandler;
import discord.structure.CommandName;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;

public abstract class AbstractSlashCommand implements DiscordEventHandler<ChatInputInteractionEvent> {
    public abstract CommandName getCommand();

    public boolean filterByCommand(ChatInputInteractionEvent event) {
        final var command = getCommand();
        if (command == ANY) {
            return true;
        } else {
            return command.getCommandName().equals(event.getCommandName());
        }
    }
}
