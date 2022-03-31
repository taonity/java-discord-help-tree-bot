package discord.listeners;

import discord.Configs;
import discord.commands.SlashCommand;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

@Component
public class SlashCommandListener {

    private final Collection<SlashCommand> commands;
    private final Configs configs;


    public SlashCommandListener(List<SlashCommand> slashCommands, GatewayDiscordClient client, Configs configs) {
        commands = slashCommands;
        this.configs = configs;
        client.on(ChatInputInteractionEvent.class, this::handle).subscribe();
    }

    public Mono<Void> handle(ChatInputInteractionEvent event) {
        //Convert our list to a flux that we can iterate through
        return Flux.fromIterable(commands)
                //Filter out all commands that don't match the name this event is for
                //TODO: want to divide stream
                .filter(command -> command.getName().equals(event.getCommandName()) &&
                        event.getInteraction().getChannel().block().getId().asString().equals(configs.getChannelId()))
                //Get the first (and only) item in the flux that matches our filter
                .next()
                //Have our command class handle all logic related to its specific command.
                .flatMap(command -> command.handle(event));
    }
}

