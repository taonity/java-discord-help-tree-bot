package discord.handler.command;

import discord.handler.DiscordEventHandler;
import discord.structure.CommandName;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class AbstractSlashCommand implements DiscordEventHandler<ChatInputInteractionEvent> {
    abstract public CommandName getCommand();

    public boolean filterByCommand(ChatInputInteractionEvent event) {
        return getCommand().getCommandName().equals(event.getCommandName());
    }

}
