package org.taonity.helpbot.discord.event.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.taonity.helpbot.discord.CommandName;
import reactor.core.publisher.Mono;

public abstract class AbstractPositiveSlashCommand implements SlashCommand {
    public abstract CommandName getCommand();

    public Mono<Boolean> filterByCommand(ChatInputInteractionEvent event) {
        return Mono.just(getCommand().getCommandName().equals(event.getCommandName()));
    }
}
