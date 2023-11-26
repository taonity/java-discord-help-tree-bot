package org.taonity.helpbot.discord.event.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import java.util.List;
import org.taonity.helpbot.discord.CommandName;
import reactor.core.publisher.Mono;

public abstract class AbstractNegativeSlashCommand implements SlashCommand {
    public abstract List<CommandName> getCommands();

    public Mono<Boolean> filterByCommands(ChatInputInteractionEvent event) {
        return Mono.just(getCommands().stream()
                .map(CommandName::getCommandName)
                .anyMatch(commandName -> commandName.equals(event.getCommandName())));
    }
}
