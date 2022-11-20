package discord.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class SlashCommand {
    abstract public CommandName getCommand();

    public boolean filterByCommand(ChatInputInteractionEvent event) {
        return getCommand().getCommandName().equals(event.getCommandName());
    }

    public boolean filterByChannelId(ChatInputInteractionEvent event, String channelId) {
        return event.getInteraction()
                .getChannel()
                .block()
                .getId()
                .asString()
                .equals(channelId);
    }

    public boolean filterByAuthorId(ChatInputInteractionEvent event, List<String> userWhiteList) {
        final String authorId = event.getInteraction().getMember().get().getId().asString();
        return userWhiteList.contains(authorId);
    }

    abstract public boolean filter(ChatInputInteractionEvent event);

    abstract public Mono<Void> handle(ChatInputInteractionEvent event);
}
