package discord.handler.message;

import discord.handler.EventPredicates;
import discord.model.GuildSettings;
import discord.structure.EmbedBuilder;
import discord.utils.Notificator;
import discord.structure.ChannelRole;
import discord.services.MessageChannelService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

import static discord.localisation.LocalizedMessage.TIP_MESSAGE;
import static discord.localisation.SimpleMessage.TIP_FOOTER_MESSAGE;


@Component
@RequiredArgsConstructor
public class SendTipOnPeriodicalMessageHandler implements MessageHandler {
    private final static int MIN_MESSAGE_LENGTH = 6;

    private final Notificator notificator = new Notificator();

    private final MessageChannelService channelService;
    private final GuildSettings guildSettings;
    private final EventPredicates eventPredicates;

    @Override
    public boolean filter(MessageCreateEvent event) {
        return Stream.of(event)
                .filter(eventPredicates::filterBot)
                .filter(this::messageIsEnoughLarge)
                .filter(e -> notificator.isTime())
                .filter(e -> eventPredicates.filterByChannelId(event, guildSettings.getHelpChannelId()))
                .count() == 1;
    }

    private boolean messageIsEnoughLarge(MessageCreateEvent event) {
        return event.getMessage().getContent().length() > MIN_MESSAGE_LENGTH;
    }

    @Override
    public void handle(MessageCreateEvent event) {
        final var embed = EmbedBuilder.buildTipEmbed(notificator.getNotificationText());
        channelService.getChannel(ChannelRole.HELP).createMessage(embed).subscribe();
    }
}
