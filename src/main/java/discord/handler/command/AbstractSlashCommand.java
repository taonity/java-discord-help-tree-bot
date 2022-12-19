package discord.handler.command;

import discord.handler.DiscordEventHandler;
import discord.structure.CommandName;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

import java.util.List;

import static discord.structure.CommandName.ANY;

public abstract class AbstractSlashCommand implements DiscordEventHandler<ChatInputInteractionEvent> {
    abstract public CommandName getCommand();

    public boolean filterByCommand(ChatInputInteractionEvent event) {
        final var command = getCommand();
        if(command == ANY) {
            return true;
        } else {
            return command.getCommandName().equals(event.getCommandName());
        }
    }

}
