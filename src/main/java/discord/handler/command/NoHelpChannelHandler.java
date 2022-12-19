package discord.handler.command;

import discord.handler.EventPredicates;
import discord.handler.message.MessageHandler;
import discord.localisation.SimpleMessage;
import discord.services.MessageChannelService;
import discord.structure.ChannelRole;
import discord.structure.CommandName;
import discord.structure.EmbedBuilder;
import discord.structure.EmbedType;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static discord.structure.CommandName.DIALOG;

@Component
@RequiredArgsConstructor
public class NoHelpChannelHandler extends AbstractSlashCommand {

    @Getter
    private final CommandName command = CommandName.ANY;

    private final EventPredicates eventPredicates;

    @Override
    public boolean filter(ChatInputInteractionEvent event) {
        return Stream.of(event)
                .filter(this::filterByCommand)
                .filter(eventPredicates::filterBot)
                .filter(e -> !eventPredicates.filterIfChannelExistsInSettings(e, ChannelRole.HELP))
                .count() == 1;
    }

    @Override
    public void handle(ChatInputInteractionEvent event) {

        event.reply().withEmbeds(EmbedBuilder.buildSimpleMessage(
                SimpleMessage.NO_HELP_CHANNEL_MESSAGE.getMessage(),
                EmbedType.SIMPLE_MESSAGE_EMBED_TYPE
        )).withEphemeral(true).subscribe();

    }
}
