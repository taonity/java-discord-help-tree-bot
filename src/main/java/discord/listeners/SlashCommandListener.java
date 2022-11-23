package discord.listeners;

import discord.handler.command.AbstractSlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collection;

@SuppressWarnings("unchecked")
@Component
@RequiredArgsConstructor
public class SlashCommandListener implements DiscordEventListener<ChatInputInteractionEvent> {

    private final Collection<AbstractSlashCommand> commands;

    @Override
    public void handle(ChatInputInteractionEvent event) {
        Flux.fromIterable(commands)
                .filter(commands -> commands.filter(event))
                .next()
                .flatMap(command -> command.reactiveHandle(event))
                .subscribe();
    }

}

