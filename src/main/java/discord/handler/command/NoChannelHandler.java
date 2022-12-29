package discord.handler.command;

import discord.handler.EventPredicates;
import discord.localisation.SimpleMessage;
import discord.structure.ChannelRole;
import discord.structure.CommandName;
import discord.structure.EmbedBuilder;
import discord.structure.EmbedType;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class NoChannelHandler extends AbstractSlashCommand {

    @Getter
    private final CommandName command = CommandName.ANY;

    private final EventPredicates eventPredicates;

    @Override
    public boolean filter(ChatInputInteractionEvent event) {
        return Stream.of(event)
                .filter(this::filterByCommand)
                .filter(eventPredicates::filterBot)
                .filter(e -> !eventPredicates.filterIfChannelsExistInSettings(e))
                .count() == 1;
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {

        event.reply().withEmbeds(EmbedBuilder.buildSimpleMessage(
                SimpleMessage.NO_CHANNEL_MESSAGE.getMessage(),
                EmbedType.SIMPLE_MESSAGE_EMBED_TYPE
        )).withEphemeral(true).withEphemeral(true).subscribe();

    }
}
