package discord.listeners;

import discord.commands.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@SuppressWarnings("unchecked")
@Component
@RequiredArgsConstructor
public class SlashCommandListener implements DiscordEventListener<ChatInputInteractionEvent> {

    private final Collection<SlashCommand> commands;

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return Flux.fromIterable(commands)
                .filter(commands -> commands.filter(event))
                .next()
                .flatMap(command -> command.handle(event));
    }

}

